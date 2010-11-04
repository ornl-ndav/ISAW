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
class PG3_to_ndata(GenericLoad):

    EventFile =0
    BackFile  =1
    VFile =2
    ConfigFile = 3
    firstEvent=4
    NEvents=5
    mind=6
    maxd=7
    delta=8
    pchargeScalar=9
    BadPeakWidth = 10
    BadIntrvlReplace =11
    BadNumChanAv  = 12
    FilterCutOff = 13
    FilterOrder = 14
    toTree = 15
    show = 16
    data = 17
    filetypes = 18
    SeqNums = 19
    
    def __init__(self):
        Operator.__init__(self, "PG3_to_ndata")
        self.instr = "PG3"
        try:
            self.IOBS = IOBS
        except:
            self.IOBS = None

    def getCommand(self):
        return "PG3_to_ndata"

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/PG3/"
        self.addParameter(StringPG("PG3 run number", 1695))
        self.addParameter(IntegerPG("PG3 background run number",-1))
        self.addParameter(IntegerPG("PG3 vanadium run number",1425))
        self.addParameter(LoadFilePG("Configuration file name",
                                     "/SNS/PG3/2010_2_11A_CAL/"))
        self.addParameter(FloatPG("First event to load", 0.))
        self.addParameter(FloatPG("Number of events to load", 1e12))
        self.addParameter(FloatPG("Min d-spacing", 0.2))
        self.addParameter(FloatPG("Max d-spacing", 5.0))
        self.addParameter(FloatPG("deltaD/D", 4e-4))
        self.addParameter(FloatPG("normalize to pcharge", 1e13))
        self.addParameter(FloatPG("Estimated Peak Width( delta_d/d)",.0050))
        self.addParameter(FloatPG("Interval to Replace(Times Peak Width)",1.9))
        self.addParameter(IntegerPG("Number of Channels to Average", 10))
        self.addParameter(FloatPG("Filter Cut off",.02))
        self.addParameter(IntegerPG("Filter order", 2))
        self.addParameter(BooleanPG("Send all data to tree", False))
        self.addParameter(BooleanPG("Show plots", 0))       
        self.addParameter(DataDirPG("Save directory","/SNS/users/"))
        choices = ChoiceListPG("File types", "gsas and fullprof")
        choices.addItem("gsas")
        choices.addItem("fullprof")
        self.addParameter(choices)
        self.addParameter(BooleanPG("Sequential Bank Numbering",1))  
 
    def send(self, ds, showPlots, sendData):
        if ds is None:
            return
        from Command import ScriptUtil
        if showPlots:
            ScriptUtil.display(ds)
        if sendData and self.IOBS is not None:
            ScriptUtil.send(ds, self.IOBS)

    def getParamValue(self,index):
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

        if self.BackgroundDS is not None:
           op = DataSetSubtract(SampleDS, self.BackgroundDS,0)
           op.getResult()

       
        SampleDS.clampToZero()

        GsasFileName = self.toGSASFilename(runnumber)

        save_as = self.getParamValue(self.filetypes)

        if self.VanadiumDS is not None:
            op = SampleDS.getOperator("Divide by a DataSet")
           
            op.getParameter(0).setValue(self.VanadiumDS)
            X = op.getResult()
            if isinstance(X, ErrorString):
               return X

        useSeqNumbering = self.getParamValue(self.SeqNums)

        if "fullprof" in save_as:
            filename = str(runnumber)+".dat"
            import os
            filename = os.path.join(self.outputDir, filename)
            X=ScriptUtil.ExecuteCommand("SaveFullProf",[SampleDS,filename,useSeqNumbering])
            if isinstance(X, ErrorString):
               return X
            self.send(SampleDS, self.getParamValue(self.show),
                      self.getParamValue(self.toTree))

        if GsasFileName is not None and "gsas" in save_as:
            op = SampleDS.getOperator("Multiply sample values yi, by delta_xi")
            if op is None:
                op = SampleDS.getOperator("MultiplyByDeltaX")
            op.getParameter(0).setValue(True)
            SampleDS_gsas = op.getResult()
            if isinstance(X, ErrorString):
                return X
            X = ScriptUtil.ExecuteCommand("Save3ColGSAS",[None,SampleDS_gsas, GsasFileName, useSeqNumbering])
            if isinstance(X, ErrorString):
               return X
            self.send(SampleDS_gsas, self.getParamValue(self.show),
                      self.getParamValue(self.toTree))

        if self.getParamValue(self.toTree):
            return SampleDS
        else:
            return None

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
                   self.getParamValue(self.pchargeScalar),
                   self.getParamValue(self.toTree),
                   self.getParamValue(self.show)]
       
        BackgroundFile = self.getParamValue(1)
        if BackgroundFile is None or BackgroundFile <=0:
           BackgroundFile = None

        if BackgroundFile is not None:
           self.args[0]= BackgroundFile
           self.BackgroundDS =  ScriptUtil.ExecuteCommand("PG3_to_dspace",
                                                          self.args)
        else:
           self.BackgroundDS = None

        VanadiumFile = self.getParamValue(self.VFile)
        if VanadiumFile is None or VanadiumFile <=0:
           VanadiumFile = None

        if VanadiumFile is not None:

           vanargs = [VanadiumFile,
                      self.getParamValue(self.ConfigFile),
                      self.getParamValue(self.firstEvent),
                      self.getParamValue(self.NEvents),
                      self.getParamValue(self.mind),
                      self.getParamValue(self.maxd),
                      self.getParamValue(self.delta),
                      self.getParamValue(self.pchargeScalar),
                      self.getParamValue(self.BadPeakWidth),
                      self.getParamValue(self.BadIntrvlReplace),
                      self.getParamValue(self.BadNumChanAv),
                      self.getParamValue(self.FilterCutOff),
                      self.getParamValue(self.FilterOrder),
                      True, False, False]
           X =  ScriptUtil.ExecuteCommand("VanadiumLoad", vanargs)
           if isinstance(X, ErrorString):
              return X
           self.VanadiumDS = X
          						  
        else:
           self.VanadiumDS = None

        self.outputDir = self.getParamValue(self.data)

        for runnumber in runs:
            X = self.processRun(runnumber)
            if isinstance(X, ErrorString):
                return X

        return None

