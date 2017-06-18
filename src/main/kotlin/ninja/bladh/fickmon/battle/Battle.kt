package ninja.bladh.fickmon.battle

import ninja.bladh.fickmon.statemachine.Message
import ninja.bladh.fickmon.statemachine.State
import ninja.bladh.fickmon.statemachine.StateMachine
import java.util.*

private data class TurnInfo(val actions: List<Action>)
private data class TurnChoice(val playerNum: Int, val action: Action)

/**
 * Currently this works so that every player will "take turns" to send in their move, and after everyones turn
 * has been up, we will "resolve" what actually happened.
 *
 * this probably sucks if we want to include network play and would prefer people to be able to submit their turn
 * whenever and have the turn be resolved when everyone sent in their move.
 *
 * however, i find this premature. in the beginning, most likely every battle will take place between two players,
 * one human and one AI.
 */

class Battle(val random: Random, players: List<Player>) : StateMachine() {

    private val playerTurns = players.mapIndexed { index: Int, player: Player ->
        if (player.human) PlayerTurn(index, player) else AITurn(index, player)
    }
    private val main = Main()
    private val resolveState = Resolve()
    private var callbacks: BattleCallbacks? = null

    override fun run() {
        initialState = main
        super.run()
    }

    inner class Main : State(null) {

        val actions: MutableList<Action> = ArrayList()

        override fun enter(message: Message?) {
            val index: Int
            if (message?.message?.equals("CONTINUE") ?: false) {
                val turnChoice: TurnChoice = message?.misc as? TurnChoice ?: throw IllegalStateException(
                        "Invalid turn choice submitted")
                index = turnChoice.playerNum + 1
                actions.add(turnChoice.action)
            } else {
                // if we are not CONTINUING, logic dictates that we are starting over.
                index = 0
                actions.clear()
            }
            if (index > playerTurns.size) {
                // Everybody has sent in their moves
                // Time to resolve
                val turnInfo: TurnInfo = TurnInfo(ArrayList<Action>(actions))
                transitionTo(state = resolveState, message = Message(misc = turnInfo))
            } else {
                transitionTo(playerTurns[index])
            }
        }

        override fun process(message: Message): Boolean {
            // be patient! it's not the time to process any messages yet
            return false
        }
    }

    open inner class PlayerTurn(val playerNum: Int, val player: Player) : State(null) {

        override fun enter(message: Message?) {
            // time to light up the instrument panel, notify the player to make a move
            callbacks?.playerTurn(player)
        }

        override fun process(message: Message): Boolean {
            if (message.message == "submit move") {
                val move: Action = message.misc as? Action ?:
                        throw IllegalArgumentException("Need to include an action when submitting move")
                // the Action should be composed outside of the state machine
                // somewhere between the state machine and the UI (not in the ui that would be stinky)
                val turnChoice = TurnChoice(playerNum, move)
                transitionTo(state = main, message = Message(misc = turnChoice, message = "CONTINUE"))
                return true
            }
            return false
        }
    }

    inner class AITurn(num: Int, player: Player) : PlayerTurn(num, player) {

        override fun enter(message: Message?) {
            // this is where the AI magic would happen
            // calculate the Action and immediately submit it
        }

        override fun process(message: Message): Boolean {
            // not processing any messages during AI turn
            // we are doing our own thing
            return false
        }
    }

    inner class Resolve : State(null) {

        override fun enter(message: Message?) {
            val turnActions: List<Action> = (message?.misc as? TurnInfo)?.actions ?:
                    throw IllegalStateException("Entered resolve state without actions to resolve")
            turnActions
                    .sortedBy { it.speedModifier }
                    .forEach { resolve(it) }
        }

        override fun process(message: Message): Boolean {
            // we want to know when the UI has finished displaying everything that's going on
            // and after this, we move on (we wont process anything until our 'enter' stage has finished anyway)
            if (message.message == "UI-OK") {
                transitionTo(state=main) // supplying no message means we start the next battle turn
                return true
            }
            return false
        }

        /*
        todo: currently only damaging targeted moves are covered
         */
        private fun resolve(action: Action) {
            if (action.perpetrator.hp < 1) {
                // character who would make a move has been defeated
                return
            }
            if (!action.action.message.isEmpty()) {
                callbacks?.message(action.action.message)
            }
            if (action.target != null) {
                // we are attacking something

                val typeMultiplier = (action.target.baseClass.primaryType relationTo action.action.type) *
                        (action.target.baseClass.secondaryType.relationTo(action.action.type))
                if (typeMultiplier == 0.0) {
                    callbacks?.message("${action.target.nickname} is immune!")
                } else {
                    val typeBonus = if (action.perpetrator.baseClass.primaryType == action.action.type
                            || action.perpetrator.baseClass.secondaryType == action.action.type)
                        1.5
                    else
                        1.0

                    val critical = if (random.nextInt(100) <= action.action.critical) 2.0 else 1.0

                    val modifier = calculateModifier(
                            typeMultiplier = typeMultiplier,
                            critical = critical,
                            typeBonus = typeBonus
                    )
                    val damage = calculateDamage(
                            level = action.perpetrator.level,
                            power = action.action.power,
                            attack = if (action.action.isPhysical) action.perpetrator.attack else action.perpetrator.special,
                            defence = if (action.action.isPhysical) action.target.defence else action.target.special,
                            modifier = modifier,
                            critical = critical
                    )
                    action.target.hp -= damage
                    callbacks?.update(action.target)
                    if (critical > 1.0) {
                        callbacks?.message("Critical hit!")
                    }
                    if (typeBonus > 2.0) {
                        callbacks?.message("Its super effective!")
                    } else if (typeBonus < 1.0) {
                        callbacks?.message("It's not very effective...")
                    }
                }
            }

        }

        private fun calculateModifier(typeMultiplier: Double, critical: Double, typeBonus: Double): Double {
            return typeMultiplier * critical * typeBonus
        }

        private fun calculateDamage(level: Int, power: Int, attack: Double, defence: Double, modifier: Double, critical: Double): Double {
            val damage =
                    (((((2.0 * level * critical) / 5.0) + 2) * power * attack / defence) / 50) * modifier
            return damage
        }

    }
}