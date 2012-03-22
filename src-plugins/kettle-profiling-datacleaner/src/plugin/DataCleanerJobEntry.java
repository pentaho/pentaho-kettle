package plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.kettle.jobentry.DataCleanerJobEntryConfiguration;
import org.eobjects.datacleaner.kettle.jobentry.DataCleanerJobEntryDialog;
import org.eobjects.datacleaner.kettle.jobentry.DataCleanerOutputType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

@JobEntry(id = "DataCleanerJobEntry", categoryDescription = "DataCleaner", image = "org/eobjects/datacleaner/logo.png", name = DataCleanerJobEntry.NAME, description = "Executes a DataCleaner job")
public class DataCleanerJobEntry extends JobEntryBase implements JobEntryInterface, Cloneable {
    
    public static final String NAME = "Execute DataCleaner job";

    private final DataCleanerJobEntryConfiguration configuration = new DataCleanerJobEntryConfiguration();

    @Override
    public Result execute(Result result, int nr) throws KettleException {

        final List<String> commands = new ArrayList<String>();
        final String executableFilePath = environmentSubstitute(configuration.getExecutableFile());
        final File executableFile = new File(executableFilePath);
        commands.add(executableFilePath);
        commands.add("-job");
        commands.add(environmentSubstitute(configuration.getJobFile()));
        commands.add("-ot");
        commands.add(configuration.getOutputType().toString());
        commands.add("-of");
        commands.add(environmentSubstitute(configuration.getOutputFile()));

        final ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(executableFile.getParentFile());
        processBuilder.redirectErrorStream(true);

        try {
            final Process process = processBuilder.start();

            if (log.isBasic()) {
                InputStream inputStream = process.getInputStream();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        logBasic("DataCleaner: " + line);
                    }
                } finally {
                    inputStream.close();
                }
            }

            int exitCode = process.waitFor();

            result.setExitStatus(exitCode);
            result.setResult(true);
        } catch (Exception e) {
            logError("Error occurred while executing DataCleaner job", e);
            result.setResult(false);
            throw new KettleException(e);
        }

        return result;
    }

    @Override
    public DataCleanerJobEntry clone() {
        final DataCleanerJobEntry clone = (DataCleanerJobEntry) super.clone();
        return clone;
    }

    public DataCleanerJobEntryConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getDialogClassName() {
        return DataCleanerJobEntryDialog.class.getName();
    }

    public String getXML() {
        final StringBuilder retval = new StringBuilder();

        retval.append(super.getXML());
        retval.append("      ").append(XMLHandler.addTagValue("executable_file", configuration.getExecutableFile()));
        retval.append("      ").append(XMLHandler.addTagValue("job_file", configuration.getJobFile()));
        retval.append("      ").append(XMLHandler.addTagValue("output_file", configuration.getOutputFile()));
        retval.append("      ").append(XMLHandler.addTagValue("output_type", configuration.getOutputType().toString()));

        return retval.toString();
    }

    public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
            throws KettleXMLException {
        try {
            super.loadXML(entrynode, databases, slaveServers);

            configuration.setExecutableFile(XMLHandler.getTagValue(entrynode, "executable_file"));
            configuration.setJobFile(XMLHandler.getTagValue(entrynode, "job_file"));
            configuration.setOutputFile(XMLHandler.getTagValue(entrynode, "output_file"));
            configuration
                    .setOutputType(DataCleanerOutputType.valueOf(XMLHandler.getTagValue(entrynode, "output_type")));

        } catch (KettleXMLException xe) {
            throw new KettleXMLException("Unable to load job entry from XML node", xe);
        }
    }

    @Override
    public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
        super.saveRep(rep, id_job);

        rep.saveJobEntryAttribute(id_job, getObjectId(), "executable_file", configuration.getExecutableFile());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "job_file", configuration.getJobFile());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "output_file", configuration.getOutputFile());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "output_type", configuration.getOutputType().toString());
    }

    @Override
    public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases,
            List<SlaveServer> slaveServers) throws KettleException {
        super.loadRep(rep, id_jobentry, databases, slaveServers);

        configuration.setExecutableFile(rep.getJobEntryAttributeString(id_jobentry, "executable_file"));
        configuration.setJobFile(rep.getJobEntryAttributeString(id_jobentry, "job_file"));
        configuration.setOutputFile(rep.getJobEntryAttributeString(id_jobentry, "output_file"));
        configuration.setOutputType(DataCleanerOutputType.valueOf(rep.getJobEntryAttributeString(id_jobentry,
                "output_type")));
    }
}
