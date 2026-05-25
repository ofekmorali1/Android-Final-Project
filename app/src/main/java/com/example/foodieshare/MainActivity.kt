package com.example.foodieshare

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.NavigationUI
import com.example.foodieshare.data.remote.PlacesClientProvider

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize PlacesClientProvider instead of just the SDK
        PlacesClientProvider.init(applicationContext, getString(R.string.google_maps_key))
        
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom nav with NavController
        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            Log.d("DEBUG", "Clicked on menu item: ${item.title}")

            if (item.itemId == R.id.createReviewFragment) {
                Log.d("DEBUG", "Navigating to Create Review...")
            }

            val navigated = NavigationUI.onNavDestinationSelected(item, navController)

            if (!navigated) {
                Log.e("DEBUG", "Navigation failed for item: ${item.title}")
            }

            return@setOnItemSelectedListener navigated
        }

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
        Log.d("DEBUG", "Activity started")

    }
}