package pro.redsoft.openxml;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author V.Solovjev
 */
public interface DigestService {

  void processFromPath(String path) throws DigestServiceException;

  void processFromStream(InputStream source,InputStream der,OutputStream out) throws DigestServiceException;

}
