package ninja.bladh.fickmon

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.net.URL
import java.util.*

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

    fun readTypes(): List<Type> {
        val typeList: MutableList<Type> = ArrayList()
        val data = read("/types.json")
        data.forEach { json ->
            val weakness = (json["weakTo"] as Iterable<*>? ?: ArrayList<String>()).map { it as String }
            val resistance = (json["resistantTo"] as Iterable<*>? ?: ArrayList<String>()).map { it as String }
            val immune = (json["immuneTo"] as Iterable<*>? ?: ArrayList<String>()).map { it as String }
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
                    id = json["id"] as Int,
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
                    secondaryType = secondary ?: Type("None", ArrayList<String>(), ArrayList<String>(), ArrayList<String>()),
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