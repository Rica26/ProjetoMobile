Mini-Archero- Projeto para Desenvolvimento de Jogos para Plataformas Móveis
=============================================

Introdução
-------

Neste Projeto realizamos um jogo que usa uma API externa para guardar utilizadores e os tempos dos mesmos depois organizando os melhores(tempos) numa leaderboard.
O nosso projeto consiste num jogador derrotar um X número de inimigos para _spawnar_ um _boss_ e tentar derrotar o mesmo o mais rápido possível.

Desenvolvimento
-------
Este Projeto é constituído por várias classes e views e navega de forma intuitiva de view para view(o menu principal para o jogo, do jogo para o victory screen ou game over screen,etc.) e vou falar de cada uma delas

MainActivity:
-
Esta Activity é a que a aplicação começa e serve para dar _setup_ ao firebase e dar _login_ anonimamente no mesmo e se o _login_ for bem sucedido vai para a **StartScreenActivity**
```kotlin
package ipca.game.projeto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)
        auth = Firebase.auth
        signInAnonymously()

    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                    val intent=Intent(this,StartScreenActivity::class.java)
                    startActivity(intent) //aqui começamos a activity StartScreen depois de realizar todas as outras operações
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
        // [END signin_anonymously]
    }
    private fun updateUI(user: FirebaseUser?) {
    }
    companion object {
         const val TAG = "AnonymousAuth"
    }

}
```
StarScreenActivity:
-
Nesta Activity temos o menu principal do jogo e se clicarmos no Play, vamos para **GameActivity**(vou explicar mais à frente o que faz esta Activity). Tal como na anterior criamos uma variável intent com a Activity que desejamos e depois iniciamos essa activity com um startActivity(intent)
```kotlin
package ipca.game.projeto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class StartScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<ImageButton>(R.id.imageButtonPlay).setOnClickListener {
            val intent= Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}
```
GameActivity:
-
Esta é a Activity que gere o jogo através da GameView, inicializando uma instância da GameView e depois definindo o conteúdo da activity de acordo com essa GameView(tendo também funções de retomar e pausar)
```kotlin
package ipca.game.projeto

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class GameActivity : AppCompatActivity() {
    lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val display=windowManager.defaultDisplay
        val size=Point()
        display.getSize(size)
        gameView= GameView(this,size.x,size.y)
        setContentView(gameView)

    }
    override fun onResume() {
        super.onResume()
        gameView.resume()
    }
    override fun onPause() {
        super.onPause()
        gameView.pause()
    }
}
```
Classes:
-
Antes de irmos para a GameView que é onde a maior parte da lógica e código do jogo se encontram vou falar das várias classes que constituem o jogo.

**_Player_**:
Esta é a classe do jogador, que inicializamos usando um construtor que recebe um _context_, uma _width_, uma _height_ e um objeto _joystick_ e dentro desse construtor inicializamos a posição inicial do jogador, um _HP_ inicial e com um _HP_ atual(que começa igual ao inicial),o dano que o jogador dá, um bitmap do jogador, as posições máximas de acordo com o bitmap e um retangulo de colisão. 
```kotlin
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
```

depois temos uma função que desenha a barra de vida do jogador, que recebe um Canvas e um Paint, em cima dele e segue-o para onde ele for, mudando de cor de acordo com a vida restante do player(verde,amarelo e vermelho)
```kotlin
fun drawHealthBar(canvas: Canvas, paint: Paint) {
        // Lógica para desenhar a barra de HP em relação à posição do jogador
        val barWidth = 100 // A largura da barra de HP (ajuste conforme necessário)
        val barHeight = 20 // A altura da barra de HP (ajuste conforme necessário)
        val barLeft = x - barWidth / 2 // Posição esquerda da barra de HP em relação ao jogador
        val barTop = y - 30 // Posição superior da barra de HP acima do jogador
        val barRight = barLeft + (barWidth * currentHP / maxHP) // Largura proporcional à saúde atual
        val barBottom = barTop + barHeight // Posição inferior da barra de HP

        // Ajusta a cor com base na saúde do jogador
        val healthColor = when {
            currentHP > maxHP * 0.5 -> Color.rgb(0, 255, 0) // Amarelo
            currentHP < maxHP * 0.25 -> Color.rgb(255, (255 * (currentHP / (maxHP * 0.5))).toInt(), 0) // Vermelho gradual
            else -> Color.rgb(255, 255, 0) // Vermelho
        }
        paint.color = healthColor

        // Desenha a barra de HP
        canvas.drawRect(barLeft, barTop, barRight, barBottom, paint)
    }
```
prosseguindo temos a função _update_ que trata da lógica do jogador, recebendo um joystick como argumento, e trata do movimento do jogador(não o deixando sair das bordas da tela usando o coerceIn) de acordo com o _joystick_, da morte do jogador e da deteção de colisão.
```kotlin
    fun update(joystick: Joystick) {

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
```

por fim temos duas funções auxiliares que tratam respetivamente da "animação" do bitmap do jogador usando a função _Matrix_ do kotlin e de obter a direção atual do jogador usando os angulos de rotação em ambas
```kotlin
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
```


