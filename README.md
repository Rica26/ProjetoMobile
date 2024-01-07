Mini-_Archero_- Projeto para Desenvolvimento de Jogos para Plataformas Móveis by Ricardo Almeida, Manuel Pacheco e Gonçalo Graça
=============================================

Introdução
=

Neste Projeto realizamos um jogo que usa uma API externa para guardar utilizadores e os tempos dos mesmos depois organizando os melhores(tempos) numa leaderboard.
O nosso projeto consiste num jogador derrotar um X número de inimigos para _spawnar_ um _boss_ e tentar derrotar o mesmo o mais rápido possível.

Desenvolvimento
=
Este Projeto é constituído por várias classes e views e navega de forma intuitiva de view para view(o menu principal para o jogo, do jogo para o victory screen ou game over screen,etc.) e vou falar de cada uma delas

_MainActivity_:
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
_StarScreenActivity_:
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
_LeaderBoardActivity_ e _User Data Class_:
-
Esta é a Activity que gere os melhores tempos, usando um _adapter_ e uma variável que guarda um lista de utilizadores(usando uma função da _data class User_ chamada _getAll_) sendo que na _getView_ do _adapter_ ordenamos os _times_(um dos parâmetros da _data class_) pelo melhor/menor tempo em segundos e mostramos o _top_ 3 de tempos na tela.


_User_:
```kotlin
package ipca.game.projeto

import android.hardware.Sensor
import com.google.firebase.firestore.toObject
import ipca.game.projeto.FirebaseHelpers.db

data class User (var times : ArrayList<Long>? = null) {

    companion object{

        fun getAll(callback:(ArrayList<User>)->Unit){
            db.collection("users")
                .addSnapshotListener{value,error->
                    if(error!=null){
                        callback(arrayListOf<User>())
                    }
                    else{
                        val users= arrayListOf<User>()
                        for(d in value!!.documents){
                            val user=d.toObject<User>()
                            users.add(user!!)
                        }
                        callback(users)
                    }
                }
        }
    }
}
```

_LeaderboardActivity_:
```kotlin
package ipca.game.projeto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot
import ipca.game.projeto.FirebaseHelpers.db

class LeaderboardActivity : AppCompatActivity() {

    val adapter=LeaderboardAdapter()
    var leaderboardList= mutableListOf<User>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        val listView: ListView = findViewById(R.id.ListViewLeaderboard)
        listView.adapter = adapter
        findViewById<ImageButton>(R.id.imageButtonStartLeader).setOnClickListener {
            val intent=Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
       User.getAll {
           leaderboardList=it
           adapter.notifyDataSetChanged()
       }
    }


    inner class LeaderboardAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return leaderboardList.count()
        }

        override fun getItem(position: Int): Any {
            return leaderboardList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rootView=layoutInflater.inflate(R.layout.row_leaderboard,parent,false)
            val timeTextView = rootView.findViewById<TextView>(R.id.textViewTimeLeader)
            leaderboardList[position].times?.sortBy { it }


            var score=""
            if(leaderboardList[position].times?.size?:0>0)
                score+="1º${leaderboardList[position].times!![0]}s\n "

            if(leaderboardList[position].times?.size?:0>1)
                score+="2º${leaderboardList[position].times!![1]}s\n "

            if(leaderboardList[position].times?.size?:0>2)
                score+="3º${leaderboardList[position].times!![2]}s "

            timeTextView.text=score
            return rootView

        }
    }
}
```

_GameActivity_:
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
prosseguindo temos a função _update_ que trata da lógica do jogador, recebendo um joystick como argumento, e trata do movimento do jogador(não o deixando sair das bordas da tela usando o coerceIn) de acordo com o _joystick_, cálculo do angulo de rotação para uso posterior, da morte do jogador e da deteção de colisão.
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

