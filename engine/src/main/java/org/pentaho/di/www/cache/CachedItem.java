/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
