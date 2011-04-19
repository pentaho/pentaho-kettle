The sample-src contains code that can be built and jar'd for use by the Hadoop 
Job Executor - Simple Mode. The sample is a WordCount example where 4 command
line arguments can be passed in to override the defaults.

--input=DIR                   The directory containing the input files for the
                              WordCount Hadoop job
--output=DIR                  The directory where the results of the WordCount
                              Hadoop job will be stored
--hdfsHost=HOST               The host<:port> of the HDFS service
                              e.g.- localhost:9000
--jobTrackerHost=HOST         The host<:port> of the job tracker service
                              e.g.- localhost:9001