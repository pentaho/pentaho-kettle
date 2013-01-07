As of January 4 the Kettle project has changed:

  - IVY support has been added to resolve dependencies.  This has eliminated
    the need to commit jar files into the version control system.
    
  - The structure of the project has changed.  What were source folders are now 
    subprojects that can be built independently.  These sub projects contain 
    there own IVY files.
    
    For example, "src-ui" has become the "ui" project.  Inside the "ui" project is the src folder that was "src-ui".  It also 
    has a build.xml, build.properties file.
    

What is that "assembly" folder?

     It is basically two things.
     
        1- A staging area for building Spoon.
        2- It owns "package-res"
        
        

What is "package-res"?

     If you take a look in "package-res" you will see a folder structure that 
     once was under the root of the Ketle project.  These folders are packaged up 
     into the distributable product.
     
     Changes to shell script, launcher and docs are made here.
     
     
 
 Getting the source:
 
     The source for TRUNK development can be found in the same place as before:  
     
         svn://source.pentaho.org/svnkettleroot/Kettle/trunk
         
 
 I got the source, so now what can I do?
 
     The default ant target will build Spoon which can then be run from the projects "dist" folder.
     
     Linux example:
        
         /workspace/Kettle-trunk/ant 
         /workspace/Kettle-trunk/cd dist
         /workspace/Kettle-trunk/chmod 777 spoon.sh
         /workspace/Kettle-trunk/./spoon.sh
     
     
     
What about that .classpath file that Eclipse needs?
 
     With the use of IVY it is no longer necessary to edit the .classpath file and check it into version control.
     There is a .classpath file checked in which contains .  You can generate one with the 
     create-dot-classpath ant target.
     
     Linux example:
     
         /workspace/Kettle-trunk/ant create-dot-classpath
         
     The ant target will resolve the dependencies and generate the .classpath file.
     After generating the .classpath file refresh your Eclipse project and have eclipse
     build the project.
     
     Please do not check in the .classpath back into Subversion.
     
     

Run and debug configurations in Eclipse?

     Running the create-dot-classpath ant target will create a launch configuration, such as
     kettle-trunk.launch, and place it in the projects root folder.  Restarting Eclipse will make it 
     available in the Run/Run Configurations... and Run/Debug configurations...
      
