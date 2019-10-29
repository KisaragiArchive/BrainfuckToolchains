package com.github.kisaragieffective.bfvm

import com.github.kisaragieffective.bfvm.instruction.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

var errors = 0

fun main() {
    // `-+><,.[]`以外の8文字は無視しても差し支えない
    val m = "++++++++[>++++++++<-]>+."
    debug(m)
    debug("m.len is ${m.length}")
    // exitProcess(0)
    var eatenIndex = 0
    val instructions = mutableListOf<BFInstruction>()
    val squareBrackets = mutableSetOf<Int>()
    eater@ while (eatenIndex <= m.lastIndex) {
        var p: Regex
        var mr: MatchResult?
            p = Regex("\\[-](?<value>\\+*+)")
            mr = p.find(m, eatenIndex)
            if (mr != null && eatenIndex == mr.range.first) { // found
                debug("HIT LDC!")
                instructions += BFLoadConstant(mr.groups["value"]!!.value.length)
                eatenIndex += mr.range.last - mr.range.first
                continue@eater
            }



            // [>+<-]
            p = Regex("\\[(?<rmv>>)\\+(?<lmv><)-\\]")
            mr = p.find(m, eatenIndex)
            if (mr != null) { // found
                if (mr.groups["rmv"] == mr.groups["lmv"]) {
                    debug("HIT BCPY!")
                    instructions += BFBreakingCopy(mr.groups["rmv"]!!.value.length)
                    eatenIndex += mr.range.last - mr.range.first
                    continue@eater
                }
            }



            // [<+>-]
            p = Regex("\\[(?<lmv><)\\+(?<rmv>>)-\\]")
            mr = p.find(m, eatenIndex)
            if (mr != null) { // found
                if (mr.groups["rmv"] != mr.groups["lmv"]) {
                    debug("HIT BCPY!")
                    instructions += BFBreakingCopy(-mr.groups["rmv"]!!.value.length)
                    eatenIndex += mr.range.last - mr.range.first
                    continue@eater
                }
            }



            // [>+++++++<-]
            p = Regex("\\[(?<rmv>>)(?<mult>\\+)(?<lmv><)-\\]")
            mr = p.find(m, eatenIndex)
            if (mr != null) { // found
                val groups = mr.groups
                if (groups["rmv"] != groups["lmv"]) {
                    instructions += BFBreakingMultiple(groups["rmv"]!!.value.length, groups["mult"]!!.value.length)
                    eatenIndex += mr.range.last - mr.range.first
                    continue@eater
                }
            }



        // [<+++++++>-]
        p = Regex("\\[(?<lmv><)(?<mult>\\+)(?<rmv>>)-\\]")
        mr = p.find(m, eatenIndex)
        if (mr != null) { // found
            val groups = mr.groups
            if (groups["rmv"] != groups["lmv"]) {
                debug("HIT BMUL!")
                instructions += BFBreakingMultiple(-groups["rmv"]!!.value.length, groups["mult"]!!.value.length)
                eatenIndex += mr.range.last - mr.range.first
                continue@eater
            }
        }


        // +
        p = Regex("\\++")
        mr = p.find(m, eatenIndex)
        if (mr != null && mr.range.first == eatenIndex) { // found
            val len = mr.range.last - mr.range.first + 1
            instructions += BFAdd(len)
            eatenIndex += len
            continue@eater
        }

        // -
        p = Regex("-+")
        mr = p.find(m, eatenIndex)
        if (mr != null && mr.range.first == eatenIndex) { // found
            val len = mr.range.last - mr.range.first + 1
            instructions += BFMinus(len)
            eatenIndex += len
            continue@eater
        }



        // >
        p = Regex(">+")
        mr = p.find(m, eatenIndex)
        if (mr != null && mr.range.first == eatenIndex) { // found
            val len = mr.range.last - mr.range.first + 1
            instructions += BFPtrRight(len)
            eatenIndex += len
            continue@eater
        }



        // <
        p = Regex("<+")
        mr = p.find(m, eatenIndex)
        if (mr != null && mr.range.first == eatenIndex) { // found
            val len = mr.range.last - mr.range.first + 1
            instructions += BFPtrLeft(len)
            eatenIndex += len
            continue@eater
        }



        // ,
        p = Regex(",+")
        mr = p.find(m, eatenIndex)
        if (mr != null && mr.range.first == eatenIndex) { // found
            val len = mr.range.last - mr.range.first + 1
            if (len != 1) {
                warn("You used `,` operator multiple times, but it has no effects.")
            }
            instructions += BFReadChar
            eatenIndex += 1 // 複数回同じ番地に同じ値を読み込んでも意味ないのでは？
            continue@eater
        }

        // .
        p = Regex("\\.+")
        mr = p.find(m, eatenIndex)
        if (mr != null && mr.range.first == eatenIndex) { // found
            val len = mr.range.last - mr.range.first + 1
            for (_z in 1..len) {
                instructions += BFPutChar
            }
            eatenIndex += len
            continue@eater
        }



        // [
        var sbidx = m.indexOf('[', eatenIndex)
        if (sbidx != -1 && sbidx !in squareBrackets) {
            var nest = 0
            var lastBreak = -1
            for (j in sbidx until m.length) {
                if (m[j] == '[') {
                    nest++
                } else if (m[j] == ']') {
                    nest--
                }
                if (nest == 0) {
                    lastBreak = j
                    break
                }
            }

            if (lastBreak == -1) {
               error("Square brackets mismatch: `]` * $nest near $eatenIndex")
            }
            instructions += BFLabel(sbidx)
            instructions += BFJumpEquals(lastBreak)
            eatenIndex++
            squareBrackets += sbidx
            continue@eater
        }



        // ]
        sbidx = m.indexOf(']', eatenIndex)
        debug("sbidx is ${sbidx}, i is $eatenIndex, in? is ${sbidx !in squareBrackets}")
        if (sbidx != -1) {
            var nest = 0
            var lastBreak = -1
            debug("`]` detected: $sbidx")
            for (j in sbidx downTo 0) {
                if (m[j] == ']') {
                    nest++
                } else if (m[j] == '[') {
                    nest--
                }
                if (nest == 0) {
                    lastBreak = j
                    break
                }
            }

            if (lastBreak == -1) {
                error("Square brackets mismatch: `[` * $nest near $eatenIndex")
            }

            if (sbidx !in squareBrackets) {
                instructions += BFLabel(sbidx)
                instructions += BFJumpNotEquals(lastBreak)

                squareBrackets += sbidx
            }
            eatenIndex++
            continue@eater
        }

        debug("remain is ${m.slice(eatenIndex..(m.lastIndex))}")
    }

    File("./c.out").writeText(compileToC(instructions.optimize()))
}

