

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from DataSetTools.operator.DataSet.Math.DataSet import *
from DataSetTools.dataset import *
from DataSetTools.operator.DataSet.Math.Scalar import *
from Command import *
from gov.anl.ipns.MathTools.Geometry  import *
from gov.anl.ipns.Util.SpecialStrings import *
from SNSlibs import *
class SNAP_to_data(GenericLoad):

    EventFile =0
    BackFile  =1
    VFile =2
    EmptyFile = 3
    Rotate =4
    Groups =5
    DetCal    =6
    BankFile =7
    MapFile =8
    firstEvent=9
    NEvents=10
    mind=11
    maxd=12
    delta=13
    pchargeScalar=14
    BadPeakds=15
    BadPeakWidth = 16
    BadIntrvlReplace =17
    BadNumChanAv  =18
    FilterCutOff = 19
    FilterOrder  =20
    toTree=21
    show=22
    data =23
    SeqNums =24
    Group1 ="All in 1 Group"
    Group2 ="Each Panel in 1 Group"
    GroupC ="Each Column in 1 Group"
    GroupP="Each Detector in 1 Group"
    GroupingName = GroupP
    Position =[]
    GroupName=""
    GroupList=[]
    GroupIDs =[]
    
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

        V = Vector()

        V.add( self.GroupP)
        V.add( self.Group1)
        V.add( self.Group2)
        V.add( self.GroupC)

        self.addParameter( ChoiceListPG("Detector Groups",V ))
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
      
        from gov.anl.ipns.Util.Numeric import IntList
        runs = IntList.ToArray(runsStr)
       
        return runs

    def toGSASFilename(self, runnumber):
        filename = "run_"+str(runnumber)+"_"+self.GroupingName + ".gsa"
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

      
        SampleDS =   Event2_GroupedTOF(instr, runnumber ,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,self.GroupList,self.Position,self.GroupingName,
             self.GroupIDs,log_param,scale)

     
        SampleDS.setTitle("Sample"+str(runnumber))
        self.send(SampleDS.clone(), showData,sendData)

        import os
        filename = os.path.join(self.outputDir, "Sample"+str(runnumber)+".isd")
        ScriptUtil.save( filename,SampleDS)

        if self.BackgroundDS is not None:
           op = DataSetSubtract(SampleDS, self.BackgroundDS,0)
           op.getResult()

       
        SampleDS.clampToZero()


        SampleDS.setTitle("Normalized Sample"+str(runnumber))
        self.send(SampleDS.clone(), showData,sendData)

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
            self.send(SampleDS.clone(),showData,sendData)

            import os
            filename = os.path.join(self.outputDir, "VNormalizedSample"+str(runnumber)+".isd")
            ScriptUtil.save( filename,SampleDS) 

            filename = "run_"+str(runnumber)+"_"+self.GroupingName+".dat"
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
        GroupChoice = self.getParamValue( self.Groups)
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

        self.GroupList =[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]]
        self.GroupName=[""]
        self.GroupingName= "GroupAll"
        self.GroupIDs=[100]
        self.Position = [[5,14]]
        if( GroupChoice == self.Group2):
           self.GroupList=[[1,2,3,4,5,6,7,8,9],[10,11,12,13,14,15,16,17,18]]
           self.GroupName = ["east","west"]
           self.GroupingName= "GroupBanks"
           self.Position = [[5],[4]]
           self.GroupIDs =[200,300]
        else:
           if GroupChoice == self.GroupC:
               self.GroupList =[[1,4,7],[2,5,8],[3,6,9],[10,13,16],[11,14,17],[12,15,18]]
               self.GroupName =["east_high","east_mid","east_low","west_high","west_mid","west_low"]
               self.GroupingName= "GroupCols"
               self.GroupIDs =[230,220,210,330,320,310]
               self.Position=[[4],[5],[6],[13],[14],[15]]
           else:
               if GroupChoice == self.GroupP:
                    self.GroupList =[[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],[11],[12],[13],[14],[15],[16],[17],[18]]
                    self.GroupName=["1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18"]
                    self.GroupingName= "GroupDets"
                    self.Position = [[1],[2],[3],[4],[5],[6],[7],[8],[9],[10],[11],[12],[13],[14],[15],[16],[17],[18]]
                    self.GroupIDs = None

        BackgroundFile = self.getParamValue(1)
        if BackgroundFile is None or BackgroundFile <=0:
           BackgroundFile = None

        if BackgroundFile is not None:

           self.BackgroundDS = Event2_GroupedTOF(instr,Backrunnum,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,self.GroupList,self.Position,self.GroupingName,self.GroupIDs,log_param,scale)

           if isinstance(self.BackgroundDS, ErrorString):
               raise ErrorString.toString()

           self.BackgroundDS.setTitle("BackGround"+str(BackgroundFile))
           self.send( self.BackgroundDS, showData, sendData)
          
           import os
           filename = os.path.join(self.outputDir, "BackGround"+str(BackgroundFile)+".isd")
           ScriptUtil.save( filename,self.BackgroundDS)
           
        else:
           self.BackgroundDS = None

        VanadiumFile = self.getParamValue(self.VFile)
        if VanadiumFile is None or VanadiumFile <=0:
           VanadiumFile = None

        if VanadiumFile is not None:


           X =  FixVanadium(sendData,showData,IOBS,instr, VanadiumFile,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,log_param,scale,BadPeakFile,
             PeakWidth_bad,PeakInterval_bad,NChanAv_bad,CutOffFilter,OrderFilter)

           
           self.VanadiumDS = X.empty_clone()

           for k in xrange( len(self.GroupList)):
              pos = getPos( X, self.Position, k)
              id = k+1
              if self.GroupIDs is not None  and len(self.GroupIDs) == len(self.GroupList):
                 id= self.GroupIDs[k]
              G1 = sumGroups( X,self.VanadiumDS,self.GroupList[ k],id,DetPosAttribute(Attribute.DETECTOR_POS,pos))
           
           self.VanadiumDS.setTitle("Vanadium"+str(VanadiumFile))
           self.send(self.VanadiumDS.clone(), showData, sendData)

           import os
           filename = os.path.join(self.outputDir, "Vanadium"+str(VanadiumFile)+".isd")
           ScriptUtil.save( filename,self.VanadiumDS)
          						  
        else:
           self.VanadiumDS = None

        EmptyFile = self.getParamValue(self.EmptyFile)

      
        if EmptyFile is None or EmptyFile <=0:
           EmptyFile = None
               
        if EmptyFile is not None:

           X =  Event2_GroupedTOF(instr, EmptyFile,DetCalFile,BankFile,
             MapFile,firstEv,NumEvents,d_min,d_max,self.GroupList,self.Position,self.GroupingName,self.GroupIDs,log_param,scale)

           self.EmptyFileDS = X

           self.EmpytFileDS.setTitle("EmpytFile"+str(self.EmpytFile))
           self.send(self.EmpytFileDS.clone(), showData, sendData)

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
       

