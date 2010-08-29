

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from DataSetTools.operator.DataSet.Math.DataSet import *
from Command import *
from gov.anl.ipns.Util.SpecialStrings import *
from SNSlibs import *
class SNAP_to_data(GenericLoad):

    EventFile =0
    BackFile  =1
    VFile =2
    EmptyFile = 3
    Rotate =4
    DetCal    =5
    BankFile =6
    MapFile =7
    firstEvent=8
    NEvents=9
    mind=10
    maxd=11
    delta=12
    pchargeScalar=13
    BadPeakds=14
    BadPeakWidth = 15
    BadIntrvlReplace =16
    BadNumChanAv  =17
    FilterCutOff = 18
    FilterOrder  =19
    toTree=20
    show=21
    data =22
    SeqNums =23
    
    def __init__(self):
        Operator.__init__(self, "SNAP_to_data")
        self.instr = "SNAP"
        try:
            self.IOBS = IOBS
        except:
            self.IOBS = None

    def getCommand(self):
        return "SNAP_to_data"

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/SNAP/"
        self.addParameter(StringPG("SNAP run number", 732))
        self.addParameter(IntegerPG("SNAP background run number",-1))
        self.addParameter(IntegerPG("SNAP vanadium run number",733))
        self.addParameter(IntegerPG("SNAP Empty Run",-1))
        self.addParameter(BooleanPG("Rotate Detectors",1))
      
        self.addParameter(LoadFilePG("DelCal file name(blank for default)",
                                    None))
        self.addParameter(LoadFilePG("TS Banking file name(Blank for default)",
                                     ""))
        self.addParameter(LoadFilePG("TS Mapping file name(blank=default)",""))

        self.addParameter(FloatPG("First event to load", 0.))
        self.addParameter(FloatPG("Number of events to load", 1e12))
        self.addParameter(FloatPG("Min d-spacing", 0.2))
        self.addParameter(FloatPG("Max d-spacing", 5.0))
        self.addParameter(FloatPG("deltaD/D", 2e-4))
        self.addParameter(FloatPG("divide pcharge by", 1e13))

        self.addParameter( LoadFilePG("File Listing d Values of Peaks to Remmove","/SNS/software/ISAW/Databases/VanadiumPeaks.dat"))
        self.addParameter( FloatPG("Estimated Peak Width( delta_d/d)",.0050))
        self.addParameter( FloatPG("Interval to Replace(Times Peak Width)",1.9))
        self.addParameter( IntegerPG("Number of Channels to Average", 10))
        self.addParameter( FloatPG("Filter Cut off",.02))
        self.addParameter( IntegerPG("Filter order", 2))
        self.addParameter(BooleanPG("Send all data to tree", False))
        self.addParameter(BooleanPG("Show plots", 0))       
        self.addParameter(DataDirPG("Save directory","/SNS/users/ehx/GSAS/"))

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

    def processRun(self, sendData,showData,IOBS,instr,runnumber,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale):

        print runnumber

        SampleDS =  EventD_space2GSAS(sendData,showData,IOBS,instr,runnumber,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale)

       
        SampleDS.setTitle("Sample"+str(runnumber))
        self.send(SampleDS.clone(), 0, 1)

        import os
        filename = os.path.join(self.outputDir, "Sample"+str(runnumber)+".isd")
        ScriptUtil.save( filename,SampleDS)

        if self.BackgroundDS is not None:
           op = DataSetSubtract(SampleDS, self.BackgroundDS,0)
           op.getResult()

       
        SampleDS.clampToZero()


        SampleDS.setTitle("Normalized Sample"+str(runnumber))
        self.send(SampleDS.clone(), 0, 1)

        import os
        filename = os.path.join(self.outputDir, "NormalizedSample"+str(runnumber)+".isd")
        ScriptUtil.save( filename,SampleDS) 
       
        GsasFileName = self.toGSASFilename(runnumber)

        useSeqNumbering = self.getParameter(self.SeqNums).getValue()

        if GsasFileName is not None:
            X = ScriptUtil.ExecuteCommand("Save3ColGSAS",[None,SampleDS, GsasFileName, useSeqNumbering])
            if isinstance(X, ErrorString):
               return X

        if self.VanadiumDS is not None:	
            op = SampleDS.getOperator("Divide by a DataSet")
           
            op.getParameter(0).setValue(self.VanadiumDS)
            X = op.getResult()
            if isinstance(X, ErrorString):
               return X
            SampleDS.setTitle("VNormalized Sample"+str(runnumber))
            self.send(SampleDS.clone(), 0, 1)

            import os
            filename = os.path.join(self.outputDir, "VNormalizedSample"+str(runnumber)+".isd")
            ScriptUtil.save( filename,SampleDS) 

            filename = str(runnumber)+".dat"
            import os
            filename = os.path.join(self.outputDir, filename)
            X=ScriptUtil.ExecuteCommand("SaveFullProf",[SampleDS,filename, useSeqNumbering])


    def getResult(self):

        runs = self.getRuns()
        self.outputDir = self.getParamValue(self.data)
