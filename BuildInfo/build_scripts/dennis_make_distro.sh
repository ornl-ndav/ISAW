#!/bin/sh

#
# build a distribution of ISAW, including the gov, IPNS, ISIS  SSG_Tools 
# source trees, along with jnexus
#
# CHANGES:
#
# 03/27/2008 -DJM
# from the packup directory.
# changed paths to match my office computer
# added EventTools package
#
# 02/14/2008 -DJM
# added -target 1.5 to support use with java 5 at ORNL
#
# 07/28/2004 -DJM
# added devTools 
#
# 07/28/2004 -DJM
# added ISIS 
#
# 03/25/2003 -PFP
# changed call to build documentation to use a perl script
#
# 03/11/2003 -PFP
# call 'recurse' to change file permissions
#
# 02/26/2003 -PFP
# added call to cp_files.sh
#
# 12/09/2002 -PFP
# added jhall.jar (JavaHelp) to the classpath
#

JAVA_HOME='/usr/local/java'
ISAW_MAKE=$HOME/IsawMake
PACKUP=$HOME/IsawMake/packup/ISAW
RECURSE=$HOME'/IsawMake/build_scripts/recurse -q'

#JAVAC='/usr/local/java/bin/javac '
JAVAC='/usr/local/java/bin/javac -target 1.5 '

CLASSPATH=$JAVA_HOME:$JAVA_HOME/jre/lib/rt.jar:$ISAW_MAKE/sgt_v20:$ISAW_MAKE/ISAW/:$ISAW_MAKE:$PACKUP/jnexus.jar:$PACKUP/sdds.jar:$PACKUP/jhall.jar:$PACKUP/jython.jar:$PACKUP/jogl.jar:$PACKUP/gluegen-rt.jar:$PACKUP/servlet.jar:$PACKUP/IPNS.jar:.


#
# !!!!! BEFORE USING THIS SCRIPT, FIRST GET THE SOURCES FROM CVS BY:
#    1. edit cvs_ISAW.config   to specify release number and build date 
#    2. use 'cvs_ISAW'         to actually check out the sources 
#

#
# Clean up the file access flags
#
$RECURSE 'chmod -x' -D./ISAW
$RECURSE 'chmod -x' -D./gov
$RECURSE 'chmod -x' -D./IPNS
$RECURSE 'chmod -x' -D./ISIS
$RECURSE 'chmod -x' -D./SSG_Tools

#
# These should either be removed from CVS, or updated if they are to be
# included  (7/4/2001)
# rm ISAW/Makefile
# rm ISAW/make_docs

#
# Set the build date and version number, NOTE: the set_version.sh script 
# must be manually edited to change the version number.
#
printf "setting build date..."
set_build_date.sh
set_version.sh
printf "done\n"

#
# Compile but don't delete the src
#
printf "building..."
$JAVAC -classpath $CLASSPATH  ./gov/anl/ipns/*.java
$JAVAC -classpath $CLASSPATH  ./gov/anl/ipns/*/*.java
$JAVAC -classpath $CLASSPATH  ./gov/anl/ipns/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./gov/anl/ipns/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./gov/anl/ipns/*/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./gov/anl/ipns/*/*/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISIS/*.java
$JAVAC -classpath $CLASSPATH  ./ISIS/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISIS/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./SSG_Tools/*.java
$JAVAC -classpath $CLASSPATH  ./SSG_Tools/*/*.java
$JAVAC -classpath $CLASSPATH  ./SSG_Tools/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./SSG_Tools/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./SSG_Tools/*/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./IPNS/*.java
$JAVAC -classpath $CLASSPATH  ./IPNS/*/*.java
$JAVAC -classpath $CLASSPATH  ./IPNS/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./IPNS/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/EventTools/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/EventTools/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/EventTools/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/EventTools/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/MessageTools/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/MessageTools/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/IsawGUI/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/IsawGUI/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/NetComm/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/NetComm/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/Operators/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Operators/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Operators/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Operators/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/DataSetTools/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/DataSetTools/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/DataSetTools/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/DataSetTools/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/DataSetTools/*/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/DataSetTools/*/*/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/Wizard/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Wizard/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Wizard/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/FileIO/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/FileIO/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*/*/*/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/devTools/*/*/*/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/IsawHelp/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/IsawHelp/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/IsawHelp/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/IsawHelp/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/Command/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Command/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Command/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Command/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/ExtTools/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/ExtTools/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/ExtTools/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/IPNSSrc/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/IPNSSrc/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/Jama/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Jama/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/Jama/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/NexIO/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/NexIO/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/NexIO/*/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/NexIO/*/*/*/*.java

$JAVAC -classpath $CLASSPATH  ./ISAW/jnt/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/jnt/*/*.java
$JAVAC -classpath $CLASSPATH  ./ISAW/jnt/*/*/*.java
printf "done\n"

#
# remove files that don't belong in the class jar file (7/4/2001 D.M.)
#
printf "removing unneeded files..." 
rm -f ISAW/*.txt                        
rm -f ISAW/make_docs
printf "done\n"

#
# move the InstrumentInfo directory to the packup directory, so it
# stays out of the jar file.
#
cp -rf ISAW/InstrumentInfo  packup/ISAW

#
#pack up class files, now putting the Operators directory in a separate file
#
printf "packing up class files..."
cd ISAW

rm -f Operators.jar
rm -rf ../Operators
mv    Operators ..

rm -rf ../Scripts
mv Scripts ..

$JAVA_HOME/bin/jar cf Isaw.jar *
mv Isaw.jar ..

cp -rf ../Operators .
cp -rf ../Scripts .
$JAVA_HOME/bin/jar cf Operators.jar Operators Scripts
mv  Operators.jar ..

cd ..
$JAVA_HOME/bin/jar cf gov.jar          gov 
$JAVA_HOME/bin/jar cf IPNS.jar         IPNS
$JAVA_HOME/bin/jar cf ISIS.jar         ISIS 
$JAVA_HOME/bin/jar cf SSG_Tools.jar    SSG_Tools 
printf "done\n"

#
# build the documentation
#
printf "producing documentation..."
export CLASSPATH=$CLASSPATH
cp -rf gov          ISAW/
cp -rf IPNS         ISAW/
cp -rf ISIS         ISAW/
cp -rf SSG_Tools    ISAW/

printf "removing $ISAW_MAKE/ISAW/docs..."
rm -rf $ISAW_MAKE/ISAW/docs

printf "creating $ISAW_MAKE/ISAW/docs..."
mkdir $ISAW_MAKE/ISAW/docs
mkdir $ISAW_MAKE/ISAW/docs/html

printf "calling build_javadoc..."
build_javadoc -c ISAW

printf "copying files..."
cd ISAW
cp -rf ../gov          .
cp -rf ../IPNS         .
cp -rf ../ISIS         .
cp -rf ../SSG_Tools    .

$JAVA_HOME/bin/jar cf docs.jar docs
mv docs.jar ..
cd ..

cp_files.sh

export CLASSPATH=
printf "done\n"
