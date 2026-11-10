package com.maviturkish.pomodoro

import android.content.Context

enum class Mode(val label: String, val minutes: Int, val color: Int) {
    FOCUS("Focus", 25, 0xFFE5534B.toInt()),
    SHORT_BREAK("Short Break", 5, 0xFF3FB68B.toInt()),
    LONG_BREAK("Long Break", 15, 0xFF4A8FE7.toInt());

    val durationMs: Long get() = minutes * 60_000L
}

/**
 * The whole timer state lives in SharedPreferences so it survives the app
 * being closed. While running we store the absolute end time; while paused
 * or idle we store the remaining milliseconds.
 */
class PomodoroState private constructor(private val context: Context) {

    var mode: Mode = Mode.FOCUS
    var endAt: Long = 0L          // > 0 means running
    var remaining: Long = Mode.FOCUS.durationMs
    var sessions: Int = 0         // completed focus sessions

    val isRunning: Boolean get() = endAt > 0L

    fun remainingNow(now: Long = System.currentTimeMillis()): Long =
        if (isRunning) (endAt - now).coerceAtLeast(0L) else remaining

    fun start(now: Long = System.currentTimeMillis()) {
        if (isRunning) return
        endAt = now + remaining
        save()
    }

    fun pause(now: Long = System.currentTimeMillis()) {
        if (!isRunning) return
        remaining = (endAt - now).coerceAtLeast(0L)
        endAt = 0L
        save()
    }

    fun reset() {
        endAt = 0L
        remaining = mode.durationMs
        save()
    }

    fun selectMode(newMode: Mode) {
        mode = newMode
        reset()
    }

    /** Move to the next phase. Counts the session only if it was a finished focus. */
    fun advance(countSession: Boolean) {
        val next = if (mode == Mode.FOCUS) {
            if (countSession) sessions++
            if (sessions > 0 && sessions % 4 == 0) Mode.LONG_BREAK else Mode.SHORT_BREAK
        } else {
            Mode.FOCUS
        }
        mode = next
        endAt = 0L
        remaining = next.durationMs
        save()
    }

    fun resetSessions() {
        sessions = 0
        save()
    }

    fun save() {
        prefs(context).edit()
            .putString(K_MODE, mode.name)
            .putLong(K_END, endAt)
            .putLong(K_REMAINING, remaining)
            .putInt(K_SESSIONS, sessions)
            .apply()
    }

    companion object {
        private const val PREFS = "pomodoro"
        private const val K_MODE = "mode"
        private const val K_END = "endAt"
        private const val K_REMAINING = "remaining"
        private const val K_SESSIONS = "sessions"

        private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        fun load(context: Context): PomodoroState {
            val p = prefs(context)
            return PomodoroState(context.applicationContext).apply {
                mode = runCatching { Mode.valueOf(p.getString(K_MODE, null) ?: "") }
                    .getOrDefault(Mode.FOCUS)
                endAt = p.getLong(K_END, 0L)
                remaining = p.getLong(K_REMAINING, mode.durationMs)
                sessions = p.getInt(K_SESSIONS, 0)
            }
        }
    }
}
