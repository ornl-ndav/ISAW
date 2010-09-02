
from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from gov.anl.ipns.Util.SpecialStrings import ErrorString
from Operators.TOF_Diffractometer import *
from Operators.Special import *
from Operators.TOF_SCD import *
from Command import ScriptUtil
from NexIO import *;
from NexIO.Util import *;
from java.lang import System
def getIsawHome():
        
        Res = System.getProperty("ISAW_HOME")
       
        if not Res.endswith("/"):
           Res +=  '/'
        return Res

def getDefaultDetCalFile( instr):
         Res = getIsawHome()+"InstrumentInfo/SNS/"+instr+"/"+instr+".DetCal"
         return Res

def getDefaultBankFile( instr):
         Res = getIsawHome()+"InstrumentInfo/SNS/"+instr+"/"+instr+"_bank.xml"
         return Res

def getDefaultMapFile( instr):
         Res = getIsawHome()+"InstrumentInfo/SNS/"+instr+"/"+instr+"_TS.dat"
         return Res

def getDefaultVanPeakFile( ):
         Res = getIsawHome()+"Databases/VanadiumPeaks.dat"
         return Res



def getCalibPG(instr, type):
        cmd = "findcalib -i"+instr+" --listall " + type
        print cmd
        from java.lang import Runtime
        proc = Runtime.getRuntime().exec(cmd)
        print proc
        import ExtTools.monq.stuff.Exec
        thing = ExtTools.monq.stuff.Exec(proc)
        if not thing.done():
            pass
        print thing.getOutputText()
        return None # should make a system call to findcalib

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

def getPosAttr( bragg, L2):
        from gov.anl.ipns.MathTools.Geometry import Vector3D, DetectorPosition
        import math
        pos = [L2*math.cos(bragg*math.pi/180.),
               L2*math.sin(bragg*math.pi/180.),
               0., 0.] # last one is the weight
        pos = Vector3D(pos)
        pos = DetectorPosition(pos)

        from DataSetTools.dataset import DetPosAttribute
        attr = DetPosAttribute("Effective Position", pos)
        
        return attr

def getRunDir(instr, runnumber):
        cmd = "findnexus -i"+instr+" -A %d" % runnumber
        import os
        f = os.popen(cmd)
        nxs = f.readline()
        nxs = nxs.strip()
        if not os.path.exists(nxs):
            raise Error, "Failed to find run \"%d\": %s"  % (runnumber, nxs)
        temp = os.path.split(nxs)[0] # NeXus directory
        temp = os.path.split(temp)[0] # run directory
        return (nxs, os.path.join(temp, "preNeXus"))

def getRunStuff(instr, runnumber):
        import os
       
        # get the nexus file and prenexus directory
        (nxs, prenxs) = getRunDir(instr, runnumber)
        # print ["nxs,prenxs",nxs,prenxs]
        # determine the event file
        event = "%s_%d_neutron_event.dat" % (instr, runnumber)
        event = os.path.join(prenxs, event)
        #print ["event",event]
#        if not os.path.exists(event):
#            raise Error, "%s does not exist" % event

        # determine the cvinfo file
        cvinfo = "%s_%d_cvinfo.xml" % (instr, runnumber)
        cvinfo = os.path.join(prenxs, cvinfo)
        if not os.path.exists(cvinfo):
            raise Error, "%s does not exist" % cvinfo

        # get the protoncharge
        pcharge = 1.
        cvinfo = open(cvinfo, "r")
        temp = cvinfo.readline()
        while not "protoncharge" in temp:
            temp = cvinfo.readline()
        if len(temp) > 0:
            start = temp.index("value=\"") + len("value=\"")
            stop = temp.index("\"", start + 1)
            pcharge = float(temp[start:stop])
        cvinfo.close()

        return (event, pcharge)

def fixUnits( dataset):
        """This needs to be done if the label and units were interchanged"""
        units = dataset.getX_units()
        label = dataset.getX_label()
        if units == "Time-of-flight":
            dataset.setX_units(label)
            dataset.setX_label(units)

def removeZeros( dataset):
        ids = range(dataset.getNum_entries())
        for i in ids:
            spectrum = dataset.getData_entry(i)
            y = spectrum.getY_values()
            dy = spectrum.getErrors()
            for j in xrange(len(y)):
                if y[j] == 0.:
                    y[j] = 1.
                    dy[j] = 1.
            #dataset.replaceData_entry(spectrum, i)

def send( ds, showPlots, sendData,IOBS):
        if ds is None:
            return
        from Command import ScriptUtil
        if showPlots:
            ScriptUtil.display(ds)
        if sendData and IOBS is not None:
            ScriptUtil.send(ds, IOBS)


