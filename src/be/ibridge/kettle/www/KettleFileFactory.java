package be.ibridge.kettle.www;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

public class KettleFileFactory implements FileItemFactory
{
    private final File file;

    public KettleFileFactory(File directory, String fileName) throws Exception
    {
        file = new File(directory, fileName);
        if (!directory.exists()) 
        {
            if (!directory.mkdirs())
            {
                throw new Exception("Could not create directory on server: " + directory.getAbsolutePath());
            }
        }
    }

    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName)
    {
        return new SimpleFileItem(file, contentType);
    }

    public void clear() throws Exception
    {
        if (file.exists())
        {
            if (!file.delete()) { throw new Exception("Could not delete previously uploaded (Kettle) file: " + file.getAbsolutePath()); }
        }
    }

}
