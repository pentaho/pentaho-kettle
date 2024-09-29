/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.injection;

import java.util.List;

public class MetaBeanLevel2 extends MetaBeanLevel2Base {

  @Injection( name = "SEPARATOR" )
  private String separator;

  @InjectionDeep
  MetaBeanLevel3[] files;

  @InjectionDeep( prefix = "SECOND" )
  private MetaBeanLevel4 filesSecond;
  @InjectionDeep( prefix = "THIRD" )
  private MetaBeanLevel4 filesThird;

  @Injection( name = "FILENAME_ARRAY", group  = "FILENAME_LINES2" )
  String[] filenames;

  @Injection( name = "ASCENDING_LIST", group = "FILENAME_LINES2" )
  List<Boolean> ascending;

  public List<Boolean> getAscending() {
    return ascending;
  }

  public void setAscending( List<Boolean> ascending ) {
    this.ascending = ascending;
  }

  public void setFilenames( String[] filenames ) {
    this.filenames = filenames;
  }

  public void setFilesThird( MetaBeanLevel4 filesThird ) {
    this.filesThird = filesThird;
  }

  public String[] getFilenames() {
    return filenames;
  }

  public void setSeparator( String separator ) {
    this.separator = separator;
  }

  public String getSeparator() {
    return separator;
  }

  public MetaBeanLevel3[] getFiles() {
    return files;
  }
}
