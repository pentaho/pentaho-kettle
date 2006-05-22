package be.ibridge.kettle.trans.step.fileinput;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    public static String[] createFilePathList(String[] fileName, String[] fileMask, String[] fileRequired)
    {
        List fileList = createFileList(fileName, fileMask, fileRequired).getFiles();
        String[] filePaths = new String[fileList.size()];
        for (int i = 0; i < filePaths.length; i++)
        {
            filePaths[i] = ((File) fileList.get(i)).getPath();
        }
        return filePaths;
    }

    public static FileInputList createFileList(String[] fileName, String[] fileMask, String[] fileRequired)
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

            // System.out.println("Checking file ["+onefile+"] mask
            // ["+onemask+"]");
            if (onefile == null) continue;

            if (onemask != null && onemask.length() > 0) // A directory & a
            // wildcard
            {
                File file = new File(onefile);
                try
                {
                    String[] fileNames = file.list(new FilenameFilter()
                    {
                        public boolean accept(File dir, String name)
                        {
                            return Pattern.matches(onemask, name);
                        }
                    });

                    if (fileNames != null) for (int j = 0; j < fileNames.length; j++)
                    {
                        fileInputList.addFile(new File(file, fileNames[j]));
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
                    if (file.canRead())
                    {
                        if (file.isFile()) fileInputList.addFile(file);
                    }
                    else
                        if (onerequired) fileInputList.addNonAccessibleFile(file);
                }
                else
                    if (onerequired) fileInputList.addNonExistantFile(file);
            }
        }

        // Sort the list: quicksort
        fileInputList.sortFiles();

        // OK, return the list in filelist...
        // files = (String[]) filelist.toArray(new String[filelist.size()]);

        return fileInputList;
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
