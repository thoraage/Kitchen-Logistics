package no.simplicityworks.kitchenlogistics

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener

class MainActivity extends Activity with TypedActivity with LocalSQLiteStorage with ZXingScanner {

  implicit def toOnClickListener(f: (View) => Unit) = new OnClickListener {
    def onClick(v: View) {
      f(v)
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    findView(TR.textview).setText("hello, world!")
    findView(TR.registerProductButton).setOnClickListener {
      (_: View) => startScanner { code =>
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
