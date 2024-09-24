/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.core.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML formatting for better VCS diff.
 * 
 * It preserve formatting only in cases: 1) inside one tag if there are only characters, 2) in comments, 3) if there are
 * some characters outside tags
 *
 * @author Alexander Buloichik
 */
public class XMLFormatter {
  private static final String STEP_PREFIX = "  ";

  private static XMLInputFactory INPUT_FACTORY = XMLInputFactory.newInstance();
  private static XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newInstance();

  static {
    INPUT_FACTORY.setProperty( XMLInputFactory.IS_COALESCING, false );
  }

  public static String format( String xml ) {
    XMLStreamReader rd = null;
    XMLStreamWriter wr = null;

    StringWriter result = new StringWriter();
    try {
      rd = INPUT_FACTORY.createXMLStreamReader( new StringReader( xml ) );

      synchronized ( OUTPUT_FACTORY ) {
        // BACKLOG-18743: This object was not thread safe in some scenarios
        // causing the `result` variable to have data from other concurrent executions
        // and making the final output invalid.
        wr = OUTPUT_FACTORY.createXMLStreamWriter( result );
      }

      StartElementBuffer startElementBuffer = null;
      StringBuilder str = new StringBuilder();
      StringBuilder prefix = new StringBuilder();
      StringBuilder cdata = new StringBuilder();
      boolean wasStart = false;
      boolean wasSomething = false;
      while ( rd.hasNext() ) {
        int event = rd.next();
        if ( event != XMLStreamConstants.CDATA && cdata.length() > 0 ) {
          // was CDATA
          wr.writeCData( cdata.toString() );
          cdata.setLength( 0 );
        }

        if ( startElementBuffer != null ) {
          if ( event == XMLStreamConstants.END_ELEMENT ) {
            startElementBuffer.writeTo( wr, true );
            startElementBuffer = null;
            prefix.setLength( prefix.length() - STEP_PREFIX.length() );
            wasStart = false;
            continue;
          } else {
            startElementBuffer.writeTo( wr, false );
            startElementBuffer = null;
          }
        }

        switch ( event ) {
          case XMLStreamConstants.START_ELEMENT:
            if ( !whitespacesOnly( str ) ) {
              wr.writeCharacters( str.toString() );
            } else if ( wasSomething ) {
              wr.writeCharacters( "\n" + prefix );
            }
            str.setLength( 0 );
            prefix.append( STEP_PREFIX );
            startElementBuffer = new StartElementBuffer( rd );
            wasStart = true;
            wasSomething = true;
            break;
          case XMLStreamConstants.END_ELEMENT:
            prefix.setLength( prefix.length() - STEP_PREFIX.length() );
            if ( wasStart ) {
              wr.writeCharacters( str.toString() );
            } else {
              if ( !whitespacesOnly( str ) ) {
                wr.writeCharacters( str.toString() );
              } else {
                wr.writeCharacters( "\n" + prefix );
              }
            }
            str.setLength( 0 );
            wr.writeEndElement();
            wasStart = false;
            break;
          case XMLStreamConstants.SPACE:
          case XMLStreamConstants.CHARACTERS:
            str.append( rd.getText() );
            break;
          case XMLStreamConstants.CDATA:
            if ( !whitespacesOnly( str ) ) {
              wr.writeCharacters( str.toString() );
            }
            str.setLength( 0 );
            cdata.append( rd.getText() );
            wasSomething = true;
            break;
          case XMLStreamConstants.COMMENT:
            if ( !whitespacesOnly( str ) ) {
              wr.writeCharacters( str.toString() );
            } else if ( wasSomething ) {
              wr.writeCharacters( "\n" + prefix );
            }
            str.setLength( 0 );
            wr.writeComment( rd.getText() );
            wasSomething = true;
            break;
          case XMLStreamConstants.END_DOCUMENT:
            wr.writeCharacters( "\n" );
            wr.writeEndDocument();
            break;
          default:
            throw new RuntimeException( "Unknown XML event: " + event );
        }
      }

      wr.flush();

      return result.toString();
    } catch ( XMLStreamException ex ) {
      throw new RuntimeException( ex );
    } finally {
      try {
        if ( wr != null ) {
          wr.close();
        }
      } catch ( Exception ex ) {
      }
      try {
        if ( rd != null ) {
          rd.close();
        }
      } catch ( Exception ex ) {
      }
    }
  }

  /**
   * Storage for start element info. It required since elements can be empty, i.e. we should call writeEmptyElement for
   * writer instead writeStartElement.
   */
  private static class StartElementBuffer {
    String prefix;
    String namespace;
    String localName;
    List<AttrBuffer> attrBuffer = new ArrayList<>();

    public StartElementBuffer( XMLStreamReader rd ) {
      prefix = rd.getPrefix();
      namespace = rd.getNamespaceURI();
      localName = rd.getLocalName();
      for ( int i = 0; i < rd.getAttributeCount(); i++ ) {
        attrBuffer.add( new AttrBuffer( rd, i ) );
      }
    }

    public void writeTo( XMLStreamWriter wr, boolean empty ) throws XMLStreamException {
      if ( empty ) {
        if ( namespace != null ) {
          wr.writeEmptyElement( prefix, localName, namespace );
        } else {
          wr.writeEmptyElement( localName );
        }
      } else {
        if ( namespace != null ) {
          wr.writeStartElement( prefix, localName, namespace );
        } else {
          wr.writeStartElement( localName );
        }
      }
      for ( AttrBuffer a : attrBuffer ) {
        a.writeTo( wr );
      }
    }
  }

  private static class AttrBuffer {
    String prefix;
    String namespace;
    String localName;
    String value;

    public AttrBuffer( XMLStreamReader rd, int attrIndex ) {
      prefix = rd.getAttributePrefix( attrIndex );
      namespace = rd.getAttributeNamespace( attrIndex );
      localName = rd.getAttributeLocalName( attrIndex );
      value = rd.getAttributeValue( attrIndex );
    }

    public void writeTo( XMLStreamWriter wr ) throws XMLStreamException {
      if ( namespace != null ) {
        wr.writeAttribute( prefix, namespace, localName, value );
      } else {
        wr.writeAttribute( localName, value );
      }
    }
  }

  private static boolean whitespacesOnly( StringBuilder str ) {
    for ( int i = 0; i < str.length(); i++ ) {
      if ( !Character.isWhitespace( str.charAt( i ) ) ) {
        return false;
      }
    }
    return true;
  }
}
