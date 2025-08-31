package com.bancomext.web.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class UtileriaConexion {

  private static final Logger log = LogManager.getLogger(UtileriaConexion.class);
  private Session session = null;
  private Channel channel = null;


  // METODO QUE REALIZA LA CONEXION
  private static Session getSession(final JSch jsch) {
    log.info("Iniciando Conexion....");

    Session session = null;
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    try {
      session = jsch.getSession(Constantes.USER_SFTP, Constantes.HOST_SFTP, Integer.parseInt(Constantes.PORT_SFTP));
      session.setPassword(Constantes.PASWORD_SFTP);

      session.setConfig(config);
      log.info("Conexion exitosa!!");
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return session;
  }

  // METODO PARA EXTRAER LOS ARCHIVOS DEL SERVIDOR SFTP DE ADVANTAGE
  public static void extractFilesSFTP(final Vector<ChannelSftp.LsEntry> listEntry,
                                      final List<String> listFolios,
                                      final ChannelSftp channelSftp) {
    log.info("Transfiriendo Archivos...");
    int cont = 0;
    for (String folioHistorico : listFolios) {
      if (!listEntry.isEmpty()) {
        for (ChannelSftp.LsEntry lsEntry : listEntry) {
          final String folio = lsEntry.getFilename().split("_")[2].trim();
          if (folio.equals(folioHistorico)) {
            try {
              channelSftp.get(lsEntry.getFilename(), "IN/");
              cont++;
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
          }
        }
      } else {
        log.info("No es encontraron los archivos PDF - XML en ReachCore.!");
      }
    }
    log.info("Archivos extraidos : " + cont);
    listEntry.clear();
    log.info("Terminado...");
  }

  public static void downloadRep(final List<String> listaFolios) {
    log.info("----------> Inicia descarga de archivos a directorio IN");
    String remoteFile;
    final FTPClient ftpClient = getConexionRepositorio();
    List<FTPFile> listFiles = new ArrayList<>();
    try {
      listFiles = Arrays.asList(ftpClient.listFiles());
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    int cont = 0;
    for (final FTPFile reg : listFiles) {
      for (final String regFol : listaFolios) {
        final String folio = reg.getName().split("_")[2].trim();
        if (folio.equals(regFol)) {
          remoteFile = reg.getName();
          final File downloadedFile = new File("IN/" + reg.getName());
          try (final OutputStream os = new BufferedOutputStream(Files.newOutputStream(downloadedFile.toPath()))) {
            boolean success = ftpClient.retrieveFile(remoteFile, os);
            if (success) {
              cont++;
              log.info("El archivo #" + cont + " ha sido descargado exitosamente.");
            } else {
              log.info("El archivo #" + cont + " NO ha sido descargado");
            }
          } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
          }
        }
        break;
      }
    }
    log.info("Archivos descargados: " + cont);
    try {
      if (ftpClient.isConnected()) {
        ftpClient.logout();
        ftpClient.disconnect();
      }
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  private static FTPClient getConexionRepositorio() {
    final FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(Constantes.HOST_FTP_INTERNO, Integer.parseInt(Constantes.PORT_FTP_INTERNO));
      ftpClient.login(Constantes.USER_FTP_INTERNO, Constantes.PASS_FTP_INTERNO);
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      log.info("Conexion Exitosa FTP");
    } catch (Exception ex) {
      log.error("Surgio un error al realizar la conexion al FTP (Repositorio) " + ex.getMessage(), ex);
    }
    return ftpClient;
  }

  // METODO QUE OBTIENE LA CONEXION
  public ChannelSftp getConnection() {
    ChannelSftp channelSftp = null;
    try {
      final JSch jsch = new JSch();
      session = getSession(jsch);
      session.connect(1200000);
      channel = session.openChannel("sftp");
      channel.connect();
      channelSftp = (ChannelSftp) channel;

      // DESARROLLO
      // channelSftp.cd("OUT");

      // PRODUCCION
      channelSftp.cd("outbox");

      log.info("Conexion Exitosa a Directorio.");
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return channelSftp;
  }

  public void closeConnection(final ChannelSftp channelSftp) {
    try {
      channelSftp.disconnect();
      channel.disconnect();
      session.disconnect();
      log.info("Cierre conexion exitoso....");
    } catch (Exception e) {
      e.getStackTrace();
      log.error(e.getMessage());
    }
  }

  public void uploadRep() {
    log.info("----------> Inicia Transferencia a Repositorio Bancomext");
    String remoteFile;
    int cont = 0;
    int size;
    InputStream inputStream = null;
    final FTPClient ftpClient = getConexionRepositorio();
    try {
      File file = new File("IMX/IN/");
      File[] listFiles = file.listFiles();
      if (listFiles != null) {
        size = listFiles.length;
        for (File reg : listFiles) {
          File localFile = new File("IMX/IN/" + reg.getName());
          remoteFile = reg.getName();
          inputStream = Files.newInputStream(localFile.toPath());
          log.info("Start uploading file...");
          boolean done = ftpClient.storeFile(remoteFile, inputStream);

          if (done) {
            cont++;
            log.info("Archivos transferidos satisfactoriamente : " + cont + " de : " + size);
          }
        }
      }
    } catch (Exception e) {
      log.error("Surigo un error a la hora de transferir el archivo." + e.getMessage(), e);
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
        if (ftpClient.isConnected()) {
          ftpClient.logout();
          ftpClient.disconnect();
        }
      } catch (IOException ex) {
        log.error(ex.getMessage(), ex);
      }
    }
  }
}
