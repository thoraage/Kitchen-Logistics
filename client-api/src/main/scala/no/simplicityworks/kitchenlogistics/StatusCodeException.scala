package no.simplicityworks.kitchenlogistics

case class StatusCodeException(message: String, status: Int, throwable: Option[Throwable] = None)
    extends RuntimeException(message, throwable.orNull)
