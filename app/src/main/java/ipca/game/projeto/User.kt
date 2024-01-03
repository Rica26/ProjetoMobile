package ipca.game.projeto

import android.hardware.Sensor
import com.google.firebase.firestore.toObject
import ipca.game.projeto.FirebaseHelpers.db

data class User (var times : ArrayList<Long>? = null) {

    companion object{

        fun getAll(callback:(ArrayList<User>)->Unit){
            db.collection("users")
                .addSnapshotListener{value,error->
                    if(error!=null){
                        callback(arrayListOf<User>())
                    }
                    else{
                        val users= arrayListOf<User>()
                        for(d in value!!.documents){
                            val user=d.toObject<User>()
                            users.add(user!!)
                        }
                        callback(users)
                    }
                }
        }
    }
}