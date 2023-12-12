package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import java.util.Random
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

class Enemy {
    var bitmap: Bitmap
    var x=0f
    var y=0f
    var speed=0
    var maxX=0
    var maxY=0
    val generator=Random()
    lateinit var detectCollision:Rect
    var rotationAngle=0f

    constructor(context:Context,width:Int,height:Int){
        maxX=width
        maxY=height
        speed=5

        bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.zombie)
        x = (generator.nextInt(maxX - bitmap.width) ).toFloat()
        y = (generator.nextInt(maxY - bitmap.height) ).toFloat()

        detectCollision = Rect(x.toInt() ,y.toInt(), bitmap.width, bitmap.height)

    }

    fun update(playerx: Float, playery: Float) {
        // Calcula a diferença entre as posições do jogador e do inimigo
        val deltaX = playerx - x
        val deltaY = playery - y

        // Calcula a distância total entre o jogador e o inimigo
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

        // Normaliza o vetor de direção para suavizar o movimento
        val directionX = if (distance > 0) deltaX / distance else 0f
        val directionY = if (distance > 0) deltaY / distance else 0f

        // Atualiza a posição do inimigo com base na direção normalizada e na velocidade
        x += directionX * speed
        y += directionY * speed

        // Garante que o inimigo não ultrapasse os limites da tela
        x = x.coerceIn(0f, maxX.toFloat() - bitmap.width)
        y = y.coerceIn(0f, maxY.toFloat() - bitmap.height)
        rotationAngle = Math.toDegrees(atan2(directionY.toDouble(), directionX.toDouble())).toFloat()


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