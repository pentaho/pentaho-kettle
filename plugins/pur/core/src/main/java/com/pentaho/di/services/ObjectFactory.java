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
package com.pentaho.di.services;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
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
