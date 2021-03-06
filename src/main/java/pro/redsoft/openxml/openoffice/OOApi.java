/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.openoffice;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.*;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.XCloseable;
import pro.redsoft.openxml.DigestServiceException;
import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.LoggingService;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author crzang
 */
public class OOApi {

  static final DigestLogger LOG = LoggingService.getLogger(OOApi.class);
  XDispatchProvider Xdp = null;
  XComponent xComp = null;
  XTextDocument xDoc = null;
  XCloseable xClose = null;
  List<DigestContainer> lstDgst;
  private XComponentLoader xCLoader;
  private XDispatchHelper xDispather;
  private Properties prop = null;
  private List<String> etaloRedlines = new ArrayList<String>();

  public OOApi(XComponentLoader xCLoader, XDispatchHelper xDispather) {
    this.xCLoader = xCLoader;
    this.xDispather = xDispather;
    prop = new Properties();
  }

  boolean init() {
    return true;
  }

  void load(String file2) throws Exception {
    long fileSize = (new File(file2)).length();
    LOG.info("load file :" + file2 + " , filesize :" + fileSize);

    prop.setProperty("derivative", file2);
    PropertyValue[] szArgs = new PropertyValue[3];
    szArgs[0] = new PropertyValue();
    szArgs[0].Name = "Hidden";
    szArgs[0].Value = Boolean.TRUE;
    szArgs[1] = new PropertyValue();
    szArgs[1].Name = "ShowTrackedChanges";
    szArgs[1].Value = Boolean.TRUE;
    szArgs[2] = new PropertyValue();
    szArgs[2].Name = "MacroExecutionMode";
    szArgs[2].Value = com.sun.star.document.MacroExecMode.ALWAYS_EXECUTE_NO_WARN;

    String sUrl = convertToURL(file2);
    try {
      int countFail = 0;
      while (xComp == null) {
        xComp = xCLoader.loadComponentFromURL(sUrl, "_blank", 0, szArgs);
        if (xComp == null) {
          LOG.info("xComp is NULL :" + countFail);
          countFail++;
          if (countFail > 20) {
            throw new RuntimeException("Error init OpenOffice component");
          }
          try {
            Thread.sleep(500);
          } catch (Exception e) {
          }
        }

      }
      countFail = 0;
      while (xDoc == null) {
        xDoc = UnoRuntime.queryInterface(XTextDocument.class, xComp);
        if (xDoc == null) {
          LOG.info("xComp is NULL :" + countFail);
          countFail++;
          if (countFail > 20) {
            throw new RuntimeException("Error init OpenOffice component");
          }
          try {
            Thread.sleep(500);
          } catch (Exception e) {
          }
        }

      }
      LOG.info("done.");
      //  findCorrection(true);
    } catch (Exception ex) {
      LOG.error("",ex);
      throw ex;
    }


  }

  void mergeEtalon(String file1) throws Exception {
    LOG.info("merge with file :" + file1);
    prop.setProperty("etalon", file1);
    try {
      Xdp = UnoRuntime.queryInterface(XDispatchProvider.class, xDoc.getCurrentController().getFrame());

      PropertyValue[] Args1 = new PropertyValue[3];
      Args1[0] = new PropertyValue();
      Args1[0].Name = "Hidden";
      Args1[0].Value = Boolean.TRUE;
      Args1[1] = new PropertyValue();
      Args1[1].Name = "ShowTrackedChanges";
      Args1[1].Value = Boolean.TRUE;
      Args1[2] = new PropertyValue();
      Args1[2].Name = "MacroExecutionMode";
      xDispather.executeDispatch(Xdp, ".uno:ShowTrackedChanges", "", 0, Args1);

      PropertyValue[] Args2 = new PropertyValue[2];
      Args2[0] = new PropertyValue();
      Args2[0].Name = "CompareDocuments";
      Args2[0].Value = Boolean.TRUE;
      Args2[1] = new PropertyValue();
      Args2[1].Name = "URL";
      Args2[1].Value = convertToURL(file1);
      xDispather.executeDispatch(Xdp, ".uno:CompareDocuments", "", 0, Args2);

      PropertyValue[] Args3 = new PropertyValue[1];
      Args3[0] = new PropertyValue();
      Args3[0].Name = "AcceptTrackedChanges";
      Args3[0].Value = Boolean.TRUE;
      xDispather.executeDispatch(Xdp, ".uno:AcceptTrackedChanges", "", 0, Args3);
      LOG.info("done");
    } catch (Exception ex) {
      LOG.error("",ex);
      throw ex;
    }
  }

