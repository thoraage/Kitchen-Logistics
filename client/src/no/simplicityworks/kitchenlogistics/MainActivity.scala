package no.simplicityworks.kitchenlogistics

import java.util.Date

import android.os.Bundle
import android.widget.{ArrayAdapter, Button}
import org.scaloid.common._

import scala.collection.JavaConversions.seqAsJavaList

class MainActivity extends SActivity with TypedActivity with KitLogRestStorage with MockDialogScanner with Dialogs {

  def updateItemsList() {
    val itemNames = database.findItems().map(item => item.product.name + " - " + item.product.code)
    this.findResource(TR.scannedItemList).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemNames))
  }

  def updateItemGroupSpinner() {
    val itemGroups = database.findItemGroups()
    this.findResource(TR.selectItemGroupSpinner).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemGroups))
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    updateItemsList()
    updateItemGroupSpinner()
    this.findResource(TR.registerProductButton).onClick {
      startScanner { code =>
        def createItem(product: Product) {
          val itemGroup = Option(this.findResource(TR.selectItemGroupSpinner).getSelectedItem.asInstanceOf[ItemGroup])
          itemGroup.flatMap(_.id).foreach { itemGroupId =>
            database.saveItem(Item(None, None, product.id.get, itemGroupId, new Date))
            updateItemsList()
          }
        }
        database.findProductByCode(code) match {
          // TODO case many =>
          case product #:: _ =>
            createItem(product)
          case Stream.Empty =>
            createInputDialog(832462, R.string.productNameTitle, R.string.productNameMessage, {
              name => createItem(database.saveProduct(Product(None, code, name, new Date)))
            })
        }
      }
    }
  }

}
