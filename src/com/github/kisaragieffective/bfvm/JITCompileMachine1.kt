package com.github.kisaragieffective.bfvm

import com.github.kisaragieffective.bfvm.instruction.BFAdd
import com.github.kisaragieffective.bfvm.instruction.BFInstruction
import com.github.kisaragieffective.bfvm.instruction.BFJumpEquals
import com.github.kisaragieffective.bfvm.instruction.BFJumpNotEquals
import com.github.kisaragieffective.bfvm.instruction.BFMinus
import com.github.kisaragieffective.bfvm.instruction.BFPtrLeft
import com.github.kisaragieffective.bfvm.instruction.BFPtrRight
import com.github.kisaragieffective.bfvm.instruction.BFPutChar
import com.github.kisaragieffective.bfvm.instruction.BFReadChar

class JITCompileMachine1(val sourceCode: String) : IVirtualMachine {
    val o = ArrayList<BFInstruction>(sourceCode.length)
    var called = false
    private var memptr = 0
    private var memory = IntArray(30_000)

    override fun execute() {
        optimize()
        var i = 0
        while (true) {
            when (val it = o[i]) {
                is BFAdd -> memory[memptr] += it.amount
                is BFMinus -> memory[memptr] -= it.amount
                is BFPtrLeft -> memptr -= it.amount
                is BFPtrRight -> memptr += it.amount
                is BFJumpEquals -> i += it.offset
                is BFJumpNotEquals -> i += it.offset
                is BFPutChar -> {
                    print(memory[memptr])
                    print(memory[memptr].toChar())
                }
            }
            i++
            if (i >= o.size - 1) break
        }
    }

    override fun optimize() {
        if (called) return
        var instructionIndex = 0
        while (true) {

            when (sourceCode[instructionIndex]) {
                '+' -> o += BFAdd()
                '-' -> o += BFMinus()
                '>' -> o += BFPtrRight()
                '<' -> o += BFPtrLeft()
                '.' -> o += BFPutChar

                '[' -> {
                    run {
                        var offset = 1
                        var nest = 1
                        // println("Start: $instructionIndex")

                        while (true) {
                            if (sourceCode[instructionIndex + offset] == '[') {
                                nest++
                            } else if (sourceCode[instructionIndex + offset] == ']') {
                                nest--
                            }
                            if (nest == 0) break
                            offset++
                        }
                        val finalOfs = offset + 1
                        // println("Jump: ${instructionIndex + finalOfs}")
                        o += BFJumpEquals(finalOfs)
                    }
                }

                ']' -> {
                    run {
                        var i = -1
                        var nest = 1
                        // println("Start: $instructionIndex")
                        while (true) {
                            if (sourceCode[instructionIndex + i] == '[') {
                                nest--
                            } else if (sourceCode[instructionIndex + i] == ']') {
                                nest++
                            }
                            if (nest == 0) break
                            i--
                        }
                        val finalOfs = i - 1
                        // println("End: ${instructionIndex + finalOfs}")
                        o += BFJumpNotEquals(-finalOfs)
                    }
                }
            }
            instructionIndex++
            if (instructionIndex >= sourceCode.length) break
        }
    }

    fun dumpCode() {
        o.forEach {
            val out = when (it) {
                is BFAdd -> "ADD ${it.amount}"
                is BFMinus -> "MINUS ${it.amount}"
                is BFPtrLeft -> "PTR_DEC ${it.amount}"
                is BFPtrRight -> "PTR_INC ${it.amount}"
                is BFPutChar -> "PUTCHAR"
                is BFReadChar -> "READCHAR"
                is BFJumpEquals -> {
                    val sgn = when {
                        it.offset > 0 -> "+"
                        else -> ""
                    }
                    "JE $sgn${it.offset}"
                }
                is BFJumpNotEquals -> {
                    val sgn = when {
                        it.offset > 0 -> "+"
                        else -> ""
                    }
                    "JNE $sgn${it.offset}"
                }
                else -> ""
            }

            println(out)
        }
    }
}