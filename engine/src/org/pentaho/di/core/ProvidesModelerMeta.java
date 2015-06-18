package org.pentaho.di.core;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.List;

public interface ProvidesModelerMeta extends ProvidesDatabaseConnectionInformation {
  RowMeta getRowMeta( StepDataInterface stepData );
  List<String> getDatabaseFields();
  List<String> getStreamFields();
}
