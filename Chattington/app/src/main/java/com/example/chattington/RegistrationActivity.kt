package com.example.chattington

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registration)


        findViewById<Button>(R.id.buttonRegisterAccount).setOnClickListener {
            val email = findViewById<EditText>(R.id.et_EmailInput).text.toString()
            val password = findViewById<EditText>(R.id.et_PasswordInput).text.toString()
            val passwordconfirmation = findViewById<EditText>(R.id.et_ConfirmPasswordInput).text.toString()
            val username = findViewById<EditText>(R.id.et_UsernameInput).text.toString()

            if (password == passwordconfirmation) {
                val db = FirebaseFirestore.getInstance()
                val user: MutableMap<String, Any> = HashMap()
                user["email"] = email
                user["password"] = password
                user["username"] = username

                db.collection("users")
                    .add(user)
                    .addOnSuccessListener {
                        Log.d("dbfirebase", "save: ${user}")
                        val text = "You successfully created an account into the awesome app!"
                        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
                        toast.show()
                    }
                    .addOnFailureListener {
                        Log.d("dbfirebase Failed", "${user}")
                        val text = "You were Unsucessful at creating an account"
                        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
                        toast.show()
                    }
                db.collection("users")
                    .get()
                    .addOnCompleteListener {
                        val result: StringBuffer = StringBuffer()
                        if (it.isSuccessful) {
                            for (document in it.result!!) {
                                Log.d(
                                    "dbfirebase", "retrieve " +
                                            "${document.data.getValue("email")} " +
                                            "${document.data.getValue("password")}" +
                                            "${document.data.getValue("username")}"
                                )
                            }
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
            } else {
                val text = "Passwords do not match!"
                val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }
}