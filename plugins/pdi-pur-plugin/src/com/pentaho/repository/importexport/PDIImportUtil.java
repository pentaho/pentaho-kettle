/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.repository.importexport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.utils.IRepositoryFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PDIImportUtil {

  private static IRepositoryFactory repositoryFactory = new IRepositoryFactory.CachingRepositoryFactory();

  /**
   * Connects to the PDI repository
   * 
   * @param repositoryName
   * @return
   * @throws KettleException
   * @throws KettleSecurityException
   * @throws ActionExecutionException
   */
  public static Repository connectToRepository( String repositoryName ) throws KettleException {
    return repositoryFactory.connect( repositoryName );
  }

  public static void setRepositoryFactory( IRepositoryFactory factory ) {
    repositoryFactory = factory;
  }

  public static Document loadXMLFrom( String xml ) throws SAXException, IOException {
    return loadXMLFrom( new ByteArrayInputStream( xml.getBytes() ) );
  }

  public static Document loadXMLFrom( InputStream is ) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    Document doc = null;
    try {
      builder = factory.newDocumentBuilder();
    } catch ( ParserConfigurationException ex ) {
      // ignore
    }
    try {
      File file = File.createTempFile( "tempFile", "temp" );
      file.deleteOnExit();
      FileOutputStream fous = new FileOutputStream( file );
      IOUtils.copy( is, fous );
      fous.flush();
      fous.close();
      doc = builder.parse( file );
      is.close();
    } catch ( Throwable th ) {
      try {
        is.close();
      } catch ( IOException e ) {
      }
    }
    return doc;
  }

  public static String asXml( Document document ) {
    try {
      Source source = new DOMSource( document.getParentNode() );
      StringWriter stringWriter = new StringWriter();
      Result result = new StreamResult( stringWriter );
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      transformer.transform( source, result );
      return stringWriter.getBuffer().toString();
    } catch ( TransformerConfigurationException e ) {
      e.printStackTrace();
      return null;
    } catch ( TransformerException e ) {
      e.printStackTrace();
      return null;
    }
  }

  public static String checkAndSanitize( final String in ) {
    if ( in == null ) {
      throw new IllegalArgumentException();
    }
    String extension = null;
    if ( in.endsWith( RepositoryObjectType.CLUSTER_SCHEMA.getExtension() ) ) {
      extension = RepositoryObjectType.CLUSTER_SCHEMA.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.DATABASE.getExtension() ) ) {
      extension = RepositoryObjectType.DATABASE.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.JOB.getExtension() ) ) {
      extension = RepositoryObjectType.JOB.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.PARTITION_SCHEMA.getExtension() ) ) {
      extension = RepositoryObjectType.PARTITION_SCHEMA.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.SLAVE_SERVER.getExtension() ) ) {
      extension = RepositoryObjectType.SLAVE_SERVER.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.TRANSFORMATION.getExtension() ) ) {
      extension = RepositoryObjectType.TRANSFORMATION.getExtension();
    }
    String out = in;
    if ( extension != null ) {
      out = out.substring( 0, out.length() - extension.length() );
    }
    if ( out.contains( "/" ) || out.equals( ".." ) || out.equals( "." ) || StringUtils.isBlank( out ) ) {
      throw new IllegalArgumentException();
    }
    out = out.replaceAll( "[/:\\[\\]\\*'\"\\|\\s\\.]", "_" ); //$NON-NLS-1$//$NON-NLS-2$
    if ( extension != null ) {
      return out + extension;
    } else {
      return out;
    }
  }

}
