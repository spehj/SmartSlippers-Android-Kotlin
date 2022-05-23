package si.uni_lj.fe.tnuv.smartslippers
import android.Manifest
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
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.button.MaterialButtonToggleGroup
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionEventListener
import si.uni_lj.fe.tnuv.smartslippers.ble.ConnectionManager
import timber.log.Timber
import androidx.core.content.ContextCompat.startActivity
import si.uni_lj.fe.tnuv.smartslippers.ConnectionActivity as ConnectionActivity1

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
class ConnectionActivity : AppCompatActivity(){
    private lateinit var serviceIntent: Intent
    private lateinit var charServiceIntent: Intent
    private lateinit var  activeServiceIntent: Intent
    private lateinit var scanButton: Button
    private lateinit var connectButton : Button
    private lateinit var scanResultsRecView : RecyclerView
    private  lateinit var scanRow: ConstraintLayout

    /*******************************************
     * Properties
     *******************************************/

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
        set(value) {
            field = value
            runOnUiThread { scanButton.text = if (value) "Stop Scan" else "Start Scan" }
            // runOnUiThread { tvStatusBle.text  = if(value != lastValue) value.toString() else lastValue }
        }

    /*
    // We get someValue from a function that detects change in bluetooth property
    private var predictionValue = someValue
        set(value){
            runOnUiThread { tvStatusBle.text  = if(value != lastValue) value else lastValue }
        }
    */


    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            if (isScanning) {
                Log.i("HOLDER", "Trying to find arduino...")
                stopBleScan()
            }
            with(result.device) {
                // Dodaj da se izbrano polje obarva modro
                scanRow = findViewById(R.id.connect_row_id)
                scanRow.setSelected(true)
                Timber.w("Connecting to $address")
                connectButton.setOnClickListener {
                    ConnectionManager.connect(this, this@ConnectionActivity)
                }
                //ConnectionManager.connect(this, this@MainActivity)
            }
        }
    }


    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)



    /*******************************************
     * Activity function overrides
     *******************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_connect)
        // Stop services if reconnecting
        serviceIntent = Intent(applicationContext, MainService::class.java)
        charServiceIntent = Intent(applicationContext, CharacteristicsService::class.java)
        activeServiceIntent = Intent(applicationContext, ActiveTimeService::class.java)
        stopService(charServiceIntent)
        stopService(serviceIntent)
        stopService(activeServiceIntent)



        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        //requestLocationPermission()
        //promptEnableBluetooth()


        // ADDED
        if (ContextCompat.checkSelfPermission(this@ConnectionActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ConnectionActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@ConnectionActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@ConnectionActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }


        scanButton = findViewById(R.id.scan_button)
        connectButton = findViewById(R.id.connect_button)
        scanButton.setOnClickListener { if (isScanning) stopBleScan() else startBleScan() }
        scanResultsRecView = findViewById(R.id.scan_results_recycler_view)
        Log.i("MainActivity", "Opened main activity")

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    /*******************************************
     * Private functions
     *******************************************/


    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(this@ConnectionActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) !==
                PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@ConnectionActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this@ConnectionActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    ActivityCompat.requestPermissions(this@ConnectionActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
            startActivity(this!!, enableBtIntent, null)
        }
    }



    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            Log.i("MainActivity", "NO PERMISSION")
            requestLocationPermission()
        } else {
            Log.i("ConnectionActivity", "Starting to SCAN")
            Toast.makeText(this@ConnectionActivity, "Searching for devices", Toast.LENGTH_SHORT).show()
            //Timber.i("Starting TO SCAN")
            val scanFilters : List<ScanFilter>
            scanFilters = ArrayList()
            val deviceName = "SmartSlippers"
            val scanFilter = ScanFilter.Builder().setDeviceName(deviceName).build()
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

    private fun setupRecyclerView() {
        scanResultsRecView.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@ConnectionActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scanResultsRecView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("ConnectionActivity", " before adapter")
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result

                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    //Log.i("MainActivity", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                    //Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                //connect_button.setOnClickListener {
                //   ConnectionManager.connect(gatt.device, this@MainActivity)
                Intent(this@ConnectionActivity, HomeActivity::class.java).also {
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
                    startActivity(it) }


                //}
                ConnectionManager.unregisterListener(this)
            }
            onDisconnect = {
                runOnUiThread {
                    val builder = AlertDialog.Builder(this@ConnectionActivity)
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