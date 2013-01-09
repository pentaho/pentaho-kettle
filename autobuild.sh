
cd $(dirname $0)
JAVA_HOME=/usr/java/j2sdk1.4.2_13
export JAVA_HOME

TEMP_FILE=/tmp/autobuild.$$.log

echo "Running autobuild : `date`"

svn update > ${TEMP_FILE}
cat ${TEMP_FILE}

NR_LINES=$( cat ${TEMP_FILE} | egrep -v "^At revision|^Updated to revision" | wc -l )

if [ ${NR_LINES} -gt 0 ]
then
  echo "----------------------------------------------------------------"
  echo "There where ${NR_LINES} commits, initiating build process..."
  echo "----------------------------------------------------------------"
  echo
  ant zip

  #
  # upload kettle3.jar to Kettle.be
  #
  cd lib
  ftp -u ftp://www.kettle.be:*******@www.kettle.be/dloads/ kettle3.jar

else
  echo "Nothing was updated"
fi

rm ${TEMP_FILE}

