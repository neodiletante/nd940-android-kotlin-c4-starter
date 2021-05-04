package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var reminderRepository: ReminderDataSource



    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest{
        //tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())
        reminderRepository = FakeDataSource()
        val reminder1 = ReminderDTO("Title1", "Description1","Location1",0.0,0.0,"a0")
        val reminder2 = ReminderDTO("Title2", "Description2","Location1",0.0,0.0,"a1")
        val reminder3 = ReminderDTO("Title3", "Description3","Location1",0.0,0.0,"a2")
            reminderRepository.saveReminder(reminder1)
            reminderRepository.saveReminder(reminder2)
            reminderRepository.saveReminder(reminder3)

            remindersListViewModel = RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                reminderRepository
            )

    }

    @After
    fun after() {
        stopKoin()
    }


    @Test
    fun loadRemindersTest(){
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.value?.size,`is`(3))

    }


    @Test
    fun returnErrorTest(){
        (reminderRepository as FakeDataSource).setReturnError(true)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.value,`is`("Test exeception"))

    }

}