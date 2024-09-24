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
