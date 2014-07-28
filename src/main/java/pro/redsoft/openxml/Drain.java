/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pro.redsoft.openxml;

import java.io.IOException;
import java.io.InputStream;

final class Drain extends Thread {

  public Drain(InputStream stream) {
    super("unoinfo stderr drain");
    this.stream = stream;
  }

  public void run() {
    try {
      while (stream.read() != -1) {
      }
    } catch (IOException e) { /* ignored */ }
  }

  private final InputStream stream;
}