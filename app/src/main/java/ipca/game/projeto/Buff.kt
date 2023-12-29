package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import java.util.Random


enum class BuffType{
    ATTACK,
    HP
}
class Buff {
    lateinit var buffType: BuffType
    var bitmap:Bitmap
    var x=0f
    var y=0f
    var generator= Random()
    var isConsumed=false
    var maxX=0
    var maxY=0
    lateinit var detectCollision:Rect

    constructor(context:Context,width:Int,height:Int,buffType: BuffType){
        this.buffType=buffType
        maxX=width
        maxY=height
        isConsumed=false
        if(buffType==BuffType.ATTACK){
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sword)

            x = (generator.nextInt(maxX - bitmap.width)).toFloat()

            y = (generator.nextInt(maxY - bitmap.height)).toFloat()

            detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)
        }
        else{
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.heart)
            x = (generator.nextInt(maxX - bitmap.width)).toFloat()

            y = (generator.nextInt(maxY - bitmap.height)).toFloat()

            detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)
        }

    }
    fun applyBuffEffect(player: Player){
        if(buffType==BuffType.ATTACK){
            player.damage += 10

        }
        else{
            player.currentHP+=50
            if(player.currentHP==player.maxHP){
                player.maxHP=player.currentHP
            }
        }
    }

    fun update(player: Player){


        /*if(buffType==BuffType.ATTACK){
            if(player.damage<150) {
                player.damage += 10
            }
        }
        else{
            player.currentHP+=50
            if(player.currentHP==player.maxHP){
                player.maxHP=player.currentHP
            }
        }*/
        detectCollision.left = x.toInt()
        detectCollision.top = y.toInt()
        detectCollision.right = (x + bitmap.width).toInt()
        detectCollision.bottom = (y + bitmap.height).toInt()
        if (Rect.intersects(player.detectCollision, detectCollision)) {
            Log.d("Buff", "Player collided with buff")
            isConsumed = true
            applyBuffEffect(player)
            Log.d("Buff", "Damage: ${player.damage}" +
                    "HP: ${player.currentHP}")
        }
    }
}