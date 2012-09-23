package no.simplicityworks.kitchenlogistics

object Boot extends App {
  
  (new JettyWebPlanComponent
//    with StaticContentWebComponent
    with ScanWebPlanComponent
    with RestWebPlanComponent
    with ProductDatabaseComponent).run()

}