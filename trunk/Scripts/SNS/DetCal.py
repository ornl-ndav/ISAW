from EventTools.EventList import *
class DetCal(GenericTOF_SCD):

   def setDefaultParameters(self):    
      self.super__clearParametersVector()
      self.addParameter(LoadFilePG("Name of NeXus file",System.getProperty("Data_Directory")))
      self.addParameter(SaveFilePG("Name of DetCal file",System.getProperty("Data_Directory")))


   def getResult(self):
      NexFile = self.getParameter(0).value
      DetFile = self.getParameter(1).value
      DumpGrids.PrintDetCalFile(NexFile,DetFile)
      return "Success"

   def  getDocumentation( self):
       S =StringBuffer()
       S.append(" Creates and saves a DetCal file from a NeXus file..\n")
       S.append("@param NexusFileName The Nexus File name.")
       S.append("@param  DetCalFileName  The Name of the DetCal file.\n")
       S.append("@return  null or an ErrorString")
    
       return S.toString()

   def getCategoryList( self):
       
       return ["Macros","DataSet","Tweak"]
        
   def __init__(self):
       Operator.__init__(self,"Create DetCal File")
