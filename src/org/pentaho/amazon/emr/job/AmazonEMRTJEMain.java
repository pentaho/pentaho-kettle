package org.pentaho.amazon.emr.job;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class AmazonEMRTJEMain {

  public static int main(String args[]) throws KettleException {

    try {
      initializeKettle();
    } catch (KettleException ke) {
      ke.printStackTrace();
      return -1;
    }

    try {
      String hadoopJobName = "Hadoop Job Name " + AmazonEMRTJEMain.class.hashCode();
      int numEMRInstances = 1;
      int numMapTasks = numEMRInstances;
      int numReduceTasks = numEMRInstances;
      String mapInputStepName = "Hadoop Input";
      String mapOutputStepName = "Hadoop Output";
      String combinerInputStepName = "Hadoop Input";
      String combinerOutputStepName = "Hadoop Output";
      String reducerInputStepName = "Hadoop Input";
      String reducerOutputStepName = "Hadoop Output";
      String outputKeyClass = null;
      String outputValueClass = null;
      String inputFormatClass = null;
      String outputFormatClass = null;
      String inputPath = null;
      String outputPath = null;

      // create the command line parser
      CommandLineParser parser = new PosixParser();

      // create the Options
      Options options = new Options();
      options.addOption(OptionBuilder.withLongOpt("job-name").withDescription("specify the hadoop job name").hasArg().withArgName("JOB NAME").create());
      options.addOption(OptionBuilder.isRequired(true).withLongOpt("num-instances")
          .withDescription("specify the number of EMR nodes, becoming numMapTasks/numReduceTasks default").hasArg().withArgName("COUNT").create());
      options.addOption(OptionBuilder.withLongOpt("num-map-tasks").withDescription("specify the number of map tasks").hasArg().withArgName("COUNT").create());
      options.addOption(OptionBuilder.withLongOpt("num-reduce-tasks").withDescription("specify the number of reduce tasks").hasArg().withArgName("COUNT")
          .create());

      options.addOption(OptionBuilder.isRequired(true).withLongOpt("input-path").withDescription("specify the input path").hasArg().withArgName("INPUT PATH")
          .create());
      options.addOption(OptionBuilder.isRequired(true).withLongOpt("output-path").withDescription("specify the output path").hasArg()
          .withArgName("OUTPUT PATH").create());

      options.addOption(OptionBuilder.withLongOpt("mapper-input-step-name").withDescription("specify the mapper input step name").hasArg()
          .withArgName("STEP NAME").create());
      options.addOption(OptionBuilder.withLongOpt("mapper-output-step-name").withDescription("specify the mapper output step name").hasArg()
          .withArgName("STEP NAME").create());
      options.addOption(OptionBuilder.withLongOpt("combiner-input-step-name").withDescription("specify the combiner input step name").hasArg()
          .withArgName("STEP NAME").create());
      options.addOption(OptionBuilder.withLongOpt("combiner-output-step-name").withDescription("specify the combiner output step name").hasArg()
          .withArgName("STEP NAME").create());
      options.addOption(OptionBuilder.withLongOpt("reducer-input-step-name").withDescription("specify the reducer input step name").hasArg()
          .withArgName("STEP NAME").create());
      options.addOption(OptionBuilder.withLongOpt("reducer-output-step-name").withDescription("specify the reducer output step name").hasArg()
          .withArgName("STEP NAME").create());
      options.addOption(OptionBuilder.isRequired(true).withLongOpt("output-key-class").withDescription("specify the output key class name").hasArg().withArgName("CLASS NAME")
          .create());
      options.addOption(OptionBuilder.isRequired(true).withLongOpt("output-value-class").withDescription("specify the output value class").hasArg().withArgName("CLASS NAME")
          .create());
      options.addOption(OptionBuilder.isRequired(true).withLongOpt("input-format-class").withDescription("specify the input format class name").hasArg()
          .withArgName("CLASS NAME").create());
      options.addOption(OptionBuilder.isRequired(true).withLongOpt("output-format-class").withDescription("specify the output format class name").hasArg()
          .withArgName("CLASS NAME").create());

      // parse the command line arguments
      CommandLine line = null;
      try {
        line = parser.parse(options, args);
        // automatically generate the help statement

        hadoopJobName = line.getOptionValue("job-name", hadoopJobName);

        numEMRInstances = Integer.parseInt(line.getOptionValue("num-instances", "" + numEMRInstances));
        numMapTasks = Integer.parseInt(line.getOptionValue("num-map-tasks", "" + numEMRInstances));
        numReduceTasks = Integer.parseInt(line.getOptionValue("num-reducer-tasks", "" + numEMRInstances));

        mapInputStepName = line.getOptionValue("mapper-input-step-name", mapInputStepName);
        mapOutputStepName = line.getOptionValue("mapper-output-step-name", mapOutputStepName);
        combinerInputStepName = line.getOptionValue("combiner-input-step-name", combinerInputStepName);
        combinerOutputStepName = line.getOptionValue("combiner-input-step-name", combinerOutputStepName);
        reducerInputStepName = line.getOptionValue("reducer-input-step-name", reducerInputStepName);
        reducerOutputStepName = line.getOptionValue("reducer-input-step-name", reducerOutputStepName);

        outputKeyClass = line.getOptionValue("output-key-class", outputKeyClass);
        outputValueClass = line.getOptionValue("output-value-class", outputValueClass);
        inputFormatClass = line.getOptionValue("input-format-class", inputFormatClass);
        outputFormatClass = line.getOptionValue("output-format-class", outputFormatClass);

        inputPath = line.getOptionValue("input-path", inputPath);
        outputPath = line.getOptionValue("output-path", outputPath);

      } catch (Throwable t) {
        t.printStackTrace();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(AmazonEMRTJEMain.class.getSimpleName(), options);
        return -1;
      }

      Class<? extends PluginTypeInterface> pluginType = LifecyclePluginType.class;
      PluginInterface plugin = PluginRegistry.getInstance().findPluginWithId(pluginType, "HadoopSpoonPlugin");
      ClassLoader pluginClassLoader = PluginRegistry.getInstance().getClassLoader(plugin);
      
      JobConf conf = new JobConf(pluginClassLoader.loadClass("org.pentaho.hadoop.mapreduce.PentahoMapRunnable"));
      conf.setJobName(hadoopJobName);
      // TODO: this could be a list of input paths apparently
      // String hdfsBaseUrl = "hdfs://" + hdfsHostName;
      //conf.set("fs.default.name", hdfsBaseUrl + ":" + hdfsPortNumber); //$NON-NLS-1$ //$NON-NLS-2$
      //conf.set("mapred.job.tracker", jobTrackerHostName + ":" + jobTrackerPortNumber); //$NON-NLS-1$ //$NON-NLS-2$
      //conf.setWorkingDirectory(new Path(workingDirectory));
      
      FileInputFormat.setInputPaths(conf, new Path[] { new Path(inputPath) });
      FileOutputFormat.setOutputPath(conf, new Path(outputPath));

      conf.set("debug", "true"); //$NON-NLS-1$
      
      try {
        // mapper
        TransMeta transMeta = new TransMeta("/home/hadoop/transformations/mapper.ktr");
        TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
        TransConfiguration transConfig = new TransConfiguration(transMeta, transExecConfig);
        conf.set("transformation-map-xml", transConfig.getXML()); //$NON-NLS-1$
        conf.set("transformation-map-input-stepname", mapInputStepName); //$NON-NLS-1$
        conf.set("transformation-map-output-stepname", mapOutputStepName); //$NON-NLS-1$
        conf.setMapRunnerClass((Class<? extends MapRunnable>) pluginClassLoader.loadClass("org.pentaho.hadoop.mapreduce.PentahoMapRunnable"));
      } catch (Throwable t) {
        System.err.println("WARNING: Could not load mapper");
      }

      try {
        // combiner
        TransMeta transMeta = new TransMeta("/home/hadoop/transformations/combiner.ktr");
        if (transMeta != null) {
          TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
          TransConfiguration transConfig = new TransConfiguration(transMeta, transExecConfig);
          transConfig = new TransConfiguration(transMeta, transExecConfig);
          conf.set("transformation-combiner-xml", transConfig.getXML()); //$NON-NLS-1$
          conf.set("transformation-combiner-input-stepname", combinerInputStepName); //$NON-NLS-1$
          conf.set("transformation-combiner-output-stepname", combinerOutputStepName); //$NON-NLS-1$
          conf.setCombinerClass((Class<? extends Reducer>) pluginClassLoader.loadClass("org.pentaho.hadoop.mapreduce.GenericTransCombiner"));
        }
      } catch (Throwable t) {
        System.err.println("WARNING: Could not load combiner");
      }

      try {
        // reducer
        TransMeta transMeta = new TransMeta("/home/hadoop/transformations/reducer.ktr");
        if (transMeta != null) {
          TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
          TransConfiguration transConfig = new TransConfiguration(transMeta, transExecConfig);
          transConfig = new TransConfiguration(transMeta, transExecConfig);
          conf.set("transformation-reduce-xml", transConfig.getXML()); //$NON-NLS-1$
          conf.set("transformation-reduce-input-stepname", reducerInputStepName); //$NON-NLS-1$
          conf.set("transformation-reduce-output-stepname", reducerOutputStepName); //$NON-NLS-1$
          conf.setReducerClass((Class<? extends Reducer>) pluginClassLoader.loadClass("org.pentaho.hadoop.mapreduce.GenericTransReduce"));
        }
      } catch (Throwable t) {
        System.err.println("WARNING: Could not load reducer");
      }

      ClassLoader mainClassLoader = AmazonEMRTJEMain.class.getClassLoader();
      if (outputKeyClass != null) {
        conf.setOutputKeyClass(mainClassLoader.loadClass(outputKeyClass));
      }
      if (outputValueClass != null) {
        conf.setOutputValueClass(mainClassLoader.loadClass(outputValueClass));
      }

      if (inputFormatClass != null) {
        Class<? extends InputFormat> inputFormat = (Class<? extends InputFormat>) mainClassLoader.loadClass(inputFormatClass);
        conf.setInputFormat(inputFormat);
      }
      if (outputFormatClass != null) {
        Class<? extends OutputFormat> outputFormat = (Class<? extends OutputFormat>) mainClassLoader.loadClass(outputFormatClass);
        conf.setOutputFormat(outputFormat);
      }

      // // process user defined values
      // for (UserDefinedItem item : userDefined) {
      //        if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
      // conf.set(item.getName(), item.getValue());
      // }
      // }
      conf.setJarByClass(pluginClassLoader.loadClass("org.pentaho.hadoop.mapreduce.PentahoMapRunnable"));

      conf.setMapOutputKeyClass(Text.class);
      conf.setMapOutputValueClass(IntWritable.class);
      
      conf.setNumMapTasks(2);
      conf.setNumReduceTasks(numReduceTasks);

      // // get a reference to the variable space
      // VariableSpace variableSpace = this.getVariables();
      // XStream xStream = new XStream();
      // // this is optional - doing it since the 2 minute tutorial does it
      // xStream.alias("variableSpace", VariableSpace.class);
      // // serialize the variable space to XML
      // String xmlVariableSpace = xStream.toXML(variableSpace);
      // // set a string in the job configuration as the serialized variablespace
      // conf.setStrings("variableSpace", xmlVariableSpace);
      // // we now tell the job what level of logging this job is running at
      // conf.setStrings("logLevel", this.getLogLevel().toString());

      JobClient jobClient = new JobClient(conf);
      RunningJob runningJob = jobClient.submitJob(conf);

    } catch (Throwable t) {
      t.printStackTrace();
      return -1;
    }

    return 0;
  }

  public static void initializeKettle() throws KettleException {
    if (!KettleEnvironment.isInitialized()) {
      // Additionally load plugins from:
      // $HADOOP_HOME/plugins
      // $HADOOP_PDI_PLUGIN_FOLDER
      String HADOOP_HOME = System.getenv("HADOOP_HOME");
      if (StringUtils.isEmpty(HADOOP_HOME)) {
        HADOOP_HOME = "/home/hadoop";
      }
      String hadoopPdiPluginPaths = HADOOP_HOME + "/plugins"; //$NON-NLS-1$ //$NON-NLS-2$
      String hadoopPdiPluginFolder = System.getenv("HADOOP_PDI_PLUGIN_FOLDER"); //$NON-NLS-1$
      if (!StringUtils.isEmpty(hadoopPdiPluginFolder)) {
        hadoopPdiPluginPaths += "," + hadoopPdiPluginFolder; //$NON-NLS-1$
      }
      hadoopPdiPluginPaths = Const.DEFAULT_PLUGIN_BASE_FOLDERS + "," + hadoopPdiPluginPaths; //$NON-NLS-1$
      System.setProperty(Const.PLUGIN_BASE_FOLDERS_PROP, hadoopPdiPluginPaths);
      KettleEnvironment.init();
    }
  }

}
