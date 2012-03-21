package no.simplicityworks.kitchenlogistics

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.{ContentValues, Context}
import android.database.Cursor

object LocalSQLiteStorage {
  val productFields = Array("id", "code", "name")
}

trait LocalSQLiteStorage extends Storage {
  this: Context =>

  lazy val database = new DatabaseImpl(this);

  protected class DatabaseImpl(context: Context) extends SQLiteOpenHelper(context, "kitchenlogistics", null, 3) with Database {

    override def saveProduct(product: Product) = {
      val values = new ContentValues
      values.put("code", product.code)
      values.put("name", product.name)
      val newId = Some(
        product.id.map {
          id =>
            getWritableDatabase.update("product", values, "id = ?", Array(id.toString))
            id
        }.getOrElse {
          getWritableDatabase.insert("product", null, values)
        })
      product.copy(id = newId)
    }

    override def findProductByCode(code: String) = {
      val cursor = getReadableDatabase.query("product", LocalSQLiteStorage.productFields, "code = ?", Array(code.trim), null, null, null)
      assert(cursor.getCount <= 1, "Expected 0 or 1; found " + cursor.getCount)
      if (cursor.moveToFirst)
        Some(Product(Some(cursor.getLong(0)), cursor.getString(1), cursor.getString(2)))
      else
        None
    }

    override def findProductById(id: Long) = {
      val cursor = getReadableDatabase.query("product", LocalSQLiteStorage.productFields, "id = ?", Array(id.toString.trim), null, null, null)
      assert(cursor.getCount == 1, "Expected 1; found " + cursor.getCount)
      cursor.moveToFirst
      Product(Some(cursor.getLong(0)), cursor.getString(1), cursor.getString(2))
    }

    def saveItem(item: Item): Item = {
      val values = new ContentValues
      values.put("productId", item.productId.toString)
      val newId = Some(
        item.id.map {
          id =>
            getWritableDatabase.update("item", values, "id = ?", Array(id.toString))
            id
        }.getOrElse {
          getWritableDatabase.insert("item", null, values)
        })
      item.copy(id = newId)
    }

    def retrieveAll[T](cursor: Cursor)(f: => T): Seq[T] = {
      var result: List[T] = Nil
      if (cursor.moveToFirst()) {
        do {
          result = f :: result
        } while (cursor.moveToNext())
      }
      result
    }

    def findItems(): Seq[Item] = {
      val cursor = getReadableDatabase.query("item", Array("id", "productId"), null, Array(), null, null, null)
      retrieveAll(cursor) {
        Item(Some(cursor.getLong(0)), cursor.getLong(1))
      }
    }

    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      db.execSQL("""
        drop table product;
      """)
      db.execSQL("""
        drop table item;
      """)
      onCreate(db)
    }

    override def onCreate(db: SQLiteDatabase) {
      db.execSQL("""
        create table product (
          id          integer primary key,
          code        text,
          name        text
        );
      """)
      db.execSQL("""
        create table item (
          id          integer primary key,
          productId   integer
        );
      """)
    }
  }

}