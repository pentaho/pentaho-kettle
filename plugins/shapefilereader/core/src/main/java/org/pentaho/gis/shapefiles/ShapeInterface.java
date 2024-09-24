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

package org.pentaho.gis.shapefiles;

import org.pentaho.di.core.row.RowMetaInterface;

/*
 * Created on 30-jun-2004
 *
 * @author Matt
 *
 */

public interface ShapeInterface {
  public int getType();

  public void setDbfData( Object[] row );
  public Object[] getDbfData();

  public void setDbfMeta( RowMetaInterface rowMeta );
  public RowMetaInterface getDbfMeta();
}
