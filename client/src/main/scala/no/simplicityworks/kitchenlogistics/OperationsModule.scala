package no.simplicityworks.kitchenlogistics

import java.text.MessageFormat
import java.util.Date

import android.content.DialogInterface
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.util.Log
import android.view._
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.{AdapterView, ArrayAdapter, PopupMenu}
import no.simplicityworks.kitchenlogistics.TypedResource._
import org.scaloid.common._

import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

trait OperationsModule {

    def operations: Operations

}

trait Operations {
    def setMenu(menu: Menu)

    def initiate()

    def scanNewItem()

    def scanRemoveItem()

    def populateDrawerMenu(): Future[Unit]

    def createNewItemGroup()

    def renameItemGroupName()

    def removeItemGroupName()

}

trait OperationsImplModule extends OperationsModule with ScannerModule with StorageModule with GuiContextModule with DialogsModule {

    override lazy val operations = new Operations {

        var selectedItemGroup: Option[ItemGroup] = None
        var itemGroupDrawerMenuChoices: List[ItemGroupDrawerMenuChoice] = Nil
        lazy val leftDrawer = guiContext.findView(TR.left_drawer)
        var menu: Option[Menu] = None

        override def initiate() {
            val view = guiContext.findView(TR.recycler_view)
            view.setHasFixedSize(true)
            view.setLayoutManager(new LinearLayoutManager(guiContext))
            view.setAdapter(ItemAdapter)
            leftDrawer.onItemClick { (_: AdapterView[_], _: View, position: Int, _: Long) =>
                val choice = leftDrawer.getAdapter.getItem(position).asInstanceOf[DrawerMenuChoice]
                guiContext.findView(TR.drawer_layout).closeDrawer(guiContext.findView(TR.left_drawer))
                choice.onSelect()
            }
        }

        override def scanNewItem() {
            scanner.startScanner { code =>
                def saveItem(product: Product, itemGroupId: Int) {
                    storage.saveItem(Item(None, None, product.id.get, itemGroupId, new Date)) onComplete {
                        case Success(_) =>
                            guiContext.runOnUiThread(reloadItemList())
                        case Failure(t) => handleFailure(t)
                    }
                }
                def createItem(product: Product) {
                    selectedItemGroup match {
                        case Some(ItemGroup(Some(itemGroupId), _, _, _)) =>
                            saveItem(product, itemGroupId)
                            WidgetHelpers.toast(R.string.itemNewCreated)
                        case _ =>
                            selectItemGroupExplicitly(R.string.selectItemGroupCancelled, itemGroup => {
                                itemGroup.id.foreach(saveItem(product, _))
                                changeItemGroup(itemGroup)
                            })
                    }
                    selectedItemGroup.flatMap(_.id).foreach(saveItem(product, _))
                }
                storage.findProductByCode(code) onComplete {
                    // TODO case many =>
                    case Success(product #:: _) =>
                        createItem(product)
                    case Success(Stream.Empty) =>
                        runOnUiThread(
                            dialogs.withField(R.string.productNameTitle, (name, feedback) => {
                                if (name.trim.length == 0) {
                                    feedback(R.string.fieldRequired.r2String)
                                } else {
                                    storage.saveProduct(Product(None, code, name, new Date)) onComplete {
                                        case Success(product) =>
                                            createItem(product)
                                            WidgetHelpers.toast(R.string.productNewCreated)
                                        case Failure(t) =>
                                            handleFailure(t)
                                    }
                                }
                            }))
                    case Failure(t) => handleFailure(t)
                }
            }
        }

        override def scanRemoveItem() {
            scanner.startScanner { code =>
                storage.findItemsByCode(code).map(_.toList) onComplete {
                    case Success(Nil) =>
                        dialogs.withMessage(R.string.notFoundTitle, R.string.itemWithCodeNotFoundMessage)
                    case Success(item :: Nil) =>
                        storage.removeItem(item.lastItemId) onFailure PartialFunction(handleFailure)
                    case Success(items) =>
                        handleFailure(new NotImplementedError(""))
                    case Failure(t) =>
                        handleFailure(t)
                }
            }
        }

        def futureOnUiThread[T](f: => T): Future[T] = {
            val promise = Promise[T]()
            guiContext.runOnUiThread {
                promise.complete(Try(f))
            }
            promise.future
        }

        val allItems = new ItemGroupDrawerMenuChoice(None)

        override def populateDrawerMenu(): Future[Unit] = {
            val future = storage.findItemGroups().map(_.toList).flatMap { itemGroups =>
                futureOnUiThread {
                    itemGroupDrawerMenuChoices = itemGroups.map(itemGroup => new ItemGroupDrawerMenuChoice(Some(itemGroup)))
                    val choices = allItems :: itemGroupDrawerMenuChoices
                    leftDrawer.setAdapter(new ArrayAdapter(guiContext, R.layout.itemgroup_list_itemgroup, choices.asJava))
                }
            }
            future.onFailure { case t: Throwable => handleFailure(t) }
            future
        }

        trait DrawerMenuChoice {
            def onSelect(): Unit
        }

        def changeItemGroup(itemGroup: ItemGroup): Unit = {
            val choice = itemGroupDrawerMenuChoices.find(_.itemGroup.exists(_.id == itemGroup.id))
            choice.foreach(_.onSelect())
        }

        override def createNewItemGroup() {
            dialogs.withField(R.string.createItemGroupNameTitle, (name, feedback) => {
                if (name.trim.length == 0) {
                    feedback(R.string.fieldRequired.r2String)
                } else {
                    storage.saveItemGroup(ItemGroup(None, None, name, new Date)) onComplete {
                        case Success(itemGroup) =>
                            populateDrawerMenu() onSuccess {
                                case _ =>
                                    changeItemGroup(itemGroup)
                                    WidgetHelpers.toast(R.string.createdItemGroup)
                            }
                        case Failure(t) =>
                            handleFailure(t)
                    }
                }
            })
        }

        override def renameItemGroupName() {
            dialogs.withField(R.string.renameItemGroupNameTitle, (name, feedback) => {
                if (name.trim.length == 0) {
                    feedback(R.string.fieldRequired.r2String)
                } else {
                    selectedItemGroup.foreach { itemGroup =>
                        storage.saveItemGroup(itemGroup.copy(name = name)) onComplete {
                            case Success(_) =>
                                populateDrawerMenu() onComplete {
                                    case Success(_) =>
                                        changeItemGroup(itemGroup)
                                        WidgetHelpers.toast(R.string.renamedItemGroup)
                                    case Failure(t) =>
                                        handleFailure(t)
                                }
                            case Failure(t) =>
                                handleFailure(t)
                        }
                    }
                }
            })
        }

        override def removeItemGroupName() {
            dialogs.confirm(R.string.confirmRemoveItemGroupNameTitle, {
                selectedItemGroup.flatMap(_.id).foreach { itemGroupId =>
                    storage.removeItemGroup(itemGroupId) onComplete {
                        case Success(_) =>
                            populateDrawerMenu() onComplete {
                                case Success(_) =>
                                    allItems.onSelect()
                                    WidgetHelpers.toast(R.string.removedItemGroup)
                                case Failure(t) =>
                                    handleFailure(t)
                            }
                        case Failure(e) =>
                            handleFailure(e)
                    }
                }
            })
        }

        class ItemGroupDrawerMenuChoice(val itemGroup: Option[ItemGroup]) extends DrawerMenuChoice {
            override def toString = itemGroup.map(_.name).getOrElse(R.string.drawerMenuAll.r2String)

            override def onSelect() {
                selectedItemGroup = itemGroup
                updateMenu()
                storage.findItemsByGroup(selectedItemGroup).map(_.toList) onComplete {
                    case Success(items) =>
                        ItemAdapter.itemSummaries = items
                        futureOnUiThread {
                            ItemAdapter.notifyDataSetChanged()
                            guiContext.setTitle(toString)
                        }
                    case Failure(e) => handleFailure(e)
                }
            }
        }

        def updateMenu() {
            for {
                menu <- menu.toSeq
                item <- Seq(R.id.action_bar_rename_item_group, R.id.action_bar_remove_item_group)
            } menu.findItem(item).setVisible(selectedItemGroup.nonEmpty)
        }

        def handleFailure(throwable: Throwable) {
            Log.e(getClass.getSimpleName, "GUI error", throwable)
            WidgetHelpers.toast(R.string.errorIntro + throwable.getMessage)
        }

        object ItemAdapter extends RecyclerView.Adapter[ItemViewHolder] {
            val inflater = LayoutInflater.from(guiContext)
            var itemSummaries: List[ItemSummary] = Nil

            new ItemGroupDrawerMenuChoice(None).onSelect()

            override def getItemCount: Int = itemSummaries.size

            override def onBindViewHolder(vh: ItemViewHolder, i: Int) {
                val count = itemSummaries(i).count
                Seq(
                    (TR.item_name, itemSummaries(i).product.name),
                    (TR.item_count, if (count == 1) "" else R.string.itemListCountItem.r2String.format(count))
                ).foreach(p => vh.view.findView(p._1).setText(p._2))
            }

            override def onCreateViewHolder(viewGroup: ViewGroup, itemIdx: Int): ItemViewHolder = {
                val view = inflater.inflate(R.layout.item_list_item, viewGroup, false)
                view.onLongClick {
                    val popup = new PopupMenu(guiContext, view)
                    val inflater = popup.getMenuInflater
                    inflater.inflate(R.menu.item_popup, popup.getMenu)
                    popup.setOnMenuItemClickListener(new OnMenuItemClickListener {
                        override def onMenuItemClick(item: MenuItem) = {
                            val itemSummary = itemSummaries(itemIdx)
                            item.getItemId match {
                                case R.id.item_popup_remove =>
                                    storage.removeItem(itemSummary.lastItemId) onComplete {
                                        case Success(_) =>
                                            reloadItemList()
                                            WidgetHelpers.toast(R.string.removedItem)
                                        case Failure(f) => handleFailure(f)
                                    }
                                    true
                                case R.id.item_popup_move =>
                                    selectItemGroupExplicitly(R.string.itemMoveCancelled, { itemGroup =>
                                        storage.getItem(itemSummary.lastItemId).map(_.copy(itemGroupId = itemGroup.id.get)).flatMap(storage.saveItem) onComplete {
                                            case Success(_) =>
                                                reloadItemList()
                                                WidgetHelpers.toast(new MessageFormat(R.string.itemMovedToItemGroup.r2String).format(Array(itemSummary.product.name, itemGroup.name)))
                                            case Failure(f) => handleFailure(f)
                                        }
                                    })
                                    true
                                case _ => false
                            }
                        }
                    })
                    popup.show()
                    true
                }
                new ItemViewHolder(view)
            }
        }

        override def setMenu(menu: Menu) {
            this.menu = Some(menu)
            updateMenu()
        }

        class ItemViewHolder(val view: View) extends RecyclerView.ViewHolder(view)

        def reloadItemList() {
            new ItemGroupDrawerMenuChoice(selectedItemGroup).onSelect()
        }

        def selectItemGroupExplicitly(cancelMessageId: CharSequence, doWithItemGroup: (ItemGroup) => Unit) {
            storage.findItemGroups().map(_.toArray) onComplete {
                case Success(itemGroups) =>
                    val builder = new AlertDialogBuilder(R.string.selectItemGroupTitle, null).negativeButton(R.string.inputDialogCancel, (dialog, _) => {
                        WidgetHelpers.toast(cancelMessageId)
                        dialog.cancel()
                    })
                    builder.setItems(itemGroups.map(_.name.asInstanceOf[CharSequence]), new DialogInterface.OnClickListener {
                        override def onClick(dialog: DialogInterface, which: Int) {
                            doWithItemGroup(itemGroups(which))
                        }
                    })
                    builder.show()
                case Failure(t) => handleFailure(t)
            }
        }

    }

}