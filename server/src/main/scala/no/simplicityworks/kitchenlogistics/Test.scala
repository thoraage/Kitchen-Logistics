package no.simplicityworks.kitchenlogistics

object Test extends App {

    def herp(i: Int): List[String] =
        if (i<1) Nil
        else "o!" :: herp(i-1)

    val lol = List(1, 2, 3).flatMap(herp)

    println(lol)

}
