package ipca.game.projeto

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseHelpers {

    val db = Firebase.firestore
    fun sendScore( time:Long, callback: (Boolean)->Unit){
        val currentUser = FirebaseAuth.getInstance().currentUser
        db.collection("users")
            .document(currentUser?.uid!!)
            .set(hashMapOf("time" to time))
            .addOnSuccessListener { documentReference ->
                Log.d(MainActivity.TAG, "DocumentSnapshot written with ID: ${documentReference}")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(MainActivity.TAG, "Error adding document", e)
                callback(false)
            }

    }
}