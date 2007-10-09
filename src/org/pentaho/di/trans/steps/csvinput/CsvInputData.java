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
 

package org.pentaho.di.trans.steps.csvinput;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class CsvInputData extends BaseStepData implements StepDataInterface
{

	public FileChannel fc;
	public ByteBuffer bb;
	public RowMetaInterface convertRowMeta;
	public RowMetaInterface outputRowMeta;
	
	public byte[] byteBuffer;
	public int    startBuffer;
	public int    endBuffer;
	public int    bufferSize;

	public byte[] delimiter;
	public byte[] enclosure;
	
	public int preferredBufferSize;
	public String filename;
	public long fileSize;
			
	/**
	 * 
	 */
	public CsvInputData()
	{
		super();
		byteBuffer = new byte[] {};
		startBuffer = 0;
		endBuffer = 0;
	}

	// Resize
	public void resizeByteBuffer() {
		// What's the new size?
		// It's (endBuffer-startBuffer)+size !!
		// That way we can at least read one full block of data using NIO
		//
		bufferSize = endBuffer-startBuffer;
		int newSize = bufferSize+preferredBufferSize;
		byte[] newByteBuffer = new byte[newSize];
		
		// copy over the old data...
		System.arraycopy(byteBuffer, startBuffer, newByteBuffer, 0, bufferSize);
		
		// replace the old byte buffer...
		byteBuffer = newByteBuffer;
		
		// Adjust start and end point of data in the byte buffer
		//
		startBuffer = 0;
		endBuffer = bufferSize;
	}

	public boolean readBufferFromFile() throws IOException {
		bb.position(endBuffer);
		int n = fc.read( bb );
		if( n == -1) {
			return false;
		}
		else {
			// adjust the highest used position...
			//
			bufferSize = endBuffer+n; 
			
			// Store the data in our byte array
			//
			bb.position(endBuffer);
			bb.get( byteBuffer, endBuffer, n);

			return true;
		} 
	}

}
