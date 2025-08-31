package com.bancomext.web.utils;

import com.bancomext.dao.ConsultasDAO;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiLogErrores;
import com.bancomext.service.mapping.VoLlaveValor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.model.SelectItem;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilerias {

  private static final Logger log = LogManager.getLogger(Utilerias.class);

  public static boolean validaNumerico(final String sNumerico) {
    final String numExp = "^[0-9]+$";
    final Pattern pattern = Pattern.compile(numExp);
    final Matcher matcher = pattern.matcher(sNumerico);
    return matcher.matches();
  }

  public static boolean esInvalida(final String cadena) {
    return cadena == null || cadena.trim().isEmpty();
  }

  public static List<SelectItem> creaSelectItem(final List<VoLlaveValor> valores) {
    final List<SelectItem> selectItems = new ArrayList<>();
    selectItems.add(new SelectItem("-1", "Seleccionar"));
    if (valores != null) {
      for (final VoLlaveValor valor : valores) {
        if (valor != null) {
          final String key = (valor.getLlave() != null ? valor.getLlave() : "");
          final String value = (valor.getValor() != null ? valor.getValor() : "");
          selectItems.add(new SelectItem(key, value));
        }
      }
    }
    return selectItems;
  }

  public static String formatearNumero(final BigDecimal cantidad, final String simboloMoneda) {
    if (cantidad == null) {
      return "";
    }
    final DecimalFormat df = new DecimalFormat("###,##0.00##");
    return simboloMoneda + df.format(cantidad.doubleValue());
  }

  public static void guardarMensajeLog(final String pantalla,
                                       final String proceso,
                                       final Exception ex,
                                       final String mensaje,
                                       final UsuarioDTO usuario) {
    final String stackTrace;
    if (ex != null) {
      final StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      stackTrace = sw.getBuffer().toString();
    } else {
      stackTrace = "";
    }

    final FiLogErrores fiLogErrores = new FiLogErrores();
    fiLogErrores.setAdicionadoPor(usuario == null ? "Automatico" : usuario.getUsuario());
    fiLogErrores.setFechaAdicion(new Date());
    fiLogErrores.setFechaCreacion(new Date());
    fiLogErrores.setUsuarioProceso(usuario == null ? "Automatico" : usuario.getUsuario());
    fiLogErrores.setModulo(pantalla);
    fiLogErrores.setProceso(proceso);
    fiLogErrores.setStacktrace(stackTrace.length() > 200 ? stackTrace.substring(0, 199) : stackTrace);
    fiLogErrores.setMensaje(mensaje);

    final BigDecimal id = ConsultasDAO.getMax("FI_LOG_ERRORES", "ID_LOG");
    fiLogErrores.setIdLog((short) (id.intValue() + 1));

    log.error(fiLogErrores.getStacktrace());
    //com.bancomext.service.ServiceLocator.getGenericService().save(fiLogErrores);
  }

  public static List<VoLlaveValor> getComboMesAnio() {

    final List<VoLlaveValor> ret = new ArrayList<>();
    final Map<String, String> meses = new HashMap<>();
    meses.put("01", "ENE");
    meses.put("02", "FEB");
    meses.put("03", "MAR");
    meses.put("04", "ABR");
    meses.put("05", "MAY");
    meses.put("06", "JUN");
    meses.put("07", "JUL");
    meses.put("08", "AGO");
    meses.put("09", "SEP");
    meses.put("10", "OCT");
    meses.put("11", "NOV");
    meses.put("12", "DIC");

    final SimpleDateFormat mesDeFecha = new SimpleDateFormat("MM");
    final SimpleDateFormat anioDeFecha = new SimpleDateFormat("yyyy");

    final Calendar c = Calendar.getInstance();
    c.setTime(new Date());

    for (int i = 1; i < 80; i++) {
      final String m = mesDeFecha.format(c.getTime());
      final String a = anioDeFecha.format(c.getTime());
      ret.add(new VoLlaveValor(m + "-" + a, meses.get(m) + "-" + a));
      c.add(Calendar.MONTH, -1);
    }
    return ret;
  }

  public static String getMesActual() {
    final SimpleDateFormat formatter = new SimpleDateFormat("MM");
    return formatter.format(Calendar.getInstance().getTime());
  }

  public static String getAnioActual() {
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
    return formatter.format(Calendar.getInstance().getTime());
  }


  public StreamedContent downlodaFile (String filePath, String nombreArchivo) throws IOException {

    StreamedContent excelResourceV = null;
    log.info("excelResourceV BEFORE" + excelResourceV);
    final File initialFile = new File(filePath);
    final InputStream targetStream = Files.newInputStream(initialFile.toPath());
    log.info("archivo existe " + initialFile.exists());

    excelResourceV = DefaultStreamedContent.builder()
            .name(nombreArchivo)
            .contentType("application/xls")
            .stream(() -> targetStream)
            .build();

    log.info("excelResourceV AFTER " + excelResourceV);

    return  excelResourceV;
  }

}
