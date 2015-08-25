Tim Kafalas 8/6/2014 rev 4/16/2015

The org.pentaho.di.services.PentahoDiPlugin class it generated completely from our wadl file and the wadl2java
ant task in the build-wadl2java.xml file.  Below are the steps to regenerating the class if changes are made to
the plugin's rest services.

1) Generate the new wadl file by issuing the following service request to a running pdi server with your rest
   service changes: 
   
   			http://localhost:9080/pentaho-di/plugin/pur-repository-plugin/api/application.wadl

2) Overwrite the wadl2java/wadl-resource/application.wadl.xml file with the results of step 1.

3) Generate the new wadl sub-resouce file by issuing the following service request to a
   running pdi server with your rest service changes: 
   
   			http://localhost:9080/pentaho-di/plugin/pur-repository-plugin/api/application.wadl/xsd0.xsd

4) Overwrite the wadl2java/wadl-resource/application.wadl/xsd0.xsd file with the results of step 3.

5) Run the "wadl2java" ant task.  This task depends on wadl2java-resolve to generate a populated
	 wadl2java/lib folder.  It then creates the org.pentaho.di.services.PentahoDiPlugin class from
	 the wadl file.