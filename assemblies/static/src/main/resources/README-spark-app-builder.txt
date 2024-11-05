
INSTRUCTION FOR USING PDI AEL's Spark Driver Generator

The script spark-app-builder.sh generates a file that is used by AEL (Pentaho Data Integration - Adaptive
Execution Layer) for running PDI transformations in spark, including native spark versions of PDI steps.
This file contains the Spark Driver Application for PDI, and the needed files by Spark Executors.
AEL is part of pentaho-ee.

Note: In previous versions, this script was named spark-app-builder.sh

You can generate this file by running the following script on mac/linux:

./spark-app-builder.sh

Or, or windows:

spark-app-builder.bat

A file named pdi-spark-driver.zip will be generated in the current directory.
For help/usage use './spark-app-builder.sh -h' .

After generating this file, next steps are:

1) Copy this file to HDFS, so it can be used by AEL for PDI submitted jobs. You can do this on linux command line with:
  - hdfs fs -copyFromLocal pdi-spark-driver.zip /your/directory-path-in-hdfs

2) Configure the PDI AEL Daemon (that runs on your hadoop cluster) to use this file, editing the 'assemblyZip' property
   of the daemon configuration file:
  - Edit $PENTAHO_CLIENT_PATH/data-integration/adaptive-execution/config/application.properties
  - Edit/Add the following line:
    assemblyZip=hdfs:/your/directory-path-in-hdfs/pdi-spark-driver.zip

3) Restart the PDI AEL Daemon
  - cd $PENTAHO_CLIENT_PATH/data-integration/adaptive-execution
  - ./daemon start

4) Check daemon log file for any issues, on $PENTAHO_CLIENT_PATH/data-integration/adaptive-execution/daemon.log .
  - cd $PENTAHO_CLIENT_PATH/data-integration/adaptive-execution
  - tail -f daemon.log  ## Use crtl-c to go back to command prompt

5) Enjoy AEL

NOTES:
 - Note that this bundle file is only used when running in "yarn" mode (AEL config property "sparkMaster=yarn").
 - The $PENTAHO_CLIENT_PATH refers to the base directory where PDI Client is installed.
 - Steps for REGENERATING pdi-spark-driver.zip are similar to the above, but additionally you _may_ need to:
   a) If HDFS file already exists on HDFS filesystem, you will need to delete it first. This can be done with:
      - hdfs -rm -f /your/directory-path-in-hdfs/pdi-spark-driver.zip
   b) Restart the PDI-AEL Daemon so your changes take effect, in case you made changes to AEL
     - cd $PENTAHO_CLIENT_PATH/data-integration/adaptive-execution
     - ./daemon stop
     - ./daemon start
     - tail -f daemon.log ## check log file for any issues
