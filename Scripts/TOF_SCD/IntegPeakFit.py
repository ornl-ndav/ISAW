#-----------------------------------
#           IntegPeakFit.py
#-----------------------------------

# Program to start the application to integrate peaks in a peak file using fitting peak shapes
# per time slice with a normal approximation 


# R. Mikkelson -- Nov 2010

from Operators.TOF_SCD import IntegrateNorm

import math

class IntegPeakFit(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        
        path= System.getProperty("ISAW_HOME","")
        self.addParameter( LoadFilePG("Peaks File Name",path))
        self.addParameter( LoadFilePG("NeXus File Name", path))
        self.addParameter( IntegerPG("Number of Bad Edge Pixels",10))
        
        self.addParameter( FloatPG("Max cell length(real)",12))
    def getResult(self):
        PeakFile = self.getParameter(0).getStringValue()
        NexFile  = self.getParameter(1).getStringValue()
        BadEdges =self.getParameter(2).getintValue()
        MaxLength = self.getParameter(3).getfloatValue()
        
        IntegrateNorm.main([PeakFile,NexFile,str(BadEdges),str(MaxLength)])

    def  getDocumentation( self):
        S =StringBuffer()
        S.append("Starts the application to integrate peaks in a peak file using  fitting peak ")

        S.append(" shapes per time slice with a bivariate normal approximation\n")
        S.append("NOTE:The Peak file can have peaks from several runs but the DataSet must ")
       
        S.append("be from one of the corresponding runs. Only that run will be integrated\n")
        S.append("@param PeakFileName  ")
        S.append("@param NeXusFileName ")
        S.append("@param BadEdges  The number of bad edges on every detector")
        S.append("@param MaxSideLength The maximum length of the unit cell(real)")
        
        return S.toString()

    def getCategoryList( self):
       
        return ["Macros","Single Crystal"]
        
    def __init__(self):
        Operator.__init__(self,"Integrate with Peak Fitting")


