package com.udacity.project4.locationreminders.reminderslist

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.LoginViewModel
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel<LoginViewModel>()
    private lateinit var geofencingClient: GeofencingClient

    private lateinit var binding: FragmentRemindersBinding

    private  val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = SaveReminderFragment.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_ONE_SHOT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FLUX", "onCreateView")
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FLUX", "onViewCreated")
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }

        _viewModel.dataLoaded.observe(viewLifecycleOwner, Observer {
            addGeofences()
        })
        //addGeofences()
        //observeAuthenticationState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FLUX", "onCreate")
        geofencingClient = activity?.let { LocationServices.getGeofencingClient(it) }!!
    }

    override fun onResume() {
        super.onResume()
        Log.d("FLUX", "onResume")
        //load the reminders list on the ui
        _viewModel.loadReminders()


    }

    override fun onStart() {
        super.onStart()
        Log.d("FLUX", "onStart")
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener{
                    if (it.isSuccessful) {
                        Log.d("FLUX", "success unauth")
                        activity?.finish()
                        val intent: Intent = Intent(activity, AuthenticationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)

                       // findNavController().popBackStack()
                    } else {
                        Log.d("FLUX", "not success unauth")
                        Toast.makeText(activity, "Logging out error!", Toast.LENGTH_SHORT).show()
                    }

                }
                //loginViewModel.initNavigating()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    fun addGeofences(){
         geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                Log.d("FLUX","Geofences Removed")
            }
         }

        for (reminder in _viewModel.remindersList.value!!){
            Log.d("FLUX","title " +reminder.title)
            Log.d("FLUX","description " + reminder.description)
            Log.d("FLUX","lat " + reminder.latitude)
            Log.d("FLUX","lon " + reminder.longitude)
            if (reminder.latitude != null && reminder.longitude != null) {
                val geofence = Geofence.Builder()
                        .setRequestId(reminder.id)
                        .setCircularRegion(reminder.latitude!!,
                                reminder.longitude!!,
                                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                        )
                        .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()

                val geofencingRequest = GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build()



                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        //  Toast.makeText(activity, R.string.geofence_entered,
                        //          Toast.LENGTH_SHORT)
                        //          .show()
                        Log.d("FLUX", "Add Geofence " + geofence.requestId)
                        Log.e("Add Geofence", geofence.requestId)
                        //viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        //Toast.makeText(activity, R.string.geofences_not_added,
                        //        Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w("FLUX", it.localizedMessage)
                        }
                    }
                }
            }
            //   }
            // }
        }

    }


/*
    private fun observeAuthenticationState() {

        loginViewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.d("FLUX", "authenticated")
                }
                else -> {
                    Log.d("FLUX", "UNauthenticated")


                   // AuthUI.getInstance().signOut(requireContext())

                    //

                    if (loginViewModel.shouldNavigate.value == true) {
                        activity?.finish()
                        val intent: Intent = Intent(activity, AuthenticationActivity::class.java)
                        startActivity(intent)

                        findNavController().popBackStack()

                        loginViewModel.doneNavigating()
                    }






                }
            }
        })
    }


 */


}
