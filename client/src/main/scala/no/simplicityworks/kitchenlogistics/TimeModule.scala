package no.simplicityworks.kitchenlogistics

import java.util.Date

import org.scaloid.common._

trait TimeModule extends GuiContextModule {

    def time = new Time {
        override def toHumanReadableDate(date: Date) = {
            import R.{string => r}
            val sec = 1000L
            val min = sec * 60
            val hour = min * 60
            val day = hour * 24
            val (year, month, week) = (day * 365, day * 30, day * 7)
            val spans = List(
                (year, (r.years, r.year)),
                (month, (r.months, r.month)),
                (week, (r.weeks, r.week)),
                (day, (r.days, r.day)),
                (hour, (r.hours, r.hour)),
                (min, (r.minutes, r.minute)),
                (sec, (r.seconds, r.second)))
            val (time, past) = {
                val time = (new Date).getTime - date.getTime
                (Math.abs(time), time > 0)
            }
            val distribution = Distribution.distribute(spans.map(_._1), time)
            val s = distribution.zip(spans.map(_._2)).dropWhile(_._1 == 0).take(2).filter(_._1 != 0) match {
                case Nil => r.now.r2String
                case times =>
                    times.map {
                        case (1, (_, singularis)) => s"1 ${singularis.r2String}"
                        case (i, (pluralis, _)) => s"$i ${pluralis.r2String}"
                    }.mkString(s" ${R.string.and.r2String} ")  + " " +
                        (if (past) R.string.ago else R.string.until).r2String
            }

            s"$s ($date}"
        }

    }

}

trait Time {

    def toHumanReadableDate(date: Date): String

}