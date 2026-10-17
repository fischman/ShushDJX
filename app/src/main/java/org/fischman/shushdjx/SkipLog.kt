package org.fischman.shushdjx

import android.os.SystemClock

// In-memory record of recent DJ X skips, shared by the service and the UI (same process).
// Times are SystemClock.elapsedRealtime() (millis).
object SkipLog {
    const val MAX_ENTRIES = 20
    const val MAX_AGE_MS = 24 * 60 * 60 * 1000L

    private val skips = ArrayDeque<Long>() // Most recent first.
    private val listeners = mutableSetOf<() -> Unit>()

    fun record() {
        skips.addFirst(SystemClock.elapsedRealtime())
        if (skips.size > MAX_ENTRIES) skips.removeLast()
        listeners.toList().forEach { it() }
    }

    // Elapsed-realtime timestamps of skips within MAX_AGE_MS, most recent first.
    fun recent(now: Long = SystemClock.elapsedRealtime()): List<Long> {
        while (skips.isNotEmpty() && now - skips.last() >= MAX_AGE_MS) skips.removeLast()
        return skips.toList()
    }

    fun addListener(l: () -> Unit) { listeners.add(l) }
    fun removeListener(l: () -> Unit) { listeners.remove(l) }
}
