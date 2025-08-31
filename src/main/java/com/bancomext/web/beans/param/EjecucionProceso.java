package com.bancomext.web.beans.param;

import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiProcesoAutManImx;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ManagedBean(name = "ejecucionProceso")
@ViewScoped
public class EjecucionProceso implements Serializable {

  private static final Logger log = LogManager.getLogger(EjecucionProceso.class);
  private final GenericService genericService = ServiceLocator.getGenericService();
  private List<FiProcesoAutManImx> listaProcAutMan;
  private List<String> listaId;
  private UsuarioDTO usuarioLogueado;
  private String horaProceso = "";
  private boolean checkBoxFlag1;
  private boolean checkBoxFlag2;
  private boolean checkBoxFlag3;
  private boolean checkBoxFlag4;
  private boolean checkBoxFlag5;
  private boolean checkBoxFlag6;
  private boolean checkBoxFlag7;
  private boolean checkBoxFlag8;
  private boolean checkBoxFlag9;
  private boolean checkBoxFlag;
  private boolean horaVisible = true;

  public EjecucionProceso() {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ParameEjecucionProceso", usuarioLogueado.getRol());
    }
    init();
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    //PrimeFaces.current().ajax().update(":form1:msgGuardar");
  }

  private static void mostrarMensaje(final String msg, final boolean esError, String nombreComponente) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(nombreComponente);
  }

  @SuppressWarnings("unchecked")
  private void init() {
    listaId = new ArrayList<>();
    listaId.add("CAN");
    listaId.add("CAP");
    listaId.add("RCL");
    listaId.add("RES");
    listaId.add("COF");

    // CICLO QUE VALIDA LOS PROCESOS PARAMETRIZADOS
    for (String s : listaId) {
      listaProcAutMan = genericService.get("FiProcesoAutManImx", "TIPO_PROCESO = '" + s + "'", "");

      if (listaProcAutMan != null && !listaProcAutMan.isEmpty()) {
        if (listaProcAutMan.get(0).getTipoProceso().equals("CAN") && listaProcAutMan.get(0).getEjecucion().equals("M")) {
          checkBoxFlag = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("CAN") && listaProcAutMan.get(0).getEjecucion().equals("A")) {
          checkBoxFlag1 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("CAP") && listaProcAutMan.get(0).getEjecucion().equals("M")) {
          checkBoxFlag2 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("CAP") && listaProcAutMan.get(0).getEjecucion().equals("A")) {
          checkBoxFlag3 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("RCL") && listaProcAutMan.get(0).getEjecucion().equals("M")) {
          checkBoxFlag4 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("RCL") && listaProcAutMan.get(0).getEjecucion().equals("A")) {
          checkBoxFlag5 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("RES") && listaProcAutMan.get(0).getEjecucion().equals("M")) {
          checkBoxFlag6 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("RES") && listaProcAutMan.get(0).getEjecucion().equals("A")) {
          checkBoxFlag7 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("COF") && listaProcAutMan.get(0).getEjecucion().equals("M")) {
          checkBoxFlag8 = true;
        } else if (listaProcAutMan.get(0).getTipoProceso().equals("COF") && listaProcAutMan.get(0).getEjecucion().equals("A")) {
          checkBoxFlag9 = true;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void ejecucionProceso() {

    log.info("INTO ejecucionProceso  0 " + checkBoxFlag + "--1 " + checkBoxFlag1 + "--2 " + checkBoxFlag2 + "--3 " + checkBoxFlag3 + checkBoxFlag4 + checkBoxFlag5 + checkBoxFlag6 + checkBoxFlag7 + checkBoxFlag8 + checkBoxFlag9 );
    // SE VERIFICA QUE NO SE REPITA NINGUN PROCESO POR SELECCION
    if ((checkBoxFlag && checkBoxFlag1) || (checkBoxFlag2 && checkBoxFlag3) || (checkBoxFlag4 && checkBoxFlag5)
                                        || (checkBoxFlag6 && checkBoxFlag7) || (checkBoxFlag8 && checkBoxFlag9) ) {
      log.info("INTO ejecucionProceso 0");
      mostrarMensaje("Verifique los procesos. Debe seleccionar Manual o Automático.", false);
    } else if (!checkBoxFlag && !checkBoxFlag1 && !checkBoxFlag2 && !checkBoxFlag3
        && !checkBoxFlag4 && !checkBoxFlag5 && !checkBoxFlag6 && !checkBoxFlag7
        && !checkBoxFlag8 && !checkBoxFlag9) {
      log.info("INTO ejecucionProceso 5");
      mostrarMensaje("Debe seleccionar un tipo de Proceso.", false);
    } else {

      log.info("INTO ejecucionProceso N msg");
      FiProcesoAutManImx procesos = new FiProcesoAutManImx();
      // CREAMOS LISTA PARA AGREGAR EL TIPO DE PROCESO SI ES MANUAL O AUTOMATICO
      List<String> listTipoProceso = new ArrayList<>();

      // AGREGAMOS LOS VALORES A LA LISTA DE PROCESOS DEPENDIENDO DE LOS FLAGS
      if (checkBoxFlag) {
        listTipoProceso.add("M");
      }
      if (checkBoxFlag1) {
        listTipoProceso.add("A");
      }
      if (checkBoxFlag2) {
        listTipoProceso.add("M");
      }
      if (checkBoxFlag3) {
        listTipoProceso.add("A");
      }
      if (checkBoxFlag4) {
        listTipoProceso.add("M");
      }
      if (checkBoxFlag5) {
        listTipoProceso.add("A");
      }
      if (checkBoxFlag6) {
        listTipoProceso.add("M");
      }
      if (checkBoxFlag7) {
        listTipoProceso.add("A");
      }
      if (checkBoxFlag8) {
        listTipoProceso.add("M");
      }
      if (checkBoxFlag9) {
        listTipoProceso.add("A");
      }

      int cont = 0;
      if (listTipoProceso.size() == 5) {
        for (int i = 0; i < listTipoProceso.size(); i++) {
          setListaProcAutMan(genericService.get(
              "FiProcesoAutManImx", " TIPO_PROCESO = '" + listaId.get(i) + "'", ""));

          if (listaProcAutMan.get(0).getProcesado() == null) {

            procesos.setNombreProceso(listaProcAutMan.get(0).getNombreProceso());
            procesos.setId(listaProcAutMan.get(0).getId());
            procesos.setTipoProceso(listaId.get(i));

            if (listTipoProceso.get(i).equals("M")) {
              procesos.setEjecucion("M");
              procesos.setHora("");
              try {
                genericService.update(procesos);
                cont++;
              } catch (Exception e) {
                mostrarMensaje("Ha surgido un error a la hora de insertar el registro.", true);
                log.error(e.getMessage(), e);
              }

            } else {
              if (!horaProceso.isEmpty()) {
                if (horaProceso.charAt(2) == ':' && horaProceso.length() == 5) {
                  procesos.setEjecucion("A");
                  procesos.setHora(horaProceso);
                  try {
                    genericService.update(procesos);
                    cont++;
                  } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    mostrarMensaje("Ha surgido un error a la hora de insertar el registro.", true);
                  }
                } else {
                  mostrarMensaje("Por favor verifique el formato de la Hora", true);
                }
              } else {
                mostrarMensaje("La hora es requerida para el proceso Automático.", true);
              }
            }

          } else {
            mostrarMensaje("Aun faltan procesos por ejecutar." + listaProcAutMan.get(0).getNombreProceso() + " No se han guardado los registros.", true);
          }

        }

        if (cont == 5)
          mostrarMensaje("Se han guardado satisfactoriamente los registros.", false);
        else
          mostrarMensaje("No se guardaron correctamente los registros", true);
      } else {
        log.info("DEBE SELECCIONAR LOS PROCESOS");
        mostrarMensaje("Debe seleccionar todos los procesos", true);
      }
    }
  }

  public void isDisabled(ValueChangeEvent e) {
    log.info("INTO isDisabled " + e.getNewValue());
    boolean bandera = (Boolean) e.getNewValue();
    setHoraVisible(bandera);
    log.info("INTO checkUncheckOption msg " + checkBoxFlag + " -- " + checkBoxFlag1 + " -- " + horaVisible);
  }

 public void checkUncheckOption() {
    log.info("INTO checkUncheckOption msg " + checkBoxFlag + " -- " + checkBoxFlag1 + "--" + horaProceso);
  }

}
