package si.uni_lj.fe.tnuv.smartslippers

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var device: BluetoothDevice
    var user = Users()
    var login : Int? = null
    var notifications : Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")

        setContentView(R.layout.settings_activity)

        val intent = intent

        loadData()
        loadDataNotifications()

        // user.id = intent.getStringExtra("key1")
        /*user.fname = intent.getStringExtra("key2")
        user.lname = intent.getStringExtra("key3")
        user.email = intent.getStringExtra("key4")
        user.password = intent.getStringExtra("key5")
        user.phone = intent.getStringExtra("key6")*/

        val db = DBHelper(this, null)
        var x = user.id!!.toLong()

        user = db.getName(x)

        val username = findViewById<TextView>(R.id.username)
        val confirmPasswordBtn = findViewById<Button>(R.id.confirmPasswordBtn)
        val newPassword = findViewById<EditText>(R.id.newPassword)
        val notificationsSwitch = findViewById<Switch>(R.id.notificationsSwitch)

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                notifications = 1
                saveDataNotifications()
            } else {
                notifications = 2
                saveDataNotifications()
            }
        }

        username.text = user.fname + " " + user.lname

        confirmPasswordBtn.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            // intent.putExtra("keyLogin", 2)
            startActivity(intent)
            saveData()
        }

        val settingsBtn = findViewById<Button>(R.id.statsBtn)

        settingsBtn.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }


        val homeBtn = findViewById<Button>(R.id.homeBtn)

        homeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).also {
                it.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                startActivity(it) }
        }
    }

    private fun saveData(){

        val userID = null

        val sharedPrefereces = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefereces.edit()
        var login1 = 2
        editor.apply {
            putString("STRING_KEY", userID)
            putInt("INT_KEY", login1)
        }.apply()
    }

    private fun loadData() {
        val sharedPrefereces = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedString = sharedPrefereces.getString("STRING_KEY", null)
        val savedInt = sharedPrefereces.getInt("INT_KEY", 2)

        user.id = savedString
        login = savedInt

        Toast.makeText(this, "${user.id}, $login", Toast.LENGTH_SHORT).show()

    }

    private fun saveDataNotifications(){

        val sharedPrefereces = getSharedPreferences("sharedPrefsNotifications", Context.MODE_PRIVATE)
        val editor = sharedPrefereces.edit()

        editor.apply {
            putInt("INT_KEY_NOTIFICATIONS", notifications)
        }.apply()
    }

    private fun loadDataNotifications() {
        val sharedPrefereces = getSharedPreferences("sharedPrefsNotifications", Context.MODE_PRIVATE)
        val savedInt = sharedPrefereces.getInt("INT_KEY_NOTIFICATIONS", 2)

        notifications = savedInt

        Toast.makeText(this, "$notifications", Toast.LENGTH_SHORT).show()

    }
}
