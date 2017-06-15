package ninja.bladh.fickmon.statemachine

abstract class State(val parentState: State?) {
    open fun enter(message: Message? = null) {}
    open fun leave() {}
    /**
     * Return true if the message was handled by this state
     * Return false if not, and a potential parent state will attempt to handle it
     */
    abstract fun process(message: Message): Boolean
}