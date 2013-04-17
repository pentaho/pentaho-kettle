package org.pentaho.di.core.extension;

public enum KettleExtensionPoint {

  // Some transformation points
  //
  TransformationPrepareExecution("TransformationStart", "A transformation begins to prepare execution"),
  TransformationStartThreads("TransformationStart", "A transformation begins to start"),
  TransformationStarted("TransformationStart", "A transformation has started"),
  TransformationFinish("TransformationFinish", "A transformation finishes"),
  TransformationMetaLoaded("TransMetaLoaded", "Transformation metadata was loaded"),
  SpoonTransMetaExecutionStart("SpoonTransMetaExecutionStart", "Spoon initiates the execution of a trans (TransMeta)"),

  JobStart("JobStart", "A job starts"),
  JobFinish("JobFinish", "A job finishes"),

  JobMetaLoaded("JobMetaLoaded", "Job metadata was loaded"),

  SpoonJobMetaExecutionStart("SpoonJobMetaExecutionStart", "Spoon initiates the execution of a job (JobMeta)"),
  ;

  public String id;

  public String description;

  private KettleExtensionPoint(String id, String description) {
    this.id = id;
    this.description = description;
  }
}
