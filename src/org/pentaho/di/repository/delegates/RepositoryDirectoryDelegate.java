package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleDatabaseRepository;
import org.pentaho.di.repository.directory.RepositoryDirectory;

public class RepositoryDirectoryDelegate extends BaseRepositoryDelegate {
	private static Class<?> PKG = RepositoryDirectory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryDirectoryDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public RowMetaAndData getDirectory(long id_directory) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY), quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY), id_directory);
	}
	
	public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException
    {
        try
        {
            root.clear();
            
            long subids[] = repository.getSubDirectoryIDs( root.getID() );
            for (int i=0;i<subids.length;i++)
            {
                RepositoryDirectory subdir = new RepositoryDirectory();
                loadRepositoryDirectory(subdir, subids[i]);
                root.addSubdirectory(subdir);
            }
            
            return root;
        }
        catch(Exception e)
        {
            throw new KettleException("An error occured loading the directory tree from the repository", e);
        }
    }
	
	public void loadRepositoryDirectory(RepositoryDirectory repositoryDirectory, long id_directory) throws KettleException
	{
		try
		{
			RowMetaAndData row = getDirectory(id_directory);
			if (row!=null)
			{
				repositoryDirectory.setID(id_directory);
				
				// Content?
				//
				repositoryDirectory.setDirectoryName( row.getString("DIRECTORY_NAME", null));
				
				// The sub-directories?
				//
				long subids[] = repository.getSubDirectoryIDs( repositoryDirectory.getID() );
				for (int i=0;i<subids.length;i++)
				{
					RepositoryDirectory subdir = new RepositoryDirectory();
					loadRepositoryDirectory(subdir, subids[i]);
					repositoryDirectory.addSubdirectory(subdir);
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "Repository.LoadRepositoryDirectory.ErrorLoading.Exception"), e);
		}
	}
	

	
    public synchronized RepositoryDirectory refreshRepositoryDirectoryTree() throws KettleException
    {
        try
        {
        	RepositoryDirectory tree = new RepositoryDirectory();
        	loadRepositoryDirectory(tree, tree.getID());
            repository.setDirectoryTree(tree);
            return tree;
        }
        catch (KettleException e)
        {
            repository.setDirectoryTree( new RepositoryDirectory() );
            throw new KettleException("Unable to read the directory tree from the repository!", e);
        }
    }

    

    public synchronized int getNrDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized long insertDirectory(long id_directory_parent, RepositoryDirectory dir) throws KettleException
	{
		long id = repository.connectionDelegate.getNextDirectoryID();

		String tablename = KettleDatabaseRepository.TABLE_R_DIRECTORY;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_parent));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), dir.getDirectoryName());

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), tablename);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}

	public synchronized void deleteDirectory(long id_directory) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;
		repository.connectionDelegate.getDatabase().execStatement(sql);
	}

	public synchronized void renameDirectory(long id_directory, String name) throws KettleException
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), name);

		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" SET "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;

		log.logBasic(toString(), "sql = [" + sql + "]");
		log.logBasic(toString(), "row = [" + r + "]");

		repository.connectionDelegate.getDatabase().execStatement(sql, r.getRowMeta(), r.getData());
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0);
		}

		return retval;
	}
	
	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleException
	{
		return repository.connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME));
	}
    
	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException
	{
		try
		{
			long id_directory_parent = 0;
			if (dir.getParent()!=null) id_directory_parent=dir.getParent().getID();
			
			dir.setID(insertDirectory(id_directory_parent, dir));
			
            LogWriter.getInstance().logDetailed(repository.getName(), "New id of directory = "+dir.getID());
                        
			repository.commit();
            
            // Reload the complete directory tree from the parent down...
			//
            repository.loadRepositoryDirectoryTree(dir.findRoot());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save directory ["+dir+"] in the repository", e);
		}
	}
	
	public void delRepositoryDirectory(RepositoryDirectory dir) throws KettleException
	{
		try
		{
			String trans[]   = repository.getTransformationNames(dir.getID());
			String jobs[]    = repository.getJobNames(dir.getID());
			long[] subDirectories = repository.getSubDirectoryIDs(dir.getID());
			if (trans.length==0 && jobs.length==0 && subDirectories.length==0)
			{
				repository.directoryDelegate.deleteDirectory(dir.getID());
			}
			else
			{
                throw new KettleException("This directory is not empty!");
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error deleting repository directory", e);
		}
	}

	public void renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException
	{
		try
		{
			renameDirectory(dir.getID(), dir.getDirectoryName());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to rename the specified repository directory ["+dir+"]", e);
		}
	}
	
	
	/**
	 * Create a new directory, possibly by creating several sub-directies of / at the same time.
	 * 
	 * @param parentDirectory the parent directory
	 * @param directoryPath The path to the new Repository Directory, to be created.
	 * @return The created sub-directory
	 * @throws KettleException In case something goes wrong
	 */
	public RepositoryDirectory createRepositoryDirectory(RepositoryDirectory parentDirectory, String directoryPath) throws KettleException
	{
	    String path[] = Const.splitPath(directoryPath, RepositoryDirectory.DIRECTORY_SEPARATOR);
	    
	    RepositoryDirectory parent = parentDirectory;
	    for (int level=1;level<=path.length;level++)
	    {
	        String subPath[] = new String[level];
	        for (int i=0;i<level;i++)
	        {
	            subPath[i] = path[i];
	        }
	 
	        RepositoryDirectory rd = parent.findDirectory(subPath);
	        if (rd==null)
	        {
	            // This directory doesn't exists, let's add it!
	        	//
	            rd = new RepositoryDirectory(parent, subPath[level-1]);
	            saveRepositoryDirectory(rd);

	            // Don't forget to add this directory to the tree!
	            //
	            parent.addSubdirectory(rd);
	            
		        parent = rd;
	        }
	        else
	        {
	            parent = rd;   
	        }
	    }
	    return parent;
	}



}
