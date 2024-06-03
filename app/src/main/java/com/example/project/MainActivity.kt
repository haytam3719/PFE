package com.example.project

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView // For non-logged-in users
    private lateinit var bottomNavigationViewLoggedIn: BottomNavigationView // For logged-in views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = findViewById(R.id.bottom_navigation) // Non-logged-in view
        bottomNavigationViewLoggedIn = findViewById(R.id.bottom_navigationn) // Logged-in view

        setupNavigation()

        tintMenuIcons(bottomNavigationView, R.color.usualColor)
        tintMenuIcons(bottomNavigationViewLoggedIn, R.color.usualColor)
    }

    private fun setupNavigation() {
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        NavigationUI.setupWithNavController(bottomNavigationViewLoggedIn, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.main_actpalceholderFragment, R.id.agences, R.id.menu_deconnecte -> {
                    // Show only the non-logged-in menu
                    bottomNavigationView.visibility = View.VISIBLE
                    bottomNavigationViewLoggedIn.visibility = View.GONE
                }
                R.id.fragment_dashboard, R.id.fragment_virement, R.id.fragment_payment, R.id.menu_connecte, R.id.agences_connecte -> {
                    // Show only the logged-in menu
                    bottomNavigationView.visibility = View.GONE
                    bottomNavigationViewLoggedIn.visibility = View.VISIBLE
                }
                else -> {
                    // Hide both menus on all other fragments
                    bottomNavigationView.visibility = View.GONE
                    bottomNavigationViewLoggedIn.visibility = View.GONE
                }
            }
        }
    }

    private fun tintMenuIcons(bottomNavigationView: BottomNavigationView, colorRes: Int) {
        val color = ContextCompat.getColor(applicationContext, colorRes)
        bottomNavigationView.menu.forEach { menuItem ->
            menuItem.icon?.mutate()?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }


}
