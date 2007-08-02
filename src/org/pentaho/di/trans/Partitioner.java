package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.w3c.dom.Node;

public interface Partitioner {

	public abstract Partitioner getInstance();
	
	public int getPartition(RowMetaInterface rowMeta, Object[] r ) throws KettleException;
	
	public void setMeta(StepPartitioningMeta meta);

	public String getId( );
	
	public String getDescription( );

	public void setId( String id );
	
	public void setDescription( String description );
	
	public String getDialogClassName();
	
	public Partitioner clone();
	
    public String getXML();
    
	public void loadXML(Node partitioningMethodNode) throws KettleXMLException;

    /**
     * Saves partitioning properties in the repository for the given step.
     * @param rep the repository to save in
     * @param id_transformation the ID of the transformation
     * @param id_step the ID of the step
     * @throws KettleDatabaseException In case anything goes wrong
     */
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException;

    public void loadRep(Repository rep, long id_step) throws KettleException;

}
