package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class ModPartitioner extends BasePartitioner {

	private String fieldName; 
	protected int partitionColumnIndex = -1;
	
	public ModPartitioner(  ) {
		super( );
	}

	public Partitioner getInstance() 
	{
		Partitioner partitioner = new ModPartitioner();
		partitioner.setId( getId() );
		partitioner.setDescription( getDescription() );
		return partitioner;

	}

	public String getDialogClassName()
	{
		return "org.pentaho.di.ui.trans.dialog.ModPartitionerDialog";
	}

		public int getPartition(RowMetaInterface rowMeta, Object[] row ) throws KettleException
		{
			init(rowMeta);

	        if (partitionColumnIndex < 0)
	        {
	            partitionColumnIndex = rowMeta.indexOfValue(fieldName);
	            if (partitionColumnIndex < 0) { 
	            	throw new KettleStepException("Unable to find partitioning field name [" + fieldName + "] in the output row..." + rowMeta); 
	            }
	        }

            Long value = rowMeta.getInteger(row, partitionColumnIndex);
            
            int targetLocation = (int)(value.longValue() % nrPartitions);

			return targetLocation;
		}

		public String getDescription() {
			return "Mod partitioner";
		}

		public String getXML()
		{
	        StringBuffer xml = new StringBuffer(150);
	        xml.append("           ").append(XMLHandler.addTagValue("field_name", fieldName));
	        return xml.toString();
		}

		public void loadXML(Node partitioningMethodNode)
			throws KettleXMLException
		{
	        fieldName = XMLHandler.getTagValue(partitioningMethodNode, "field_name");
		}

	    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	    {
	        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_FIELDNAME", fieldName);               // The fieldname to partition on 
	    }

	    public void loadRep(Repository rep, long id_step) throws KettleException
	    {
	        fieldName = rep.getStepAttributeString(id_step, "PARTITIONING_FIELDNAME");
	    }

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

}
