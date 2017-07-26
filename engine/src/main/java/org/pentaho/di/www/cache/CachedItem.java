/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Pentaho : http://www.pentaho.com
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
import java.time.LocalDate;

public class CachedItem {

  private String logId;
  private File file;
  private LocalDate exceedTime;

  public CachedItem( String logId, File file ) {
    this.logId = logId;
    this.file = file;
    exceedTime = LocalDate.now().plusDays( 1 );
  }

  public String getLogId() {
    return logId;
  }

  public void setLogId( String logId ) {
    this.logId = logId;
  }

  public File getFile() {
    return file;
  }

  public void setFile( File file ) {
    this.file = file;
  }


  public LocalDate getExceedTime() {
    return exceedTime;
  }

  public void setExceedTime( LocalDate exceedTime ) {
    this.exceedTime = exceedTime;
  }
}