**_Buff_**:
Na classe _Buff_ também começamos por criar um enum, desta vez denominado BuffType que contém os dois tipos de _buff_, um que aumenta o ataque e outro que dá vida ao jogador
```kotlin
enum class BuffType{
    ATTACK,
    HP
}
```
no construtor usa a mesma lógica que o inimigo recebendo um _BuffType_ como argumento e tendo em conta o tipo de buff vai ter alguns parâmetros diferentes
```kotlin
    constructor(context:Context,width:Int,height:Int,buffType: BuffType){
        this.buffType=buffType
        maxX=width
        maxY=height
        isConsumed=false
        if(buffType==BuffType.ATTACK){
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sword)

            x = (generator.nextInt(maxX - bitmap.width)).toFloat()

            y = (generator.nextInt(maxY - bitmap.height)).toFloat()

            detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)
        }
        else{
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.heart)
            x = (generator.nextInt(maxX - bitmap.width)).toFloat()

            y = (generator.nextInt(maxY - bitmap.height)).toFloat()

            detectCollision = Rect(x.toInt(), y.toInt(), bitmap.width, bitmap.height)
        }

    }
```
temos uma função que aplica o efeito do _buff_ dependendo do tipo e a típica função _update_ que neste caso recebe um jogador para depois verificar a colisão entre o _buff_ e o jogador. A verdade é que tentamos implementar isto de uma forma mais coerente tendo em conta o resto do projeto, metendo no _update_ a aplicação do efeito e usando a deteção de colisão no _update_ da _GameView_ mas não estava a funcionar como o suposto por isso usei esta função auxiliar e
```kotlin
fun applyBuffEffect(player: Player){
        if(buffType==BuffType.ATTACK){
            player.damage += 10

        }
        else{
            player.currentHP+=50
            if(player.currentHP==player.maxHP){
                player.maxHP=player.currentHP
            }
        }
    }

    fun update(player: Player){

        detectCollision.left = x.toInt()
        detectCollision.top = y.toInt()
        detectCollision.right = (x + bitmap.width).toInt()
        detectCollision.bottom = (y + bitmap.height).toInt()
        if (Rect.intersects(player.detectCollision, detectCollision)) {
            Log.d("Buff", "Player collided with buff")
            isConsumed = true
            applyBuffEffect(player)
            Log.d("Buff", "Damage: ${player.damage}" +
                    "HP: ${player.currentHP}")
        }
    }
}fun applyBuffEffect(player: Player){
        if(buffType==BuffType.ATTACK){
            player.damage += 10

        }
        else{
            player.currentHP+=50
            if(player.currentHP==player.maxHP){
                player.maxHP=player.currentHP
            }
        }
    }

    fun update(player: Player){

        detectCollision.left = x.toInt()
        detectCollision.top = y.toInt()
        detectCollision.right = (x + bitmap.width).toInt()
        detectCollision.bottom = (y + bitmap.height).toInt()
        if (Rect.intersects(player.detectCollision, detectCollision)) {
            Log.d("Buff", "Player collided with buff")
            isConsumed = true
            applyBuffEffect(player)
            Log.d("Buff", "Damage: ${player.damage}" +
                    "HP: ${player.currentHP}")
        }
    }
}
```
deteta-se a colisão no _update_ da classe _Buff_
```kotlin
fun applyBuffEffect(player: Player){
        if(buffType==BuffType.ATTACK){
            player.damage += 10

        }
        else{
            player.currentHP+=50
            if(player.currentHP==player.maxHP){
                player.maxHP=player.currentHP
            }
        }
    }

    fun update(player: Player){

        detectCollision.left = x.toInt()
        detectCollision.top = y.toInt()
        detectCollision.right = (x + bitmap.width).toInt()
        detectCollision.bottom = (y + bitmap.height).toInt()
        if (Rect.intersects(player.detectCollision, detectCollision)) {
            Log.d("Buff", "Player collided with buff")
            isConsumed = true
            applyBuffEffect(player)
            Log.d("Buff", "Damage: ${player.damage}" +
                    "HP: ${player.currentHP}")
        }
    }
}
```

**_Boss_**
Esta classe trata especificamente do inimigo final que _spawna_ depois de certos requisitos serem correspondidos. É um inimigo maior e bem mais forte em geral, sendo que dois ataques são normalmente fatais.
No construtor recebe os parâmetros normais e inicializa os atributos normais de outras classes, sendo até menos complexo que o inimigo visto que só há um tipo de _boss_
```kotlin
constructor(context: Context, width:Int, height:Int){
        x=(width/2).toFloat()
        y=(height/2).toFloat()
        maxHP=500
        currentHP=maxHP
        damage=50
        speed=8
        bitmap=BitmapFactory.decodeResource(context.resources,R.drawable.boss)
        maxY= height-bitmap.height
        maxX= width-bitmap.width
        detectCollision=Rect(x.toInt(),y.toInt(),bitmap.width,bitmap.height)

    }
```
a função _update_ da classe também é idêntica à dos inimigos recebendo um argumento _Player_ mas sem diferentes comportamentos dependendo da forma de ataque do inimigo sendo, tal como o construtor, menos complexa

