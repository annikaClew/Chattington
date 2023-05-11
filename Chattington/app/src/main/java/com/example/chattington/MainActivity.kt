package com.example.chattington

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
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

        // create the fragments
        val homeFragment = HomeFragment()
        val chatFragment = ChatFragment()
        val userprofileFragment = UserProfileFragment()

        // set up bottom navigation view
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.ic_home -> {
                    changeFragment(homeFragment)
                    true
                }
                R.id.ic_chat -> {
                    changeFragment(chatFragment)
                    true
                }
                R.id.ic_person -> {
                    // pass the username to the user profile fragment
                    val bundle = Bundle()
                    bundle.putString("username", username)

                    println("username: " + username)

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