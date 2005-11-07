#*******************************************************************************
# Copyright (c) 2000, 2005 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     IBM Corporation - initial API and implementation
#     Kevin Cornell (Rational Software Corporation)
#     Silenio Quarti (IBM)
#     Sam Robb (TimeSys Corporation)
#*******************************************************************************
 
# Makefile for creating the eclipse launcher program.

# This makefile expects the following environment variables set:
#
# PROGRAM_OUTPUT  - the filename of the output executable
# DEFAULT_OS      - the default value of the "-os" switch
# DEFAULT_OS_ARCH - the default value of the "-arch" switch
# DEFAULT_WS      - the default value of the "-ws" switch

# Allow for cross-compiling under linux
OSTYPE	?= $(shell if uname -s | grep -iq cygwin ; then echo cygwin; else echo linux; fi)
ifeq ($(OSTYPE),cygwin)
CCVER   = i686
CC      = i686-pc-cygwin-gcc
RC      = windres
else
CCVER   = i586
CC      = $(shell which i586-pc-cygwin-gcc)
TDIR    = $(dir $(shell test -L $(CC) && readlink $(CC) || echo $(CC)))
RC      = $(TDIR)/i586-pc-cygwin-windres
SYSINC  = -isystem $(TDIR)/../include/mingw
endif

ifeq ($(CC),)
$(error Unable to find $(CCVER)-pc-cygwin-gcc)
endif

# Define the object modules to be compiled and flags.
OBJS	= eclipse.o eclipseWin.o eclipseShm.o eclipseConfig.o eclipseUtil.o \
	  aeclipse.o aeclipseWin.o aeclipseShm.o aeclipseConfig.o aeclipseUtil.o
LIBS	= -lkernel32 -luser32 -lgdi32 -lcomctl32 -lmsvcrt
LDFLAGS = -mwindows -mno-cygwin
RES	= eclipse.res
EXEC	= $(PROGRAM_OUTPUT)
DEBUG	= $(CDEBUG)
CFLAGS	= -O -s -Wall \
	  -I. $(SYSINC) \
	  -D_WIN32 \
	  -DWIN32_LEAN_AND_MEAN \
	  -mno-cygwin
ACFLAGS = -I.. -DDEFAULT_OS="\"$(DEFAULT_OS)\"" \
	  -DDEFAULT_OS_ARCH="\"$(DEFAULT_OS_ARCH)\"" \
	  -DDEFAULT_WS="\"$(DEFAULT_WS)\"" \
	  $(DEBUG) $(CFLAGS)
WCFLAGS	= -DUNICODE $(ACFLAGS)

all: $(EXEC)

eclipse.o: ../eclipseOS.h ../eclipseUnicode.h ../eclipse.c ../eclipseShm.h
	$(CC) $(DEBUG) $(WCFLAGS) -c -o $@ ../eclipse.c

eclipseUtil.o: ../eclipseUtil.h ../eclipseUnicode.h ../eclipseUtil.c
	$(CC) $(DEBUG) $(WCFLAGS) -c -o $@ ../eclipseUtil.c

eclipseShm.o: ../eclipseShm.h ../eclipseUnicode.h ../eclipseShm.c
	$(CC) $(DEBUG) $(WCFLAGS) -c -o $@ ../eclipseShm.c

eclipseConfig.o: ../eclipseConfig.h ../eclipseUnicode.h ../eclipseConfig.c
	$(CC) $(DEBUG) $(WCFLAGS) -c -o $@ ../eclipseConfig.c
	
eclipseWin.o: ../eclipseOS.h ../eclipseUnicode.h eclipseWin.c
	$(CC) $(DEBUG) $(WCFLAGS) -c -o $@ eclipseWin.c

aeclipse.o: ../eclipseOS.h ../eclipseUnicode.h ../eclipse.c ../eclipseShm.h
	$(CC) $(DEBUG) $(ACFLAGS) -c -o $@ ../eclipse.c

aeclipseUtil.o: ../eclipseUtil.h ../eclipseUnicode.h ../eclipseUtil.c
	$(CC) $(DEBUG) $(ACFLAGS) -c -o $@ ../eclipseUtil.c

aeclipseShm.o: ../eclipseShm.h ../eclipseUnicode.h ../eclipseShm.c
	$(CC) $(DEBUG) $(ACFLAGS) -c -o $@ ../eclipseShm.c

aeclipseConfig.o: ../eclipseConfig.h ../eclipseUnicode.h ../eclipseConfig.c
	$(CC) $(DEBUG) $(ACFLAGS) -c -o $@ ../eclipseConfig.c
	
aeclipseWin.o: ../eclipseOS.h ../eclipseUnicode.h eclipseWin.c
	$(CC) $(DEBUG) $(ACFLAGS) -c -o $@ eclipseWin.c

$(RES): eclipse.rc
	$(RC) --output-format=coff --include-dir=.. -o $@ $<

$(EXEC): $(OBJS) $(RES)
	$(CC) $(LDFLAGS) -o $(EXEC) $(OBJS) $(RES) $(LIBS)

install: all
	cp $(EXEC) $(OUTPUT_DIR)
	rm -f $(EXEC) $(OBJS) $(RES)

clean:
	$(RM) $(EXEC) $(OBJS) $(RES)