package org.pentaho.di.trans.steps.rest;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StringMessageBodyWriterTest {

  @Test
  public void getSize() {
    checkGetSize( "0123456789" );
    checkGetSize( null );
    checkGetSize( "" );
  }

  private void checkGetSize( String entity ) {
    StringMessageBodyWriter stringMessageBodyWriter = mock( StringMessageBodyWriter.class );
    doReturn( Long.valueOf( entity == null ? -1 : entity.length() ) ).when( stringMessageBodyWriter ).getSize( any(), any(), any(), any(), any() );
    long size = stringMessageBodyWriter.getSize( entity, null, null, null, null );
    assertEquals( entity == null ? -1 : entity.length(), size );
  }
}