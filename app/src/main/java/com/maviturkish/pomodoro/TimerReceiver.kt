package com.maviturkish.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Fires when the timer ends while the app is in the background. */
class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = PomodoroState.load(context)
        // Only act if a timer is actually running and has reached its end
        if (state.isRunning && state.endAt <= System.currentTimeMillis() + 1_000L) {
            TimerAlarm.completeCurrent(context, state)
        }
    }
}
