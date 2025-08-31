package com.bancomext.gacd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DespachadorService {

  private static final Logger log = LogManager.getLogger(DespachadorService.class);

  public static byte[] obtenerCfdi(final String serie, final String folio, final String uuid, final String formato) {
    final String ruta = DespachadorDAO.getRutaCfdi(serie, folio, uuid);
    byte[] file = new byte[0];
    if (ruta == null) {
      return file;
    }
    final String dest = ruta + "." + formato.toLowerCase();
    final Path filePath = Paths.get(dest);
    try {
      file = Files.readAllBytes(filePath);
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
    return file;
  }

}
