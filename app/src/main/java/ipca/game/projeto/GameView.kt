package ipca.game.projeto

import android.content.Context
import android.content.Intent
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
    var bossList=CopyOnWriteArrayList<Boss>()
    var boss:Boss
    var playerList=CopyOnWriteArrayList<Player>()
    var paint:Paint
    var canvas:Canvas?=null
    var player:Player
    var enemies= CopyOnWriteArrayList<Enemy>()
    var joystick:Joystick
    var projectile= CopyOnWriteArrayList<Projectile>()
    var pEnemy=CopyOnWriteArrayList<ProjectileEnemy>()
    var enemyTypeRandom:Int
    var bossSpawned=false
    lateinit var enemyType: EnemyType
    var enemyDead=0
    val generator=Random()
    val backgroundImage: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg)

    constructor(context: Context,width:Int,height:Int):super(context){
        surfaceHolder=holder
        paint=Paint()

        enemyTypeRandom=0
        //enemyType=EnemyType.SKELETON

        joystick = Joystick(width / 4f, height * 3 / 4f, 100f, 50f)
        player=Player(context,width,height,joystick)
        playerList.add(player)
        boss= Boss(context,width, height)
        enemySpawnHandler.postDelayed(object :Runnable{
            override fun run() {
                enemyTypeRandom=generator.nextInt(100)+1
                if(enemyTypeRandom<=35 && !bossSpawned){
                        enemyType=EnemyType.SKELETON
                        val enemy=Enemy(context,width, height,enemyType)
                        enemies.add(enemy)

                    }
                else if(enemyTypeRandom>=50 && !bossSpawned){
                        enemyType=EnemyType.ZOMBIE
                        val enemy=Enemy(context,width, height,enemyType)
                        enemies.add(enemy)

                    }
                    else{
                        if(!bossSpawned) {
                            enemyType = EnemyType.SLIME
                            val enemy = Enemy(context, width, height, enemyType)
                            enemies.add(enemy)
                        }
                    }
                    enemySpawnHandler.postDelayed(this,5000)
                }
            },5000)


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
        for (p in playerList) {
            p.update(joystick)
            if(p.isDead){
                isPlaying=false
            }
        }
        for (e in enemies){
            e.update(player)
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

                    if (!player.isDead) {
                        player.currentHP -= e.damage
                        if(e.enemyType==EnemyType.SLIME){
                            joystick.decreaseSpeed(3)
                        }
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
            if(e.isDead){
                enemyDead++
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
            for (b in bossList){

                if(Rect.intersects(p.detectCollision,b.detectCollision)) {
                    p.isDestroyed = true
                    b.currentHP -= player.damage
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
        if (!bossSpawned && enemyDead == 5) {
            Log.d("GameView", "Spawning boss...")
            bossList.add(boss)
            bossSpawned = true
            enemies.clear()  // Limpe a lista de inimigos após spawnar o chefe
        } else {
            Log.d("GameView", "Not spawning boss. bossSpawned=$bossSpawned, enemyDead=$enemyDead")
        }

            for(b in bossList){
            b.update(player)
            if(Rect.intersects(b.detectCollision,player.detectCollision)) {
                if (b.x < player.x) {
                    b.x -= b.speed
                } else {
                    b.x + b.speed
                }
                if (b.y < player.y) {
                    b.y -= b.speed
                } else {
                    b.y += b.speed
                }
            }
            if(Rect.intersects(b.detectCollision,player.detectCollision)){
                if(System.currentTimeMillis() >= b.lastDamageTime + b.damageCooldown) {
                    if(!player.isDead) {
                        player.currentHP -= b.damage
                        b.lastDamageTime = System.currentTimeMillis()
                    }
                }
            }
            if(b.isDead){
                isPlaying=false
            }
        }


        projectile.removeAll { it.isDestroyed }
        enemies.removeAll { it.isDead }
        pEnemy.removeAll{it.isDestroyed}
        bossList.removeAll{it.isDead}
        playerList.removeAll{it.isDead}



        if(!isPlaying && player.isDead) {
            val intent= Intent(context,GameOver::class.java)
            context.startActivity(intent)
        }
        if(!isPlaying && boss.isDead){
            val intent=Intent(context,Victory::class.java)
            context.startActivity(intent)
        }
    }
    fun draw() {
        if (surfaceHolder.surface.isValid) {
            canvas = surfaceHolder.lockCanvas()
            canvas?.drawBitmap(backgroundImage, 0f, 0f, paint)
            joystick.draw(canvas!!)
            for(p in playerList) {
                canvas?.drawBitmap(p.getRotatedBitmap(), p.x, p.y, paint)
                p.drawHealthBar(canvas!!, paint)
            }
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
            for(b in bossList) {
                canvas?.drawBitmap(b.getRotatedBitmap(), b.x, b.y, paint)
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
                        if (!player.isWalking && !player.isDead) {
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