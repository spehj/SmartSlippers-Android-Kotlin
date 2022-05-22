package si.uni_lj.fe.tnuv.smartslippers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*
import kotlin.concurrent.thread

class MainService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null


    override fun onCreate() {
        super.onCreate()



    }

    private val timer = Timer()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getLongExtra(TIME_EXTRA, 0L)


        /*
        if (IS_FIRST_TIME){
            IS_FIRST_TIME = false
            thread {
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            }

        }

         */
        if (!IS_ACTIVITY_RUNNING){
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            IS_ACTIVITY_RUNNING = true
        }
        else{
            Log.i("SERVICE", "Timer already running.")
        }




        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        timer.purge()
        super.onDestroy()
        IS_ACTIVITY_RUNNING = false

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


    /*
    // use in HomeActivity
    private fun updateUiDecks(charName: String?) {
        tvCurrActValue.text = charName.toString()
        if (charName.toString() != "Uncertain") {
            tvLastActValue.text = "Live"
        }

    }


    // Function that catch new values from Arduino Nano 33 BLE
    private fun getNewValues(charName: String?, charValue: Int) {
        val intent = Intent(CHAR_UPDATED)
        when (charName) {
            "Hoja" -> {
                //this.tvHojaValue.text = charValue.toString()
                if (charValue == 1) {
                    intent.putExtra(TIME_EXTRA, charName)
                    sendBroadcast(intent)

                    //hojaActivity.start() // added1

                }
            }
            "Idle" -> {
                //this.tvIdleValue.text = charValue.toString()
                if (charValue == 1) {
                    intent.putExtra(TIME_EXTRA, charName)
                    //startTimer()
                }
            }
            "Stopnice" -> {
                //this.tvStopniceValue.text = charValue.toString()
                if (charValue == 1) {
                    intent.putExtra(TIME_EXTRA, charName)
                    //resetTimer()
                    // In real app you would use this in case of a fall

                    // ADD TO HOME ACTIVITY
                    //tvStatusValue.setBackgroundResource(R.drawable.banner_red);
                    //tvStatusValue.text = "FALL DETECTED"

                    // Send notification
                    //sendNotification("FALL DETECTED!")
                    //showAlertDialog()

                } else {

                    //tvStatusValue.setBackgroundResource(R.drawable.banner_green);
                    //tvStatusValue.text = "EVERYTHING IS GOOD"
                }
            }
            "Tek" -> {
                //this.tvDvigaloValue.text = charValue.toString()
                if (charValue == 1) {
                    intent.putExtra(TIME_EXTRA, charName)
                    //resetTimer()
                }
            }
            "Uncertain" -> {
                //this.tvUncertainValue.text = charValue.toString()
                if (charValue == 1) {
                    intent.putExtra(TIME_EXTRA, charName)
                    //startTimer()
                    //lastActivityTime = System.currentTimeMillis()

                    //hojaActivity.stop() // added1
                }
            }
        }

        // Send value back to HomeActivity
        sendBroadcast(intent)
    }

     */





    companion object {
        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
        const val CHAR_UPDATED = "charUpdated"
        const val CHAR_EXTRA = "charExtra"
        var IS_ACTIVITY_RUNNING = false
        var IS_FIRST_TIME = true

    }


}