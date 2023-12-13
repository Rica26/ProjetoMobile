package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

class Player {

    var bitmap : Bitmap
    val joystick:Joystick
    var x= 0f
    var y= 0f

    var maxHP:Int
    var damage:Int
    var currentHP:Int

    var maxX=0
    var maxY=0
    var isWalking = false
    var isDead = false
    //var lastShootTime = System.currentTimeMillis()
    //val shootInterval = 1000L

    lateinit var detectCollision : Rect
    var rotationAngle = 0f
    constructor(context: Context,width: Int, height: Int){
        x=70f
        y=50f
        maxHP=100
        currentHP=maxHP
        damage=10
        bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.cowboy)
        joystick = Joystick(width / 4f, height * 3 / 4f, 100f, 50f)
        maxY= height-bitmap.height
        maxX= width-bitmap.width
        detectCollision=Rect(x.toInt(),y.toInt(),bitmap.width,bitmap.height)

    }

    fun update(stickX:Float,stickY:Float) {

        if(isWalking) {
            val deltaX = stickX - joystick.centerX
            val deltaY = stickY - joystick.centerY

            val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

            val directionX = if (distance > 0) deltaX / distance else 0f
            val directionY = if (distance > 0) deltaY / distance else 0f

            val speed = distance.coerceAtMost(joystick.maxSpeed)


            x += directionX * speed
            y += directionY * speed

            x = x.coerceIn(0f, maxX.toFloat())
            y = y.coerceIn(0f, maxY.toFloat())
            rotationAngle = Math.toDegrees(atan2(directionY.toDouble(), directionX.toDouble())).toFloat()
        }
        if(currentHP<=0){
            isDead=true
        }

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
    fun getPlayerDirection(): Pair<Float, Float> {
        val radians = Math.toRadians(rotationAngle.toDouble())
        val directionX = cos(radians).toFloat()
        val directionY = sin(radians).toFloat()
        return Pair(directionX, directionY)
    }



}