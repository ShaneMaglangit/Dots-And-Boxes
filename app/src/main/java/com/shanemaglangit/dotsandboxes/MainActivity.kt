package com.shanemaglangit.dotsandboxes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.shanemaglangit.dotsandboxes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Updates the text view when the scores change
        binding.board.scoreChangedListener = object: ScoreChangedListener {
            override fun scoreChanged(userScore: Int, botScore: Int) {
                binding.textUserScore.text = userScore.toString()
                binding.textBotScore.text = botScore.toString()
            }
        }

        setContentView(binding.root)
    }
}
