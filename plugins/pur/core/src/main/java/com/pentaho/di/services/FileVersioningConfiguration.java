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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for fileVersioningConfiguration complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fileVersioningConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versionCommentEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="versioningEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "fileVersioningConfiguration", propOrder = { "versionCommentEnabled", "versioningEnabled" } )
public class FileVersioningConfiguration {

  protected boolean versionCommentEnabled;
  protected boolean versioningEnabled;

  /**
   * Gets the value of the versionCommentEnabled property.
   * 
   */
  public boolean isVersionCommentEnabled() {
    return versionCommentEnabled;
  }

  /**
   * Sets the value of the versionCommentEnabled property.
   * 
   */
  public void setVersionCommentEnabled( boolean value ) {
    this.versionCommentEnabled = value;
  }

  /**
   * Gets the value of the versioningEnabled property.
   * 
   */
  public boolean isVersioningEnabled() {
    return versioningEnabled;
  }

  /**
   * Sets the value of the versioningEnabled property.
   * 
   */
  public void setVersioningEnabled( boolean value ) {
    this.versioningEnabled = value;
  }

}
