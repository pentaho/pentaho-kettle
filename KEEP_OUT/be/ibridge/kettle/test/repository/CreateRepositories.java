package be.ibridge.kettle.test.repository;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.TransMeta;

public class CreateRepositories
{
    private static LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_DETAILED);

    private static final String testTransformationFile = "testfiles/testTransformation.xml"; 
    private static final String testXML1 = "testfiles/testXML1.xml";
    private static final String testXML2 = "testfiles/testXML2.xml";
    
    private static final String directoryName = "directory";
    
    private static final String xml[] = 
    {
        /*
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<connection>" +
            "<name>MS Access repository test</name>" +
            "<type>MSACCESS</type>" +
            "<access>ODBC</access>" +
            "<database>REPTEST</database>" +
          "</connection>",
        */
        
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<connection>" +
            "<name>Postgres repository test</name>" +
            "<server>localhost</server>" +
            "<type>POSTGRESQL</type>" +
            "<access>Native</access>" +
            "<database>reptest</database>" +
            "<port>5432</port>" +
            "<username>matt</username>" +
            "<password>abcd</password>" +
          "</connection>",
          
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
              "<name>SQLServer repository test</name>" +
              "<server>localhost</server>" +
              "<type>MSSQL</type>" +
              "<access>Native</access>" +
              "<database>reptest</database>" +
              "<port>1433</port>" +
              "<username>matt</username>" +
              "<password>abcd</password>" +
            "</connection>",
            
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<connection>" +
                "<name>MySQL repository test</name>" +
                "<server>localhost</server>" +
                "<type>MYSQL</type>" +
                "<access>Native</access>" +
                "<database>reptest</database>" +
                "<port>3306</port>" +
                "<username>matt</username>" +
                "<password>abcd</password>" +
              "</connection>"

    };

    /**
     * Test the creation, working and destruction of a repository.
     * @param xml XML describing the database connection
     * @throws Exception in case something goes wrong (unacceptable ;-))
     */
    public static final void testRepository(String xml) throws Exception
    {
       DatabaseMeta databaseMeta = new DatabaseMeta(xml);
       RepositoryMeta repositoryMeta = new RepositoryMeta(databaseMeta.getName(), databaseMeta.getName(), databaseMeta);
       Repository repository = new Repository(log, repositoryMeta, null);
       repository.connect(true, false, databaseMeta.getName());
       
       // drop it all in case something was left over...
       repository.dropRepositorySchema();                                                      

       // Create the repository schema
       repository.createRepositorySchema(null, false);
       
       // Create a new directory in the root directory
       RepositoryDirectory root = repository.getDirectoryTree();
       log.logBasic(databaseMeta.getName(), "Create directory "+root+directoryName);
       RepositoryDirectory newDirectory = new RepositoryDirectory(repository.getDirectoryTree(), "directory");
       
       // Add the new directory to the repository
       log.logBasic(databaseMeta.getName(), "Add directory "+newDirectory+" to repository");
       newDirectory.addToRep(repository);  // Create a directory in it...
       repository.
       
       // Load a test transformation from an XML file... 
       log.logBasic(databaseMeta.getName(), "Load test transformation from file "+testTransformationFile);
       TransMeta transMeta1 = new TransMeta(testTransformationFile); 
       transMeta1.setDirectory(newDirectory);
       
       // Save the transformation to the repository...
       log.logBasic(databaseMeta.getName(), "Save transformation ["+transMeta1.getName()+"] to directory ["+transMeta1.getDirectory()+"] in the repository");
       transMeta1.saveRep(repository); 
       
       // Load the transformation from the repository
       log.logBasic(databaseMeta.getName(), "Load transformation ["+transMeta1.getName()+"] from directory ["+transMeta1.getDirectory()+"] in the repository");
       TransMeta transMeta2 = new TransMeta(repository, transMeta1.getName(), newDirectory);
       
       // Compare the XML between the 2
       String xml1 = transMeta1.getXML();
       String xml2 = transMeta2.getXML();
       
       if (xml1.equals(xml2))
       {
           log.logBasic(databaseMeta.getName(), "The saved transformation is equal to the re-loaded transformation (the XML form)");
       }
       else
       {
           log.logError(databaseMeta.getName(), "The saved transformation is NOT equal to the re-loaded transformation (the XML form)");

           // Save first transformation to xml
           DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(testXML1)));
           dos.write(xml1.getBytes(Const.XML_ENCODING));
           dos.close();

           // Save second transformation to xml
           DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(new File(testXML2)));
           dos2.write(xml2.getBytes(Const.XML_ENCODING));
           dos2.close();
           
           throw new KettleException("XML Read-back is not the same as the one we saved!  Please run a diff "+testXML1+" "+testXML2);
       }
       
       // Drop the complete repository
       log.logBasic(databaseMeta.getName(), "Save transformation ["+transMeta1.getName()+"] to directory ["+transMeta1.getDirectory()+"] in the repository");
       repository.dropRepositorySchema();
       
       repository.disconnect();
    }

    /**
    * @param args
    */
    public static void main(String[] args) throws Exception
    {
       StepLoader.getInstance().read();
       
       for (int i=0;i<xml.length;i++)
       {
           testRepository(xml[i]);
       }
    }

}
