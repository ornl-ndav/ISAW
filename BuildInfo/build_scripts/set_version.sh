#!/bin/sh
#
#  Shell script to set the version number where it needs to be set.
#

VERSION="1.9.0_02"

sed "s/Unknown_Version/$VERSION/" ISAW/DataSetTools/util/SharedData.java > tmp_SharedData.java

mv tmp_SharedData.java  ISAW/DataSetTools/util/SharedData.java

sed "s/Unknown_Version/$VERSION/"  ISAW/IsawHelp/About.html > tmp_About.html

mv tmp_About.html ISAW/IsawHelp/About.html

