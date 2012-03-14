package no.simplicityworks.kitchenlogistics

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.{ContentValues, Context}

trait LocalSQLiteStorage extends Storage {
  this: Context =>

  lazy val database = new DatabaseImpl(this);

  protected class DatabaseImpl(context: Context) extends SQLiteOpenHelper(context, "kitchenlogistics", null, 1) with Database {

    override def save(product: Product) = {
      val values = new ContentValues
      values.put("identifier", product.identifier)
      values.put("name", product.name)
      val newId = Some(
        product.id.map { id =>
          getWritableDatabase.update("product", values, "id = ?", Array(id.toString))
          id
      }.getOrElse {
        getWritableDatabase.insert("product", null, values)
      })
      product.copy(id = newId)
    }

    override def findByIdentifier(identifier: String) = {
      val cursor = getReadableDatabase.query("product", Array("id", "identifier", "name"), "identifier = ?", Array(identifier.trim), null, null, null)
      assert(cursor.getCount <= 1, "Expected 0 or 1; found " + cursor.getCount)
      if (cursor.moveToFirst)
        Some(Product(Some(cursor.getLong(0)), cursor.getString(1), cursor.getString(2)))
      else
        None
    }

    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      db.execSQL("""
        drop table product;
      """)
    }

    override def onCreate(db: SQLiteDatabase) {
      db.execSQL("""
        create table product (
          id          integer primary key,
          identifier  text,
          name        text
        );
      """)
    }
  }

}