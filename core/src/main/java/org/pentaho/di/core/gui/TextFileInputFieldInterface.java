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

package org.pentaho.di.core.gui;

public interface TextFileInputFieldInterface extends Comparable<TextFileInputFieldInterface> {
  public int getPosition();

  public int getLength();

  public String getName();

  public void setLength( int i );

  public TextFileInputFieldInterface createNewInstance( String newFieldname, int x, int newlength );
}
