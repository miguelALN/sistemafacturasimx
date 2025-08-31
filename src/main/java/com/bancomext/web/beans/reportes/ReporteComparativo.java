package com.bancomext.web.beans.reportes;

import com.bancomext.service.ConsultasService;
import com.bancomext.service.GenericService;
import com.bancomext.service.ServiceLocator;
import com.bancomext.service.dto.InventoryStatus;
import com.bancomext.service.dto.Product;
import com.bancomext.service.dto.UsuarioDTO;
import com.bancomext.service.mapping.FiCatCuentasSicav;
import com.bancomext.service.mapping.FiHisRepComparativo;
import com.bancomext.web.utils.Constantes;
import com.bancomext.web.utils.Utilerias;
import com.bancomext.web.utils.UtileriasReportesExcel;
import com.bancomext.web.validator.ValidadorSesion;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@ManagedBean(name = "reporteComparativo")
@ViewScoped
public class ReporteComparativo implements Serializable {

  private static final Logger log = LogManager.getLogger(ReporteComparativo.class);
  private static final String filePath = "IMX/exportar/";
  private final GenericService genericService = ServiceLocator.getGenericService();
  private String nombreArchivo = "ReporteComparativo";
  private Date fechaConsulta;
  private DataTable htmlDataTable;
  private List<FiCatCuentasSicav> listaCuentas;
  private List<FiHisRepComparativo> listaComparativo;
  private UsuarioDTO usuarioLogueado;
  private String cliente;
  private String contrato;
  private String ctaImp;
  private String sctaImp;
  private String ssctaImp;
  private String sssctaImp;
  private String ctaExp;
  private String sctaExp;
  private String ssctaExp;
  private String sssctaExp;
  private String ctaFiu;
  private String sctaFiu;
  private String ssctaFiu;
  private String sssctaFiu;
  private String mensaje;
  private String nombreArchivoFacturas;
  private UsuarioDTO usuario;
  private boolean paginatorVisible = false;
  private int defaultRows = Constantes.REGISTROS;
  private int placeholder;
  private StreamedContent excelResourceV;
  private Utilerias utilerias;
  private List<Product> products;


  public ReporteComparativo() throws ParseException, IOException {
    usuarioLogueado = ValidadorSesion.getUsuarioLogueado();
    if (usuarioLogueado != null) {
      ValidadorSesion.validarPermiso("ReporteComparativo", usuarioLogueado.getRol());
    }
    init();
  }

