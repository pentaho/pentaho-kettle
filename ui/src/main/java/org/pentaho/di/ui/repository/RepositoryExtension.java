/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2017 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.ui.repository;

import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;

public class RepositoryExtension {

  private UIRepositoryObject repositoryObject;

  public RepositoryExtension( UIRepositoryObject repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }

  public UIRepositoryObject getRepositoryObject() {
    return repositoryObject;
  }

  public void setRepositoryObject( UIRepositoryObject repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }
}
