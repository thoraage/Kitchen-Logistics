package no.simplicityworks.kitchenlogistics

import java.text.MessageFormat
import java.util.{Date, Locale}

import android.content.DialogInterface
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.util.Log
import android.view._
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import no.simplicityworks.kitchenlogistics.TypedResource._
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait OperationsModule extends StorageModule {

    def operations: Operations

    trait Operations {
        def initiate()

        def scanNewItem()

        def scanRemoveItem()

        def createNewItemGroup()

        def renameItemGroupName()

        def removeItemGroupName()

        def searchItems()

        def handleFailure(throwable: Throwable)

        def changeItemSummaries(title: String, items: List[ItemSummary])
    }

}

trait OperationsImplModule extends OperationsModule with ScannerModule with GuiContextModule with DialogsModule with StableValuesModule with DrawerMenuModule with TimeModule {

    override lazy val operations: OperationsImpl = new OperationsImpl

    class OperationsImpl extends Operations {

        override def initiate() {
            val view = guiContext.findView(TR.recycler_view)
            view.setHasFixedSize(true)
            view.setLayoutManager(new LinearLayoutManager(guiContext))
            view.setAdapter(ItemAdapter)
            drawerMenu.initiate()
            dialogs.restick()
        }

        override def scanNewItem() {
            def saveItem(product: Product, itemGroupId: Int) {
                storage.saveItem(Item(None, None, product.id.get, itemGroupId, new Date)) onComplete {
                    case Success(_) =>
                        guiContext.runOnUiThread(reloadItemList())
                    case Failure(t) => handleFailure(t)
                }
            }
            def createItem(product: Product) {
                stableValues.selectedItemGroup match {
                    case Some(ItemGroup(Some(itemGroupId), _, _, _)) =>
                        saveItem(product, itemGroupId)
                        WidgetHelpers.toast(R.string.itemNewCreated)
                    case _ =>
                        selectItemGroupExplicitly(R.string.selectItemGroupCancelled, itemGroup => {
                            itemGroup.id.foreach(saveItem(product, _))
                            drawerMenu.changeItemGroup(itemGroup)
                        })
                }
            }
            scanner.startScanner().onComplete {
                case Success(Some(code)) =>
                    storage.findProductByCode(code) onComplete {
                        // TODO case many =>
                        case Success(product #:: _) =>
                            createItem(product)
                        case Success(Stream.Empty) =>
                            runOnUiThread {
                                val title = new MessageFormat(R.string.productNameTitle.r2String).format(Array(code))
                                dialogs.withField(title, "", (name, feedback) => {
                                    if (name.trim.length == 0) {
                                        feedback(R.string.fieldRequired.r2String)
                                    } else {
                                        storage.saveProduct(Product(None, code, name.trim, Locale.getDefault.getISO3Language, new Date)) onComplete {
                                            case Success(product) =>
                                                createItem(product)
                                                WidgetHelpers.toast(R.string.productNewCreated)
                                            case Failure(t) =>
                                                handleFailure(t)
                                        }
                                    }
                                })
                            }
                        case Failure(t) => handleFailure(t)
                    }
                case Success(None) =>
                case Failure(e) =>
                    handleFailure(e)
            }
        }

        override def scanRemoveItem() {
            scanner.startScanner().onComplete{
                case Success(Some(code)) =>
                    storage.findItemsByCode(code).map(_.toList) onComplete {
                        case Success(Nil) =>
                            dialogs.withMessage(R.string.notFoundTitle, R.string.itemWithCodeNotFoundMessage)
                        case Success(item :: Nil) =>
                            removeItem(item.lastItemId)
                        case Success(items) =>
                            handleFailure(new NotImplementedError(""))
                        case Failure(t) =>
                            handleFailure(t)
                    }
                case Success(None) =>
                case Failure(e) =>
                    handleFailure(e)
            }
        }

        override def createNewItemGroup() {
            dialogs.withField(R.string.createItemGroupNameTitle.r2String, "", (name, feedback) => {
                if (name.trim.length == 0) {
                    feedback(R.string.fieldRequired.r2String)
                } else {
                    storage.saveItemGroup(ItemGroup(None, None, name, new Date)) onComplete {
                        case Success(itemGroup) =>
                            drawerMenu.populateDrawerMenu(Some(itemGroup))
                            WidgetHelpers.toast(R.string.createdItemGroup)
                        case Failure(t) =>
                            handleFailure(t)
                    }
                }
            })
        }

        override def renameItemGroupName() {
            val name = stableValues.selectedItemGroup.map(_.name).getOrElse("")
            dialogs.withField(R.string.renameItemGroupNameTitle.r2String, name, (name, feedback) => {
                if (name.trim.length == 0) {
                    feedback(R.string.fieldRequired.r2String)
                } else {
                    stableValues.selectedItemGroup.foreach { itemGroup =>
                        storage.saveItemGroup(itemGroup.copy(name = name)) onComplete {
                            case Success(_) =>
                                drawerMenu.populateDrawerMenu(Some(itemGroup))
                                WidgetHelpers.toast(R.string.renamedItemGroup)
                            case Failure(t) =>
                                handleFailure(t)
                        }
                    }
                }
            })
        }

        override def removeItemGroupName() {
            dialogs.confirm(R.string.confirmRemoveItemGroupNameTitle, {
                stableValues.selectedItemGroup.flatMap(_.id).foreach { itemGroupId =>
                    storage.removeItemGroup(itemGroupId) onComplete {
                        case Success(_) =>
                            drawerMenu.populateDrawerMenu(None)
                            WidgetHelpers.toast(R.string.removedItemGroup)
                        case Failure(e) =>
                            handleFailure(e)
                    }
                }
            })
        }

        override def changeItemSummaries(title: String, items: List[ItemSummary]) {
            ItemAdapter.itemSummaries = items
            guiContext.futureOnUiThread {
                ItemAdapter.notifyDataSetChanged()
                guiContext.setTitle(title)
            }
        }

        override def handleFailure(throwable: Throwable) {
            Log.e(getClass.getSimpleName, "GUI error", throwable)
            WidgetHelpers.toast(R.string.errorIntro + throwable.getMessage)
        }

        trait ViewTypeHandler {
            val viewType: Int
            val layout: Int
            def populate(view: View, itemSummary: ItemSummary)
        }

        object ItemAdapter extends RecyclerView.Adapter[ItemViewHolder] {
            object ShortViewTypeHandler extends ViewTypeHandler {
                override val viewType = 0
                override val layout = R.layout.item_list_item

                override def populate(view: View, itemSummary: ItemSummary) {
                    val count = itemSummary.count
                    Seq(
                        (TR.item_name, itemSummary.product.name),
                        (TR.item_count, if (count == 1) "" else R.string.itemListCountItem.r2String.format(count)),
                        (TR.item_group,
                            if (stableValues.selectedItemGroup.isDefined) ""
                            else stableValues.itemGroups.find(_.id.exists(_ == itemSummary.itemGroupId)).map(_.name).getOrElse(""))
                    ).foreach(p => view.findView(p._1).setText(p._2))
                }
            }
            val inflater = LayoutInflater.from(guiContext)
            object LongViewTypeHandler extends ViewTypeHandler {
                override val viewType = ShortViewTypeHandler.viewType + 1
                override val layout = R.layout.item_list_item_selected

                override def populate(view: View, itemSummary: ItemSummary) {
                    Seq(
                        (TR.item_name, itemSummary.product.name),
                        (TR.item_group,
                            if (stableValues.selectedItemGroup.isDefined) ""
                            else stableValues.itemGroups.find(_.id.exists(_ == itemSummary.itemGroupId)).map(_.name).getOrElse(""))
                    ).foreach(p => view.findView(p._1).setText(p._2))
                    val itemScanList = view.findView(TR.items)
                    itemScanList.removeAllViews()
                    selectedItems.foreach(_._2.foreach { item =>
                        val scannedItem = inflater.inflate(R.layout.item_list_item_selected_scan, itemScanList, false)
                        scannedItem.findView(TR.time).setText(time.toHumanReadableDate(item.created))
                        itemScanList.addView(scannedItem)
                    })
                }
            }
            val viewTypeMap = Seq(ShortViewTypeHandler, LongViewTypeHandler).map(h => (h.viewType, h)).toMap
            var itemSummaries: List[ItemSummary] = Nil
            var selected: Option[Int] = None
            var selectedVerbose = false
            var selectedItems: Option[(Int, Seq[Item])] = None

            reloadItemList()

            override def getItemCount: Int = itemSummaries.size

            override def onBindViewHolder(vh: ItemViewHolder, idx: Int) {
                val itemSummary = itemSummaries(idx)
                vh.viewTypeHandler.populate(vh.view, itemSummary)
                def selectIdx() {
                    selectedItems = None
                    selected.foreach(notifyItemChanged)
                    selected = Some(idx)
                    selected.foreach(notifyItemChanged)
                }
                vh.view.onLongClick({
                    selectIdx()
                    popupItemMenu(vh, itemSummary)
                })
                vh.view onClick { _: View =>
                    selectIdx()
                    for (productId <- itemSummary.product.id) {
                        storage.getItemsByProductAndGroup(productId, stableValues.selectedItemGroup.flatMap(_.id)).andThen {
                            case Success(items) =>
                                selectedItems = Some((idx, items))
                                notifyItemChanged(idx)
                                ItemAdapter.notifyDataSetChanged()
                            case Failure(e) => handleFailure(e)
                        }
                    }
                }
                vh.view.setSelected(selected.contains(idx))
            }

            override def getItemViewType(idx: Int) = {
                val viewType1 = (if (selectedItems.exists(_._1 == idx)) LongViewTypeHandler else ShortViewTypeHandler).viewType
                viewType1
            }

            override def onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder = {
                val viewTypeHandler = viewTypeMap(viewType)
                val view = inflater.inflate(viewTypeHandler.layout, viewGroup, false)
                new ItemViewHolder(view, viewGroup, viewTypeHandler)
            }
        }

        def popupItemMenu(vh: ItemViewHolder, itemSummary: ItemSummary): Boolean = {
            val popup = new PopupMenu(guiContext, vh.view)
            val inflater = popup.getMenuInflater
            inflater.inflate(R.menu.item_popup, popup.getMenu)
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener {
                override def onMenuItemClick(item: MenuItem) = {
                    item.getItemId match {
                        case R.id.item_popup_rename =>
                            saveProduct(itemSummary.product)
                            true
                        case R.id.item_popup_remove =>
                            removeItem(itemSummary.lastItemId)
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

        def saveProduct(product: Product) {
            dialogs.withField(R.string.productRenameTitle.r2String, product.name, (name, feedback) => {
                if (name.trim.length == 0) {
                    feedback(R.string.fieldRequired.r2String)
                } else {
                    storage.saveProduct(product.copy(name = name)) onComplete {
                        case Success(items) =>
                            WidgetHelpers.toast(R.string.itemProductRenamed.r2String)
                            reloadItemList()
                        case Failure(e) => handleFailure(e)
                    }
                }
            })
        }

        def removeItem(itemId: Int): Unit = {
            storage.removeItem(itemId) onComplete {
                case Success(_) =>
                    reloadItemList()
                    WidgetHelpers.toast(R.string.removedItem)
                case Failure(f) => handleFailure(f)
            }
        }

        class ItemViewHolder(var view: View, val viewGroup: ViewGroup, val viewTypeHandler: ViewTypeHandler) extends RecyclerView.ViewHolder(view)

        def reloadItemList() {
            drawerMenu.reloadItemGroupList()
        }

        def selectItemGroupExplicitly(cancelMessageId: CharSequence, doWithItemGroup: (ItemGroup) => Unit) {
            storage.getItemGroups.map(_.toArray) onComplete {
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

        override def searchItems() {
            dialogs.withField(R.string.searchTitle.r2String, "", (search, _) => {
                storage.searchItems(search).map(_.toList).onComplete {
                    case Success(items) =>
                        stableValues.selectedItemGroup = None
                        changeItemSummaries(R.string.searchTitle.r2String, items)
                    case Failure(e) => handleFailure(e)
                }
            })
        }

     }

}