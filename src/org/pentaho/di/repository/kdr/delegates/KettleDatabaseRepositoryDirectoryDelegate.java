package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryDirectoryDelegate extends KettleDatabaseRepositoryBaseDelegate {
	private static Class<?> PKG = RepositoryDirectory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public KettleDatabaseRepositoryDirectoryDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public RowMetaAndData getDirectory(ObjectId id_directory) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY), quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY), id_directory);
	}
	
	public RepositoryDirectory loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException
    {
        try
        {
        	root.clear();
            ObjectId subids[] = repository.getSubDirectoryIDs( root.getObjectId() );
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
	
	public void loadRepositoryDirectory(RepositoryDirectory repositoryDirectory, ObjectId id_directory) throws KettleException
	{
		if (id_directory==null) {
			// This is the root directory, id = OL
			id_directory = new LongObjectId(0L);
		}
		
		try
		{
			RowMetaAndData row = getDirectory(id_directory);
			if (row!=null)
			{
				repositoryDirectory.setObjectId(id_directory);
				
				// Content?
				//
				repositoryDirectory.setName( row.getString("DIRECTORY_NAME", null));
				
				// The sub-directories?
				//
				ObjectId subids[] = repository.getSubDirectoryIDs( repositoryDirectory.getObjectId() );
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
	


	/*
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
    */

    

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

	private synchronized ObjectId insertDirectory(ObjectId id_directory_parent, RepositoryDirectory dir) throws KettleException
	{
		ObjectId id = repository.connectionDelegate.getNextDirectoryID();

		String tablename = KettleDatabaseRepository.TABLE_R_DIRECTORY;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), id);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT, ValueMetaInterface.TYPE_INTEGER), id_directory_parent);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), dir.getName());

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), tablename);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}

	public synchronized void deleteDirectory(ObjectId id_directory) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;
		repository.connectionDelegate.getDatabase().execStatement(sql);
	}

	public synchronized void renameDirectory(ObjectId id_directory, String name) throws KettleException
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), name);

		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" SET "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;

		log.logBasic("sql = [" + sql + "]");
		log.logBasic("row = [" + r + "]");

		repository.connectionDelegate.getDatabase().execStatement(sql, r.getRowMeta(), r.getData());
	}

	public synchronized int getNrSubDirectories(ObjectId id_directory) throws KettleException
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
	
	public synchronized ObjectId[] getSubDirectoryIDs(ObjectId id_directory) throws KettleException
	{
		return repository.connectionDelegate.getIDs("SELECT "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DIRECTORY)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory+" ORDER BY "+quote(KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME));
	}
    
	public void saveRepositoryDirectory(RepositoryDirectory dir) throws KettleException
	{
		try
		{
			ObjectId id_directory_parent = null;
			if (dir.getParent()!=null) {
				id_directory_parent=dir.getParent().getObjectId();
			}
			
			dir.setObjectId(insertDirectory(id_directory_parent, dir));
			
            log.logDetailed("New id of directory = "+dir.getObjectId());
                        
			repository.commit();
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
			String trans[]   = repository.getTransformationNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
			String jobs[]    = repository.getJobNames(dir.getObjectId(), false); // TODO : include or exclude deleted objects?
			ObjectId[] subDirectories = repository.getSubDirectoryIDs(dir.getObjectId());
			if (trans.length==0 && jobs.length==0 && subDirectories.length==0)
			{
				repository.directoryDelegate.deleteDirectory(dir.getObjectId());
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

	public ObjectId renameRepositoryDirectory(RepositoryDirectory dir) throws KettleException
	{
		try
		{
			renameDirectory(dir.getObjectId(), dir.getName());
			return dir.getObjectId(); // doesn't change in this specific case.
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
