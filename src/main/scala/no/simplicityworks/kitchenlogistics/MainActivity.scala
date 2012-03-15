package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.view.View

class MainActivity extends TypedActivity with LocalSQLiteStorage with MockDialogScanner {

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    findView(TR.textview).setText("hello, world!")
    findView(TR.registerProductButton).setOnClickListener {
      (_: View) => startScanner {
        code =>
          findView(TR.textview).setText(code)
          database.findByIdentifier(code).map {
            product => findView(TR.textview).setText(product.name)
          }.getOrElse {
            database.save(new Product(None, code, "Yoyomama"))
          }
      }
    }
  }

}
