/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.ObjectLocationSpecificationMethod;

/**
 * Interface indicating that a step has repository references to another file(s). The goal is
 * updating directories during repo importing
 */
public interface HasRepositoryDirectories {

  /**
   * If step has reference(s) to another transformation(s)/job(s) returns an array of repository directories.
   * An implementation is considered to define the array order itself.
   *
   * @return String array of repository directories
   */
  public String[] getDirectories();

  /**
   * If step has reference(s) to another transformation(s)/job(s) sets updated repository directories from
   * incoming String array. An implementation is considered to define the array order itself.
   *
   * @param directory Array of updated rep directories to set
   */
  public void setDirectories( String[] directory );

  /**
   * If step has reference(s) to another transformation(s)/job(s) returns an array of specification method(s)
   * defining the type of an access to a referenced file.
   * An implementation is considered to define the array order itself.
   *
   * @return String array of specification method(s)
   */
  public ObjectLocationSpecificationMethod[] getSpecificationMethods();

}
