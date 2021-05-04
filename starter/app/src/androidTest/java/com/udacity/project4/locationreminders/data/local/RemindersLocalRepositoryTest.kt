package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()

        localDataSource =
                RemindersLocalRepository(
                        database.reminderDao(),
                        Dispatchers.Main
                )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new task saved in the database.
        val reminder = ReminderDTO(
                "title",
                "description",
                "location",
                1.0,
                2.0,
                "a0")
        localDataSource.saveReminder(reminder)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same task is returned.
        Assert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.title, `is`("title"))
        Assert.assertThat(result.data.description, `is`("description"))
        Assert.assertThat(result.data.location, `is`("location"))
        Assert.assertThat(result.data.latitude, `is`(1.0))
        Assert.assertThat(result.data.longitude, `is`(2.0))
    }

    @Test
    fun saveReminders_retrievesAllReminders() = runBlocking {
        // GIVEN - A new task saved in the database.
        val reminder = ReminderDTO(
                "title",
                "description",
                "location",
                1.0,
                2.0,
                "a1")

        val reminder2 = ReminderDTO(
                "title2",
                "description2",
                "location2",
                3.0,
                4.0,
                "a2")
        localDataSource.saveReminder(reminder)
        localDataSource.saveReminder(reminder2)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminders()

        // THEN - Same task is returned.
        Assert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.size, `is`(2))
        Assert.assertThat(result.data.get(0).title, `is`("title"))
        Assert.assertThat(result.data.get(1).title, `is`("title2"))
    }

    @Test
    fun saveReminders_deleteReminders() = runBlocking {
        // GIVEN - A new task saved in the database.
        val reminder = ReminderDTO(
                "title",
                "description",
                "location",
                1.0,
                2.0,
                "a1")

        val reminder2 = ReminderDTO(
                "title2",
                "description2",
                "location2",
                3.0,
                4.0,
                "a2")
        localDataSource.saveReminder(reminder)
        localDataSource.saveReminder(reminder2)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminders()

        // THEN - Same task is returned.
        Assert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.size, `is`(2))


        val deleted = localDataSource.deleteAllReminders()

        val result2 = localDataSource.getReminders()

        // THEN - Same task is returned.
        Assert.assertThat(result2 is Result.Success, `is`(true))
        result2 as Result.Success
        Assert.assertThat(result2.data.size, `is`(0))
    }

}