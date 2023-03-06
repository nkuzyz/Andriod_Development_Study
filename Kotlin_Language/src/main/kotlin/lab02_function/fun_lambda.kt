

//参数（及其类型，如果需要）位于所谓的函数箭头 -> 的左侧。
// 要执行的代码位于该函数箭头的右侧。一旦将 lambda 赋给变量，就可像调用函数一样调用它。
fun updateDirty(dirty: Int, operation: (Int) -> Int): Int {
    return operation(dirty)
}
fun main(args: Array<String>){
    println("Program arguments: ${args.joinToString()}")
    //创建一个名为 waterFilter 的变量。
    //waterFilter 可以是任何采用 Int 并返回 Int 的函数。
    //将 lambda 赋给 waterFilter。
    //lambda 会返回参数 dirty 除以 2 所得到的值。
    var dirtyLevel = 20
    val waterFilter: (Int) -> Int = { dirty -> dirty / 2 }
    println(waterFilter(dirtyLevel))
    //lambda 的真正强大之处在于：它们用于创建高阶函数，其中，一个函数的参数是另一个函数。
    println(updateDirty(30, waterFilter))
    //如需将该参数指定为常规函数，请使用 :: 运算符。
    //  这样，Kotlin 就能知道您将函数引用作为参数传递，而不是尝试调用该函数。
    // 紧凑型函数
    fun increaseDirty( start: Int ) = start + 1

    println(updateDirty(15, ::increaseDirty))

}