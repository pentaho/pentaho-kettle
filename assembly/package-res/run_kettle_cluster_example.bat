for f in `find third-party -type f -name "*.jar"` `find build/jars -type f -name "*.jar"`
do
  CLASSPATH=$CLASSPATH:$f
done

JAVA_BIN=java

$JAVA_BIN -cp $CLASSPATH com.pentaho.appliance.Appliance resource.env=resource/solution/example/example.envxml resource.main=resource/solution/example/GenerateRows.ktr control.start control.finish
