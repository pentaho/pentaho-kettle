 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

/**
 *   Kettle was (re-)started in March 2003
 */

package org.pentaho.di.pkg;
import org.pentaho.di.pan.Pan;

import org.pentaho.di.core.exception.KettleException;


/**
 * Executes a transformation calles transformation.xml from within a jar file.
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
