package no.simplicityworks.kitchenlogistics

import java.text.MessageFormat
import java.util.Date

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view._
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.{PopupMenu, SeekBar}
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

        def changeItemSummaries(title: String, items: List[ItemSummary])
    }

}

trait OperationsImplModule extends OperationsModule with ScannerModule with GuiContextModule with DialogsModule
    with StableValuesModule with DrawerMenuModule with TimeModule with ItemOperationsModule with GeneralOperationsModule {

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
            scanner.startScanner().onComplete {
                case Success(Some(code)) =>
                    storage.findProductByCode(code) onComplete {
                        // TODO case many =>
                        case Success(product #:: _) => itemOperations.createItem(product)
                        case Success(Stream.Empty) => itemOperations.createNewProductItem(code)
                        case Failure(t) => generalOperations.handleFailure(t)
                    }
                case Success(None) =>
                case Failure(e) => generalOperations.handleFailure(e)
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
                            generalOperations.handleFailure(new NotImplementedError(""))
                        case Failure(t) =>
                            generalOperations.handleFailure(t)
                    }
                case Success(None) =>
                case Failure(e) => generalOperations.handleFailure(e)
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
                            generalOperations.notifyUpdated(R.string.createdItemGroup)
                        case Failure(t) =>
                            generalOperations.handleFailure(t)
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
                                generalOperations.notifyUpdated(R.string.renamedItemGroup)
                            case Failure(t) =>
                                generalOperations.handleFailure(t)
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
                            generalOperations.notifyUpdated(R.string.removedItemGroup)
                        case Failure(e) =>
                            generalOperations.handleFailure(e)
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
                        scannedItem.findView(TR.updatedTime).setText(time.toHumanReadableDate(item.updated))
                        val amountBar = scannedItem.findView(TR.amountBar)
                        amountBar.setProgress((item.amount * 1000f).toInt)
                        amountBar.onProgressChanged((_: SeekBar, amount: Int, _: Boolean) => changeAmount(item, amount.toFloat / 1000f))
                        itemScanList.addView(scannedItem)
                    })
                }
            }
            val viewTypeMap = Seq(ShortViewTypeHandler, LongViewTypeHandler).map(h => (h.viewType, h)).toMap
            var itemSummaries: List[ItemSummary] = Nil
            var selected: Option[Int] = None
            var selectedVerbose = false
            var selectedItems: Option[(Int, Seq[Item])] = None

            drawerMenu.reloadItemAndItemGroupList()

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
                        stableValues.selectedItemGroup.map { selectedItemGroup =>
                            storage.getItemsByProductAndGroup(productId, selectedItemGroup.id).map(_.toList)
                        }.getOrElse {
                            // If we're showing recent items, then they will not be bundled. Each product is one item
                            storage.getItem(itemSummary.lastItemId).map(_ :: Nil)
                        } andThen {
                            case Success(items) =>
                                selectedItems = Some((idx, items))
                                notifyItemChanged(idx)
                                ItemAdapter.notifyDataSetChanged()
                            case Failure(e) => generalOperations.handleFailure(e)
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

        def changeAmount(item: Item, amount: Float) {
            storage.saveItem(item.copy(amount = amount)) onComplete {
                case Success(_) => generalOperations.notifyUpdated(R.string.changedItemAmount)
                case Failure(f) => generalOperations.handleFailure(f)
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
                            drawerMenu.selectItemGroupExplicitly(R.string.itemMoveCancelled, { itemGroup =>
                                storage.getItem(itemSummary.lastItemId).map(_.copy(itemGroupId = itemGroup.id.get)).flatMap(storage.saveItem) onComplete {
                                    case Success(_) =>
                                        drawerMenu.reloadItemAndItemGroupList()
                                        generalOperations.notifyUpdated(new MessageFormat(R.string.itemMovedToItemGroup.r2String).format(Array(itemSummary.product.name, itemGroup.name)))
                                    case Failure(f) => generalOperations.handleFailure(f)
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
                            generalOperations.notifyUpdated(R.string.itemProductRenamed)
                            drawerMenu.reloadItemAndItemGroupList()
                        case Failure(e) => generalOperations.handleFailure(e)
                    }
                }
            })
        }

        def removeItem(itemId: Int): Unit = {
            storage.removeItem(itemId) onComplete {
                case Success(_) =>
                    drawerMenu.reloadItemAndItemGroupList()
                    generalOperations.notifyUpdated(R.string.removedItem)
                case Failure(f) => generalOperations.handleFailure(f)
            }
        }

        class ItemViewHolder(var view: View, val viewGroup: ViewGroup, val viewTypeHandler: ViewTypeHandler) extends RecyclerView.ViewHolder(view)

        override def searchItems() {
            dialogs.withField(R.string.searchTitle.r2String, "", (search, _) => {
                storage.searchItems(search).map(_.toList).onComplete {
                    case Success(items) =>
                        stableValues.selectedItemGroup = None
                        changeItemSummaries(R.string.searchTitle.r2String, items)
                    case Failure(e) => generalOperations.handleFailure(e)
                }
            })
        }

     }

}