def getPos(DS_d, Position,group):
     
           Det1 = Position[group]
        
           nDet =0
           position=[]
           for detKey in xrange(len(Det1)):
               D = DS_d.getData_entry_with_id(Det1[detKey])
               if position is None:
                  position.append( AttrUtil.getDetectorPosition(D))
                  nDet +=1
               else:
                  position.append(AttrUtil.getDetectorPosition(D))
                  nDet +=1
           weight =[]
          
           for kk in range( nDet):
               weight.append(1.0/nDet)
         
           pos = DetectorPosition.getAveragePosition( position, weight)
           return pos

        
def Event2_GroupedTOF(instr,runnum,DetCalFile,BankFile,
             MapFile,firstEvent,NumEvents,d_min,d_max,Group, Position,GroupingName,GroupIDs,log_param,scale):

       DS_d = Event2d_DataSet(instr,runnum,DetCalFile,BankFile,
             MapFile,firstEvent,NumEvents,d_min,d_max,log_param,scale)
       DS_dGrouped = DS_d.empty_clone()
      
       for key in xrange(len(Group)):
            pos = getPos( DS_d,Position,key)

            id = key+1
            if GroupIDs is not None  and len(GroupIDs) == len(Group):
                 id= GroupIDs[key]
             
            sumGroups(DS_d, DS_dGrouped, Group[key], id , DetPosAttribute(Attribute.DETECTOR_POS,pos))

