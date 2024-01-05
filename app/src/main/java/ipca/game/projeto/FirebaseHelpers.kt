package ipca.game.projeto

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseHelpers {

    val db = Firebase.firestore
    fun sendScore(time: Long, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userDocRef = db.collection("users").document(currentUser?.uid!!)


        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    userDocRef
                        .update("times", FieldValue.arrayUnion(time))
                        .addOnSuccessListener {
                            Log.d(MainActivity.TAG, "Time added successfully.")
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w(MainActivity.TAG, "Error updating document", e)
                            callback(false)
                        }
                } else {

                    val userData = hashMapOf("times" to arrayListOf(time))
                    userDocRef
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(MainActivity.TAG, "Document created successfully.")
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w(MainActivity.TAG, "Error creating document", e)
                            callback(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(MainActivity.TAG, "Error checking document existence", e)
                callback(false)
            }
    }
}