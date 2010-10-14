#Author: P.Peterson, R. Mikkelson
#Script to reduce Powgen data to GSAS and Fullprof data files
#Cycle 2010-A V run 733, Si run 732, 30Hz f-1 HR guide with ghost correction
#Underlying script PG3_to_dspace.py changed to separate instrument configuration to make it cycle independent Sep 2010
#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from DataSetTools.operator.DataSet.Math.DataSet import *
from Command import *
from gov.anl.ipns.Util.SpecialStrings import *
class PG3_to_rawdata(GenericLoad):

    EventFile =0
    ConfigFile = 1
    firstEvent=2
    NEvents=3
    mind=4
    maxd=5
    delta=6
    toTree = 7
    show = 8
    data = 9
    SeqNums = 10
    
    def __init__(self):
        Operator.__init__(self, "PG3_to_rawdata")
        self.instr = "PG3"
        try:
            self.IOBS = IOBS
        except:
            self.IOBS = None

    def getCommand(self):
        return "PG3_to_rawdata"

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/PG3/"
        self.addParameter(StringPG("PG3 run number", 1370))
        self.addParameter(LoadFilePG("Configuration file name",
                                     "/SNS/PG3/2010_2_11A_CAL/"))
        self.addParameter(FloatPG("First event to load", 0.))
        self.addParameter(FloatPG("Number of events to load", 1e12))
        self.addParameter(FloatPG("Min d-spacing", 0.2))
        self.addParameter(FloatPG("Max d-spacing", 5.0))
        self.addParameter(FloatPG("deltaD/D", 2e-4))
        self.addParameter(BooleanPG("Send all data to tree", False))
        self.addParameter(BooleanPG("Show plots", 0))       
        self.addParameter(DataDirPG("Save directory","/SNS/users/"))
        self.addParameter(BooleanPG("Sequential Bank Numbering",1))  
 

    def getParamValue(self,index):
        param = self.getParameter(index)
        if param is None:
            raise RuntimeError("Failed to get parameter with index %d" % index)
        else:
            return param.getValue()

    def getRuns(self):
        runsStr = self.getParamValue(0)
        print runsStr
        from gov.anl.ipns.Util.Numeric import IntList
        runs = IntList.ToArray(runsStr)
        print runs
        return runs

    def toGSASFilename(self, runnumber):
        filename = str(runnumber) + ".gsa"
        import os
        filename = os.path.join(self.outputDir, filename)
        return filename
    def toFullprofFilename(self, runnumber):
        filename = str(runnumber) + ".dat"
        import os
        filename = os.path.join(self.outputDir, filename)
        return filename

    def processRun(self, runnumber):
        print runnumber
        self.args[0] = runnumber
        SampleDS = ScriptUtil.ExecuteCommand("PG3_to_dspace",self.args)

        if isinstance(SampleDS, ErrorString):
           print SampleDS
           return SampleDS

        SampleDS.clampToZero()

        GsasFileName = self.toGSASFilename(runnumber)

        if GsasFileName is not None:
            useSeqNumbering = self.getParamValue(self.SeqNums)
            X = ScriptUtil.ExecuteCommand("Save3ColGSAS",[None,SampleDS, GsasFileName, useSeqNumbering])
            if isinstance(X, ErrorString):
               return X

    def getResult(self):
        # get the configuration file and load it
        execfile(self.getParamValue(self.ConfigFile))
        config = getConfig()

        runs = self.getRuns()
        self.args=[None, self.getParamValue(self.ConfigFile),
                   self.getParamValue(self.firstEvent),
                   self.getParamValue(self.NEvents),
                   self.getParamValue(self.mind),
                   self.getParamValue(self.maxd),
                   self.getParamValue(self.delta),
                   0.,
                   self.getParamValue(self.toTree),
                   self.getParamValue(self.show)]
       

        self.outputDir = self.getParamValue(self.data)

        for runnumber in runs:
           self.processRun(runnumber)

        return None

