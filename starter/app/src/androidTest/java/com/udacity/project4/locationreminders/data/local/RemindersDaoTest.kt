package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveAndGetReminder() = runBlockingTest{
        val reminder = ReminderDTO(
            "Title",
            "Description",
            "Location",
            1.0,
            0.2,
            "a0")
        database.reminderDao().saveReminder(reminder)
        val loaded = database.reminderDao().getReminderById("a0")
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveAndGetAllReminders() = runBlockingTest{
        val reminder = ReminderDTO(
                "Title",
                "Description",
                "Location",
                1.0,
                0.2,
                "a0")
        val reminder2 = ReminderDTO(
                "Title",
                "Description",
                "Location",
                1.0,
                0.2,
                "a1")
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        val list = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(list, notNullValue())
        assertThat(list.size, `is`(2))
        assertThat(list.get(0), `is`(reminder))
        assertThat(list.get(1), `is`(reminder2))
    }

    @Test
    fun saveAndDeleteAllReminders() = runBlockingTest{
        val reminder = ReminderDTO(
                "Title",
                "Description",
                "Location",
                1.0,
                0.2,
                "a0")
        val reminder2 = ReminderDTO(
                "Title",
                "Description",
                "Location",
                1.0,
                0.2,
                "a1 ")
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        var list = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(list, notNullValue())
        assertThat(list.size, `is`(2))

        database.reminderDao().deleteAllReminders()
        list = database.reminderDao().getReminders()
       //    assertThat<List<ReminderDTO>>(list, nullValue())
        assertThat(list.size, `is`(0))
    }

}