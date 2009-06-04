package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;

public class RepositoryDirectoryDelegate extends BaseRepositoryDelegate {
	private static Class<?> PKG = RepositoryDirectory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryDirectoryDelegate(Repository repository) {
		super(repository);
	}
	
	public RowMetaAndData getDirectory(long id_directory) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_DIRECTORY), quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY), id_directory);
	}
	
	public void loadRepositoryDirectoryTree(RepositoryDirectory root) throws KettleException
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
	

	
    public synchronized void refreshRepositoryDirectoryTree() throws KettleException
    {
        try
        {
        	RepositoryDirectory tree = new RepositoryDirectory();
        	loadRepositoryDirectory(tree, tree.getID());
            repository.setDirectoryTree(tree);
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

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_DIRECTORY)+" WHERE "+quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
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

		String tablename = Repository.TABLE_R_DIRECTORY;
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(Repository.FIELD_DIRECTORY_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(Repository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_parent));
		table.addValue(new ValueMeta(Repository.FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), dir.getDirectoryName());

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), tablename);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}

	public synchronized void deleteDirectory(long id_directory) throws KettleException
	{
		String sql = "DELETE FROM "+quoteTable(Repository.TABLE_R_DIRECTORY)+" WHERE "+quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;
		repository.connectionDelegate.getDatabase().execStatement(sql);
	}

	public synchronized void renameDirectory(long id_directory, String name) throws KettleException
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta(Repository.FIELD_DIRECTORY_DIRECTORY_NAME, ValueMetaInterface.TYPE_STRING), name);

		String sql = "UPDATE "+quoteTable(Repository.TABLE_R_DIRECTORY)+" SET "+quote(Repository.FIELD_DIRECTORY_DIRECTORY_NAME)+" = ? WHERE "+quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY)+" = " + id_directory;

		log.logBasic(toString(), "sql = [" + sql + "]");
		log.logBasic(toString(), "row = [" + r + "]");

		repository.connectionDelegate.getDatabase().execStatement(sql, r.getRowMeta(), r.getData());
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_DIRECTORY)+" WHERE "+quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0);
		}

		return retval;
	}
	
	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleException
	{
		return repository.connectionDelegate.getIDs("SELECT "+quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY)+" FROM "+quoteTable(Repository.TABLE_R_DIRECTORY)+" WHERE "+quote(Repository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT)+" = " + id_directory+" ORDER BY "+quote(Repository.FIELD_DIRECTORY_DIRECTORY_NAME));
	}



}
