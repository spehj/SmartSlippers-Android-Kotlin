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


class SplashActivityConnect : AppCompatActivity() {

    val TAG = "HOLDER"

    var userPravilni = Users()
    var login : Int? = null
    var user = Users()
    val db = DBHelper(this, null)

    // This is the loading time of the splash screen
    private val SPLASH_TIME_OUT:Long = 10000 // 5 sec


    // Properties
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            if (isScanning) {
                Log.i("HOLDER", "Trying to find SmartSlippers...")
                isScanning = false
                stopBleScan()
            }
            /*
            with(result.device) {
                Log.i("HOLDER", "MAC found: ${result.device.address}")
                if (result.device.address == user.mac){
                    ConnectionManager.connect(this, this@SplashActivityConnect)
                    Timber.w("Connecting to $address")
                    Log.i("HOLDER", "Connecting to known MAC.")
                }
                else{
                    Log.i("HOLDER", "MAC not known")
                }


                    // TUKAJ SHRANI address v bazo
                    //user.mac = address.toString()
                    //db.updateMac(user)

                }
                //ConnectionManager.connect(this, this@MainActivity)

             */
            }


        }


    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

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

        setContentView(R.layout.activity_splash_connect)

        loadData()


        // ADDED
        if (ContextCompat.checkSelfPermission(this@SplashActivityConnect,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@SplashActivityConnect,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@SplashActivityConnect,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@SplashActivityConnect,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
        //ConnectionManager.registerListener(connectionEventListener)
        var x = userPravilni.id!!.toLong()
        user = db.getName(x)


        startBleScan()


        /*
        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            // startActivity(Intent(this,MainActivity::class.java))
            //ConnectionManager.connect(this, this@SplashActivity)
            /*
            if(login == 1){

                stopBleScan()

                //val intent = Intent(this, HomeActivity::class.java)
                //startActivity(intent)
            }
            else if(login == 2){
                val intent = Intent(this, ConnectionActivity::class.java)
                startActivity(intent)
            }

             */
            // close this activity
            stopBleScan()
            isScanning = false
            finish()
        }, SPLASH_TIME_OUT)

         */
    }

    override fun onResume() {
        super.onResume()
        ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }




    private fun loadData() {
        val sharedPrefereces = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedString = sharedPrefereces.getString("STRING_KEY", null)
        val savedInt = sharedPrefereces.getInt("INT_KEY", 2)

        userPravilni.id = savedString
        login = savedInt

        //Toast.makeText(this, "${userPravilni.id}, $login", Toast.LENGTH_SHORT).show()
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            Log.i(TAG, "NO PERMISSION")
            requestLocationPermission()
        } else {
            Log.i(TAG, "Starting to SCAN")
            Toast.makeText(this@SplashActivityConnect, "Searching for your SmartSlippers", Toast.LENGTH_SHORT).show()
            Log.i("TAG", "SCANNIN AGAIN WITHOUT NEED")
            //Timber.i("Starting TO SCAN")
            val scanFilters : List<ScanFilter>
            scanFilters = ArrayList()
            val deviceMAC = user.mac
            Log.i(TAG, "deviceMAC: ${deviceMAC}")
            val scanFilter = ScanFilter.Builder().setDeviceAddress(deviceMAC).build()
            //val scanFilter = ScanFilter.Builder().setDeviceMAC(deviceMAC).build()
            scanFilters.add(scanFilter)

            scanResults.clear()
            //Log.i("ConnectionActivity", "Scan results")
            scanResultAdapter.notifyDataSetChanged()
            //Log.i("ConnectionActivity", "Scan results notify")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                promptEnableBluetooth()

            }
            bleScanner.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i(TAG, "Device address is: ${result.device.address}")
            ConnectionManager.connect(result.device, this@SplashActivityConnect)
            Log.i("HOLDER", "Connecting to known MAC: ${result.device.address}.")
            stopBleScan()

        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(this@SplashActivityConnect,
                    Manifest.permission.ACCESS_FINE_LOCATION) !==
                PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@SplashActivityConnect,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this@SplashActivityConnect,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    ActivityCompat.requestPermissions(this@SplashActivityConnect,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ContextCompat.startActivity(this!!, enableBtIntent, null)
        }
    }



    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                //connect_button.setOnClickListener {
                //   ConnectionManager.connect(gatt.device, this@MainActivity)
                Intent(this@SplashActivityConnect, HomeActivity::class.java).also {
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
                    startActivity(it) }
                ConnectionManager.unregisterListener(this)
            }
            onDisconnect = {
                runOnUiThread {
                    val builder = AlertDialog.Builder(this@SplashActivityConnect)
                    builder.setTitle("Disconnected")
                    builder.setMessage("Disconnected or unable to connect to device. ")
                    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                        Toast.makeText(applicationContext,
                            android.R.string.ok, Toast.LENGTH_SHORT).show()
                    }
                    builder.show()


                }
            }
        }
    }

    private fun stopBleScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //requestLocationPermission()
            //promptEnableBluetooth()

        }
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Location permission required")
            builder.setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                    "location access in order to scan for BLE devices.")
            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.ok, Toast.LENGTH_SHORT).show()
            }
            builder.show()

        }
    }


    /*******************************************
     * Extension functions
     *******************************************/

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}
