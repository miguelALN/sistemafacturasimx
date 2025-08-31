package com.bancomext.web.beans.oper;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCuerpoCorreos;
import com.bancomext.service.mapping.VoHistoricoEncabezado;
import com.bancomext.service.mapping.VoTablaElemento;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@ManagedBean(name = "procResumenFacturaPrime")
@ViewScoped
public class ProcResumenFacturaPrime implements Serializable {

  private static final Logger log = LogManager.getLogger(ProcResumenFacturaPrime.class);

  public ProcResumenFacturaPrime() {
    final UsuarioDTO usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ResumenFacturacionPorClientePrimCom", usuarioLogueado.getRol());
    }
  }

  private static void enviarCorreo(final List<VoTablaElemento> listaCorreos, final String asunto,
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
        log.info("Correo: " + listaCorreos.get(i).getElemento());
        try {
          to[i] = new InternetAddress(listaCorreos.get(i).getElemento());
        } catch (AddressException ae) {
          log.error(ae.getMessage(), ae);
        }
      }
      UtileriaCorreo.enviarCorreo(to, cc, asunto, contenido, "SUCRE", null);
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

    try {

      final List<VoHistoricoEncabezado> listaMensual =
          ConsultasService.getResumenFacturacion(primerDiaMesAnterior, ultimoDiaMesAnterior);

      if (listaMensual.isEmpty()) {
        return;
      }

      final List<String> correosEnviados = new ArrayList<>();
      final List<VoHistoricoEncabezado> listaPorCliente = new ArrayList<>();
      BigDecimal totalFacturas = new BigDecimal("0.0");

      int contador = 0;
      for (int i = 0; i < listaMensual.size(); i++) {
        final String cliente = listaMensual.get(i).getNombre();
        log.info("Cliente: " + cliente);

        for (final VoHistoricoEncabezado voHistoricoEncabezado : listaMensual) {

          final VoHistoricoEncabezado vo = new VoHistoricoEncabezado();

          if (cliente.equals(voHistoricoEncabezado.getNombre())) {
            vo.setNombre(voHistoricoEncabezado.getNombre());
            vo.setNumeroFolio(voHistoricoEncabezado.getNumeroFolio());
            vo.setFechaValor(voHistoricoEncabezado.getFechaValor());
            vo.setImporte(voHistoricoEncabezado.getImporte());
            vo.setCodigoCliente(voHistoricoEncabezado.getCodigoCliente());
            vo.setProducto(voHistoricoEncabezado.getProducto());

            listaPorCliente.add(vo);

            totalFacturas = totalFacturas.add(voHistoricoEncabezado.getImporte());
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
          final List<FiCuerpoCorreos> lst = ServiceLocator.getGenericService().get("FiCuerpoCorreos",
              " correo = 'CORREOS_RESUMEN_FACTURACION_MENSUAL'", "");
          if (!lst.isEmpty()) {
            asunto = lst.get(0).getAsunto();
            contenido = lst.get(0).getCuerpo();
          }

          final List<VoTablaElemento> listaCorreosPrimeCom =
              ConsultasService.getCorreosCFDIPrime(listaPorCliente.get(0).getCodigoCliente());

          enviarCorreo(listaCorreosPrimeCom, asunto,
              UtileriaCorreo.contenidoProcesoEnvioFacturasReporteMesPrimeCom(listaPorCliente, totalFacturas, contenido));
          correosEnviados.add(cliente);
          contador++;
        }
        listaPorCliente.clear();
        totalFacturas = new BigDecimal("0.0");
      }

      mostrarMensaje("Se ha procesado correctamente el resumen de facturación mensual", false);
      log.info("Correos enviados Prime: " + contador);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      mostrarMensaje("Error al procesar el resumen de facturación mensual", true);
    }
  }

}
