#-----------------------------------
#           IntegPeakFit.py
#-----------------------------------

# Program to start the application to integrate peaks in a peak file using fitting peak shapes
# per time slice with a normal approximation 


# R. Mikkelson -- gui construction:  November, 2010

from Operators.TOF_SCD import IntegrateNorm
import math

class IntegPeakFit(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        
    def getResult(self):
        IntegrateNorm.main(None)

    def  getDocumentation( self):
        S =StringBuffer()
        S.append("Starts the application to integrate peaks in a peak file using fitting peak ")

        S.append(" shapes per time slice with a normal approximation\n")
        S.append("NOTE:The Peak file must only have peaks from ONE RUN and the DataSet must ")
        S.append("For deuterated oxalic acid dihydrate, input\n")
        S.append("be from the corresponding run\n")
        return S.toString()

    def getCategoryList( self):
       
        return ["Macros","Single Crystal"]
        
    def __init__(self):
        Operator.__init__(self,"Integrate with Peak Fitting")


