package org.pentaho.di.trans.steps.tableagilemart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.metadata.registry.Entity;
import org.pentaho.metadata.registry.IMetadataRegistry;
import org.pentaho.metadata.registry.RegistryFactory;
import org.pentaho.metadata.registry.util.RegistryUtil;

public class AgileMartUtil {

	public void updateMetadata( ProvidesDatabaseConnectionInformation dpci, long rowCount ) {
		   // try to update the metadata registry

		RegistryFactory factory = RegistryFactory.getInstance();
		    IMetadataRegistry registry = factory.getMetadataRegistry();
		    RegistryUtil util = new RegistryUtil();
		    String databaseName = dpci.getDatabaseMeta().getName();
		    String schemaName = dpci.getSchemaName();
		    String tableName = dpci.getTableName();
		    Entity entity = util.getTableEntity(databaseName.toLowerCase(), (schemaName==null) ? null : schemaName.toLowerCase(), tableName.toLowerCase(), false);
		    if( entity != null ) {
				System.out.println("Util.updateMetadata writing "+util.generateTableId(dpci.getDatabaseMeta().getName(), dpci.getSchemaName(), dpci.getTableName())+" rowCount="+rowCount);
		    	if( rowCount == -1 ) {
		    		// the table has been emptied
			    	util.setAttribute(entity, "rowcount", 0);
		    	} else {
		    		// add an offset
			    	util.updateAttribute(entity, "rowcount", rowCount);
		    	}
		    	DateFormat fmt = new SimpleDateFormat();
		    	Date now = new Date();
		    	util.setAttribute(entity, "lastupdate", fmt.format(now));
		    	util.setAttribute(entity, "lastupdatetick", now.getTime());
		    } else {
				System.out.println("Util.updateMetadata failed writing "+util.generateTableId(dpci.getDatabaseMeta().getName(), dpci.getSchemaName(), dpci.getTableName()));
		    	
		    }
		    try {
				registry.commit();
			} catch (Exception e) {
				// no biggie
			}
	}	
	
}
