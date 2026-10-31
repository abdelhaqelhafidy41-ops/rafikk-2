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
        FridayPreferences.resetAllFridayData(context)
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
        assertEquals(300, FridayPreferences.getSalawatGoal(context))

        // Increment
        val count1 = FridayPreferences.incrementSalawat(context)
        assertEquals(1, count1)
        assertEquals(1, FridayPreferences.getSalawatCount(context))

        // Increment again
        FridayPreferences.incrementSalawat(context)
        assertEquals(2, FridayPreferences.getSalawatCount(context))

        // Change goal
        FridayPreferences.setSalawatGoal(context, 500)
        assertEquals(500, FridayPreferences.getSalawatGoal(context))

        // Reset
        FridayPreferences.resetSalawat(context)
        assertEquals(0, FridayPreferences.getSalawatCount(context))
        // Goal remains
        assertEquals(500, FridayPreferences.getSalawatGoal(context))
    }

    @Test
    fun testFridaySunanChecklist() {
        val sunnahId = "ghusl"
        assertFalse(FridayPreferences.getCheckedSunan(context).contains(sunnahId))

        FridayPreferences.toggleSunnahChecked(context, sunnahId)
        assertTrue(FridayPreferences.getCheckedSunan(context).contains(sunnahId))

        FridayPreferences.toggleSunnahChecked(context, sunnahId)
        assertFalse(FridayPreferences.getCheckedSunan(context).contains(sunnahId))
    }

    @Test
    fun testFridayAdhkarFavoritesAndCompleted() {
        val dhikrId = "salawat_ibrahimiyyah"
        assertFalse(FridayPreferences.getFavoriteAdhkar(context).contains(dhikrId))
        assertFalse(FridayPreferences.getCompletedAdhkar(context).contains(dhikrId))

        // Toggle Favorite
        FridayPreferences.toggleFavoriteDhikr(context, dhikrId)
        assertTrue(FridayPreferences.getFavoriteAdhkar(context).contains(dhikrId))

        // Toggle Completed
        FridayPreferences.toggleCompletedDhikr(context, dhikrId)
        assertTrue(FridayPreferences.getCompletedAdhkar(context).contains(dhikrId))

        // Untoggle Completed
        FridayPreferences.toggleCompletedDhikr(context, dhikrId)
        assertFalse(FridayPreferences.getCompletedAdhkar(context).contains(dhikrId))
    }

    @Test
    fun testFridayDataAuthenticity() {
        val sunan = FridayData.sunanList
        assertTrue("Friday Sunan must contain at least 7 authentic items", sunan.size >= 7)

        for (item in sunan) {
            assertTrue("Sunan ID must not be blank", item.id.isNotBlank())
            assertTrue("Sunan title must not be blank", item.title.isNotBlank())
            assertTrue("Sunan description must not be blank", item.description.isNotBlank())
            assertTrue("Sunan hadith evidence must not be blank", item.evidence.isNotBlank())
            assertTrue("Sunan hadith source must not be blank", item.source.isNotBlank())
        }

        val adhkar = FridayData.adhkarList
        assertTrue("Friday Adhkar must contain authentic supplications", adhkar.size >= 4)

        for (adhkarItem in adhkar) {
            assertTrue("Dhikr text must not be blank", adhkarItem.text.isNotBlank())
            assertTrue("Dhikr title must not be blank", adhkarItem.title.isNotBlank())
            assertTrue("Dhikr hadith citation must not be blank", adhkarItem.source.isNotBlank())
            val isAuthenticSource = adhkarItem.source.contains("بخاري") ||
                    adhkarItem.source.contains("مسلم") ||
                    adhkarItem.source.contains("داود") ||
                    adhkarItem.source.contains("ترمذي") ||
                    adhkarItem.source.contains("نسائي") ||
                    adhkarItem.source.contains("حاكم") ||
                    adhkarItem.source.contains("ماجه") ||
                    adhkarItem.source.contains("طبراني") ||
                    adhkarItem.source.contains("أحمد") ||
                    adhkarItem.source.contains("ألباني") ||
                    adhkarItem.source.contains("الباني")
            assertTrue(
                "Dhikr citation must reference authentic sources for ${adhkarItem.title} (${adhkarItem.source})",
                isAuthenticSource
            )
        }

        assertTrue("Hour of Response description must be present and authentic", FridayData.HOUR_OF_RESPONSE_EXPLANATION.isNotBlank())
        assertTrue(
            "Hour of Response explanation must reference the Prophetic Hadith and Sahih Muslim",
            FridayData.HOUR_OF_RESPONSE_EXPLANATION.contains("ساعة") && FridayData.HOUR_OF_RESPONSE_EXPLANATION.contains("الجمعة")
        )
    }

    @Test
    fun testNextFridayCalculation() {
        val nextFridayTime = FridayReminderManager.getNextFridayMillis("10:00")
        val now = System.currentTimeMillis()

        assertTrue("Next Friday must be in the future", nextFridayTime > now)

        val cal = Calendar.getInstance().apply { timeInMillis = nextFridayTime }
        assertEquals("Calculated day of week must be Friday", Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals("Calculated hour must be 10", 10, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Calculated minute must be 0", 0, cal.get(Calendar.MINUTE))
        assertEquals("Calculated second must be 0", 0, cal.get(Calendar.SECOND))
    }
}
