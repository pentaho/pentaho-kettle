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
