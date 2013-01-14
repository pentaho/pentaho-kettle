package org.pentaho.di.starmodeler;

import org.pentaho.di.core.Const;

public enum DimensionType {
  SLOWLY_CHANGING_DIMENSION,
  JUNK_DIMENSION,
  DATE,
  TIME,
  OTHER,
  ;
  
  
  public static DimensionType getDimensionType(String typeString) {
    if (Const.isEmpty(typeString)) {
      return DimensionType.OTHER;
    }
    return DimensionType.valueOf(typeString);
  }

}
