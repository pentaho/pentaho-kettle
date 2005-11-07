#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

CLASSPATH=.
CLASSPATH=$CLASSPATH:lib/kettle.jar
CLASSPATH=$CLASSPATH:libswt/jface.jar
CLASSPATH=$CLASSPATH:libswt/runtime.jar

# **************************************************
# ** JDBC & other libraries used by Kettle:       **
# **************************************************

for f in `find libext`
do
  CLASSPATH=$CLASSPATH:$f
done

# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

JAVA_BIN=java
LIBPATH="NONE"

case `uname -s` in 
	AIX)
		LIBPATH=libswt/aix/
		;;

	SunOS) 
		LIBPATH=libswt/solaris/
		;;

	Darwin)
		LIBPATH=libswt/osx/
		JAVA_BIN=libswt/osx/java_swt
		;;

	Linux)  
		LIBPATH=libswt/linux/
		;;

	HP-UX) 
		LIBPATH=libswt/hpux/
		;;
	CYGWIN*)
		./Spoon.bat
		# exit
		;;

	*) 
		echo Spoon is not supported on this hosttype : `uname -s`
		exit
		;;
esac 

export LIBPATH

if [ "$LIBPATH" != "NONE" ]
then
  for f in `find $LIBPATH -name '*.jar'`
  do
    CLASSPATH=$CLASSPATH:$f
  done
fi


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 128m to higher values in case you run out of memory.  **
# ******************************************************************

OPT="-Xmx256m -cp $CLASSPATH -Djava.library.path=$LIBPATH"

# ***************
# ** Run...    **
# ***************

$JAVA_BIN $OPT be.ibridge.kettle.spoon.Spoon "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"

