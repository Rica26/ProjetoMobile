package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button

class GameView:SurfaceView,Runnable {

    var isPlaying=false
    var gameThread:Thread?=null
    var surfaceHolder:SurfaceHolder
    var paint:Paint
    var canvas:Canvas?=null
    var player:Player
    var enemies= arrayListOf<Enemy>()
    var joystick:Joystick
    val backgroundImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg)

    constructor(context: Context,width:Int,height:Int):super(context){
        surfaceHolder=holder
        paint=Paint()

        player=Player(context,width,height)

        for(i in 1..5){
            enemies.add(Enemy(context,width,height))
        }
        joystick = Joystick(width / 4f, height * 3 / 4f, 100f, 50f)


    }

    override fun run() {
        while (isPlaying){
            update()
            draw()
            control()
        }

    }
    fun update(){
        player.update(joystick.stickX,joystick.stickY)
        for (e in enemies){
            e.update(player.x,player.y)
        }
    }
    fun draw() {
        if (surfaceHolder.surface.isValid) {
            canvas = surfaceHolder.lockCanvas()
            canvas?.drawBitmap(backgroundImage, 0f, 0f, paint)
            joystick.draw(canvas!!)
            canvas?.drawBitmap(player.getRotatedBitmap(), player.x, player.y, paint)
            for (e in enemies){
                canvas?.drawBitmap(e.getRotatedBitmap(), e.x, e.y, paint)
            }
            paint.color = Color.WHITE
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (joystick.isInsideJoystick(event.x, event.y)) {
                    joystick.updateStickPosition(event.x,event.y)
                    player.isWalking=true

                }
            }
            MotionEvent.ACTION_UP->{
                player.isWalking=false
                joystick.resetStick()
            }
        }
        return true
    }

    fun control(){
        Thread.sleep(17)
    }
    fun resume(){
        gameThread=Thread(this)
        gameThread?.start()
        isPlaying=true
    }
    fun pause() {
        isPlaying = false
        gameThread?.join()
    }


}