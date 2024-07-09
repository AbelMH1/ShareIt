package uniovi.eii.shareit.view

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.ActivityMainBinding
import uniovi.eii.shareit.view.home.HomeFragmentDirections

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)

        destinationChangedListener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
            binding.appBarMain.toolbar.isVisible = arguments?.getBoolean("ShowAppBar", true) ?: true // TODO: Decide Op2:
            // TODO: Decide Op1:
//            if (arguments?.getBoolean("ShowAppBar", true) == true)
//                supportActionBar?.setDisplayHomeAsUpEnabled(true)
//            else supportActionBar?.setDisplayHomeAsUpEnabled(false)
            Log.d("BACKSTACK", controller.currentBackStack.value.map { stackEntry -> stackEntry.destination.displayName }.toString())
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.navView.setNavigationItemSelectedListener(this)
        configureToolBar()
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(destinationChangedListener)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(destinationChangedListener)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var successNavigation = true
        when (item.itemId) {
            R.id.log_out -> navController.navigate(HomeFragmentDirections.actionNavHomeToNavLogin())
            else -> successNavigation = NavigationUI.onNavDestinationSelected(item, navController)
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return successNavigation
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun configureToolBar() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.main, menu)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    binding.appBarMain.toolbar.menu.setGroupDividerEnabled(true)
                } else MenuCompat.setGroupDividerEnabled(menu, true)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_account -> {
                        Toast.makeText(this@MainActivity, "Account...", Toast.LENGTH_SHORT).show()
                        true
                    }

                    else -> false
                }
            }
        }, this)
    }

    class ErrorCleaningTextWatcher (private val etLayout: TextInputLayout) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            etLayout.error = null
        }
    }
}