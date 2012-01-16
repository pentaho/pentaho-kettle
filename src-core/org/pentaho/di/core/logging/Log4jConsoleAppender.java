/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jConsoleAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private String  name;
    
    public Log4jConsoleAppender()
    {
    }
    
    public void addFilter(Filter filter)
    {
        this.filter = filter;
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void clearFilters()
    {
        filter=null;
    }

    public void close()
    {
    }

    public void doAppend(LoggingEvent event)
    {
        String line = layout.format(event);
        System.out.println(line);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setErrorHandler(ErrorHandler arg0)
    {
    }

    public ErrorHandler getErrorHandler()
    {
        return null;
    }

    public void setLayout(Layout layout)
    {
        this.layout = layout;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public boolean requiresLayout()
    {
        return true;
    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
}
