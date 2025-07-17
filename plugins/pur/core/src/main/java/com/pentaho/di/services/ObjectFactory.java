/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package com.pentaho.di.services;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * com.pentaho.di.services package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

  private final static QName _FileVersioningConfiguration_QNAME = new QName( "", "fileVersioningConfiguration" );

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
   * com.pentaho.di.services
   * 
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link FileVersioningConfiguration }
   * 
   */
  public FileVersioningConfiguration createFileVersioningConfiguration() {
    return new FileVersioningConfiguration();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link FileVersioningConfiguration }{@code >}
   * 
   */
  @XmlElementDecl( namespace = "", name = "fileVersioningConfiguration" )
  public JAXBElement<FileVersioningConfiguration> createFileVersioningConfiguration( FileVersioningConfiguration value ) {
    return new JAXBElement<FileVersioningConfiguration>( _FileVersioningConfiguration_QNAME,
        FileVersioningConfiguration.class, null, value );
  }

}
