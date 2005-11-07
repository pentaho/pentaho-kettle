
FILE=$1
NEWFILE=$1.new

echo " /**********************************************************************" >  ${NEWFILE}
echo " **                                                                   **" >> ${NEWFILE}
echo " **               This code belongs to the KETTLE project.            **" >> ${NEWFILE}
echo " **                                                                   **" >> ${NEWFILE}
echo " ** It belongs to, is maintained by and is copyright 1999-2005 by     **" >> ${NEWFILE}
echo " **                                                                   **" >> ${NEWFILE}
echo " **      i-Bridge bvba                                                **" >> ${NEWFILE}
echo " **      Fonteinstraat 70                                             **" >> ${NEWFILE}
echo " **      9400 OKEGEM                                                  **" >> ${NEWFILE}
echo " **      Belgium                                                      **" >> ${NEWFILE}
echo " **      http://www.kettle.be                                         **" >> ${NEWFILE}
echo " **      info@kettle.be                                               **" >> ${NEWFILE}
echo " **                                                                   **" >> ${NEWFILE}
echo " **********************************************************************/" >> ${NEWFILE}
echo " " >> ${NEWFILE}
cat ${FILE} >> ${NEWFILE}


