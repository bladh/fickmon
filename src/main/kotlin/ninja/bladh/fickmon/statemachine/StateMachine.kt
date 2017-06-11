package ninja.bladh.fickmon.statemachine

open class StateMachine {
    var iState: State? = null
    var currentState: State? = null
    var started: Boolean = false

    fun setInitialState(state: State) {
        if (!started) {
            iState = state
        } else {
            throw IllegalStateException("Cannot set initial state on a running state machine")
        }
    }

    fun start() {
        if (iState == null) {
            throw IllegalStateException("Must define a initial state before starting state machine")
        }
        currentState = iState
        started = true
        currentState!!.enter() // something is very wrong if we NPE here so throw it
    }

    fun process(message: Message) : Boolean {
        if (currentState == null) {
            throw IllegalStateException("Current state is null")
        }
        return process(message, currentState as State)
    }

    private fun process(message: Message, state: State) : Boolean {
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

    fun transitionTo(state: State) {
        currentState?.leave()
        currentState = state
        currentState?.enter()
    }

    fun stop() {
        currentState?.leave()
    }
}