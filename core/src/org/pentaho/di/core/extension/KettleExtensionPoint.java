package org.pentaho.di.core.extension;

public enum KettleExtensionPoint {

  // Some transformation points
  //
  TransformationPrepareExecution("TransformationPrepareExecution", "A transformation begins to prepare execution"),
  TransformationStartThreads("TransformationStartThreads", "A transformation begins to start"),
  TransformationStarted("TransformationStart", "A transformation has started"),
  TransformationFinish("TransformationFinish", "A transformation finishes"),
  TransformationMetaLoaded("TransformationMetaLoaded", "Transformation metadata was loaded"),
  SpoonTransMetaExecutionStart("SpoonTransMetaExecutionStart", "Spoon initiates the execution of a trans (TransMeta)"),
  SpoonTransExecutionConfiguration("SpoonTransExecutionConfiguration", "Right before Spoon configuration of transformation to be executed takes place"),

  JobStart("JobStart", "A job starts"),
  JobFinish("JobFinish", "A job finishes"),
  JobMetaLoaded("JobMetaLoaded", "Job metadata was loaded"),
  SpoonJobMetaExecutionStart("SpoonJobMetaExecutionStart", "Spoon initiates the execution of a job (JobMeta)"),
  SpoonJobExecutionConfiguration("SpoonJobExecutionConfiguration", "Right before Spoon configuration of job to be executed takes place"),
  
  DatabaseConnected("DatabaseConnected", "A connection to a database was made"),
  DatabaseDisconnected("DatabaseDisconnected", "A connection to a database was terminated"),
  ;

  public String id;

  public String description;

  private KettleExtensionPoint(String id, String description) {
    this.id = id;
    this.description = description;
  }
}
