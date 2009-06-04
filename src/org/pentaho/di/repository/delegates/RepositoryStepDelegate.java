package org.pentaho.di.repository.delegates;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

public class RepositoryStepDelegate extends BaseRepositoryDelegate {
	private static Class<?> PKG = StepMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public RepositoryStepDelegate(Repository repository) {
		super(repository);
	}
	
	public synchronized long getStepTypeID(String code) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(Repository.TABLE_R_STEP_TYPE), quote(Repository.FIELD_STEP_TYPE_ID_STEP_TYPE), quote(Repository.FIELD_STEP_TYPE_CODE), code);
	}

	public synchronized long getStepID(String name, long id_transformation) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(Repository.TABLE_R_STEP), quote(Repository.FIELD_STEP_ID_STEP), quote(Repository.FIELD_STEP_NAME), name, quote(Repository.FIELD_STEP_ID_TRANSFORMATION), id_transformation);
	}

	public synchronized String getStepTypeCode(long id_database_type) throws KettleException
	{
		return repository.connectionDelegate.getStringWithID(quoteTable(Repository.TABLE_R_STEP_TYPE), quote(Repository.FIELD_STEP_TYPE_ID_STEP_TYPE), id_database_type, quote(Repository.FIELD_STEP_TYPE_CODE));
	}

	public RowMetaAndData getStep(long id_step) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_STEP), quote(Repository.FIELD_STEP_ID_STEP), id_step);
	}

	public RowMetaAndData getStepType(long id_step_type) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_STEP_TYPE), quote(Repository.FIELD_STEP_TYPE_ID_STEP_TYPE), id_step_type);
	}

	public RowMetaAndData getStepAttribute(long id_step_attribute) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_STEP_ATTRIBUTE), quote(Repository.FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE), id_step_attribute);
	}

    /**
     * Create a new step by loading the metadata from the specified repository.  
     * @param rep
     * @param id_step
     * @param databases
     * @param counters
     * @param partitionSchemas
     * @throws KettleException
     */
	public StepMeta loadStepMeta( long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters, List<PartitionSchema> partitionSchemas) throws KettleException
	{
        StepMeta stepMeta = new StepMeta();
        StepLoader steploader = StepLoader.getInstance();

		try
		{
			RowMetaAndData r = getStep(id_step);
			if (r!=null)
			{
				stepMeta.setID(id_step);
				
				stepMeta.setName( r.getString("NAME", null) ); //$NON-NLS-1$
				stepMeta.setDescription( r.getString("DESCRIPTION", null) ); //$NON-NLS-1$
				
				long id_step_type = r.getInteger("ID_STEP_TYPE", -1L); //$NON-NLS-1$
				RowMetaAndData steptyperow = getStepType(id_step_type);
				
				stepMeta.setStepID( steptyperow.getString("CODE", null) ); //$NON-NLS-1$
				stepMeta.setDistributes( r.getBoolean("DISTRIBUTE", true) ); //$NON-NLS-1$
				stepMeta.setCopies( (int)r.getInteger("COPIES", 1) ); //$NON-NLS-1$
				int x = (int)r.getInteger("GUI_LOCATION_X", 0); //$NON-NLS-1$
				int y = (int)r.getInteger("GUI_LOCATION_Y", 0); //$NON-NLS-1$
				stepMeta.setLocation( new Point(x,y) );
				stepMeta.setDraw( r.getBoolean("GUI_DRAW", false) ); //$NON-NLS-1$
				
				// Generate the appropriate class...
				StepPlugin sp = steploader.findStepPluginWithID(stepMeta.getStepID());
                if (sp!=null)
                {
                	stepMeta.setStepMetaInterface( BaseStep.getStepInfo(sp, steploader) );
                }
                else
                {
                    throw new KettleStepLoaderException(BaseMessages.getString(PKG, "StepMeta.Exception.UnableToLoadClass",stepMeta.getStepID()+Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }

				stepMeta.setStepMetaInterface( BaseStep.getStepInfo(sp, steploader) );
				if (stepMeta.getStepMetaInterface()!=null)
				{
					// Read the step info from the repository!
					stepMeta.getStepMetaInterface().readRep(repository, stepMeta.getID(), databases, counters);
				}
                
                // Get the partitioning as well...
				stepMeta.setStepPartitioningMeta( loadStepPartitioningMeta(stepMeta.getID()) );
                
                // Get the cluster schema name
                stepMeta.setClusterSchemaName( repository.getStepAttributeString(id_step, "cluster_schema") );
                
                // Done!
                return stepMeta;
			}
			else
			{
				throw new KettleException(BaseMessages.getString(PKG, "StepMeta.Exception.StepInfoCouldNotBeFound",String.valueOf(id_step))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "StepMeta.Exception.StepCouldNotBeLoaded",String.valueOf(stepMeta.getID())), dbe); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	public void saveStepMeta(StepMeta stepMeta, long id_transformation) throws KettleException
	{
        LogWriter log = LogWriter.getInstance();
        
		try
		{
			log.logDebug(toString(), BaseMessages.getString(PKG, "StepMeta.Log.SaveNewStep")); //$NON-NLS-1$
			// Insert new Step in repository
			stepMeta.setID(insertStep(	id_transformation,
									stepMeta.getName(), 
									stepMeta.getDescription(),
									stepMeta.getStepID(),
									stepMeta.isDistributes(),
									stepMeta.getCopies(),
									stepMeta.getLocation()==null?-1:stepMeta.getLocation().x,
									stepMeta.getLocation()==null?-1:stepMeta.getLocation().y,
									stepMeta.isDrawn()
								)
					);
            
            // Save partitioning selection for the step
			//
			repository.stepDelegate.saveStepPartitioningMeta(stepMeta.getStepPartitioningMeta(), id_transformation, stepMeta.getID());
	
			// The id_step is known, as well as the id_transformation
			// This means we can now save the attributes of the step...
			//
			log.logDebug(toString(), BaseMessages.getString(PKG, "StepMeta.Log.SaveStepDetails")); //$NON-NLS-1$
			stepMeta.getStepMetaInterface().saveRep(repository, id_transformation, stepMeta.getID());
            
            // Save the name of the clustering schema that was chosen.
			//
            repository.saveStepAttribute(id_transformation, stepMeta.getID(), "cluster_schema", stepMeta.getClusterSchema()==null?"":stepMeta.getClusterSchema().getName());
		}
		catch(KettleException e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "StepMeta.Exception.UnableToSaveStepInfo",String.valueOf(id_transformation)), e); //$NON-NLS-1$
		}
	}


    public void saveStepErrorMeta(StepErrorMeta meta, long id_transformation, long id_step) throws KettleException
    {
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_source_step", meta.getSourceStep()!=null ? meta.getSourceStep().getName() : "");
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_target_step", meta.getTargetStep()!=null ? meta.getTargetStep().getName() : "");
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_is_enabled",  meta.isEnabled());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_nr_valuename",  meta.getNrErrorsValuename());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_descriptions_valuename",  meta.getErrorDescriptionsValuename());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_fields_valuename",  meta.getErrorFieldsValuename());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_codes_valuename",  meta.getErrorCodesValuename());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_max_errors",  meta.getMaxErrors());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_max_pct_errors",  meta.getMaxPercentErrors());
        repository.saveStepAttribute(id_transformation, id_step, "step_error_handling_min_pct_rows",  meta.getMinPercentRows());
    }
    
    public StepErrorMeta loadStepErrorMeta(VariableSpace variables, StepMeta stepMeta, List<StepMeta> steps) throws KettleException
    {
    	StepErrorMeta meta = new StepErrorMeta(variables, stepMeta);
    	
    	meta.setTargetStep(  StepMeta.findStep( steps, repository.getStepAttributeString(stepMeta.getID(), "step_error_handling_target_step") ) );
    	meta.setEnabled( repository.getStepAttributeBoolean(stepMeta.getID(), "step_error_handling_is_enabled") );
    	meta.setNrErrorsValuename( repository.getStepAttributeString(stepMeta.getID(), "step_error_handling_nr_valuename") );
    	meta.setErrorDescriptionsValuename(repository.getStepAttributeString(stepMeta.getID(), "step_error_handling_descriptions_valuename") );
    	meta.setErrorFieldsValuename( repository.getStepAttributeString(stepMeta.getID(), "step_error_handling_fields_valuename") );
    	meta.setErrorCodesValuename( repository.getStepAttributeString(stepMeta.getID(), "step_error_handling_codes_valuename") );
    	meta.setMaxErrors( repository.getStepAttributeInteger(stepMeta.getID(), "step_error_handling_max_errors") );
    	meta.setMaxPercentErrors( (int) repository.getStepAttributeInteger(stepMeta.getID(), "step_error_handling_max_pct_errors") );
    	meta.setMinPercentRows( repository.getStepAttributeInteger(stepMeta.getID(), "step_error_handling_min_pct_rows") );
    	
    	return meta;
    }

    
    public StepPartitioningMeta loadStepPartitioningMeta(long id_step) throws KettleException
    {
    	StepPartitioningMeta stepPartitioningMeta = new StepPartitioningMeta();
    	
    	stepPartitioningMeta.setPartitionSchemaName( repository.getStepAttributeString(id_step, "PARTITIONING_SCHEMA") );
        String methodCode   = repository.getStepAttributeString(id_step, "PARTITIONING_METHOD");
        stepPartitioningMeta.setMethod( StepPartitioningMeta.getMethod(methodCode) );
        if( stepPartitioningMeta.getPartitioner() != null ) {
        	stepPartitioningMeta.getPartitioner().loadRep( repository, id_step);
        }
        stepPartitioningMeta.hasChanged(true);
        
        return stepPartitioningMeta;
    }


    /**
     * Saves partitioning properties in the repository for the given step.
     * 
     * @param meta the partitioning metadata to store.
     * @param id_transformation the ID of the transformation
     * @param id_step the ID of the step
     * @throws KettleDatabaseException In case anything goes wrong
     * 
     */
    public void saveStepPartitioningMeta(StepPartitioningMeta meta, long id_transformation, long id_step) throws KettleException
    {
        repository.saveStepAttribute(id_transformation, id_step, "PARTITIONING_SCHEMA",    meta.getPartitionSchema()!=null?meta.getPartitionSchema().getName():""); // selected schema
        repository.saveStepAttribute(id_transformation, id_step, "PARTITIONING_METHOD",    meta.getMethodCode());          // method of partitioning
        if( meta.getPartitioner() != null ) {
        	meta.getPartitioner().saveRep( repository, id_transformation, id_step);
        }
    }
    
    
	public synchronized long insertStep(long id_transformation, String name, String description, String steptype,
			boolean distribute, long copies, long gui_location_x, long gui_location_y, boolean gui_draw)
			throws KettleException
	{
		long id = repository.connectionDelegate.getNextStepID();

		long id_step_type = getStepTypeID(steptype);

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(Repository.FIELD_STEP_ID_STEP, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(Repository.FIELD_STEP_DESCRIPTION, ValueMetaInterface.TYPE_STRING), description);
		table.addValue(new ValueMeta(Repository.FIELD_STEP_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_step_type));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_DISTRIBUTE, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(distribute));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_COPIES, ValueMetaInterface.TYPE_INTEGER), new Long(copies));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta(Repository.FIELD_STEP_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(gui_draw));

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), Repository.TABLE_R_STEP);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}


	public synchronized int getNrSteps(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_STEP)+" WHERE "+quote(Repository.FIELD_STEP_ID_TRANSFORMATION)+" = " + id_transformation;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}


	public synchronized int getNrStepAttributes(long id_step) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_STEP_ATTRIBUTE)+" WHERE "+quote(Repository.FIELD_STEP_ATTRIBUTE_ID_STEP)+" = " + id_step;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

    
}
