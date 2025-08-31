package com.bancomext.web.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class LecturaArchivosHelper {

  private static final Logger log = LogManager.getLogger(LecturaArchivosHelper.class);

  public static void descargarExcel(final String filePath, final String nombreArchivo) {
    final File fileGenerado = new File(filePath + nombreArchivo + ".xls");
    try (final InputStream is = Files.newInputStream(fileGenerado.toPath())) {
      byte[] bytes = toByteArray(is);
      descargarArchivo(bytes, nombreArchivo, "xls",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
  }

  public static void descargarTxt(final String filePath, final String nombreArchivo) {
    final File fileGenerado = new File(filePath + nombreArchivo + ".txt");
    try (final InputStream is = Files.newInputStream(fileGenerado.toPath())) {
      byte[] bytes = toByteArray(is);
      descargarArchivo(bytes, nombreArchivo, "txt", "aplication/txt");
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
  }

  private static byte[] toByteArray(final InputStream is) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] array = new byte[4];
    int i;
    while ((i = is.read(array, 0, array.length)) != -1) {
      output.write(array, 0, i);
    }
    return output.toByteArray();
  }

  private static void descargarArchivo(final byte[] bytes,
                                       final String nombre,
                                       final String extension,
                                       final String contentType) {
    final HttpServletResponse response =
        (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
    response.reset();
    response.resetBuffer();
    response.setContentType(contentType);
    response.addHeader("Content-disposition", "attachment; filename=\"" + nombre + "." + extension + "\"");

    try (final ServletOutputStream outputStream = response.getOutputStream()) {
      outputStream.write(bytes);
      outputStream.flush();
      outputStream.close();
      FacesContext.getCurrentInstance().responseComplete();
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
  }

}
