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
package org.pentaho.di.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;



public class StreamLogger implements Runnable
{
    private InputStream is;

    private String      type;

	private LogChannelInterface	log;

    public StreamLogger(LogChannelInterface log, InputStream is, String type)
    {
    	this.log = log;
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
            {
                log.logBasic(type + " " + line); //$NON-NLS-1$
            }
        }
        catch (IOException ioe)
        {
            log.logError(type + " " + Const.getStackTracker(ioe)); //$NON-NLS-1$
        }
    }
}
