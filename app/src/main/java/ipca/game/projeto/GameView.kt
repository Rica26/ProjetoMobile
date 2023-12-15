package ipca.game.projeto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import java.util.Random
import java.util.concurrent.CopyOnWriteArrayList

class GameView:SurfaceView,Runnable {

    var isPlaying=false
    var gameThread:Thread?=null
    var surfaceHolder:SurfaceHolder
    val enemySpawnHandler = Handler(Looper.getMainLooper())
    val enemyShootCooldown = 2000L
    var lastEnemyShootTime: Long = 0
    var paint:Paint
    var canvas:Canvas?=null
    var player:Player
    var enemies= CopyOnWriteArrayList<Enemy>()
    var joystick:Joystick
    var projectile= CopyOnWriteArrayList<Projectile>()
    var pEnemy=CopyOnWriteArrayList<ProjectileEnemy>()
    var enemyTypeRandom:Int
    lateinit var enemyType: EnemyType
    val generator=Random()
    val backgroundImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg)

    constructor(context: Context,width:Int,height:Int):super(context){
        surfaceHolder=holder
        paint=Paint()

        enemyTypeRandom=generator.nextInt(2)
        enemyType=EnemyType.SKELETON

        player=Player(context,width,height)



            enemySpawnHandler.postDelayed(object :Runnable{
                override fun run() {
                    enemyTypeRandom=generator.nextInt(2)
                    if(enemyTypeRandom==1){
                        enemyType=EnemyType.SKELETON
                        val enemy=Enemy(context,width, height,enemyType)
                        enemies.add(enemy)

                    }
                    else{
                        enemyType=EnemyType.ZOMBIE
                        val enemy=Enemy(context,width, height,enemyType)
                        enemies.add(enemy)

                    }
                    enemySpawnHandler.postDelayed(this,3000)
                }
            },3000)



        joystick = Joystick(width / 4f, height * 3 / 4f, 100f, 50f)


    }
    private fun spawnEnemyProjectiles(enemy: Enemy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEnemyShootTime >= enemyShootCooldown) {
            if (enemy.isRanged && !enemy.isDead) {
                val enemyDir = enemy.getEnemyDirection()
                pEnemy.add(
                    ProjectileEnemy(
                        context,
                        width,
                        height,
                        enemy,
                        enemyDir.first,
                        enemyDir.second
                    )
                )
                lastEnemyShootTime = currentTime
            }
        }
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
            spawnEnemyProjectiles(e)
            if(Rect.intersects(e.detectCollision,player.detectCollision)){
                if(e.x<player.x){
                    e.x-=e.speed
                }else{
                    e.x+e.speed
                }
                if(e.y<player.y){
                    e.y-=e.speed
                }else{
                    e.y+=e.speed
                }
                if (System.currentTimeMillis() >= e.lastDamageTime + e.damageCooldown) {

                    if (player.currentHP>0) {
                        player.currentHP -= e.damage
                        e.lastDamageTime = System.currentTimeMillis()
                    }

                }
            }
            for (otherEnemy in enemies){
                if(e!=otherEnemy && Rect.intersects(e.detectCollision,otherEnemy.detectCollision)){
                    if(e.x<otherEnemy.x){
                        e.x-=e.speed
                    }else{
                        e.x+=e.speed
                    }
                    if(e.y<otherEnemy.y){
                        e.y-=e.speed
                    }else{
                        e.y+=e.speed
                    }
                }
            }

        }
        for (p in projectile) {
            p.update()
            for (e in enemies) {
                if (Rect.intersects(p.detectCollision, e.detectCollision)) {
                    // A colisão ocorreu, destrua o projétil e o inimigo
                    p.isDestroyed = true
                    e.currentHP-=player.damage
                }


            }

        }




        for(p in pEnemy){
            p.update()
            for(e in enemies) {
                if (Rect.intersects(p.detectCollision, player.detectCollision)) {
                    p.isDestroyed = true
                    player.currentHP -= e.damage
                }
            }
        }

        projectile.removeAll { it.isDestroyed }
        enemies.removeAll { it.isDead }
        pEnemy.removeAll{it.isDestroyed}
        /*if(player.isDead){
            System.exit(0)
        }*/
    }
    fun draw() {
        if (surfaceHolder.surface.isValid) {
            canvas = surfaceHolder.lockCanvas()
            canvas?.drawBitmap(backgroundImage, 0f, 0f, paint)
            joystick.draw(canvas!!)
            canvas?.drawBitmap(player.getRotatedBitmap(), player.x, player.y, paint)
            player.drawHealthBar(canvas!!,paint)
            for (e in enemies){
                canvas?.drawBitmap(e.getRotatedBitmap(), e.x, e.y, paint)
            }
            paint.color = Color.BLACK
            for (p in projectile){
                paint.strokeWidth=p.bWidth
                canvas?.drawPoint(p.x,p.y,paint)
            }
            for(p in pEnemy){
                paint.strokeWidth=10f
                canvas?.drawCircle(p.x,p.y,p.bWidth/2,paint)
            }
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
                player.isWalking = false
                val playerDirection = player.getPlayerDirection()


                // Agendando a adição de projéteis restantes com um intervalo de 500 milissegundos
                val interval = 500
                val handler = Handler(Looper.getMainLooper())
                projectile.add(
                    Projectile(
                        context,
                        width,
                        height,
                        player,
                        playerDirection.first,
                        playerDirection.second
                    )
                )

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        if (!player.isWalking) {
                            projectile.add(
                                Projectile(
                                    context,
                                    width,
                                    height,
                                    player,
                                    playerDirection.first,
                                    playerDirection.second
                                )
                            )
                            handler.postDelayed(this, interval.toLong())
                        }
                    }
                }, interval.toLong())



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