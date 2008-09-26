#           Save NeXus Cache information
#
# Saves the cache of classnames needed to quickly load in NeXus
#   files with a lot of NXdata or fields under an NXentry and/or NxInstrument
# @param filename   The name of a NeXus file with standard template
#@param  CacheFile  The name of the file where the names are cached or the 
#                      default location and name if blank
# @return  "OK" or error string.  The cache file is set up if successful




from gov.anl.ipns.Util.SpecialStrings  import *
from DataSetTools.retriever import NexusRetriever
from DataSetTools.operator.Generic.Save import *

class SaveNexCache ( GenericSave):
    
    
    
   def setDefaultParameters(self):
       self.super__clearParametersVector()
       X=System.getProperty("Data_Directory", None)
       self.addParameter( LoadFilePG("NeXus File name", X ) )
       X = System.getProperty("user.home", None)       
       self.addParameter( SaveFilePG("Cache File name or blank for default", X ) )
                     
       
   def getCommand(self):
       return "SaveNeXusCache"
   
   def getResult(self):
      filename = self.getParameter(0).getValue()
      toFilename = self.getParameter(1).getValue()
      retriever = NexusRetriever(filename)
      retriever.SaveSetUpInfo( toFilename)
      #replace None by the name of the file to store this information

      print "finished"
      return "OK"

   def getDocumentation( self):
      S = StringBuffer("         Create NeXus Cache\n")
      S.append("@overview Saves the cache of classnames needed to quickly load in NeXus ")
      S.append("files with a lot of NXdata or fields under an NXentry and/or NxInstrument\n")
      S.append(" @param filename   The name of a NeXus file with standard template\n")
      S.append("@param  CacheFile  The name of the file where the names are cached or the ") 
      S.append("                      default location and name if blank\n")
      S.append(" @return  \"OK\" or error string.  The cache file is set up if successful\n")
      return S.toString()     
   
       # Constructor
   def __init__( self ):
       Operator.__init__( self, "Create NeXus Cache" )
