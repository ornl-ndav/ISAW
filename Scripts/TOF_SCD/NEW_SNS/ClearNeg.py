
from DataSetTools.dataset.DataSet import *
from DataSetTools.operator import *

class ClearNeg(GenericTOF_SCD):
    def setDefaultParameters(self):
        
      self.super__clearParametersVector()
      self.addParameter( DataSetPG("Data Set", EMPTY_DATA_SET))
      
    def getResult(self):
      DS =self.getParameter(0).getValue()
      DS.clampToZero()
      return "success"


    def getCategoryList( self):
       
      return Operator.DATA_SET_TWEAK_MACROS
    
    def getDocumentation( self):
      S = StringBuffer()
      S.append("@overview    ") 
    
      S.append("@param   ")
      S.append("The DataSet whose negative entries are to be cleared")
      
      S.append("@return  \"Success\" or an error string")
      return S.toString()

    def  __init__(self):
      Operator.__init__(self,"Clear Negative Entries")

