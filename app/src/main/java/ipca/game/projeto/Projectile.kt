package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

class Projectile {
    var x=0f

    var y=0f
    var speed=0f
    var maxX=0
    var maxY=0

    var isDestroyed = false
    var bWidth=0f
    var bHeight=0f
    var directionX=0f
    var directionY=0f

    lateinit var detectCollision : Rect
    lateinit var player : Player
    lateinit var  enemy:Enemy

    constructor(context:Context,width:Int,height:Int, player: Player,directionX:Float,directionY: Float){
        x=player.x
        y=player.y
        this.player = player
        this.directionX=directionX
        this.directionY=directionY
        speed=50f
        maxX=width
        maxY=height
        bWidth=20f
        bHeight=20f
        detectCollision=Rect(x.toInt(),y.toInt(),bWidth.toInt(),bHeight.toInt())
    }

    constructor(context: Context,width: Int,height: Int,enemy: Enemy,directionX: Float,directionY: Float){
        x=enemy.x
        y=enemy.y
        this.enemy=enemy
        this.directionX=directionX
        this.directionY=directionY
        speed=20f
        maxX=width
        maxY=height
        bWidth=40f
        bHeight=40f
        detectCollision=Rect(x.toInt(),y.toInt(),bWidth.toInt(),bHeight.toInt())


    }


    fun update() {


            x += directionX * speed
            y += directionY * speed

            if (x < 0 || y < 0 || x > maxX || y > maxY) {
                isDestroyed = true
            }

            detectCollision.left = x.toInt()
            detectCollision.top = y.toInt()
            detectCollision.right = (x + bWidth).toInt()
            detectCollision.bottom = (y + bHeight).toInt()

    }


}

