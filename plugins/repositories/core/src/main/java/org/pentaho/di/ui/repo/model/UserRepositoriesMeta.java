/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2016-2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.pentaho.di.ui.repo.model;

import org.eclipse.rap.rwt.SingletonUtil;
import org.pentaho.di.core.WebSpoonUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;

public class UserRepositoriesMeta {
  private RepositoryMeta currentRepository;
  private RepositoryMeta connectedRepository;
  private RepositoriesMeta repositoriesMeta;

  public UserRepositoriesMeta() {
    repositoriesMeta = new RepositoriesMeta();
    try {
      repositoriesMeta.readData();
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }
  }

  public static UserRepositoriesMeta getInstance() {
    return SingletonUtil.getUniqueInstance( UserRepositoriesMeta.class, WebSpoonUtils.getUISession() );
  }

  public RepositoriesMeta getRepositoriesMeta() {
    return repositoriesMeta;
  }
  public void setRepositoriesMeta( RepositoriesMeta repositoriesMeta ) {
    this.repositoriesMeta = repositoriesMeta;
  }
  public RepositoryMeta getConnectedRepository() {
    return connectedRepository;
  }
  public void setConnectedRepository( RepositoryMeta connectedRepository ) {
    this.connectedRepository = connectedRepository;
  }
  public RepositoryMeta getCurrentRepository() {
    return currentRepository;
  }
  public void setCurrentRepository( RepositoryMeta currentRepository ) {
    this.currentRepository = currentRepository;
  }
}
