package ninja.bladh.fickmon.battle

import ninja.bladh.fickmon.Character
import ninja.bladh.fickmon.Move

data class Action(val perpetrator: Character, val action: Move, val speedModifier: Double, val target: Character? = null)