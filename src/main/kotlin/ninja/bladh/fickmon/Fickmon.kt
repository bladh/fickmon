package ninja.bladh.fickmon

data class Fickmon(
        val id: Int,
        val name: String,
        val primaryType: Type,
        val secondaryType: Type,
        val frontImagePath: String,
        val backImagePath: String,
        val smallImagePath: String,
        val baseHealth: Double,
        val baseAttack: Double,
        val baseDefence: Double,
        val baseSpeed: Double,
        val baseSpecial: Double,
        val perLevelHealth: Double,
        val perLevelAttack: Double,
        val perLevelDefence: Double,
        val perLevelSpeed: Double,
        val perLevelSpecial: Double,
        val learnedMoves: Map<Int, Move>,
        val evolutionLevel: Int,
        val evolutionMon: String
)