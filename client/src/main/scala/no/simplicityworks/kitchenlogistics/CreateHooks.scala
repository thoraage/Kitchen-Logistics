package no.simplicityworks.kitchenlogistics

import android.os.Bundle

trait CreateHooks {
  var createHooks: Seq[(Bundle) => Unit] = Nil
}