package si.uni_lj.fe.tnuv.smartslippers

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothDevice
import android.content.*
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import si.uni_lj.fe.tnuv.smartslippers.ble.*
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager.isConnected
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager.readCharacteristic
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager.teardownConnection
import si.uni_lj.fe.tnuv.smartslippers.databinding.HomeActivityBinding
import java.lang.Boolean.getBoolean
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class HomeActivity : AppCompatActivity() {

    private var timeLastActServ: String? = ""
    private var timeOfActivity = 0.0
    private var numOfSteps = 0
    private var activityName = ""
    private val CHANNEL_ID = "channel_id_example_id_2"
    private val notificationId = 102
    private lateinit var device: BluetoothDevice
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationManager: NotificationManager

    // SERVICE
    private lateinit var binding: HomeActivityBinding
    private lateinit var serviceIntent: Intent
    private var timerStarted = false
    private lateinit var charServiceIntent : Intent
    private lateinit var activeServiceIntent :Intent

    // Insert activity values
    private lateinit var tvCurrActValue: TextView
    private lateinit var tvLastActValue: TextView
    private lateinit var tvActTimeValue: TextView
    private lateinit var tvStatusValue: TextView
    private lateinit var tvConnStatusIndicator: TextView
    private lateinit var tvConnStatusText: TextView
    private lateinit var tvButtonReconnect: Button


    companion object{
        var IS_FIRST_TIME = true
    }


    val hojaActivity = UserActivity("Hoja")

    private var lastActivityTime: Long = System.currentTimeMillis()


    private val characteristicMap = mutableMapOf<String, String>()

    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }
    private val characteristicProperties by lazy {
        characteristics.map { characteristic ->
            characteristic to mutableListOf<CharacteristicProperty>().apply {
                if (characteristic.isNotifiable()) add(CharacteristicProperty.Notifiable)
                if (characteristic.isIndicatable()) add(CharacteristicProperty.Indicatable)
                if (characteristic.isReadable()) add(CharacteristicProperty.Readable)
                if (characteristic.isWritable()) add(CharacteristicProperty.Writable)
                if (characteristic.isWritableWithoutResponse()) {
                    add(CharacteristicProperty.WritableWithoutResponse)
                }
            }.toList()
        }.toMap()
    }

    private var notifyingCharacteristics = mutableListOf<UUID>()

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        createNotificationChannel()


        // SERVICE
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.home_activity) // Old way

        //tvHojaValue = findViewById(R.id.)
        tvCurrActValue = findViewById(R.id.tvCurrActValue)
        tvLastActValue = findViewById(R.id.tvLastActValue)
        tvActTimeValue = findViewById(R.id.tvActTimeValue)
        tvStatusValue = findViewById(R.id.tvStatusValue)
        tvConnStatusIndicator = findViewById(R.id.tvConnStatusIndicator)
        tvConnStatusText = findViewById(R.id.tvConnStatusText)

        if (!MainService.IS_ACTIVITY_RUNNING) {

            Log.i("SERVICE", "Starting MainService")
        } else {
            Log.i("SERVICE", "MainService already running")
        }

        // SERVICE
        serviceIntent = Intent(applicationContext, MainService::class.java)
        charServiceIntent = Intent(applicationContext, CharacteristicsService::class.java)
        activeServiceIntent = Intent(applicationContext, ActiveTimeService::class.java)




        supportActionBar?.hide()
        characteristicMap.put("05ed8326-b407-11ec-b909-0242ac120002", "Walking")
        characteristicMap.put("f72e3316-b407-11ec-b909-0242ac120002", "Stairs")
        characteristicMap.put("05f99232-b408-11ec-b909-0242ac120002", "Running")
        characteristicMap.put("f7a9b8d6-b408-11ec-b909-0242ac120002", "Idle")
        characteristicMap.put("44f709ee-d2bf-11ec-9d64-0242ac120002", "Uncertain")
        characteristicMap.put("9318a796-d8f9-11ec-9d64-0242ac120002", "Fall")
        characteristicMap.put("684dc082-d8f9-11ec-9d64-0242ac120002", "Steps")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")


        if (IS_FIRST_TIME){
            ConnectionManager.registerListener(connectionEventListener)
            // Here we define all characteristics where we want notifications
            ConnectionManager.enableNotifications(device, characteristics[3])
            ConnectionManager.enableNotifications(device, characteristics[4])
            ConnectionManager.enableNotifications(device, characteristics[5])
            ConnectionManager.enableNotifications(device, characteristics[6])
            ConnectionManager.enableNotifications(device, characteristics[7])
            ConnectionManager.enableNotifications(device, characteristics[8])
            ConnectionManager.enableNotifications(device, characteristics[9])

            binding.tvActTimeValue.text = "0h 0min 0s"
            IS_FIRST_TIME = false
        }else{
            initialCharacteristicsRead()
            refreshValues()
            Log.i("RES", "Activ time. ${binding.tvActTimeValue.text}")

        }
        registerReceiver(updateTime, IntentFilter(MainService.TIMER_UPDATED))
        registerReceiver(updateCurrentActivity, IntentFilter(CharacteristicsService.CHAR_UPDATED))
        registerReceiver(updateActiveTime, IntentFilter(ActiveTimeService.ACTIVE_UPDATED))




        // Set initial BLE status indicator and text
        if (device.isConnected()) {
            //tvConnStatusIndicator.setBackgroundColor(Color.parseColor("#00D7AE"))
            tvConnStatusIndicator.setBackgroundResource(R.drawable.status_led);

            tvConnStatusText.text = "Slippers Connected"
        }

        // Listen for button clicks
        tvButtonReconnect = findViewById(R.id.tvButtonReconnect);
        tvButtonReconnect.setOnClickListener() {
            teardownConnection(device)
            val intent1 = Intent(this, ConnectionActivity::class.java)
            startActivity(intent1)
        }

        val settingsBtn = findViewById<Button>(R.id.settingsBtn)
        settingsBtn.setOnClickListener {
            val intent2 = Intent(this, SettingsActivity::class.java).putExtra(
                BluetoothDevice.EXTRA_DEVICE,
                device
            )
            startActivity(intent2)
        }

        val statsBtn = findViewById<Button>(R.id.statsBtn)
        statsBtn.setOnClickListener {
            val intent3 = Intent(this, StatisticsActivity::class.java).putExtra(
                BluetoothDevice.EXTRA_DEVICE,
                device
            )
            startActivity(intent3)
        }


                // Updating Text View at current
                // iteration




                // Thread sleep for 1 sec
                //Thread.sleep(1000)












    }

    override fun onResume() {
        super.onResume()
        initialCharacteristicsRead()
        //refreshValues()
        Log.i("RES", "RESUMED")
    }

    override fun onPause() {
        super.onPause()
        // Unregister receivers
        unregisterReceiver(updateTime)
        unregisterReceiver(updateCurrentActivity)
        unregisterReceiver(updateActiveTime)
        Log.i("CHARSERV", "Receivers unregisted.")
    }

    private fun refreshValues(){
        // Last activity
        binding.tvLastActValue.text = timeLastActServ
        Log.i("RES", "RESUMED last act: $timeLastActServ")
        // Number of steps
        binding.tvStepsValue.text = numOfSteps.toString()
        Log.i("RES", "RESUMED steps: ${numOfSteps.toString()}")
        // Current activity
        binding.tvCurrActValue.text = activityName
        Log.i("RES", "RESUMED current act: $activityName")
        // Active time
        binding.tvActTimeValue.text = getTimeStringFromDouble(ActiveTimeService.activeTime)
        Log.i("RES", "RESUMED active time: ${getTimeStringFromDouble(timeOfActivity)}")

    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            timeLastActServ = intent.getStringExtra(MainService.TIME_EXTRA)
            //Log.i("CHARSERV", "Time from last activity: $timeLastActServ")
            binding.tvLastActValue.text = timeLastActServ
            //Log.i("CHARSERV", "Time from last activity slide: ${binding.tvLastActValue.text}")
        }
    }

    private val updateCurrentActivity: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("LogNotTimber")
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            var newChar : String? = ""
            var newCharValue : String? = ""
            //var newStepsValue : Int = 0
            if (extras != null) {
                newChar = extras?.getString(CharacteristicsService.CHAR_EXTRA)
                newCharValue = extras?.getString(CharacteristicsService.CHAR_VALUE)
                numOfSteps = extras?.getInt(CharacteristicsService.STEPS_VALUE)

            }
            //val newChar = intent.getStringExtra(MainService.CHAR_EXTRA)
            Log.i("CHARSERV", "New value of char: $newChar")
            if (newChar != "Steps"){
                binding.tvCurrActValue.text = newChar
                activityName = newChar.toString()
                updateUiDecks(newChar)
                stopService(charServiceIntent)
            }
            else if(newChar == "Steps"){
                Log.i("CHARSERV", "Number of steps: ${numOfSteps.toString()}")
                //newCharValue?.toInt()?.let { countSteps(it) }
                binding.tvStepsValue.text = numOfSteps.toString()
                stopService(charServiceIntent)
            }

        }
    }

    private fun countSteps(newSteps : Int){
        numOfSteps += newSteps
    }

    private val updateActiveTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            timeOfActivity = intent.getDoubleExtra(ActiveTimeService.ACTIVE_EXTRA, 0.0)
            binding.tvActTimeValue.text = getTimeStringFromDouble(timeOfActivity)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt%86400/3600
        val minutes = resultInt%86400 % 3600 /60
        val seconds = resultInt % 86400 % 3600 % 3600 %60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Int, minutes: Int, seconds: Int): String = "${hours}h ${minutes}min ${seconds}s"

    private fun startActiveTimer(){
        activeServiceIntent.putExtra(ActiveTimeService.ACTIVE_EXTRA, timeOfActivity)
        startService(activeServiceIntent)
        //timerStarted = true
    }

    private fun stopActiveTimer(){
        stopService(activeServiceIntent)
    }

    private fun updateActivity(charName: String?, charValue: String){

        val extras = Bundle().apply {
            putString(CharacteristicsService.CHAR_EXTRA, charName)
            putString(CharacteristicsService.CHAR_VALUE, charValue)
        }
        charServiceIntent.putExtras(extras)
        startService(charServiceIntent)
    }

    private fun stopActivityService(){
        // Stop listening for characteristcs change from Arduino
        stopService(charServiceIntent)
    }

    private fun updateUiDecks(charName: String?) {
        tvCurrActValue.text = charName.toString()
        if ((charName.toString() == "Running") || (charName.toString() == "Walking")|| (charName.toString() == "Stairs")){
            tvLastActValue.text = "Live"
            startActiveTimer() // Start activity stopwatch
        }
        else if ((charName.toString() == "Uncertain") || (charName.toString() == "Idle") || (charName.toString() == "Fall")){
            stopActiveTimer() // Stop activity stopwatch
        }

        if(charName.toString() == "Fall"){
            tvStatusValue.setBackgroundResource(R.drawable.banner_red);
            tvStatusValue.text = "FALL DETECTED"
            // Send notification
            sendNotification("FALL DETECTED!")
            showAlertDialog()
        }
        else {
            tvStatusValue.setBackgroundResource(R.drawable.banner_green);
            tvStatusValue.text = "EVERYTHING IS GOOD"
        }

    }

    private fun initialCharacteristicsRead(){
        // Initial read of characteristics
        readCharacteristic(device, characteristics[3])
        readCharacteristic(device, characteristics[4])
        readCharacteristic(device, characteristics[5])
        readCharacteristic(device, characteristics[6])
        readCharacteristic(device, characteristics[7])
        readCharacteristic(device, characteristics[8])
        readCharacteristic(device, characteristics[9])
    }


    /*

    // Function that catch new values from Arduino Nano 33 BLE
    private fun getNewValues(charName: String?, charValue: Int) {
        when (charName) {
            "Hoja" -> {
                //this.tvHojaValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    hojaActivity.start() // added1
                    resetTimer()


                }
            }
            "Idle" -> {
                //this.tvIdleValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    startTimer()
                }
            }
            "Stopnice" -> {
                //this.tvStopniceValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    resetTimer()
                    // In real app you would use this in case of a fall
                    tvStatusValue.setBackgroundResource(R.drawable.banner_red);
                    tvStatusValue.text = "FALL DETECTED"

                    // Send notification
                    sendNotification("FALL DETECTED!")
                    showAlertDialog()

                } else {
                    tvStatusValue.setBackgroundResource(R.drawable.banner_green);
                    tvStatusValue.text = "EVERYTHING IS GOOD"
                }
            }
            "Tek" -> {
                //this.tvDvigaloValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    resetTimer()
                }
            }
            "Uncertain" -> {
                //this.tvUncertainValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    startTimer()
                    //lastActivityTime = System.currentTimeMillis()

                    hojaActivity.stop() // added1
                }
            }
        }

        /*
        if (tvCurrActValue.toString() != charValue.toString()){
            this.tvCurrActValue.text = charName.toString()
        }
        */


    }

     */

    @OptIn(ExperimentalStdlibApi::class)
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {

                    //tvConnStatusIndicator.setBackgroundColor(Color.parseColor("#CB1A5E"))
                    binding.tvConnStatusIndicator.setBackgroundResource(R.drawable.status_led_disconnected);
                    binding.tvConnStatusText.text = "Slippers Disconnected"


                    /*
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()

                     */


                }
            }
            onCharacteristicRead = { _, characteristic ->
                // Get the name and value of changed characteristic
                var characteristicName = characteristicMap[characteristic.uuid.toString()]
                var strReceived = characteristic.value.toHexString();
                // Parse string to get HEX value
                strReceived = strReceived.substring(2, 4)
                // Convert HEX to Int
                var intReceived = strReceived.toInt(16)


                Log.i("OPERATIONS", "Value read on ${characteristicName}: ${intReceived}")
                updateActivity(characteristicName, intReceived.toString())
                // Pass new values to activity
                /*
                runOnUiThread {
                    getNewValues(characteristicName, intReceived)
                }

                 */
            }

            onCharacteristicChanged = { _, characteristic ->

                // Get the name and value of changed characteristic
                var characteristicName = characteristicMap[characteristic.uuid.toString()]
                var strReceived = characteristic.value.toHexString();
                // Parse string to get HEX value
                strReceived = strReceived.substring(2, 4)
                // Convert HEX to Int
                var intReceived = strReceived.toInt(16)


                Log.i("OPERATIONS", "Value changed on ${characteristicName}: ${intReceived}")
                updateActivity(characteristicName, intReceived.toString())
                // Pass new values to activity
                /*
                runOnUiThread {
                    getNewValues(characteristicName, intReceived)
                }

                 */


            }

            onNotificationsEnabled = { _, characteristic ->
                Log.i(
                    "OPERATIONS",
                    "Enabled notifications on ${characteristicMap[characteristic.uuid.toString()]}"
                )
                notifyingCharacteristics.add(characteristic.uuid)
            }

            onNotificationsDisabled = { _, characteristic ->
                Log.i(
                    "OPERATIONS",
                    "Disabled notifications on ${characteristicMap[characteristic.uuid.toString()]}"
                )
                notifyingCharacteristics.remove(characteristic.uuid)
            }
        }
    }

    private enum class CharacteristicProperty {
        Readable,
        Writable,
        WritableWithoutResponse,
        Notifiable,
        Indicatable;

        val action
            get() = when (this) {
                Readable -> "Read"
                Writable -> "Write"
                WritableWithoutResponse -> "Write Without Response"
                Notifiable -> "Toggle Notifications"
                Indicatable -> "Toggle Indications"
            }
    }

    private fun createNotificationChannel() {
        Log.i("NOT", "Create")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SmartSlippers Notification"
            val descriptionText = "New event happened"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(enterEmail: String) {
        Log.i("NOT", "Send")


        var intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)


        /* NOT WORKING
        val intent = packageManager.getLaunchIntentForPackage("si.uni_lj.fe.tnuv.smartslippers")
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

         */

        /*
        val pm = packageManager
        val notificationIntent = pm.getLaunchIntentForPackage("si.uni_lj.fe.tnuv.smartslippers")
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            notificationIntent, 0
        )

         */

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val bitmap =
            BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)
        val bitmapLargeIcon =
            BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("FALL DETECTED")
            .setContentText(enterEmail)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(bitmapLargeIcon)
            .setStyle((NotificationCompat.BigPictureStyle().bigPicture(bitmap)))
            .setContentIntent(pendingIntent)
        //.setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun showAlertDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@HomeActivity)
        alertDialog.setTitle("Fall has been detected")
        alertDialog.setMessage("Do you wat to make a call?")
        alertDialog.setPositiveButton(
            "yes"
        ) { _, _ ->
            Toast.makeText(this@HomeActivity, "Calling the first contact.", Toast.LENGTH_LONG)
                .show()
        }
        alertDialog.setNegativeButton(
            "No"
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }
}