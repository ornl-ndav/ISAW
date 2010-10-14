#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from DataSetTools.operator.DataSet.Math.DataSet import *
from gov.anl.ipns.Util.SpecialStrings import *
from Operators.TOF_Diffractometer  import *
from Operators.Special import *

class VanadiumFix(GenericLoad):

    EventFile = 0
    ConfigFile = 1
    firstEvent = 2
    NEvents = 3
    mind = 4
    maxd=5
    delta=6
    pchargeScalar=7
    BadPeakWidth = 8
    BadIntrvlReplace = 9
    BadNumChanAv  = 10
    FilterCutOff = 11
    FilterOrder  =12
    ZeroUncertainties = 13
    toTree=14
    show=15
    
    
    def __init__(self):
        Operator.__init__(self, "Vanadium Load")
        self.instr = "PG3"
        try:
            self.IOBS = IOBS
        except:
            self.IOBS = None

    def getCommand(self):
        return "VanadiumLoad"

    def getCalibPG(self, type):
        cmd = "findcalib -i pg3 --listall " + type
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

    def setDefaultParameters(self):
        self.super__clearParametersVector()

        self.addParameter(IntegerPG("PG3 vanadium  run number", 733))  # param 0
        self.addParameter(LoadFilePG("Configuration file name",
                          "/SNS/PG3/2010_2_11A_CAL/"))                 # param 1
        self.addParameter(FloatPG("First event to load", 0.))          # param 2
        self.addParameter(FloatPG("Number of events to load", 1e12))   # param 3
        self.addParameter(FloatPG("Min d-spacing", 0.2))               # param 4
        self.addParameter(FloatPG("Max d-spacing", 10.))               # param 5
        self.addParameter(FloatPG("deltaD/D", 2e-4))                   # param 6
        self.addParameter(FloatPG("nomalize to pcharge", 1e13))        # param 7
        self.addParameter(FloatPG("Estimated Peak Width( delta_d/d)",
                                  .0050))                             # param 8
        self.addParameter(FloatPG("Interval to Replace(Times Peak Width)",
                                  1.9))                               # param 9
        self.addParameter(IntegerPG("Number of Channels to Average",
                                    10))                              # param 10
        self.addParameter(FloatPG("Filter Cut off",.02))              # param 11
        self.addParameter(IntegerPG("Filter order", 2))               # param 12
        self.addParameter(BooleanPG("Set uncertainties to zero", True)) # param 13
        self.addParameter(BooleanPG("Send all data to tree", False))  # param 14
        self.addParameter(BooleanPG("Show plots", False))             # param 15

    def getParamValue(self, index):
        param = self.getParameter(index)
        if param is None:
            raise RuntimeError("Failed to get parameter %d" % index)
        else:
            return param.getValue()

 
    def send(self, ds, showPlots, sendData):
        if ds is None:
            return
        from Command import ScriptUtil
        if showPlots:
            ScriptUtil.display(ds)
        if sendData and self.IOBS is not None:
            ScriptUtil.send(ds, self.IOBS)

    def applyMultScatt(self, datain):
        # constants
        rod_radius = 0.3175 # V rod radius in cm
        COEFF1 = 2.8        # Absorption Cross Section/1.81
        COEFF2 = 0.0721     # Density
        COEFF3 = 5.1        # Total Scattering Cross Section

        L1 = datain.getData_entry(0).getAttribute("Initial Path").getValue()

        datatemp = datain.empty_clone()
        dataout = datain.empty_clone()
        
        from math import pi
        from Command import ScriptUtil

        num_spectra = datain.getNum_entries()
        for i in xrange(num_spectra):
            data = datain.getData_entry(i)
            pos = data.getAttribute("Effective Position").getDetectorPosition()
            flight_path = L1 + pos.getDistance()
            angle = 180. * pos.getScatteringAngle() / pi
            datatemp.addData_entry(data)
            result = ScriptUtil.ExecuteCommand("Abs_MScatt_Correct",
                                               [datatemp, angle, rod_radius, COEFF1, COEFF2, COEFF3, flight_path])
            if isinstance(result, ErrorString):
                return result
            data = result.getData_entry(0)
            dataout.addData_entry(data)
            datatemp.removeData_entry(0)
        return dataout

    def getResult(self):
        # get the configuration file and load it
        execfile(self.getParamValue(1))
        config = getConfig()

        # whether or not to send all datasets to the tree
        sendData = self.getParamValue(self.toTree)

        # whether or not to plot intermediate results
        showData = self.getParamValue(self.show)

        from Command import ScriptUtil
        args = []
        args.append(self.getParamValue(self.EventFile)) # event file
        args.append(self.getParamValue(self.ConfigFile)) # config file
        args.append(self.getParamValue(self.firstEvent)) # first event
        args.append(self.getParamValue(self.NEvents)) # number of events
        args.append(self.getParamValue(self.mind)) # d-space min
        args.append(self.getParamValue(self.maxd)) # d-space max
        args.append(self.getParamValue(self.delta))
        args.append(self.getParamValue(self.pchargeScalar))
        args.append( 0)
        args.append(0)
        X = ScriptUtil.ExecuteCommand( "PG3_to_dspace", args)
        if  isinstance( X, ErrorString):
           return X
        BadPeakFile = config["VAN_PEAKS"]
        PeakWidth_bad = self.getParamValue( self.BadPeakWidth)
        PeakInterval_bad = self.getParamValue( self.BadIntrvlReplace)
        NChanAv_bad = self.getParamValue( self.BadNumChanAv)
        RemovePeaks_Calc.RemovePeaks_tof(X, BadPeakFile,PeakWidth_bad, PeakInterval_bad,NChanAv_bad)

        CutOffFilter = self.getParameter(self.FilterCutOff).getValue()
        OrderFilter  = self.getParameter( self.FilterOrder).getValue()
        Res = LowPassFilterDS( X, CutOffFilter, OrderFilter).getResult()
        if isinstance( Res, ErrorString):
           return Res

        if self.getParamValue(self.ZeroUncertainties):
            num_spectra = X.getNum_entries()
            for i in xrange(num_spectra):
                data = X.getData_entry(i)
                errors = data.getErrors()
                for j in xrange(len(errors)):
                    errors[j] = 0.

        self.send(X, showData, sendData)
       
        return self.applyMultScatt(X)
        
