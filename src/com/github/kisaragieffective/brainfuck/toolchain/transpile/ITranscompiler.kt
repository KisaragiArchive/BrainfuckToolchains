package com.github.kisaragieffective.brainfuck.toolchain.transpile

import com.github.kisaragieffective.brainfuck.toolchain.instruction.BFInstruction

interface ITranscompiler {
    fun toSourceCode(instruction: BFInstruction): String
}