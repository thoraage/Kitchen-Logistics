package no.simplicityworks.kitchenlogistics

class Boot {
  
  val mainModule = new StorageService with ThreadMountedScalaQuerySession {
    // bake your module cake here
  }

}