fun List<BFInstruction>.optimize(): List<BFInstruction> {
    val ret = LinkedList<BFInstruction>()
    var i = 0
    while (i <= this.lastIndex) {
        if (i + 3 <= this.lastIndex) {
            if (this.slice(i..(i+3)).map { it::class.java } == listOf(BFPtrRight::class.java, BFAdd::class.java, BFPtrLeft::class.java, BFMinus::class.java)) {
                val sliced = this.slice(i..(i+3))
                var (r, a, l, m) = sliced
                r = r as BFPtrRight
                a = a as BFAdd
                l = l as BFPtrLeft
                m = m as BFMinus
                if (r.amount == l.amount && m.amount == 1) {
                    if (a.amount == 1) {
                        debug("HIT BCPY")
                        ret += BFBreakingCopy(r.amount)
                        i += 4
                        continue
                    } else if (a.amount > 1) {
                        debug("HIT BMPL")
                        ret += BFBreakingMultiple(r.amount, a.amount)
                        i += 4
                        continue
                    }
                }
            }
        }


        ret += this[i]
        i++
    }

    return ret
}

fun warn(mes: String) {
    println("[WARN] $mes")
}

fun getIR(instruction: BFInstruction): String {
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

fun getCStyle(instruction: BFInstruction): String {
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
            instruction.relativeIndexToAdd.entries.map {
                "mem[idx + ${it.key}] += ${it.value};"
            }.joinToString("\n")
        }
    }
}

fun compileToIR(instructions: List<BFInstruction>): String {
    return """
        |${instructions.joinToString("\n") { getIR(it) }}
    """.trimMargin()
}

fun compileToC(instructions: List<BFInstruction>): String {
    return """
    |#include <stdio.h>
    |#include <stdlib.h>
    |#include <stdint.h>
    |
    |int readchar(void) __attribute__((noinline));
    |int readchar(void)
    |{
    |    int c = getchar();
    |    if (c == EOF) {
    |         exit(0);
    |    }
    |    return c;
    |}
    |
    |void writechar(int c) __attribute__((noinline));
    |void writechar(int c)
    |{
    |    putchar(c);
    |    fflush(stdout);
    |}
    |
    |int main(void)
    |{
    |    uint8_t mem[30000] = {0};
    |    uint32_t idx = 0;
    |${instructions.joinToString("\n") { bfInstruction ->
        "    " + getCStyle(bfInstruction) 
        }
    }
    |}
    """.trimMargin()
}

fun error(mes: String) {
    println("[ERROR] $mes")
    errors++
}

fun debug(mes: String) {
    println("[DEBUG] $mes")
}

fun checkGithub() = (URL("https://raw.githubusercontent.com/kostya/benchmarks/master/brainfuck/mandel.b").openConnection() as HttpURLConnection).apply {
    requestMethod = "GET"
    connect()
}.inputStream.bufferedReader().lineSequence().joinToString("").replace(Regex("[^-+,.><\\[\\]]+"), "")

inline fun handleRegex(regex: String, f: () -> Unit) {
    val rex = Regex(regex)
    f()
}