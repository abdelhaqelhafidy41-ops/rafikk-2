package com.example.friday

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Clean, isolated local preferences for Friday section features.
 * Operates purely on SharedPreferences to avoid any alterations to existing database schemas.
 */
object FridayPreferences {
    private const val PREF_NAME = "friday_preferences"

    // Settings Keys
    private const val KEY_SECTION_ENABLED = "friday_section_enabled"
    private const val KEY_WIDGET_ENABLED = "friday_widget_enabled"
    private const val KEY_KAHF_REMINDER = "kahf_reminder_enabled"
    private const val KEY_KAHF_TIME = "kahf_reminder_time" // "HH:mm"
    private const val KEY_SALAWAT_REMINDER = "salawat_reminder_enabled"
    private const val KEY_SALAWAT_TIME = "salawat_reminder_time"
    private const val KEY_DUA_REMINDER = "dua_reminder_enabled"
    private const val KEY_DUA_TIME = "dua_reminder_time"
    private const val KEY_JUMUAH_PREP_REMINDER = "jumuah_prep_reminder_enabled"
    private const val KEY_JUMUAH_PREP_TIME = "jumuah_prep_reminder_time"

    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_SHOW_HIJRI = "show_hijri"
    private const val KEY_SHOW_SALAWAT_WIDGET = "show_salawat_widget"

