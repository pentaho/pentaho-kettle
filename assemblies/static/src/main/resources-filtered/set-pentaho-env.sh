#!/bin/sh

# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2010 - ${copyright.year} by Hitachi Vantara : http://www.hitachivantara.com
#
# *****************************************************************************
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# *****************************************************************************

# -----------------------------------------------------------------------------
# Finds a suitable Java
#
# Looks in well-known locations to find a suitable Java then sets two 
# environment variables for use in other script files. The two environment
# variables are:
# 
# * _PENTAHO_JAVA_HOME - absolute path to Java home
# * _PENTAHO_JAVA - absolute path to Java launcher (e.g. java)
# 
# The order of the search is as follows:
#
# 1. argument #1 - path to Java home
# 2. environment variable PENTAHO_JAVA_HOME - path to Java home
# 3. jre folder at current folder level
# 4. java folder at current folder level
# 5. jre folder one level up
# 6 java folder one level up
# 7. jre folder two levels up
# 8. java folder two levels up
# 9. environment variable JAVA_HOME - path to Java home
# 10. environment variable JRE_HOME - path to Java home


# 
# If a suitable Java is found at one of these locations, then 
# _PENTAHO_JAVA_HOME is set to that location and _PENTAHO_JAVA is set to the 
# absolute path of the Java launcher at that location. If none of these 
# locations are suitable, then _PENTAHO_JAVA_HOME is set to empty string and 
# _PENTAHO_JAVA is set to java.
# 
# Finally, there is one final optional environment variable: PENTAHO_JAVA.
# If set, this value is used in the construction of _PENTAHO_JAVA. If not 
# set, then the value java is used. 
# -----------------------------------------------------------------------------

setPentahoEnv() {
  DIR_REL=`dirname $0`
  cd $DIR_REL
  DIR=`pwd`
  cd - > /dev/null
	
  if [ -n "$PENTAHO_JAVA" ]; then
    __LAUNCHER="$PENTAHO_JAVA"
  else
    __LAUNCHER="java"
  fi
  if [ -n "$1" ] && [ -d "$1" ] && [ -x "$1"/bin/$__LAUNCHER ]; then
    # echo "DEBUG: Using value ($1) from calling script"
    _PENTAHO_JAVA_HOME="$1"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER  
  elif [ -n "$PENTAHO_JAVA_HOME" ]; then
    # echo "DEBUG: Using PENTAHO_JAVA_HOME"
    _PENTAHO_JAVA_HOME="$PENTAHO_JAVA_HOME"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -x "$DIR/jre/bin/$__LAUNCHER" ]; then
    # echo DEBUG: Found JRE at the current folder
    _PENTAHO_JAVA_HOME="$DIR/jre"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -x "$DIR/java/bin/$__LAUNCHER" ]; then
    # echo DEBUG: Found JAVA at the current folder
    _PENTAHO_JAVA_HOME="$DIR/java"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -x "$DIR/../jre/bin/$__LAUNCHER" ]; then
    # echo DEBUG: Found JRE one folder up
    _PENTAHO_JAVA_HOME="$DIR/../jre"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -x "$DIR/../java/bin/$__LAUNCHER" ]; then
    # echo DEBUG: Found JAVA one folder up
    _PENTAHO_JAVA_HOME="$DIR/../java"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -x "$DIR/../../jre/bin/$__LAUNCHER" ]; then
    # echo DEBUG: Found JRE two folders up
    _PENTAHO_JAVA_HOME="$DIR/../../jre"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -x "$DIR/../../java/bin/$__LAUNCHER" ]; then
    # echo DEBUG: Found JAVA two folders up
    _PENTAHO_JAVA_HOME="$DIR/../../java"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -n "$JAVA_HOME" ]; then
    # echo "DEBUG: Using JAVA_HOME"
    _PENTAHO_JAVA_HOME="$JAVA_HOME"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  elif [ -n "$JRE_HOME" ]; then
    # echo "DEBUG: Using JRE_HOME"
    _PENTAHO_JAVA_HOME="$JRE_HOME"
    _PENTAHO_JAVA="$_PENTAHO_JAVA_HOME"/bin/$__LAUNCHER
  else
    # echo "WARNING: Using java from path"
    _PENTAHO_JAVA_HOME=
    _PENTAHO_JAVA=$__LAUNCHER
  fi
  # echo "DEBUG: _PENTAHO_JAVA_HOME=$_PENTAHO_JAVA_HOME"
  # echo "DEBUG: _PENTAHO_JAVA=$_PENTAHO_JAVA"
}
