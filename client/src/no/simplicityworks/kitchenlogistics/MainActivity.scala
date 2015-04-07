package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.view.View
import android.widget.{ArrayAdapter, Button}
import org.scaloid.common._

import scala.collection.JavaConversions.seqAsJavaList

class MainActivity extends SActivity with TypedActivity with KitLogRestStorage with ZXingScanner with Dialogs {

  def updateItemsList() {
    val itemNames = database.findItems().map(item => item.product.name + " - " + item.product.code)
    find(TR.scannedItemList).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemNames))
  }

  def updateItemGroupSpinner() {
    val itemGroups = database.findItemGroups().map(_.name)
    find(TR.selectItemGroupSpinner).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemGroups))
  }

  def find[V <: View](tr: TypedResource[V]): V = find[V](tr.id)

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    updateItemsList()
    updateItemGroupSpinner()
    find(TR.registerProductButton).onClick {
      _: Button => startScanner {
        def createItem(product: Product) {
          database.saveItem(Item(None, product.id.get))
          updateItemsList()
        }
        code =>
          database.findProductByCode(code) match {
            // TODO case many =>
            case List(product) =>
              createItem(product)
            case Nil =>
              createInputDialog(832462, R.string.productNameTitle, R.string.productNameMessage, {
                name => createItem(database.saveProduct(Product(None, code, name, "2015-03-01T00:00:00.000Z")))
              })
          }
      }
    }
  }

}