  private static void mostrarMensaje(final String msg, final boolean esError) {
    final FacesMessage mensaje = new FacesMessage();
    mensaje.setSummary(msg);
    mensaje.setDetail("");
    mensaje.setSeverity(esError ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
    FacesContext.getCurrentInstance().addMessage(null, mensaje);
    PrimeFaces.current().ajax().update(":form1:messages");
  }

  private void init() throws ParseException, IOException {
    //Cuenta Imp
    ctaImp = ConsultasService.getStringParametro("cta_Imp");
    sctaImp = ConsultasService.getStringParametro("scta_Imp");
    ssctaImp = ConsultasService.getStringParametro("sscta_Imp");
    sssctaImp = ConsultasService.getStringParametro("ssscta_Imp");
    ctaExp = ConsultasService.getStringParametro("cta_Exp");
    sctaExp = ConsultasService.getStringParametro("scta_Exp");
    ssctaExp = ConsultasService.getStringParametro("sscta_Exp");
    sssctaExp = ConsultasService.getStringParametro("ssscta_Exp");
    ctaFiu = ConsultasService.getStringParametro("cta_Fiu");
    sctaFiu = ConsultasService.getStringParametro("scta_Fiu");
    ssctaFiu = ConsultasService.getStringParametro("sscta_Fiu");
    sssctaFiu = ConsultasService.getStringParametro("ssscta_Fiu");
    excelResourceV = null;
    //setUsuariousuarioLogueado;
    if (usuario != null) {
      mensaje = null;
    }
    utilerias = new Utilerias();

  }

  @SuppressWarnings("unchecked")
  public void consultar()  {

    log.info("consultar " + fechaConsulta + " " + cliente);
    final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
    Calendar cal = Calendar.getInstance();
    String fecha = "";

    if (!validaCampos()) {
      cal.setTime(fechaConsulta);
      cal.add(Calendar.DAY_OF_MONTH, 1);
      StringBuilder condicion = new StringBuilder();
      condicion.append(fechaConsulta != null ?
              " trunc(id.fechaProceso) =to_date('" + formato.format(fechaConsulta) + "','dd/mm/yyyy') @" : "");
      condicion.append(cliente != null && !cliente.isEmpty() ?
              " id.campoA='" + cliente + "'@" : "");
      String[] con = condicion.toString().split("@");
      condicion = new StringBuilder();
      for (int i = 0; i < con.length; i++) {
        condicion.append(i == 0 ? con[i] : " and " + con[i]);
      }
      log.info("condicion: " + condicion);

      setListaComparativo(genericService.get("FiHisRepComparativo", condicion.toString(), "order by id.fechaProceso desc "));
      log.info("listaComparativo.size() " + listaComparativo.size());
      setPaginatorVisible(listaComparativo.size() > 6);
      cal.add(Calendar.DAY_OF_MONTH, -1);

      condicion = new StringBuilder(condicion.toString().trim().length() > 1 ? " and " + condicion : "");

      if (fechaConsulta != null) {
        fecha = formato.format(fechaConsulta);
      }

      exportarExcel(condicion.toString(), fecha, cliente);
    }
  }

  public boolean validaCampos() {
    boolean isError = false;
    String error;
    String ui;
    if ((cliente == null) && (fechaConsulta == null)) {
      error = "Campo necesario";
      ui = "form1:consultar";
      isError = true;
      mostrarMensaje(error, true);
      setMensaje("Almenos un campo es necesario.");
    }

    return isError;
  }

  @SuppressWarnings("unchecked")
  public void exportarExcel(final String condicion, final String fecha, final String codigocliente) {

    log.info("Inicia Exportacion Archivo Excel");

    nombreArchivo = UtileriasReportesExcel.borrarExportsExistentes(filePath, nombreArchivo);
    final File archivo = new File(filePath + nombreArchivo + ".xls");
    archivo.setWritable(true);

    UtileriasReportesExcel generaExcel = new UtileriasReportesExcel();

    try (final HSSFWorkbook wb = new HSSFWorkbook()) {
      log.info("Condicion excel =" + condicion + "+" + fecha + "cliente:" + codigocliente);

      final HSSFSheet sheet = wb.createSheet();
      final HSSFRow currentRow = sheet.createRow((short) 1);
      currentRow.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      if (fecha != null && !fecha.isEmpty()) {
        currentRow.createCell(0).setCellValue("Fecha :" + fecha);
      } else if (codigocliente != null) {
        currentRow.createCell(0).setCellValue("Cliente :" + codigocliente);
      } else {
        currentRow.createCell(0).setCellValue("");
      }

      final HSSFRow rowCuentaImp = sheet.createRow((short) 2);
      rowCuentaImp.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      rowCuentaImp.createCell(0).setCellValue(
          "Cuenta :" + ctaImp + "-" + sctaImp + "-" + ssctaImp + "-" + sssctaImp);

      final List<Object> listacontenido =
          (List<Object>) genericService.get("FiHisRepComparativo", " cta='" + ctaImp +
              "' And scta='" + sctaImp + "' and sscta='" + ssctaImp + "' and ssscta='" + sssctaImp + "' " +
              condicion, "order by cuenta, nombreCliente, contrato ");

      final List<String> headers =
          Arrays.asList("Nombre Cliente", "Campo A", "Contrato", "Cuenta", "IMX", "Contabilidad", "Diferencia");

      final List<String> listaNombresColumnas =
          Arrays.asList("nombreCliente", "id.campoA", "id.contrato", "cuenta", "montoImx", "montoSicav", "montoDif");

      generaExcel.generaTablaExcel(wb, sheet, 3, headers, listaNombresColumnas, listacontenido);

      final List<Object> listacontenido2 = (List<Object>) genericService.get("FiHisRepComparativo",
          " cta='" + ctaExp + "' And scta='" + sctaExp + "' and sscta='" + ssctaExp +
              "' and ssscta='" + sssctaExp + "' " + condicion, "order by cuenta, nombreCliente, contrato ");

      final HSSFRow rowCuentaExp = sheet.createRow((short) listacontenido.size() + 5);
      rowCuentaExp.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      rowCuentaExp.createCell(0).setCellValue(
          "Cuenta :" + ctaExp + "-" + sctaExp + "-" + ssctaExp + "-" + sssctaExp);

      generaExcel.generaTablaExcel(
          wb, sheet, listacontenido.size() + 6, headers, listaNombresColumnas, listacontenido2);

      final List<Object> listacontenido3 = (List<Object>) genericService.get("FiHisRepComparativo",
          " cta='" + ctaFiu + "' and scta='" + sctaFiu + "' and sscta='" + ssctaFiu +
              "' and ssscta='" + sssctaFiu + "' " + condicion, "order by cuenta, nombreCliente, contrato ");

      final HSSFRow rowCuentaFiu =
          sheet.createRow((short) listacontenido.size() + 5 + listacontenido2.size() + 3);
      rowCuentaFiu.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
      rowCuentaFiu.createCell(0).setCellValue(
          "Cuenta :" + ctaFiu + "-" + sctaFiu + "-" + ssctaFiu + "-" + sssctaFiu);

      generaExcel.generaTablaExcel(wb, sheet, listacontenido.size() + 5 + listacontenido2.size() + 4,
          headers, listaNombresColumnas, listacontenido3);

      int inicio = listacontenido.size() + 5 + listacontenido2.size() + 3 + listacontenido3.size() + 3;

      for (FiCatCuentasSicav cuentassicav : (List<FiCatCuentasSicav>)
          genericService.get("FiCatCuentasSicav", "", " order by id.cta desc")) {

        final HSSFRow rowCuenta = sheet.createRow((short) inicio);
        rowCuenta.createCell(0).setCellType(Cell.CELL_TYPE_STRING);
        rowCuenta.createCell(0).setCellValue("Cuenta :" + cuentassicav.getId().getCta() + "-" +
            cuentassicav.getId().getScta() + "-" + cuentassicav.getId().getSscta() + "-" +
            cuentassicav.getId().getSsscta());

        final List<Object> listacontenido4 = (List<Object>) genericService.get("FiHisRepComparativo",
            " cta='" + cuentassicav.getId().getCta() + "' and scta='" + cuentassicav.getId().getScta() +
                "' and sscta='" + cuentassicav.getId().getSscta() + "' and ssscta='" + cuentassicav.getId().getSsscta() +
                "' " + condicion, "order by cuenta, nombreCliente, contrato ");

        generaExcel.generaTablaExcel(wb, sheet, inicio + 1, headers, listaNombresColumnas, listacontenido4);
        inicio = inicio + 4 + listacontenido4.size();
      }
      try (final FileOutputStream out = new FileOutputStream(archivo)) {
        wb.write(out);

        excelResourceV = utilerias.downlodaFile(filePath + nombreArchivo + ".xls", nombreArchivo + ".xls");

      }
    } catch (Exception ex) {
      log.info("ERROR " + ex.getMessage());
      mostrarMensaje("Error cerrando Archivo Excel", true);
      log.error("Error Creando Archivo Excel " + ex.getMessage(), ex);
      Utilerias.guardarMensajeLog("ReporteComparativo", "ExportarExcel", ex,
          "Error Creando Archivo Excel", usuarioLogueado);
    }
    log.info("Termina Exportacion Archivo Excel");
  }


  public StreamedContent downlodaFile () throws IOException {

    excelResourceV = null;
    log.info("excelResourceV BEFORE" + excelResourceV);
    final File initialFile = new File(filePath + nombreArchivo + ".xls");
    final InputStream targetStream = Files.newInputStream(initialFile.toPath());
    log.info("archivo existe " + initialFile.exists());

      excelResourceV = DefaultStreamedContent.builder()
              .name("ReporteComparativo.xls")
              .contentType("application/xls")
              .stream(() -> targetStream)
              .build();

      log.info("excelResourceV AFTER " + excelResourceV);

    return  excelResourceV;
  }

  public void updateCalendar() {
    log.info("INICIAL " + fechaConsulta + " cliente " + cliente);
  }

  public void fillProducts() {

      log.info("INTO fillProducts BEFORE FILL");

      products = new ArrayList<>();
      products.add(new Product(1000, "f230fh0g3", "Bamboo Watch", "Product Description", "bamboo-watch.jpg", 65,
              "Accessories", 24, InventoryStatus.INSTOCK, 5));
      products.add(new Product(1001, "nvklal433", "Black Watch", "Product Description", "black-watch.jpg", 72,
              "Accessories", 61, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1002, "zz21cz3c1", "Blue Band", "Product Description", "blue-band.jpg", 79,
              "Fitness", 2, InventoryStatus.LOWSTOCK, 3));
      products.add(new Product(1003, "244wgerg2", "Blue T-Shirt", "Product Description", "blue-t-shirt.jpg", 29,
              "Clothing", 25, InventoryStatus.INSTOCK, 5));
      products.add(new Product(1004, "h456wer53", "Bracelet", "Product Description", "bracelet.jpg", 15,
              "Accessories", 73, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1005, "av2231fwg", "Brown Purse", "Product Description", "brown-purse.jpg", 120,
              "Accessories", 0, InventoryStatus.OUTOFSTOCK, 4));
      products.add(new Product(1006, "bib36pfvm", "Chakra Bracelet", "Product Description", "chakra-bracelet.jpg", 32,
              "Accessories", 5, InventoryStatus.LOWSTOCK, 3));
      products.add(new Product(1007, "mbvjkgip5", "Galaxy Earrings", "Product Description", "galaxy-earrings.jpg", 34,
              "Accessories", 23, InventoryStatus.INSTOCK, 5));
      products.add(new Product(1008, "vbb124btr", "Game Controller", "Product Description", "game-controller.jpg", 99,
              "Electronics", 2, InventoryStatus.LOWSTOCK, 4));
      products.add(new Product(1009, "cm230f032", "Gaming Set", "Product Description", "gaming-set.jpg", 299,
              "Electronics", 63, InventoryStatus.INSTOCK, 3));
      products.add(new Product(1010, "plb34234v", "Gold Phone Case", "Product Description", "gold-phone-case.jpg", 24,
              "Accessories", 0, InventoryStatus.OUTOFSTOCK, 4));
      products.add(new Product(1011, "4920nnc2d", "Green Earbuds", "Product Description", "green-earbuds.jpg", 89,
              "Electronics", 23, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1012, "250vm23cc", "Green T-Shirt", "Product Description", "green-t-shirt.jpg", 49,
              "Clothing", 74, InventoryStatus.INSTOCK, 5));
      products.add(new Product(1013, "fldsmn31b", "Grey T-Shirt", "Product Description", "grey-t-shirt.jpg", 48,
              "Clothing", 0, InventoryStatus.OUTOFSTOCK, 3));
      products.add(new Product(1014, "waas1x2as", "Headphones", "Product Description", "headphones.jpg", 175,
              "Electronics", 8, InventoryStatus.LOWSTOCK, 5));
      products.add(new Product(1015, "vb34btbg5", "Light Green T-Shirt", "Product Description", "light-green-t-shirt.jpg", 49,
              "Clothing", 34, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1016, "k8l6j58jl", "Lime Band", "Product Description", "lime-band.jpg", 79,
              "Fitness", 12, InventoryStatus.INSTOCK, 3));
      products.add(new Product(1017, "v435nn85n", "Mini Speakers", "Product Description", "mini-speakers.jpg", 85,
              "Clothing", 42, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1018, "09zx9c0zc", "Painted Phone Case", "Product Description", "painted-phone-case.jpg", 56,
              "Accessories", 41, InventoryStatus.INSTOCK, 5));
      products.add(new Product(1019, "mnb5mb2m5", "Pink Band", "Product Description", "pink-band.jpg", 79,
              "Fitness", 63, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1020, "r23fwf2w3", "Pink Purse", "Product Description", "pink-purse.jpg", 110,
              "Accessories", 0, InventoryStatus.OUTOFSTOCK, 4));
      products.add(new Product(1021, "pxpzczo23", "Purple Band", "Product Description", "purple-band.jpg", 79,
              "Fitness", 6, InventoryStatus.LOWSTOCK, 3));
      products.add(new Product(1022, "2c42cb5cb", "Purple Gemstone Necklace", "Product Description", "purple-gemstone-necklace.jpg", 45,
              "Accessories", 62, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1023, "5k43kkk23", "Purple T-Shirt", "Product Description", "purple-t-shirt.jpg", 49,
              "Clothing", 2, InventoryStatus.LOWSTOCK, 5));
      products.add(new Product(1024, "lm2tny2k4", "Shoes", "Product Description", "shoes.jpg", 64,
              "Clothing", 0, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1025, "nbm5mv45n", "Sneakers", "Product Description", "sneakers.jpg", 78,
              "Clothing", 52, InventoryStatus.INSTOCK, 4));
      products.add(new Product(1026, "zx23zc42c", "Teal T-Shirt", "Product Description", "teal-t-shirt.jpg", 49,
              "Clothing", 3, InventoryStatus.LOWSTOCK, 3));
      products.add(new Product(1027, "acvx872gc", "Yellow Earbuds", "Product Description", "yellow-earbuds.jpg", 89,
              "Electronics", 35, InventoryStatus.INSTOCK, 3));
      products.add(new Product(1028, "tx125ck42", "Yoga Mat", "Product Description", "yoga-mat.jpg", 20,
              "Fitness", 15, InventoryStatus.INSTOCK, 5));
      products.add(new Product(1029, "gwuby345v", "Yoga Set", "Product Description", "yoga-set.jpg", 20,
              "Fitness", 25, InventoryStatus.INSTOCK, 8));

      log.info("INTO fillProducts AFTER FILL" + products.size());
  }

  public List<Product> getProducts(int size) {

    if (size > products.size()) {
      Random rand = new Random();

      List<Product> randomList = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        int randomIndex = rand.nextInt(products.size());
        randomList.add(products.get(randomIndex));
      }

      return randomList;
    }

    else {
      return new ArrayList<>(products.subList(0, size));
    }

  }

  public List<Product> getClonedProducts(int size) {
    List<Product> results = new ArrayList<>();
    List<Product> originals = getProducts(size);
    for (Product original : originals) {
      results.add(original.clone());
    }

    // make sure to have unique codes
    for (Product product : results) {
      product.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    }

    return results;
  }


}
