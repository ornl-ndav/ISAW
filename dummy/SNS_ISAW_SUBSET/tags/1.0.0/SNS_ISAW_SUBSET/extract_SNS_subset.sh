#
# This is a SIMPLE MINDED shell script that copies the souce files needed
# by the Display*D classes from an ISAW distribution to a directory
# SNS_SUBSET in the current directory.
#
# NOTE: Change the line below to point to your unpacked ISAW soruce tree
# This will only work with ISAW version 1.8.0 beta 2, or later.
#
ISAW=/home/dennis/WORK/ISAW

#
# Make the base directory structure
#
mkdir SNS_SUBSET
cd    SNS_SUBSET
mkdir gov
mkdir gov/anl
mkdir gov/anl/ipns
mkdir gov/anl/ipns/ViewTools
mkdir gov/anl/ipns/ViewTools/Displays

#
# Copy over the top level Display classes, similar to those being developed by
# Jim
#
cp $ISAW/gov/anl/ipns/ViewTools/Displays/Display.java    gov/anl/ipns/ViewTools/Displays
cp $ISAW/gov/anl/ipns/ViewTools/Displays/Display1D.java  gov/anl/ipns/ViewTools/Displays
cp $ISAW/gov/anl/ipns/ViewTools/Displays/Display2D.java  gov/anl/ipns/ViewTools/Displays
cp $ISAW/gov/anl/ipns/ViewTools/Displays/Display3D.java  gov/anl/ipns/ViewTools/Displays

#
# Copy over the supporting ViewTools
#
mkdir gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/ActiveJPanel.java           gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/AnimationController.java    gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/ActionValue*.java           gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/Color*.java                 gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/FontUtil.java               gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/SplitPaneWithState.java     gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/TextRangeUI.java            gov/anl/ipns/ViewTools/UI
cp $ISAW/gov/anl/ipns/ViewTools/UI/TextValueUI.java            gov/anl/ipns/ViewTools/UI

cp -rf $ISAW/gov/anl/ipns/ViewTools/Components           gov/anl/ipns/ViewTools 
rm  gov/anl/ipns/ViewTools/Components/OneD/DifferenceViewComponent.java

#
# Copy over the lower level panels that the ViewTools are built on
#
cp -rf $ISAW/gov/anl/ipns/ViewTools/Panels               gov/anl/ipns/ViewTools

#
# Remove my old home-made 3D stuff, and first pass at jogl/Opengl,
# since new 3D work uses SSG_Tools
#
rm -rf gov/anl/ipns/ViewTools/Panels/ThreeD
rm -rf gov/anl/ipns/ViewTools/Panels/GL_ThreeD

cp -rf $ISAW/gov/anl/ipns/ViewTools/Layouts              gov/anl/ipns/ViewTools

#
#  Some basic linear algebra, used by some MathTools/Geometry classes
#
mkdir gov/anl/ipns/MathTools
cp    $ISAW/gov/anl/ipns/MathTools/LinearAlgebra.java    gov/anl/ipns/MathTools

#
# Basic 3D Geometry classes used or linked to by the 3D viewer stuff
#
mkdir gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/Tran3D.java         gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/Tran3D_d.java       gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/Vector3D.java       gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/Vector3D_d.java     gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/Position3D.java     gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/Position3D_d.java   gov/anl/ipns/MathTools/Geometry
cp    $ISAW/gov/anl/ipns/MathTools/Geometry/EulerAngles.java    gov/anl/ipns/MathTools/Geometry

#
# Various utilites used in different view components, "should" support hardcopy
# output and save to file for displays
#
mkdir gov/anl/ipns/Util
mkdir gov/anl/ipns/Util/File
cp    $ISAW/gov/anl/ipns/Util/File/IXmlIO.java                    gov/anl/ipns/Util/File
cp    $ISAW/gov/anl/ipns/Util/File/xml_utils.java                 gov/anl/ipns/Util/File
cp    $ISAW/gov/anl/ipns/Util/File/SerializeUtil.java             gov/anl/ipns/Util/File
cp    $ISAW/gov/anl/ipns/Util/File/TextFileReader.java            gov/anl/ipns/Util/File
cp    $ISAW/gov/anl/ipns/Util/File/RobustFileFilter.java          gov/anl/ipns/Util/File