    // Weekly Progress Keys
    private const val KEY_KAHF_READ_WEEK = "kahf_read_week_key"
    private const val KEY_SALAWAT_COUNT = "salawat_count"
    private const val KEY_SALAWAT_GOAL = "salawat_goal"
    private const val KEY_CHECKED_SUNAN = "checked_sunan"
    private const val KEY_FAVORITE_ADHIKR = "favorite_adhkar"
    private const val KEY_COMPLETED_ADHIKR = "completed_adhkar"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Returns an identifier for the current Islamic week (from Thursday sunset to Friday sunset, or calendar week).
     */
    fun getCurrentWeekKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return "${year}-W${week}"
    }

    /**
     * Returns true if today is Friday (Calendar.FRIDAY = 6).
     */
    fun isTodayFriday(): Boolean {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    /**
     * Returns remaining days until next Friday (0 if today is Friday).
     */
    fun getDaysUntilNextFriday(): Int {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_WEEK)
        val target = Calendar.FRIDAY
        var diff = target - today
        if (diff <= 0) diff += 7
        return diff
    }

    // --- Section & Widget Enablement ---
    fun isFridaySectionEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SECTION_ENABLED, true)
    fun setFridaySectionEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SECTION_ENABLED, enabled).apply()

    fun isFridayWidgetEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_WIDGET_ENABLED, true)
    fun setFridayWidgetEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_WIDGET_ENABLED, enabled).apply()

    // --- Reminder Toggles & Times ---
    fun isKahfReminderEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_KAHF_REMINDER, true)
    fun setKahfReminderEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_KAHF_REMINDER, enabled).apply()

    fun getKahfReminderTime(context: Context): String = getPrefs(context).getString(KEY_KAHF_TIME, "09:00") ?: "09:00"
    fun setKahfReminderTime(context: Context, time: String) = getPrefs(context).edit().putString(KEY_KAHF_TIME, time).apply()

    fun isSalawatReminderEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SALAWAT_REMINDER, true)
    fun setSalawatReminderEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SALAWAT_REMINDER, enabled).apply()

    fun getSalawatReminderTime(context: Context): String = getPrefs(context).getString(KEY_SALAWAT_TIME, "11:00") ?: "11:00"
    fun setSalawatReminderTime(context: Context, time: String) = getPrefs(context).edit().putString(KEY_SALAWAT_TIME, time).apply()

    fun isDuaReminderEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_DUA_REMINDER, true)
    fun setDuaReminderEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_DUA_REMINDER, enabled).apply()

    fun getDuaReminderTime(context: Context): String = getPrefs(context).getString(KEY_DUA_TIME, "16:30") ?: "16:30"
    fun setDuaReminderTime(context: Context, time: String) = getPrefs(context).edit().putString(KEY_DUA_TIME, time).apply()

    fun isJumuahPrepReminderEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_JUMUAH_PREP_REMINDER, true)
    fun setJumuahPrepReminderEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_JUMUAH_PREP_REMINDER, enabled).apply()

    fun getJumuahPrepReminderTime(context: Context): String = getPrefs(context).getString(KEY_JUMUAH_PREP_TIME, "10:30") ?: "10:30"
    fun setJumuahPrepReminderTime(context: Context, time: String) = getPrefs(context).edit().putString(KEY_JUMUAH_PREP_TIME, time).apply()

    // --- Sound, Vibration, Display ---
    fun isSoundEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()

    fun isVibrationEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true)
    fun setVibrationEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()

    fun isShowHijriEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SHOW_HIJRI, true)
    fun setShowHijriEnabled(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SHOW_HIJRI, enabled).apply()

    fun isShowSalawatOnWidget(context: Context): Boolean = getPrefs(context).getBoolean(KEY_SHOW_SALAWAT_WIDGET, true)
    fun setShowSalawatOnWidget(context: Context, enabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_SHOW_SALAWAT_WIDGET, enabled).apply()

    // --- Surah Al-Kahf Weekly Reading Status ---
    fun isKahfReadThisWeek(context: Context): Boolean {
        val savedWeek = getPrefs(context).getString(KEY_KAHF_READ_WEEK, "") ?: ""
        return savedWeek == getCurrentWeekKey()
    }

    fun setKahfReadThisWeek(context: Context, isRead: Boolean) {
        val weekKey = if (isRead) getCurrentWeekKey() else ""
        getPrefs(context).edit().putString(KEY_KAHF_READ_WEEK, weekKey).apply()
    }

    // --- Salawat Counter ---
    fun getSalawatCount(context: Context): Int = getPrefs(context).getInt(KEY_SALAWAT_COUNT, 0)
    fun setSalawatCount(context: Context, count: Int) = getPrefs(context).edit().putInt(KEY_SALAWAT_COUNT, count).apply()
    fun incrementSalawat(context: Context): Int {
        val next = getSalawatCount(context) + 1
        setSalawatCount(context, next)
        return next
    }
    fun resetSalawat(context: Context) = setSalawatCount(context, 0)

    fun getSalawatGoal(context: Context): Int = getPrefs(context).getInt(KEY_SALAWAT_GOAL, 300)
    fun setSalawatGoal(context: Context, goal: Int) = getPrefs(context).edit().putInt(KEY_SALAWAT_GOAL, goal).apply()

    // --- Checked Sunan for the week ---
    fun getCheckedSunan(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_CHECKED_SUNAN, emptySet()) ?: emptySet()
    }

    fun toggleSunnahChecked(context: Context, sunnahId: String) {
        val current = getCheckedSunan(context).toMutableSet()
        if (current.contains(sunnahId)) {
            current.remove(sunnahId)
        } else {
            current.add(sunnahId)
        }
        getPrefs(context).edit().putStringSet(KEY_CHECKED_SUNAN, current).apply()
    }

    fun resetSunanProgress(context: Context) {
        getPrefs(context).edit().remove(KEY_CHECKED_SUNAN).apply()
    }

    // --- Favorite and Completed Adhkar ---
    fun getFavoriteAdhkar(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_FAVORITE_ADHIKR, emptySet()) ?: emptySet()
    }

    fun toggleFavoriteDhikr(context: Context, dhikrId: String) {
        val current = getFavoriteAdhkar(context).toMutableSet()
        if (current.contains(dhikrId)) current.remove(dhikrId) else current.add(dhikrId)
        getPrefs(context).edit().putStringSet(KEY_FAVORITE_ADHIKR, current).apply()
    }

    fun getCompletedAdhkar(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_COMPLETED_ADHIKR, emptySet()) ?: emptySet()
    }

    fun toggleCompletedDhikr(context: Context, dhikrId: String) {
        val current = getCompletedAdhkar(context).toMutableSet()
        if (current.contains(dhikrId)) current.remove(dhikrId) else current.add(dhikrId)
        getPrefs(context).edit().putStringSet(KEY_COMPLETED_ADHIKR, current).apply()
    }

    // --- Complete Reset of Friday Data Only ---
    fun resetAllFridayData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
