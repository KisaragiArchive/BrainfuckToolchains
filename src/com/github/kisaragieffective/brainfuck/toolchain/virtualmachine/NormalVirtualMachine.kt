package com.github.kisaragieffective.brainfuck.toolchain.virtualmachine

class NormalVirtualMachine(val sourceCode: String) : IVirtualMachine {
    private var instructionIndex = 0
    private var memptr = 0
    private var memory = IntArray(30_000)
    override fun optimize() {
        // NOP
    }

    override fun execute() {
        while (true) {
            when (sourceCode[instructionIndex]) {
                '+' -> {
                    memory[memptr]++
                }

                '-' -> {
                    memory[memptr]--
                }

                '>' -> memptr++
                '<' -> memptr--
                '.' -> {
                    print(memory[memptr].toChar())
                }

                '[' -> {
                    if (memory[memptr] == 0) {
                        var i = instructionIndex + 1
                        var nest = 1
                        while (true) {
                            if (sourceCode[i] == '[') {
                                nest++
                            } else if (sourceCode[i] == ']') {
                                nest--
                            }
                            if (nest == 0) break
                            i++
                        }
                        instructionIndex = i
                    }
                }

                ']' -> {
                    if (memory[memptr] != 0) {
                        var i = instructionIndex - 1
                        var nest = 1
                        while (true) {
                            if (sourceCode[i] == '[') {
                                nest--
                            } else if (sourceCode[i] == ']') {
                                nest++
                            }
                            if (nest == 0) break
                            i--
                        }
                        instructionIndex = i
                    }
                }
            }
            instructionIndex++
            if (instructionIndex == sourceCode.lastIndex) break
        }

    }
}