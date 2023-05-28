package com.example.chattington.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.chattington.R
import com.example.chattington.fragments.ChatFragment
import com.example.chattington.fragments.HomeFragment
import com.example.chattington.fragments.UserProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get the username from the login/register activity
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        // create the fragments
        val homeFragment = HomeFragment()
        val chatFragment = ChatFragment()
        val userprofileFragment = UserProfileFragment()

        // initially set the home fragment
        changeFragment(homeFragment)

        // set up bottom navigation view
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.ic_home -> {
                    changeFragment(homeFragment)
                    true
                }
                R.id.ic_chat -> {
                    // if the current fragment is the chat fragment, change the passed chat id to -1L
                    // this will cause the chat fragment to create a new chat
                    val bundle = Bundle()
                    bundle.putLong("conversation_id", -1L)
                    chatFragment.arguments = bundle
                    changeFragment(chatFragment)
                    true
                }
                R.id.ic_person -> {
                    // pass the username to the user profile fragment
                    val bundle = Bundle()
                    bundle.putString("username", username)
                    bundle.putString("email", email)
                    bundle.putString("password", password)

                    userprofileFragment.arguments = bundle
                    changeFragment(userprofileFragment)
                    true
                }
                else -> false
            }
        }

    }
    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fContainer, fragment)
            commit()
        }
    }
}