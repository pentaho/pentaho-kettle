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

 

package org.pentaho.di.shapefilereader;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.gis.shapefiles.ShapeFile;


/**
 * @author Matt
 * @since 22-jan-2005
 */
public class ShapeFileReaderData extends BaseStepData implements StepDataInterface 
{
	public ShapeFile shapeFile;
	public int shapeNr;
	public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public ShapeFileReaderData()
	{
		super();
	}
}
