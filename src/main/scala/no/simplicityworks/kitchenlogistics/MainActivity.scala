package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import collection.JavaConversions._

class MainActivity extends TypedActivity with LocalSQLiteStorage with ZXingScanner with Dialogs {

  def updateItemsList() {
    val items = database.findItems().map(_.product.name)
    findView(TR.scannedItemList).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, items))
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    updateItemsList()
    findView(TR.registerProductButton).setOnClickListener {
      (_: View) => startScanner {
        def createItem(product: Product) {
          database.saveItem(Item(None, product.id.get))
          updateItemsList()
        }
        code =>
          database.findProductByCode(code).map(createItem).getOrElse {
            createInputDialog(832462, R.string.productNameTitle, R.string.productNameMessage, {
              name => createItem(database.saveProduct(Product(None, code, name)))
            })
          }
      }
    }
  }

}
