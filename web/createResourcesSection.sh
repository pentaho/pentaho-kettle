
cd ..

echo '
 <resources>
  <j2se version="1.4+" java-vm-args="-Xmx256M"/>
  <jar href="lib/kettle3.jar"/>
'


for file in $( find libext -name "*.jar" )
do
   echo '  <jar href="'${file}'"/>'
done

echo '
  <jar href="libswt/jface.jar"/>
  <jar href="libswt/runtime.jar"/>
 </resources>
'

cd -
