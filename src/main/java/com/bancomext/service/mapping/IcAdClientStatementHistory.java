package com.bancomext.service.mapping;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class IcAdClientStatementHistory implements Serializable {

  private BigDecimal dateId;
  private String clAccountCase;
  private String clRefindividu;
  private String factorRefindividu;
  private String cofactorRefindividu;
  private String provider;
  private String contractNumber;
  private String contractCase;
  private String currency;
  private BigDecimal availability;
  private BigDecimal fiu;
  private BigDecimal fiucash;
  private BigDecimal fiusl;
  private BigDecimal portfolio;
  private BigDecimal financingBase;
  private BigDecimal blockedAmount;
  private BigDecimal finLimits;
  private BigDecimal nonMatchedPayments;
  private BigDecimal coveredAmount;
  private BigDecimal notcoveredAmount;
  private BigDecimal disputeAmount;
  private BigDecimal fundableAmount;
  private BigDecimal purchasedAmount;
  private String memoSource;
  private String refpiece;
  private Date dt04Dt;
  private String gpirole;
  private BigDecimal availabilityEur;
  private BigDecimal fiuEur;
  private BigDecimal fiucashEur;
  private BigDecimal fiuslEur;
  private BigDecimal portfolioEur;
  private BigDecimal financingBaseEur;
  private BigDecimal blockedAmountEur;
  private BigDecimal finLimitsEur;
  private BigDecimal nonMatchedPaymentsEur;
  private BigDecimal disputeAmountEur;
  private BigDecimal fundableAmountEur;
  private BigDecimal purchasedAmountEur;
  private String provisionFg;
  private String cofactorRefext;
  private BigDecimal retentOthersEur;
  private BigDecimal retentDispEur;
  private BigDecimal retentDisp;
  private BigDecimal retentionsEur;
  private BigDecimal retentions;
  private BigDecimal retentOthers;
  private BigDecimal retentSuprfin;
  private String capContract;
  private String capCase;
  private BigDecimal dt04;

}
