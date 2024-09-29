/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.logging.log4j;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;

public class Log4jFileAppender implements Appender {
    private Layout layout;
    private Filter filter;

    private FileObject file;

    private String name;

    private OutputStream fileOutputStream;

    public Log4jFileAppender(FileObject file ) throws IOException {
        this.file = file;

        fileOutputStream = KettleVFS.getOutputStream( file, false );
    }

    public Log4jFileAppender(FileObject file, boolean append ) throws IOException {
        this.file = file;

        fileOutputStream = KettleVFS.getOutputStream( file, append );
    }

    public void addFilter( Filter filter ) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void clearFilters() {
        filter = null;
    }

    public void close() {
        try {
            fileOutputStream.close();
        } catch ( IOException e ) {
            System.out.println( "Unable to close Logging file [" + file.getName() + "] : " + e.getMessage() );
        }
    }

    public void append( LogEvent event ) {
        String line = ((Log4jLayout) layout).format( event ) + Const.CR;
        try {
            fileOutputStream.write( line.getBytes( Const.XML_ENCODING ) );
        } catch ( IOException e ) {
            System.out.println( "Unable to close Logging file [" + file.getName() + "] : " + e.getMessage() );
        }
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLayout( Layout layout ) {
        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }

    @Override
    public boolean ignoreExceptions() {
        return false;
    }

    @Override
    public org.apache.logging.log4j.core.ErrorHandler getHandler() {
        return null;
    }

    @Override
    public void setHandler(org.apache.logging.log4j.core.ErrorHandler handler) {

    }

    public boolean requiresLayout() {
        return true;
    }

    public FileObject getFile() {
        return file;
    }

    public void setFilename( FileObject file ) {
        this.file = file;
    }

    public OutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public void setFileOutputStream( OutputStream fileOutputStream ) {
        this.fileOutputStream = fileOutputStream;
    }

    public void setFilter( Filter filter ) {
        this.filter = filter;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}

