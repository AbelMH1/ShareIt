package uniovi.eii.shareit.view

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import uniovi.eii.shareit.R
import uniovi.eii.shareit.databinding.ActivityMainBinding
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.utils.loadProfileImageIntoView
import uniovi.eii.shareit.viewModel.MainViewModel
import uniovi.eii.shareit.viewModel.ProfileViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)

        destinationChangedListener = NavController.OnDestinationChangedListener { controller, destination, arguments ->
            binding.appBarMain.toolbar.isVisible = arguments?.getBoolean("ShowAppBar", true) ?: true

            Log.d("BACKSTACK", controller.currentBackStack.value.map { stackEntry -> stackEntry.destination.displayName }.toString())
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_account, R.id.nav_explore
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.navView.setNavigationItemSelectedListener(this)
        configureToolBar()

        val profileViewModel: ProfileViewModel by viewModels()
        mainViewModel.loggedUser.observe(this) {
            if (it != null) profileViewModel.registerUserDataListener(it.uid)
            else {
                try {
                    navController.getBackStackEntry(R.id.nav_login)
                    Log.d("NAV", "nav_login already in backstack")
                } catch (e: IllegalArgumentException) {
                    Log.d("NAV", "No backstack entry for nav_login")
                    navController.navigate(R.id.log_out_to_nav_login)
                } finally {
                    profileViewModel.unregisterUserDataListener()
                }
            }
        }

        profileViewModel.currentUser.observe(this) {
            updateDrawMenuUser(it)
        }
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(destinationChangedListener)
        Log.d("USUARIO", Firebase.auth.currentUser?.email.toString())
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(destinationChangedListener)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var successNavigation = true
        when (item.itemId) {
            R.id.log_out_to_nav_login -> {
                mainViewModel.logOut()
                Toast.makeText(this, getString(R.string.toast_successful_logout), Toast.LENGTH_SHORT).show()
            }

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

    private fun updateDrawMenuUser(user: User) {
        val header = binding.navView.getHeaderView(0)
        val imgView: ImageView = header.findViewById(R.id.imgProfile)
        val nameTv: TextView = header.findViewById(R.id.displayName)
        val emailTv: TextView = header.findViewById(R.id.email)
        nameTv.text = user.name
        emailTv.text = user.email
        baseContext.loadProfileImageIntoView(
            user.imagePath,
            user.lastUpdatedImage.time,
            imgView,
        )
    }

    class ErrorCleaningTextWatcher(private val etLayout: TextInputLayout) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            etLayout.error = null
        }
    }
}