package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest{
    @ExperimentalCoroutinesApi

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderRepository: ReminderDataSource

    @Before
    fun setupViewModel() {
        //tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())
        reminderRepository = FakeDataSource()
       /*
        val reminder1 = ReminderDTO("Title1", "Description1","Location1",0.0,0.0,"a0")
        val reminder2 = ReminderDTO("Title2", "Description2","Location2",1.0,1.0,"a1")
        val reminder3 = ReminderDTO("Title3", "Description3","Location3",2.0,2.0,"a2")
        runBlocking {
            remiderRepository.saveReminder(reminder1)
            remiderRepository.saveReminder(reminder2)
            remiderRepository.saveReminder(reminder3)
        }*/

        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            reminderRepository
        )

    }

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun clearReminderTest(){
        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Selected Location"
        saveReminderViewModel.selectedPOI.value = PointOfInterest(LatLng(1.0,0.1),"POI","a0")
        saveReminderViewModel.latitude.value = 1.0
        saveReminderViewModel.longitude.value = 0.1

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.value,`is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.value,`is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value,`is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.value,`is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value,`is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.value,`is`(nullValue()))

    }

    @Test
    fun saveReminderTest(){
        val reminder: ReminderDataItem = ReminderDataItem("Title4", "Description4","Location4",0.0,0.0,"a3")
                saveReminderViewModel.saveReminder(reminder)

        var reminder3: ReminderDataItem? = null
        runBlocking {
            val result: Result<ReminderDTO> = saveReminderViewModel.dataSource.getReminder("a3")

            when (result) {
                is Result.Success<*> -> {
                    val reminder2: ReminderDTO = result.data as ReminderDTO
                    reminder3 = ReminderDataItem(
                            reminder2.title,
                            reminder2.description,
                            reminder2.location,
                            reminder2.latitude,
                            reminder2.longitude,
                            reminder2.id
                    )
                }
            }
            assertThat(reminder, `is`(reminder3))

        }


    }



    @Test
    fun validateEnteredDataTitleTest(){

        val reminder: ReminderDataItem = ReminderDataItem("", "Description4","Location",0.0,0.0,"a3")

        saveReminderViewModel.validateEnteredData(reminder)

        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))

    }

    @Test
    fun validateEnteredDataLocationTest(){

        val reminder: ReminderDataItem = ReminderDataItem("Title", "Description4","",0.0,0.0,"a3")

        saveReminderViewModel.validateEnteredData(reminder)

        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_select_location))

    }

    @Test
    fun validateAndSaveReminderTest(){
        val reminder: ReminderDataItem = ReminderDataItem("Title4", "Description4","Location4",0.0,0.0,"a3")
        saveReminderViewModel.validateAndSaveReminder(reminder)

        var reminder3: ReminderDataItem? = null
        runBlocking {
            val result: Result<ReminderDTO> = saveReminderViewModel.dataSource.getReminder("a3")

            when (result) {
                is Result.Success<*> -> {
                    val reminder2: ReminderDTO = result.data as ReminderDTO
                    reminder3 = ReminderDataItem(
                            reminder2.title,
                            reminder2.description,
                            reminder2.location,
                            reminder2.latitude,
                            reminder2.longitude,
                            reminder2.id
                    )
                }
            }
            assertThat(reminder, `is`(reminder3))

        }
    }

    @Test
    fun validateAndSaveReminderNullValueTest(){
        val reminder: ReminderDataItem = ReminderDataItem(null, "Description4","Location4",0.0,0.0,"a3")
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))

    }





}