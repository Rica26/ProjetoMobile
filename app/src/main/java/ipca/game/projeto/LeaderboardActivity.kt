package ipca.game.projeto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot
import ipca.game.projeto.FirebaseHelpers.db

class LeaderboardActivity : AppCompatActivity() {

    val adapter=LeaderboardAdapter()
    var leaderboardList= mutableListOf<User>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        val listView: ListView = findViewById(R.id.ListViewLeaderboard)
        listView.adapter = adapter
        findViewById<ImageButton>(R.id.imageButtonStartLeader).setOnClickListener {
            val intent=Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
       User.getAll {
           leaderboardList=it
           adapter.notifyDataSetChanged()
       }
    }


    inner class LeaderboardAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return leaderboardList.count()
        }

        override fun getItem(position: Int): Any {
            return leaderboardList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rootView=layoutInflater.inflate(R.layout.row_leaderboard,parent,false)
            val timeTextView = rootView.findViewById<TextView>(R.id.textViewTimeLeader)
            leaderboardList[position].times?.sortBy { it }


            var score=""
            if(leaderboardList[position].times?.size?:0>0)
                score+="1º${leaderboardList[position].times!![0]}s\n "

            if(leaderboardList[position].times?.size?:0>1)
                score+="2º${leaderboardList[position].times!![1]}s\n "

            if(leaderboardList[position].times?.size?:0>2)
                score+="3º${leaderboardList[position].times!![2]}s "

            timeTextView.text=score
            return rootView

        }
    }
}