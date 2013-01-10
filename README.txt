As of January TBD, 2013 the Kettle project has changed:

  - Apache Ivy support has been added to resolve dependencies.  This has eliminated
    the need to commit JAR files into the version control system. It will also help with conflict management,
    to ensure all Kettle modules and plugins are using the same JARs.
    
  - The structure of the project has changed.  What were source folders are now 
    subprojects that can be built independently.  These sub projects contain 
    their own IVY files.
    
    For example, "src-ui" has become the "ui" project.  Inside the "ui" project is the src folder that was "src-ui".  
    It also has a build.xml, build.properties file, etc.
    
        
        
Getting the source:
 
     The source for TRUNK development can be found in the same place as before:  
     
         svn://source.pentaho.org/svnkettleroot/Kettle/trunk
         
         
         
I already have that checked out.

     That would be the old structure.  DO NOT DO an svn update or commit from this.
     An svn update will bring in the new structure but then also create tree conflicts
     on the files that have been changed in your project.  The tree conflicts will prevent
     successful commits.
     
     If you were to do a commit of files that have been moved without doing an 
     update then the commit will fail with a message stating that the repository 
     path does not exist.
     
     
     
What is the best way to get my code into the new structure?

     Check this project out:  svn://source.pentaho.org/svnkettleroot/Kettle/branches/trunk-restruct.
     Merge the uncommitted code you have into it and stop committing into trunk.  
     This branch will become trunk when we cutover.
     
     Changes committed into trunk before the cutover will be merged if those
     changes were not committed into the trunk-restruct branch.
     
          
          
What if I have outstanding commits that can't be committed into trunk before the cutover?

     You are going to have to merge them into the new structure after the cutover.
     
     
     
What is the best way to merge?

     Whichever way you are comfortable.

     If you are using svn patches then they will have to be done on a per file basis.
     Eclipse compare works but would be on a per file basis as well.
     
     If you have changes to multiple files in the same folder then a tool like Meld
     would allow for multiple file differences.
     
     
       
I got the source.  What is that "assembly" folder?

     The assembly folder serves two purposes:
     
        1- It provides a staging area for building Kettle.
        2- It contains resources needed for a Kettle distribution. The resources are contained in the "package-res" folder.        
        

        
What is "package-res" in assembly?

     If you take a look in "package-res" you will see a folder structure that 
     once was under the root of the Kettle project.  These folders are packaged up 
     into the distributable product.
     
     Changes to shell scripts, launcher, images, and docs are made here.
     


So now what can I do?
 
     The default Ant target will build Spoon which can then be run from the projects dist folder.
     
     Linux example:
        
         Ant 
         cd dist
         sh spoon.sh     
         
     
     
