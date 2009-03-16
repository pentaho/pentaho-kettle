package org.pentaho.di.trans.steps.infobrightoutput;

import org.pentaho.di.core.database.DatabaseMeta;

public interface InfobrightLoaderDialogInterface {
  public DatabaseMeta getDatabaseMetadata();

  public String getDialogTitle();
}