package org.pentaho.di.core.logging;

import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

public class FileLoggingEventListener implements LoggingEventListener {

  private String filename;
  private FileObject file;
  public FileObject getFile() {
    return file;
  }

  private OutputStream outputStream;
  private Log4jKettleLayout layout;
  
  private KettleException exception;
  
  public FileLoggingEventListener(String filename, boolean append) throws KettleException {
    this.filename = filename;
    this.layout = new Log4jKettleLayout(true);
    this.exception = null;
    
    file = KettleVFS.getFileObject(filename);
    outputStream = null;
    try {
      outputStream = KettleVFS.getOutputStream(file, append);
    } catch(Exception e) {
      throw new KettleException("Unable to create a logging event listener to write to file '"+filename+"'", e);
    }
  }
  
  @Override
  public void eventAdded(LoggingEvent event) {
    
    String logText = layout.format(event);
    
    try {
      outputStream.write( logText.getBytes() );
      outputStream.write( Const.CR.getBytes() );
      
    } catch(Exception e) {
      exception = new KettleException("Unable to write to logging event to file '"+filename+"'", e);
    }

  }
  
  public void close() throws KettleException {
    try {
      if (outputStream!=null) {
        outputStream.close();
      }
    } catch(Exception e) {
      throw new KettleException("Unable to close output of file '"+filename+"'", e);
    }
  }

  public KettleException getException() {
    return exception;
  }

  public void setException(KettleException exception) {
    this.exception = exception;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }
}
