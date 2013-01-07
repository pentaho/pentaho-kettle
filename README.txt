As of January 8, 2013 the Kettle project has changed:

  - Apache Ivy support has been added to resolve dependencies.  This has eliminated
    the need to commit JAR files into the version control system. It will also help with conflict management,
    to ensure all Kettle modules and plugins are using the same JARs.
    
  - The structure of the project has changed.  What were source folders are now 
    subprojects that can be built independently.  These sub projects contain 
    their own IVY files.
    
    For example, "src-ui" has become the "ui" project.  Inside the "ui" project is the src folder that was "src-ui".  It also 
    has a build.xml, build.properties file, etc.
    
    
    
What is that "assembly" folder?

     The assembly folder serves two purposes:
     
        1- It provides a staging area for building Kettle.
        2- It contains resources needed for a Kettle distribution. The resources are contained in the "package-res" folder.        
        

        
What is "package-res"?

     If you take a look in "package-res" you will see a folder structure that 
     once was under the root of the Kettle project.  These folders are packaged up 
     into the distributable product.
     
     Changes to shell scripts, launcher, images, and docs are made here.
     
     
 
Getting the source:
 
     The source for TRUNK development can be found in the same place as before:  
     
         svn://source.pentaho.org/svnkettleroot/Kettle/trunk

         
 
I got the source, so now what can I do?
 
     The default Ant target will build Spoon which can then be run from the projects "dist" folder.
     
     Linux example:
        
         /workspace/Kettle-trunk/ant 
         /workspace/Kettle-trunk/cd dist
         /workspace/Kettle-trunk/chmod 777 spoon.sh
         /workspace/Kettle-trunk/./spoon.sh     

         
     
What about that .classpath file that Eclipse needs?
 
     With the use of Ivy it is no longer necessary to edit the .classpath file and check it into version control.
     There is a .classpath file checked in which contains references to Kettle source code and output folder(s).  
     You can generate a "full" .classpath file (including Kettle's dependencies) with the "create-dot-classpath" Ant target.
     
     Linux example:
     
         /workspace/Kettle-trunk/ant create-dot-classpath
         
     The Ant target will resolve the dependencies and generate the .classpath file.
     After generating the .classpath file, refresh your Eclipse project and have Eclipse
     build the project.
     
     Please do not check in the .classpath back into version control.
     
     

How do I set up Run and Debug configurations in Eclipse?

     Running the "create-dot-classpath" Ant target will create a launch configuration (using the template provided by project.launch) 
     named using your project folder name, such as kettle-trunk.launch, and it will place the .launch file in the projects root folder.  
     Restarting Eclipse will make it available in the Run/Run Configurations... and Run/Debug configurations... drop-down menus. The launch
     configuration is available without restarting Eclipse by right-clicking on the .launch file and selecting "Run As..." then the name of 
     the project.



OK.  I just want to add a new property to a step using Eclipse as my IDE.  What do I have to do?

     - Check out the project and set it up as an Eclipse Java project.
     - Run the create-dot-classpath ant target
     - Refresh the Eclipse changes.
     - Make the appropriate code changes in the step meta and the step dialog.
     - Changes can be verified by running the <project>.launch file where <project>
       is the name of the Eclipse project.
     
     
     
Why does the build output appear to download JARs multiple times?

     Although it may _appear_ to be downloading JARs multiple times, Ivy will download the dependencies _once_ and cache them (in your home folder 
     under .ivy2/cache) for later use. When a dependency is being downloaded you will see multiple periods displayed (for example: ".........").
     While running the default Ant target (or the "create-dot-classpath" target), Ivy will resolve the dependencies for each Kettle module and core plugin.
     It does this by first checking the local repository, then your local cache, then other public repositories. Ivy will output a line for each resolved 
     dependency, but that does not mean the artifact is being downloaded. Rather, Ivy is checking to see if the artifact is already present locally and if so,
     will use it. Therefore, you may see lots of lines in the Ant output for Ivy resolve tasks, but if you don't see the periods, then the artifacts already
     exist locally and will not be downloaded again.
     

     
After checking out Kettle, why does the first build take so long?

     This is an effect of the use of Ivy for dependency management. Instead of the checkout itself taking a long time (as all JARs used to be checked into 
     version control), instead the initial checkout should be much faster but the first build will be much longer. This is due to Ivy downloading all the 
     dependencies to its local cache. You should see significant improvement in the time it takes to build every time after that.
     
     
     
I seem to be getting Ivy-related errors while running Ant targets. What should I do?

     It is possible that your Ivy cache has become corrupt. If you know which dependencies seem to be causing the issue, you can go to the Ivy cache (under your home
     folder at .ivy2/cache), find the folder containing the artifact(s), delete the folder, then re-run your Ant target. If this does not work, you can run the 
     "ivy-clean-cache" and "ivy-clean-local" Ant tasks to clean your entire Ivy cache and local repository, respectively.

     
     
I'm making a change to Kettle that requires a new (or newer version of a) third-party library or dependency. What do I do?

     No JAR files should be committed to the Kettle project. Instead, locate the ivy.xml file in the module or core plugin folder that contains your code changes, 
     and find the <dependency> tag that refers to the dependency you'd like to update. If the dependency exists, simply update the revision and run the 
     "resolve" Ant target. If the dependency tag for an existing JAR is not present in the ivy.xml file, it is likely being brought in "transitively" 
     by a dependency on some other Kettle or Pentaho module. In this case, for development you can add the Ivy dependency to the file manually and run the 
     "resolve" Ant target. However, rather than committing the change to ivy.xml, please write a Jira case asking for the update of the desired dependencies. 
     This will allow Pentaho to ensure that updating the dependencies won't interfere with other modules that use the same JARs. 
     
     If a new dependency is needed, simply add the dependency to the appropriate ivy.xml file and commit with descriptive comments.
     
     
     