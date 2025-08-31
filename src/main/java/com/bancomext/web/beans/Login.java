package com.bancomext.web.beans;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ProcedimientosService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiAccesos;
import com.bancomext.service.mapping.FiCatRoles;
import com.bancomext.service.mapping.FiParamRoles;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.UtileriaCorreo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
@ManagedBean(name = "login")
@SessionScoped
public class Login implements Serializable {

  private static final Logger log = LogManager.getLogger(Login.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private final String ID_SISTEMA_FACTURAS = "SCFIMX";
  private String nombre = "";
  private UIComponent loginButton;
  private String strUsuario;
  private String clave = ""; //password
  private String rol;
  //MENU
  private boolean ADMINMENU = false;

  private boolean USUARIOS = false;
  private boolean CATROLES = false;
  private boolean ROLESMODULOS = false;

  private boolean CATPARAMGENERALES = false;
  private boolean CATCORREOREPORTES = false;
  private boolean CATCORREOCLIENTES = false;
  private boolean CATCUENTASCOMPARATIVO = false;

  private boolean PROCESOFACTORAJE = false;
  private boolean PROCESOCANCELACION = false;
  private boolean PROCESORESUMENFACTURACION = false;
  private boolean PROCESORESUMENFACTURACIONPRIMECOM = false;
  private boolean CARGAFACTURAS = false;
  private boolean CATCUENTASGENERACION = false;

  private boolean OPERACION = false;
  private boolean FOLIOSFISCALES = false;
  private boolean VERIFICAPROCESOCFDI = false;
  private boolean HISTORICOARCHIVOSCFD = false;
  private boolean FACTURASYEDOSDECTA = false;
  private boolean CIFRASTOTALES = false;
  private boolean CLIENTESCARTERAVENCIDA = false;

  private boolean REPORTES = false;
  private boolean REPORTECOMPARATIVO = false;
  private boolean REPORTESALDOSFIU = false;
  private boolean REPORTEBITAERR = false;

  // PARAMETRIZACION
  private boolean PARAMEJECUCIONPROCESO = false;
  private boolean PARAMECODIGOOPERACIONCTA = false;
  private boolean PARAMECOFACTOR = false;

  // AGRUPACION POLIZAS
  private boolean AGRUPACIONPOLIZAS = false;
  private boolean AGRUPACIONPOLIZASEXTEMPORANEO = false;
  private boolean BITACORAAGRUPACIONPOLIZAS = false;

  // PROCESOS MANUALES
  private boolean PROCESOMANCANCELACIONPROVINTERESES = false;
  private boolean PROCESOMANCAPITALIZACIONINTERESES = false;
  private boolean PROCESOMANRECLASIFICARSALDONEGATIVO = false;
  private boolean PROCESOMANRESTAURARSALDONEGATIVO = false;
  private boolean PROCESOMANCLASIFICACIONDECOFACTOR = false;

  // REPORTES(BITACORAS)
  private boolean BITACORACANCELACIONPROVINTERESES = false;
  private boolean BITACORACAPITALIZACIONINTERESES = false;
  private boolean BITACORARECLASIFICARSALDONEGATIVO = false;
  private boolean BITACORACLASIFICACIONDECOFACTOR = false;
  private boolean BITACORARESTAURARSALDONEGATIVO = false;

  // PROCESO AUTOMATICO
  private boolean PROCESOAUTOMATICO = false;

  // PRIMER DIA HABIL
  private boolean PRIMERDIAHABIL = false;

  // PRUEBAS SFTP
  private boolean PRUEBASFTP = false;

  // MENU REPORTES
  // * RESUMEN DE FACTURACION POR CLIENTE
  // * BITACORA DE ACCIONES
  private boolean RESUMENFACTURACIONPORCLIENTE = false;
  private boolean BITACORADEACCIONES = false;

  private static void enviarCorreo(final List<FiAccesos> listaCorreos, final String contenido) {
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        try {
          if ("Desarrollo".equals(Constantes.AMBIENTE)) {
            to[i] = new InternetAddress(Constantes.MAIL_DESARROLLO);
          } else {
            to[i] = new InternetAddress(listaCorreos.get(i).getEmail());
            log.info("Correo: " + listaCorreos.get(i).getEmail());
          }
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
      UtileriaCorreo.enviarCorreo(to, null, "Acceso denegado de Usuario Facturas IMX", contenido,
          "FACTORAJE", null);
    }
  }

  public void generaAccesos(final FiCatRoles rol) {

    System.out.println("INTO LOGIN generaAccesos");
    @SuppressWarnings("unchecked") final List<FiParamRoles> roles = (List<FiParamRoles>)
        genericService.get("FiParamRoles", " id.idRol =" + rol.getIdRol(), "");

    for (FiParamRoles accMenu : roles) {

      final String desc = accMenu.getFiModulosMenu().getDescripcion();

      if (desc.equals("Usuarios")) {
        USUARIOS = true;
        ADMINMENU = true;
      }
      if (desc.equals("CatRoles")) {
        CATROLES = true;
        ADMINMENU = true;
      }
      if (desc.equals("RolesModulos")) {
        ROLESMODULOS = true;
        ADMINMENU = true;
      }
      if (desc.equals("CatParamGenerales")) {
        CATPARAMGENERALES = true;
        ADMINMENU = true;
      }
      if (desc.equals("CatCorreoReportes")) {
        CATCORREOREPORTES = true;
        ADMINMENU = true;
      }
      if (desc.equals("CatCorreoClientes")) {
        CATCORREOCLIENTES = true;
        ADMINMENU = true;
      }
      if (desc.equals("ConfigCuentasSicav")) {
        CATCUENTASCOMPARATIVO = true;
        ADMINMENU = true;
      }
      if (desc.equals("ConfigCuentasGeneracion")) {
        CATCUENTASGENERACION = true;
        ADMINMENU = true;
      }
      if (desc.equals("ProcesoFactoraje")) {
        PROCESOFACTORAJE = true;
        OPERACION = true;
      }
      if (desc.equals("FoliosFiscales")) {
        FOLIOSFISCALES = true;
        OPERACION = true;
      }
      if (desc.equals("CargaFacturas")) {
        CARGAFACTURAS = true;
        OPERACION = true;
      }
      if (desc.equals("HistoricoArchivosCFD")) {
        HISTORICOARCHIVOSCFD = true;
        OPERACION = true;
      }
      if (desc.equals("ProcesoCancelacionManual")) {
        PROCESOCANCELACION = true;
        OPERACION = true;
      }
      if (desc.equals("ProcesoResumenFacturacion")) {
        PROCESORESUMENFACTURACION = true;
        OPERACION = true;
      }
      if (desc.equals("ResumenFacturacionPorClientePrimCom")) {
        PROCESORESUMENFACTURACIONPRIMECOM = true;
        OPERACION = true;
      }
      if (desc.equals("FacturasyEdosdeCta")) {
        FACTURASYEDOSDECTA = true;
        REPORTES = true;
      }
      if (desc.equals("VerificaProcesosCFDI")) {
        VERIFICAPROCESOCFDI = true;
        REPORTES = true;
      }
      if (desc.equals("CifrasTotales")) {
        CIFRASTOTALES = true;
        REPORTES = true;
      }
      if (desc.equals("ClientesCarteraVencida")) {
        CLIENTESCARTERAVENCIDA = true;
        REPORTES = true;
      }
      if (desc.equals("ReporteComparativo")) {
        REPORTECOMPARATIVO = true;
        REPORTES = true;
      }
      if (desc.equals("ReporteSaldosFIU")) {
        REPORTESALDOSFIU = true;
        REPORTES = true;
      }
      if (desc.equals("BitacoraErrores")) {
        REPORTEBITAERR = true;
        REPORTES = true;
      }

      // INICIA PARAMETRIZACION
      if (desc.equals("ParameEjecucionProceso")) {
        PARAMEJECUCIONPROCESO = true;
      }
      if (desc.equals("ParameCodigoOperacionCta")) {
        PARAMECODIGOOPERACIONCTA = true;
      }
      if (desc.equals("ParameCofactor")) {
        PARAMECOFACTOR = true;
      }

      // INICIA AGRUPACION DE POLIZAS
      if (desc.equals("AgrupacionPolizas")) {
        AGRUPACIONPOLIZAS = true;
      }
      if (desc.equals("AgrupacionPolizasExtemporaneo")) {
        AGRUPACIONPOLIZASEXTEMPORANEO = true;
      }
      if (desc.equals("BitacoraAgrupacionPolizas")) {
        BITACORAAGRUPACIONPOLIZAS = true;
      }

      // INICIA PROCESOS MANUALES

      if (desc.equals("ProcesoManCancelacionProvIntereses")) {
        PROCESOMANCANCELACIONPROVINTERESES = true;
      }
      if (desc.equals("ProcesoManCapitalizacionIntereses")) {
        PROCESOMANCAPITALIZACIONINTERESES = true;
      }
      if (desc.equals("ProcesoManReclasificarSaldoNegativo")) {
        PROCESOMANRECLASIFICARSALDONEGATIVO = true;
      }
      if (desc.equals("ProcesoManRestaurarSaldoNegativo")) {
        PROCESOMANRESTAURARSALDONEGATIVO = true;
      }
      if (desc.equals("ProcesoManClasificacionDeCofactor")) {
        PROCESOMANCLASIFICACIONDECOFACTOR = true;
      }

      // INICIA BITACORA
      if (desc.equals("BitacoraCancelacionProvIntereses")) {
        BITACORACANCELACIONPROVINTERESES = true;
      }
      if (desc.equals("BitacoraCapitalizacionIntereses")) {
        BITACORACAPITALIZACIONINTERESES = true;
      }
      if (desc.equals("BitacoraReclasificarSaldoNegativo")) {
        BITACORARECLASIFICARSALDONEGATIVO = true;
      }
      if (desc.equals("BitacoraRestaurarSaldoNegativo")) {
        BITACORARESTAURARSALDONEGATIVO = true;
      }
      if (desc.equals("BitacoraClasificacionDeCofactor")) {
        BITACORACLASIFICACIONDECOFACTOR = true;
      }

      // PROCESO AUTOMATICO
      if (desc.equals("ProcesoAutomatico")) {
        PROCESOAUTOMATICO = true;
      }
      if (desc.equals("PrimerDiaHabil")) {
        PRIMERDIAHABIL = true;
      }

      // PRUEBAS SFTPPRUEBASFTP
      if (desc.equals("pruebasSFTP")) {
        PRUEBASFTP = true;
      }

      // MENU REPORTES / REPORTES / RESUMEN DE FACTURACION POR CLIENTE Y  BITACORA DE ACCIONES
      if (desc.equals("ResumenFacturacionPorCliente")) {
        RESUMENFACTURACIONPORCLIENTE = true;
      }
      if (desc.equals("BitacoraDeAcciones")) {
        BITACORADEACCIONES = true;
      }
    }
  }

  public String checkUserAction() {
    System.out.println("Constantes.DEV " + Constantes.PRODUCCION);
    return (Constantes.PRODUCCION) ? checkUserAction_PROD() : checkUserAction_DEV();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private String checkUserAction_PROD() {
    String flujoRetorno = "";
    Map sessionMap;
    UsuarioDTO usuario = new UsuarioDTO();
    List rolesSADID = new ArrayList();
    boolean usuPassValido;
    boolean rolValido = false;
    boolean tieneEmail = false;
    boolean usuarioSinIngresar = false;
    String nodo = "ou=Empleados,ou=Personas,o=BANCOMEXT";
    String[] attrIdsToSearch = new String[]{"groupMembership", "securityEquals", "fullName", "mail"};
    try {
      log.info("Inicio Login");
      if (strUsuario == null || clave == null) {
        usuPassValido = false;
      } else {
        strUsuario = strUsuario.trim().toLowerCase();
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        //env.put("java.naming.provider.url", "ldap://" + Constantes.URL_LDAP + ":" + Constantes.PORT_LDAP + "/o=bancomext");
        env.put("java.naming.provider.url", "ldap://" + Constantes.URL_LDAP + ":" + Constantes.PORT_LDAP);
        env.put("java.naming.security.authentication", "simple");
        env.put("java.naming.security.principal", "cn=" + strUsuario.toUpperCase() + "," + nodo);
        env.put("java.naming.security.credentials", clave);

        try {
          log.info("Inicia validacion nodo Empleados");
          System.out.println("Inicia validacion nodo Empleados");
          new InitialDirContext(env);
          usuPassValido = true;
        } catch (javax.naming.AuthenticationException er) {
          //Error intentando conexion
          System.out.println("Fallo conexion nodo Empleados :" + er.getMessage());
          log.info("Fallo conexion nodo Empleados :" + er.getMessage());
          log.info("Intentando conexion a nodo Clientes ");
          log.info("Usuario:" + strUsuario.toUpperCase());
          nodo = "ou=Clientes,ou=Personas,o=BANCOMEXT";
          env.put(Context.SECURITY_PRINCIPAL, "cn=" + strUsuario.toUpperCase() + "," + nodo);
          try {
            new InitialDirContext(env);
            usuPassValido = true;
          } catch (javax.naming.AuthenticationException error) {
            usuPassValido = false;
          }
        }

        if (usuPassValido) {
          usuario.setUsuario(strUsuario); //lowercase
          usuario.setClave(clave);
          String usuarioAut = strUsuario;
          try {
            // DESCOMENTAR!!!!!!!!!!! Inicio
            ProcedimientosService.imxPRegistraUsuario(usuarioAut);
            // DESCOMENTAR!!!!!!!!!!! Fin
            log.info("SE EJECUTA PROCESO SESSION CORRECTAMENTE. ---->");
          } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
          }


          InitialDirContext context = new InitialDirContext(env);
          String filter = String.format("(&(cn=%s)(objectclass=person))", strUsuario);

          SearchControls constraints = new SearchControls();
          constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
          constraints.setReturningAttributes(attrIdsToSearch);

          NamingEnumeration results = context.search(nodo, filter, constraints);
          SearchResult result = (SearchResult) results.next();

          //Obtenemos los permisos "groupMembership"
          Attributes attrs = result.getAttributes();

          //Obtenemos el rol , "securityEquals"
          Attribute attr = attrs.get(attrIdsToSearch[1]);
          NamingEnumeration e = attr.getAll();
          while (e.hasMore()) {
            String entrada = (String) e.next();

            if (entrada != null && entrada.contains("cn=")) {

              final List<String> apps = obtenerParametroMultiple(entrada);

              if (apps.contains(ID_SISTEMA_FACTURAS)) {
                final String permiso = obtenerParametroSimple(entrada);
                log.info("app: " + ID_SISTEMA_FACTURAS + ", permiso: " + permiso);
                rolesSADID.add(permiso);
              }
            }
          }
          FiCatRoles ultimoRolEncontrado = new FiCatRoles();
          if (!rolesSADID.isEmpty()) {
            final List<FiCatRoles> roles = genericService.getAll("FiCatRoles");
            for (final FiCatRoles rol : roles) {
              log.info("checking rol: " + rol.getDescripcion());
              for (String rolsadid : (List<String>) rolesSADID) {
                if (rol.getDescripcion().equals(rolsadid)) {
                  rolValido = true;
                  setRol(rol.getDescripcion());
                  usuario.setRol(rol.getDescripcion());
                  ultimoRolEncontrado = rol;
                  log.info("valid rol found: " + rolsadid);
                }
              }
            }
            if (!rolValido) {
              rol = null;
              usuario.setRol(null);
            } else {
              generaAccesos(ultimoRolEncontrado);
            }
          } else {
            rol = null;
            usuario.setRol(null);
          }
          //obtenemos el nombre  "fullName"
          attr = attrs.get(attrIdsToSearch[2]);
          e = attr.getAll();
          while (e.hasMore()) {
            String value = (String) e.next();
            if (value != null) {
              log.info("nombre de ldap:" + value);
              nombre = value;
              usuario.setNombre(value);
            }
          }
          //obtenemos el email  "mail"
          attr = attrs.get(attrIdsToSearch[3]);
          if (attr != null) {
            e = attr.getAll();
            while (e.hasMore()) {
              String value = (String) e.next();
              if (value != null) {
                tieneEmail = true;
                usuario.setEmail(value);
              }
            }
          } else {
            if (rolValido) {
              tieneEmail = true;
            }

            usuario.setEmail("");
          }
        }

      }
      //se niega acceso si no se tiene un Rol valido o email
      if (rolValido && tieneEmail) {
        //Verificacion de Duplicidad de usuarios
        try {
          List lstAccesos =
              genericService.get("FiAccesos", "upper(idpromotor)=upper('" + strUsuario + "')", "");
          if (lstAccesos != null && !lstAccesos.isEmpty()) {
            if (((FiAccesos) lstAccesos.get(0)).getFechaSalida() == null) {
              // DESCOMENTAR !!!!! Inicio
              if (((FiAccesos) lstAccesos.get(0)).getFechaEntrada() != null &&
                  (((new Date().getTime() -
                      ((FiAccesos) lstAccesos.get(0)).getFechaEntrada().getTime()) / 1000) / 60) < 30) {
                HttpSession sess =
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
                creaMensaje("Su usuario esta bloqueado, por favor espere " +
                    (int) Math.floor((double) sess.getMaxInactiveInterval() / 1000) / 60 + " minutos");
              } else {
                usuarioSinIngresar = true;
              }
              // DESCOMENTAR !!!!! Fin
            } else {
              usuarioSinIngresar = true;
            }
          } else {
            usuarioSinIngresar = true;
          }
        } catch (Exception ex) {
          creaMensaje("Ha ocurrido un error inesperado, por favor vuelvalo a intentar");
          throw ex;
        }

        if (usuarioSinIngresar) {
          flujoRetorno = "success";
          sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
          sessionMap.put("usuario", usuario);
        }

      } else {
        if (!usuPassValido) {
          if (strUsuario == null || clave == null) {
            creaMensaje("Usuario y Contrasenia requeridos ");
          } else {
            creaMensaje("Acceso denegado, No existe el usuario y/o la contrasenia");
          }
        } else if (!rolValido) {
          String grupoAdministradores = ConsultasService.getGrupoAdministradores();

          enviarCorreo(
              genericService.get("FiAccesos", " upper(rol) = upper('" + grupoAdministradores + "')", ""),
              UtileriaCorreo.contenidoLogin(usuario.getUsuario(), "El usuario no tiene un rol permitido")
          );
          creaMensaje("Acceso denegado, No se tiene un rol permitido");
        } else {
          creaMensaje("Acceso denegado, No se tiene un correo valido");
        }
      }
    } catch (Exception ex) {
      System.out.println("Error :" + ex.getStackTrace());
      log.error(ex.getMessage(), ex);
      creaMensaje("Error :" + ex.getMessage());
    }

    return flujoRetorno;

  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private String checkUserAction_DEV() {
    System.out.println("INTO checkUserAction_DEV begin");
    ADMINMENU = true;
    AGRUPACIONPOLIZAS = true;
    AGRUPACIONPOLIZASEXTEMPORANEO = true;
    BITACORAAGRUPACIONPOLIZAS = true;
    BITACORACANCELACIONPROVINTERESES = true;
    BITACORACAPITALIZACIONINTERESES = true;
    BITACORACLASIFICACIONDECOFACTOR = true;
    BITACORADEACCIONES = true;
    BITACORARECLASIFICARSALDONEGATIVO = true;
    BITACORARESTAURARSALDONEGATIVO = true;
    CARGAFACTURAS = true;
    CATCORREOCLIENTES = true;
    CATCORREOREPORTES = true;
    CATCUENTASCOMPARATIVO = true;
    CATCUENTASGENERACION = true;
    CATPARAMGENERALES = true;
    CATROLES = true;
    CIFRASTOTALES = true;
    CLIENTESCARTERAVENCIDA = true;
    FACTURASYEDOSDECTA = true;
    FOLIOSFISCALES = true;
    HISTORICOARCHIVOSCFD = true;
    OPERACION = true;
    PARAMECODIGOOPERACIONCTA = true;
    PARAMECOFACTOR = true;
    PARAMEJECUCIONPROCESO = true;
    PRIMERDIAHABIL = true;
    PROCESOAUTOMATICO = true;
    PROCESOCANCELACION = true;
    PROCESOFACTORAJE = true;
    PROCESOMANCANCELACIONPROVINTERESES = true;
    PROCESOMANCAPITALIZACIONINTERESES = true;
    PROCESOMANCLASIFICACIONDECOFACTOR = true;
    PROCESOMANRECLASIFICARSALDONEGATIVO = true;
    PROCESOMANRESTAURARSALDONEGATIVO = true;
    PROCESORESUMENFACTURACION = true;
    PROCESORESUMENFACTURACIONPRIMECOM = true;
    PRUEBASFTP = true;
    REPORTEBITAERR = true;
    REPORTECOMPARATIVO = true;
    REPORTES = true;
    REPORTESALDOSFIU = true;
    RESUMENFACTURACIONPORCLIENTE = true;
    ROLESMODULOS = true;
    USUARIOS = true;
    VERIFICAPROCESOCFDI = true;

    nombre = "Monica Gonzalez";
    rol = "UsrAdmin";
    final UsuarioDTO usuario = new UsuarioDTO();
    usuario.setUsuario(strUsuario);
    usuario.setClave(clave);
    usuario.setNombre(nombre);
    usuario.setRol(rol);
    usuario.setEmail("mgonzale@banco.com");
    usuario.setFechaEntrada(new Date());
    System.out.println("INTO checkUserAction_DEV middle");
    final Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    System.out.println("INTO checkUserAction_DEV after middle");
    sessionMap.put("usuario", usuario);
    System.out.println("INTO checkUserAction_DEV after setSession");
    System.out.println("INTO checkUserAction_DEV finish");
    return "success";
  }

  public void creaMensaje(String error) {
    FacesMessage message = new FacesMessage();
    FacesContext context = FacesContext.getCurrentInstance();
    message.setDetail(error);
    message.setSummary(error);
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    context.addMessage(loginButton.getClientId(context), message);
  }

  public void logout() {
    try {
      final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
      externalContext.getSessionMap().remove("usuario");
      final HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
      final HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      externalContext.redirect(externalContext.getRequestContextPath());
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  private String obtenerParametroSimple(final String entrada) {
    final String parametro = "cn=";
    int beginIndex = entrada.indexOf(parametro) + parametro.length();
    int endIndex = entrada.indexOf(",", beginIndex);
    if (endIndex < 0) {
      endIndex = entrada.length();
    }
    return entrada.substring(beginIndex, endIndex);
  }

  private List<String> obtenerParametroMultiple(final String entrada) {
    final List<String> valores = new ArrayList<>();
    final String parametro = "ou=";
    int endIndex = 0;
    int beginIndex;
    while ((beginIndex = entrada.indexOf(parametro, endIndex)) > 0) {
      beginIndex += parametro.length();
      endIndex = entrada.indexOf(",", beginIndex);
      if (endIndex < 0) {
        endIndex = entrada.length();
      }
      valores.add(entrada.substring(beginIndex, endIndex));
    }
    return valores;
  }

}
