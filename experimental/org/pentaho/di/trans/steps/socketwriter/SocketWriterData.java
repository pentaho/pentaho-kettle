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
 

package org.pentaho.di.trans.steps.socketwriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 27-nov-2006
 *
 */
public class SocketWriterData extends BaseStepData implements StepDataInterface
{
    public DataOutputStream outputStream;
    public Socket clientSocket;
    public DataInputStream inputStream;
    public int flushInterval;
    public ServerSocket serverSocket;
    
    /**
	 * 
	 */
	public SocketWriterData()
	{
		super();
	}

}
