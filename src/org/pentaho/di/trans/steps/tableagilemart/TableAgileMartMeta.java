/**
 * The Pentaho proprietary code is licensed under the terms and conditions
 * of the software license agreement entered into between the entity licensing
 * such code and Pentaho Corporation. 
 */
package org.pentaho.di.trans.steps.tableagilemart;

import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

@Step(id = "TableAgileMart", image = "plugins/steps/MonetDBAgileMartPlugin/icon.png", name = "Table Agile Mart", description="Table Agile Mart", categoryDescription="Agile BI")
public class TableAgileMartMeta extends TableOutputMeta {

	protected long rowLimit = getLongProperty("AgileBIRowLimit", 100000); // have a nice default
	
	public TableAgileMartMeta() {
		setTableName("tmp_agile_table");
	}
	
	public long getRowLimit() {
		return rowLimit;
	}

	public void setRowLimit( long limit ) {
		rowLimit = limit;
	}
	
	public static String getStringProperty(String name, String defaultValue ) {

		String value = Props.isInitialized() ? Props.getInstance().getProperty(name) : null;
		if( value == null ) {
			value = defaultValue;
		}
		return value;
		
	}
	
	public static long getLongProperty(String name, long defaultValue ) {

		String valueStr = Props.isInitialized() ? Props.getInstance().getProperty(name) : null;
		try {
			long value = Long.parseLong(valueStr);
			return value;
		} catch (NumberFormatException e) {
			// the value for this property is not a valid number
		}
		return defaultValue;
	}
	
	protected void setupDatabaseMeta() {

		if( this.getDatabaseMeta() == null ) {
			if( getParentStepMeta() != null ) {
				TransMeta transMeta = getParentStepMeta().getParentTransMeta();
				if( transMeta != null ) {
					setDatabaseMeta(transMeta.findDatabase(transMeta.environmentSubstitute(getStringProperty("AgileBIDatabase", "AgileBI"))));
				}
			}
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{

		setupDatabaseMeta();
		
		((TableAgileMartMeta)stepMeta.getStepMetaInterface()).setDatabaseMeta(this.getDatabaseMeta());

		return new TableAgileMart(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public Object clone()
	{
		TableAgileMartMeta retval = (TableAgileMartMeta)super.clone();
		
		return retval;
	}
	
	@Override
	public void setDefault()
	{
		
		setupDatabaseMeta();
		
		allocate(0);
	}
	
	@Override
	public String getXML()
	{
		setupDatabaseMeta();
		return super.getXML();
	}	
}
