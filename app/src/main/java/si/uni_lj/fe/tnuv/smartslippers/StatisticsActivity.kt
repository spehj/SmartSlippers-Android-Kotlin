package si.uni_lj.fe.tnuv.smartslippers

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import si.uni_lj.fe.tnuv.smartslippers.databinding.ActivityMainBinding

class StatisticsActivity : AppCompatActivity() {
    private lateinit var device: BluetoothDevice

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")
        setContentView(R.layout.activity_statistics)

        //binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(binding.root)





        val settingsBtn = findViewById<Button>(R.id.settingsBtn)

        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        val homeBtn = findViewById<Button>(R.id.homeBtn)

        homeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).also {
                it.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                startActivity(it) }
        }

    }
}