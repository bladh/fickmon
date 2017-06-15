package ninja.bladh.fickmon.battle

import ninja.bladh.fickmon.Character

/**
 * These callbacks will be fired off in rapid succession by the state machine, so keep a queue handy if they are
 * to be displayed in a readable manner!
 */
interface BattleCallbacks {
    /**
     * Display which players turn it is to make a move (in case we have multiple actual players?)
     */
    fun playerTurn(player: Player)

    /**
     * Show a message on the battle screen
     */
    fun message(message: String)

    /**
     * Update the target character (new health value, condition, whatever
     */
    fun update(target: Character)
}