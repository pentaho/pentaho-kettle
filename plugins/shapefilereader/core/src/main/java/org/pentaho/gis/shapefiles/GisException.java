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


package org.pentaho.gis.shapefiles;

/**
 *
 *
 * @author Matt
 * @since  6-apr-2005
 */
public class GisException extends Exception {
  private static final long serialVersionUID = 7857025723282851070L;

  /**
   *
   */
  public GisException() {
    super();
  }
  /**
   * @param arg0
   */
  public GisException( String arg0 ) {
    super( arg0 );
  }

  /**
   * @param arg0
   * @param arg1
   */
  public GisException( String arg0, Throwable arg1 ) {
    super( arg0, arg1 );
  }

  /**
   * @param arg0
   */
  public GisException( Throwable arg0 ) {
    super( arg0 );
  }
}