# EventD_space2GSAS(sendData,showData,IOBS,instr,runnum,DetCalFile,BankFile,
#             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale)
        sendData =self.getParamValue(self.toTree)
        showData =self.getParamValue(self.show)
        instr="SNAP"
        #Samprunnum=self.getParamValue(self.DetCal)
        Backrunnum=self.getParamValue(self.BackFile)
        Vanrunnum=self.getParamValue(self.VFile)
        Emptrunnum=self.getParamValue(self.EmptyFile)
        DetCalFile =self.getParamValue(self.DetCal)
        BankFile=self.getParamValue(self.BankFile)
        MapFile=self.getParamValue(self.MapFile)
        rotDetCal = self.getParamValue( self.Rotate)
        firstEv=self.getParamValue(self.firstEvent)
        NumEvents=self.getParamValue(self.NEvents)
        d_min=self.getParamValue(self.mind)
        d_max=self.getParamValue(self.maxd)
        log_param=self.getParamValue(self.delta)
        scale=self.getParamValue(self.pchargeScalar)
        BadPeakFile=self.getParamValue(self.BadPeakds)
        PeakWidth_bad=self.getParamValue(self.BadPeakWidth)
        PeakInterval_bad=self.getParamValue(self. BadIntrvlReplace)
        NChanAv_bad=self.getParamValue(self.BadNumChanAv)
        CutOffFilter=self.getParamValue(self.FilterCutOff)
        OrderFilter=self.getParamValue(self.FilterOrder)

        if DetCalFile is None :
           DetCalFile =getDefaultDetCalFile(instr)
        if BankFile is None:
           BankFile =getDefaultBankFile(instr)
        if MapFile is None:
           MapFile =getDefaulMapFile(instr)

        if rotDetCal:
            DetCalFile = rotateDetectors(instr,Vanrunnum,DetCalFile)

       
        BackgroundFile = self.getParamValue(1)
        if BackgroundFile is None or BackgroundFile <=0:
           BackgroundFile = None

        if BackgroundFile is not None:

           self.BackgroundDS = EventD_space2GSAS(sendData,showData,IOBS,instr,Backrunnum,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale)

           self.BackgroundDS.setTitle("BackGround"+str(BackgroundFile))
           self.send(self.BackgroundDS, 0, 1)

           import os
           filename = os.path.join(self.outputDir, "BackGround"+str(BackgroundFile)+".isd")
           ScriptUtil.save( filename,self.BackgroundDS)
           
        else:
           self.BackgroundDS = None

        VanadiumFile = self.getParamValue(self.VFile)
        if VanadiumFile is None or VanadiumFile <=0:
           VanadiumFile = None

        if VanadiumFile is not None:


           X =  FixVanadium( sendData,showData,IOBS,instr, VanadiumFile,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale,BadPeakFile,
             PeakWidth_bad,PeakInterval_bad,NChanAv_bad,CutOffFilter,OrderFilter)

           self.VanadiumDS = X
           
           self.VanadiumDS.setTitle("Vanadium"+str(VanadiumFile))
           self.send(self.VanadiumDS.clone(), 0, 1)

           import os
           filename = os.path.join(self.outputDir, "Vanadium"+str(VanadiumFile)+".isd")
           ScriptUtil.save( filename,self.VanadiumDS)
          						  
        else:
           self.VanadiumDS = None

        EmptyFile = self.getParamValue(self.EmptyFile)

      
        if EmptyFile is None or EmptyFile <=0:
           EmptyFile = None