```kotlin
fun update(player: Player) {

        val deltaX = player.x - x
        val deltaY = player.y - y
        
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

            
        directionX = if (distance > 0) deltaX / distance else 0f
        directionY = if (distance > 0) deltaY / distance else 0f

            
        x += directionX * speed
        y += directionY * speed
        
        rotationAngle = Math.toDegrees(atan2(directionY.toDouble(), directionX.toDouble())).toFloat()

        if(currentHP<=0){
            isDead=true
        }
        
        detectCollision.left = x.toInt()
        detectCollision.top = y.toInt()
        detectCollision.right = (x + bitmap.width).toInt()
        detectCollision.bottom = (y + bitmap.height).toInt()
    }
```
por fim temos a função auxiliar que usa o _Matrix_ para "animar" o bitmap do _Boss_


FirebaseHelpers:
-
// O documento não existe, cria-o com o campo "times"
```kotlin
fun sendScore(time: Long, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userDocRef = db.collection("users").document(currentUser?.uid!!)

        
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // O documento existe, atualiza o campo "times" usando um array
                    userDocRef
                        .update("times", FieldValue.arrayUnion(time))
                        .addOnSuccessListener {
                            Log.d(MainActivity.TAG, "Time added successfully.")
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w(MainActivity.TAG, "Error updating document", e)
                            callback(false)
                        }
                } else {
                    
                    val userData = hashMapOf("times" to arrayListOf(time))
                    userDocRef
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(MainActivity.TAG, "Document created successfully.")
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w(MainActivity.TAG, "Error creating document", e)
                            callback(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(MainActivity.TAG, "Error checking document existence", e)
                callback(false)
            }
    }
```

_GameView_
-
Aqui está a SurfaceView que junta tudo, ainda adiciona mais partes e permite o jogo funcionar. No construtor inicializamos praticamente todos os objetos pretendidos: o surfaceHolder que nos permite interagir com a tela, o paint, o _joystick_, o jogador, os inimigos, os buffs, o boss, a música, etc. Vou explicar sucintamente algumas das decisões aqui, portanto começando pelo jogador criamos um objeto jogador e depois adicionamos o jogador a uma lista(em termos de facilidade de lógica achei melhor criar uma lista de um unico jogador), também criamos um objeto _boss_ mas ainda não o adicionamos à lista, com os inimigos usamos um handler para _spawnar_ inimigos de 5 em 5 segundos e usamos um Random para gerar um numero de 1 a 100 e dependendo do numero que calhar(e se o _boss_ já _spawnow_) _spawna_ um dos 3 tipos de inimigo, com o _buff_ é igual mas em vez de ser de 5 em 5 é de 20 em 20 segundos.

```kotlin
    constructor(context: Context,width:Int,height:Int):super(context){
        surfaceHolder=holder
        paint=Paint()
        backgroundMusic=MediaPlayer.create(context,R.raw.ost)

        bossMusic=MediaPlayer.create(context,R.raw.bossfightost)

        enemyTypeRandom=0
        buffTypeRandom=0

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
        buffHandler.postDelayed(object:Runnable{
            override fun run() {
                buffTypeRandom=generator.nextInt(100)+1
                if(buffTypeRandom>15){
                    buffType=BuffType.ATTACK
                    val buff=Buff(context,width,height, buffType)
                    buffs.add(buff)
                }
                else{
                    buffType=BuffType.HP
                    val buff=Buff(context,width,height, buffType)
                    buffs.add(buff)
                }
                Log.d("Buff", "Buff created. Type: $buffType, Position: ($x, $y)")
                buffHandler.postDelayed(this,20000)
            }
        },20000)


    }
```
depois temos uma função que trata de _spawnar_ os projéteis dos inimigos _Ranged_ periodicamente usando a função _getEnemyDirection_ para os projeteis sairem nessa direção e adicionando projeteis de inimigo à lista correspondente. Honestamente esta não foi a primeira implentação mas depois de adicionarmos mais mecânicas ao jogo, algo interferiu com a nossa implementação original e então resolvemos assim.
```kotlin
    fun spawnEnemyProjectiles(enemy: Enemy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - enemy.lastEnemyShootTime >= enemyShootCooldown) {
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
                enemy.lastEnemyShootTime = currentTime
            }
        }
    }
```
a função run que damos _override_ da função nativa do _Runnable_ da _SurfaceView_ que trata do fluxo do jogo
```kotlin
    override fun run() {
        while (isPlaying){
            update()
            draw()
            control()
        }

    }
```
a função _update_ onde aplicamos o respetivo _update_ de cada objeto, tratamos das colisões quase todas e da lógica após essas colisões, efeitos sonoros, o _spawn_ do _boss_ e eu vou explicar de uma maneira resumida algumas decisões.
Na parte das colisões quando os jogadores colidem com os inimigos, estes tiram vida ao mesmo e dá um efeito sonoro, quando projeteis respetivos colidem com o jogador/inimigos são destruídos, tiram vida e adicionam um efeito sonoro. Também prevenimos a sobreposição dos bitmaps com as colisões afastando e aproximando dependendo das posições. Contamos o número de inimigos mortos para ver quando o _boss_ _spawna_, sendo que quando os requisitos são preenchidos todos os inimigos são removidos, a música de normal pára, começa a música do _boss_ e o objeto _boss_ anteriormente inicializado no construtor é adicionado à lista correspondente. No fim removemos todos os objetos da lista que foram "mortos" e/ou "destruídos e definimos a lógica de fim de jogo, se o jogador morrer dá um efeito sonoro de derrota e somos encaminhados para a atividade de _GameOver_ para podermos recomeçar, se o _boss_ for derrotado dá um efeito sonoro de vitória e somos encaminhados para a atividade _Victory_ onde nos diz o tempo que demoramos e pára sempre a música em ambos os casos

