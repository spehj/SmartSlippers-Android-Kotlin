package si.uni_lj.fe.tnuv.smartslippers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.*

class ActiveTimeService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    companion object{
        const val ACTIVE_UPDATED = "activeUpdated"
        const val ACTIVE_EXTRA = "activeExtra"
        var activeTime : Double = 0.0
        var IS_ACTIVITY_RUNNING = false
    }

    override fun onCreate() {
        super.onCreate()
    }

    private val timer = Timer()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getDoubleExtra(ACTIVE_EXTRA, 0.0)
        if (!IS_ACTIVITY_RUNNING) {
            timer.scheduleAtFixedRate(TimeTask(activeTime), 0, 1000)
            IS_ACTIVITY_RUNNING = true
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        //activeTime = 0.0
        timer.cancel()
        timer.purge()
        super.onDestroy()
        IS_ACTIVITY_RUNNING = false

    }

    private inner class TimeTask(private var time: Double) : TimerTask(){
        override fun run() {
            val intent = Intent(ACTIVE_UPDATED)
            time++
            intent.putExtra(ACTIVE_EXTRA,time)
            sendBroadcast(intent)
        }

    }


}