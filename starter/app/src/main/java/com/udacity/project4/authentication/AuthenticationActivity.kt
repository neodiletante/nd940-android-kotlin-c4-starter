package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthenticationBinding
    private val viewModel by viewModels<LoginViewModel>()

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_authentication)
        binding.btnLogin.setOnClickListener{ launchSignInFlow() }

        observeAuthenticationState()

    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.


        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
               // navigateToMainActivity()
               //findNavController(binding.root).navigate(R.id.to_main_activity)
               // Activity.findNavController(R.id.nav_host_fragment).navigate
                Log.i(
                        TAG,
                        "Successfully signed in user " +
                                "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )

                val intent = Intent(this,RemindersActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                startActivity(intent)
                this.finish()

            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    fun navigateToMainActivity() {
        //val directions = AuthenticationActivityDirections.toMainActivity()
       // findNavController(binding.root).navigate(R.id.mainActivity)
        val intent = Intent(this,RemindersActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.d("FLUX","auth")
                    //binding.welcomeText.text = getFactWithPersonalization(factToDisplay)
                    val intent = Intent(this,RemindersActivity::class.java)
                    startActivity(intent)
                    //binding.btnLogin.text = getString(R.string.logout_button_text)
                    binding.btnLogin.setOnClickListener {
                        AuthUI.getInstance().signOut(this)
                    }
                }
                else -> {
                    Log.d("FLUX","unauth")
                    //binding.welcomeText.text = factToDisplay

                    //binding.authButton.text = getString(R.string.login_button_text)
                    binding.btnLogin.setOnClickListener {
                        launchSignInFlow()
                    }
                }
            }
        })
    }

}
