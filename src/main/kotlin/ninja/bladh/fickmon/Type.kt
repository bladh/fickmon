package ninja.bladh.fickmon

data class Type(
        val id: Int,
        val name: String,
        val weakTo: Array<Int>,
        val resistantTo: Array<Int>,
        val immuneTo: Array<Int>
)