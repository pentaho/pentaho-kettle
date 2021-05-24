Upgrading the rap-extensions module to a new RAP version.
--------------------

1. Completely replace the following two modules: 


    /rap-extensions/releng/org.eclipse.rap.build
    /rap-extensions/releng/org.eclipse.rap.clientbuilder
With the ones that correspond to the desired 
version available at:
    
    https://github.com/eclipse/rap/tree/master/releng

2. Update the corresponding files in the following modules:


    /rap-extensions/bundles/org.eclipse.rap.filedialog
    /rap-extensions/bundles/org.eclipse.rap.fileupload
    /rap-extensions/bundles/org.eclipse.rap.jface
    /rap-extensions/bundles/org.eclipse.rap.rwt
    /rap-extensions/test/org.eclipse.rap.rwt.testfixture
With the latest corresponding to the desired
version available at:

    https://github.com/eclipse/rap/tree/master/bundles
    https://github.com/eclipse/rap/tree/master/tests



3. Update the RAP version in the root pom.xml


    <rap.version>3.12.0</rap.version>

4. Execute


    mvn clean verify