package org.pentaho.di.trans.steps.infobrightoutput;

import java.sql.Connection;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.infobright.etl.model.BrighthouseRecord;
import com.infobright.etl.model.DataFormat;
import com.infobright.io.InfobrightNamedPipeLoader;
import com.infobright.logging.EtlLogger;


/**
 */
public class InfobrightLoaderStepData extends BaseStepData implements StepDataInterface {
  
  private Database db;  // only for initial use and the loader.
  private String[] requiredFields;
  com.infobright.io.InfobrightNamedPipeLoader loader;
  BrighthouseRecord record;
  public RowMetaInterface requiredRowMeta;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface insertRowMeta;
  
  /**
   * Default constructor.  Does nothing special.
   */
  public InfobrightLoaderStepData() {
    super();
  }

  void databaseSetup(InfobrightLoaderMetadata meta, VariableSpace space) throws KettleException {
    
    db = new Database(meta.getDatabaseMeta());
    db.connect();

    // FIXME: This will fail if the first row of the table contains a value that
    // cannot be read by Java. For example, a DATE field that contains the value
    // '0000-00-00'. In this case, the Kettle error message will misleadingly say
    // that the table doesn't exist. There doesn't seem to be any workaround.
    // See Pentaho JIRA: PDI-2117.
    requiredRowMeta = meta.getRequiredFields(space); 
    requiredFields = requiredRowMeta.getFieldNames();
    
    try {
      // once the loader is built, this db connection cannot be used by this thread anymore.
      // the loader is using it and any other uses of the connection will block.
      if (meta.getInfobrightProductType() == null) {
        meta.setDataFormat(DataFormat.TXT_VARIABLE); // default for ICE
      }
      DataFormat dataFormat = DataFormat.valueForDisplayName(meta.getInfobrightProductType());

      Connection conn = db.getConnection();
      String tableName = meta.getTablename();
      EtlLogger logger = meta.getLogger();
      loader = new InfobrightNamedPipeLoader(tableName, conn, logger, dataFormat);
      record = loader.createRecord(false); // TODO set to true to support error path
      loader.start();
      
    } catch (Exception e) {
      db.disconnect();
      db = null;
      throw new KettleDatabaseException(e);
    }
  }

  String[] getRequiredFields() {
    return requiredFields;
  }
  
  void dispose() throws Exception {
    try {
      if (loader != null) {
        loader.stop();
      }
      loader = null;
    }
    finally {
      if (db != null) {
        db.disconnect();
      }
      db = null;
    }
  }
}
