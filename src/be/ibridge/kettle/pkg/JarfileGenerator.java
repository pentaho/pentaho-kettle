package be.ibridge.kettle.pkg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.TransMeta;

public class JarfileGenerator
{
    public static final String TRANSFORMATION_FILENAME = "transformation.xml";
    
    public static final void generateJarFile(TransMeta transMeta)
    {
        LogWriter log = LogWriter.getInstance();
        int count;
        byte[] buffer = new byte[4096];

        KettleDependencies deps = new KettleDependencies(transMeta);
        
        File kar = new File("kar");
        if (kar.exists()) deleteDirectory(kar);
        kar.mkdir();
        File libDir = new File(kar.getPath()+Const.FILE_SEPARATOR+"lib");
        libDir.mkdir();
        File libextDir = new File(kar.getPath()+Const.FILE_SEPARATOR+"libext");
        libextDir.mkdir();
        
        Value karFilename = new Value("filename", "kettle.jar");
        if (!Const.isEmpty(transMeta.getFilename()))
        {
            karFilename.setValue(transMeta.getFilename());
            karFilename.replace(" ", "_").replace(".", "_").lower();
            karFilename.setValue(karFilename.getString()+".kar");
        }
        karFilename.setValue(kar.getPath()+Const.FILE_SEPARATOR+karFilename.getString()); // in the kar directory
        
        File karFile = new File(karFilename.getString());
        String classPath = "";
        
        try
        {
            for (int i = 0; i < deps.getLibraryFiles().length; i++)
            {
                String libFilename = deps.getLibraryFiles()[i];
                classPath+=" "+libFilename;
                File libFile = new File(libFilename);
                File target  = new File(kar.getPath()+Const.FILE_SEPARATOR+libFilename);
                FileOutputStream fileOutputStream = new FileOutputStream(target);

                // Now put the content of this file into this copy...
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(libFile));

                // Read the file the file and write it to the jar.
                while ((count = inputStream.read(buffer)) != -1) fileOutputStream.write(buffer, 0, count);

                inputStream.close();
                fileOutputStream.close();
            }
            
            // The manifest file
            String strManifest = "";
            strManifest += "Manifest-Version: 1.0"+Const.CR;
            strManifest += "Created-By: Kettle version "+Const.VERSION;
            strManifest += Attributes.Name.MAIN_CLASS.toString()+": " + (JarPan.class.getName()) + Const.CR;
            strManifest += Attributes.Name.CLASS_PATH.toString()+":" + classPath + Const.CR;

            // Create a new manifest file in the root.
            File manifestFile = new File("manifest.mf");
            FileOutputStream fos = new FileOutputStream(manifestFile);
            fos.write(strManifest.getBytes());
            fos.close();
            
            // The transformation, also in the kar directory...
            String strTrans = transMeta.getXML();
            File transFile = new File(kar.getPath()+Const.FILE_SEPARATOR+TRANSFORMATION_FILENAME);
            fos = new FileOutputStream(transFile);
            fos.write(strTrans.getBytes());
            fos.close();
        }
        catch (Exception e)
        {
            log.logError(JarfileGenerator.class.getName(), "Error zipping files into archive [" + karFile.getPath() + "] : " + e.toString());
            log.logError(JarfileGenerator.class.getName(), Const.getStackTracker(e));
        }
    }

    private static void deleteDirectory(File dir)
    {
        File[] files = dir.listFiles();
        for (int i=0;i<files.length;i++)
        {
            if (files[i].isDirectory()) deleteDirectory(files[i]);
            files[i].delete();
        }
        dir.delete();
    }
}
