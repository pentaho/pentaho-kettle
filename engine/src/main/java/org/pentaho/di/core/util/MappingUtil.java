package org.pentaho.di.core.util;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.TransMeta;

public class MappingUtil {

  private MappingUtil() {
    // private constructor to prevent instantiation of utility class
  }

  public static boolean validRepositoryPath( TransMeta transMeta, String fileName ) {
    if ( StringUtils.isNotBlank( fileName ) && fileName.endsWith( ".ktr" ) ) {
      fileName = fileName.replace( ".ktr", "" );
    }

    String transPath = transMeta.environmentSubstitute( fileName );
    String realTransname = transPath;
    String realDirectory = "";
    int index = StringUtils.isBlank( transPath ) ? -1 : transPath.lastIndexOf( "/" );
    if ( index != -1 ) {
      realTransname = transPath.substring( index + 1 );
      realDirectory = transPath.substring( 0, index );
    }

    return !StringUtils.isBlank( realDirectory ) && !StringUtils.isBlank( realTransname );
  }
}
