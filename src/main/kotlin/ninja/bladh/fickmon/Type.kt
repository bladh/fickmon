package ninja.bladh.fickmon

data class Type(
        val name: String,
        val weakTo: List<String>,
        val resistantTo: List<String>,
        val immuneTo: List<String>
) {
    override fun equals(other: Any?): Boolean {
        if (other is Type) {
            return name == other.name
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    infix fun relationTo(type: Type): Double = relationTo(type.name)

    infix fun relationTo(typeName: String): Double = when (typeName) {
        in weakTo -> 2.0
        in resistantTo -> 0.5
        in immuneTo -> 0.0
        else -> 1.0
    }
}