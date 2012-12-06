package org.eobjects.datacleaner.kettle.jobentry;

public enum DataCleanerOutputType {

    HTML("html"), TEXT("txt"), SERIALIZED("analysis.result.dat");

    private final String fileExtension;

    private DataCleanerOutputType(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
