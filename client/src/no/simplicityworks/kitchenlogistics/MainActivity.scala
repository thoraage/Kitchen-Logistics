package no.simplicityworks.kitchenlogistics

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.{Menu, MenuItem}
import org.scaloid.common._

class MainActivity extends ActionBarActivity with SActivity with TypedFindView {

    var app: OperationsModule with StorageModule with ScannerModule with DialogsModule =
        new OperationsImplModule with KitLogRestStorageModule with MockDialogScannerModule with DialogsModule {
            override def guiContext = MainActivity.this
        }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
        item.getItemId match {
            case R.id.actionBarSearch =>
                //search
                true
            case R.id.actionBarNew =>
                app.operations.scanNewItem()
                true
            case R.id.actionBarRemove =>
                app.operations.scanRemoveItem()
                true
            case _ =>
                super.onOptionsItemSelected(item)
        }
    }

    override def onCreate(bundle: Bundle) {
        super.onCreate(bundle)
        setContentView(R.layout.main)
        app.operations.initiate()
        val actionBar = getSupportActionBar
        actionBar.setDisplayHomeAsUpEnabled(true)
        app.operations.populateDrawerMenu()
    }

    override def onCreateOptionsMenu(menu: Menu): Boolean = {
        val inflater = getMenuInflater
        inflater.inflate(R.menu.actionbar, menu)
        super.onCreateOptionsMenu(menu)
    }

}