# Should add something to data set log. Check
       Res = Convert2Tof( DS_dGrouped,"Run"+str(runnum)+":"+GroupingName)
       
       fixUnits( Res )
       removeZeros( Res )
       return Res
      
 
def Event2d_DataSet(instr,runnum,DetCalFile,BankFile,
             MapFile,firstEvent,NumEvents,d_min,d_max,log_param,scale):
        
        (eventFile, pcharge) = getRunStuff(instr,runnum)
        
        pcharge = getProtonsCharge( instr, runnum)
        pcharge = pcharge/scale
        print "scale = %.2f" % pcharge


        log_param = log_param * d_min # log param
        useLogBinning = 1
        nbins =0
        useD_spaceMapFile = 0
        d_spaceMapFile =""
        # do the initial load and time focus
        import os

#        if not os.path.exists(event):
#            raise Error, "%s does not exist" % event
        
        args= [eventFile,DetCalFile,BankFile,MapFile,firstEvent,
                                  NumEvents,d_min,d_max,useLogBinning,log_param,nbins,
                                  0,"",0,"",0,0]
        if  os.path.exists(eventFile):
             panel_ds = Util.Make_d_DataSet( eventFile,DetCalFile,BankFile,MapFile,firstEvent,NumEvents,d_min,d_max,useLogBinning,log_param,nbins, 0,"",0,"",0,0)
        else:
             L = eventFile.split('_')
             L[len(L)-2]="neutron0"
             eventFile1 = ""
             for i in range(0,len(L)-1):
                eventFile1 += L[i]+'_'
             eventFile1 += L[len(L)-1]
             panel_ds1 = Util.Make_d_DataSet( eventFile1,DetCalFile,BankFile,MapFile,firstEvent,NumEvents,d_min,d_max,useLogBinning,log_param,nbins, 0,"",0,"",0,0)
             eventFile2 =""
             L[len(L)-2]="neutron1"
             for i in range(0,len(L)-1):
                eventFile2 += L[i]+'_'
             eventFile2 += L[len(L)-1]
             panel_ds2 = Util.Make_d_DataSet( eventFile2,DetCalFile,BankFile,MapFile,firstEvent,NumEvents,d_min,d_max,useLogBinning,log_param,nbins, 0,"",0,"",0,0)
             panel_ds = DataSetMerge(panel_ds1,panel_ds2).getResult()     

        
       

        panel_ds.setSqrtErrorsAtLeast_1()
        if  isinstance(DataSetScalarDivide( panel_ds, pcharge,0).getResult(), ErrorString):
           raise "Cannot divide by a scalar"
        

        return panel_ds

def Convert2Tof( d_DataSet, title):

        op = d_DataSet.getOperator("ToTof")
        if op is None:
            op = d_DataSet.getOperator("Convert d-Spacing to TOF")
        tof_DataSet = op.getResult()
        if isinstance(tof_DataSet, ErrorString): # error occured
            return tof_DataSet
        title = tof_DataSet.getTitle().replace("_d-spacing", " tof")
        tof_DataSet.setTitle(title)
       
        return tof_DataSet

def sumGroups( inds, outds, groups, newId, position):
        data = inds.getData_entry_with_id(groups[0])
        if data is None:
            raise "Something went wrong with group id = %d" % groups[0]
        for i in groups[1:]:
            data = data.add(inds.getData_entry_with_id(i))
            if data is None:
                raise "Something went wrong with group id = %d" % i
        data.setGroup_ID(newId)
        data.setAttribute(position)
        outds.addData_entry(data)
