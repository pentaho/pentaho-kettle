package be.ibridge.kettle.core;

import be.ibridge.kettle.repository.RepositoryDirectory;

public class LastUsedFile
{
    private String  filename;

    private String  directory;

    private boolean sourceRepository;

    private String  repositoryName;

    /**
     * @param filename
     * @param directory
     * @param sourceRepository
     * @param repositoryName
     */
    public LastUsedFile(String filename, String directory, boolean sourceRepository, String repositoryName)
    {
        this.filename = filename;
        this.directory = directory;
        this.sourceRepository = sourceRepository;
        this.repositoryName = repositoryName;
    }
    
    public String toString()
    {
        String string = "";
        
        if (sourceRepository && !Const.isEmpty(directory) && !Const.isEmpty(repositoryName))
        {
            string+="["+repositoryName+"] "; 
            
            if (directory.endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR))
            {
                string+=": "+directory+filename;
            }
            else
            {
                string+=": "+RepositoryDirectory.DIRECTORY_SEPARATOR+filename;
            }
        }
        else
        {
            string+=filename;
        }
            
        return string;
    }
    
    public int hashCode()
    {
        return toString().hashCode();
    }
    
    public boolean equals(Object obj)
    {
        LastUsedFile file = (LastUsedFile) obj;
        return toString().equals(file.toString());
    }

    /**
     * @return the directory
     */
    public String getDirectory()
    {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }

    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * @return the repositoryName
     */
    public String getRepositoryName()
    {
        return repositoryName;
    }

    /**
     * @param repositoryName the repositoryName to set
     */
    public void setRepositoryName(String repositoryName)
    {
        this.repositoryName = repositoryName;
    }

    /**
     * @return the sourceRepository
     */
    public boolean isSourceRepository()
    {
        return sourceRepository;
    }

    /**
     * @param sourceRepository the sourceRepository to set
     */
    public void setSourceRepository(boolean sourceRepository)
    {
        this.sourceRepository = sourceRepository;
    }
}
