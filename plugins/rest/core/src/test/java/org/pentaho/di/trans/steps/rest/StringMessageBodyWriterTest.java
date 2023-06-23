package org.pentaho.di.trans.steps.rest;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public class StringMessageBodyWriterTest {


  private StringMessageBodyWriter stringMessageBodyWriter;

  @Before
  public void init() {
    stringMessageBodyWriter = new StringMessageBodyWriter();
  }

  @Test
  public void verifyIfNullStringReturnsMinusOne() {
    long size = stringMessageBodyWriter.getSize( null, null, null, null, null );
    assertEquals( -1, size );
  }

  @Test
  public void verifyIfEmptyStringReturnsZero() {
    long size = stringMessageBodyWriter.getSize( "", null, null, null, null );
    assertEquals( 0, size );
  }

  @Test
  public void verifyIfStringReturnCorrectLength() {
    String entity = "0123456789";
    long size = stringMessageBodyWriter.getSize( entity, null, null, null, null );
    assertEquals( entity.getBytes().length, size );
  }

}
