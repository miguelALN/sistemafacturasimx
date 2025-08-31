package com.bancomext.web.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeneradorZip {

  private static final Logger log = LogManager.getLogger(GeneradorZip.class);

  public static void generarArchivoZip() {
    final List<String> listXml = listaArchivosEnIn(".xml");
    final List<String> listPdf = listaArchivosEnIn(".pdf");

    if (!listXml.isEmpty() && !listPdf.isEmpty()) {
      for (final String nombrePdf : listPdf) {
        for (final String nombreXml : listXml) {
          final String archivoXmlSinExt = nombreXml.replaceAll("\\.\\w+$", "");
          final String archivoPdfSinExt = nombrePdf.replaceAll("\\.\\w+$", "");
          if (archivoPdfSinExt.equals(archivoXmlSinExt)) {
            final String nombreSinExt =
                nombrePdf.replaceAll(".pdf", "").replaceAll(".PDF", "");
            try (final FileOutputStream fos = new FileOutputStream("IMX/ZIP/" + nombreSinExt + ".zip");
                 final ZipOutputStream zos = new ZipOutputStream(fos)) {
              agregarArchivoAZip(nombrePdf, zos);
              agregarArchivoAZip(nombreXml, zos);
              zos.closeEntry();
            } catch (Exception ex) {
              log.error("Eroor al generar los archivos ZIP...." + ex.getMessage(), ex);
            }
            break;
          }
        }
      }
    }
  }

  private static List<String> listaArchivosEnIn(final String ext) {
    final File[] files = new File("IMX/IN/").listFiles((dir1, name) -> name.toLowerCase().endsWith(ext));
    if (files != null) {
      return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    } else {
      return new ArrayList<>();
    }
  }

  private static void agregarArchivoAZip(final String nombreArchivo, final ZipOutputStream zos) {
    final ZipEntry ze = new ZipEntry(nombreArchivo);
    try (final FileInputStream in = new FileInputStream("IMX/IN/" + nombreArchivo)) {
      zos.putNextEntry(ze);
      int len;
      final byte[] buffer = new byte[1024];
      while ((len = in.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      log.debug("Agregado:" + nombreArchivo);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

}
