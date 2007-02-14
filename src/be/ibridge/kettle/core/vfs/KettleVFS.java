package be.ibridge.kettle.core.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import be.ibridge.kettle.core.Const;

public class KettleVFS
{
    private int fileNumber;
    
    private KettleVFS()
    {
        fileNumber=1;
    }
    
    private int getNextFileNumber()
    {
        return fileNumber++;
    }
    
    private static KettleVFS kettleVFS = new KettleVFS();
    
    public static FileObject getFileObject(String vfsFilename) throws IOException
    {
        FileSystemManager fsManager = VFS.getManager();
        
        // We have one problem with VFS: if the file is in a subdirectory of the current one: somedir/somefile
        // In that case, VFS doesn't parse the file correctly.
        // We need to put file: in front of it to make it work.
        // However, how are we going to verify this?
        // 
        // We are going to see if the filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
        // If not, we are going to assume it's a file.
        //
        boolean relativeFilename=true;
        String[] schemes = VFS.getManager().getSchemes();
        for (int i=0;i<schemes.length && relativeFilename;i++)
        {
            if (vfsFilename.startsWith(schemes[i]+":")) relativeFilename=false;
        }
        
        String filename;
        if (relativeFilename)
        {
            File file = new File(vfsFilename);
            filename = file.getAbsolutePath();
        }
        else
        {
            filename = vfsFilename;
        }
        
        FileObject fileObject = fsManager.resolveFile( filename );
        
        return fileObject;
    }
    
    public static String getFileContent(String vfsFilename) throws IOException
    {
        InputStream inputStream = getInputStream(vfsFilename);
        int c;
        StringBuffer stringBuffer = new StringBuffer();
        while ( (c=inputStream.read())!=-1) stringBuffer.append((char)c);
        inputStream.close();
        return stringBuffer.toString();
    }
    
    public static boolean fileExists(String vfsFilename) throws IOException
    {
        FileObject fileObject = getFileObject(vfsFilename);
        return fileObject.exists();
    }
    
    public static InputStream getInputStream(String vfsFilename) throws IOException
    {
        FileObject fileObject = getFileObject(vfsFilename);
        FileContent content = fileObject.getContent();
        return content.getInputStream();
    }
    
    public static OutputStream getOutputStream(String vfsFilename, boolean append) throws IOException
    {
        FileObject fileObject = getFileObject(vfsFilename);
        fileObject.createFile();
        FileContent content = fileObject.getContent();
        return content.getOutputStream();
    }
    
    public static String getFilename(FileObject fileObject)
    {
        FileName fileName = fileObject.getName();
        String root = fileName.getRootURI();
        if (!root.startsWith("file:")) return fileName.getURI(); // nothing we can do about non-normal files.
        if (root.endsWith(":/")) // Windows
        {
            root = root.substring(8,10);
        }
        else // *nix & OSX
        {
            root = "";
        }
        String fileString = root + fileName.getPath();
        if (!"/".equals(Const.FILE_SEPARATOR))
        {
            fileString = Const.replace(fileString, "/", Const.FILE_SEPARATOR);
        }
        return fileString;
    }
    
    public static FileObject createTempFile(String prefix, String suffix, String directory) throws IOException
    {
        FileObject fileObject;
        do
        {
            String filename = directory+"/"+prefix+"_"+kettleVFS.getNextFileNumber()+suffix;
            fileObject = getFileObject(filename);
        }
        while (fileObject.exists());
        return fileObject;
    }
}