  void saveMerged(String outdoc) throws DigestServiceException {
    PropertyValue[] Args1 = new PropertyValue[2];
    Args1[0] = new PropertyValue();
    Args1[0].Name = "Overwrite";
    Args1[0].Value = Boolean.TRUE;
    Args1[0] = new PropertyValue();
    Args1[0].Name = "FilterName";
    Args1[0].Value = "MS Word 97";

    XStorable xStore = UnoRuntime.queryInterface(XStorable.class, xComp);
    try {
      xStore.storeAsURL(convertToURL(outdoc), Args1);
    } catch (IOException ex) {
      LOG.error("",ex);
    }

  }

  void exportDgst(OutputStream dgstFile) throws DigestServiceException {
    LOG.info("Export to :" + dgstFile);
    try {
      lstDgst = new ArrayList<DigestContainer>();
      findCorrection(false);
      export(dgstFile);
      LOG.info("done");
    } catch (com.sun.star.uno.Exception ex) {
      LOG.error("",ex);
    }
  }

  String convertToURL(String url) throws DigestServiceException {
    String sUrl = url;
    if (sUrl.indexOf("private:") != 0) {
      try {
        File source = new File(sUrl);
        StringBuffer sbTmp = new StringBuffer("file:///");
        sbTmp.append(source.getCanonicalPath().replace('\\', '/'));
        sUrl = sbTmp.toString();
      } catch (java.io.IOException ex) {
        LOG.error("",ex);
      }
    }
    return sUrl;
  }

