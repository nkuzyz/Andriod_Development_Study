package com.example.zyzfirstandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

/**
 * This activity allows the user to roll a dice and view the result
 * on the screen.
 */
const val KEY_VALUE = 1
class MainActivity : AppCompatActivity() {
    private var diceRoll = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener { rollDice() }
        if (savedInstanceState != null) {
            diceRoll = savedInstanceState.getInt(KEY_VALUE.toString(), 1)
        }
        showImage(diceRoll)
        Log.i("MainActivity", "onCreate Called")
    }
    /**
     * Roll the dice and update the screen with the result.
     */
    private fun rollDice() {

        // Create new Dice object with 6 sides and roll it
        val dice = Dice(6)
        diceRoll = dice.roll()
        showImage(diceRoll)

    }

    private fun showImage(diceRoll: Int){
        // Update the screen with the dice roll
        val resultTextView02: TextView = findViewById(R.id.textView2)
        resultTextView02.text = diceRoll.toString()

        val diceImage: ImageView = findViewById(R.id.imageView)
        val drawableResource = when (diceRoll) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }
        diceImage.setImageResource(drawableResource)
        diceImage.contentDescription = diceRoll.toString()
    }



    class Dice(private val numSides: Int) {

        fun roll(): Int {
            return (1..numSides).random()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_VALUE.toString(), diceRoll)
    }
}
