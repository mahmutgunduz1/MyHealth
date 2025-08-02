package com.semihacetintas.myhealth.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_teal)

        val locale = Locale("en")
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Set up the Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")
        navController = navHostFragment.navController
        
        // Add destination changed listener
        navController.addOnDestinationChangedListener(this)

        // Set background to null to make the BottomNavigationView transparent
        binding.bottomNavigationView.background = null
        
        // Disable the middle menu item (placeholder for FAB)
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

        // Handle FAB click
        binding.fabAdd.setOnClickListener {
            navigateToAddAppointment()
        }

        // Set up Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.shortsFragment -> {
                    // Navigate to shorts fragment if it exists, or create a fallback
                    if (navController.graph.findNode(R.id.shortsFragment) != null) {
                        navController.navigate(R.id.shortsFragment)
                    } else {
                        // Fallback to calendar fragment if shorts doesn't exist
                        navController.navigate(R.id.calendarFragment)
                    }
                    true
                }
                R.id.subscriptionsFragment -> {
                    // Navigate to subscriptions fragment if it exists, or create a fallback
                    if (navController.graph.findNode(R.id.subscriptionsFragment) != null) {
                        navController.navigate(R.id.subscriptionsFragment)
                    } else {
                        // Fallback to appointment detail if subscriptions doesn't exist
                        navController.navigate(R.id.addAppointmentDetail)
                    }
                    true
                }
                R.id.libraryFragment -> {
                    // Navigate to library fragment if it exists, or create a fallback
                    if (navController.graph.findNode(R.id.libraryFragment) != null) {
                        navController.navigate(R.id.libraryFragment)
                    } else {
                        // Fallback to profile fragment if library doesn't exist
                        navController.navigate(R.id.profileFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // Giriş ve kayıt ekranlarında bottom navigation ve FAB'ı gizle
        when (destination.id) {
            R.id.loginFragment, R.id.registerFragment -> {

                binding.bottomAppBar.visibility = View.GONE
                binding.fabAdd.visibility = View.GONE
            }
            else -> {

                binding.bottomAppBar.visibility = View.VISIBLE
                binding.fabAdd.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToAddAppointment() {
        // Navigate based on current destination
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> navController.navigate(R.id.action_homeFragment_to_addAppointmentFragment)
            R.id.calendarFragment -> navController.navigate(R.id.action_calendarFragment_to_addAppointmentFragment)
            R.id.profileFragment -> navController.navigate(R.id.action_profileFragment_to_addAppointmentFragment)
            else -> navController.navigate(R.id.addAppointmentFragment)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Mevcut destinasyonu kontrol et ve UI'ı güncelle
        navController.currentDestination?.let { destination ->
            onDestinationChanged(navController, destination, null)
        }
    }



}