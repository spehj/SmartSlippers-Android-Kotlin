package si.uni_lj.fe.tnuv.smartslippers

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // remove title bar in app activity
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.sign_up_activity)

        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.lastName)
        val emailSignUp = findViewById<EditText>(R.id.emailSignUp)
        val passwordSignUp = findViewById<EditText>(R.id.passwordSignUp)
        val rePasswordSignUp = findViewById<EditText>(R.id.rePasswordSignUp)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val firstNameError = findViewById<TextView>(R.id.firstNameError)
        val lastNameError = findViewById<TextView>(R.id.lastNameError)
        val emailError = findViewById<TextView>(R.id.emailError)
        val passwordError = findViewById<TextView>(R.id.passwordError)

        firstNameError.alpha = 0.0f
        lastNameError.alpha = 0.0f
        emailError.alpha = 0.0f
        passwordError.alpha = 0.0f

        var validInfo1 : Boolean = false
        var validInfo2 : Boolean = false
        var validInfo3 : Boolean = false

        signUpButton.setOnClickListener {

            val db = DBHelper(this,null)

            val fname = firstName.text.toString()
            val lname = lastName.text.toString()
            val email = emailSignUp.text.toString()
            val textPasswordSignUp = passwordSignUp.text.toString()
            val textRePasswordSignUp = rePasswordSignUp.text.toString()

            validInfo1 = if (TextUtils.isEmpty(fname)) {
                val timer = object: CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        firstNameError.text = getString(R.string.no_first_name)
                        firstNameError.alpha = 1.0f
                    }

                    override fun onFinish() {
                        firstNameError.alpha = 0.0f
                    }
                }
                timer.start()
                false
            }else{
                true
            }
            validInfo2 = if (TextUtils.isEmpty(lname)) {
            val timer = object: CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    lastNameError.text = getString(R.string.no_last_name)
                    lastNameError.alpha = 1.0f
                }

                override fun onFinish() {
                    lastNameError.alpha = 0.0f
                }
            }
            timer.start()
            false
        }else{
            true
        }
            validInfo3 = if (textPasswordSignUp != textRePasswordSignUp) {
                Toast.makeText(this, "re entered password is not the same", Toast.LENGTH_SHORT).show()
                false
            }else{
                true
            }
            if (validInfo1 && validInfo2 && validInfo3){
                Toast.makeText(this, "account has been created", Toast.LENGTH_SHORT).show()
                db.addName(fname, lname, email, textRePasswordSignUp)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}