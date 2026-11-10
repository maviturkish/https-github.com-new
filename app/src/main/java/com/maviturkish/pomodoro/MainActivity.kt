package com.maviturkish.pomodoro

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var state: PomodoroState

    private lateinit var ring: RingView
    private lateinit var timeText: TextView
    private lateinit var modeText: TextView
    private lateinit var sessionsText: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var skipButton: Button
    private lateinit var tabs: Map<Mode, TextView>

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            tick()
            if (state.isRunning) handler.postDelayed(this, 250L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ring = findViewById(R.id.ring)
        timeText = findViewById(R.id.timeText)
        modeText = findViewById(R.id.modeText)
        sessionsText = findViewById(R.id.sessionsText)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        skipButton = findViewById(R.id.skipButton)
        tabs = mapOf(
            Mode.FOCUS to findViewById(R.id.tabFocus),
            Mode.SHORT_BREAK to findViewById(R.id.tabShort),
            Mode.LONG_BREAK to findViewById(R.id.tabLong)
        )

        tabs.forEach { (mode, view) ->
            view.setOnClickListener {
                TimerAlarm.cancel(this)
                state.selectMode(mode)
                render()
            }
        }

        startButton.setOnClickListener {
            if (state.isRunning) {
                state.pause()
                TimerAlarm.cancel(this)
            } else {
                askNotificationPermission()
                TimerAlarm.clearNotification(this)
                state.start()
                TimerAlarm.schedule(this, state.endAt)
                handler.removeCallbacks(ticker)
                handler.post(ticker)
            }
            render()
        }

        resetButton.setOnClickListener {
            TimerAlarm.cancel(this)
            state.reset()
            render()
        }

        skipButton.setOnClickListener {
            TimerAlarm.cancel(this)
            state.advance(countSession = false)
            render()
        }

        // Long-press the counter to reset today's sessions
        sessionsText.setOnLongClickListener {
            state.resetSessions()
            render()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        state = PomodoroState.load(this)
        tick()
        if (state.isRunning) {
            handler.removeCallbacks(ticker)
            handler.post(ticker)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun tick() {
        if (state.isRunning && state.remainingNow() <= 0L) {
            TimerAlarm.completeCurrent(this, state)
        }
        render()
    }

    private fun render() {
        val mode = state.mode
        val remaining = state.remainingNow()

        val totalSeconds = (remaining + 999L) / 1000L
        timeText.text = String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
        modeText.text = if (state.isRunning) mode.label else if (remaining < mode.durationMs) "Paused" else mode.label

        ring.ringColor = mode.color
        ring.progress = remaining.toFloat() / mode.durationMs.toFloat()

        startButton.text = when {
            state.isRunning -> "Pause"
            remaining < mode.durationMs -> "Resume"
            else -> "Start"
        }
        startButton.backgroundTintList = ColorStateList.valueOf(mode.color)

        tabs.forEach { (m, view) ->
            val selected = m == mode
            view.setBackgroundResource(if (selected) R.drawable.tab_selected else 0)
            view.setTextColor(if (selected) 0xFFFFFFFF.toInt() else 0x99FFFFFF.toInt())
        }

        val inCycle = state.sessions % 4
        val dots = (1..4).joinToString(" ") { if (it <= inCycle) "●" else "○" }
        sessionsText.text = "$dots\nCompleted today: ${state.sessions}"

        window.statusBarColor = 0xFF151820.toInt()
        if (state.isRunning) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }
}
