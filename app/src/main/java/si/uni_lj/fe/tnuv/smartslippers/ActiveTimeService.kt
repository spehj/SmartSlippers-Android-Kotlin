package si.uni_lj.fe.tnuv.smartslippers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

class ActiveTimeService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    companion object{
        const val ACTIVE_UPDATED = "activeUpdated"
        const val ACTIVE_EXTRA = "activeExtra"
        var activeTime : Double = 0.0 // Current sum of active time
        var lastActiveTime : Double = 0.0   // Sum of active times in the past
        var currentActiveTime : Double = 0.0 // Active time in this period
        var IS_ACTIVITY_RUNNING = false
    }

    override fun onCreate() {
        super.onCreate()
    }

    private val timer = Timer()


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //val time = intent.getDoubleExtra(ACTIVE_EXTRA, 0.0)
        Log.i("RES", "ON START: last active time: $lastActiveTime")
        if (!IS_ACTIVITY_RUNNING) {
            timer.scheduleAtFixedRate(TimeTask(currentActiveTime), 0, 1000)
            IS_ACTIVITY_RUNNING = true
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        lastActiveTime = activeTime // Add active time in this period to sum of last active times
        currentActiveTime = 0.0
        Log.i("RES", "ON DESTROY: last active time: $lastActiveTime")
        timer.cancel()
        timer.purge()
        super.onDestroy()
        IS_ACTIVITY_RUNNING = false

    }

    private inner class TimeTask(private var time: Double) : TimerTask(){
        override fun run() {
            val intent = Intent(ACTIVE_UPDATED)
            time++
            currentActiveTime = time
            Log.i("RES", "Current Active time: ${currentActiveTime}")
            activeTime = lastActiveTime + currentActiveTime
            Log.i("RES", "Sum Active time: ${activeTime}")

            intent.putExtra(ACTIVE_EXTRA, activeTime)
            sendBroadcast(intent)
        }

    }


}