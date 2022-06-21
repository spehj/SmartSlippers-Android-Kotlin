package si.uni_lj.fe.tnuv.smartslippers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionEventListener
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager
import timber.log.Timber


class SplashActivity : AppCompatActivity() {



    var userPravilni = Users()
    var login : Int? = null
    var user = Users()
    val db = DBHelper(this, null)

    // This is the loading time of the splash screen
    private val SPLASH_TIME_OUT:Long = 3000 // 1 sec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // remove title bar in app activity
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()

        setContentView(R.layout.activity_splash)

        loadData()

        // ADDED
        if (ContextCompat.checkSelfPermission(this@SplashActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@SplashActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@SplashActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@SplashActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
        var x = userPravilni.id?.toLong()
        user = db.getName(x)


        //startBleScan()


        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            // startActivity(Intent(this,MainActivity::class.java))
            //ConnectionManager.connect(this, this@SplashActivity)
            if(login == 1){
                //ConnectionManager.registerListener(connectionEventListener)
                //stopBleScan()

                val intent = Intent(this, SplashActivityConnect::class.java)
                startActivity(intent)
            }
            else if(login == 2){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            // close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }

    override fun onResume() {
        super.onResume()
    }




    private fun loadData() {
        val sharedPrefereces = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedString = sharedPrefereces.getString("STRING_KEY", null)
        val savedInt = sharedPrefereces.getInt("INT_KEY", 2)

        userPravilni.id = savedString
        login = savedInt

        //Toast.makeText(this, "${userPravilni.id}, $login", Toast.LENGTH_SHORT).show()
    }
}