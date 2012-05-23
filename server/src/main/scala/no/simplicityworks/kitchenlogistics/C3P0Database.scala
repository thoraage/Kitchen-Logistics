package no.simplicityworks.kitchenlogistics

import org.scalaquery.ql.basic.{BasicTable => BTable}

/*object LiveDataProvider {
  private[LiveDataProvider] lazy val db = new C3P0Database
  //List all available topics in the system 
  lazy val animalsQ = for {a <- Animals if a.dangerous === false} yield
    t
}

class LiveDataProvider extends BrainDataProvider with WithImpls {
  def getDangerourAnimals(): List[Animal] = {
    LiveDataProvider.db.withSession {
      animalsQ.list(topic.pk).head
    }
  }
}

import com.senti.db.AbstractDBConn
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.scalaquery.session.Database

class CCDBConn extends AbstractDBConn {
  def getPool(): ComboPooledDataSource = {
    import com.senti.conf.Configuration
    try {
      val pool = new ComboPooledDataSource
      pool setDriverClass classOf[com.mysql.jdbc.Driver].getName
      // load the jdbc driver 
      val conf = Configuration getInstance "connection.properties"
      pool setJdbcUrl ("jdbc:mysql://" + conf
        .getString(".host") + "/"
        + conf.getString("schema"))
      pool setUser (conf.getString("username"))
      pool setPassword (conf.getString("password"))
      // the settings below are optional -- c3p0 can work with 
      defaults
      pool setMinPoolSize 1
      pool setAcquireIncrement 1;
      pool setMaxPoolSize 3;
      pool.getProperties.put("utf8", "true");
      pool
    }
    catch {
      case e: InstantiationException => {
        println(e);
        throw new RuntimeException(e)
      }
      case unknown => {
        println("unhandled exception");
        throw new RuntimeException(unknown)
      }
    }
  }
}
  */

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.scalaquery.session.Database
import java.sql.Connection

class C3P0Database(val url: String) extends Database {
  val driver = org.scalaquery.ql.extended.H2Driver

  val cpds: ComboPooledDataSource = new ComboPooledDataSource
  cpds.setDriverClass(classOf[org.h2.Driver].getName)
  cpds.setJdbcUrl(url);
  //cpds.setUser("swaldman");
  //cpds.setPassword("test-password");

  // the settings below are optional -- c3p0 can work with defaults
  cpds.setMinPoolSize(1);
  cpds.setAcquireIncrement(1);
  cpds.setMaxPoolSize(1);

  override def createConnection(): Connection = cpds.getConnection
}
