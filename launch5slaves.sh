
PORTS="8080 8081 8082 8083 8084"

if [ "$1" != "-nobuild" ]
then
  ant
fi

cd distrib

for port in $PORTS
do
  sh carte.sh 127.0.0.1 $port > /tmp/carte_localhost_$port.log 2>> /tmp/carte_localhost_$port.log &
done

sleep 3

PROCESSLIST=$( ps -eaf | grep "org.pentaho.di.www.Carte" | egrep -v "grep" | awk '{ print $2 }' )
echo "Use the following command to kill the slave servers: "
echo 
echo -n "kill "
for pid in $PROCESSLIST
do
  echo -n "$pid "
done

echo 
echo 
