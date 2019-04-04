Tim Kafalas 8/6/2014 rev 6/19/2017

The org.pentaho.di.services.PentahoDiPlugin class it generated completely from our wadl file and the wadl2java
ant task in the build-wadl2java.xml file.  Below are the steps to regenerating the class if changes are made to
the plugin's rest services.

1) Generate the new wadl file by issuing the following service request to a running platform server with your rest
   service changes: 
   
   			http://localhost:8080/pentaho/plugin/pur-repository-plugin/api/application.wadl

2) Overwrite the wadl2java/wadl-resource/application.wadl.xml file with the results of step 1.

3) Generate the new wadl sub-resouce file by issuing the following service request to a
   running platform server with your rest service changes:
   
   			http://localhost:8080/pentaho/plugin/pur-repository-plugin/api/application.wadl/xsd0.xsd

4) Overwrite the wadl2java/wadl-resource/application.wadl/xsd0.xsd file with the results of step 3.

5) Run the "wadl2java" maven target by issuing the command "mvn antrun:run@wadl2java". It then creates the
    org.pentaho.di.services.PentahoDiPlugin class from the wadl file.