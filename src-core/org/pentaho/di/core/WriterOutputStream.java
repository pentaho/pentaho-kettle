package org.pentaho.di.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * This class provides a simple wrapper to disguise a Writer as an OutputStream.
 * 
 * @author matt
 */
public class WriterOutputStream extends OutputStream {
  private Writer writer;
  private String encoding;
  
  public WriterOutputStream(Writer writer) {
    this.writer = writer;
  }

  public WriterOutputStream(Writer writer, String encoding) {
    this.writer = writer;
    this.encoding = encoding;
  }

  @Override
  public void write(int b) throws IOException {
     write(new byte[] { (byte)b, });
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    byte[] buf = new byte[len];
    System.arraycopy(b, off, buf, 0, len);
    write(buf);
  }
  
  @Override
  public void write(byte[] b) throws IOException {
    if (Const.isEmpty(encoding)) {
      writer.append(new String(b));
    } else {
      writer.append(new String(b, encoding));
    }
  }
  
  @Override
  public void close() throws IOException {
    writer.close();
    writer=null;
  }
  
  @Override
  public void flush() throws IOException {
    writer.flush();
  }
  
  public Writer getWriter() {
    return writer;
  }
  
  public String getEncoding() {
    return encoding;
  }
}
