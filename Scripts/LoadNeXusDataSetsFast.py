from DataSetTools.retriever  import *
from gov.anl.ipns.Util.SpecialStrings  import *
from DataSetTools.operator.Generic.Load import *

class LoadNeXusDataSetsFast(GenericLoad):


   def setDefaultParameters(self):
      self.super__clearParametersVector()
      self.addParameter(LoadFilePG("NeXus data File",None))
      self.addParameter(IntArrayPG("DataSet nums to load(blank for all)",""))
      self.addParameter(IntArrayPG("GroupIDs to load(blank for all)",""))
      self.addParameter(BooleanPG("Use Default Cache",0))
      self.addParameter(LoadFilePG("Cache filename",System.getProperty("user.home")+"/ISAW"))
      

   def getCommand( self):
      return "LoadNeXusDataSetsFast"

   def getResult( self):
      filename = self.getParameter(0).getValue()
      dsList  = self.getParameter(1).getValue()
      groupList = self.getParameter(2).getValue()
      useDefaultCache = self.getParameter(3).getValue()
      Cache =self.getParameter(4).getValue()
     
      return NexusRetriever.LoadNeXusDataSetsFast( filename,IntListString(dsList) ,IntListString(groupList), useDefaultCache, Cache)

  
   def createSetUpInf( self ,filename, Cache): 
      retriever = NexusRetriever( filename)
      retriever.SaveSetUpInfo(Cache)

   def getDocumentation( self):
      Res = StringBuffer(2000)
      Res.append("     Load Nexus files w. caching\n")
      Res.append(" @overview  Loads specified data sets and groups from a NeXus file. It has an option to \n")
      Res.append("     use caching of some name attributes for NeXus Classes  in these files \n")
      Res.append("@param filename  the name of the NeXus file")
      Res.append("@param dsList  the list of data sets numbers to load, eg.\"1:55,9:20\". Empty string for ALL") 
      Res.append("@param  groupList the list of group ID's to keep(\"1:2000,:3000:5000\"). Empty string for ALL")
      Res.append("@param useDefaultCache  true to use default Cache in {user.home}/ISAW. The extension is.startup\b")
      Res.append("       and the name corresponds to the first 3 characters(uppercase) of the filename\n");
      Res.append("@param CacheFileName  The name of the cache file if it is not the default. Blank or \n")
      Res.append("   non existent files will not incorporate any of this type of caching\n")
      Res.append("@return an array of data sets as specified.")
      return Res.toString()
     # Constructor
   def __init__( self ):
       Operator.__init__( self, "Fast Load NeXus" )

 



