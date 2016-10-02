package no.simplicityworks.kitchenlogistics

object Distribution {

    def distribute(spans: List[Long], time: Long): List[Long] = {
        val (_, distribution) = spans.foldLeft((time, List[Long]())) { case ((leftTime, list), spanTime) =>
            val i = leftTime / spanTime
            if (i >= 1) (leftTime - i * spanTime, i :: list) else (leftTime, 0 :: list)
        }
        distribution.reverse
    }

}
