package ninja.bladh.fickmon.statemachine

data class Message(val code: Int = 0,
                   val message: String? = null,
                   val misc: Any? = null)