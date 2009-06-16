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
/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created July 10, 2007 
 * @author Marc Batchelor
 * 
 */
package org.pentaho.di.resource;

import java.util.Hashtable;
import java.util.Map;

/**
 * With this resource naming scheme we try to keep the original filename.
 * However, if there are multiple files with the same name, we add a sequence nr starting at 2.
 * 
 * For example :
 * 
 *     Load orders.ktr
 *     Load orders 2.ktr
 *     Load orders 3.ktr
 *     etc.
 * 
 * @author matt
 *
 */
public class SequenceResourceNaming extends SimpleResourceNaming {
	
	  private Map<String, Integer> sequenceMap;
	  
	  public SequenceResourceNaming() {
		  sequenceMap = new Hashtable<String, Integer>();
	  }
	  
	  //
	  // End result could look like any of the following:
	  //
	  // Inputs:
	  //    Prefix       : Marc Sample Transformation
	  //    Original Path: D:\japps\pentaho\kettle\samples
	  //    Extension    : .ktr
	  //
	  // Output Example 1 (no file system prefix, no path used)
	  //     Marc_Sample_Transformation_001.ktr
	  // Output Example 2 (file system prefix: ${KETTLE_FILE_BASE}!, no path used)
	  //     ${KETTLE_FILE_BASE}!Marc_Sample_Transformation_003.ktr
	  // Output Example 3 (file system prefix: ${KETTLE_FILE_BASE}!, path is used)
	  //     ${KETTLE_FILE_BASE}!japps/pentaho/kettle/samples/Marc_Sample_Transformation_014.ktr
	  
	  protected String getFileNameUniqueIdentifier(String filename, String extension) {

		  String key = filename+extension;
		  Integer seq = sequenceMap.get(key);
		  if (seq==null) {
			  seq=new Integer(2);
			  sequenceMap.put(key, seq);
			  return null;
		  }
		  
		  sequenceMap.put(key, new Integer(seq.intValue()+1));
		  
		  return seq.toString();
	  }

  
}
