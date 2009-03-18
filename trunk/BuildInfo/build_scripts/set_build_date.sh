#!/bin/sh
#
#  Shell script to set the build date to the current date.
#
sed "s/Unknown_Build_Date/`date`/" ISAW/DataSetTools/util/SharedData.java > tmp_SharedData.java

mv tmp_SharedData.java ISAW/DataSetTools/util/SharedData.java

sed "s/Unknown_Build_Date/`date`/" ISAW/IsawHelp/About.html > tmp_About.html

mv tmp_About.html ISAW/IsawHelp/About.html

