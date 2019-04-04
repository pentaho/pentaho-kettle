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

package org.pentaho.di.repository;

import java.util.Date;

/**
 * A revision is simply a name, a commit comment and a date
 *
 * @author matt
 *
 */
public interface ObjectRevision {

  /**
   * @return The internal name or number of the revision
   */
  public String getName();

  /**
   * @return The creation date of the revision
   */
  public Date getCreationDate();

  /**
   * @return The revision comment
   */
  public String getComment();

  /**
   * @return The user that caused the revision
   */
  public String getLogin();

}
