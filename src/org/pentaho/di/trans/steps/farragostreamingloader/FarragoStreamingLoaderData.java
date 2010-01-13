/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * Copyright (c) 2010 DynamoBI Corporation.  All rights reserved.
 * This software was developed by DynamoBI Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Farrago 
 * Streaming Loader.  The Initial Developer is DynamoBI Corporation.
 * 
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.trans.steps.farragostreamingloader;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the Farrago Streaming Loader step.
 * 
 * @author Matt
 * @since Jan-05-2010
 */
public class FarragoStreamingLoaderData
    extends BaseStepData
    implements StepDataInterface
{
    public Database db;

    public int keynrs[]; // nr of keylookup -value in row...

    public int format[];

    public StreamLogger errorLogger;

    public StreamLogger outputLogger;

    public String schemaTable;

    public String sql_statement;

    public Socket client;
    public ObjectOutputStream objOut;

    public FarragoStreamingLoader.SqlRunner sqlRunner;

    /**
     * Default constructor.
     */
    public FarragoStreamingLoaderData()
    {
        super();

        db = null;
    }
}
