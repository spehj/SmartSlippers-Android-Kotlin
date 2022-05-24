package si.uni_lj.fe.tnuv.smartslippers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ForgotPasswordActivity : AppCompatActivity() {

    private val CHANNEL_ID = "channel_id_example_id_1"
    private val notificationId = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.forgot_password)

        createNotificationChannel()

        val sendNotification = findViewById<Button>(R.id.sendNotification)
        val email = findViewById<EditText>(R.id.enterEmail)

        sendNotification.setOnClickListener {

            val db = DBHelper(this,null)
            val cursor = db.count()
            var x : Long = 1
            cursor!!.moveToFirst()

            var forgottenPassword : String = "ta email ne obstaja"

            val enterEmail = email.text.toString()

            var enakost : Boolean = false

            var user = Users()

            do{
                user = db.getName(x)
                if(user.email == enterEmail){
                    forgottenPassword = user.password.toString()
                    enakost = true
                }
                x++
            }while(cursor.moveToNext())

            if(TextUtils.isEmpty(enterEmail)){
                email.error = "polje ne sme ostati prazno"
            }
            else if(!enakost){
                email.error = "vnesen email ne obstaja"
            }
            else{
                sendNotification(forgottenPassword)
            }
        }
    }

    private fun createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "notification title"
            val descriptionText = "notification description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(enterEmail : String){

        val intent = Intent(this, ForgotPasswordActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)
        val bitmapLargeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your password")
            .setContentText(enterEmail)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(bitmapLargeIcon)
            .setStyle((NotificationCompat.BigPictureStyle().bigPicture(bitmap)))
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }
}