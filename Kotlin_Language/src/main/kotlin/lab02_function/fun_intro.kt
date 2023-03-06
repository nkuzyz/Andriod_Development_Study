import java.util.*

fun feedTheFish() {
    val day = randomDay()
    val food = fishFood(day)
    println ("Today is $day and the fish eat $food")
    println("Change water: ${shouldChangeWater(day)}")
}
fun randomDay() : String {
    val week = arrayOf ("Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday")

    //nextInt() 函数采用整数限制，这会将数字从 Random() 限制到 0 至 6，以与 week 数组匹配。
    return week[Random().nextInt(week.size)]
}
fun fishFood (day : String) : String {
    // 由于每个表达式都具有值，因此您可以使此代码更简洁一些。直接返回 when 表达式的值，并清除 food 变量。
    // when 表达式的值是满足条件的分支的最后一个表达式的值。
    return when (day) {
        "Monday" -> "flakes"
        "Wednesday" -> "redworms"
        "Thursday" -> "granules"
        "Friday" -> "mosquitoes"
        "Sunday" -> "plankton"
        // 使用 else 向 when 表达式添加默认分支。
        else -> "nothing"
    }
}

fun swim(speed: String = "fast") {
    println("swimming $speed")
}
// 如果未为参数指定默认值，必须始终传递相应的参数。
// 一周当中的具体日期是必需参数，但默认温度为 22 度，默认脏污级别为 20。
fun shouldChangeWater (day: String, temperature: Int = 22, dirty: Int = 20): Boolean {
    return when {
        // 在星期天、温度过高或水过脏时
        isTooHot(temperature) -> true
        isDirty(dirty) -> true
        isSunday(day) -> true
        else -> false
    }
}

// 可以在 = 符号后指定该函数的正文，省略大括号 {}，并省略 return。
fun isTooHot(temperature: Int) = temperature > 30

fun isDirty(dirty: Int) = dirty > 30

fun isSunday(day: String) = day == "Sunday"
fun main(args: Array<String>) {
//    println("Hello World!")
//
//    // Try adding program arguments via Run/Debug configuration.
//    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
//    println("Program arguments: ${args.joinToString()}")

    // 在 Kotlin 中，几乎所有内容都是表达式，并且都具有值（即使该值为 kotlin.Unit）。
//    val isUnit = println("This is an expression")
//    println(isUnit)
//    val temperature = 10
//    val isHot = if (temperature > 50) true else false
//    println(isHot)
//    val message = "The water temperature is ${ if (temperature > 50) "too warm" else "OK" }."
//    println(message)

    //详细了解函数
    feedTheFish()
    //when 语句类似于 switch，但 when 会在每个分支结束时自动中断。在您检查枚举的情况下，它还可确保您的代码覆盖所有分支。

    // 探索默认值和紧凑型函数  解函数参数的默认值 减少测试用代码路径的数量

    swim()   // uses default speed 使用默认值调用该函数。
    swim("slow")   // positional argument 调用该函数并传递未命名的 speed 参数
    swim(speed="turtle-like")   // named parameter 命名 speed 参数以调用该函数。

}

