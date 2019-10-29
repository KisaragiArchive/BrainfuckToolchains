package com.github.kisaragieffective.bfvm.instruction

sealed class BFInstruction

class BFAdd(val amount: Int = 1) : BFInstruction()

class BFMinus(val amount: Int = 1) : BFInstruction()

class BFPtrLeft(val amount: Int = 1) : BFInstruction()

class BFPtrRight(val amount: Int = 1) : BFInstruction()

object BFPutChar : BFInstruction()

object BFReadChar : BFInstruction()

class BFJumpEquals(val offset: Int) : BFInstruction()

class BFJumpNotEquals(val offset: Int) : BFInstruction()

class BFLabel(val idx: Int) : BFInstruction()

class BFBreakingCopy(val offset: Int) : BFInstruction()

class BFBreakingMultiple(val offset: Int, val times: Int) : BFInstruction()

sealed class ABFLoadConstant(val value: Int) : BFInstruction()

class BFLoadConstant(value: Int) : ABFLoadConstant(value)

object BFLoadZero : ABFLoadConstant(0)

/**
 * BFInitializeMap(mapOf(-1 to 3, 2 to 4, 3 to 5)) // <+++>>>++++>+++++
 */
class BFInitializeMap(val relativeIndexToAdd: Map<Int, Int>) : BFInstruction()