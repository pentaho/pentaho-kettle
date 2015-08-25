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
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.platform.api.engine.ActionExecutionException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PDIImportUtil {

  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance"; //$NON-NLS-1$

  /**
   * Connects to the PDI repository
   * 
   * @param logWriter
   * @return
   * @throws KettleException
   * @throws KettleSecurityException
   * @throws ActionExecutionException
   */
  public static Repository connectToRepository( String repositoryName ) throws KettleException {

    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    boolean singleDiServerInstance =
        "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      if ( singleDiServerInstance ) {

        // only load a default enterprise repository. If this option is set, then you cannot load
        // transformations or jobs from anywhere but the local server.

        String repositoriesXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><repositories>" //$NON-NLS-1$
                + "<repository><id>PentahoEnterpriseRepository</id>" //$NON-NLS-1$
                + "<name>" + SINGLE_DI_SERVER_INSTANCE + "</name>" //$NON-NLS-1$ //$NON-NLS-2$
                + "<description>" + SINGLE_DI_SERVER_INSTANCE + "</description>" //$NON-NLS-1$ //$NON-NLS-2$
                + "<repository_location_url>" + PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + "</repository_location_url>" //$NON-NLS-1$ //$NON-NLS-2$
                + "<version_comment_mandatory>N</version_comment_mandatory>" //$NON-NLS-1$
                + "</repository>" //$NON-NLS-1$
                + "</repositories>"; //$NON-NLS-1$

        ByteArrayInputStream sbis = new ByteArrayInputStream( repositoriesXml.getBytes( "UTF8" ) );
        repositoriesMeta.readDataFromInputStream( sbis );
      } else {
        // TODO: add support for specified repositories.xml files...
        repositoriesMeta.readData(); // Read from the default $HOME/.kettle/repositories.xml file.
      }
    } catch ( Exception e ) {
      throw new KettleException( "Meta repository not populated", e ); //$NON-NLS-1$
    }

    // Find the specified repository.
    RepositoryMeta repositoryMeta = null;
    try {
      if ( singleDiServerInstance ) {
        repositoryMeta = repositoriesMeta.findRepository( SINGLE_DI_SERVER_INSTANCE );
      } else {
        repositoryMeta = repositoriesMeta.findRepository( repositoryName );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Repository not found", e ); //$NON-NLS-1$
    }

    if ( repositoryMeta == null ) {
      throw new KettleException( "RepositoryMeta is null" ); //$NON-NLS-1$
    }

    Repository repository = null;
    try {
      repository =
          PluginRegistry.getInstance().loadClass( RepositoryPluginType.class,
            repositoryMeta.getId(), Repository.class );
      repository.init( repositoryMeta );

    } catch ( Exception e ) {
      throw new KettleException( "Could not get repository instance", e ); //$NON-NLS-1$
    }

    // Two scenarios here: internal to server or external to server. If internal, you are already authenticated. If
    // external, you must provide a username and additionally specify that the IP address of the machine running this
    // code is trusted.
    repository.connect( PentahoSessionHolder.getSession().getName(), "password" );

    return repository;
  }

  public static Document loadXMLFrom( String xml ) throws SAXException, IOException {
    return loadXMLFrom( new ByteArrayInputStream( xml.getBytes() ) );
  }

  public static Document loadXMLFrom( InputStream is )  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    Document doc = null;
    try {
      builder = factory.newDocumentBuilder();
    } catch ( ParserConfigurationException ex ) {
      //ignore
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
    } catch(Throwable th) {
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

  public static String checkAndSanitize(final String in) {
    if (in == null) {
      throw new IllegalArgumentException();
    }
    String extension = null;
    if (in.endsWith(RepositoryObjectType.CLUSTER_SCHEMA.getExtension())) {
      extension = RepositoryObjectType.CLUSTER_SCHEMA.getExtension();
    } else if (in.endsWith(RepositoryObjectType.DATABASE.getExtension())) {
      extension = RepositoryObjectType.DATABASE.getExtension();
    } else if (in.endsWith(RepositoryObjectType.JOB.getExtension())) {
      extension = RepositoryObjectType.JOB.getExtension();
    } else if (in.endsWith(RepositoryObjectType.PARTITION_SCHEMA.getExtension())) {
      extension = RepositoryObjectType.PARTITION_SCHEMA.getExtension();
    } else if (in.endsWith(RepositoryObjectType.SLAVE_SERVER.getExtension())) {
      extension = RepositoryObjectType.SLAVE_SERVER.getExtension();
    } else if (in.endsWith(RepositoryObjectType.TRANSFORMATION.getExtension())) {
      extension = RepositoryObjectType.TRANSFORMATION.getExtension();
    }
    String out = in;
    if (extension != null) {
      out = out.substring(0, out.length()-extension.length());
    }
    if (out.contains("/") || out.equals("..") || out.equals(".") || StringUtils.isBlank(out)) {
      throw new IllegalArgumentException();
    }
    out = out.replaceAll("[/:\\[\\]\\*'\"\\|\\s\\.]", "_");  //$NON-NLS-1$//$NON-NLS-2$
    if (extension != null) {
      return out + extension;
    } else {
      return out;
    }
  }

}
