package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiFacturasEncabezado implements Serializable, Cloneable {

  private FiFacturasEncabezadoId id;

  private BigDecimal importe;
  private BigDecimal importeAnterior;
  private BigDecimal importeCont;
  private BigDecimal tipoDeCambio;

  private Date fechaCambioImp;
  private Date fechaVerificacion;

  private String atencion;
  private String calle;
  private String campoB;
  private String codigoMoneda;
  private String codigoPostal;
  private String colonia;
  private String estado;
  private String linea;
  private String localidad;
  private String moneda;
  private String municipio;
  private String negociacion;
  private String nombre;
  private String numExterior;
  private String numFactura;
  private String numInterior;
  private String pais;
  private String referencia;
  private String rfc;
  private String secuencia;
  private String secuenciaImx;
  private String statusVerificacion;
  private String tipoCredito;
  private String usuCambioImporte;

  @Override
  public FiFacturasEncabezado clone() {
    try {
      final FiFacturasEncabezado clone = (FiFacturasEncabezado) super.clone();
      clone.id = id.clone();

      if (clone.atencion == null) {
        clone.atencion = "";
      }
      if (clone.calle == null) {
        clone.calle = "";
      }
      if (clone.campoB == null) {
        clone.campoB = "";
      }
      if (clone.codigoMoneda == null) {
        clone.codigoMoneda = "";
      }
      if (clone.codigoPostal == null) {
        clone.codigoPostal = "";
      }
      if (clone.colonia == null) {
        clone.colonia = "";
      }
      if (clone.estado == null) {
        clone.estado = "";
      }
      if (clone.importe == null) {
        clone.importe = new BigDecimal("0");
      }
      if (clone.linea == null) {
        clone.linea = "";
      }
      if (clone.localidad == null) {
        clone.localidad = "";
      }
      if (clone.moneda == null) {
        clone.moneda = "";
      }
      if (clone.municipio == null) {
        clone.municipio = "";
      }
      if (clone.negociacion == null) {
        clone.negociacion = "";
      }
      if (clone.numExterior == null) {
        clone.numExterior = "";
      }
      if (clone.numInterior == null) {
        clone.numInterior = "";
      }
      if (clone.nombre == null) {
        clone.nombre = "";
      }
      if (clone.numFactura == null) {
        clone.numFactura = "";
      }
      if (clone.pais == null) {
        clone.pais = "";
      }
      if (clone.referencia == null) {
        clone.referencia = "";
      }
      if (clone.rfc == null) {
        clone.rfc = "";
      }
      if (clone.secuenciaImx == null) {
        clone.secuenciaImx = "";
      }
      if (clone.statusVerificacion == null) {
        clone.statusVerificacion = "";
      }
      if (clone.tipoCredito == null) {
        clone.tipoCredito = "";
      }
      if (clone.usuCambioImporte == null) {
        clone.usuCambioImporte = "";
      }

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
