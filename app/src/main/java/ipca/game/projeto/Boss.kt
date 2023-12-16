package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import java.util.Random
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Boss {

    var bitmap: Bitmap
    var x=0f
    var y=0f
    var speed=0
    var maxHP:Int
    var currentHP:Int
    var lastDamageTime = System.currentTimeMillis()
    val damageCooldown = 5000L
    var maxX=0
    var maxY=0

    lateinit var detectCollision: Rect
    var isDead=false
    var damage:Int
    var rotationAngle=0f
    var directionX=0f
    var directionY=0f




    constructor(context: Context, width:Int, height:Int){
        x=(width/2).toFloat()
        y=(height/2).toFloat()
        maxHP=500
        currentHP=maxHP
        damage=50
        speed=5
        bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.boss)
        maxY= height-bitmap.height
        maxX= width-bitmap.width
        detectCollision=Rect(x.toInt(),y.toInt(),bitmap.width,bitmap.height)

    }

    fun update(player: Player) {

            val deltaX = player.x - x
            val deltaY = player.y - y

            // Calcula a distância total entre o jogador e o inimigo
            val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

            // Normaliza o vetor de direção para suavizar o movimento
            directionX = if (distance > 0) deltaX / distance else 0f
            directionY = if (distance > 0) deltaY / distance else 0f

            // Atualiza a posição do inimigo com base na direção normalizada e na velocidade
            x += directionX * speed
            y += directionY * speed

        // Garante que o inimigo não ultrapasse os limites da tela
        x = x.coerceIn(0f, maxX.toFloat() - bitmap.width)
        y = y.coerceIn(0f, maxY.toFloat() - bitmap.height)
        rotationAngle = Math.toDegrees(atan2(directionY.toDouble(), directionX.toDouble())).toFloat()

        if(currentHP<=0){
            isDead=true
        }


        // Atualiza o objeto de detecção de colisão
        detectCollision.left = x.toInt()
        detectCollision.top = y.toInt()
        detectCollision.right = (x + bitmap.width).toInt()
        detectCollision.bottom = (y + bitmap.height).toInt()
    }
    fun getRotatedBitmap(): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle, bitmap.width / 2f, bitmap.height / 2f)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}