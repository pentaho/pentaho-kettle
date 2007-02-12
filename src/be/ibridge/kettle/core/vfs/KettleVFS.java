package be.ibridge.kettle.core.vfs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

public class KettleVFS
{
    public static String getFileContent(String vfsFilename) throws IOException
    {
        FileSystemManager fsManager = VFS.getManager();
        FileObject fileObject = fsManager.resolveFile( vfsFilename );
        FileContent content = fileObject.getContent();
        StringBuffer stringBuffer = new StringBuffer();
        InputStream inputStream = content.getInputStream();
        int c;
        while ( (c=inputStream.read())!=-1) stringBuffer.append((char)c);
        inputStream.close();
        return stringBuffer.toString();
    }
    
    public static boolean fileExists(String vfsFilename) throws IOException
    {
        FileSystemManager fsManager = VFS.getManager();
        FileObject fileObject = fsManager.resolveFile( vfsFilename );
        return fileObject.exists();
    }
}
