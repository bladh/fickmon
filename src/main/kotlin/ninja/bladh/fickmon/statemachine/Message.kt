package ninja.bladh.fickmon.statemachine

data class Message(val code: Int,
                   val message: String?,
                   val misc: Any?)