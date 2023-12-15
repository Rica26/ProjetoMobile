package ipca.game.projeto

import android.content.Context
import android.graphics.Rect

class ProjectileEnemy {

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
    lateinit var  enemy:Enemy

    constructor(context: Context, width: Int, height: Int, enemy: Enemy, directionX: Float, directionY: Float){
        x=enemy.x
        y=enemy.y
        this.enemy=enemy
        this.directionX=directionX
        this.directionY=directionY
        speed=5f
        maxX=width
        maxY=height
        bWidth=50f
        bHeight=50f
        detectCollision= Rect(x.toInt(),y.toInt(),bWidth.toInt(),bHeight.toInt())


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