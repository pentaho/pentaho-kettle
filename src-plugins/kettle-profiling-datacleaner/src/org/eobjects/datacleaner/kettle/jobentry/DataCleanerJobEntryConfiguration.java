package org.eobjects.datacleaner.kettle.jobentry;

import java.io.Serializable;

public class DataCleanerJobEntryConfiguration implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private String executableFile;
    private String jobFile;
    private DataCleanerOutputType outputType;
    private String outputFile;

    public String getJobFile() {
        if (jobFile == null) {
            jobFile = "examples/employees.analysis.xml";
        }
        return jobFile;
    }

    public void setJobFile(String jobFile) {
        this.jobFile = jobFile;
    }

    public String getExecutableFile() {
        if (executableFile == null) {
            executableFile = "${user.home}/DataCleaner/DataCleaner-console.exe";
        }
        return executableFile;
    }

    public void setExecutableFile(String executableFile) {
        this.executableFile = executableFile;
    }

    public String getOutputFile() {
        if (outputFile == null) {
            outputFile = "out." + getOutputType().getFileExtension();
        }
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public DataCleanerOutputType getOutputType() {
        if (outputType == null) {
            outputType = DataCleanerOutputType.SERIALIZED;
        }
        return outputType;
    }

    public void setOutputType(DataCleanerOutputType outputType) {
        this.outputType = outputType;
    }
}
