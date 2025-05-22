package com.pentaho.di.repovfs.vfs;

import com.pentaho.di.repovfs.repo.RepositoryClient;
import com.pentaho.di.repovfs.repo.RepositoryClient.RepositoryClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will hold the whole content in memory until transferring data on close.
 */
public class JCRFileOutputStream extends ByteArrayOutputStream {

  private static final Logger log = LoggerFactory.getLogger( JCRFileOutputStream.class );

  private final RepositoryClient client;
  private final String[] name;

  private boolean closed = false;

  public JCRFileOutputStream( String[] fileName, RepositoryClient client ) {
    this.name = fileName;
    this.client = client;
  }

  @Override
  public void close() throws IOException {
    if ( closed ) {
      throw new IOException( new IllegalStateException( "Already closed" ) );
    }

    closed = true;
    try ( ByteArrayInputStream bais = new ByteArrayInputStream( toByteArray() ) ) {
      client.writeData( name, bais );
    } catch ( RepositoryClientException e ) {
      // VFS can save an empty file before saving with the content,
      // and that will fail for ktr/kjb but still be ok
      log.error( e.getMessage(), e );
    }
  }

}
