#
#  This is a simple example jython script that uses the basic
#  ISAW classes to load and display one DataSet from an IPNS
#  runfile. 
#
#  Import packages for data retriever and view manager
#
from DataSetTools.viewer import *
from DataSetTools.retriever import *
#
#  Specify the file to load, construct a retriever for the
#  data and load one DataSet.  Current IPNS runfiles have
#  three DataSets, numbered 0, 1 and 2.  DataSet 0 is the
#  monitor data.  DataSet 1 is the pulse height data.
#  DataSet 2 has the sample histograms.  NOTE: A NeXus
#  file can be loaded similarly using the NexusRetriever.
#
filename="/usr2/SCD_TEST/SCD08336.RUN"
retriever=RunfileRetriever(filename)
ds=retriever.getDataSet(2)
#
#  Create a view manager for the DataSet, specifying the
#  type for the initial view to be an "Image View"
#
viewmanager=ViewManager(ds,"Image View")
