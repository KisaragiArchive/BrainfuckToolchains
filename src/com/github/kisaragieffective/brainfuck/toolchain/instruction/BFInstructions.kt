package com.github.kisaragieffective.brainfuck.toolchain.instruction

/**
 * 命令の基底クラス。
 * 無制限な外部拡張の抑制とwhenのパターンマッチを網羅していることを静的に証明するためにsealed classになっている。
 */
sealed class BFInstruction

/**
 * `+`に対応する。
 * 番地ポインタが指す値を`amount`だけ増やす。
 */
class BFAdd(val amount: Int = 1) : BFInstruction()

/**
 * `-`に対応する。
 * 番地ポインタが指す値を`amount`だけ減らす。
 */
class BFMinus(val amount: Int = 1) : BFInstruction()

/**
 * `<`に対応する。
 * 番地ポインタを`amount`だけ減らす。
 * 減らした結果、0未満になった場合の動作は処理系定義である。
 */
class BFPtrLeft(val amount: Int = 1) : BFInstruction()

/**
 * `>`に対応する。
 * 番地ポインタを`amount`だけ増やす。
 */
class BFPtrRight(val amount: Int = 1) : BFInstruction()

/**
 * `.`に対応する。
 * 番地ポインタが指す値をASCIIコードとして解釈し、標準出力にその文字を出力する。
 * この命令は自動的に改行しない。
 */
object BFPutChar : BFInstruction()

/**
 * `,`に対応する。
 * 番地ポインタが指す値を標準入力から1文字読み込み、それのASCIIコードとして再解釈したものに変更する。
 * その後、標準入力の文字ポインタを一つ進める。
 */
object BFReadChar : BFInstruction()

/**
 * `[`に対応する。
 */
class BFJumpEquals(val offset: Int) : BFInstruction()

/**
 * `]`に対応する。
 */
class BFJumpNotEquals(val offset: Int) : BFInstruction()

/**
 * ジャンプで使用するラベル。
 * 現状使いみちはない。
 */
class BFLabel(val idx: Int) : BFInstruction()

/**
 * 現在指している値を`offset`だけずれた番地にムーブしたあと、現在の番地の値を0にする。
 */
class BFBreakingCopy(val offset: Int) : BFInstruction()

/**
 * 現在指している値を`offset`だけずれた番地にムーブしたあと、その値を`times`倍して代入し直し、現在の番地の値を0にする。
 */
class BFBreakingMultiple(val offset: Int, val times: Int) : BFInstruction()

sealed class ABFLoadConstant(val value: Int) : BFInstruction()

/**
 * 番地ポインタが指している値を`value`に変更する。
 */
class BFLoadConstant(value: Int) : ABFLoadConstant(value)

/**
 * 番地ポインタが指している値を0に変更する。
 */
object BFLoadZero : ABFLoadConstant(0)

/**
 * 現在の番地からマップのキーの数だけ相対的にずれた番地に、対応するバリューを増減させる。
 * 例:
 * ```
 * BFInitializeMap(mapOf(-1 to 3, 2 to 4, 3 to 5)) // <+++>>>++++>+++++
 * ```
 */
class BFInitializeMap(val relativeIndexToAdd: Map<Int, Int>) : BFInstruction()