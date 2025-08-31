package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FiRechazoFacturasImx implements Serializable {

  private FiRechazoFacturasImxId id;
  private String posPostName;
  private String lciPfLibEs;
  private String ccpCurrency;
  private String ccpCapContract;
  private String ccpCapCase;
  private String posPostStatementCase;
  private String cliClientAdr1;
  private String cliClientAdr2;
  private String cliClientAdr3;
  private String cliClientAdr4;
  private String cliClientBisiness;
  private String cliClientVille;
  private String cliClientPrenom;
  private String cliClientNom;
  private String cliClientPays;
  private String cliClientRefext;
  private String cliClientDomaine;
  private String cliClientRueEnterne;
  private String cliClientLangue;
  private String cliClientCp;
  private String cliClientDivision;
  private String cliClientType;
  private String cliClientTva;
  private String lctAbrev;
  private String lctValeurTrad;
  private BigDecimal posPostAmtOnlyvat;
  private BigDecimal posPostAmtNovat;
  private BigDecimal posPostAmtWithvatFactor;
  private BigDecimal posPostAmtWithvat;
  private String motivoRechazo;
  private Date fechaRechazo;

}


