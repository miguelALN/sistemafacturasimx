package com.bancomext.web.beans.oper;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCorreosPorCliente;
import com.bancomext.service.mapping.FiCuerpoCorreos;
import com.bancomext.service.mapping.VoReporteFacturasMensual;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.UtileriaCorreo;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "procResumenFacturacion")
@ViewScoped
public class ProcResumenFacturacion implements Serializable {

  private static final Logger log = LogManager.getLogger(ProcResumenFacturacion.class);

  public ProcResumenFacturacion() {
    final UsuarioDTO usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ProcesoResumenFacturacion", usuarioLogueado.getRol());
    }
  }

  private static void enviarCorreo(final List<FiCorreosPorCliente> listaCorreos, final String asunto,
                                   final String contenido) {
    final InternetAddress[] cc = new InternetAddress[1];
    try {
      if ("Desarrollo".equals(Constantes.AMBIENTE)) {
        cc[0] = new InternetAddress(Constantes.MAIL_DESARROLLO);
      } else {
        cc[0] = new InternetAddress("facturacionbancomext@bancomext.gob.mx");
      }
    } catch (AddressException ae) {
      log.error(ae.getMessage(), ae);
    }
    if (listaCorreos != null && !listaCorreos.isEmpty()) {
      final InternetAddress[] to = new InternetAddress[listaCorreos.size()];
      for (int i = 0; i < listaCorreos.size(); i++) {
        log.info("Correo: " + listaCorreos.get(i).getId().getEmail());
        try {
          to[i] = new InternetAddress(listaCorreos.get(i).getId().getEmail());
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
      UtileriaCorreo.enviarCorreo(to, cc, asunto, contenido, "FACTORAJE", null);
    }
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  @SuppressWarnings({"unchecked"})
  public void procesarResumen() {

    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -1);
    cal.set(Calendar.DATE, 1);
    final Date primerDiaMesAnterior = cal.getTime();
    cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    final Date ultimoDiaMesAnterior = cal.getTime();

    String fechaProcesosInicio = "01/10/2013";
    String fechaProcesosFinal = "31/10/2013";
    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
    Date fecha = new Date();
    Date fechaFinal = new Date();
    try {
        fecha = formato.parse(fechaProcesosInicio);
        fechaFinal = formato.parse(fechaProcesosFinal);
        log.info("fecha " + fecha + "fechaFinal " + fechaFinal);
    } catch (Exception exception) {
        log.info(" ERROR " + exception.getMessage() + exception.getCause());
    }

    try {
      final List<VoReporteFacturasMensual> listaMensual =
          ConsultasService.getReporteFacturas(primerDiaMesAnterior, ultimoDiaMesAnterior, null);

      if (listaMensual.isEmpty()) {
        mostrarMensaje("No existen facturas para procesar ", false);
        return;
      }

      final List<String> correosEnviados = new ArrayList<>();
      final List<VoReporteFacturasMensual> listaPorCliente = new ArrayList<>();
      BigDecimal totalFacturas = new BigDecimal("0.0");

      int contador = 0;
      for (int i = 0; i < listaMensual.size(); i++) {
        final String cliente = listaMensual.get(i).getCliente();
        log.info("Cliente: " + cliente);

        for (final VoReporteFacturasMensual voReporteFacturasMensual : listaMensual) {

          final VoReporteFacturasMensual vo = new VoReporteFacturasMensual();

          if (cliente.equals(voReporteFacturasMensual.getCliente())) {
            vo.setCliente(voReporteFacturasMensual.getCliente());
            vo.setNoFactura(voReporteFacturasMensual.getNoFactura());
            vo.setFecha(voReporteFacturasMensual.getFecha());
            vo.setConcepto(voReporteFacturasMensual.getConcepto());
            vo.setTotal(voReporteFacturasMensual.getTotal());
            vo.setCodigoCliente(voReporteFacturasMensual.getCodigoCliente());

            listaPorCliente.add(vo);

            totalFacturas = totalFacturas.add(voReporteFacturasMensual.getTotal());
          }
        }

        boolean enviado = false;
        for (final String reg : correosEnviados) {
          if (reg.equals(cliente)) {
            enviado = true;
            break;
          }
        }

        log.info("Enviado = " + enviado);
        if (!enviado) {
          // ENVIAR CORREO
          String asunto = "";
          String contenido = "";

          final GenericService genericService = ServiceLocator.getGenericService();

          final List<FiCuerpoCorreos> lst = genericService.get(
              "FiCuerpoCorreos", " correo = 'CORREOS_RESUMEN_FACTURACION_MENSUAL'", "");
          if (!lst.isEmpty()) {
            asunto = lst.get(0).getAsunto();
            contenido = lst.get(0).getCuerpo();
          }

          int codigoCliente = listaPorCliente.get(0).getCodigoCliente();
          enviarCorreo(
              genericService.get("FiCorreosPorCliente",
                  " id.codigoCliente='" + codigoCliente + "'", " Order by  id.email asc "),
              asunto, UtileriaCorreo.contenidoProcesoEnvioFacturasReporteMes(listaPorCliente, totalFacturas, contenido));
          correosEnviados.add(cliente);
          contador++;
        }
        listaPorCliente.clear();
        totalFacturas = new BigDecimal("0.0");
      }

      mostrarMensaje("Se ha procesado correctamente el resumen de facturación mensual", false);
      log.info("Correos enviados: " + contador);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      mostrarMensaje("Error al procesar el resumen de facturación mensual", true);
    }
  }
}
