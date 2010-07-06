package org.pentaho.di.ui.trans.step;

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 8, 2010
 * Time: 10:30:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface StepTableDataObject {
  String getName();
  String getDataType();
  int getLength();
  int getPrecision();
  StepTableDataObject createNew( ValueMetaInterface val );
}
