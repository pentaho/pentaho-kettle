/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

define( [
  'repositories'
  ],
  function ( repoConnectionApp ) {

    repoConnectionApp.service("repositoryTypesModel", function() {
      this.repositoryTypes = JSON.parse(getRepositoryTypes());
      this.selectedRepository = null;
    });

    repoConnectionApp.service("repositoriesModel", function() {
      this.repositories = JSON.parse(getRepositories());
      this.selectedRepository = null;
    });

    repoConnectionApp.service("pentahoRepositoryModel",function() {
      this.displayName = "";
      this.url = getDefaultUrl();
      this.description = "Pentaho repository | " + getDefaultUrl();
      this.isDefault = false;
    });

    repoConnectionApp.service("kettleFileRepositoryModel", function() {
      this.displayName = "";
      this.location = "";
      this.doNotModify = false;
      this.showHiddenFolders = false;
      this.description = "Kettle File Repository";
      this.isDefault = false;
    });

    repoConnectionApp.service("kettleDatabaseRepositoryModel", function() {
      this.databases = JSON.parse(getDatabases());
      this.displayName = "";
      this.databaseConnection = "None";
      this.description = "Kettle Database Repository";
      this.isDefault = false;
      this.selectedDatabase = null;
    });

    repoConnectionApp.service("repositoryConnectModel", function() {
      this.username = "";
      this.password = "";
    });

});
