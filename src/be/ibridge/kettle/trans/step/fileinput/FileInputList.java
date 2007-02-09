package be.ibridge.kettle.trans.step.fileinput;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.util.StringUtil;

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
            File file = (File) iter.next();
            buffer.append(file.getPath());
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
            filePaths[i] = ((File) fileList.get(i)).getPath();
        }
        return filePaths;
    }

    public static FileInputList createFileList(String[] fileName, String[] fileMask, String[] fileRequired)
    {
        boolean[] includeSubdirs = includeSubdirsFalse(fileName.length);
        return createFileList(fileName, fileMask, fileRequired, includeSubdirs);
    }
    
    public static FileInputList createFileList(String[] fileName, String[] fileMask, String[] fileRequired,
        boolean[] includeSubdirs)
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
    
    /**
     * Copies all elements of a String array into a Vector
     * @param sArray The string array
     * @return The Vector.
     */
    private static void appendToVector(Vector v, String[] sArray, String sPrefix)
    {
        if (sArray == null || sArray.length == 0)
            return;

        for (int i = 0; i < sArray.length; i++)
            v.add(sPrefix + sArray[i]);
    }
    
    /**
     * 
     * @param dir
     * @param onemask
     * @param matchingFileNames
     * @param sPrefix
     */
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
    
    public List getFiles()
    {
        return files;
    }

    public List getNonAccessibleFiles()
    {
        return nonAccessibleFiles;
    }

    public List getNonExistantFiles()
    {
        return nonExistantFiles;
    }

    public void addFile(File file)
    {
        files.add(file);
    }

    public void addNonAccessibleFile(File file)
    {
        nonAccessibleFiles.add(file);
    }

    public void addNonExistantFile(File file)
    {
        nonExistantFiles.add(file);
    }

    public void sortFiles()
    {
        Collections.sort(files);
        Collections.sort(nonAccessibleFiles);
        Collections.sort(nonExistantFiles);
    }

    public File getFile(int i)
    {
        return (File) files.get(i);
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
