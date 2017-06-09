package ninja.bladh.fickmon

/*
The "statAffectKind" variable refers to if the stat change should be multiplicative, additive, or setting a new value.
0 = additive
1 = multiplicative
2 = set
 */
data class Condition(
        val name: String,
        val short: String,
        val message: String,
        val type: Type,
        val ownTypeResists: Boolean,
        val canOverrideCondition: Boolean,
        val persistAfterBattle: Boolean,
        val affectsStat: List<String>,
        val statAffectAmount: Double,
        val statAffectKind: Int,
        val hpLostPerTurn: Double,
        val hpLostKind: Int,
        val inhibitsMoves: Boolean,
        val selfCureInTurns: Int,
        val opponentHpLostPerTurn: Double,
        val opponentHpLostKind: Int,
        val resistType: List<String>,
        val weakType: List<String>,
        val immuneType: List<String>
) {
    override fun equals(other: Any?): Boolean {
        if (other is Condition) {
            return name == other.name
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
