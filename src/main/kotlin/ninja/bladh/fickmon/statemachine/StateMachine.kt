package ninja.bladh.fickmon.statemachine

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

private class TransitionMessage(val state: State, val message: Message?)

open class StateMachine : Runnable {

    companion object {
        private val TRANSITION: Int = 333
    }

    private var started: Boolean = false
    private val messageQueue: BlockingQueue<Message> = LinkedBlockingQueue()
    protected var initialState: State? = null
    set(value) {
        if (!started) {
            field = value
        } else {
            throw IllegalStateException("Cannot set initial state on a running state machine")
        }
    }

    override fun run() {
        var currentState = initialState ?:
                throw IllegalStateException("Must define a initial state before starting state machine")
        started = true
        currentState.enter(null)
        while (started) {
            val message = messageQueue.poll()
            var handled = false // looks stupid, but kept in case i want to handle other messages internally
            if (message.code == TRANSITION) {
                val transitionMessage = message.misc as? TransitionMessage
                if (transitionMessage != null) {
                    currentState.leave()
                    currentState = transitionMessage.state
                    currentState.enter(transitionMessage.message)
                    handled = true
                }
            }

            if (!handled) {
                process(message, currentState)
            }
        }
        currentState.leave()
    }

    /**
     * Send a Message to be processed by the current state in the state machine (or by any of its parents)
     *
     * @param message: Message to be processed
     */
    fun process(message: Message) {
        messageQueue.put(message)
    }

    private fun process(message: Message, state: State): Boolean {
        var handled = state.process(message)
        if (!handled) {
            if (state.parentState != null) {
                handled = process(message, state.parentState)
            } else {
                return false
            }
        }
        return handled
    }

    /**
     * Transition to a different state, possibly delivering a starting message
     *
     * @param state: State to transition to.
     * @param clearQueue: True if we want to clear the message queue and transition now,
     *                    False if we want to wait for other messages to process
     * @param message: Optional message to deliver upon transition
     */
    protected fun transitionTo(state: State, clearQueue: Boolean = true, message: Message? = null) {
        if (clearQueue) {
            messageQueue.clear()
        }
        process(Message(
                code = TRANSITION,
                misc = TransitionMessage(state, message)
        ))
    }

    /**
     * Stops the state machine, but allowing it to finish what it's doing.
     */
    fun stop() {
        started = false
    }
}