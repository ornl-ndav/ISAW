#
# generic Makefile for isaw
#
# $Log$
# Revision 1.6  2000/07/10 15:42:34  neffk
# selection/deselection can be done by double-clicking on the items in the
# lists on the 'Select' tab.
#
# Revision 1.5  2000/07/07 21:57:13  neffk
# changed the aspect ratio of the graph to 1:2 (vertical:horizontal)
#
# Revision 1.4  2000/07/06 19:55:55  neffk
# added progress file
#
# Revision 1.3  2000/07/06 18:01:12  neffk
# added Makefile
#
# Revision 1.3  2000/06/22 22:29:48  neffk
# *** empty log message ***
#


CONTAINERS = components/containers
UTIL = util


silent:
	make -s list


#
# list options
#
list:
	echo "--------------------------------------------------"
	echo "all           - builds viewer (user)"
	echo "run           - runs viewer in standalone (user)"
	echo "run-all       - runs IsawGUI (user)"
	echo "docs          - creates documentation (user)"
	echo "tags-all      - creates tags in the source tree (developer only)"
	echo "--------------------------------------------------"
	echo ""


#
# build application
#
#all: $(CLASS)
#	javac SelectedGraphView.java
all:
	javac -g *.java
	cd components/containers; javac -g *.java;  cd ../..


#
# run application
#
run:
	java OverplotView.SelectedGraphView

run-all:
	java IsawGUI.Isaw


#
# create documentation
#
DOC = /IPNShome/neffk/java/dev/doc/OverplotView

docs:
	rm -R $(DOC)/*; javadoc -d $(DOC) \
                          OverplotView \
                          OverplotView.components.containers \
                          OverplotView.util





#
# produces ctags 
#
tags-all:
	ctags *.java
	cd components/containers;  ctags *.java;  cd ../..
	cd util;  ctags *.java;  cd ..


clean: 
	rm ./*.class;  rm $(CONTAINERS)/*.class;   rm $(UTIL)/*.class


