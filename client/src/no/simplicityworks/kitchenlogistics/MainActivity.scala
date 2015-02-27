package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import collection.JavaConversions.seqAsJavaList

class MainActivity extends TypedActivity with LocalSQLiteStorage with ZXingScanner with Dialogs {

  def updateItemsList() {
    val itemNames = database.findItems().map(item => item.product.name + " - " + item.product.code)
    findView(TR.scannedItemList).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemNames))
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
          database.findProductByCode(code) match {
            case Some(product) =>
              createItem(product)
            case None =>
              createInputDialog(832462, R.string.productNameTitle, R.string.productNameMessage, {
                name => createItem(database.saveProduct(Product(None, code, name)))
              })
          }
      }
    }
  }

}
