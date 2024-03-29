package com.example.chattington.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chattington.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if the app is running for the first time or not
        val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        // If the app is running for the first time, start the registration activity
        if (isFirstRun) {
            startActivity(Intent(this, RegistrationActivity::class.java))
            Toast.makeText(this, "First Run", Toast.LENGTH_LONG)
                .show()
        }

        // Set isFirstRun to false if the app is not running for the first time
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
            .putBoolean("isFirstRun", false).commit()

        val loginButton = findViewById<Button>(R.id.btn_Login)
        val registerLink = findViewById<TextView>(R.id.tv_NewUser)
        val intent = Intent(this, MainActivity::class.java)
        val intent2 = Intent(this, RegistrationActivity::class.java)

        loginButton.setOnClickListener {
            startActivity(intent)
        }

        registerLink.setOnClickListener {
            startActivity(intent2)
        }

        findViewById<Button>(R.id.btn_Login).setOnClickListener {

            val email = findViewById<EditText>(R.id.et_EmailInput).text.toString()
            val password = findViewById<EditText>(R.id.et_PasswordInput).text.toString()

            auth = FirebaseAuth.getInstance()
            val db = Firebase.firestore

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(baseContext, "Email and password cannot be empty", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(baseContext, "Invalid email format", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //querying db for users with matching email
            val docRef = db.collection("users").whereEqualTo("email", email)
            docRef.get()
                .addOnSuccessListener { documents -> //if the document exists
                    for (document in documents) { //loop through all of the documents
                        val password2 =
                            document.getString("password") //get password for that document
                        if (password != null && password.equals(password2)) //if password matches the one user entered
                        {
                            Log.d(TAG, "signInWithEmail:success") //log them in

                            val username = document.getString("username")
                            // Move the startActivity call inside the success block
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("email", email)
                            intent.putExtra("password", password)

                            startActivity(intent)
                            // Finish the LoginActivity to prevent going back to it using the back button
                            finish()
                        }
                        else //wrong password
                        {
                            Toast.makeText(baseContext, "Incorrect password", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                    Toast.makeText(baseContext, "Account not found", Toast.LENGTH_LONG).show()
                }
        }
    }
}