/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author John
 */
public class digestContainer {

  List<digestOperation> lst;
  String prevPara = "";
  int paraNum = 0;
  String doc = "";
  private boolean isStart = false;


  void setParaNum(int paraCounter, String d) {
    paraNum = paraCounter;
    doc = d;
  }


  public digestContainer() {
    lst = new ArrayList<digestOperation>();
  }

  void addOperation(String type, String Id, String affer, String before, String val) {
    lst.add(new digestOperation(type, Id, affer, before, val));
  }

  void addOperation(digestOperation op) {
    lst.add(op);
  }

  void process() {
  }

  void setPrevPara(String prevPara) {
    this.prevPara = prevPara;
  }

  void exportData(XMLStreamWriter writer) {
    try {
      writer.writeAttribute("paragraph", String.valueOf(paraNum));
      Logger.getLogger(digestContainer.class.getName()).log(Level.INFO, "paraNum:" + paraNum);
      writer.writeAttribute("document", doc);
      writer.writeStartElement("text");
      writer.writeAttribute("type", "skeleton");
      resultStr result = new resultStr();
      for (digestOperation d : lst) {
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

      Logger.getLogger(digestContainer.class.getName()).log(Level.FINE, "result.mixed :" + result.mixed);
    } catch (XMLStreamException ex) {
      Logger.getLogger(digestContainer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  void setIsStart() {
    isStart = true;
  }

  public boolean isStart() {
    return isStart;
  }
}
