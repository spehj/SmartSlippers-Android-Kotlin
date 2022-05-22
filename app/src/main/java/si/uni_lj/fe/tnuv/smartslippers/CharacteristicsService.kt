package si.uni_lj.fe.tnuv.smartslippers

import android.app.Service
import android.content.Intent
import android.content.Intent.getIntent
import android.os.IBinder
import android.util.Log

@Suppress("DEPRECATION")
class CharacteristicsService: Service() {
    private lateinit var mainServiceIntent: Intent
    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val CHAR_UPDATED = "charUpdated"
        const val CHAR_EXTRA = "charExtra"
        const val CHAR_VALUE = "charValue"
        var IS_ACTIVITY_RUNNING = false
        var IS_FIRST_TIME = true

    }
    override fun onCreate() {
        super.onCreate()
        IS_ACTIVITY_RUNNING = true
        mainServiceIntent = Intent(applicationContext, MainService::class.java)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras
        if (extras != null){
            val characteristicsName = extras?.getString(CHAR_EXTRA)
            val characteristicsValue = extras?.getString(CHAR_VALUE)

            getNewValues(characteristicsName, characteristicsValue)

        }

        if (IS_FIRST_TIME) {
            IS_FIRST_TIME = false
            // send new data to main thread
        }

        return START_NOT_STICKY
    }

    private fun startTimer() {
        // Timer from last activity
        mainServiceIntent.putExtra(MainService.TIME_EXTRA, System.currentTimeMillis())
        startService(mainServiceIntent)

    }

    private fun resetTimer() {
        stopService(mainServiceIntent)
        //binding.tvLastActValue.text = "Live"

    }

    /*
    private fun updateUiDecks(charName: String?) {
        tvCurrActValue.text = charName.toString()
        if (charName.toString() != "Uncertain") {
            tvLastActValue.text = "Live"
        }

    }

     */

    // Function that catch new values from Arduino Nano 33 BLE
    private fun getNewValues(charName: String?, charValue: String?) {
        val intent = Intent(CHAR_UPDATED)
        Log.i("CHARSERV", "get char called.")
        when (charName) {
            "Hoja" -> {
                //this.tvHojaValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    //hojaActivity.start() // added1
                    resetTimer()
                    intent.putExtra(CHAR_EXTRA, charName)
                    Log.i("CHARSERV", "Hoja")
                    sendBroadcast(intent)


                }
            }
            "Idle" -> {
                //this.tvIdleValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    startTimer()
                    intent.putExtra(CHAR_EXTRA, charName)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Idle")
                }
            }
            "Stopnice" -> {
                //this.tvStopniceValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    resetTimer()
                    intent.putExtra(CHAR_EXTRA, charName)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Stopnice")
                    // In real app you would use this in case of a fall
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
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    resetTimer()
                    intent.putExtra(CHAR_EXTRA, charName)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Tek")
                }
            }
            "Uncertain" -> {
                //this.tvUncertainValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    startTimer()
                    intent.putExtra(CHAR_EXTRA, charName)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Uncertain")
                    //lastActivityTime = System.currentTimeMillis()

                    //hojaActivity.stop() // added1
                }
            }
        }


        /*
        if (tvCurrActValue.toString() != charValue.toString()){
            this.tvCurrActValue.text = charName.toString()
        }
        */


    }
}