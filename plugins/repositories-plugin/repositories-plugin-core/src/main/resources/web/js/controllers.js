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
      'repositories/models',
      'repositories/services'
    ],

  function(angular) {

    var repoConnectionAppControllers = angular.module('repoConnectionAppControllers', []);

    repoConnectionAppControllers.controller("PentahoRepositoryController", function($scope, $translate, $location, $rootScope, $timeout, $filter, pentahoRepositoryModel, repositoryTypesModel, loadingAnimationModel, repositoriesService) {
      $scope.model = pentahoRepositoryModel.model;
      $rootScope.fromConnect = false;
      var connectedRepositoryName = getConnectedRepositoryName();
      $rootScope.connectNowVisible = !($rootScope.fromEdit && connectedRepositoryName === pentahoRepositoryModel.model.displayName);
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
            $translate('repositories.error.exists.label').then(function (translation) {
              $rootScope.triggerError(translation);
            });
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
        loadingAnimationModel.displayName = this.model.displayName;
        $timeout(function(){
          repositoriesService.add( $scope.model ).
          then(function(response) {
            $location.path("/pentaho-repository-creation-success");
            if($scope.model.isDefault) {
              setDefaultRepository($scope.model.displayName);
            }
            $rootScope.next();
          }, function (response) {
            $location.path("/pentaho-repository-creation-failure");
            $rootScope.next();
          });
        },1000);
        $location.path("/loading-animation");
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
      $scope.setDefaultConn = function(value) {
        this.model.isDefault = value;
      }
      $scope.back = function() {
        $rootScope.clearError();
        if ($rootScope.fromEdit) {
          $rootScope.fromEdit = false;
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
    });

    repoConnectionAppControllers.controller("KettleFileRepositoryController", function($scope, $translate, $rootScope, $location, $filter, kettleFileRepositoryModel, repositoriesService) {
      $scope.model = kettleFileRepositoryModel.model;
      $rootScope.fromConnect = false;
      var connectedRepositoryName = getConnectedRepositoryName();
      $rootScope.connectNowVisible = !($rootScope.fromEdit && connectedRepositoryName === kettleFileRepositoryModel.model.displayName);
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
            $translate('repositories.error.exists.label').then(function (translation) {
              $rootScope.triggerError(translation);
            });
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
        repositoriesService.add( $scope.model ).
        then(function(response) {
          $location.path("/kettle-file-repository-creation-success");
          if($scope.model.isDefault) {
            setDefaultRepository($scope.model.displayName);
          }
          $rootScope.next();
        }, function (response) {
          $location.path("/kettle-file-repository-creation-failure");
          $rootScope.next();
        });
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
      $scope.setDoNotModify = function(value) {
        this.model.doNotModify = value;
      }
      $scope.setShowHiddenFolders = function(value) {
        this.model.showHiddenFolders = value;
      }
      $scope.setDefaultConn = function(value) {
        this.model.isDefault = value;
      }
      $scope.back = function() {
        $rootScope.clearError();
        if ($rootScope.fromEdit) {
          $rootScope.fromEdit = false;
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
    });

    repoConnectionAppControllers.controller("KettleDatabaseRepositoryController", function($scope, $rootScope, $location, $timeout, $filter, kettleDatabaseRepositoryModel, loadingAnimationModel, repositoriesService) {
      $scope.model = kettleDatabaseRepositoryModel.model;
      $rootScope.fromConnect = false;
      var connectedRepositoryName = getConnectedRepositoryName();
      $rootScope.connectNowVisible = !($rootScope.fromEdit && connectedRepositoryName === kettleDatabaseRepositoryModel.model.displayName);
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
            $translate('repositories.error.exists.label').then(function (translation) {
              $rootScope.triggerError(translation);
            });
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
        loadingAnimationModel.displayName = this.model.displayName;
        $timeout(function(){
          repositoriesService.add( $scope.model ).
          then(function(response) {
            $location.path("/kettle-database-repository-creation-success");
            if($scope.model.isDefault) {
              setDefaultRepository($scope.model.displayName);
            }
            $rootScope.next();
          }, function (response) {
            $location.path("/kettle-database-repository-creation-failure");
            $rootScope.next();
          });
        },1000);
        $location.path("/loading-animation");
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
      $scope.setDefaultConn = function(value) {
        this.model.isDefault = value;
      }
      $scope.back = function() {
        $rootScope.clearError();
        if ($rootScope.fromEdit) {
          $rootScope.fromEdit = false;
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
      var clickedDefault = false;
      $scope.selectRepository = function(repository) {
        if(!clickedDefault) {
          repositoriesModel.selectedRepository = repositoriesModel.selectedRepository == repository ? null : repository;
        }
        clickedDefault = false;
      }
      $scope.setDefault = function(repository) {
        clickedDefault = true;
        var name = repository != null ? repository.displayName : null;
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
        repositoriesModel.selectedRepository = null;
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

    repoConnectionAppControllers.controller("RepositoryConnectController", function($scope, $rootScope, $location, $timeout, repositoryConnectModel, loadingAnimationModel, repositoriesService) {
      $scope.model = repositoryConnectModel;
      $scope.model.username = getCurrentUser();
      $scope.model.currentRepositoryName = getCurrentRepository();
      $rootScope.fromConnect = true;
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
        loadingAnimationModel.displayName = this.model.currentRepositoryName;
        $timeout(function(){
          repositoriesService.login( $scope.model.username, $scope.model.password ).
          then(function(response) {
            console.log(response);
            close();
          }, function (response) {
            console.log(response);
            $timeout(function(){
              $rootScope.triggerError(response.data.message);
            },600);
            $location.path("/repository-connect");
            $rootScope.backFade();
          });
        },1000);
        $location.path("/loading-animation");
        $rootScope.nextFade();
      }
      $scope.addToolTips = addToolTips;
      var errorMessage = getErrorMessage();
      if (errorMessage != "") {
    	  $rootScope.triggerError(errorMessage);
      }
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

    repoConnectionAppControllers.controller("LoadingAnimationController", function($scope, loadingAnimationModel) {
      $scope.model = loadingAnimationModel;
    });
  }
);
