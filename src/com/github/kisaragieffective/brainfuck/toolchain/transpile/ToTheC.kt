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

object ToTheC : ITranscompiler {
    override fun toSourceCode(instruction: BFInstruction): String {
        return when (instruction) {
            is BFAdd -> "mem[idx] += ${instruction.amount};"
            is BFLabel -> "// :${instruction.idx} is here"
            is BFMinus -> "mem[idx] -= ${instruction.amount};"
            is BFPtrLeft -> "idx -= ${instruction.amount};"
            is BFPtrRight -> "idx += ${instruction.amount};"
            BFPutChar -> "writechar(mem[idx]);"
            BFReadChar -> "readchar();"
            is BFJumpEquals -> "while(mem[idx]) { //JE :${instruction.offset}"
            is BFJumpNotEquals -> "} //JNE :${instruction.offset}"
            is BFBreakingCopy -> "//BCPY BEGIN\nmem[idx+${instruction.offset}] = mem[idx];\nmem[idx] = 0;\n//BCPY END"
            is BFBreakingMultiple -> "//BMPL BEGIN\nmem[idx+${instruction.offset}] = mem[idx] * ${instruction.times};\nmem[idx] = 0;\n//BCPY END"
            is BFLoadConstant -> "mem[idx] = ${instruction.value};"
            BFLoadZero -> "mem[idx] = 0;"
            is BFInitializeMap -> {
                instruction.relativeIndexToAdd.entries.joinToString("\n") {
                    "mem[idx + ${it.key}] += ${it.value};"
                }
            }
        }
    }
}