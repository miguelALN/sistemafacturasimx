package com.bancomext.dao;

import com.bancomext.web.utils.Constantes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {

  private static final Logger log = LogManager.getLogger(DBConnection.class);

  public static Connection getConnFactIMX() {
    return createConnection("jdbcFacturasIMX2CBANK11");
  }

  public static Connection getConnBanks() {
    return createConnection("EstadoCuentaDS");
  }

  public static Connection getConnectionGacd() {
    return createConnection(Constantes.JNDI_GACD);
  }


  private static Connection createConnection(final String dataSource) {
    //log.debug("Usando el pool de Connection : " + dataSource);
    try {
      final InitialContext ic = new InitialContext();
      final DataSource ds = (DataSource) ic.lookup(dataSource);
      return ds.getConnection();
    } catch (NamingException ne) {
      log.error("No se encuentra el dataSource : " + dataSource + " NamingException : " + ne.getMessage(), ne);
    } catch (SQLException sqle) {
      log.error("Error al obtener el Connection : " + dataSource + " SQLException : " + sqle.getMessage(), sqle);
    }
    return null;
  }
}
