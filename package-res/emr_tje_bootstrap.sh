#!/bin/bash
set -e
echo "Starting bootstrap script..."
# pass in url minus file as arg 1 and filename as arg 2
cd /home/hadoop
echo "Copying MR transformations from S3 to local FS"
hadoop dfs -copyToLocal $1/transformations/ .
echo "Retrieving kettle distribution..."
# wget -S -T 10 -t 5 $1/$2
hadoop dfs -copyToLocal $1/$2 .
echo "Extracting kettle distribution..."
unzip -qqo $2
echo "Exiting bootstrap script..."
ls -la /home/hadoop
ls -la /home/hadoop/plugins
exit 0