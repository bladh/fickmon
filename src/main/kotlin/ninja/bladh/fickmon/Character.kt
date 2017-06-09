package ninja.bladh.fickmon

data class Character(
        var baseClass: Fickmon,
        var experience: Int,
        var nickname: String,
        val trainerId: Long,
        var level: Int,
        var hp: Double,
        var condition: Condition,
        var maxHp: Double,
        var speed: Double,
        var defence: Double,
        var attack: Double,
        var special: Double,
        var moves: List<Move>
)