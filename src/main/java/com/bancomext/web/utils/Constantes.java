package com.bancomext.web.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class Constantes {

  private static final Logger log = LogManager.getLogger(Constantes.class);

  public static final boolean PRODUCCION;
  public static final int REGISTROS = 9;

  public static final String URL_LDAP;
  public static final String PORT_LDAP;
  public static final String HOST_SFTP;
  public static final String USER_SFTP;
  public static final String PASWORD_SFTP;
  public static final String PORT_SFTP;
  public static final String SW_HOST_SFTP;
  public static final String SW_USER_SFTP;
  public static final String SW_RUTA_PPK;
  public static final int SW_PORT_SFTP;
  public static final String AMBIENTE;
  public static final String USER_FTP_INTERNO;
  public static final String PASS_FTP_INTERNO;
  public static final String HOST_FTP_INTERNO;
  public static final String PORT_FTP_INTERNO;
  public static final String MAIL_JNDI;
  public static final String MAIL_FACTURAS;
  public static final String MAIL_FACTURAS_SUCRE;
  public static final String MAIL_CARTAS;
  public static final String MAIL_PRIME;
  public static final String MAIL_DESARROLLO;
  public static final String FS_REPO_CFDI;
  public static final String JNDI_GACD;
  // GRUPOS CORREO

  static {
    log.debug("*********************************************************");
    log.debug("*************** CARGANDO CONFIGURACION  *****************");
    log.debug("*********************************************************");

    final Properties generales = inicializar("ConfiguracionGeneral.properties");

    PRODUCCION = true;

    URL_LDAP = getProperty(generales, "ldap_");
    PORT_LDAP = getProperty(generales, "ldap_port");

    HOST_SFTP = getProperty(generales, "ftp_host");
    USER_SFTP = getProperty(generales, "ftp_user");
    PASWORD_SFTP = getProperty(generales, "ftp_password");
    PORT_SFTP = getProperty(generales, "ftp_port");

    SW_HOST_SFTP = getProperty(generales, "sw_ftp_host");
    SW_USER_SFTP = getProperty(generales, "sw_ftp_user");
    SW_PORT_SFTP = parseaEntero(getProperty(generales, "sw_ftp_port"));
    SW_RUTA_PPK = getProperty(generales, "sw_ruta_ppk");

    FS_REPO_CFDI = getProperty(generales, "fs_repo_cfdi");
    JNDI_GACD = getProperty(generales, "jndi_Gacd");

    final Properties props = inicializar("sistemaFacturasiMX.properties");

    AMBIENTE = getProperty(props, "Ambiente");
    USER_FTP_INTERNO = getProperty(props, "userFTP");
    PASS_FTP_INTERNO = getProperty(props, "passFTP");
    HOST_FTP_INTERNO = getProperty(props, "hostFTP");
    PORT_FTP_INTERNO = getProperty(props, "portFTP");

    MAIL_JNDI = getProperty(props, "mailjndi");
    MAIL_FACTURAS = getProperty(props, "mailFacturas");
    MAIL_FACTURAS_SUCRE = getProperty(props, "mailFacturasSucre");
    MAIL_CARTAS = getProperty(props, "mailCartasCredito");
    MAIL_PRIME = getProperty(props, "mailFacturasPrimeCom");
    MAIL_DESARROLLO = getProperty(props, "correoDesarrollo");

    log.debug("*********************************************************");
    log.debug("*************** FIN CONFIGURACION  *****************");
    log.debug("*********************************************************");
  }

  private static Properties inicializar(final String propertiesFile) {
    log.debug("***********************************************");
    log.debug("De la Configuracion Path : " + getPath());
    log.debug("***********************************************");
    return obtieneProperties(propertiesFile);
  }

  private static Properties obtieneProperties(final String propertiesFile) {
    final Properties prop = new Properties();
    final String s1 = getPath() + "/" + propertiesFile;
    try (final BufferedReader bufferedreader = new BufferedReader(new FileReader(s1))) {
      prop.load(bufferedreader);
    } catch (FileNotFoundException fne) {
      log.error("EL ARCHIVO PROPERTIES " + propertiesFile + " NO EXISTE", fne);
    } catch (IOException ioe) {
      log.error("NO SE PUEDE CARGAR EL ARCHIVO PROPERTIES " + propertiesFile + " NO EXISTE", ioe);
    }
    return prop;
  }

  private static String getPath() {
    String s = null;
    final File file = new File(".");
    try {
      s = file.getCanonicalPath();
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
    return s;
  }

  private static String getProperty(final Properties prop, final String value) {
    final String aux = prop.getProperty(value);
    if (aux != null) {
      if (!value.contains("password")) {
        log.debug(value + " : {" + aux + "}");
      }
    } else {
      log.error("No se encontro el valor : {" + value + "}");
    }
    return aux;
  }

  private static int parseaEntero(final String property) {
    if (property != null) {
      try {
        return Integer.parseInt(property);
      } catch (NumberFormatException ex) {
        return 22;
      }
    } else {
      return 22;
    }
  }
}
