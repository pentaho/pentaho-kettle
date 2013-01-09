@echo off
rem  eobjects.org DataCleaner
rem  Copyright (C) 2010 eobjects.org
rem  
rem  This copyrighted material is made available to anyone wishing to use, modify,
rem  copy, or redistribute it subject to the terms and conditions of the GNU
rem  Lesser General Public License, as published by the Free Software Foundation.
rem  
rem  This program is distributed in the hope that it will be useful,
rem  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
rem  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
rem  for more details.
rem  
rem  You should have received a copy of the GNU Lesser General Public License
rem  along with this distribution; if not, write to:
rem  Free Software Foundation, Inc.
rem  51 Franklin Street, Fifth Floor
rem  Boston, MA  02110-1301  USA

set DATACLEANER_HOME=%~dp0
cd /d %DATACLEANER_HOME%
echo Using DATACLEANER_HOME: %DATACLEANER_HOME%

call java -Xmx1024m -jar DataCleaner.jar %*