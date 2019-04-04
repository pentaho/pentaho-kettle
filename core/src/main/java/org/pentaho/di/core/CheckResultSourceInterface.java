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

package org.pentaho.di.core;

import org.pentaho.di.repository.ObjectId;

/**
 *
 * Implementing classes of this interface can provide more information about the source of the CheckResult remark.
 *
 * @author mbatchel 6/25/07
 */
public interface CheckResultSourceInterface {
  /**
   * @return The name of the source generating the CheckResult
   */
  public String getName();

  /**
   * @return The description of the source generating the CheckResult
   */
  public String getDescription();

  /**
   * @return The ID of the source generating the CheckResult
   */
  public ObjectId getObjectId();

  /**
   * @return The Type ID of the source generating the CheckResult. The Type ID is the system-defined type identifier
   *         (like TRANS or SORT).
   */
  public String getTypeId();
}
