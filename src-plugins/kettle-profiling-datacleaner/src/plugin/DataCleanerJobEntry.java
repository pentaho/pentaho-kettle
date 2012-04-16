package plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.vfs.FileObject;
import org.eobjects.datacleaner.kettle.jobentry.DataCleanerJobEntryConfiguration;
import org.eobjects.datacleaner.kettle.jobentry.DataCleanerJobEntryDialog;
import org.eobjects.datacleaner.kettle.jobentry.DataCleanerOutputType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * A job entry for executing DataCleaner jobs
 * 
 * @author Kasper Sorensen
 * @since 22-03-2012
 */
@JobEntry(id = "DataCleanerJobEntry", categoryDescription = "Utility", image = "org/eobjects/datacleaner/logo.png", name = DataCleanerJobEntry.NAME, description = "Executes a DataCleaner job")
public class DataCleanerJobEntry extends JobEntryBase implements JobEntryInterface, Cloneable {

    public static final String NAME = "Execute DataCleaner job";

    private final DataCleanerJobEntryConfiguration configuration = new DataCleanerJobEntryConfiguration();

    @Override
    public Result execute(Result result, int nr) throws KettleException {

        final List<String> commands = new ArrayList<String>();
        final String executableFilePath = environmentSubstitute(configuration.getExecutableFilename());
        final File executableFile = new File(executableFilePath);
        final String outputFilename = environmentSubstitute(configuration.getOutputFilename());

        commands.add(executableFilePath);
        commands.add("-job");
        commands.add(environmentSubstitute(configuration.getJobFilename()));
        commands.add("-ot");
        commands.add(configuration.getOutputType().toString());
        commands.add("-of");
        commands.add(outputFilename);

        final String additionalArguments = configuration.getAdditionalArguments();
        if (additionalArguments != null && additionalArguments.length() != 0) {
            final String[] args = additionalArguments.split(" ");
            for (String arg : args) {
                commands.add(arg);
            }
        }

        final ProcessBuilder processBuilder = new ProcessBuilder(commands);
        final File dataCleanerDirectory = executableFile.getParentFile();
        processBuilder.directory(dataCleanerDirectory);
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

            if (configuration.isOutputFileInResult()) {
                File outputFile = new File(outputFilename);
                if (!outputFile.exists()) {
                    outputFile = new File(dataCleanerDirectory, outputFilename);
                }

                if (outputFile.exists()) {
                    final Map<String, ResultFile> files = new ConcurrentHashMap<String, ResultFile>();
                    final FileObject fileObject = KettleVFS.getFileObject(outputFile.getCanonicalPath(), this);
                    final ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileObject,
                            parentJob.getJobname(), toString());
                    files.put(outputFilename, resultFile);
                    result.setResultFiles(files);
                }
            }

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
        retval.append("      ")
                .append(XMLHandler.addTagValue("executable_file", configuration.getExecutableFilename()));
        retval.append("      ").append(XMLHandler.addTagValue("job_file", configuration.getJobFilename()));
        retval.append("      ").append(XMLHandler.addTagValue("output_file", configuration.getOutputFilename()));
        retval.append("      ").append(XMLHandler.addTagValue("output_type", configuration.getOutputType().toString()));
        retval.append("      ").append(
                XMLHandler.addTagValue("output_file_in_result", configuration.isOutputFileInResult()));

        return retval.toString();
    }

    public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
            throws KettleXMLException {
        try {
            super.loadXML(entrynode, databases, slaveServers);

            configuration.setExecutableFilename(XMLHandler.getTagValue(entrynode, "executable_file"));
            configuration.setJobFilename(XMLHandler.getTagValue(entrynode, "job_file"));
            configuration.setOutputFilename(XMLHandler.getTagValue(entrynode, "output_file"));
            configuration
                    .setOutputType(DataCleanerOutputType.valueOf(XMLHandler.getTagValue(entrynode, "output_type")));
            final String outputFileInResult = XMLHandler.getTagValue(entrynode, "output_file_in_result");
            if ("false".equalsIgnoreCase(outputFileInResult)) {
                configuration.setOutputFileInResult(false);
            }

        } catch (KettleXMLException xe) {
            throw new KettleXMLException("Unable to load job entry from XML node", xe);
        }
    }

    @Override
    public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
        super.saveRep(rep, id_job);

        rep.saveJobEntryAttribute(id_job, getObjectId(), "executable_file", configuration.getExecutableFilename());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "job_file", configuration.getJobFilename());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "output_file", configuration.getOutputFilename());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "output_type", configuration.getOutputType().toString());
        rep.saveJobEntryAttribute(id_job, getObjectId(), "output_file_in_result", configuration.isOutputFileInResult());
    }

    @Override
    public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases,
            List<SlaveServer> slaveServers) throws KettleException {
        super.loadRep(rep, id_jobentry, databases, slaveServers);

        configuration.setExecutableFilename(rep.getJobEntryAttributeString(id_jobentry, "executable_file"));
        configuration.setJobFilename(rep.getJobEntryAttributeString(id_jobentry, "job_file"));
        configuration.setOutputFilename(rep.getJobEntryAttributeString(id_jobentry, "output_file"));
        configuration.setOutputType(DataCleanerOutputType.valueOf(rep.getJobEntryAttributeString(id_jobentry,
                "output_type")));
        configuration
                .setOutputFileInResult(rep.getJobEntryAttributeBoolean(id_jobentry, "output_file_in_result", true));
    }
}
