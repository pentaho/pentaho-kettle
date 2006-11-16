package be.ibridge.kettle.www;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.DeferredFileOutputStream;
import org.apache.commons.fileupload.FileItem;

public class SimpleFileItem implements FileItem
{
    private static final long serialVersionUID = -911815804583620706L;

    private final DeferredFileOutputStream outputStream;

    private final String                   contentType;

    public SimpleFileItem(File file, String contentType)
    {
        this.contentType = contentType;
        this.outputStream = new DeferredFileOutputStream(0, file);
    }

    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(getFile());
    }

    public File getFile()
    {
        return outputStream.getFile();
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getName()
    {
        return getFile().getName();
    }

    public boolean isInMemory()
    {
        return false;
    }

    public long getSize()
    {
        return getFile().length();
    }

    public byte[] get()
    {
        return null;
    }

    public String getString(String encoding) throws UnsupportedEncodingException
    {
        return null;
    }

    public String getString()
    {
        return null;
    }

    public void write(File file) throws Exception
    {
    }

    public void delete()
    {
        getFile().delete();
    }

    public String getFieldName()
    {
        return null;
    }

    public void setFieldName(String name)
    {
    }

    public boolean isFormField()
    {
        return false;
    }

    public void setFormField(boolean state)
    {
    }

    public OutputStream getOutputStream() throws IOException
    {
        return outputStream;
    }

}
