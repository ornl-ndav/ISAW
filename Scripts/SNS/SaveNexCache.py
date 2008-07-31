#           Save NeXus Cache information
#
# Saves the cache of classnames needed to quickly load in NeXus
#   files with a lot of NXdata or fields under an NXentry

#NOTE: replace filename ="C:/ISAW/SampleRuns/SNS/ARCS/ARCS_65.nxs" by an
#    actual filename on your system

from IPNSSrc import blind
from Operators.TOF_SCD import IndexJ
from DataSetTools.retriever import NexusRetriever

filename = "C:/ISAW/SampleRuns/SNS/ARCS/ARCS_65.nxs"
retriever = NexusRetriever(filename)
retriever.SaveSetUpInfo( None)
#replace None by the name of the file to store this information

print "finished"