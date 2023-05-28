package com.example.chattington.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.chattington.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // get all the passed data from the bundle
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        findViewById<Button>(R.id.btn_settingsBack).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            // pass the username to the user profile fragment
            val bundle = Bundle()
            bundle.putString("username", username)
            bundle.putString("email", email)
            bundle.putString("password", password)

            startActivity(intent)
        }

        // set the text fields to the passed data
        findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_currentUsername).setText(username)
        findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_EditEmail).setText(email)
        findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_CurrentPass).setText(password)

        // make a listener for each edit button
        findViewById<Button>(R.id.btn_EditUsername).setOnClickListener(
            // when the edit button is clicked, allow the username to be edited
            // and change the button text to "Save"
            {
                // if the button text is "Edit", then allow the username to be edited
                if ( findViewById<Button>(R.id.btn_EditUsername).text == "Edit" ) {
                    findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_currentUsername).isEnabled = true
                    findViewById<Button>(R.id.btn_EditUsername).setText("Done")
                }
                else {
                    // if the button text is "Save", then save the new username
                    findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_currentUsername).isEnabled = false
                    findViewById<Button>(R.id.btn_EditUsername).setText("Edit")
                }
            }
        )

        // make a listener for each edit button
        findViewById<Button>(R.id.btn_EditEmail).setOnClickListener(
            // when the edit button is clicked, allow the email to be edited
            // and change the button text to "Save"
            {
                // if the button text is "Edit", then allow the email to be edited
                if ( findViewById<Button>(R.id.btn_EditEmail).text == "Edit" ) {
                    findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_EditEmail).isEnabled = true
                    findViewById<Button>(R.id.btn_EditEmail).setText("Done")
                }
                else {
                    // if the button text is "Save", then save the new email
                    findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_EditEmail).isEnabled = false
                    findViewById<Button>(R.id.btn_EditEmail).setText("Edit")
                }
            }
        )

        // make a listener for each edit button
        findViewById<Button>(R.id.btn_EditPassword).setOnClickListener(
            // when the edit button is clicked, allow the password to be edited
            // and change the button text to "Save"
            {
                // if the button text is "Edit", then allow the password to be edited
                if ( findViewById<Button>(R.id.btn_EditPassword).text == "Edit" ) {
                    findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_CurrentPass).isEnabled = true
                    findViewById<Button>(R.id.btn_EditPassword).setText("Done")
                }
                else {
                    // if the button text is "Save", then save the new password
                    findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_CurrentPass).isEnabled = false
                    findViewById<Button>(R.id.btn_EditPassword).setText("Edit")
                }
            }
        )
    }
}