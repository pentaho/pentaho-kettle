package org.pentaho.di.core.fileinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileType;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;

public class FileInputList
{
    private List                files              = new ArrayList();
    private List                nonExistantFiles   = new ArrayList(1);
    private List                nonAccessibleFiles = new ArrayList(1);
    
    private static final String YES                = "Y";

    public static String getRequiredFilesDescription(List nonExistantFiles)
    {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iter = nonExistantFiles.iterator(); iter.hasNext();)
        {
            FileObject file = (FileObject) iter.next();
            buffer.append(file.getName().getURI());
            buffer.append(Const.CR);
        }
        return buffer.toString();
    }

    private static boolean[] includeSubdirsFalse(int iLength)
    {
        boolean[] includeSubdirs = new boolean[iLength];
        for (int i = 0; i < iLength; i++)
            includeSubdirs[i] = false;
        return includeSubdirs;
    }
    
    public static String[] createFilePathList(String[] fileName, String[] fileMask, String[] fileRequired)
    {
        boolean[] includeSubdirs = includeSubdirsFalse(fileName.length);
        return createFilePathList(fileName, fileMask, fileRequired, includeSubdirs);
    }
    
    public static String[] createFilePathList(String[] fileName, String[] fileMask, String[] fileRequired,
        boolean[] includeSubdirs)
    {
        List fileList = createFileList(fileName, fileMask, fileRequired, includeSubdirs).getFiles();
        String[] filePaths = new String[fileList.size()];
        for (int i = 0; i < filePaths.length; i++)
        {
            filePaths[i] = ((FileObject) fileList.get(i)).getName().getURI();
            // filePaths[i] = KettleVFS.getFilename((FileObject) fileList.get(i));
        }
        return filePaths;
    }

    public static FileInputList createFileList(String[] fileName, String[] fileMask, String[] fileRequired)
    {
        boolean[] includeSubdirs = includeSubdirsFalse(fileName.length);
        return createFileList(fileName, fileMask, fileRequired, includeSubdirs);
    }
    
    public static FileInputList createFileList(String[] fileName, String[] fileMask, String[] fileRequired, boolean[] includeSubdirs)
    {
        FileInputList fileInputList = new FileInputList();

        // Replace possible environment variables...
        final String realfile[] = StringUtil.environmentSubstitute(fileName);
        final String realmask[] = StringUtil.environmentSubstitute(fileMask);

        for (int i = 0; i < realfile.length; i++)
        {
            final String onefile = realfile[i];
            final String onemask = realmask[i];
            final boolean onerequired = YES.equalsIgnoreCase(fileRequired[i]);
            final boolean subdirs = includeSubdirs[i];
            
            if (Const.isEmpty(onefile)) continue;

            // 
            // If a wildcard is set we search for files
            //
            if (!Const.isEmpty(onemask))
            {
                try
                {
                    // Find all file names that match the wildcard in this directory
                    //
                    FileObject directoryFileObject = KettleVFS.getFileObject(onefile);
                    if (directoryFileObject != null && directoryFileObject.getType() == FileType.FOLDER) // it's a directory
                    {
                        FileObject[] fileObjects = directoryFileObject.findFiles(
                                new AllFileSelector()
                                {
                                    public boolean traverseDescendents(FileSelectInfo info)
                                    {
                                        return info.getDepth()==0 || subdirs;
                                    }
                                    
                                    public boolean includeFile(FileSelectInfo info)
                                    {
                                        String name = info.getFile().getName().getBaseName();
                                        boolean matches = Pattern.matches(onemask, name);
                                        /*
                                        if (matches)
                                        {
                                            System.out.println("File match: URI: "+info.getFile()+", name="+name+", depth="+info.getDepth());
                                        }
                                        */
                                        return matches;
                                    }
                                }
                            );
                        if (fileObjects != null) 
                        {
                            for (int j = 0; j < fileObjects.length; j++)
                            {
                                if (fileObjects[j].exists()) fileInputList.addFile(fileObjects[j]);
                            }
                        }
                        if (Const.isEmpty(fileObjects))
                        {
                            if (onerequired) fileInputList.addNonAccessibleFile(directoryFileObject);
                        }
                        
                        // Sort the list: quicksort, only for regular files
                        fileInputList.sortFiles();
                    }
                    else
                    {
                        FileObject[] children = directoryFileObject.getChildren();
                        for (int j = 0; j < children.length; j++)
                        {
                            // See if the wildcard (regexp) matches...
                            String name = children[j].getName().getBaseName();
                            if (Pattern.matches(onemask, name)) fileInputList.addFile(children[j]);
                        }
                        // We don't sort here, keep the order of the files in the archive.
                    }
                }
                catch (Exception e)
                {
                    LogWriter.getInstance().logError("FileInputList", Const.getStackTracker(e));
                }
            }
            else
            // A normal file...
            {
                try
                {
                    FileObject fileObject = KettleVFS.getFileObject(onefile);
                    if (fileObject.exists())
                    {
                        if (fileObject.isReadable())
                        {
                            fileInputList.addFile(fileObject);
                        }
                        else
                        {
                            if (onerequired) fileInputList.addNonAccessibleFile(fileObject);
                        }
                    }
                    else
                    {
                        if (onerequired) fileInputList.addNonExistantFile(fileObject);
                    }
                }
                catch (Exception e)
                {
                    LogWriter.getInstance().logError("FileInputList", Const.getStackTracker(e));
                }
            }
        }


        return fileInputList;
    }
    
    /* 
    public static FileInputList createFileList(String[] fileName, String[] fileMask, String[] fileRequired, boolean[] includeSubdirs)
    {
        FileInputList fileInputList = new FileInputList();

        // Replace possible environment variables...
        final String realfile[] = StringUtil.environmentSubstitute(fileName);
        final String realmask[] = StringUtil.environmentSubstitute(fileMask);

        for (int i = 0; i < realfile.length; i++)
        {
            final String onefile = realfile[i];
            final String onemask = realmask[i];
            final boolean onerequired = YES.equalsIgnoreCase(fileRequired[i]);
            boolean subdirs = includeSubdirs[i];
            // System.out.println("Checking file ["+onefile+"] mask
            // ["+onemask+"]");
            if (onefile == null) continue;

            if (!Const.isEmpty(onemask))
            // If wildcard is set we assume it's a directory
            {
                File file = new File(onefile);
                try
                {
                    // Find all file names that match the wildcard in this directory
                    String[] fileNames = file.list(new FilenameFilter()
                    {
                        public boolean accept(File dir, String name)
                        {
                            return Pattern.matches(onemask, name);
                        }
                    });
                    if (subdirs)
                    {
                        Vector matchingFilenames = new Vector();
                        appendToVector(matchingFilenames, fileNames, "");
                        findMatchingFiles(file, onemask, matchingFilenames, "");
                        fileNames = new String[matchingFilenames.size()];
                        matchingFilenames.copyInto(fileNames);
                    }

                    if (fileNames != null) 
                    {
                        for (int j = 0; j < fileNames.length; j++)
                        {
                            File localFile = new File(file, fileNames[j]);
                            if (!localFile.isDirectory() && localFile.isFile()) fileInputList.addFile(localFile);
                        }
                    }
                    
                    if (Const.isEmpty(fileNames))
                    {
                        if (onerequired) fileInputList.addNonAccessibleFile(file);
                    }
                }
                catch (Exception e)
                {
                    LogWriter.getInstance().logError("FileInputList", Const.getStackTracker(e));
                }
            }
            else
            // A normal file...
            {
                File file = new File(onefile);
                if (file.exists())
                {
                    if (file.canRead() && file.isFile())
                    {
                        if (file.isFile()) fileInputList.addFile(file);
                    }
                    else
                    {
                        if (onerequired) fileInputList.addNonAccessibleFile(file);
                    }
                }
                else
                {
                    if (onerequired) fileInputList.addNonExistantFile(file);
                }
            }
        }

        // Sort the list: quicksort
        fileInputList.sortFiles();

        // OK, return the list in filelist...
        // files = (String[]) filelist.toArray(new String[filelist.size()]);

        return fileInputList;
    }
    */
    
    
    /*
     * Copies all elements of a String array into a Vector
     * @param sArray The string array
     * @param sPrefix the prefix to put before all strings to be copied to the vector
     * @return The Vector.
    private static void appendToVector(Vector v, String[] sArray, String sPrefix)
    {
        if (sArray == null || sArray.length == 0)
            return;

        for (int i = 0; i < sArray.length; i++)
            v.add(sPrefix + sArray[i]);
    }
     */

    /*
    private static void appendToVector(Vector v, String[] sArray, String sPrefix)
    {
        if (sArray == null || sArray.length == 0)
            return;

        for (int i = 0; i < sArray.length; i++)
            v.add(sPrefix + sArray[i]);
    }
    */

    
    /*
     * 
     * @param dir
     * @param onemask
     * @param matchingFileNames
     * @param sPrefix
    private static void findMatchingFiles(File dir, final String onemask, Vector matchingFileNames, String sPrefix)
    {
        if (!Const.isEmpty(sPrefix))
        {
            String[] fileNames = dir.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return Pattern.matches(onemask, name);
                }
            });
            appendToVector(matchingFileNames, fileNames, sPrefix);
        }
        
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++)
        {
            File f = new File(dir.getAbsolutePath() + Const.FILE_SEPARATOR + files[i]);
            if (f.isDirectory())
            {
                findMatchingFiles(f, onemask, matchingFileNames, sPrefix + files[i] + Const.FILE_SEPARATOR);
            }
        }
    }
     */
    
    public List getFiles()
    {
        return files;
    }
    
    public String[] getFileStrings()
    {
        String[] fileStrings = new String[files.size()];
        for (int i=0;i<fileStrings.length;i++)
        {
            fileStrings[i] = KettleVFS.getFilename((FileObject) files.get(i));
        }
        return fileStrings;
    }

    public List getNonAccessibleFiles()
    {
        return nonAccessibleFiles;
    }

    public List getNonExistantFiles()
    {
        return nonExistantFiles;
    }

    public void addFile(FileObject file)
    {
        files.add(file);
    }

    public void addNonAccessibleFile(FileObject file)
    {
        nonAccessibleFiles.add(file);
    }

    public void addNonExistantFile(FileObject file)
    {
        nonExistantFiles.add(file);
    }

    public void sortFiles()
    {
        Collections.sort(files, KettleVFS.getComparator());
        Collections.sort(nonAccessibleFiles, KettleVFS.getComparator());
        Collections.sort(nonExistantFiles, KettleVFS.getComparator());
    }

    /*
    private boolean containsComparable(List list)
    {
        if (list == null || list.size() == 0)
            return false;
        
        return (list.get(0) instanceof Comparable);
    }
    */
    
    public FileObject getFile(int i)
    {
        return (FileObject) files.get(i);
    }

    public int nrOfFiles()
    {
        return files.size();
    }

    public int nrOfMissingFiles()
    {
        return nonAccessibleFiles.size() + nonExistantFiles.size();
    }
}
