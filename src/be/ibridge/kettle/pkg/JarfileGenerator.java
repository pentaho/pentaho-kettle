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

        File karFile = new File("kettle.kar");
        String classPath = "";
        
        JarOutputStream jarOutputStream = null;
        try
        {
            jarOutputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(karFile)));

            for (int i = 0; i < deps.getLibraryFiles().length; i++)
            {
                String libFilename = deps.getLibraryFiles()[i];
                classPath+=" "+libFilename;
                File libFile = new File(libFilename);

                JarEntry jarEntry = new JarEntry(libFile.getPath());
                jarOutputStream.putNextEntry(jarEntry);

                // Now put the content of this file into this archive...
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(libFile));

                // Read the file the file and write it to the jar.
                while ((count = inputStream.read(buffer)) != -1) jarOutputStream.write(buffer, 0, count);

                inputStream.close();
                jarOutputStream.closeEntry();
            }
            
            // The manifest file
            String strManifest = Attributes.Name.MAIN_CLASS.toString()+": " + (JarPan.class.getName()) + Const.CR;
            strManifest+= Attributes.Name.CLASS_PATH.toString()+":" + classPath + Const.CR;
            File manifestFile = new File("Manifest.mf");
            FileOutputStream fos = new FileOutputStream(manifestFile);
            fos.write(strManifest.getBytes());
            fos.close();
            
            JarEntry jarEntry = new JarEntry("meta-inf/Manifest.mf");
            jarOutputStream.putNextEntry(jarEntry);
            FileInputStream inputStream = new FileInputStream(manifestFile);
            while ((count = inputStream.read(buffer)) != -1) jarOutputStream.write(buffer, 0, count);
            inputStream.close();
            jarOutputStream.closeEntry();
            
            // The transformation...
            String strTrans = transMeta.getXML();
            File transFile = new File(TRANSFORMATION_FILENAME);
            fos = new FileOutputStream(transFile);
            fos.write(strTrans.getBytes());
            fos.close();
            jarEntry = new JarEntry(TRANSFORMATION_FILENAME);
            jarOutputStream.putNextEntry(jarEntry);
            inputStream = new FileInputStream(transFile);
            while ((count = inputStream.read(buffer)) != -1) jarOutputStream.write(buffer, 0, count);
            inputStream.close();
            jarOutputStream.closeEntry();
        }
        catch (Exception e)
        {
            log.logError(JarfileGenerator.class.getName(), "Error zipping files into archive [" + karFile.getPath() + "] : " + e.toString());
            log.logError(JarfileGenerator.class.getName(), Const.getStackTracker(e));
        }
        finally
        {
            if (jarOutputStream != null)
            {
                try
                {
                    jarOutputStream.flush();
                    jarOutputStream.finish();
                    jarOutputStream.close();
                }
                catch (IOException e)
                {
                    log.logError(JarfileGenerator.class.getName(), "Unable to close kar archive zip file archive : " + e.toString());
                    log.logError(JarfileGenerator.class.getName(), Const.getStackTracker(e));
                }
            }
        }

    }
}
