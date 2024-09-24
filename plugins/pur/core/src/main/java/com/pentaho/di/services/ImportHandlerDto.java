/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ImportHandlerDto complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImportHandlerDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MimeTypeDefinitions" type="{http://www.pentaho.com/schema/}MimeTypeDefinitionsDto"/>
 *       &lt;/sequence>
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ImportHandlerDto", propOrder = { "mimeTypeDefinitions" } )
public class ImportHandlerDto {

  @XmlElement( name = "MimeTypeDefinitions", required = true )
  protected MimeTypeDefinitionsDto mimeTypeDefinitions;
  @XmlAttribute( name = "class", required = true )
  protected String clazz;

  /**
   * Gets the value of the mimeTypeDefinitions property.
   * 
   * @return possible object is {@link MimeTypeDefinitionsDto }
   * 
   */
  public MimeTypeDefinitionsDto getMimeTypeDefinitions() {
    return mimeTypeDefinitions;
  }

  /**
   * Sets the value of the mimeTypeDefinitions property.
   * 
   * @param value
   *          allowed object is {@link MimeTypeDefinitionsDto }
   * 
   */
  public void setMimeTypeDefinitions( MimeTypeDefinitionsDto value ) {
    this.mimeTypeDefinitions = value;
  }

  /**
   * Gets the value of the clazz property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getClazz() {
    return clazz;
  }

  /**
   * Sets the value of the clazz property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setClazz( String value ) {
    this.clazz = value;
  }

}
