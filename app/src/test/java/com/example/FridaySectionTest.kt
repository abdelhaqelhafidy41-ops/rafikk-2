package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.friday.FridayData
import com.example.friday.FridayPreferences
import com.example.friday.FridayReminderManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FridaySectionTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        // Reset preferences before test
        FridayPreferences.resetWeeklyProgress(context)
        FridayPreferences.resetSalawat(context)
    }

    @Test
    fun testSurahKahfWeeklyReadToggle() {
        // Initial state should be false
        assertFalse(FridayPreferences.isKahfReadThisWeek(context))

        // Mark as read
        FridayPreferences.setKahfReadThisWeek(context, true)
        assertTrue(FridayPreferences.isKahfReadThisWeek(context))

        // Unmark
        FridayPreferences.setKahfReadThisWeek(context, false)
        assertFalse(FridayPreferences.isKahfReadThisWeek(context))
    }

    @Test
    fun testSalawatCounterAndGoal() {
        assertEquals(0, FridayPreferences.getSalawatCount(context))
        assertEquals(100, FridayPreferences.getSalawatGoal(context))

        // Increment
        val count1 = FridayPreferences.incrementSalawat(context)
        assertEquals(1, count1)
        assertEquals(1, FridayPreferences.getSalawatCount(context))

        // Increment again
        FridayPreferences.incrementSalawat(context)
        assertEquals(2, FridayPreferences.getSalawatCount(context))

        // Change goal
        FridayPreferences.setSalawatGoal(context, 300)
        assertEquals(300, FridayPreferences.getSalawatGoal(context))

        // Reset
        FridayPreferences.resetSalawat(context)
        assertEquals(0, FridayPreferences.getSalawatCount(context))
        // Goal remains
        assertEquals(300, FridayPreferences.getSalawatGoal(context))
    }

    @Test
    fun testFridaySunanChecklist() {
        val sunnahId = "ghusl"
        assertFalse(FridayPreferences.isSunnahDone(context, sunnahId))

        FridayPreferences.toggleSunnah(context, sunnahId)
        assertTrue(FridayPreferences.isSunnahDone(context, sunnahId))

        FridayPreferences.toggleSunnah(context, sunnahId)
        assertFalse(FridayPreferences.isSunnahDone(context, sunnahId))
    }

    @Test
    fun testFridayAdhkarFavoritesAndCompleted() {
        val dhikrId = "friday_salawat_1"
        assertFalse(FridayPreferences.isDhikrFavorite(context, dhikrId))
        assertFalse(FridayPreferences.isDhikrCompleted(context, dhikrId))

        // Toggle Favorite
        FridayPreferences.toggleDhikrFavorite(context, dhikrId)
        assertTrue(FridayPreferences.isDhikrFavorite(context, dhikrId))

        // Toggle Completed
        FridayPreferences.toggleDhikrCompleted(context, dhikrId)
        assertTrue(FridayPreferences.isDhikrCompleted(context, dhikrId))

        // Untoggle Completed
        FridayPreferences.toggleDhikrCompleted(context, dhikrId)
        assertFalse(FridayPreferences.isDhikrCompleted(context, dhikrId))
    }

    @Test
    fun testFridayDataAuthenticity() {
        val sunan = FridayData.fridaySunan
        assertTrue("Friday Sunan must contain at least 7 authentic items", sunan.size >= 7)

        for (item in sunan) {
            assertTrue("Sunan ID must not be blank", item.id.isNotBlank())
            assertTrue("Sunan title must not be blank", item.title.isNotBlank())
            assertTrue("Sunan description must not be blank", item.description.isNotBlank())
            assertTrue("Sunan hadith evidence must not be blank", item.hadithSource.isNotBlank())
        }

        val adhkar = FridayData.fridayAdhkar
        assertTrue("Friday Adhkar must contain authentic supplications", adhkar.size >= 4)

        for (adhkarItem in adhkar) {
            assertTrue("Dhikr text must not be blank", adhkarItem.text.isNotBlank())
            assertTrue("Dhikr title must not be blank", adhkarItem.title.isNotBlank())
            assertTrue("Dhikr hadith citation must not be blank", adhkarItem.hadithSource.isNotBlank())
            assertTrue(
                "Dhikr citation must reference authentic sources",
                adhkarItem.hadithSource.contains("البخاري") ||
                        adhkarItem.hadithSource.contains("مسلم") ||
                        adhkarItem.hadithSource.contains("أبو داود") ||
                        adhkarItem.hadithSource.contains("الترمذي") ||
                        adhkarItem.hadithSource.contains("النسائي") ||
                        adhkarItem.hadithSource.contains("الحاكم") ||
                        adhkarItem.hadithSource.contains("أحمد")
            )
        }

        assertTrue("Hour of Response description must be present and authentic", FridayData.HOUR_OF_RESPONSE_SCHOLARLY.isNotBlank())
        assertTrue(
            "Hour of Response must reference the Prophetic Hadith",
            FridayData.HOUR_OF_RESPONSE_HADITH.contains("ساعة") && FridayData.HOUR_OF_RESPONSE_HADITH.contains("الجمعة")
        )
    }

    @Test
    fun testNextFridayCalculation() {
        val nextFridayTime = FridayReminderManager.getNextFridayMillis(10, 0)
        val now = System.currentTimeMillis()

        assertTrue("Next Friday must be in the future", nextFridayTime > now)

        val cal = Calendar.getInstance().apply { timeInMillis = nextFridayTime }
        assertEquals("Calculated day of week must be Friday", Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals("Calculated hour must be 10", 10, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Calculated minute must be 0", 0, cal.get(Calendar.MINUTE))
        assertEquals("Calculated second must be 0", 0, cal.get(Calendar.SECOND))
    }
}
