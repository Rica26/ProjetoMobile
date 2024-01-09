package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
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
    var speed=0
    var isWalking = false
    var isDead = false
    lateinit var detectCollision : Rect
    var rotationAngle = 0f
    constructor(context: Context,width: Int, height: Int,joystick: Joystick){
        x=70f
        y=50f
        maxHP=100
        currentHP=maxHP
        damage=30
        bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.cowboy)
        this.joystick = joystick
        maxY= height-bitmap.height
        maxX= width-bitmap.width
        detectCollision=Rect(x.toInt(),y.toInt(),bitmap.width,bitmap.height)

    }

    fun drawHealthBar(canvas: Canvas, paint: Paint) {
        val barWidth = 100
        val barHeight = 20
        val barLeft = x - barWidth / 2
        val barTop = y - 30
        val barRight = barLeft + (barWidth * currentHP / maxHP)
        val barBottom = barTop + barHeight
        val healthColor = when {
            currentHP > maxHP * 0.5 -> Color.rgb(0, 255, 0)
            currentHP < maxHP * 0.25 -> Color.rgb(255, (255 * (currentHP / (maxHP * 0.5))).toInt(), 0)
            else -> Color.rgb(255, 255, 0)
        }
        paint.color = healthColor
        canvas.drawRect(barLeft, barTop, barRight, barBottom, paint)
    }

    fun update() {

        if(isWalking) {
            val deltaX = joystick.stickX - joystick.centerX
            val deltaY = joystick.stickY - joystick.centerY

            val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

            val directionX = if (distance > 0) deltaX / distance else 0f
            val directionY = if (distance > 0) deltaY / distance else 0f

            speed = distance.coerceAtMost(joystick.maxSpeed.toFloat()).toInt()


            x += directionX * speed
            y += directionY * speed

            x = x.coerceIn(0f, maxX.toFloat())
            y = y.coerceIn(0f, maxY.toFloat())
            rotationAngle = Math.toDegrees(atan2(directionY.toDouble(), directionX.toDouble())).toFloat()
        }
        if(currentHP<0){
            currentHP=0
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