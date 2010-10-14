#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from DataSetTools.operator.DataSet.Math.DataSet import *
from Command import *
from gov.anl.ipns.Util.SpecialStrings import *
class PG3_to_gsas(GenericLoad):

    EventFile =0
    BackFile  =1
    DetCal    =2
    BankFile =3
    MapFile =4
    dMapFile =5
    firstEvent=6
    NEvents=7
    mind=8
    maxd=9
    delta=10
    pchargeScalar=11
    ghosFile=12
    toTree=13
    show=14
    Gsas =15
    SeqNums =16
    def __init__(self):
        Operator.__init__(self, "PG3_to_gsas")
        self.instr = "PG3"
        try:
            self.IOBS = IOBS
        except:
            self.IOBS = None

    def getCommand(self):
        return "PG3_to_gsas"

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/PG3/"
        self.addParameter(StringPG("PG3 run number", 666))

        self.addParameter(IntegerPG("PG3 background run number",-1))
      
        self.addParameter(LoadFilePG("DelCal file name",
                                     "/SNS/users/pf9/NEW_PG3.DetCal"))
        self.addParameter(LoadFilePG("TS Banking file name",
                                     PG3ROOT + "2010_2_11_CAL/calibrations/PG3_bank_2010_04_22.xml"))
        self.addParameter(LoadFilePG("TS Mapping file name",
                                     PG3ROOT + "2010_2_11A_CAL/calibrations/PG3_TS_2009_04_17.dat"))
        self.addParameter(LoadFilePG("d-space mapping file",
                                     PG3ROOT + "2010_2_11_CAL/PG3_D664_dspacemap_2010_03_17.dat"))
        self.addParameter(FloatPG("First event to load", 0.))
        self.addParameter(FloatPG("Number of events to load", 1e12))
        self.addParameter(FloatPG("Min d-spacing", 0.2))
        self.addParameter(FloatPG("Max d-spacing", 5.0))
        self.addParameter(FloatPG("deltaD/D", 2e-4))
        self.addParameter(FloatPG("divide pcharge by", 1e13))
        self.addParameter(LoadFilePG("Ghost map file", 
                                     PG3ROOT + "2010_2_11_CAL/PG3_D664_ghostmap_2010_03_17.dat"))
        self.addParameter(BooleanPG("Send all data to tree", False))
        self.addParameter(BooleanPG("Show plots", False))
        self.addParameter(SaveFilePG("Save directory","/SNS/users/3ah/GSAS/"))
        self.addParameter(BooleanPG("Sequential Bank Numbering", True))

    def getParamValue(self, index):
        return self.getParameter(index).getValue()

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

    def processRun(self, runnumber):
        print runnumber
        self.args[0] = runnumber
        SampleDS = ScriptUtil.ExecuteCommand("PG3_to_dspace",self.args)

        if self.BackgroundDS is not None:
           op = DataSetSubtract(SampleDS, self.BackgroundDS,0)
           op.getResult()

        SampleDS.clampToZero()

        GsasFileName = self.toGSASFilename(runnumber)

        if GsasFileName is not None:
            useSeqNumbering = self.getParameter(self.SeqNums).getValue()
            X = ScriptUtil.ExecuteCommand("Save3ColGSAS",[None,SampleDS, GsasFileName, useSeqNumbering])
            if isinstance(X, ErrorString):
                return X

    def getResult(self):
        runs = self.getRuns()
        self.args=[None,self.getParamValue(2),self.getParamValue(3),
                   self.getParamValue(4),self.getParamValue(5),
                   self.getParamValue(6),
                   self.getParamValue(7),self.getParamValue(8),
                   self.getParamValue(9),
                   self.getParamValue(10),self.getParamValue(11),
                   self.getParamValue(12),self.getParamValue(13),
                   self.getParamValue(14)]

        BackgroundFile = self.getParamValue(1)
        if BackgroundFile is None or BackgroundFile <=0:
           BackgroundFile = None

        if BackgroundFile is not None:
           self.args[0]= BackgroundFile
           self.BackgroundDS =  ScriptUtil.ExecuteCommand("PG3_to_dspace",
                                                          self.args)
        else:
           self.BackgroundDS = None

        self.outputDir = self.getParamValue(self.Gsas)

        for runnumber in runs:
            self.processRun(runnumber)

        return None

