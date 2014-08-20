/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.openoffice;

import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.LoggingService;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author crzang
 */
public class DigestOperation {

  static final DigestLogger LOG = LoggingService.getLogger(DigestOperation.class);
  String type;
  String redlineid;
  String afterTxt;
  String beforeTxt;
  String curTxt;

  public DigestOperation(String type, String redlineid, String affetTxt, String beforeTxt, String curTxt) {
    this.type = type;
    this.redlineid = redlineid;
    this.afterTxt = affetTxt;
    this.beforeTxt = beforeTxt;
    this.curTxt = curTxt;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getRedlineid() {
    return redlineid;
  }

  public void setRedlineid(String redlineid) {
    this.redlineid = redlineid;
  }

  public String getAffetTxt() {
    return afterTxt;
  }

  public void setAffetTxt(String affetTxt) {
    this.afterTxt = affetTxt;
  }

  public String getBeforeTxt() {
    return beforeTxt;
  }

  public void setBeforeTxt(String beforeTxt) {
    this.beforeTxt = beforeTxt;
  }

  public String getCurTxt() {
    return curTxt;
  }

  public void setCurTxt(String curTxt) {
    this.curTxt = curTxt;
  }

  void printdata() {
    LOG.info("before : \n" + beforeTxt);
    LOG.info("operation : " + type + ". redlineid" + redlineid);
    LOG.info("value : \n" + curTxt);
    LOG.info("affer : \n" + afterTxt);
    LOG.info("----------------");
  }

  void exportdata(XMLStreamWriter writer, resultStr result) {
    try {
      if (!beforeTxt.isEmpty()) {
        writer.writeStartElement("section");
        writer.writeAttribute("type", "text");
        writer.writeCharacters(beforeTxt);
        writer.writeEndElement();
      }
      writer.writeStartElement("section");
      writer.writeAttribute("type", type.toLowerCase());
      writer.writeAttribute("id", redlineid);
      writer.writeCharacters(curTxt);
      writer.writeEndElement();
      if (!afterTxt.isEmpty()) {
        writer.writeStartElement("section");
        writer.writeAttribute("type", "text");
        writer.writeCharacters(afterTxt);
        writer.writeEndElement();
      }
      if (type.toLowerCase().equals("insert")) {
        result.before += beforeTxt + afterTxt;
        result.after += beforeTxt + curTxt + afterTxt;
        if (curTxt.trim().isEmpty())
          result.mixed += beforeTxt + "<u>Вставлен(ы) пустой параграф</u>" + afterTxt;
        else
          result.mixed += beforeTxt + "<u>" + curTxt + "</u>" + afterTxt;
      }
      if (type.toLowerCase().equals("delete")) {
        result.before += beforeTxt + curTxt + afterTxt;
        result.after += beforeTxt + afterTxt;
        if (curTxt.trim().isEmpty())
          result.mixed += beforeTxt + "<s>Удален(ы) пустой параграф</s>" + afterTxt;
        else
          result.mixed += beforeTxt + "<s>" + curTxt + "</s>" + afterTxt;
      }
      if (type.toLowerCase().equals("insert annotation")) {
        result.before += beforeTxt + afterTxt;
        result.after += beforeTxt + curTxt + afterTxt;

        result.mixed += beforeTxt + "<u> Вставлен комментарий : '" + curTxt + "' </u>" + afterTxt;
      }
      if (type.toLowerCase().equals("delete annotation")) {
        result.before += beforeTxt + curTxt + afterTxt;
        result.after += beforeTxt + afterTxt;
        result.mixed += beforeTxt + "<s> Удален комментарий :" + curTxt + "</s>" + afterTxt;
      }
      if (type.toLowerCase().equals("modify table")) {
        result.after = "Таблица изменена/удалена";
        result.before = "Таблица изменена/удалена";
        result.mixed = curTxt;
      }
    } catch (XMLStreamException ex) {
      Logger.getLogger(DigestOperation.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


}
