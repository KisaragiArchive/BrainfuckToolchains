package com.github.kisaragieffective.brainfuck.toolchain

import com.github.kisaragieffective.brainfuck.toolchain.virtualmachine.JITCompileMachine1
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureNanoTime

fun main() {
    val nanos = measureNanoTime {
        val env = JITCompileMachine1((URL("https://raw.githubusercontent.com/kostya/benchmarks/master/brainfuck/mandel.b").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connect()
        }.inputStream.bufferedReader().lineSequence().joinToString(""))
        env.optimize()
        env.dumpCode()
        val ex = measureNanoTime {
            env.execute()
        }
        println("Time: $ex ns")
    }
    println()
    println()
    println("-----------------")
    println("$nanos nano seconds.")

}