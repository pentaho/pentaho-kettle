The YAML files located in these need to be validated that they 
do indeed work.  They should be run using GPLoad.

The GPLoad-insert3.cfg file fails on our Greenplum server as
we do not have another server that runs Greenplum's GPFDist.
When gpload is run with this control file the syntax of
the file appears to be correct as the error happens is when 
trying to resolve etl-host1.