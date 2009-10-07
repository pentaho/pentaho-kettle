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
/**
 * 
 */
package org.pentaho.di.core.playlist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

class FilePlayListReplayLineNumberFile extends FilePlayListReplayFile
{
    Set<Long> lineNumbers = new HashSet<Long>();

    public FilePlayListReplayLineNumberFile(FileObject lineNumberFile, String encoding, FileObject processingFile, String filePart) throws KettleException
    {
        super(processingFile, filePart);
        initialize(lineNumberFile, encoding);
    }

    private void initialize(FileObject lineNumberFile, String encoding) throws KettleException
    {
        BufferedReader reader = null;
        try
        {
            if (encoding == null)
                reader = new BufferedReader(new InputStreamReader(KettleVFS.getInputStream(lineNumberFile)));
            else
                reader = new BufferedReader(new InputStreamReader(KettleVFS.getInputStream(lineNumberFile), encoding));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                if (line.length() > 0) lineNumbers.add(Long.valueOf(line));
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Could not read line number file " + lineNumberFile.getName().getURI(), e);
        }
        finally
        {
            if (reader != null) try
            {
                reader.close();
            }
            catch (IOException e)
            {
                throw new KettleException("Could not close line number file " + lineNumberFile.getName().getURI(), e);
            }
        }
    }

    public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart) throws KettleException
    {
        return lineNumbers.contains(new Long(lineNr));
    }
}
