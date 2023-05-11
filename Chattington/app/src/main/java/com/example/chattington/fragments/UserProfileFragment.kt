package com.example.chattington.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.chattington.LoginActivity
import com.example.chattington.R
import com.example.chattington.SettingsActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // get the passed username from the bundle
        val username = arguments?.getString("username")
        val email = arguments?.getString("email")
        val password = arguments?.getString("password")

        // set the user profile title to the user's name from the firebase database
        view.findViewById<TextView>(R.id.tv_UsernameDisplay).setText("Username: " + username)

        // Floating Action Button for registering new users
        val settingsButton: View = view.findViewById(R.id.btn_Settings)
        settingsButton.setOnClickListener { view ->
            val intent = Intent(activity, SettingsActivity::class.java)
            // sent the username to the settings activity
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }

        // make a listener for the logout button
        val logoutButton: View = view.findViewById(R.id.btn_LogOut)

        // when the logout button is clicked, go back to the login screen
        logoutButton.setOnClickListener { view ->
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return view
    }
}