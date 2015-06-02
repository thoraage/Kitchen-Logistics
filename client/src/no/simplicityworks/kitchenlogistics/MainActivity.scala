package no.simplicityworks.kitchenlogistics

import java.util.Date

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.util.Log
import android.view.{Menu, MenuItem, View, ViewGroup}
import android.widget.{AdapterView, ArrayAdapter, TextView}
import org.scaloid.common._

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Try, Failure, Success}

class MainActivity extends ActionBarActivity with SActivity with TypedActivity with KitLogRestStorageModule with MockDialogScannerModule with Dialogs {

  lazy val leftDrawer = this.findResource(TR.left_drawer)

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.actionBarSearch =>
        //search
        true
      case R.id.actionBarNew =>
        scanner.startScanner { code =>
          def createItem(product: Product) {
            selectedItemGroup.flatMap(_.id).foreach { itemGroupId =>
              storage.saveItem(Item(None, None, product.id.get, itemGroupId, new Date)) onComplete {
                case Success(_) =>
                  runOnUiThread(new ItemGroupDrawerMenuChoice(selectedItemGroup).onSelect())
                case Failure(t) => handleFailure(t)
              }
            }
          }
          storage.findProductByCode(code) onComplete {
            // TODO case many =>
            case Success(product #:: _) =>
              createItem(product)
            case Success(Stream.Empty) =>
              createInputDialog(832462, R.string.productNameTitle, R.string.productNameMessage, { name =>
                storage.saveProduct(Product(None, code, name, new Date)) onComplete {
                  case Success(product) => createItem(product)
                  case Failure(t) => handleFailure(t)
                }
              })
            case Failure(t) => handleFailure(t)
          }
        }
        true
      case _ =>
        Log.i("MainActivity", item.toString)
        super.onOptionsItemSelected(item)
    }
  }

  trait DrawerMenuChoice {
    def onSelect(): Unit
  }

  object NewItemGroupDrawerMenuChoice extends DrawerMenuChoice {
    override def toString = R.string.drawerMenuNewItemGroup.r2String
    override def onSelect() {
      createInputDialog(34002784, R.string.itemGroupNameTitle, R.string.itemGroupNameMessage, { name =>
        storage.saveItemGroup(ItemGroup(None, None, name, new Date)) onComplete {
          case Success(itemGroup) =>
            populateDrawerMenu() foreach { _ =>
              println(itemGroupDrawerMenuChoices + ", " + itemGroup)
              val choice = itemGroupDrawerMenuChoices.find(_.itemGroup.exists(_.id == itemGroup.id))
              choice.foreach(_.onSelect())
            }
          case Failure(t) => handleFailure(t)
        }
      })
    }
  }

  var selectedItemGroup: Option[ItemGroup] = None
  var itemGroupDrawerMenuChoices: List[ItemGroupDrawerMenuChoice] = Nil

  class ItemGroupDrawerMenuChoice(val itemGroup: Option[ItemGroup]) extends DrawerMenuChoice {
    override def toString = itemGroup.map(_.name).getOrElse(R.string.drawerMenuAll.r2String)
    override def onSelect() {
      selectedItemGroup = itemGroup
      storage.findItems(selectedItemGroup).map(_.toList) onComplete {
        case Success(items) =>
          ItemAdapter.itemSummaries = items
          runOnUiThread {
            ItemAdapter.notifyDataSetChanged()
            setTitle(toString)
          }
        case Failure(e) => handleFailure(e)
      }
    }
  }

  def futureOnUiThread[T](f: => T): Future[T] = {
    val promise = Promise[T]()
    runOnUiThread {
      promise.complete(Try(f))
    }
    promise.future
  }

  def populateDrawerMenu(): Future[Unit] = {
    val future = storage.findItemGroups().map(_.toList).flatMap { itemGroups =>
      futureOnUiThread {
        itemGroupDrawerMenuChoices = itemGroups.map(itemGroup => new ItemGroupDrawerMenuChoice(Some(itemGroup)))
        val choices = (new ItemGroupDrawerMenuChoice(None) ::
            itemGroupDrawerMenuChoices) :::
            List(NewItemGroupDrawerMenuChoice)
        leftDrawer.setAdapter(new ArrayAdapter(this, R.layout.itemlistitem, choices))
      }
    }
    future.onFailure { case t: Throwable => handleFailure(t) }
    future
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    val view = this.findResource(TR.my_recycler_view)
    view.setHasFixedSize(true)
    view.setLayoutManager(new LinearLayoutManager(this))
    view.setAdapter(ItemAdapter)
    val actionBar = getSupportActionBar
    actionBar.setDisplayHomeAsUpEnabled(true)
    populateDrawerMenu()

    leftDrawer.onItemClick { (_: AdapterView[_], _: View, position: Int, _: Long) =>
      val choice = leftDrawer.getAdapter.getItem(position).asInstanceOf[DrawerMenuChoice]
      MainActivity.this.findResource(TR.drawer_layout).closeDrawer(MainActivity.this.findResource(TR.left_drawer))
      choice.onSelect()
    }
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

  def handleFailure(throwable: Throwable) = throw throwable

  object ItemAdapter extends RecyclerView.Adapter[ItemViewHolder] {
    var itemSummaries: List[ItemSummary] = Nil

    new ItemGroupDrawerMenuChoice(None).onSelect()

    override def getItemCount: Int = itemSummaries.size

    override def onBindViewHolder(vh: ItemViewHolder, i: Int) {
      vh.v.setText(itemSummaries(i).product.name)
    }

    override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemViewHolder = {
      new ItemViewHolder(new TextView(MainActivity.this))
    }
  }

}
