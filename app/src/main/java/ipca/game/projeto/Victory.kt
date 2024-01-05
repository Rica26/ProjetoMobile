package ipca.game.projeto

import android.content.ContentValues.TAG
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context


class Victory : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)
        val mediaPlayer=MediaPlayer.create(this,R.raw.victory)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mp->mp.release() }
        val finalTime = intent.getLongExtra("finalTime", 0)
        val textViewTime = findViewById<TextView>(R.id.textViewTime)

        // Convertendo o tempo para segundos
        val seconds = finalTime / 1000

        // Configurando o texto do TextView
        textViewTime.text = "Time: $seconds segundos"

        FirebaseHelpers.sendScore(seconds){

        }
        findViewById<ImageButton>(R.id.imageButtonVRetry).setOnClickListener {
            val intent= Intent(this,GameActivity::class.java)
            startActivity(intent)

        }

    }

}


