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

	/**
	 * Increase the endBuffer pointer by one.<br>
	 * If there is not enough room in the buffer to go there, resize the byte buffer and read more data.<br>
	 * if there is no more data to read and if the endBuffer pointer has reached the end of the byte buffer, we return true.<br>
	 * @return true if we reached the end of the byte buffer.
	 * @throws IOException In case we get an error reading from the input file.
	 */
	public boolean increaseEndBuffer() throws IOException {
		endBuffer++;
		
		if (endBuffer>=bufferSize) {
			// Oops, we need to read more data...
			// Better resize this before we read other things in it...
			//
			resizeByteBuffer();
			
			// Also read another chunk of data, now that we have the space for it...
			if (!readBufferFromFile()) {
				// Break out of the loop if we don't have enough buffer space to continue...
				//
				if (endBuffer>=bufferSize) 
				{
					return true;
				}
			}
		}
		
		return false;
	}

	/**
      <pre>	 
      [abcd "" defg] --> [abcd " defg]
      [""""] --> [""]
      [""] --> ["]
      </pre>	 

     @return the byte array with escaped enclosures escaped.
	*/
	public byte[] removeEscapedEnclosures(byte[] field, int nrEnclosuresFound) {
		byte[] result = new byte[field.length-nrEnclosuresFound];
		int resultIndex=0;
		for (int i=0;i<field.length;i++)
		{
			if (field[i]==enclosure[0])
			{
				if (i+1<field.length && field[i+1]==enclosure[0])
				{
					// field[i]+field[i+1] is an escaped enclosure...
					// so we ignore this one
					// field[i+1] will be picked up on the next iteration.
				}
				else
				{
					// Not an escaped enclosure...
					result[resultIndex++] = field[i];
				}
			}
			else
			{
				result[resultIndex++] = field[i];
			}
		}
		return result;
	}

}
