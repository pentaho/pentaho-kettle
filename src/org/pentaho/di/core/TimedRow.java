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
package org.pentaho.di.core;

import java.util.Date;

public class TimedRow
{
    private Date logDate;

    private Object[]  row;

    /**
     * @param logDate
     * @param row
     */
    public TimedRow(Date logDate, Object[] row)
    {
        this.logDate = logDate;
        this.row = row;
    }

    /**
     * @param row
     */
    public TimedRow(Object[] row)
    {
        this.logDate = new Date();
        this.row = row;
    }
    
    public String toString()
    {
    	StringBuffer str = new StringBuffer();
    	for (int i=0;i<row.length;i++) {
    		if (i>0) str.append(", ");
    		if (row[i]==null) {
    			str.append("null");
    		}
    		else {
    			str.append(row[i].toString());
    		}
    	}
        return str.toString();
    }
    
    /**
     * @return the row
     */
    public Object[] getRow()
    {
        return row;
    }

    /**
     * @param row the row to set
     */
    public void setRow(Object[] row)
    {
        this.row = row;
    }

    /**
     * @return the logDate
     */
    public Date getLogDate()
    {
        return logDate;
    }

    /**
     * @param logDate the logDate to set
     */
    public void setLogDate(Date logDate)
    {
        this.logDate = logDate;
    }

    /**
     * Get the logging time for this row.
     * @return the logging time for this row.
     */
    public long getLogtime()
    {
        if (logDate == null) return 0L;
        return logDate.getTime();
    }
    
}