def EventD_space2GSAS(sendData,showData,IOBS,instr,runnum,DetCalFile,BankFile,
             MapFile,firstEvent,NumEvents,d_min,d_max,log_param,scale):
        
        (eventFile, pcharge) = getRunStuff(instr,runnum)
        
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
             L[len(L)-2]="neutron1"
             eventFile1 = ""
             for i in range(0,len(L)-1):
                eventFile1 += L[i]+'_'
             eventFile1 += L[len(L)-1]
             panel_ds1 = Util.Make_d_DataSet( eventFile,DetCalFile,BankFile,MapFile,firstEvent,NumEvents,d_min,d_max,useLogBinning,log_param,nbins, 0,"",0,"",0,0)
             eventFile2 =""
             L[len[L]-2]="neutron2"
             for i in range(0,len(L)-1):
                eventFile2 += L[i]+'_'
             eventFile2 += L[len(L)-1]
             panel_ds2 = Util.Make_d_DataSet( eventFile,DetCalFile,BankFile,MapFile,firstEvent,NumEvents,d_min,d_max,useLogBinning,log_param,nbins, 0,"",0,"",0,0)
             panel_ds = DataSetMerge(panel_ds1,panel_ds2).getResult()     

        
       
        send(panel_ds, showData, sendData,IOBS)

        panel_ds.setSqrtErrorsAtLeast_1()
        
        
        # sum spectra together
        gsas_ds = panel_ds.clone()
        
        # convert back to tof
        op = gsas_ds.getOperator("ToTof")
        if op is None:
            op = gsas_ds.getOperator("Convert d-Spacing to TOF")
        gsas_tof = op.getResult()
        if isinstance(gsas_tof, ErrorString): # error occured
            return gsas_tof
        title = gsas_tof.getTitle().replace("_d-spacing", " tof")
        gsas_tof.setTitle(title)
        send(gsas_tof, showData, sendData,IOBS)

        # normalize the data
        op = gsas_tof.getOperator("Divide by Scalar")
        scalar = op.getParameter(0)
        import java.lang.Float
        scalar.setValue(java.lang.Float(pcharge))
        op.getResult()
        send(gsas_tof, showData, sendData,IOBS)
       
        fixUnits(gsas_tof)
        removeZeros(gsas_tof)

        send( gsas_tof, showData,sendData,IOBS)
        return gsas_tof
def run():
    print EventD_space2GSAS( 1,0,IOBS,"PG3",666,"/SNS/users/pf9/NEW_PG3.DetCal","/SNS/PG3/2010_2_11_CAL/calibrations/PG3_bank_2010_04_22.xml","/SNS/PG3/2009_2_11A_CAL/calibrations/PG3_TS_2009_04_17.dat",0,1E12,.2,5,2E-4,9.99E12)

def FixVanadium( sendData,showData,IOBS,instr,runnum,DetCalFile,BankFile,
             MapFile,firstEvent,NumEvents,d_min,d_max,log_param,scale,BadPeakFile,
             PeakWidth_bad,PeakInterval_bad,NChanAv_bad,CutOffFilter,OrderFilter
               ):
        # whether or not to send all datasets to the tree
       

        
        print firstEvent
        ds =EventD_space2GSAS(0,showData,IOBS,instr,runnum,DetCalFile,BankFile,
             MapFile,firstEvent,NumEvents,d_min,d_max,log_param,scale)
       
      
        send(ds.clone(), showData, sendData,IOBS)
        print ["ds?", ds.getNum_entries()]
        RemovePeaks_Calc.RemovePeaks_tof(ds, BadPeakFile,PeakWidth_bad, PeakInterval_bad,NChanAv_bad)

        
        Res = LowPassFilterDS(ds, CutOffFilter, OrderFilter).getResult()

        if isinstance( Res, ErrorString):
           return Res

        send(ds, showData, sendData,IOBS)
       
        return ds
        
def run5():
     print FixVanadium(1,0,IOBS,"PG3",539,"/SNS/users/pf9/NEW_PG3.DetCal",
       "/SNS/PG3/2010_2_11_CAL/calibrations/PG3_bank_2010_03_11.xml",
        "/SNS/PG3/2010_2_11A_CAL/calibrations/PG3_TS_2009_04_17.dat",
        0,1E12,.2,10,2E-4,9.999E10,"/SNS/users/ehx/SNS_ISAW/Databases/VanadiumPeaks.dat",.005,1.9,10,.02,2)

def rotateDetectors( instr, runnum, DetCalFile1):
     
      if DetCalFile1 is None:
          DetCalFile1 = getDefaultDetCalFile( instr)
      
      import os
      (nxs, prenxs) = getRunDir(instr, runnum)
      cvinfo = "%s_%d_cvinfo.xml" % (instr, runnum)
      cvinfo = os.path.join(prenxs, cvinfo)
      if not os.path.exists(cvinfo):
         raise Error, "%s does not exist" % cvinfo
           
      V = Util.getRotationAngles( cvinfo)
      if V is None or V.size()< 1:
          print "No information to rotate the Detectors"
          return DetCalFile1

      ang1 = V.firstElement().firstElement()
      ang2 = V.elementAt(1).firstElement()
      #will only do one detector for now
      DetCal1 = System.getProperty("user.home")
      if not DetCal1.endswith("/"):
          DetCal1 +='/'
      Save = DetCal1
      Save= DetCal1
      DetCal1 += "ISAW/tmp/dummy.DetCal"
      DetCal2 = Save+"ISAW/tmp/dummy2.DetCal"
      print ["new DetCal file",DetCal1]
      General_Utils.RotateDetectors( DetCalFile1,14,DetCal1, ang1,0,0)
      try:
         General_Utils.RotateDetectors( DetCal1,5,DetCal2, ang2,0,0, ScriptUtil.ToVec([1,2,3,4,5,6,7,8,9]))
      except:
         return DetCal1
      try:
         General_Utils.RotateDetectors( DetCal1,5, Save +"ISAW/tmp/dummy2.DetCal", ang2,0,0,ScriptUtil.ToVec([1,2,3,4,5,6,7,8,9]))
      except:
         return DetCal1
#      ScriptUtil.ExecuteCommand("RotateSnapDetectors",[DetCalFile1,DetCal1,1,ang1,1,ang2])

      return DetCal2

def getProtonsCharge( instr, runnum):
      import os
      (nxs, prenxs) = getRunDir(instr, runnum)
      node = NexNode( nxs)
      for i in range(0,node.getNChildNodes()):
         n = node.getChildNode( i)
         if n.getNodeClass().equals("NXentry"):
            V = n.getNodeValue()
            F = ConvertDataTypes.floatValue(V)
            return Float(F).doubleValue()
      return Double.NaN
