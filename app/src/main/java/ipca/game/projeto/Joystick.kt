package ipca.game.projeto

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

class Joystick( val centerX: Float, val centerY: Float, val baseRadius: Float, val stickRadius: Float) {

    val paintBase: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintStick: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var stickX: Float = centerX
    var stickY: Float = centerY
    var maxSpeed: Float = 20f
    var lastDirection:Pair<Float, Float> = Pair(0f,0f)

    init {
        paintBase.color = Color.GRAY
        paintBase.style = Paint.Style.FILL

        paintStick.color = Color.DKGRAY
        paintStick.style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, baseRadius, paintBase)
        canvas.drawCircle(stickX, stickY, stickRadius, paintStick)
    }
    fun updateStickPosition(newX: Float, newY: Float) {

        val deltaX = newX - centerX
        val deltaY = newY - centerY

        // Calcula a distância do stick ao centro
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

        if (distance > stickRadius) {
            val scaleFactor = stickRadius / distance
            stickX = centerX + deltaX * scaleFactor
            stickY = centerY + deltaY * scaleFactor
        } else {

            stickX = newX
            stickY = newY
        }
    }

    fun isInsideJoystick(touchX: Float, touchY: Float): Boolean {
        val distance = sqrt((touchX - centerX).pow(2) + (touchY - centerY).pow(2))
        return distance <= baseRadius
    }

    fun resetStick(){
        stickX=centerX
        stickY=centerY
    }
}

