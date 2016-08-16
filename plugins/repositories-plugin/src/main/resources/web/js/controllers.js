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

define(
    [
        'angular',
        'angular-sanitize',
        'repositories/models'
    ],

  function(angular) {

    var repoConnectionAppControllers = angular.module('repoConnectionAppControllers', []);

    repoConnectionAppControllers.controller("PentahoRepositoryController", function($scope, $location, $rootScope, $filter, pentahoRepositoryModel, repositoryTypesModel) {
      $scope.model = pentahoRepositoryModel.model;
      $scope.getStarted = function() {
        $rootScope.fromEdit = false;
        pentahoRepositoryModel.reset();
        $location.path("/pentaho-repository-connection-details");
        $rootScope.next();
      }
      $scope.otherRepositories = function() {
        repositoryTypesModel.selectedRepositoryType = null;
        $rootScope.nextFade();
      }
      $scope.canFinish = function() {
        if (this.model.displayName == "" || this.model.url == "") {
          return false;
        }
        return true;
      }
      function checkDuplicate() {
        repositories = JSON.parse(getRepositories());
        for(var i = 0; i < repositories.length; i++){
          if( $filter('lowercase')(repositories[i].displayName) == $filter('lowercase')($scope.model.displayName) ){
            $rootScope.triggerError("A repository connection with that name already exists. Please enter a different name.");
            return true;
          }
        }
        return false;
      }
      $scope.resetErrorMsg = function() {
        $rootScope.resetErrorMsg();
      }
      $scope.finish = function() {
        if( $rootScope.hasError ){
          $rootScope.refreshError();
          return;
        }
        if( this.model.displayName != getCurrentRepository() ) {
          if( checkDuplicate() ){
            return;
          }
        }
        if (createRepository("PentahoEnterpriseRepository", JSON.stringify(this.model))) {
          $location.path("/pentaho-repository-creation-success");
        } else {
          $location.path("/pentaho-repository-creation-failure");
        }
        if(this.model.isDefault) {
          setDefaultRepository(this.model.displayName);
        }
        $rootScope.next();
      };
      $scope.connect = function() {
       $location.path("/repository-connect");
       $rootScope.next();
      }
      $scope.createNewConnection = function() {
       $location.path("/repository-manager");
       $rootScope.next();
       reset();
      }
      $scope.changeSettings = function() {
        $location.path("/pentaho-repository-connection-details");
        $rootScope.back();
      }
      $scope.back = function() {
        $rootScope.clearError();
        if (this.model.id) {
          $location.path("/repository-manager");
        } else {
          $location.path("/pentaho-repository");
        }
        $rootScope.back();
      }
      $scope.close = function() {
        close();
      }
      $scope.help = function() {
        help();
      }
      $scope.successText = $rootScope.fromEdit ? "Your connection has been edited and is ready to use." : "Your connection was created and is ready to use.";
    });

    repoConnectionAppControllers.controller("KettleFileRepositoryController", function($scope, $rootScope, $location, $filter, kettleFileRepositoryModel) {
      $scope.model = kettleFileRepositoryModel.model;
      $scope.selectLocation = function() {
        this.model.location = selectLocation();
      }
      $scope.canFinish = function() {
        if (this.model.displayName == "" || this.model.location == "") {
          return false;
        }
        return true;
      }
      function checkDuplicate() {
        repositories = JSON.parse(getRepositories());
        for(var i = 0; i < repositories.length; i++){
          if( $filter('lowercase')(repositories[i].displayName) == $filter('lowercase')($scope.model.displayName) ){
            $rootScope.triggerError("A repository connection with that name already exists. Please enter a different name.");
            return true;
          }
        }
        return false;
      }
      $scope.resetErrorMsg = function() {
        $rootScope.resetErrorMsg();
      }
      $scope.finish = function() {
        if( $rootScope.hasError ){
          $rootScope.refreshError();
          return;
        }
        if( this.model.displayName != getCurrentRepository() ) {
          if( checkDuplicate() ) {
            return;
          }
        }
        if (createRepository("KettleFileRepository", JSON.stringify(this.model))) {
          $location.path("/kettle-file-repository-creation-success");
        } else {
          $location.path("/kettle-file-repository-creation-failure");
        }
        if(this.model.isDefault) {
          setDefaultRepository(this.model.displayName);
        }
        $rootScope.next();
      }
      $scope.createNewConnection = function() {
        $location.path("/repository-manager");
        $rootScope.next();
        reset();
      }
      $scope.connect = function() {
        connectToRepository();
      }
      $scope.changeSettings = function() {
        $location.path("/kettle-file-repository-details");
        $rootScope.back();
      }
      $scope.back = function() {
        $rootScope.clearError();
        if (this.model.id) {
          $location.path("/repository-manager");
        } else {
          $location.path("/create-new-connection");
        }
        $rootScope.back();
      }
      $scope.close = function() {
       close();
      }
      $scope.help = function() {
        help();
      }
      $scope.successText = $rootScope.fromEdit ? "Your connection has been edited and is ready to use." : "Your Kettle file repository was created and is ready to use.";
    });

    repoConnectionAppControllers.controller("KettleDatabaseRepositoryController", function($scope, $rootScope, $location, $filter, kettleDatabaseRepositoryModel) {
      $scope.model = kettleDatabaseRepositoryModel.model;
      $scope.selectDatabase = function() {
        $rootScope.clearError();
        $location.path("/kettle-database-repository-select");
        $rootScope.next();
      }
      $scope.canFinish = function() {
        if (this.model.displayName == "" || this.model.databaseConnection == "None") {
          return false;
        }
        return true;
      }
      function checkDuplicate() {
        repositories = JSON.parse(getRepositories());
        for(var i = 0; i < repositories.length; i++){
          if( $filter('lowercase')(repositories[i].displayName) == $filter('lowercase')($scope.model.displayName) ){
            $rootScope.triggerError("A repository connection with that name already exists. Please enter a different name.");
            return true;
          }
        }
        return false;
      }
      $scope.resetErrorMsg = function() {
        $rootScope.resetErrorMsg();
      }
      $scope.finish = function() {
        if( $rootScope.hasError ){
          $rootScope.refreshError();
          return;
        }
        if( this.model.displayName != getCurrentRepository() ) {
          if( checkDuplicate() ){
            return;
          }
        }
        if (createRepository("KettleDatabaseRepository", JSON.stringify(this.model))) {
          $location.path("/kettle-database-repository-creation-success");
        } else {
          $location.path("/kettle-database-repository-creation-failure");
        }
        if(this.model.isDefault) {
          setDefaultRepository(this.model.displayName);
        }
        $rootScope.next();
      }
      $scope.createNewConnection = function() {
        $location.path("/repository-manager"); 
        $rootScope.next();
        reset();
      }
      $scope.connect = function() {
        $location.path("/repository-connect");
        $rootScope.next();
      }
      $scope.changeSettings = function() {
        $location.path("/kettle-database-repository-details");
        $rootScope.back();
      }
      $scope.back = function() {
        $rootScope.clearError();
        if (this.model.id) {
          $location.path("/repository-manager");
        } else {
          $location.path("/create-new-connection");
        }
        $rootScope.back();
      }
      $scope.close = function() {
       close();
      }
      $scope.help = function() {
        help();
      }
      $scope.successText = $rootScope.fromEdit ? "Your connection has been edited and is ready to use." : "Your Kettle database repository was created and is ready to use.";
    });

    repoConnectionAppControllers.controller("CreateNewConnectionController", function($scope, $location, $rootScope, repositoryTypesModel, kettleDatabaseRepositoryModel, kettleFileRepositoryModel) {
      $scope.model = repositoryTypesModel;
      $scope.selectRepositoryType = function(repositoryType) {
        repositoryTypesModel.selectedRepositoryType = repositoryType;
      }
      $scope.close = function() {
        close();
      };
      $scope.getStarted = function(repositoryType) {
        $rootScope.fromEdit = false;
        if (repositoryType.id == "KettleFileRepository") {
          kettleFileRepositoryModel.reset();
          $location.path("/kettle-file-repository-details");
          $rootScope.next();
        }
        if (repositoryType.id == "KettleDatabaseRepository") {
          kettleDatabaseRepositoryModel.reset();
          $location.path("/kettle-database-repository-details");
          $rootScope.next();
        }
      }
      $scope.help = function() {
        help();
      }
    });

    repoConnectionAppControllers.controller("KettleDatabaseRepositorySelectController", function($scope, $rootScope, $location, kettleDatabaseRepositoryModel) {
      $scope.model = kettleDatabaseRepositoryModel.model;
      $scope.databases = JSON.parse(getDatabases());
      $scope.selectDatabase = function(database) {
        $scope.selectedDatabase = database;
        if (database == null) {
          $scope.model.databaseConnection = "None";
        } else {
          $scope.model.databaseConnection = database.name;
        }
      }
      if( $scope.databases.length == 1 ){
        $scope.selectDatabase( $scope.databases[0] );
      }
      function updateSelected(dbName) {
        for (var i = 0; i < $scope.databases.length; i++) {
          if ($scope.databases[i].name == dbName) {
            $scope.selectDatabase($scope.databases[i]);
          }
        }
      }
      function defaultSelect() {
        if ($scope.databases.length == 1) {
          $scope.selectDatabase($scope.databases[0]);
        } else {
          $scope.selectDatabase(null);
        }
      }
      if ($scope.model.databaseConnection) {
        updateSelected($scope.model.databaseConnection);
      } else {
        defaultSelect();
      }
      $scope.createNewConnection = function() {
        createNewConnection();
        $scope.databases = JSON.parse(getDatabases());
        updateSelected($scope.model.databaseConnection);
      }
      $scope.editConnection = function() {
        var dbName = editDatabaseConnection(this.selectedDatabase.name);
        $scope.databases = JSON.parse(getDatabases());
        updateSelected(dbName);
      }
      $scope.deleteConnection = function() {
        deleteDatabaseConnection(this.selectedDatabase.name);
        $scope.databases = JSON.parse(getDatabases());
        defaultSelect();
      }
      $scope.back = function() {
        $location.path("/kettle-database-repository-details");
        $rootScope.back();
      }
      $scope.close = function() {
        close();
      }
      $scope.help = function() {
        help();
      }
    });

    repoConnectionAppControllers.controller("RepositoryManagerController", function($scope, $rootScope, $location, repositoriesModel, pentahoRepositoryModel, kettleFileRepositoryModel, kettleDatabaseRepositoryModel) {
      repositoriesModel.repositories = JSON.parse(getRepositories());
      repositoriesModel.selectedRepository = null;
      $scope.model = repositoriesModel;
      $scope.selectRepository = function(repository) {
        repositoriesModel.selectedRepository = repository;
      }
      $scope.setDefault = function(name) {
        setDefaultRepository(name);
        for ( i = 0; i < repositoriesModel.repositories.length; i++) {
          if ( repositoriesModel.repositories[i].displayName == name) {
            repositoriesModel.repositories[i].isDefault = true;
          } else {
            repositoriesModel.repositories[i].isDefault = false;
          }
        }
      }
      $scope.edit = function(repository) {
        $rootScope.fromEdit = true;
        var repositoryString = loadRepository(repository.displayName);
        var repository = JSON.parse(repositoryString);
        if (repository.id == "KettleFileRepository") {
          kettleFileRepositoryModel.model = repository;
          $location.path("/kettle-file-repository-details");
        } else if (repository.id == "KettleDatabaseRepository") {
          kettleDatabaseRepositoryModel.model = repository;
          $location.path("/kettle-database-repository-details");
        } else {
          pentahoRepositoryModel.model = repository;
          $location.path("/pentaho-repository-connection-details");
        }
        $rootScope.next();
      }
      $scope.delete = function(repository) {
        deleteRepository(repository.displayName);
        for ( i = 0; i < this.model.repositories.length; i++) {
          if ( this.model.repositories[i].displayName == repository.displayName) {
            this.model.repositories.splice(i, 1);
          }
        }
      }
      $scope.add = function() {
        $location.path("/pentaho-repository");
        $rootScope.next();
      }
      $scope.close = function() {
        close();
      }
      $scope.help = function() {
        help();
      }
      $scope.addToolTips = addToolTips;
    });

    repoConnectionAppControllers.controller("RepositoryConnectController", function($scope, $rootScope, repositoryConnectModel) {
      $scope.model = repositoryConnectModel;
      $scope.model.username = getCurrentUser();
      $scope.model.currentRepositoryName = getCurrentRepository();
      $scope.canConnect = function() {
        if (this.model.username == "" || this.model.password == "") {
          return false;
        }
        return true;
      }
      $scope.resetErrorMsg = function() {
        $rootScope.resetErrorMsg();
      }
      $scope.connect = function() {
        if( $rootScope.hasError ){
          $rootScope.refreshError();
          return;
        }
        var response = JSON.parse(loginToRepository(this.model.username, this.model.password));
        if( response.success == false){
          $rootScope.triggerError(response.errorMessage);
        } else {
          close();
        }
      }
      $scope.addToolTips = addToolTips;
    });
    
    function addToolTips() {
      var eles = document.querySelectorAll('.mightOverflow');
      setTimeout(function() {
        for (var i = 0; i < eles.length; i++) {
          var ele = eles[i];
          if (ele.offsetWidth < ele.scrollWidth) {
            ele.title = ele.innerText;
          }
        }
      });
    }
  }
);
