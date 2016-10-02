package no.simplicityworks.kitchenlogistics

import java.util.Date

import android.text.format.{DateFormat, DateUtils}

trait TimeModule extends GuiContextModule {

    def time = new Time {

        override def toHumanReadableDate(date: Date) =
            DateUtils.getRelativeTimeSpanString(date.getTime, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString

    }

}

trait Time {

    def toHumanReadableDate(date: Date): String

}