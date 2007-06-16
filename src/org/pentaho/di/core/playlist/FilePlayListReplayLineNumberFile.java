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

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.exception.KettleException;

class FilePlayListReplayLineNumberFile extends FilePlayListReplayFile
{
    Set lineNumbers = new HashSet();

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
                reader = new BufferedReader(new InputStreamReader(lineNumberFile.getContent().getInputStream()));
            else
                reader = new BufferedReader(new InputStreamReader(lineNumberFile.getContent().getInputStream(), encoding));
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
                LogWriter.getInstance().logBasic("TextFilePlayLineNumber", "Could not close line number file " + lineNumberFile.getName().getURI());
            }
        }
    }

    public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart) throws KettleException
    {
        return lineNumbers.contains(new Long(lineNr));
    }
}
