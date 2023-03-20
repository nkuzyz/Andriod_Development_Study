package lab03_class.app
interface FishAction {
    fun eat()
}
class PrintingFishAction(val food: String) : FishAction {
    override fun eat() {
        println(food)
    }
}
interface FishColor {
    val color: String
}

// FishColor 的帮助程序类
object GoldColor : FishColor {
    override val color = "gold"
}

object GrayColor : FishColor{
    override val color = "gray"
}
class Plecostomus(fishColor: FishColor = GoldColor):
    FishAction by PrintingFishAction("eat algae"),
    FishColor by fishColor

class Shark(fishColor: FishColor = GrayColor):
        FishColor by fishColor,
        FishAction by PrintingFishAction("hunt and eat fish")

