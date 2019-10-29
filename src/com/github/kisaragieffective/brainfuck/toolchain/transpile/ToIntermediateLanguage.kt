package com.github.kisaragieffective.brainfuck.toolchain.transpile

import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFAdd
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFBreakingCopy
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFBreakingMultiple
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFInitializeMap
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFInstruction
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFJumpEquals
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFJumpNotEquals
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFLabel
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFLoadConstant
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFLoadZero
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFMinus
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFPtrLeft
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFPtrRight
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFPutChar
import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFReadChar

object ToIntermediateLanguage : ITranscompiler {
    override fun toSourceCode(instruction: BFInstruction): String {
        return when (instruction) {
            is BFAdd -> "ADD ${instruction.amount}"
            is BFLabel -> ":${instruction.idx}"
            is BFMinus -> "MINUS ${instruction.amount}"
            is BFPtrLeft -> "PTR -${instruction.amount}"
            is BFPtrRight -> "PTR ${instruction.amount}"
            BFPutChar -> "PUTCHAR"
            BFReadChar -> "READCHAR"
            is BFJumpEquals -> "JE :${instruction.offset}"
            is BFJumpNotEquals -> "JNE :${instruction.offset}"
            is BFBreakingCopy -> "BCP ${instruction.offset}"
            is BFBreakingMultiple -> "BMPL ${instruction.offset}, ${instruction.times}"
            is BFLoadConstant -> "LCD ${instruction.value}"
            BFLoadZero -> "LCD 0"
            is BFInitializeMap -> "INITMAP ${instruction.relativeIndexToAdd.entries.joinToString(";") { "${it.key}->${it.value}" }}"
        }
    }
}