What about that .classpath file that Eclipse needs?
 
     With the use of Ivy it is no longer necessary to edit the .classpath file and check it into version control.
     There is a file called classpath.template in the root folder in which contains references to Kettle source code and output folders.  
     You can generate a full .classpath file (including Kettle's dependencies) with the "create-dot-classpath" Ant target. 
     
     Linux example:
     
         /workspace/Kettle-trunk/ant create-dot-classpath
         
     The Ant target will will copy classpath.template to .classpath, resolve the dependencies and generate the .classpath file.
     After generating the .classpath file, refresh your Eclipse project and have Eclipse build the project.
     
     Please do not commit the .classpath into the version control system.
     
     

How do I set up Run and Debug configurations in Eclipse?

     Running the "create-dot-classpath" Ant target will create a launch configuration (using the template provided by project.launch) 
     named using your project folder name, such as kettle-trunk.launch, and it will place the .launch file in the projects root folder.  
     Restarting Eclipse will make it available in the Run/Run Configurations... and Run/Debug configurations... drop-down menus. The launch
     configuration is available without restarting Eclipse by right-clicking on the .launch file and selecting "Run As..." then the name of 
     the project.



OK.  I just want to add a new property to a step using Eclipse as my IDE.  What do I have to do?

     - Check out the project and set it up as an Eclipse Java project.
     - Run the create-dot-classpath Ant target
     - Refresh the Eclipse project to synch the workspace with the file system.
     - Make the appropriate code changes in the step meta and the step dialog.
     - Changes can be verified by running the <project>.launch file where <project>
       is the name of the Eclipse project.
     
     
     
If I want to build the project with Ant should I always use the default target?

     I would right after checking out the project but would avoid the target from that
     point on. The default target does quite a bit with cleaning and resolving.
     
     Consider the previous scenario with adding a new property to a step.  You checked out the project 
     and ran the default Ant target.  You change only the steps meta, dialog and execution java source.  Do you 
     want to clean the project and resolve the dependencies again?  Probably not.  
     
     Running the Ant "compile" target in the project's root folder will compile changed source
     code for all the modules.
     


My code changes were just in the engine module.  Can I run Ant from there?

     You can use the build file located in the engine folder, e.g., 
     
         Kettle>cd engine
         Kettle>ant compile
     

I get compile errors!  Cannot find symbols and packages that don't exist!

     When you did that default build from the projects root folder you resolved dependencies into it's 
     lib folder.  You need to resolve engine's dependencies and then compile:
     
         Kettle>cd engine
         Kettle>ant resolve compile


That seems redundant.

     Yes but we are building modules now.  Since your ivy cache should already have 
     the dependencies the resolve should be quick.



I ran Spoon from the project's dist folder.  Why can't I see my changes I just compiled?

     You need to do a an "ant dist" at the project level.  In the scenario of adding a new
     property to a step we do not need to resolve dependencies so the dist_nodeps target is 
     even quicker:
     
         Here is an example of compiling engine source and then "disting" the project:
     
         Kettle> cd engine
         Kettle/engine>ant compile
         
             No compile errors!  
             
         Kettle/engine>cd ..
         Kettle>ant dist-nodeps
         
             No errors!
             
         Kettle>cd dist
         Kettle>sh spoon.sh
         
             Changes should be reflected in Spoon!
             

If I needed to change something in DB, like the default port for PostgreSQL, do I need to check out all
of Kettle and build it?

     No you don't.  You can check out the db module.  Run it's default ant target.  If
     you are using Eclipse run the create-dot-classpath and refresh the project in Eclipse.
     Make your code change in PostgreSQLDatabaseMeta and run the dist ant target.  
     A kettle-db jar will be built and placed in the project's dist folder.
     
     To test out your changes you can grab a Kettle build from CI:
        http://ci.pentaho.com/view/Data Integration/job/Kettle/
        
     Replace the kettle-db jar in the CI build's lib folder and run Spoon.  Create
     a new DB connection with PostgreSQL as the connection yype.  You should see
     your new default port number.
     
    
     
Why does the build output appear to download JARs multiple times?

     Although it may _appear_ to be downloading JARs multiple times, Ivy will download the dependencies _once_ and cache them (in your home folder 
     under .ivy2/cache) for later use. When a dependency is being downloaded you will see multiple periods displayed (for example: ".........").
     While running the default Ant target (or the "create-dot-classpath" target), Ivy will resolve the dependencies for each Kettle module and core plugin.
     It does this by first checking the local repository, then your local cache, then other public repositories. Ivy will output a line for each resolved 
     dependency, but that does not mean the artifact is being downloaded. Rather, Ivy is checking to see if the artifact is already present locally and if so,
     will use it. Therefore, you may see lots of lines in the Ant output for Ivy resolve tasks, but if you don't see the periods, then the artifacts already
     exist locally and will not be downloaded again.
     

     
After checking out Kettle for the first time, why does the first build take so long?

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
     
     IMPORTANT: If a new dependency (JAR) is being introduced, make sure the license is _not_ GPL or AGPL. These licenses are not "Pentaho-friendly" and we cannot
     distribute these JARs without all Kettle source code becoming GPL.  LGPL licensing is ok for JARs but not for code. The most "Pentaho-friendly" licenses are
     permissive licenses such as Apache or MIT. If you have any questions about licensing, please contact Pentaho.