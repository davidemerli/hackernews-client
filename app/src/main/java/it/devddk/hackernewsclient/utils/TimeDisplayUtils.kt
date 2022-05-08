package it.devddk.hackernewsclient.utils

import android.content.Context
import it.devddk.hackernewsclient.R
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TimeDisplayUtils(private val context: Context) {
    fun toDateTimeAgoInterval(
        time: LocalDateTime?,
        now: LocalDateTime = LocalDateTime.now(),
    ): String {
        val unitMap = mapOf(ChronoUnit.YEARS to R.plurals.years_ago,
            ChronoUnit.WEEKS to R.plurals.weeks_ago,
            ChronoUnit.DAYS to R.plurals.days_ago,
            ChronoUnit.HOURS to R.plurals.hours_ago,
            ChronoUnit.MINUTES to R.plurals.minutes_ago)

        if(time == null) {
            context.getString(R.string.time_unknown)
        }

        return unitMap.firstNotNullOfOrNull { (unit, stringId) ->
            val btw = unit.between(time, now)
            when {
                btw >= Integer.MAX_VALUE -> context.getString(R.string.long_ago)
                btw > 0 -> context.resources.getQuantityString(stringId, btw.toInt(), btw.toInt())
                else -> null
            }
        } ?: context.getString(R.string.now)
    }
}