**_Joystick_**:
É com o _joystick_ que movimentamos o jogador, desenhando o mesmo na tela e depois usando o circulo interior para mover o jogador sendo que a velocidade do jogador depende do quão longe do centro está o circulo interior. Quando o joystick é "largado" a sua posição dá _reset_(volta ao centro) e o jogador pára. Temos também uma função auxiliar _DecreaseSpeed_ que vai ser util para uma mecânica que vou explicar mais à frente.
```kotlin
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
    var maxSpeed: Int = 20
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
    fun decreaseSpeed(amount: Int) {
        maxSpeed -= amount

        // Certifique-se de que a velocidade não se torne negativa
        if (maxSpeed < 0) {
            maxSpeed = 0
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
```
**_Projectile_** e **_ProjectileEnemy_**:
Aqui falo das duas porque a lógica é a mesma mas criei duas classes em separado para não criar conflito com as colisões, só muda que uma começa na posição do jogador(o construtor recebe um jogador como argumento) e a outra começa na posição dos inimigos que disparam(o construtor recebe um inimigo como argumento), ambas recebem direções em x e em y para irem numa direção fixa no entanto têm alguns parametros com valores diferentes(o _speed_ e os tamanhos). Nenhuma usa bitmap, preferindo desenhar na tela os projeteis.


_Projectile_:
```kotlin
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
```

_ProjectileEnemy_: 
```kotlin
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
```

o _update_ de ambas é exatamente igual sendo que vão dirigem-se numa direção fixa até haver colisão(definindo aqui também a deteção de colisão) com as paredes(ou com algum objeto player ou enemy mas essa lógica tá na GameView):

```kotlin
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

```

**_Enemy_**:
Nesta classe começamos por criar um enum EnemyType que é constituída pelos 3 tipos de inimigos que definimos para o jogo
```kotlin
enum class EnemyType {
    ZOMBIE,
    SKELETON,
    SLIME
}
```

o construtor aqui é interessante, recebe na mesma um context,width e height mas também recebe um EnemyType. Dentro do construtor usamos um when para meter parametros diferentes dependendo do EnemyType sendo que cada um tem atributos próprios, sendo que a posição inicial de cada tipo é sempre aleatória.

```kotlin
constructor(context:Context,width:Int,height:Int,enemyType: EnemyType){
        this.enemyType=enemyType
        when(enemyType) {

            EnemyType.ZOMBIE-> {
                maxX = width
                maxY = height
                speed = 7
                damage = 5
                maxHP = 50
                isRanged=false

                currentHP = maxHP

                bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.zombie)
                x = (generator.nextInt(maxX - bitmap.width)).toFloat()

                y = (generator.nextInt(maxY - bitmap.height)).toFloat()

                detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)

            }
            EnemyType.SKELETON -> {
                maxX = width
                maxY = height
                speed = 5
                damage = 10
                maxHP = 30
                isRanged=true

                currentHP = maxHP

                bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.skeleton)
                x = (generator.nextInt(maxX - bitmap.width)).toFloat()

                y = (generator.nextInt(maxY - bitmap.height)).toFloat()

                detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)


            }
            EnemyType.SLIME->{
                maxX = width
                maxY = height
                speed = 1
                damage = 2
                maxHP = 70
                isRanged=false

                currentHP = maxHP

                bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.slime)
                x = (generator.nextInt(maxX - bitmap.width)).toFloat()

                y = (generator.nextInt(maxY - bitmap.height)).toFloat()

                detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)

            }
        }
    }

```
no _update_, que recebe um player como argumento, já dividimos a lógica em o inimigo ser _ranged_ ou não, se for ranged afasta-se do player, se não for ranged aproxima-se do player ambos com velocidade constante(e respetiva dependendo do EnemyType) e usando o coerceIn tal como no player para não poderem _spawnar_ fora das bordas do jogo. Calcula também o angulo de rotação para depois usar numa função posterior,trata da morte dos inimigos e faz a deteção de colisão de acordo com o retangulo definido nos construtores.
    
```kotlin
    
    fun update(player: Player) {

        if(!isRanged) {
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
        }

        if(isRanged){
            val deltaX = player.x - x
            val deltaY = player.y - y

            // Calcula a distância total entre o jogador e o inimigo
            val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

            // Normaliza o vetor de direção para suavizar o movimento
            directionX = if (distance > 0) deltaX / distance else 0f
            directionY = if (distance > 0) deltaY / distance else 0f

            // Atualiza a posição do inimigo com base na direção normalizada e na velocidade
            x -= directionX
            y -= directionY

        }

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
    
    ```
por fim, tal como no _Player_, tem duas funções auxiliares uma para tratar da "animação" usando o _Matrix_() e o rotationagle calculado no update e outra para obter a direção do inimigo.

```kotlin
    fun getRotatedBitmap(): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle, bitmap.width / 2f, bitmap.height / 2f)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    fun getEnemyDirection(): Pair<Float, Float> {
        val radians = Math.toRadians(rotationAngle.toDouble())
        val directionX = cos(radians).toFloat()
        val directionY = sin(radians).toFloat()
        return Pair(directionX, directionY)
    }
```
    
