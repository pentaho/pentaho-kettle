/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RepositoryExportSaxParser extends DefaultHandler2 {

  public static final String STRING_REPOSITORY = "repository";
  public static final String STRING_TRANSFORMATIONS = "transformations";
  public static final String STRING_TRANSFORMATION = "transformation";
  public static final String STRING_JOBS = "jobs";
  public static final String STRING_JOB = "job";

  private SAXParser saxParser;

  private RepositoryElementReadListener repositoryElementReadListener;

  @VisibleForTesting final StringBuilder xml;
  private final String filename;

  private boolean add;
  private boolean cdata;

  RepositoryImportFeedbackInterface feedback;

  public RepositoryExportSaxParser( String filename, RepositoryImportFeedbackInterface feedback ) {
    this.filename = filename;
    this.feedback = feedback;
    this.xml = new StringBuilder( 50000 );
    this.add = false;
    this.cdata = false;
  }

  public void parse( RepositoryElementReadListener repositoryElementReadListener )
    throws SAXException, ParserConfigurationException, IOException {
    this.repositoryElementReadListener = repositoryElementReadListener;

    SAXParserFactory factory = XMLParserFactoryProducer.createSecureSAXParserFactory();
    this.saxParser = factory.newSAXParser();
    this.saxParser.parse( new File( filename ), this );
  }

  @Override public void startElement( String uri, String localName, String qName, Attributes attributes ) {
    add =
      !( STRING_REPOSITORY.equals( qName ) || STRING_TRANSFORMATIONS.equals( qName ) || STRING_JOBS.equals( qName ) );

    if ( add ) {

      // A new job or transformation?
      //
      if ( STRING_TRANSFORMATION.equals( qName ) || STRING_JOB.equals( qName ) ) {
        xml.setLength( 0 );
      }

      Map<String, String> attMap = Collections.emptyMap();
      if ( attributes != null && attributes.getLength() > 0 ) {
        attMap = IntStream.range( 0, attributes.getLength() )
          .boxed()
          .collect( Collectors.toMap( attributes::getQName, attributes::getValue ) );
      }

      XMLHandler.openTag( xml, qName, attMap );
    }
  }

  @Override public void endElement( String uri, String localName, String qName ) {
    if ( add ) {
      XMLHandler.closeTag( xml, qName );
    }

    if ( STRING_TRANSFORMATION.equals( qName ) ) {
      if ( !repositoryElementReadListener.transformationElementRead( xml.toString(), feedback ) ) {
        saxParser.reset();
      }
    } else if ( STRING_JOB.equals( qName )
      && !repositoryElementReadListener.jobElementRead( xml.toString(), feedback ) ) {
      saxParser.reset();
    }
  }

  @Override public void startCDATA() {
    cdata = true;
  }

  @Override public void endCDATA() {
    cdata = false;
  }

  @Override public void characters( char[] ch, int start, int length ) {
    if ( add ) {

      String string = new String( ch, start, length );

      if ( cdata ) {
        XMLHandler.buildCDATA( xml, string );
      } else {
        XMLHandler.appendReplacedChars( xml, string );
      }
    }
  }

  @Override
  public void fatalError( SAXParseException e ) {
    repositoryElementReadListener.fatalXmlErrorEncountered( e );
  }
}
