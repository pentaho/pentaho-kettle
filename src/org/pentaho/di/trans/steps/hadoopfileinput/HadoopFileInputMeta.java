package org.pentaho.di.trans.steps.hadoopfileinput;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;

@Step(id = "HadoopFileInputPlugin", image = "HDP.png", name = "Hadoop File Input", description="Process files from an HDFS location", categoryDescription="Hadoop")
public class HadoopFileInputMeta extends TextFileInputMeta {

}
