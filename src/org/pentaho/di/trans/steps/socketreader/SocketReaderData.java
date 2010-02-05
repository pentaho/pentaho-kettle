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
 

package org.pentaho.di.trans.steps.socketreader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 27-nov-2006
 *
 */
public class SocketReaderData extends BaseStepData implements StepDataInterface
{

	public Socket socket;
    public DataOutputStream outputStream;
    public DataInputStream inputStream;
    public RowMetaInterface rowMeta;

    /**
	 * 
	 */
	public SocketReaderData()
	{
		super();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (socket!=null) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
		} catch (java.io.IOException e) {
		} finally {
			super.finalize();
		}
	}

}
