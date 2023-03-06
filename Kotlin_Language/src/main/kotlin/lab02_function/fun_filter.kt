
// 默认情况下，filter 是即刻过滤器，并且在您每次使用该过滤器时，系统都会创建一个列表。
//为使过滤器延迟显示，您可以使用 Sequence，这是一种集合，每次仅支持查看一项内容，从开头开始，一直到其结尾。
// 方便的是，这正是延迟过滤器所需的 API。

fun main() {
    val decorations = listOf ("rock", "pagoda", "plastic plant", "alligator", "flowerpot")

    // eager, creates a new list
    val eager = decorations.filter { it [0] == 'p' }
    println("eager: $eager")
    // lazy, will wait until asked to evaluate
    // 当您以 Sequence 形式返回过滤器结果时，filtered 变量不会保存新列表，
    // 而是保存列表元素的 Sequence 以及要应用于这些元素的过滤器信息。
    // 每当您访问 Sequence 的元素时，系统就会应用过滤器，并将结果返回给您。
    val filtered = decorations.asSequence().filter { it[0] == 'p' }
    println("filtered: $filtered")
    // force evaluation of the lazy list
    //使用 toList() 将序列转换为 List，以强制对该序列执行求值。输出结果。
    val newList = filtered.toList()
    println("new list: $newList")

    //输出 lazyMap 仅会输出对 Sequence 的引用，系统不会调用内部 println()。
    // 输出第一个元素仅会访问第一个元素。将 Sequence 转换为 List 可访问所有元素。
    val lazyMap = decorations.asSequence().map {
        println("access: $it")
        it
    }
    println("lazy: $lazyMap") // 输出 lazyMap 仅会输出对 Sequence 的引用 lazy: kotlin.sequences.TransformingSequence@19469ea2
    println("-----")
    println("first: ${lazyMap.first()}") // 输出第一个元素仅会访问第一个元素 access: rock first: rock
    println("-----")
    println("all: ${lazyMap.toList()}") // 将 Sequence 转换为 List 可访问所有元素。

    //与获取第一个元素一样，系统仅会对访问的元素调用内部 println()。
    val lazyMap2 = decorations.asSequence().filter {it[0] == 'p'}.map {
        println("access: $it")
        it
    }
    println("-----")
    println("filtered: ${lazyMap2.toList()}")

    //flatten()。此函数从一系列数组或一系列列表等一系列集合创建列表。
    val mysports = listOf("basketball", "fishing", "running")
    val myplayers = listOf("LeBron James", "Ernest Hemingway", "Usain Bolt")
    val mycities = listOf("Los Angeles", "Chicago", "Jamaica")
    val mylist = listOf(mysports, myplayers, mycities)     // list of lists
    println("-----")
    println("mylist: ${mylist}")
    println("Flat: ${mylist.flatten()}")
}