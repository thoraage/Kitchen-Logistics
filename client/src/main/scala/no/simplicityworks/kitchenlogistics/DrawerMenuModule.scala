package no.simplicityworks.kitchenlogistics

import android.content.DialogInterface
import android.view.{Menu, View}
import android.widget.{AdapterView, ArrayAdapter}
import org.scaloid.common._

import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait DrawerMenuModule extends StorageModule {

    def drawerMenu: DrawerMenu

    trait DrawerMenu {
        def initiate()
        def populateDrawerMenu(itemGroup: Option[ItemGroup])
        def changeItemGroup(itemGroup: ItemGroup)
        def setMenu(menu: Menu)
        def reloadItemAndItemGroupList()
        def selectItemGroupExplicitly(cancelMessageId: CharSequence, doWithItemGroup: (ItemGroup) => Unit)
    }

}


trait DrawerMenuImplModule extends DrawerMenuModule with DialogsModule with OperationsModule with StableValuesModule
    with GeneralOperationsModule {

    override lazy val drawerMenu = new DrawerMenu {

        val recentItems = new ItemGroupDrawerMenuChoice(None)

        var itemGroupDrawerMenuChoices: List[ItemGroupDrawerMenuChoice] = Nil

        val leftDrawer = guiContext.findView(TR.left_drawer)

        var menu: Option[Menu] = None

        override def initiate() {
            menu = None
            leftDrawer.onItemClick { (_: AdapterView[_], _: View, position: Int, _: Long) =>
                val choice = leftDrawer.getAdapter.getItem(position).asInstanceOf[DrawerMenuChoice]
                guiContext.findView(TR.drawer_layout).closeDrawer(guiContext.findView(TR.left_drawer))
                choice.onSelect()
            }
            populateDrawerMenu(stableValues.selectedItemGroup)
        }

        override def populateDrawerMenu(itemGroup: Option[ItemGroup]) {
            val future = storage.getItemGroups.map(_.toList).flatMap { itemGroups =>
                stableValues.itemGroups = itemGroups
                guiContext.futureOnUiThread {
                    itemGroupDrawerMenuChoices = itemGroups.map(itemGroup => new ItemGroupDrawerMenuChoice(Some(itemGroup)))
                    val choices = recentItems :: itemGroupDrawerMenuChoices
                    leftDrawer.setAdapter(new ArrayAdapter(guiContext, R.layout.itemgroup_list_itemgroup, choices.asJava))
                }
            }
            future onComplete {
                case Success(_) =>
                    itemGroup.foreach(changeItemGroup)
                    if (itemGroup.isEmpty) recentItems.onSelect()
                case Failure(t) =>
                    generalOperations.handleFailure(t)
            }
        }

        trait DrawerMenuChoice {
            def onSelect(): Unit
        }

        override def changeItemGroup(itemGroup: ItemGroup) {
            val choice = itemGroupDrawerMenuChoices.find(_.itemGroup.exists(_.id == itemGroup.id))
            choice.foreach(_.onSelect())
        }

        class ItemGroupDrawerMenuChoice(val itemGroup: Option[ItemGroup]) extends DrawerMenuChoice {
            override def toString = itemGroup.map(_.name).getOrElse(R.string.drawerMenuRecent.r2String)

            override def onSelect() {
                stableValues.selectedItemGroup = itemGroup
                updateMenu()
                stableValues.selectedItemGroup.map(ig => storage.findItemsByGroup(Some(ig))).getOrElse(storage.getRecentItems(10)).map(_.toList) onComplete {
                    case Success(items) => operations.changeItemSummaries(toString, items)
                    case Failure(e) => generalOperations.handleFailure(e)
                }
            }
        }

        override def setMenu(menu: Menu) {
            this.menu = Some(menu)
            updateMenu()
        }

        def updateMenu() {
            for {
                menu <- menu.toSeq
                item <- Seq(R.id.action_bar_rename_item_group, R.id.action_bar_remove_item_group)
            } menu.findItem(item).setVisible(stableValues.selectedItemGroup.nonEmpty)
        }

        override def reloadItemAndItemGroupList() {
            new ItemGroupDrawerMenuChoice(stableValues.selectedItemGroup).onSelect()
        }

        override def selectItemGroupExplicitly(cancelMessageId: CharSequence, doWithItemGroup: (ItemGroup) => Unit) {
            storage.getItemGroups.map(_.toArray) onComplete {
                case Success(itemGroups) =>
                    val builder = new AlertDialogBuilder(R.string.selectItemGroupTitle, null).negativeButton(R.string.inputDialogCancel, (dialog, _) => {
                        generalOperations.notifyUpdated(cancelMessageId)
                        dialog.cancel()
                    })
                    builder.setItems(itemGroups.map(_.name.asInstanceOf[CharSequence]), new DialogInterface.OnClickListener {
                        override def onClick(dialog: DialogInterface, which: Int) {
                            doWithItemGroup(itemGroups(which))
                        }
                    })
                    builder.show()
                case Failure(t) => generalOperations.handleFailure(t)
            }
        }

    }

}
