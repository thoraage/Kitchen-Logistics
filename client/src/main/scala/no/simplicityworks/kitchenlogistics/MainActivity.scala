package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.view.{Window, Menu, MenuItem}
import org.scaloid.common._

class MainActivity extends SActivity with TypedFindView {

    var app: OperationsModule with StorageModule with ScannerModule with DialogsModule =
        new OperationsImplModule with KitLogRestStorageModule with MockDialogScannerModule with DialogsModule {
            override def guiContext = MainActivity.this

            override def storageConfiguration = new StorageConfiguration {
                override lazy val hostAddress = "http://192.168.0.195:8080"
                override lazy val authenticator = BasicAuthenticator("thoredge", "pass")
            }
        }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        item.getItemId match {
//            case R.id.actionBarSearch =>
//                //search
//                true
            case R.id.action_bar_scan_new =>
                app.operations.scanNewItem()
                true
            case R.id.action_bar_scan_remove =>
                app.operations.scanRemoveItem()
                true
            case R.id.action_bar_new_item_group =>
                app.operations.createNewItemGroup()
                true
            case R.id.action_bar_rename_item_group =>
                app.operations.renameItemGroupName()
                true
            case R.id.action_bar_remove_item_group =>
                app.operations.removeItemGroupName()
                true
            case _ =>
                super.onOptionsItemSelected(item)
        }
    }

    override def onCreate(bundle: Bundle) {
        super.onCreate(bundle)
        requestWindowFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.main)
        app.operations.initiate()
        app.operations.populateDrawerMenu()
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        val inflater = getMenuInflater
        inflater.inflate(R.menu.action_bar, menu)
        app.operations.setMenu(menu)
        super.onCreateOptionsMenu(menu)
    }

}
