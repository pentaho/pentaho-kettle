#!/bin/sh
# 
# eobjects.org DataCleaner
# Copyright (C) 2010 eobjects.org
# 
# This copyrighted material is made available to anyone wishing to use, modify,
# copy, or redistribute it subject to the terms and conditions of the GNU
# Lesser General Public License, as published by the Free Software Foundation.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
# for more details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with this distribution; if not, write to:
# Free Software Foundation, Inc.
# 51 Franklin Street, Fifth Floor
# Boston, MA  02110-1301  USA

cd `dirname $0`
DATACLEANER_HOME=`pwd`
export DATACLEANER_HOME
echo "Using DATACLEANER_HOME: $DATACLEANER_HOME"

JAVA_OPTS="$JAVA_OPTS -Xmx1024m"

macos=false
case "`uname`" in
	Darwin*) macos=true;;
esac

if $macos; then
	JAVA_OPTS="$JAVA_OPTS -Xdock:name=DataCleaner"
fi

exec java $JAVA_OPTS -jar DataCleaner.jar $*