mkdir gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/floatPoint2D.java             gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/Compare_floatPoint2D_X.java   gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/Format.java                   gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/IntList.java                  gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/UniqueIntGenerator.java       gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/ClosedInterval.java           gov/anl/ipns/Util/Numeric
cp    $ISAW/gov/anl/ipns/Util/Numeric/arrayUtil.java                gov/anl/ipns/Util/Numeric

mkdir gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/WindowShower.java              gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/ElapsedTime.java               gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/SharedMessages.java            gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/StringUtil.java                    gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/StatusPane.java                    gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/StatusPane_Base.java               gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/SaveImageActionListener.java       gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/PrintComponentActionListener.java  gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/ComponentPrintable.java            gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/PrintUtilities.java                gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/ColorSelector.java                 gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/DocumentIO.java                    gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/SaveDocToFileListener.java         gov/anl/ipns/Util/Sys
cp    $ISAW/gov/anl/ipns/Util/Sys/ClearDocListener.java              gov/anl/ipns/Util/Sys

#
# Character input filters used by some view controls
#
cp -rf $ISAW/gov/anl/ipns/Util/StringFilter                          gov/anl/ipns/Util

#
# Messaging stuff used by Contour map
#
mkdir gov/anl/ipns/Util/Messaging
cp -rf $ISAW/gov/anl/ipns/Util/Messaging/Property                gov/anl/ipns/Util/Messaging
cp -rf $ISAW/gov/anl/ipns/Util/Messaging/Information             gov/anl/ipns/Util/Messaging

#
# The only thing we need from DataSetTools is the XScale concept
#
mkdir DataSetTools
mkdir DataSetTools/dataset
cp    $ISAW/DataSetTools/dataset/XScale.java                     DataSetTools/dataset/
cp    $ISAW/DataSetTools/dataset/VariableXScale.java             DataSetTools/dataset/
cp    $ISAW/DataSetTools/dataset/UniformXScale.java              DataSetTools/dataset/
cp    $ISAW/DataSetTools/dataset/GeometricProgressionXScale.java DataSetTools/dataset/

#
# Bring over SSG_Tools, but remove the texture map files, demos
# and fonts that will "never" be used for SNS data
#
cp -rf $ISAW/SSG_Tools                                   .
rm -rf SSG_Tools/Data
rm -rf SSG_Tools/Demos
rm     SSG_Tools/Fonts/GothicGermanTriplex.java
rm     SSG_Tools/Fonts/GothicBritishTriplex.java
rm     SSG_Tools/Fonts/GothicItalianTriplex.java
rm     SSG_Tools/Fonts/CyrilicComplex.java
rm     SSG_Tools/Fonts/ItalicComplex.java
rm     SSG_Tools/Fonts/ItalicTriplex.java
rm     SSG_Tools/Fonts/ScriptComplex.java
rm     SSG_Tools/Fonts/ScriptSimplex.java

#
# Bring over the ExcelAdapter for cut/paste from tables
#
mkdir ExtTools
cp    $ISAW/ExtTools/ExcelAdapter.java                   ExtTools

#
# Get rid to the CVS directories, if present
#
rm -rf CVS
rm -rf */CVS
rm -rf */*/CVS
rm -rf */*/*/CVS
rm -rf */*/*/*/CVS
rm -rf */*/*/*/*/CVS
rm -rf */*/*/*/*/*/CVS
rm -rf */*/*/*/*/*/*/CVS
rm -rf */*/*/*/*/*/*/*/CVS

