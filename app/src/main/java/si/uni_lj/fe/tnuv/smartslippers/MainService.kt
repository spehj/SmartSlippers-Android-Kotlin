package si.uni_lj.fe.tnuv.smartslippers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

class MainService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null


    override fun onCreate() {
        super.onCreate()
        IS_ACTIVITY_RUNNING = true


    }

    private val timer = Timer()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getLongExtra(TIME_EXTRA, 0L)
        if (IS_FIRST_TIME){
            IS_FIRST_TIME = false
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        }
        else{
            Log.i("SERVICE", "Timer already has an instance.")
        }


        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
        IS_ACTIVITY_RUNNING = false
        IS_FIRST_TIME = true
    }


    // Add code to reset at midnight every day for active time
    private inner class TimeTask(private var time: Long) : TimerTask() {
        override fun run() {
            val intent = Intent(TIMER_UPDATED)
            val timeFromLastAc: String = timeFromActivity(time)
            //Log.i("SERVICE", "Service sending")
            intent.putExtra(TIME_EXTRA, timeFromLastAc)
            sendBroadcast(intent)

        }
    }


    private fun timeFromActivity(timeOfLastActivity: Long): String {
        val timeNow: Long = System.currentTimeMillis()
        val timeDifference: Long = timeNow - timeOfLastActivity
        // timeDifference is in milliseconds
        val seconds = timeDifference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val secondsLeft = seconds - (60 * minutes)
        val minutesLeft = minutes - (60 * hours)

        val result = if (hours >= 24) {
            ">1 day"
        } else {
            "${hours}h ${minutesLeft}min ${secondsLeft}s ago"
        }
        return result
    }


    companion object {
        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
        var IS_ACTIVITY_RUNNING = false
        var IS_FIRST_TIME = true

    }
}