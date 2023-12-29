package ipca.game.projeto

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class GameOver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)
        val mediaPlayer= MediaPlayer.create(this,R.raw.gameover)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mp->mp.release() }


        findViewById<ImageButton>(R.id.imageButtonRetry).setOnClickListener {
            val intent= Intent(this,GameActivity::class.java)
            startActivity(intent)

        }
    }
}