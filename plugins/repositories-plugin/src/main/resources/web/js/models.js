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
      this.url = "http://localhost:9080/pentaho-di";
      this.description = "";
      this.isDefaultOnStartup = false;
    });

    repoConnectionApp.service("kettleFileRepositoryModel", function() {
      this.displayName = "";
      this.location = "";
      this.doNotModify = false;
      this.showHiddenFolders = false;
      this.description = "";
      this.isDefaultOnStartup = false;

    });

});
