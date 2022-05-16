package si.uni_lj.fe.tnuv.smartslippers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionEventListener
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager.isConnected
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager.readCharacteristic
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager.teardownConnection
import si.uni_lj.fe.tnuv.smartslippers.ble.isIndicatable
import si.uni_lj.fe.tnuv.smartslippers.ble.isNotifiable
import si.uni_lj.fe.tnuv.smartslippers.ble.isReadable
import si.uni_lj.fe.tnuv.smartslippers.ble.isWritable
import si.uni_lj.fe.tnuv.smartslippers.ble.isWritableWithoutResponse
import si.uni_lj.fe.tnuv.smartslippers.ble.toHexString
import java.util.Date
import java.util.Locale
import java.util.UUID


class HomeActivity : AppCompatActivity() {
    private val CHANNEL_ID = "channel_id_example_id_2"
    private val notificationId = 102
    private lateinit var device: BluetoothDevice
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationManager: NotificationManager

    // Insert activity values
    private lateinit var tvHojaValue: TextView
    private lateinit var tvIdleValue: TextView
    private lateinit var tvStopniceValue: TextView
    private lateinit var tvDvigaloValue: TextView
    private lateinit var tvUncertainValue: TextView
    private lateinit var tvCurrActValue: TextView
    private lateinit var tvLastActValue: TextView
    private lateinit var tvActTimeValue: TextView
    private  lateinit var tvStatusValue: TextView
    private lateinit var tvConnStatusIndicator : TextView
    private lateinit var tvConnStatusText: TextView
    private lateinit var tvButtonReconnect: Button

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
    /*
    private val characteristicAdapter: CharacteristicAdapter by lazy {
        CharacteristicAdapter(characteristics) {}
    }

     */
    private var notifyingCharacteristics = mutableListOf<UUID>()

    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionManager.registerListener(connectionEventListener)
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        createNotificationChannel()
        supportActionBar?.hide()
        characteristicMap.put("05ed8326-b407-11ec-b909-0242ac120002", "Hoja")
        characteristicMap.put("f72e3316-b407-11ec-b909-0242ac120002", "Stopnice")
        characteristicMap.put("05f99232-b408-11ec-b909-0242ac120002", "Tek")
        characteristicMap.put("f7a9b8d6-b408-11ec-b909-0242ac120002", "Idle")
        characteristicMap.put("44f709ee-d2bf-11ec-9d64-0242ac120002", "Uncertain")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")
        setContentView(R.layout.home_activity)

        //tvHojaValue = findViewById(R.id.)
        tvCurrActValue = findViewById(R.id.tvCurrActValue)
        tvLastActValue = findViewById(R.id.tvLastActValue)
        tvActTimeValue = findViewById(R.id.tvActTimeValue)
        tvStatusValue = findViewById(R.id.tvStatusValue)
        tvConnStatusIndicator = findViewById(R.id.tvConnStatusIndicator)
        tvConnStatusText = findViewById(R.id.tvConnStatusText)

        // Set initial BLE status indicator and text
        if (device.isConnected()){
            //tvConnStatusIndicator.setBackgroundColor(Color.parseColor("#00D7AE"))
                tvConnStatusIndicator.setBackgroundResource(R.drawable.status_led);

            tvConnStatusText.text = "Slippers Connected"
        }
        tvButtonReconnect = findViewById(R.id.tvButtonReconnect);

        tvButtonReconnect.setOnClickListener(){
            teardownConnection(device)
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)
        }

        val settingsBtn = findViewById<Button>(R.id.settingsBtn)

        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Initial read of characteristics
        readCharacteristic(device, characteristics[3])
        readCharacteristic(device, characteristics[4])
        readCharacteristic(device, characteristics[5])
        readCharacteristic(device, characteristics[6])
        readCharacteristic(device, characteristics[7])

        // Here we define all characteristics where we want notifications
        ConnectionManager.enableNotifications(device, characteristics[3])
        ConnectionManager.enableNotifications(device, characteristics[4])
        ConnectionManager.enableNotifications(device, characteristics[5])
        ConnectionManager.enableNotifications(device, characteristics[6])
        ConnectionManager.enableNotifications(device, characteristics[7])

        // Declaring Main Thread
        Thread(Runnable {
            while (true) {
                // Updating Text View at current
                // iteration
                runOnUiThread {
                    if (tvCurrActValue.text == "Uncertain") {
                        tvLastActValue.text = timeFromActivity(lastActivityTime)

                    }
                    tvActTimeValue.text = hojaActivity.current()
                }

                // Thread sleep for 1 sec
                Thread.sleep(1000)
                // Updating Text View at current
                // iteration
                //runOnUiThread{ tv.text = msg2 }
                // Thread sleep for 1 sec
                //Thread.sleep(1000)
            }
        }).start()
    }

    override fun onDestroy() {
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        super.onDestroy()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("TAG", "Option selected $item")
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun timeFromActivity(timeOfLastActivity: Long): String {
        val timeNow: Long = System.currentTimeMillis()
        val timeDifference: Long = timeNow - timeOfLastActivity
        // timeDifference is in milliseconds
        var seconds = timeDifference / 1000
        var minutes = seconds / 60
        var hours = minutes / 60
        var secondsLeft = seconds - (60 * minutes)
        val minutesLeft = minutes - (60 * hours)

        var result = if (hours >= 24) {
            ">1 day"
        } else {
            "${hours}h ${minutesLeft}min ${secondsLeft}s ago"
        }
        return result
    }

    private fun updateUiDecks(charName: String?) {
        tvCurrActValue.text = charName.toString()
        if (charName.toString() != "Uncertain") {
            tvLastActValue.text = "Live"
        }

    }

    // Function that catch new values from Arduino Nano 33 BLE
    private fun getNewValues(charName: String?, charValue: Int) {
        when (charName) {
            "Hoja" -> {
                //this.tvHojaValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    hojaActivity.start() // added1


                }
            }
            "Idle" -> {
                //this.tvIdleValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                }
            }
            "Stopnice" -> {
                //this.tvStopniceValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    // In real app you would use this in case of a fall
                    tvStatusValue.setBackgroundResource(R.drawable.banner_red);
                    tvStatusValue.text = "FALL DETECTED"

                    // Send notification
                    sendNotification("FALL DETECTED!")

                }else{
                    tvStatusValue.setBackgroundResource(R.drawable.banner_green);
                    tvStatusValue.text = "EVERYTHING IS GOOD"
                }
            }
            "Tek" -> {
                //this.tvDvigaloValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                }
            }
            "Uncertain" -> {
                //this.tvUncertainValue.text = charValue.toString()
                if (charValue == 1) {
                    updateUiDecks(charName)
                    lastActivityTime = System.currentTimeMillis()

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

    @OptIn(ExperimentalStdlibApi::class)
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {

                    //tvConnStatusIndicator.setBackgroundColor(Color.parseColor("#CB1A5E"))
                    tvConnStatusIndicator.setBackgroundResource(R.drawable.status_led_disconnected);
                    tvConnStatusText.text = "Slippers Disconnected"


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
                // Pass new values to activity
                runOnUiThread {
                    getNewValues(characteristicName, intReceived)
                }
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
                // Pass new values to activity
                runOnUiThread {
                    getNewValues(characteristicName, intReceived)
                }


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

    private fun createNotificationChannel(){
        Log.i("NOT", "Create")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "notification title"
            val descriptionText = "notification description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(enterEmail : String){
        Log.i("NOT", "Send")
        /*
        val intent = Intent(this, HomeActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        */
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)



        val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)
        val bitmapLargeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logocopati)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("FALL DETECTED")
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