package no.simplicityworks.kitchenlogistics

import java.util.Date

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.{MenuItem, Menu, ViewGroup}
import android.widget.{TextView, ArrayAdapter, Button}
import org.scaloid.common._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.Future
import scala.util.{Failure, Success}

class MainActivity extends ActionBarActivity with SActivity with TypedActivity with KitLogRestStorage with MockDialogScanner with Dialogs {

  def updateItemsList() {
//    val itemNames = database.findItems().map(item => item.product.name + " - " + item.product.code)
//    this.findResource(TR.scannedItemList).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemNames))
  }

  def updateItemGroupSpinner() {
    val itemGroups = database.findItemGroups()
    this.findResource(TR.selectItemGroupSpinner).setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemGroups))
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.actionBarSearch =>
        //search
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main2)
    val view = this.findResource(TR.my_recycler_view)
    view.setHasFixedSize(true)
    view.setLayoutManager(new LinearLayoutManager(this))
    view.setAdapter(new ItemAdapter)

    updateItemsList()
//    updateItemGroupSpinner()
//    this.findResource(TR.registerProductButton).onClick {
//      startScanner { code =>
//        def createItem(product: Product) {
//          val itemGroup = Option(this.findResource(TR.selectItemGroupSpinner).getSelectedItem.asInstanceOf[ItemGroup])
//          itemGroup.flatMap(_.id).foreach { itemGroupId =>
//            database.saveItem(Item(None, None, product.id.get, itemGroupId, new Date))
//            updateItemsList()
//          }
//        }
//        database.findProductByCode(code) match {
//          // TODO case many =>
//          case product #:: _ =>
//            createItem(product)
//          case Stream.Empty =>
//            createInputDialog(832462, R.string.productNameTitle, R.string.productNameMessage, {
//              name => createItem(database.saveProduct(Product(None, code, name, new Date)))
//            })
//        }
//      }
//    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.actionbar, menu)
    super.onCreateOptionsMenu(menu)
  }

  class ItemViewHolder(val v: TextView) extends RecyclerView.ViewHolder(v) {

  }

  def handleFailure(throwable: Throwable) = throw throwable;

  class ItemAdapter extends RecyclerView.Adapter[ItemViewHolder] {
    var itemSummaries: List[ItemSummary] = Nil


    Future {
      database.findItems().toList
    } onComplete {
      case Success(items) =>
        itemSummaries = items
        runOnUiThread(notifyDataSetChanged())
      case Failure(e) => handleFailure(e)
    }

    override def getItemCount: Int = itemSummaries.size

    override def onBindViewHolder(vh: ItemViewHolder, i: Int) {
      vh.v.setText(itemSummaries(i).product.name)
    }

    override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemViewHolder = {
      new ItemViewHolder(new TextView(MainActivity.this))
    }
  }

}
