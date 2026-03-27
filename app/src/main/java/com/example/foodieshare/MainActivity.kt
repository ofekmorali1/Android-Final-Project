package com.example.foodieshare

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom nav with NavController
        bottomNav.setupWithNavController(navController)

        val authDestinations = setOf(
            R.id.loginFragment,
            R.id.registerFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id in authDestinations) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
