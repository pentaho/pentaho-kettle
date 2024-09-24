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

package org.pentaho.di.www.cache;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;

public class CachedItem implements Serializable {

  private File file;
  private LocalDate exceedTime;
  private int from;

  public CachedItem( File file, int from ) {
    this.file = file;
    this.from = from;
    exceedTime = LocalDate.now().plusDays( 1 );
  }

  public File getFile() {
    return file;
  }

  public void setFile( File file ) {
    this.file = file;
  }

  public int getFrom() {
    return from;
  }

  public void setFrom( int from ) {
    this.from = from;
  }

  public LocalDate getExceedTime() {
    return exceedTime;
  }

  public void setExceedTime( LocalDate exceedTime ) {
    this.exceedTime = exceedTime;
  }
}
