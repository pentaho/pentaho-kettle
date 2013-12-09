As of January 14, 2013 the Kettle project has changed:

  - Apache Ivy support has been added to resolve dependencies.  This has eliminated
    the need to commit JAR files into the version control system. It will also help with conflict management,
    to ensure all Kettle modules and plugins are using the same JARs.
    
  - The structure of the project has changed.  What were source folders are now 
    subprojects that can be built independently.  These sub projects contain 
    their own IVY files.
    
    For example, "src-ui" has become the "ui" module.  Inside the "ui" project is the src folder that was "src-ui".  
    It also has files such as build.xml, build.properties, ivy.xml, etc.
    
        
        
Getting the source:
 
     The source for TRUNK development can be found in the same place as before:  
     
         svn://source.pentaho.org/svnkettleroot/Kettle/trunk
         
         
         
What if I already have that checked out?

     If you checked out trunk before January 14, 2013, your project has the old structure.  
     
     _DO NOT_ do an SVN update or commit from a project with the old structure. An SVN update 
     will bring in the new structure but then also create tree conflicts on the files that 
     have been changed in your project.  The tree conflicts will prevent successful commits.
     
     If you were to do a commit of files that have been moved without doing an 
     update then the commit will fail with a message stating that the repository 
     path does not exist.
     
     
     
I have code changes against the old structure. What is the best way to get my code into the new structure?

     Checkout the latest trunk as a separate project.  Merge the uncommitted code you have into 
     it and commit (after testing of course). You can then remove your old project.
     
          
     
What is the best way to merge?

     Whichever way you are comfortable.

     If you are using SVN patches then they will have to be done on a per-file basis.
     Eclipse compare works but would be on a per-file basis as well.
     
     If you have changes to multiple files in the same folder then a tool like Meld or WinMerge
     would allow for multiple file differences.
     
     
    
OK, I have the new project structure checked out. So now what can I do?

    Right after checking out, you should run the following:
    
    ant clean-all resolve create-dot-classpath
    
    These targets will resolve and retrieve the dependencies (third-party and Pentaho JARs, e.g.) and
    update your Eclipse classpath.
    
    The default Ant target will build and locally publish the Kettle modules and core plugins. If you are
    using Ant to build a local distribution, run the "dist" target. This will perform the build, then 
    it will create a local distribution of Kettle, and Spoon can then be run from the project's dist/ folder.
     
     Linux example:
        
         ant dist
         cd dist
         sh spoon.sh     
         
     If you are using Eclipse, you may notice upon initial checkout that there is no .classpath file 
     in the repository. With the use of Ivy, it is no longer necessary to edit the .classpath file 
     and check it into version control. There is a file called classpath.template in the root 
     folder in which contains references to Kettle source code and output folders. You can 
     generate a full .classpath file (including Kettle's dependencies) with the 
     "resolve create-dot-classpath" Ant targets. 
     
     Linux example:
     
         /workspace/Kettle-trunk/ant resolve create-dot-classpath
         
     The Ant target will will copy classpath.template to .classpath, resolve the dependencies 
     into the lib/ folder, and generate the .classpath file.
     After generating the .classpath file, refresh your Eclipse project and have Eclipse build the project.
     
     Please do not commit the .classpath into the version control system.
     
     

How do I set up Run and Debug configurations in Eclipse?

     Running the "create-dot-classpath" Ant target will create a launch configuration (using the template provided by project.launch) 
     named using your project folder name, such as kettle-trunk.launch, and it will place the .launch file in the projects root folder.  
     Restarting Eclipse will make it available in the Run/Run Configurations... and Run/Debug configurations... drop-down menus. The launch
     configuration is available without restarting Eclipse by right-clicking on the .launch file and selecting "Run As..." then the name of 
     the project.



Let's say I just want to add a new property to a step using Eclipse as my IDE.  What do I have to do?

     - Check out the project and set it up as an Eclipse Java project.
     - Run "ant clean-all resolve create-dot-classpath"
     - Refresh the Eclipse project to synch the workspace with the file system.
     - Make the appropriate code changes in the step meta and the step dialog.
     - Run the default Ant target
     - Changes can be verified by running the <project>.launch file where <project>
       is the name of the Eclipse project.
     
     
     
If I want to build the project with Ant should I always use the default target?

     To simply build/compile the code, use the default target. To get a full Kettle distribution, use 
     the "dist" Ant target. To build the distribution (or any module or plugin) from a clean workspace, 
     run the following Ant targets from the root directory of the desired artifact:
     
     ant clean-all resolve dist
     


My code changes were just in the engine module.  Can I run Ant from there?

     You can use the build file located in the engine folder, e.g., 
     
         Kettle>cd engine
         Kettle>ant clean-all resolve dist
     

I get compile errors!  Cannot find symbols and packages that don't exist!

     When you did that default build from the projects root folder you resolved dependencies into its 
     lib folder.  You need to resolve engine's dependencies and then compile:
     
         Kettle>cd engine
         Kettle>ant resolve compile


That seems redundant.

     Yes but we are building modules now.  If your Ivy cache already contains the dependencies,
     the resolve should be fairly quick.



I ran Spoon from the project's dist folder.  Why can't I see my changes I just compiled?

     You need to do a an "ant dist" at the project level.
     
         Here is an example of compiling engine source and then "disting" the project:
     
         Kettle> cd engine
         Kettle/engine>ant compile
         
             No compile errors!  
             
         Kettle/engine>cd ..
         Kettle>ant dist
         
             No errors!
             
         Kettle>cd dist
         Kettle>sh spoon.sh
         
             Changes should be reflected in Spoon!
             

If I needed to change something in DB, like the default port for PostgreSQL, do I need to check out all
of Kettle and build it?

     You will get a full working copy of Kettle when you checkout a branch from the Git project. However you
     do not need to build all of Kettle if your changes are isolated to a particular module or plugin. In this 
     example you can go into the "core" folder and run the following Ant target set:
     
     clean-all resolve dist
     
     A kettle-core JAR will be built and placed in the project's dist/ folder.
     
     To test out your changes you can grab a Kettle build from CI:
        http://ci.pentaho.com/view/Data Integration/job/Kettle/
        
     Replace the kettle-core jar in the CI build's lib/ folder and run Spoon.  Create
     a new DB connection with PostgreSQL as the connection type.  You should see
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

I removed the right directories from the ~/.ivy2/cache directory but I am still having ivy issues.

    If you are seeing error messages like:
    
    [ivy:resolve] :: problems summary ::
[ivy:resolve] :::: WARNINGS
[ivy:resolve]     module not found: pentaho-kettle#kettle-db;TRUNK-SNAPSHOT
[ivy:resolve]   ==== local: tried
[ivy:resolve]     /home/rbouman/.ivy2/local/pentaho-kettle/kettle-db/TRUNK-SNAPSHOT/ivys/ivy.xml
    
    then your local ivy files (in .ivy2/local) is trying to pull in a jar that is no longer available (and probably, no longer needed).
    To remedy this, remove the entire .ivy2/local directory and retry.
     
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
     

What is that "assembly" folder?

     The assembly folder serves two purposes:
     
        1- It provides a staging area for building Kettle.
        2- It contains resources needed for a Kettle distribution. The resources are contained in the "package-res" folder.        
        
        
What is "package-res" in assembly?

     If you take a look in "package-res" you will see a folder structure that once was under the root of the Kettle 
     project.  These folders are packaged up into the distributable product.
     
     Changes to shell scripts, launcher, images, and docs are made here.     
