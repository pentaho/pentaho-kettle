
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;



/**
 * Class created to demonstrate the creation of transformations on-the-fly.
 * 
 * @author Matt
 * 
 */
public class TransBuilder
{
    public static final String[] databasesXML = {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<connection>" +
            "<name>target</name>" +
            "<server>localhost</server>" +
            "<type>MSSQL</type>" +
            "<access>Native</access>" +
            "<database>test</database>" +
            "<port>1433</port>" +
            "<username>matt</username>" +
            "<password>abcd</password>" +
          "</connection>",
          
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
              "<name>source</name>" +
              "<server>localhost</server>" +
              "<type>MYSQL</type>" +
              "<access>Native</access>" +
              "<database>test</database>" +
              "<port>3306</port>" +
              "<username>matt</username>" +
              "<password>abcd</password>" +
            "</connection>"  
    };

    /**
     * Creates a new Transformation using input parameters such as the tablename to read from.
     * @param transformationName The name of the transformation
     * @param sourceDatabaseName The name of the database to read from
     * @param sourceTableName The name of the table to read from
     * @param sourceFields The field names we want to read from the source table
     * @param targetDatabaseName The name of the target database
     * @param targetTableName The name of the target table we want to write to
     * @param targetFields The names of the fields in the target table (same number of fields as sourceFields)
     * @return A new transformation
     * @throws KettleException In the rare case something goes wrong
     */
    public static final TransMeta buildCopyTable(String transformationName, String sourceDatabaseName, String sourceTableName, String[] sourceFields, String targetDatabaseName, String targetTableName, String[] targetFields) throws KettleException
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
          TransMeta transMeta = new TransMeta();
          transMeta.setName(transformationName);
            
            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }
            
            DatabaseMeta sourceDBInfo = transMeta.findDatabase(sourceDatabaseName);
            DatabaseMeta targetDBInfo = transMeta.findDatabase(targetDatabaseName);

            
            //
            // Add a note
            //
            String note = "Reads information from table [" + sourceTableName+ "] on database [" + sourceDBInfo + "]" + Const.CR;
            note += "After that, it writes the information to table [" + targetTableName + "] on database [" + targetDBInfo + "]";
            NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
            transMeta.addNote(ni);

            // 
            // create the source step...
            //
            String fromstepname = "read from [" + sourceTableName + "]";
            TableInputMeta tii = new TableInputMeta();
            tii.setDatabaseMeta(sourceDBInfo);
            String selectSQL = "SELECT "+Const.CR;
            for (int i=0;i<sourceFields.length;i++)
            {
                if (i>0) selectSQL+=", "; else selectSQL+="  ";
                selectSQL+=sourceFields[i]+Const.CR;
            }
            selectSQL+="FROM "+sourceTableName;
            tii.setSQL(selectSQL);

            PluginRegistry registry = PluginRegistry.getInstance();

            String fromstepid = registry.getPluginId(StepPluginType.class, tii);
            StepMeta fromstep = new StepMeta(fromstepid, fromstepname, (StepMetaInterface) tii);
            fromstep.setLocation(150, 100);
            fromstep.setDraw(true);
            fromstep.setDescription("Reads information from table [" + sourceTableName + "] on database [" + sourceDBInfo + "]");
            transMeta.addStep(fromstep);

            //
            // add logic to rename fields
            // Use metadata logic in SelectValues, use SelectValueInfo...
            //
            SelectValuesMeta svi = new SelectValuesMeta();
            svi.allocate(0, 0, sourceFields.length);
            for (int i = 0; i < sourceFields.length; i++)
            {
                svi.getMeta()[i].setName(sourceFields[i]);
                svi.getMeta()[i].setRename(targetFields[i]);
            }

            String selstepname = "Rename field names";
            String selstepid = registry.getPluginId(StepPluginType.class, svi);
            StepMeta selstep = new StepMeta(selstepid, selstepname, (StepMetaInterface) svi);
            selstep.setLocation(350, 100);
            selstep.setDraw(true);
            selstep.setDescription("Rename field names");
            transMeta.addStep(selstep);

            TransHopMeta shi = new TransHopMeta(fromstep, selstep);
            transMeta.addTransHop(shi);
            fromstep = selstep;

            // 
            // Create the target step...
            //
            //
            // Add the TableOutputMeta step...
            //
            String tostepname = "write to [" + targetTableName + "]";
            TableOutputMeta toi = new TableOutputMeta();
            toi.setDatabaseMeta(targetDBInfo);
            toi.setTablename(targetTableName);
            toi.setCommitSize(200);
            toi.setTruncateTable(true);

            String tostepid = registry.getPluginId(StepPluginType.class, toi);
            StepMeta tostep = new StepMeta(tostepid, tostepname, (StepMetaInterface) toi);
            tostep.setLocation(550, 100);
            tostep.setDraw(true);
            tostep.setDescription("Write information to table [" + targetTableName + "] on database [" + targetDBInfo + "]");
            transMeta.addStep(tostep);

            //
            // Add a hop between the two steps...
            //
            TransHopMeta hi = new TransHopMeta(fromstep, tostep);
            transMeta.addTransHop(hi);

            // OK, if we're still here: overwrite the current transformation...
            return transMeta;
        }
        catch (Exception e)
        {
            throw new KettleException("An unexpected error occurred creating the new transformation", e);
        }
    }

    /**
     * 1) create a new transformation
     * 2) save the transformation as XML file
     * 3) generate the SQL for the target table
     * 4) Execute the transformation
     * 5) drop the target table to make this program repeatable
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
    	KettleEnvironment.init();
        
    	// Init the logging...
    	//
        Log4jFileAppender fileAppender = LogWriter.createFileAppender("TransBuilder.log", true);
        LogWriter.getInstance().addAppender(fileAppender);
                
        // The parameters we want, optionally this can be 
        String fileName = "NewTrans.xml";
        String transformationName = "Test Transformation";
        String sourceDatabaseName = "source";
        String sourceTableName = "Customer";
        String sourceFields[] = { 
                "customernr",
                "Name",
                "firstname",
                "lang",
                "sex",
                "street",
                "housnr",
                "bus",
                "zipcode",
                "location",
                "country",
                "date_of_birth"
            };

        String targetDatabaseName = "target";
        String targetTableName = "Cust";
        String targetFields[] = { 
                "CustNo",
                "LastName",
                "FirstName",
                "Lang",
                "gender",
                "Street",
                "Housno",
                "busno",
                "ZipCode",
                "City",
                "Country",
                "BirthDate"
            };

        
        // Generate the transformation.
        TransMeta transMeta = TransBuilder.buildCopyTable(
                transformationName,
                sourceDatabaseName,
                sourceTableName,
                sourceFields,
                targetDatabaseName,
                targetTableName,
                targetFields
                );
        
        // Save it as a file:
        String xml = transMeta.getXML();
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));
        dos.write(xml.getBytes("UTF-8"));
        dos.close();
        System.out.println("Saved transformation to file: "+fileName);

        // OK, What's the SQL we need to execute to generate the target table?
        String sql = transMeta.getSQLStatementsString();
        
        // Execute the SQL on the target table:
        Database targetDatabase = new Database(transMeta, transMeta.findDatabase(targetDatabaseName));
        targetDatabase.connect();
        targetDatabase.execStatements(sql);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.setLogLevel(LogLevel.DETAILED);
        trans.execute(null);
        trans.waitUntilFinished();
        
        // For testing/repeatability, we drop the target table again
        targetDatabase.execStatement("drop table "+targetTableName);
        targetDatabase.disconnect();
    }


}
