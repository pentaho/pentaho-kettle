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

package org.pentaho.di.ui.i18n;

public class SourceCrawlerXMLElement {
  private String searchElement;
  private String keyTag;
  private String keyAttribute;

  /**
   * @param searchElement
   * @param keyTag
   * @param keyAttribute
   */
  public SourceCrawlerXMLElement( String searchElement, String keyTag, String keyAttribute ) {
    this.searchElement = searchElement;
    this.keyTag = keyTag;
    this.keyAttribute = keyAttribute;
  }

  /**
   * @return the searchElement
   */
  public String getSearchElement() {
    return searchElement;
  }

  /**
   * @param searchElement
   *          the searchElement to set
   */
  public void setSearchElement( String searchElement ) {
    this.searchElement = searchElement;
  }

  /**
   * @return the keyTag
   */
  public String getKeyTag() {
    return keyTag;
  }

  /**
   * @param keyTag
   *          the keyTag to set
   */
  public void setKeyTag( String keyTag ) {
    this.keyTag = keyTag;
  }

  /**
   * @return the keyAttribute
   */
  public String getKeyAttribute() {
    return keyAttribute;
  }

  /**
   * @param keyAttribute
   *          the keyAttribute to set
   */
  public void setKeyAttribute( String keyAttribute ) {
    this.keyAttribute = keyAttribute;
  }

}
