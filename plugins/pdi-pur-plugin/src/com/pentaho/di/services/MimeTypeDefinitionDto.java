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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for MimeTypeDefinitionDto complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MimeTypeDefinitionDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extension" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="mimeType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="locale" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="converter" type="{http://www.w3.org/2001/XMLSchema}string" default="streamConverter" />
 *       &lt;attribute name="versionEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="versionCommentEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "MimeTypeDefinitionDto", propOrder = { "extension" } )
public class MimeTypeDefinitionDto {

  @XmlElement( required = true )
  protected List<String> extension;
  @XmlAttribute( name = "mimeType", required = true )
  protected String mimeType;
  @XmlAttribute( name = "hidden" )
  protected Boolean hidden;
  @XmlAttribute( name = "locale" )
  protected Boolean locale;
  @XmlAttribute( name = "converter" )
  protected String converter;
  @XmlAttribute( name = "versionEnabled" )
  protected Boolean versionEnabled;
  @XmlAttribute( name = "versionCommentEnabled" )
  protected Boolean versionCommentEnabled;

  /**
   * Gets the value of the extension property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
   * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
   * the extension property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getExtension().add( newItem );
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link String }
   * 
   * 
   */
  public List<String> getExtension() {
    if ( extension == null ) {
      extension = new ArrayList<String>();
    }
    return this.extension;
  }

  /**
   * Gets the value of the mimeType property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the value of the mimeType property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setMimeType( String value ) {
    this.mimeType = value;
  }

  /**
   * Gets the value of the hidden property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public boolean isHidden() {
    if ( hidden == null ) {
      return false;
    } else {
      return hidden;
    }
  }

  /**
   * Sets the value of the hidden property.
   * 
   * @param value
   *          allowed object is {@link Boolean }
   * 
   */
  public void setHidden( Boolean value ) {
    this.hidden = value;
  }

  /**
   * Gets the value of the locale property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public boolean isLocale() {
    if ( locale == null ) {
      return false;
    } else {
      return locale;
    }
  }

  /**
   * Sets the value of the locale property.
   * 
   * @param value
   *          allowed object is {@link Boolean }
   * 
   */
  public void setLocale( Boolean value ) {
    this.locale = value;
  }

  /**
   * Gets the value of the converter property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getConverter() {
    if ( converter == null ) {
      return "streamConverter";
    } else {
      return converter;
    }
  }

  /**
   * Sets the value of the converter property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setConverter( String value ) {
    this.converter = value;
  }

  /**
   * Gets the value of the versionEnabled property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public boolean isVersionEnabled() {
    if ( versionEnabled == null ) {
      return false;
    } else {
      return versionEnabled;
    }
  }

  /**
   * Sets the value of the versionEnabled property.
   * 
   * @param value
   *          allowed object is {@link Boolean }
   * 
   */
  public void setVersionEnabled( Boolean value ) {
    this.versionEnabled = value;
  }

  /**
   * Gets the value of the versionCommentEnabled property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public boolean isVersionCommentEnabled() {
    if ( versionCommentEnabled == null ) {
      return false;
    } else {
      return versionCommentEnabled;
    }
  }

  /**
   * Sets the value of the versionCommentEnabled property.
   * 
   * @param value
   *          allowed object is {@link Boolean }
   * 
   */
  public void setVersionCommentEnabled( Boolean value ) {
    this.versionCommentEnabled = value;
  }

}
