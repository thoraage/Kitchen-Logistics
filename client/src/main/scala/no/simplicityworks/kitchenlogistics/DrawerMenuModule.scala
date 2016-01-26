package no.simplicityworks.kitchenlogistics

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
        def reloadItemGroupList()
    }

}


trait DrawerMenuImplModule extends DrawerMenuModule with DialogsModule with OperationsModule with StableValuesModule {

    override lazy val drawerMenu = new DrawerMenu {

        val allItems = new ItemGroupDrawerMenuChoice(None)

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
                guiContext.futureOnUiThread {
                    itemGroupDrawerMenuChoices = itemGroups.map(itemGroup => new ItemGroupDrawerMenuChoice(Some(itemGroup)))
                    val choices = allItems :: itemGroupDrawerMenuChoices
                    leftDrawer.setAdapter(new ArrayAdapter(guiContext, R.layout.itemgroup_list_itemgroup, choices.asJava))
                }
            }
            future onComplete {
                case Success(_) =>
                    itemGroup.foreach(changeItemGroup)
                    if (itemGroup.isEmpty) allItems.onSelect()
                case Failure(t) =>
                    operations.handleFailure(t)
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
            override def toString = itemGroup.map(_.name).getOrElse(R.string.drawerMenuAll.r2String)

            override def onSelect() {
                stableValues.selectedItemGroup = itemGroup
                updateMenu()
                storage.findItemsByGroup(stableValues.selectedItemGroup).map(_.toList) onComplete {
                    case Success(items) => operations.changeItemSummaries(toString, items)
                    case Failure(e) => operations.handleFailure(e)
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

        override def reloadItemGroupList() {
            new ItemGroupDrawerMenuChoice(stableValues.selectedItemGroup).onSelect()
        }

    }

}
