package si.uni_lj.fe.tnuv.smartslippers

import android.app.Service
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

@Suppress("DEPRECATION")
class CharacteristicsService : Service() {

    private lateinit var mainServiceIntent: Intent
    override fun onBind(p0: Intent?): IBinder? = null



    companion object {
        const val CHAR_UPDATED = "charUpdated"
        const val CHAR_EXTRA = "charExtra"
        const val CHAR_VALUE = "charValue"
        const val STEPS_VALUE = "stepsValue"
        var stepsCounter = 0
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
        if (extras != null) {
            val characteristicsName = extras?.getString(CHAR_EXTRA)
            val characteristicsValue = extras?.getString(CHAR_VALUE)
            var charNumSteps = extras?.getInt(STEPS_VALUE, 0)

            getNewValues(characteristicsName, characteristicsValue, charNumSteps)

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

    override fun onDestroy() {
        super.onDestroy()
        var IS_ACTIVITY_RUNNING = false
        var IS_FIRST_TIME = true
    }

    private fun resetTimer() {
        stopService(mainServiceIntent)
        //binding.tvLastActValue.text = "Live"

    }

    // Function that catch new values from Arduino Nano 33 BLE
    private fun getNewValues(charName: String?, charValue: String?, numSteps: Int) {
        val intent = Intent(CHAR_UPDATED)
        var charNumSteps = numSteps
        //Log.i("CHARSERV", "get char called.")
        when (charName) {
            "Walking" -> {
                //this.tvHojaValue.text = charValue.toString()
                if (charValue == "1") {
                    val extras = Bundle().apply {
                        putString(CHAR_EXTRA, charName)
                        putString(CHAR_VALUE, charValue)
                    }
                    intent.putExtras(extras)

                    //updateUiDecks(charName)
                    //hojaActivity.start() // added1
                    resetTimer()

                    Log.i("CHARSERV", "Hoja")
                    sendBroadcast(intent)


                }
            }
            "Idle" -> {
                //this.tvIdleValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    startTimer()
                    val extras = Bundle().apply {
                        putString(CHAR_EXTRA, charName)
                        putString(CHAR_VALUE, charValue)
                    }
                    intent.putExtras(extras)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Idle")
                }
            }
            "Stairs" -> {
                //this.tvStopniceValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    resetTimer()
                    val extras = Bundle().apply {
                        putString(CHAR_EXTRA, charName)
                        putString(CHAR_VALUE, charValue)
                    }
                    intent.putExtras(extras)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Stopnice")

                }
            }
            "Running" -> {
                //this.tvDvigaloValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    resetTimer()
                    val extras = Bundle().apply {
                        putString(CHAR_EXTRA, charName)
                        putString(CHAR_VALUE, charValue)
                    }
                    intent.putExtras(extras)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Tek")
                }
            }
            "Uncertain" -> {
                //this.tvUncertainValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    startTimer()
                    val extras = Bundle().apply {
                        putString(CHAR_EXTRA, charName)
                        putString(CHAR_VALUE, charValue)
                    }
                    intent.putExtras(extras)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Uncertain")
                    //lastActivityTime = System.currentTimeMillis()

                    //hojaActivity.stop() // added1
                }
            }
            "Fall" -> {
                //this.tvUncertainValue.text = charValue.toString()
                if (charValue == "1") {
                    //updateUiDecks(charName)
                    startTimer()
                    val extras = Bundle().apply {
                        putString(CHAR_EXTRA, charName)
                        putString(CHAR_VALUE, charValue)
                    }
                    intent.putExtras(extras)
                    sendBroadcast(intent)
                    Log.i("CHARSERV", "Uncertain")
                    //lastActivityTime = System.currentTimeMillis()

                    //hojaActivity.stop() // added1
                }
            }

            "Steps" -> {
                stepsCounter+= charValue?.toInt()!!
                val extras = Bundle().apply {
                    putString(CHAR_EXTRA, charName)
                    putString(CHAR_VALUE, charValue)
                    putInt(STEPS_VALUE, stepsCounter)
                }
                intent.putExtras(extras)
                sendBroadcast(intent)
                //lastActivityTime = System.currentTimeMillis()

                //hojaActivity.stop() // added1

            }


        }


        /*
        if (tvCurrActValue.toString() != charValue.toString()){
            this.tvCurrActValue.text = charName.toString()
        }
        */


    }
}