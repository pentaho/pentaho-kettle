define(
    [
        'angular',
        'angular-sanitize',
        'repositories/models'
    ],

  function(angular) {

    var repoConnectionAppControllers = angular.module('repoConnectionAppControllers', []);

    repoConnectionAppControllers.controller("PentahoRepositoryController", function($scope, $location, $rootScope, pentahoRepositoryModel) {
      $scope.model = pentahoRepositoryModel;

      $scope.canFinish = function() {
        if (pentahoRepositoryModel.displayName == "" || pentahoRepositoryModel.url == "") {
          return false;
        }
        return true;
      }
      $scope.finish = function() {
        if (createRepository("PentahoEnterpriseRepository", JSON.stringify(pentahoRepositoryModel))) {
          $location.path("/pentaho-repository-creation-success")
        } else {
          $location.path("/pentaho-repository-creation-failure")
        }
        $rootScope.next();
      };
      $scope.close = function() {
        close();
      };
    });

    repoConnectionAppControllers.controller("PentahoRepositoryCreationSuccessController", function($scope, $location, $rootScope) {
       $scope.createNewConnection = function() {
         $location.path("/pentaho-repository");
         $rootScope.next();
       }
       $scope.connect = function() {
         $location.path("/pentaho-repository-connect");
         $rootScope.next();
       }
       $scope.close = function() {
         close();
       }
       $scope.successText = "Your connection was created and is ready to use.";
    });

    repoConnectionAppControllers.controller("PentahoRepositoryCreationFailureController", function($scope, $location, $rootScope) {
       $scope.createNewConnection = function() {
         $location.path("/pentaho-repository");
         $rootScope.next();
       }
       $scope.back = function() {
         $rootScope.back();
       }
       $scope.close = function() {
         close();
       }
    });

    repoConnectionAppControllers.controller("CreateNewConnectionController", function($scope, $location, $rootScope, repositoryTypesModel) {
      $scope.model = repositoryTypesModel;
      $scope.selectRepositoryType = function(repositoryType) {
        repositoryTypesModel.selectedRepositoryType = repositoryType;
      }
      $scope.close = function() {
        close();
      };
      $scope.getStarted = function(repositoryType) {
        if (repositoryType.id == "KettleFileRepository") {
          $location.path("/kettle-file-repository-details");
          $rootScope.next();
        }
      }
    })

    repoConnectionAppControllers.controller("KettleDatabaseRepositoryDetailsController", function() {

    });

    repoConnectionAppControllers.controller("KettleFileRepositoryDetailsController", function($scope, $rootScope, $location, kettleFileRepositoryModel) {
      $scope.model = kettleFileRepositoryModel;
      $scope.selectLocation = function() {
        kettleFileRepositoryModel.location = selectLocation();
      }
      $scope.back = function() {
        $rootScope.back();
      }
      $scope.canFinish = function() {
        if (kettleFileRepositoryModel.displayName == "" || kettleFileRepositoryModel.location == "") {
          return false;
        }
        return true;
      }
      $scope.finish = function() {
        if (createRepository("KettleFileRepository", JSON.stringify(kettleFileRepositoryModel))) {
          $location.path("/kettle-file-repository-creation-success")
        } else {
          $location.path("/kettle-file-repository-creation-failure")
        }
        $rootScope.next();
      }
    });

    repoConnectionAppControllers.controller("KettleFileRepositoryCreationSuccessController", function($scope, $rootScope, $location) {
      $scope.createNewConnection = function() {
        $location.path("/pentaho-repository");
        $rootScope.next();
      }
      $scope.connect = function() {
        connectToRepository();
      }
      $scope.close = function() {
        close();
      }
      $scope.successText = "Your Kettle file repository was created and is ready to use.";
    });

    repoConnectionAppControllers.controller("KettleFileRepositoryCreationFailureController", function($scope, $location, $rootScope) {
       $scope.createNewConnection = function() {
         $location.path("/pentaho-repository");
         $rootScope.next();
       }
       $scope.back = function() {
         $rootScope.back();
       }
       $scope.close = function() {
         close();
       }
    });

    repoConnectionAppControllers.controller("RepositoryManagerController", function($scope, $rootScope, $location, repositoriesModel) {
      $scope.model = repositoriesModel;
      $scope.selectRepository = function(repository) {
        repositoriesModel.selectedRepository = repository;
      }
      $scope.setDefault = function(repository) {
        setDefaultRepository(repository);
      }
      $scope.edit = function(repository) {
      }
      $scope.delete = function(repository) {
        deleteRepository(repository.name);
        for ( i = 0; i < repositoriesModel.repositories.length; i++) {
          if ( repositoriesModel.repositories[i].name == repository.name) {
            repositoriesModel.repositories.splice(i, 1);
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
    });

    repoConnectionAppControllers.controller("PentahoRepositoryConnectController", function($scope) {
      $scope.username = "";
      $scope.password = "";
      $scope.canConnect = function() {
        if (this.username == "" || this.password == "") {
          return false;
        }
        return true;
      }

      $scope.connect = function() {
        if (loginToRepository(this.username, this.password)) {
          close();
        }
      }
    });
  }
);
