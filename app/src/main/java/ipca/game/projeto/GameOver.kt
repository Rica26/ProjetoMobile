package ipca.game.projeto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class GameOver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)


        findViewById<ImageButton>(R.id.imageButtonRetry).setOnClickListener {
            val intent= Intent(this,GameActivity::class.java)
            startActivity(intent)

        }
    }
}