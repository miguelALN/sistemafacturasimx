package com.bancomext.gacd;

import com.bancomext.dao.DBConnection;
import com.bancomext.web.utils.Constantes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DespachadorDAO {

  private static final Logger log = LogManager.getLogger(DespachadorDAO.class);

  public static String getRutaCfdi(final String serie, final String folio, final String uuid) {
    String ruta = null;
    final String query;
    if (uuid == null || uuid.isEmpty() || uuid.equals("NO FISCAL")) {
      query = "SELECT ICG_NOMBRE_TXT, ICG_ANIO, ICG_MES, ICG_ARCHIVO FROM TBL_INFO_CFDI_GACD WHERE ICG_FOLIO = " +
          folio + ((serie != null && !serie.trim().isEmpty()) ? " AND ICG_SERIE = '" + serie + "'" : "");
    } else {
      query = "SELECT ICG_NOMBRE_TXT, ICG_ANIO, ICG_MES, ICG_ARCHIVO FROM TBL_INFO_CFDI_GACD " +
          " WHERE ICG_UUID = '" + uuid.trim() + "' " +
          "    OR ICG_UUID = '" + uuid.trim().toUpperCase() + "'" +
          "    OR ICG_UUID = '" + uuid.trim().toLowerCase() + "'";
    }
    try (final Connection conn = DBConnection.getConnectionGacd();
         final PreparedStatement stm = conn.prepareStatement(query);
         final ResultSet rs = stm.executeQuery()) {
      if (rs != null) {
        if (rs.next()) {
          final String archivo = rs.getString("ICG_ARCHIVO");
          if (archivo != null && !archivo.equalsIgnoreCase("null")) {
            final String nombreTxt = rs.getString("ICG_NOMBRE_TXT");
            final int anio = rs.getInt("ICG_ANIO");
            final String mes = rs.getString("ICG_MES");
            ruta = Constantes.FS_REPO_CFDI + "/" + anio + "/" + mes + "/" + archivo;
          }
        }
      }
    } catch (SQLException sqle) {
      log.error("SQLException : " + sqle.getMessage(), sqle);
    }
    return ruta;
  }

}
