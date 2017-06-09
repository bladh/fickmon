package ninja.bladh.fickmon

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.net.URL
import java.util.*

val noType: Type = Type("None", ArrayList<String>(), ArrayList<String>(), ArrayList<String>())

class JsonReader {
    private fun read(resourceFile: String): JsonArray<JsonObject> {
        val url: URL? = JsonReader::class.java.getResource(resourceFile)
        if (url != null) {
            return Parser().parse(url.openStream()) as JsonArray<JsonObject>
        } else {
            System.err.println("$resourceFile could not be found!")
            return JsonArray()
        }
    }

    private fun getMove(moveName: String, moveList: List<Move>): Move {
        return moveList.filter { it.name == moveName }.first()
    }

    private fun readStrings(value: String, obj: JsonObject): List<String> {
        return (obj[value] as? Iterable<*> ?: ArrayList<String>()).map { it as String }
    }

    fun readTypes(): List<Type> {
        val typeList: MutableList<Type> = ArrayList()
        val data = read("/types.json")
        data.forEach { json ->
            val weakness = readStrings("weakTo", json)
            val resistance = readStrings("resistantTo", json)
            val immune = readStrings("immuneTo", json)
            typeList.add(Type(
                    name = json["name"] as String,
                    weakTo = weakness,
                    resistantTo = resistance,
                    immuneTo = immune
            ))
        }
        return typeList
    }

    fun readMoves(types: List<Type>): List<Move> {
        val moveList: MutableList<Move> = ArrayList()
        val data = read("/moves.json")
        data.forEach { json ->
            val type: Type = types.filter { it.name == (json["type"] as String) }.first()
            moveList.add(Move(
                    name = json["name"] as String,
                    message = json["message"] as? String ?: "",
                    isPhysical = json["isPhysical"] as Boolean,
                    maxPP = json["maxPP"] as Int,
                    power = json["power"] as Int,
                    critical = json["critical"] as Int,
                    accuracy = json["accuracy"] as Int,
                    type = type
            ))

        }
        return moveList
    }

    fun readConditions(types: List<Type>): List<Condition> {
        val conditionList: MutableList<Condition> = ArrayList()
        val data = read("/conditions.json")
        data.forEach { json ->
            val type = types.filter { it.name == json["type"] }.firstOrNull() ?: noType
            val affectStats: List<String> = readStrings("affectStats", json)
            val resistType: List<String> = readStrings("resistType", json)
            val weakType: List<String> = readStrings("weakType", json)
            val immuneType: List<String> = readStrings("immuneType", json)
            conditionList.add(Condition(
                    type = type,
                    name = json["name"] as String,
                    short = json["short"] as String,
                    message = json["message"] as String,
                    ownTypeResists = json["ownTypeResists"] as? Boolean ?: false,
                    canOverrideCondition = json["canOverrideCondition"] as? Boolean ?: false,
                    persistAfterBattle = json["persistAfterBattle"] as? Boolean ?: false,
                    hpLostPerTurn = json["hpLostPerTurn"] as? Double ?: 0.0,
                    hpLostKind = json["hpLostKind"] as? Int ?: 0,
                    statAffectAmount = json["statAffectAmount"] as? Double ?: 0.0,
                    statAffectKind = json["statAffectKind"] as? Int ?: 0,
                    opponentHpLostPerTurn = json["opponentLostPerTurn"] as? Double ?: 0.0,
                    opponentHpLostKind = json["opponentHpLostKind"] as? Int ?: 0,
                    inhibitsMoves = json["inhibitsMoves"] as? Boolean ?: false,
                    selfCureInTurns = json["selfCureInTurns"] as? Int ?: 0,
                    affectsStat = affectStats,
                    resistType = resistType,
                    weakType = weakType,
                    immuneType = immuneType
            ))
        }
        return conditionList
    }

    fun readFickmon(types: List<Type>, moves: List<Move>): List<Fickmon> {
        val monList: MutableList<Fickmon> = ArrayList()
        val data = read("/fickmon.json")
        data.forEach { json ->
            val primary: Type = types.filter { it.name == json["primaryType"] }.first()
            val secondary: Type? = types.filter { it.name == json["secondaryType"] }.first()
            val learnedMovesJson = (json["learnedMoves"] as Iterable<*>).map { it as JsonObject }
            val learnedMoves = learnedMovesJson.map { it["level"] as Int to getMove(it["move"] as String, moves) }.toMap()
            monList.add(Fickmon(
                    id = json["id"] as Int,
                    name = json["name"] as String,
                    primaryType = primary,
                    secondaryType = secondary ?: noType,
                    frontImagePath = json["frontImagePath"] as String,
                    backImagePath = json["backImagePath"] as String,
                    smallImagePath = json["smallImagePath"] as String,
                    baseHealth = json["baseHealth"] as Double,
                    baseAttack = json["baseAttack"] as Double,
                    baseDefence = json["baseDefence"] as Double,
                    baseSpeed = json["baseSpeed"] as Double,
                    baseSpecial = json["baseSpecial"] as Double,
                    perLevelHealth = json["perLevelHealth"] as Double,
                    perLevelAttack = json["perLevelAttack"] as Double,
                    perLevelDefence = json["perLevelDefence"] as Double,
                    perLevelSpeed = json["perLevelSpeed"] as Double,
                    perLevelSpecial = json["perLevelSpecial"] as Double,
                    evolutionLevel = json["evolutionLevel"] as? Int ?: 0,
                    evolutionMon = json["evolutionMon"] as? String ?: "",
                    learnedMoves = learnedMoves
            ))
        }
        return monList
    }
}

fun main(args: Array<String>) {
    val reader: JsonReader = JsonReader()
    val types = reader.readTypes()
    val moves = reader.readMoves(types)
    val mons = reader.readFickmon(types, moves)
    /*
    types.sortedBy(Type::name)
            .forEach { type ->
        val strength = types.filter { type.name in it.weakTo }.map(Type::name)
        val ineffective = types.filter { type.name in it.immuneTo || type.name in it.resistantTo }.map { it.name }
        System.out.println("${type.name}\nSuper effective against $strength\nNot good against $ineffective\nWeak to ${type.weakTo}\nResists ${type.resistantTo}\nImmune to ${type.immuneTo}\n------")
    }
    */
    mons.forEach { System.out.println(it) }
}