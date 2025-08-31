package com.bancomext.web.beans.despachador;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.*;

@Data
@ManagedBean(name = "despachador")
@ViewScoped
public class Despachador implements Serializable {
  private static final Logger log = LogManager.getLogger(Despachador.class);

  private String cadenita;

  public Despachador() {


    String cadenita = leerArchivo("correo_ProcesoGeneracionFactoraje.html");

    final String piepagina = "<br><table style='font: bold 11px Verdana, Arial, Helvetica, sans-serif;" +
        " background: #00928F;text-shadow: 0.1em 0.1em #000;color: #FFFFFF;width: 600px;" +
        "height:100px;'><tr><td><center>LA BANCA ELECTR&#211;NICA DE BANCOMEXT<br>La seguridad y rapidez " +
        "que necesitas.</center></td></tr></table>";


    cadenita = cadenita.replace("<GeneradoPor>", "    Proceso generado por: " + "restrada");
    cadenita = cadenita.replace("<FechasProeso>", "5 de Mayo, 1883");
    cadenita = cadenita.replace("<piepagina>", piepagina);

    log.info("cadenita=" + cadenita);
  }

  private static String leerArchivo(final String archivo) {
    final StringBuilder sb = new StringBuilder();
    try (final InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(archivo)) {
      if (is != null) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        reader.lines().forEach(sb::append);
      }
    } catch (IOException ioe) {
      log.error(ioe.getMessage());
    }
    return sb.toString();
  }

}


