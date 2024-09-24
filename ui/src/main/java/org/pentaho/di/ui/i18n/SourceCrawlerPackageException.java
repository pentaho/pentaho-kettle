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

public class SourceCrawlerPackageException {
  private String startsWith;
  private String packageName;

  /**
   * @param startsWiths
   * @param packageName
   */
  public SourceCrawlerPackageException( String startsWith, String packageName ) {
    this.startsWith = startsWith;
    this.packageName = packageName;
  }

  /**
   * @return the startsWith
   */
  public String getStartsWith() {
    return startsWith;
  }

  /**
   * @param startsWith
   *          the startsWith to set
   */
  public void setStartsWith( String startsWith ) {
    this.startsWith = startsWith;
  }

  /**
   * @return the packageName
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * @param packageName
   *          the packageName to set
   */
  public void setPackageName( String packageName ) {
    this.packageName = packageName;
  }

}
