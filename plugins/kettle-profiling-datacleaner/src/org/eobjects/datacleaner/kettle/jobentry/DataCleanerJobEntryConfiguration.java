package org.eobjects.datacleaner.kettle.jobentry;

import java.io.Serializable;

public class DataCleanerJobEntryConfiguration implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private String executableFilename;
    private String jobFilename;
    private DataCleanerOutputType outputType;
    private String outputFilename;
    private String additionalArguments;
    private boolean outputFileInResult = true;

    public String getJobFilename() {
        if (jobFilename == null) {
            jobFilename = "examples/employees.analysis.xml";
        }
        return jobFilename;
    }

    public void setJobFilename(String jobFile) {
        this.jobFilename = jobFile;
    }

    public String getExecutableFilename() {
        if (executableFilename == null) {
            executableFilename = "${user.home}/DataCleaner/DataCleaner-console.exe";
        }
        return executableFilename;
    }

    public void setExecutableFilename(String executableFile) {
        this.executableFilename = executableFile;
    }

    public String getOutputFilename() {
        if (outputFilename == null) {
            outputFilename = "out." + getOutputType().getFileExtension();
        }
        return outputFilename;
    }

    public void setOutputFilename(String outputFile) {
        this.outputFilename = outputFile;
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

    public String getAdditionalArguments() {
        if (additionalArguments == null) {
            additionalArguments = "";
        }
        return additionalArguments;
    }

    public void setAdditionalArguments(String additionalArguments) {
        this.additionalArguments = additionalArguments;
    }

    public void setOutputFileInResult(boolean outputFileInResult) {
        this.outputFileInResult = outputFileInResult;
    }

    public boolean isOutputFileInResult() {
        return outputFileInResult;
    }
}
