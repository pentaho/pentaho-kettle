/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.excelinput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Extended {@link KettleValueException} that allows passing extra
 * context info up the chain (sheet, row, and column IDs).
 * 
 * If we were really obssessive, we'd cache both the names and
 * indexes of all the items, including the input file. But this
 * will do for a start.
 * 
 * @author timh
 * @since 14-FEB-2008
 */

public class KettleCellValueException extends KettleException {

	private static Class<?> PKG = ExcelInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final long serialVersionUID = 1L;
	
	final private int sheetnr;
	final private int rownr;
	final private int colnr;
	final private String fieldName;
	
	/**
	 * Standard constructor.
	 * <p/>
	 * <em>Note:</em> All indexes below have a 0-origin
	 * (internal index), but are reported with a 1-origin
	 * (human index).
	 * 
	 * @param ex The Exception to wrap.
	 * @param sheetnr Sheet number
	 * @param rownr Row number
	 * @param colnr Column number
	 * @param fieldName The name of the field being converted
	 */
	public KettleCellValueException(KettleException ex,
									int sheetnr, int rownr,
									int colnr, String fieldName) {
		super(ex);
		// Note that internal indexes start at 0
		this.sheetnr = sheetnr + 1;
		this.rownr = rownr + 1;
		this.colnr = colnr + 1;
		this.fieldName = fieldName;
	}

	@Override
	public String getMessage() {
		String msgText = BaseMessages.getString(PKG, "KettleCellValueException.CannotConvertFieldFromCell", 
					Integer.toString(sheetnr), Integer.toString(rownr), Integer.toString(colnr), fieldName, super.getMessage());
		return msgText;
	}


	
}
