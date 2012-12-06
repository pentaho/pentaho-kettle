################USAGE OF REPORTRUNNER#########################
# echo "org.eclipse.birt.report.engine.impl.ReportRunner Usage:";
# echo "--mode/-m [ run | render | runrender] the default is runrender "
# echo "for runrender mode: "
# echo "" "we should add it in the end <design file> "
# echo "" "--format/-f [ HTML | PDF ] "
# echo "" "--output/-o <target file>"
# echo "" "--htmlType/-t < HTML | ReportletNoCSS >"
# echo "" "--locale /-l<locale>"
# echo "" "--parameter/-p <"parameterName=parameterValue">"
# echo "" "--file/-F <parameter file>"
# echo "" "--encoding/-e <target encoding>"
# echo " "
# echo "Locale: default is english"
# echo "parameters in command line will overide parameters in parameter file"
# echo "parameter name cant include characters such as \ ', '=', ':'"
# echo " "
# echo "For RUN mode:"
# echo "we should add it in the end<design file>"
# echo "" "--output/-o <target file>"
# echo "" "--locale /-l<locale>"
# echo "" "--parameter/-p <parameterName=parameterValue>"
# echo "" "--file/-F <parameter file>"
# echo " "
# echo "Locale: default is english"
# echo "parameters in command line will overide parameters in parameter file"
# echo "parameter name cant include characters such as \ ', '=', ':'"
# echo " "
# echo "For RENDER mode:"
# echo "" "we should add it in the end<design file>"
# echo "" "--output/-o <target file>"
# echo "" "--page/-p <pageNumber>"
# echo "" "--locale /-l<locale>"
# echo " "
# echo "Locale: default is english"
################END OF USAGE #########################
if [ "$BIRT_HOME" = "" ];

then
echo " The BIRT_HOME need be set before BirtRunner can run.";
else

export BIRTCLASSPATH="$BIRT_HOME/ReportEngine/lib/chartengineapi.jar:$BIRT_HOME/ReportEngine/lib/chartexamplescoreapi.jar:$BIRT_HOME/ReportEngine/lib/chartitemapi.jar:$BIRT_HOME/ReportEngine/lib/com.ibm.icu_4.2.1.v20100412.jar:$BIRT_HOME/ReportEngine/lib/commons-cli-1.0.jar:$BIRT_HOME/ReportEngine/lib/coreapi.jar:$BIRT_HOME/ReportEngine/lib/crosstabcoreapi.jar:$BIRT_HOME/ReportEngine/lib/dataadapterapi.jar:$BIRT_HOME/ReportEngine/lib/dataaggregationapi.jar:$BIRT_HOME/ReportEngine/lib/dataextraction.jar:$BIRT_HOME/ReportEngine/lib/dteapi.jar:$BIRT_HOME/ReportEngine/lib/emitterconfig.jar:$BIRT_HOME/ReportEngine/lib/engineapi.jar:$BIRT_HOME/ReportEngine/lib/flute.jar:$BIRT_HOME/ReportEngine/lib/js.jar:$BIRT_HOME/ReportEngine/lib/modelapi.jar:$BIRT_HOME/ReportEngine/lib/modelodaapi.jar:$BIRT_HOME/ReportEngine/lib/odadesignapi.jar:$BIRT_HOME/ReportEngine/lib/org.apache.commons.codec_1.3.0.v20100518-1140.jar:$BIRT_HOME/ReportEngine/lib/org.eclipse.emf.common_2.6.0.v20100914-1218.jar:$BIRT_HOME/ReportEngine/lib/org.eclipse.emf.ecore_2.6.1.v20100914-1218.jar:$BIRT_HOME/ReportEngine/lib/org.eclipse.emf.ecore.xmi_2.5.0.v20100521-1846.jar:$BIRT_HOME/ReportEngine/lib/org.w3c.css.sac_1.3.0.v200805290154.jar:$BIRT_HOME/ReportEngine/lib/scriptapi.jar"

JAVACMD='java';
$JAVACMD -cp "$BIRTCLASSPATH" -DBIRT_HOME="$BIRT_HOME/ReportEngine" org.eclipse.birt.report.engine.api.ReportRunner ${1+"$@"}

fi
