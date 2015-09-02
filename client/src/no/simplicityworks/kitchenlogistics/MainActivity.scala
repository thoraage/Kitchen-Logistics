package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.view.{Window, Menu, MenuItem}
import org.scaloid.common._

class MainActivity extends SActivity with TypedFindView {

    var app: OperationsModule with StorageModule with ScannerModule with DialogsModule =
        new OperationsImplModule with KitLogRestStorageModule with MockDialogScannerModule with DialogsModule {
            override def guiContext = MainActivity.this
        }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        item.getItemId match {
//            case R.id.actionBarSearch =>
//                //search
//                true
            case R.id.actionBarNew =>
                app.operations.scanNewItem()
                true
            case R.id.actionBarRemove =>
                app.operations.scanRemoveItem()
                true
            case R.id.actionBarNewItemGroup =>
                app.operations.createNewItemGroup()
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
        val actionBar = getActionBar
        actionBar.setDisplayHomeAsUpEnabled(true)
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer)
        app.operations.populateDrawerMenu()
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        val inflater = getMenuInflater
        inflater.inflate(R.menu.action_bar, menu)
        super.onCreateOptionsMenu(menu)
    }

}
