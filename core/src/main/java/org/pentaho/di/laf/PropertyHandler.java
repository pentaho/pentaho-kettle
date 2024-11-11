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


package org.pentaho.di.laf;

public interface PropertyHandler extends Handler {
  /**
   * load properties for the given properties file
   *
   * @param filename
   * @return true if load was successful
   */
  public boolean loadProps( String filename );

  /**
   * check to see whether a property file exists within the classpath or filesystem
   *
   * @param filename
   * @return true if resource exists
   */
  public boolean exists( String filename );

  /**
   * return the value of a given key from the properties list
   *
   * @param key
   * @return null if the key is not found
   */
  public String getProperty( String key );

  /**
   * return the value of a given key from the properties list, returning the defValue string should the key not be found
   *
   * @param key
   * @param defValue
   * @return a string representing either the value associated with the passed key or defValue should that key not be
   *         found
   */
  public String getProperty( String key, String defValue );
}
