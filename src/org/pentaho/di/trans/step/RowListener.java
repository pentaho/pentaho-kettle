/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;


public interface RowListener
{
	/**
	 * This method is called when a row is read from another step
	 * @param rowMeta the metadata of the row
	 * @param row the data of the row
	 * @throws KettleStepException an exception that can be thrown to hard stop the step
	 */
    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException;

	/**
	 * This method is called when a row is written to another step (even if there is no next step)
	 * @param rowMeta the metadata of the row
	 * @param row the data of the row
	 * @throws KettleStepException an exception that can be thrown to hard stop the step
	 */
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException;

    /**
	 * This method is called when the error handling of a row is writing a row to the error stream.
	 * @param rowMeta the metadata of the row
	 * @param row the data of the row
	 * @throws KettleStepException an exception that can be thrown to hard stop the step
	 */
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException;
}