```kotlin

    fun update(){
        backgroundMusic?.isLooping = true
        backgroundMusic?.start()
        elapsedTime = System.currentTimeMillis() - startTime

        for (p in playerList) {
            p.update(joystick)
            if(p.isDead){
                isPlaying=false
            }

        }
        for(b in buffs){
            b.update(player)
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
                        val mediaPlayer = MediaPlayer.create(context, R.raw.playergethit)
                        mediaPlayer.start()
                        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
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
                    val mediaPlayer = MediaPlayer.create(context, R.raw.enemygethit)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                }
            }
            for (b in bossList){

                if(Rect.intersects(p.detectCollision,b.detectCollision)) {
                    p.isDestroyed = true
                    b.currentHP -= player.damage
                    val mediaPlayer = MediaPlayer.create(context, R.raw.enemygethit)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                }
            }

        }




        for(p in pEnemy){
            p.update()
            for(e in enemies) {
                if (Rect.intersects(p.detectCollision, player.detectCollision)) {
                    p.isDestroyed = true
                    player.currentHP -= e.damage
                    val mediaPlayer = MediaPlayer.create(context, R.raw.playergethit)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                }
            }
        }
        if (!bossSpawned && enemyDead == 5) {
            //backgroundMusic?.isLooping=false
            //backgroundMusic?.release()
            backgroundMusic?.stop()



            bossMusic?.isLooping=true
            bossMusic?.start()

            bossList.add(boss)
            bossSpawned = true
            enemies.clear()  // Limpe a lista de inimigos após spawnar o chefe
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

                        val mediaPlayer=MediaPlayer.create(context,R.raw.playergethit)

                        mediaPlayer.start()

                        mediaPlayer.setOnCompletionListener { mp->mp.release() }

                    }

                }

            }

            if(b.isDead){

                isPlaying=false

            }

        }


        projectile.removeAll { it.isDestroyed }
        buffs.removeAll{it.isConsumed}
        enemies.removeAll { it.isDead }
        pEnemy.removeAll{it.isDestroyed}
        bossList.removeAll{it.isDead}
        playerList.removeAll{it.isDead}



        if(!isPlaying && player.isDead) {
            bossMusic?.release()
            backgroundMusic?.release()
            val intent= Intent(context,GameOver::class.java)
            context.startActivity(intent)
        }
        if(!isPlaying && boss.isDead){
            bossMusic?.release()
            backgroundMusic?.release()
            finalTime=elapsedTime
            val intent=Intent(context,Victory::class.java)
            intent.putExtra("finalTime", finalTime)
            context.startActivity(intent)
        }
    }

```
a função _draw_ trata de desenhar tudo na tela, os bitmaps, os projeteis(que não usam bitmap), a barra de vida do jogador, o texto a dizer o tempo decorrido, o joystick bloqueando o surfaceHolder no inicio e depois metendo as alterações em efeito e desbloqueando o mesmo.

