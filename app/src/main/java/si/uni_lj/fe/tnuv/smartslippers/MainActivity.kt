package si.uni_lj.fe.tnuv.smartslippers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 9999
class MainActivity : AppCompatActivity() {


    // main function
    @RequiresApi(Build.VERSION_CODES.M)
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

        setContentView(R.layout.activity_main)
        //checkPermissions(this, this)
        initializeBluetoothOrRequestPermission()


        val loginButton = findViewById<Button>(R.id.loginButton)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val signUp = findViewById<TextView>(R.id.signUp)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val notEmail = findViewById<TextView>(R.id.notEmail)
        val notPassword = findViewById<TextView>(R.id.notPassword)

        // set opacity of text to 0 (invisible)
        notEmail.alpha = 0.0f
        notPassword.alpha = 0.0f

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {

            val db = DBHelper(this,null)

            val email1 = email.text.toString()
            val password1 = password.text.toString()

            val cursor = db.count()

            var x : Long = 2

            cursor!!.moveToFirst()

            var enakostEmail : Boolean = false
            var enakostPassword : Boolean = false

            while(cursor.moveToNext()) {

                var (emailLogin, passwordLogin) = db.getName(x)

                // Toast.makeText(this, "$emailLogin", Toast.LENGTH_LONG).show()

                if(email1 == emailLogin){
                    enakostEmail = true
                }

                if(email1 == emailLogin && password1 == passwordLogin){
                    enakostPassword = true
                }
                // Toast.makeText(this, "email:$emailLogin, $email1, p: $passwordLogin, $password1", Toast.LENGTH_SHORT).show()

                x++
            }

            // at last we close our cursor
            cursor.close()

            if(TextUtils.isEmpty(email1)){
                val timer = object: CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        notEmail.text = getString(R.string.no_email)
                        notEmail.alpha = 1.0f
                    }

                    override fun onFinish() {
                        notEmail.alpha = 0.0f
                    }
                }
                timer.start()
            }
            else if(TextUtils.isEmpty(password1)){
                val timer = object: CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        notPassword.text = getString(R.string.no_password)
                        notPassword.alpha = 1.0f
                    }

                    override fun onFinish() {
                        notPassword.alpha = 0.0f
                    }
                }
                timer.start()
            }
            else if (!enakostEmail){
                val timer = object: CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        notEmail.text = getString(R.string.wrong_email)
                        notEmail.alpha = 1.0f
                    }

                    override fun onFinish() {
                        notEmail.alpha = 0.0f
                    }
                }
                timer.start()
            }
            else if (!enakostPassword){
                val timer = object: CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        notPassword.text = getString(R.string.wrong_password)
                        notPassword.alpha = 1.0f
                    }

                    override fun onFinish() {
                        notPassword.alpha = 0.0f
                    }
                }
                timer.start()
            }
            else {
                val intent = Intent(this, ConnectionActivity::class.java)
                startActivity(intent)
            }
        }

        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }



    // Functions to check for permissions
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeBluetoothOrRequestPermission() {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            Log.i("MAIN", "All permissions granted.")
        } else {
            requestPermissions(missingPermissions.toTypedArray(), BLUETOOTH_PERMISSION_REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    // all permissions are granted
                    //initializeBluetooth()
                } else {
                    // some permissions are not granted
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }






}