package org.pentaho.di.core.xml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.pentaho.di.core.xml.XMLHandler.buildCDATA;
import static org.pentaho.di.core.xml.XMLHandler.openTag;

/**
 */
public class XMLHandlerUnitTest {

  @Test
  public void openTagWithNotNull() {
    assertEquals( "<qwerty>", openTag( "qwerty" ) );
  }

  @Test
  public void openTagWithNull() {
    assertEquals( "<null>", openTag( null ) );
  }

  @Test
  public void openTagWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    openTag( builder, "rty" );
    assertEquals( "qwe<rty>", builder.toString() );
  }


  @Test
  public void buildCdataWithNotNull() {
    assertEquals( "<![CDATA[qwerty]]>", buildCDATA( "qwerty" ) );
  }

  @Test
  public void buildCdataWithNull() {
    assertEquals( "<![CDATA[]]>", buildCDATA( null ) );
  }

  @Test
  public void buildCdataWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    buildCDATA( builder, "rty" );
    assertEquals( "qwe<![CDATA[rty]]>", builder.toString() );
  }
}
