package com.shanemaglangit.dotsandboxes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.shanemaglangit.dotsandboxes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Updates the text view when the scores change
        binding.board.scoreChangedListener = object : ScoreChangedListener {
            override fun scoreChanged(userScore: Int, botScore: Int) {
                binding.textUserScore.text = userScore.toString()
                binding.textBotScore.text = botScore.toString()
            }
        }

        // listen to when the game ends, show a snackbar showing who won
        binding.board.gameEndListener = object: GameEndListener {
            override fun gameEnd(winner: Player?) {
                val message = when(winner) {
                    Player.USER -> "Congratulations! You won"
                    Player.BOT -> "You lost. Better luck next time"
                    else -> "It is a draw"
                }

                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Reset game when the reset button on the toolbar is clicked
        binding.toolbar.setOnMenuItemClickListener {
            binding.board.reset()
            true
        }

        setContentView(binding.root)
    }
}
