package org.pentaho.di.repository.filerep;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.w3c.dom.Node;

public class KettleFileRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta {

	public static String REPOSITORY_TYPE_ID = "KettleFileRepository";

	private String baseDirectory;
	private boolean readOnly;

	public KettleFileRepositoryMeta() {
		super(REPOSITORY_TYPE_ID);
	}
	
	public KettleFileRepositoryMeta(String id, String name, String description, String baseDirectory) {
		super(id, name, description);
		this.baseDirectory = baseDirectory;
	}
	
    public RepositoryCapabilities getRepositoryCapabilities() {
    	return new RepositoryCapabilities() {
    		public boolean supportsUsers() { return false; }
    		public boolean isReadOnly() { return readOnly; }
    		public boolean supportsRevisions() { return false; }
    	};
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);
		
		retval.append("  <repository>").append(Const.CR);
		retval.append(super.getXML());
		retval.append("    ").append(XMLHandler.addTagValue("base_directory", baseDirectory));
		retval.append("    ").append(XMLHandler.addTagValue("read_only", readOnly));
		retval.append("  </repository>").append(Const.CR);
        
		return retval.toString();
	}

	public void loadXML(Node repnode, List<DatabaseMeta> databases) throws KettleException
	{
		super.loadXML(repnode, databases);
		try
		{
			baseDirectory = XMLHandler.getTagValue(repnode, "base_directory") ;
			readOnly = "Y".equalsIgnoreCase(XMLHandler.getTagValue(repnode, "read_only"));
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to load Kettle file repository meta object", e);
		}
	}

	/**
	 * @return the baseDirectory
	 */
	public String getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}
