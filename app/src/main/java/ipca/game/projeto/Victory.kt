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

    /*private fun checkAndUpdateScore(newTime: Long) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userDocRef = FirebaseHelpers.db.collection("users").document(currentUser?.uid!!)

        // Obtém o tempo atual armazenado na base de dados
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val currentTime = documentSnapshot.getLong("time") ?: Long.MAX_VALUE

                // Verifica se o novo tempo é menor do que o tempo existente
                if (newTime < currentTime) {
                    // Se sim, substitui o tempo na base de dados
                    FirebaseHelpers.sendScore(newTime) {
                        Log.d(TAG, "Score updated successfully.")
                    }
                } else {
                    Log.d(TAG, "New time is not smaller. Not updating.")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user document", e)
            }
    }*/
    /*private fun checkAndUpdateScore(newTime: Long) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userDocRef = FirebaseHelpers.db.collection("users").document(currentUser?.uid!!)

        // Obtém a lista de tempos atual armazenada na base de dados
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val currentTimes = documentSnapshot.get("times") as? List<Long> ?: emptyList()

                // Adiciona o novo tempo à lista de tempos
                val newTimes = currentTimes + listOf(newTime)

                // Atualiza a lista de tempos na base de dados
                FirebaseHelpers.sendScore(newTimes) {
                    Log.d(TAG, "Times updated successfully.")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user document", e)
            }
    }*/
}