```kotlin
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
            for(b in buffs){
                canvas?.drawBitmap(b.bitmap,b.x,b.y,paint)
            }

            paint.textSize = 50f  // Tamanho do texto aumentado para 30
            paint.color = Color.BLACK  // Cor do texto
            val text = "Tempo: ${elapsedTime / 1000} segundos"
            val textWidth = paint.measureText(text)
            val x = (width - textWidth) / 2  // Centralizando o texto horizontalmente
            val y = 70f  // Posição vertical do texto
            canvas?.drawText(text, x, y, paint)







            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }
```
depois temos uma variável que _spawna_ periodicamente os projéteis do jogador usando a mesma lógica do _spawnEnemyProjectile_ mas usando um handler em vez do tempo do sistema(e também sendo mais rápido). Mais uma vez esta não tinha sido a nossa implementação original mas um _bug_ na parte do movimento com o disparo dos projeteis obrigou-nos a esta mudança e no _spawnEnemyProjectile_ o handler tava a dar problemas por alguma razão daí termos uma implementação diferente.

```kotlin
val shootRunnable = object : Runnable {
        override fun run() {
            for (p in playerList) {
                if (!p.isWalking && !p.isDead) {
                    val playerDir = p.getPlayerDirection()
                    projectile.add(
                        Projectile(
                            context,
                            width,
                            height,
                            p,
                            playerDir.first,
                            playerDir.second
                        )
                    )
                }
            }
            bulletHandler.postDelayed(this, 500)
        }
    }

```

por fim temos as funções _onTouchEvent_, _control_, _resume_ e _pause_. A função onTouchEvent, tal como o nome diz, trata da parte de quando há um toque no ecrã(ou não), neste caso quando há um toque no _joystick_ e movemos o centro do _joystick_ o jogador move-se e não dispara, quando o jogador pára de se mover dispara para a direção onde tá virado e o joystick volta à posição inicial. A função _control_ controla os _FPSs_ do jogo suspendendo a thread por 17 milissegundos. As funções _resume_ e _pause_ tratam de pausar e continuar o jogo mas no nosso jogo não as implementamos.
```kotlin
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

                if (!bulletHandler.hasCallbacks(shootRunnable)) {
                    bulletHandler.post(shootRunnable)
                }



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
        startTime = System.currentTimeMillis()

    }
    fun pause() {
        isPlaying = false
        gameThread?.join()
    }


}

```

Victory
-
O _screen_ de vitória, dá um som como referimos anteriormente, mostra o tempo que demoramos a conseguir ganhar e envia esse tempo para a _FireStore_ usando o _sendScore_ do _FirebaseHelpers_, tendo um botão para podermos tentar outra vez voltando à _GameActivity_

```kotlin
package ipca.game.projeto

import android.content.ContentValues.TAG
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Context


class Victory : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)
        val mediaPlayer=MediaPlayer.create(this,R.raw.victory)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mp->mp.release() }
        val finalTime = intent.getLongExtra("finalTime", 0)
        val textViewTime = findViewById<TextView>(R.id.textViewTime)

        // Convertendo o tempo para segundos
        val seconds = finalTime / 1000

        // Configurando o texto do TextView
        textViewTime.text = "Time: $seconds segundos"

        FirebaseHelpers.sendScore(seconds){

        }
        findViewById<ImageButton>(R.id.imageButtonVRetry).setOnClickListener {
            val intent= Intent(this,GameActivity::class.java)
            startActivity(intent)

        }

    }

}

```
GameOver
-
o _screen_ de derrota, dá o som de derrota, diz-nos que morremos e permite-nos tentar outra vez(com um botão) voltando à _GameActivity_

```kotlin
package ipca.game.projeto

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class GameOver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)
        val mediaPlayer= MediaPlayer.create(this,R.raw.gameover)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mp->mp.release() }


        findViewById<ImageButton>(R.id.imageButtonRetry).setOnClickListener {
            val intent= Intent(this,GameActivity::class.java)
            startActivity(intent)

        }
    }
}
```

Conclusão
=
Adoramos fazer este trabalho,tivemos algumas dificuldades principalmente no ínicio com o _joystick_ e o movimento do jogador mas a experiência foi mesmo muito enriquecedora e sentimos que aprendemos bastante e muito provavelmente vamos continuar a melhorá-lo e até publicá-lo.

