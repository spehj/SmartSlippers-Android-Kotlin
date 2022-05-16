package si.uni_lj.fe.tnuv.smartslippers

/*import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService

class sendNotificationsClass constructor(CHANNEL_ID : String, notificationId : Int) {

    val CHANNEL_ID = CHANNEL_ID
    val notificationId = notificationId

    fun createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "notification title"
            val descriptionText = "notificitation description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(){

        val intent = Intent(this, second_activity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)
        val bitmapLargeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Example Title")
            .setContentText("Example Description")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(bitmapLargeIcon)
            .setStyle((NotificationCompat.BigPictureStyle().bigPicture(bitmap)))
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }*/
//}