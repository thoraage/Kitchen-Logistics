package no.simplicityworks.kitchenlogistics

import java.text.MessageFormat
import java.util.{Date, Locale}

import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait ItemOperationsModule extends StorageModule with GuiContextModule with DialogsModule with DrawerMenuModule
    with GeneralOperationsModule {

    def itemOperations = ItemOperations

    object ItemOperations {
        def saveItem(product: Product, itemGroupId: Int) {
            storage.saveItem(Item(None, None, product.id.get, itemGroupId, 1.0f)) onComplete {
                case Success(_) =>
                    guiContext.runOnUiThread(drawerMenu.reloadItemAndItemGroupList())
                case Failure(t) => generalOperations.handleFailure(t)
            }
        }
        def createItem(product: Product) {
            stableValues.selectedItemGroup match {
                case Some(ItemGroup(Some(itemGroupId), _, _, _)) =>
                    saveItem(product, itemGroupId)
                    generalOperations.notifyUpdated(R.string.itemNewCreated)
                case _ =>
                    drawerMenu.selectItemGroupExplicitly(R.string.selectItemGroupCancelled, itemGroup => {
                        itemGroup.id.foreach(saveItem(product, _))
                        drawerMenu.changeItemGroup(itemGroup)
                    })
            }
        }
        def createNewProductItem(code: String): Any = {
            runOnUiThread {
                val title = new MessageFormat(R.string.productNameTitle.r2String).format(Array(code))
                dialogs.withField(title, "", (name, feedback) => {
                    if (name.trim.length == 0) {
                        feedback(R.string.fieldRequired.r2String)
                    } else {
                        storage.saveProduct(Product(None, code, name.trim, Locale.getDefault.getISO3Language, new Date)) onComplete {
                            case Success(product) =>
                                createItem(product)
                                generalOperations.notifyUpdated(R.string.productNewCreated)
                            case Failure(t) =>
                                generalOperations.handleFailure(t)
                        }
                    }
                })
            }
        }

    }

}