  private void findCorrection(boolean isEtalon) throws com.sun.star.uno.Exception, DigestServiceException {
    String etalon = prop.getProperty("etalon");
    if (xDoc != null) {
      XText xText = xDoc.getText();
      com.sun.star.container.XEnumerationAccess xEnumerationAccess =
          (com.sun.star.container.XEnumerationAccess) UnoRuntime.queryInterface(
              com.sun.star.container.XEnumerationAccess.class, xText);
      XEnumeration xEnum = xEnumerationAccess.createEnumeration();
      XTextRange RedlineEnd = null;
      XTextRange RedlineStart = null;
      String beforeTxt = "";
      String afferTxt = "";
      DigestOperation prevoper = null;
      XTextRange prevRedlineEnd = null;
      int paraCounter = 0;
      boolean prevRedlineIsTable = false;
      String prevPara = "";
      boolean tRedline = false;
      boolean hasRedline = true;
      boolean isFirst = !isEtalon;
      boolean startFileAddet = false;
      while (xEnum.hasMoreElements()) {
        if (!isFirst) {
          hasRedline = false;
        }
        LOG.debug("analize paragraph :" + paraCounter);
        tRedline = false;
        DigestContainer dgstCont = null;

        String curTxt = "";
        List<XTextRange> arrTxt = new ArrayList<XTextRange>();
        Object oo = xEnum.nextElement();

        XTextContent xTextElement = UnoRuntime.queryInterface(
            XTextContent.class,
            oo);
        //xTextElement.getAnchor()
        XTextCursor cur = null;
        XTextRange txt = null;

        try {
          txt = xDoc.getText().createTextCursorByRange(xTextElement.getAnchor());
          cur = UnoRuntime.queryInterface(XTextCursor.class, txt);
        } catch (Exception e) {
          Logger.getLogger(OOApi.class.getName()).log(Level.INFO, null, e);
        }
        if (cur != null) {
          XServiceInfo ServInfo = UnoRuntime.queryInterface(XServiceInfo.class, xTextElement.getAnchor());
          List<String> kk=new ArrayList<String>();
                    for(String s:ServInfo.getSupportedServiceNames()){
                     //LOG.debug(s);
                      kk.add(s);
                    }
          if (ServInfo.supportsService("com.sun.star.text.TextTable")) {
            LOG.debug("ServInfo.supportsService(\"com.sun.star.text.TextTable\")");
            XTextTable xTbl = UnoRuntime.queryInterface(XTextTable.class, xTextElement);

            Boolean isStart = false;
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, xTbl);
            String firstThreeRow = getThreeRow(xTbl);

            try {
              PropertyValue[] rline = (PropertyValue[]) xPropSet.getPropertyValue("StartRedline");
              String RedlineIdentifier = "";
              for (int i = 0; i < rline.length; ++i) {
                if (rline[i].Name.equals("IsStart")) {
                  tRedline = true;
                  isStart = (Boolean) rline[i].Value;
                }
                if (rline[i].Name.equals("RedlineIdentifier")) {
                  RedlineIdentifier = (String) rline[i].Value;
                }
                //LOG.debug("Name : " + rline[i].Name + ". Type : " + rline[i].Value.toString());
              }
              String ss = "";
              if (tRedline) {
                LOG.debug("tRedline");
                if (isStart) {
                  LOG.debug("isStart");
                } else {
                  if (!prevRedlineIsTable) {
                    LOG.debug("!prevRedlineIsTable");
                    if (dgstCont == null) {
                      LOG.debug("dgstCont == null");
                      dgstCont = new DigestContainer();
                      dgstCont.setPrevPara(prevPara);
                      dgstCont.setParaNum(paraCounter, etalon);
                    }
                    if (isEtalon) {
                      LOG.debug("isEtalon");
                      etaloRedlines.add(RedlineIdentifier);
                    } else if (!etaloRedlines.contains(RedlineIdentifier)) {
                      DigestOperation oper = new DigestOperation("Modify table", RedlineIdentifier, "Таблица изменена/удалена", "Таблица изменена/удалена", firstThreeRow + "<b>Таблица изменена/удалена</b>");
                      dgstCont.addOperation(oper);
                    }
                    prevoper = null;
                    prevRedlineEnd = null;
                    beforeTxt = "";
                    afferTxt = "";
                    RedlineEnd = null;
                    RedlineStart = null;
                    prevRedlineIsTable = true;
                  }
                  prevRedlineIsTable = false;

                }
              }
            } catch (Exception e) {
              //  e.printStackTrace();
              LOG.error("",e);
            }
            if (!tRedline) {
              LOG.debug("!tRedline");
              prevPara = firstThreeRow;
            }

          } else {
            LOG.debug("!ServInfo.supportsService(\"com.sun.star.text.TextTable\")");
            XParagraphCursor xpara = UnoRuntime.queryInterface(XParagraphCursor.class, cur);
            //if(!xpara.isStartOfParagraph())
            xpara.gotoStartOfParagraph(false);
            xpara.gotoEndOfParagraph(true);
            curTxt = xpara.getString();

            XEnumerationAccess xParaEnumerationAccess = UnoRuntime.queryInterface(
                XEnumerationAccess.class,
                xTextElement);
            if (xParaEnumerationAccess != null) {
              LOG.debug("xParaEnumerationAccess != null");
              XEnumeration xTextPortionEnum = xParaEnumerationAccess.createEnumeration();
              while (xTextPortionEnum.hasMoreElements()) {
                com.sun.star.text.XTextRange xTextPortion =
                    (com.sun.star.text.XTextRange) UnoRuntime.queryInterface(
                        com.sun.star.text.XTextRange.class,
                        xTextPortionEnum.nextElement());
                arrTxt.add(xTextPortion);
              }
            }

          }
          XTextField annotation = null;
          for (XTextRange xTextPortion : arrTxt) {


            cur.gotoRange(xTextPortion.getStart(), false);
            cur.gotoRange(xTextPortion.getEnd(), true);
            String valueCurPortion = cur.getString();

            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(
                XPropertySet.class, xTextPortion);
            XPropertySetInfo xPropSetInfo = xPropSet.getPropertySetInfo();

            Property[] pr = xPropSetInfo.getProperties();

            if (xPropSetInfo.hasPropertyByName("RedlineType")) {

              LOG.debug("xPropSetInfo.hasPropertyByName(\"RedlineType\")");
              try {
                String RedlineIdentifier = (String) xPropSet.getPropertyValue("RedlineIdentifier");
                String RedlineType = (String) xPropSet.getPropertyValue("RedlineType");
                Boolean isStart = (Boolean) xPropSet.getPropertyValue("IsStart");
                if (isStart) {
                  LOG.debug("isStart");
                  hasRedline = true;
                  RedlineStart = xTextPortion;
                  if (prevRedlineEnd == null) {
                    cur.gotoRange(xTextPortion.getStart(), false);
                    cur.gotoRange(xTextElement.getAnchor().getStart(), true);
                  } else {
                    cur.gotoRange(prevRedlineEnd.getEnd(), false);
                    cur.gotoRange(xTextPortion.getStart(), true);
                  }
                  beforeTxt = cur.getString();
                  if (prevoper != null) {
                    LOG.debug("prevoper != null");
                    prevoper.setAffetTxt(beforeTxt);
                  }

                } else {
                  LOG.debug("!isStart");
                  RedlineEnd = xTextPortion;
                  String redlineTXT = "";
                  boolean skip = false;
                  if (RedlineStart != null) {
                    LOG.debug("RedlineStart != null");
                    cur.gotoRange(RedlineStart.getStart(), false);
                    cur.gotoRange(RedlineEnd.getEnd(), true);
                    redlineTXT = cur.getString();


                  } else {
                    LOG.debug("RedlineStart == null");
                    cur.gotoRange(xTextPortion.getStart(), false);
                    cur.gotoRange(RedlineEnd.getEnd(), true);
                    redlineTXT = cur.getString();
                    skip = true;

                  }

                  if (!skip) {
                    LOG.debug("!skip");
                    XParagraphCursor xpara = UnoRuntime.queryInterface(XParagraphCursor.class, cur);
                    xpara.gotoRange(RedlineEnd.getStart(), false);
                    boolean needAffer = true;
                    if (xpara.isEndOfParagraph()) {
                      afferTxt = "";
                      needAffer = false;
                    }
                    if (xpara.isStartOfParagraph()) {
                      afferTxt = "";
                      needAffer = false;
                    }
                    if (needAffer) {
                      xpara.gotoEndOfParagraph(true);
                      afferTxt = xpara.getString();
                    }
                    if (dgstCont == null) {
                      LOG.debug("dgstCont == null");
                      dgstCont = new DigestContainer();
                      if (prevPara.trim().isEmpty() && paraCounter == 0) {

                        dgstCont.setPrevPara("НАЧАЛО ФАЙЛА");
                        dgstCont.setIsStart();

                      } else {
                        dgstCont.setPrevPara(prevPara);
                      }

                      dgstCont.setParaNum(paraCounter, etalon);
                    }
                    if (annotation != null && redlineTXT.isEmpty()) {
                      LOG.debug("annotation != null && redlineTXT.isEmpty()");
                      XPropertySet xAnnotationProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, annotation);
                      //LOG.debug(xAnnotationProps.getPropertyValue("Author")+";"+xAnnotationProps.getPropertyValue("Content"));
                      RedlineType += " annotation";
                      redlineTXT = (String) xAnnotationProps.getPropertyValue("Content");
                    }
                    if (isEtalon) {
                      LOG.debug("isEtalon");
                      etaloRedlines.add(RedlineIdentifier);
                    } else if (!etaloRedlines.contains(RedlineIdentifier)) {
                      LOG.debug("!etaloRedlines.contains(RedlineIdentifier)");
                      DigestOperation oper = new DigestOperation(RedlineType, RedlineIdentifier, afferTxt, beforeTxt, redlineTXT);
                      prevoper = oper;
                      dgstCont.addOperation(oper);
                      prevRedlineEnd = RedlineEnd;

                    }
                    beforeTxt = "";
                    afferTxt = "";
                    RedlineEnd = null;
                    RedlineStart = null;
                    prevRedlineIsTable = false;

                  } else {
                    LOG.debug("skip");
                  }
                }

              } catch (Exception ex) {
                LOG.error("",ex);
              }
              //prevPara = "";
              // hasRedline = true;
            } else {
              LOG.debug("!xPropSetInfo.hasPropertyByName(\"RedlineType\")");
              //if(!curTxt.isEmpty())
            }
            annotation = UnoRuntime.queryInterface(XTextField.class, xPropSet.getPropertyValue("TextField"));
          }
          if (dgstCont
              != null && !isEtalon) {
            if (dgstCont.isStart()) {
              if (!startFileAddet) {
                lstDgst.add(dgstCont);
              }
              startFileAddet = true;
            } else {
              lstDgst.add(dgstCont);
            }
            prevoper = null;
            prevRedlineEnd = null;
          }

        } else {
          LOG.debug("cur == null");
        }
        if (!hasRedline && !curTxt.trim().isEmpty() && !tRedline) {
          prevPara = curTxt;
        }
        if (!hasRedline || isFirst) {
          paraCounter++;
        }
        if (isFirst) {
          isFirst = false;
        }
      }
      //xDoc.dispose();
    } else {
      LOG.debug("xDoc is null");
    }
  }

  void close() throws DigestServiceException {
    LOG.info("try close");
    try {
      Xdp = null;
      xClose = UnoRuntime.queryInterface(XCloseable.class, xComp);
      if (xClose
          != null) {
        xClose.close(false);
      } else {
        if (xComp != null) {
          xComp.dispose();
        }
      }
      if (xDoc != null) {
        //xDoc.dispose();
      }
    } catch (Exception ex) {
      LOG.error("",ex);
    }
  }

  private void export(OutputStream dgstFile) throws DigestServiceException {
    try {
      prop.setProperty("BlockCount", String.valueOf(lstDgst.size()));
      Calendar c = Calendar.getInstance();
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      prop.setProperty("ReportTime", dateFormat.format(c.getTime()));

      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      XMLStreamWriter writer =
          factory.createXMLStreamWriter(
            dgstFile, "UTF-8");

      writer.writeStartDocument();
      writer.writeStartElement("diffdigest");
      writer.writeStartElement("report");
      writer.writeStartElement("params");
      Enumeration<Object> keys = prop.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        writer.writeStartElement("param");
        writer.writeAttribute("type", "file");
        writer.writeAttribute("name", key);
        writer.writeAttribute("value", prop.getProperty(key));
        writer.writeEndElement();
      }
      writer.writeEndElement();
      writer.writeEndElement();
      writer.writeStartElement("redlines");
      LOG.info("count redline :" + lstDgst.size());
      for (DigestContainer d : lstDgst) {
        writer.writeStartElement("redline");
        writer.writeAttribute("block-id", String.valueOf(lstDgst.indexOf(d)));
        d.exportData(writer);
        writer.writeEndElement();
      }
      writer.writeEndElement();
      writer.writeEndDocument();

      writer.flush();
      writer.close();
      dgstFile.close();
    } catch (Exception ex) {
      LOG.error("",ex);
    }
  }

  private String getThreeRow(XTextTable xTbl) throws DigestServiceException {
    StringBuilder sb = new StringBuilder();
    sb.append("<table border='1'>");
    int rowCount = 0;
    String[] Cells = xTbl.getCellNames();
    boolean firstRow = true;

    try {
      for (String name : Cells) {
        int curRow = Integer.valueOf(name.replaceAll("\\D+", ""));
        if (curRow != rowCount) {
          if (!firstRow) {
            sb.append("</tr>");
          }
          firstRow = false;
          sb.append("<tr>");
          rowCount = curRow;
          if (rowCount > 4) {
            break;
          }
        }
        sb.append("<td>");
        XText xtext = UnoRuntime.queryInterface(XText.class, xTbl.getCellByName(name));
        //arrTxt.add(xtext);

        XEnumerationAccess xParaEnumerationAccess = UnoRuntime.queryInterface(
            XEnumerationAccess.class,
            xtext);
        String aText;
        if (xParaEnumerationAccess != null) {
          XEnumeration xTextPortionEnum = xParaEnumerationAccess.createEnumeration();
          while (xTextPortionEnum.hasMoreElements()) {

            aText = "";
            XEnumerationAccess xPortionEA = (XEnumerationAccess) UnoRuntime.queryInterface(
                XEnumerationAccess.class, xTextPortionEnum.nextElement());
            XEnumeration xPortionEnum = xPortionEA.createEnumeration();
            while (xPortionEnum.hasMoreElements()) {
              XTextRange xRange = UnoRuntime.queryInterface(XTextRange.class, xPortionEnum.nextElement());
              aText += xRange.getString();
              sb.append(xRange.getString());
              if (xRange != null) {
                //arrTxt.add(xRange);
              }
            }
            //      LOG.info("Paragraph text: " + aText);

          }

        }
        sb.append("</td>");
      }
    } catch (Exception ex) {
      LOG.error("",ex);
    }
    sb.append("</tr>");
    sb.append("</table>");
    return sb.toString();
  }
}