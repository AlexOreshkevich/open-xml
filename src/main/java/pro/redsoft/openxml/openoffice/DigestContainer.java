/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml.openoffice;

import pro.redsoft.openxml.DigestServiceException;
import pro.redsoft.openxml.logging.DigestLogger;
import pro.redsoft.openxml.logging.LoggingService;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author crzang
 */
public class DigestContainer {

    static final DigestLogger LOG = LoggingService.getLogger(DigestContainer.class);
  List<DigestOperation> lst;
  String prevPara = "";
  int paraNum = 0;
  String doc = "";
  private boolean isStart = false;


  void setParaNum(int paraCounter, String d) {
    paraNum = paraCounter;
    doc = d;
  }


  public DigestContainer() {
    lst = new ArrayList<DigestOperation>();
  }

  void addOperation(String type, String Id, String affer, String before, String val) {
    lst.add(new DigestOperation(type, Id, affer, before, val));
  }

  void addOperation(DigestOperation op) {
    lst.add(op);
  }

  void process() {
  }

  void setPrevPara(String prevPara) {
    this.prevPara = prevPara;
  }

  void exportData(XMLStreamWriter writer) throws DigestServiceException {
    try {
      writer.writeAttribute("paragraph", String.valueOf(paraNum));
        LOG.info("paraNum:" + paraNum);
      writer.writeAttribute("document", doc);
      writer.writeStartElement("text");
      writer.writeAttribute("type", "skeleton");
      resultStr result = new resultStr();
      for (DigestOperation d : lst) {
        d.exportdata(writer, result);
      }

      writer.writeEndElement();
      writer.writeStartElement("text");
      writer.writeAttribute("type", "before");
      writer.writeCharacters(result.before);
      writer.writeEndElement();
      writer.writeStartElement("text");
      writer.writeAttribute("type", "after");
      writer.writeCharacters(result.after);
      writer.writeEndElement();
      writer.writeStartElement("text");
      writer.writeAttribute("type", "mixed");

      if (!prevPara.isEmpty())
        writer.writeCharacters("<p style='color: gray;' >" + prevPara + "</p>");
      writer.writeCharacters("<p>" + result.mixed + "</p>");
      writer.writeEndElement();

        LOG.info( "result.mixed :" + result.mixed);
    } catch (XMLStreamException ex) {
        LOG.error(null, ex);
    }
  }

  void setIsStart() {
    isStart = true;
  }

  public boolean isStart() {
    return isStart;
  }
}
