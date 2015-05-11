package no.simplicityworks.kitchenlogistics

import java.util.Date

import android.os.Bundle
import android.support.v7.app.{ActionBar, ActionBarActivity}
import android.support.v7.internal.widget.AdapterViewCompat
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.util.Log
import android.view.{View, MenuItem, Menu, ViewGroup}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.widget.{AdapterView, TextView, ArrayAdapter, Button}
import org.scaloid.common._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.Future
import scala.util.{Failure, Success}

class MainActivity extends ActionBarActivity with SActivity with TypedActivity with KitLogRestStorage with MockDialogScanner with Dialogs {

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.actionBarSearch =>
        //search
        true
      case _ =>
        Log.i("MainActivity", item.toString)
        super.onOptionsItemSelected(item)
    }
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main2)
    val view = this.findResource(TR.my_recycler_view)
    view.setHasFixedSize(true)
    view.setLayoutManager(new LinearLayoutManager(this))
    view.setAdapter(ItemAdapter)
    val actionBar = getSupportActionBar
    actionBar.setDisplayHomeAsUpEnabled(true)
    val leftDrawer = this.findResource(TR.left_drawer)
    Future {
      database.findItemGroups().toList
    } onComplete {
      case Success(itemGroups) =>
        runOnUiThread {
          leftDrawer.setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, itemGroups))
        }
      case Failure(e) => handleFailure(e)
    }
    leftDrawer.onItemClick((_: AdapterView[_], _: View, position: Int, _: Long) => ItemAdapter.loadItems(Some(leftDrawer.getAdapter.getItem(position).asInstanceOf[ItemGroup])))
//    leftDrawer.setOnItemClickListener(new OnItemClickListener {
//      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = Log.i("MainActivity", "nu da")
//    })

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

  object ItemAdapter extends RecyclerView.Adapter[ItemViewHolder] {
    var itemSummaries: List[ItemSummary] = Nil

    def loadItems(itemGroup: Option[ItemGroup] = None) {
      Future {
        database.findItems().toList
      } onComplete {
        case Success(items) =>
          itemSummaries = items
          runOnUiThread {
            notifyDataSetChanged()
            val title = itemGroup.map(_.name).getOrElse("All")
            Log.i("MainActivity", s"Ending up with title: $title")
            setTitle(title)
            MainActivity.this.findResource(TR.drawer_layout).closeDrawer(MainActivity.this.findResource(TR.left_drawer))
          }
        case Failure(e) => handleFailure(e)
      }
    }
    loadItems()

    override def getItemCount: Int = itemSummaries.size

    override def onBindViewHolder(vh: ItemViewHolder, i: Int) {
      vh.v.setText(itemSummaries(i).product.name)
    }

    override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemViewHolder = {
      new ItemViewHolder(new TextView(MainActivity.this))
    }
  }

}
