# Project Layout

* Apache Ivy support has been added to resolve dependencies.
  * This has eliminated the need to commit JAR files into the version control system. It will also help with conflict management, to ensure all Kettle modules and plugins are using the same JARs.
* The structure of the project has changed.  What were source folders are now subprojects that can be built independently.  These sub projects contain  their own IVY files.
  * For example, "src-ui" has become the "ui" module.  Inside the "ui" project is the src folder that was "src-ui". It also has files such as build.xml, build.properties, ivy.xml, etc.

# Compiling

1. Run `ant clean-all resolve create-dot-classpath`
  * These targets will resolve and retrieve the dependencies (third-party and Pentaho JARs, e.g.) and update your Eclipse classpath.  
2. Run `ant dist`
  * This will perform a build of the Kettle modules and core plugins, and generate a local distribution folder, `dist/`, which can be used to run the "Spoon" or other programs.

Notes:
 * Apache Ivy manages the creation of the .classpath file for the Eclipse project, and it is not needed, or recommended, to include this file into a pull request.
 * A copy of the `ant` binary is also available within most Eclipse installations and can be run through console by properly setting the path environment variable.
 * The build process requires also the Maven package (`sudo apt-get install maven2` on Ubuntu Linux).

# Contributing

1. Submit a pull request, referencing the relevant [Jira case](http://jira.pentaho.com/secure/Dashboard.jspa)
2. Attach a Git patch file to the relevant [Jira case](http://jira.pentaho.com/secure/Dashboard.jspa)

Use of the Pentaho checkstyle format (via `ant checkstyle` and reviewing the report) and developing working Unit Tests helps to ensure that pull requests for bugs and improvements are processed quickly.

# FAQ

## How do I set up Run and Debug configurations in Eclipse?

Running the "create-dot-classpath" Ant target will create a launch configuration (using the template provided by project.launch) named using your project folder name, such as kettle-trunk.launch, and it will place the .launch file in the projects root folder.

Restarting Eclipse will make it available in the Run/Run Configurations... and Run/Debug configurations... drop-down menus. The launch configuration is available without restarting Eclipse by right-clicking on the .launch file and selecting "Run As..." then the name of the project.

## Let's say I just want to add a new property to a step using Eclipse as my IDE.  What do I have to do?

1. Check out the project and set it up as an Eclipse Java project.
2. Run "ant clean-all resolve create-dot-classpath"
3. Refresh the Eclipse project to synch the workspace with the file system.
4. Make the appropriate code changes in the step meta and the step dialog.
5. Run the default Ant target
6. Changes can be verified by running the <project>.launch file where <project> is the name of the Eclipse project.

## If I want to build the project with Ant should I always use the default target?

To simply build/compile the code, use the default target. To get a full Kettle distribution, use the "dist" Ant target. To build the distribution (or any module or plugin) from a clean workspace,  run the following Ant targets from the root directory of the desired artifact:

    ant clean-all resolve dist

## My code changes were just in the engine module.  Can I run Ant from there?

You can use the build file located in the engine folder, e.g., 
     
    cd engine
    ant clean-all resolve dist

## I get compile errors!  Cannot find symbols and packages that don't exist!

When you did that default build from the projects root folder you resolved dependencies into its lib folder.  You need to resolve engine's dependencies and then compile:

    cd engine
    ant resolve compile

That seems redundant.

Yes but we are building modules now.  If your Ivy cache already contains the dependencies, the resolve should be fairly quick.

## I ran Spoon from the project's dist folder.  Why can't I see my changes I just compiled?

You need to do a an "ant dist" at the project level.
     
Here is an example of compiling engine source and then "disting" the project:
     
    cd engine
    ant compile

No compile errors!  
             
    cd ..
    ant dist

No errors!

    cd dist
    sh spoon.sh

Changes should be reflected in Spoon!
             

## If I needed to change something in DB, like the default port for PostgreSQL, do I need to check out all of Kettle and build it?

You will get a full working copy of Kettle when you checkout a branch from the Git project. However you do not need to build all of Kettle if your changes are isolated to a particular module or plugin. In this example you can go into the "core" folder and run the following Ant target set:

    clean-all resolve dist

A kettle-core JAR will be built and placed in the project's dist/ folder.

To test out your changes you can grab a Kettle build from CI: http://ci.pentaho.com/view/Data%20Integration/job/Kettle/

Replace the kettle-core jar in the CI build's lib/ folder and run Spoon.  Create a new DB connection with PostgreSQL as the connection type.  You should see your new default port number.

## Why does the build output appear to download JARs multiple times?

     Although it may _appear_ to be downloading JARs multiple times, Ivy will download the dependencies _once_ and cache them (in your home folder 
     under .ivy2/cache) for later use. When a dependency is being downloaded you will see multiple periods displayed (for example: ".........").
     While running the default Ant target (or the "create-dot-classpath" target), Ivy will resolve the dependencies for each Kettle module and core plugin.
     It does this by first checking the local repository, then your local cache, then other public repositories. Ivy will output a line for each resolved 
     dependency, but that does not mean the artifact is being downloaded. Rather, Ivy is checking to see if the artifact is already present locally and if so,
     will use it. Therefore, you may see lots of lines in the Ant output for Ivy resolve tasks, but if you don't see the periods, then the artifacts already
     exist locally and will not be downloaded again.

## After checking out Kettle for the first time, why does the first build take so long?

This is an effect of the use of Ivy for dependency management. Instead of the checkout itself taking a long time (as all JARs used to be checked into version control), instead the initial checkout should be much faster but the first build will be much longer. This is due to Ivy downloading all the dependencies to its local cache. You should see significant improvement in the time it takes to build every time after that.

## I seem to be getting Ivy-related errors while running Ant targets. What should I do?

It is possible that your Ivy cache has become corrupt. If you know which dependencies seem to be causing the issue, you can go to the Ivy cache (under your home folder at .ivy2/cache), find the folder containing the artifact(s), delete the folder, then re-run your Ant target. If this does not work, you can run the "ivy-clean-cache" and "ivy-clean-local" Ant tasks to clean your entire Ivy cache and local repository, respectively.

I removed the right directories from the ~/.ivy2/cache directory but I am still having ivy issues.

If you are seeing error messages like:

```    
    [ivy:resolve] :: problems summary ::
[ivy:resolve] :::: WARNINGS
[ivy:resolve]     module not found: pentaho-kettle#kettle-db;TRUNK-SNAPSHOT
[ivy:resolve]   ==== local: tried
[ivy:resolve]     /home/rbouman/.ivy2/local/pentaho-kettle/kettle-db/TRUNK-SNAPSHOT/ivys/ivy.xml
```
    
then your local ivy files (in .ivy2/local) is trying to pull in a jar that is no longer available (and probably, no longer needed). To remedy this, remove the entire .ivy2/local directory and retry.

## I'm making a change to Kettle that requires a new (or newer version of a) third-party library or dependency. What do I do?

No JAR files should be committed to the Kettle project. Instead, locate the ivy.xml file in the module or core plugin folder that contains your code changes, and find the <dependency> tag that refers to the dependency you'd like to update. If the dependency exists, simply update the revision and run the "resolve" Ant target. If the dependency tag for an existing JAR is not present in the ivy.xml file, it is likely being brought in "transitively"  by a dependency on some other Kettle or Pentaho module. In this case, for development you can add the Ivy dependency to the file manually and run the "resolve" Ant target. However, rather than committing the change to ivy.xml, please write a Jira case asking for the update of the desired dependencies. This will allow Pentaho to ensure that updating the dependencies won't interfere with other modules that use the same JARs. 
     
If a new dependency is needed, simply add the dependency to the appropriate ivy.xml file and commit with descriptive comments.
     
IMPORTANT: If a new dependency (JAR) is being introduced, make sure the license is _not_ GPL or AGPL. These licenses are not "Pentaho-friendly" and we cannot distribute these JARs without all Kettle source code becoming GPL.  LGPL licensing is ok for JARs but not for code. The most "Pentaho-friendly" licenses are permissive licenses such as Apache or MIT. If you have any questions about licensing, please contact Pentaho.

## What is that "assembly" folder?

The assembly folder serves two purposes:

1. It provides a staging area for building Kettle.
2. It contains resources needed for a Kettle distribution. The resources are contained in the "package-res" folder.        

## What is "package-res" in assembly?

If you take a look in "package-res" you will see a folder structure that once was under the root of the Kettle project.  These folders are packaged up into the distributable product.

Changes to shell scripts, launcher, images, and docs are made here.
