
cd ..

echo '
 <resources>
  <j2se version="1.5+" java-vm-args="-Xmx256M"/>
  '
  
for file in $( find lib -name "*.jar" )
do
   echo '  <jar href="'${file}'"/>'
done

echo

for file in $( find libext -name "*.jar" )
do
   echo '  <jar href="'${file}'"/>'
done

echo 

for file in $( find libswt -maxdepth 1 -name "*.jar")
do
   echo '  <jar href="'${file}'"/>'
done

echo '
  <extension name="sun" href="activation.jnlp"/>
  <extension name="sun" href="mail.jnlp"/>
  <extension name="sun" href="swt.jnlp"/>'

echo '
  </resources>
'

cd -
