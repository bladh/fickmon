package ninja.bladh.fickmon

data class Move(
        val id: Int,
        val name: String,
        val message: String,
        val type: Type,
        val isPhysical: Boolean,
        val maxPP: Int,
        val power: Int,
        val accuracy: Int,
        val critical: Int
)