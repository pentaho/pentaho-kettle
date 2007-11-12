 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

/**
 *   Kettle was (re-)started in March 2003
 */

package org.pentaho.di.pkg;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.pan.Pan;


/**
 * Executes a transformation calls transformation.xml from within a jar file.
 * 
 * @author Matt
 * @since
 */
public class JarPan
{
	public static void main(String[] a)
	{
	    String args[] = new String[a.length+1];
        args[0] = "-jarfile:/"+JarfileGenerator.TRANSFORMATION_FILENAME;
        for (int i=0;i<a.length;i++) args[i+1] = a[i];
        
        try
        {
            Pan.main(args);
		}
		catch(KettleException ke)
		{
			System.out.println("ERROR occurred: "+ke.getMessage());
            System.exit(2);
		}

	}
}