#FixVanadium( sendData,showData,IOBS,instr,runnum,DetCalFile,BankFile,
#             MapFile,firstEvent,NumEvents,d_min,d_max,log_param,scale,BadPeakFile,
#             PeakWidth_bad,PeakInterval_bad,NChanAv_bad,CutOffFilter,OrderFilter
               
        if EmptyFile is not None:

           X = EventD_space2GSAS(sendData,showData,IOBS,instr, EmptyFile,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale)
           self.EmptyFileDS = X

           self.EmpytFileDS.setTitle("EmpytFile"+str(self.EmpytFile))
           self.send(self.EmpytFileDS.clone(), 0, 1)

           import os
           filename = os.path.join(self.outputDir, "EmpytFile"+str(EmpytFile)+".isd")
           ScriptUtil.save( filename,self.EmpytFileDS)

           op = DataSetSubtract(self.VanadiumDS, EmptyFileDS.BackgroundDS,0)
           op.getResult()

        else:
           self.EmptyFileDS = None



        for runnumber in runs:
           self.processRun(sendData,showData,IOBS,instr,runnumber,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale)

        return None


    def getDocumentation( self):

        Res = StringBuffer()
        Res.append("@Overview  This operator will reduce powder data for SNAP\n")
        Res.append("Currently this operator does no ghosting or grouping other ")
        Res.append(" by detector\n")
        Res.append("@algorithm Event data is read from  sample, background, ")
        Res.append( " Vanadium, and EmptyCan  event files, converted to ")
        Res.append(  "dspacing and binned into histograms, one for each ")
        Res.append(  " detector. The Vanadium data has Vanadium peaks removed,")
        Res.append(  " then smoothed.  Each sample has the background(optionally)")
        Res.append(  " subtracted from it and divided by Vanadium(optional) - ")
        Res.append(  "EmptyCan(optional). The data sets are converted to tof so ")
        Res.append(  " can be used by gsas") 
        Res.append( "@param SNAP run number for the event file")
        Res.append( "@param SNAP background run number(optional)")
  
        Res.append( "@param SNAP vanadium run number(optional)")
        Res.append( "@param SNAP Empty Run(optional)")
        Res.append( "@param Rotate Detectors  if true the DetCal file below will ")
        Res.append( "     be rotated to the angle in the cvinfo file")
      
        Res.append( "@param DelCal file name(blank for default)")
                                    
        Res.append( "@param TS Banking file name(Blank for default) the file ")
        Res.append( "     with the correspondence between DAS id's and pixel ")
        Res.append( "     IDs")
        Res.append("@param TS Mapping file name(blank=default) the file with ")
        Res.append(   "    bank number and corresponding pixel ID's")
        Res.append("@param First event to load from the event file")
        Res.append("@param Number of events to load from the event file")
        Res.append(" @param Min d-spacing for binning the event data ")

        Res.append( "@param Max d-spacing for binning the event data ")
        Res.append("@param deltaD/D log bin relative width for binning event data")
        Res.append("@param divide pcharge by to scale resultant values")
        Res.append( "@param File Listing d Values of Peaks to Remove")
        Res.append( "@param Estimated Vanadium Peak Width( delta_d/d)")
        Res.append( "@param Interval to Replace(Times Peak Width)")
        Res.append( "@param Number of Channels to Average for the Replaced peaks")
        Res.append(" @param Filter Cut off for smoothing Vanadium spectra")
        Res.append( "@param Filter order")
        Res.append("@param Send all data to tree")
        Res.append(" @param Show plots")       
        Res.append(" @param Save directory for gsas and fullprof results")

        Res.append("@param Use Sequential Bank Numbering in gsas and fullprof ")
        Res.append("   results ")
        Res.append("@return the fullprof and gsas results for each run and also ")
        Res.append("  several data sets that can be viewed with ISAW ")
     
        return Res.toString()  
        
