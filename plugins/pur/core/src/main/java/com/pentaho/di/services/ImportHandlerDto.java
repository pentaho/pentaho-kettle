/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
