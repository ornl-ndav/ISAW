# TOPAZ spectrum

#  Script to obtain spectrum for each detector (Bank) and
#  write output to ASCII files. Input is a data file of
#  vanadium or TiZr, and a background file.

#  Based on TOPAZ_spectrum_multiple_banks.iss

#  Jython version:  A. J. Schultz, August 2010

from DataSetTools.operator import *
from DataSetTools.parameter import *
from gov.anl.ipns.Parameters import *
from DataSetTools.operator.Generic import GenericOperator
from DataSetTools.operator.DataSet.Attribute import *
from DataSetTools.operator.DataSet.Math.Analyze import *
from DataSetTools.operator.Generic.Batch import *
from DataSetTools.operator.Generic.Special import ViewASCII
from DataSetTools.operator.Generic.TOF_SCD import *
from DataSetTools.parameter import *
from DataSetTools.util import *
from IsawGUI import Util
from java.lang import *
from java.util import *
from Command import *

import sys


class TOPAZ_spectrum(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Raw data path:", "C:/Users/Arthur/Desktop/Topaz/spectrum/1268"))
        self.addParameter(StringPG("Run number of data file:", "1268"))
        self.addParameter(StringPG("Run number of background file:", "1270"))
        self.addParameter(IntegerPG("Number of pixels in one column or row:", 256))
        self.addParameter(IntegerPG("First column of region (min X):", 10))
        self.addParameter(IntegerPG("Last column of region (max X):", 245))
        self.addParameter(IntegerPG("First row of region (min Y):", 10))
        self.addParameter(IntegerPG("Last row of region (max Y):", 245))
        self.addParameter(IntegerPG("Number of detectors:", 14))
        self.addParameter(BooleanEnablePG("Apply Savitzky-Golay smoothing Filter?", "[1,3,0]"))
        self.addParameter(IntegerPG("Number of points to the left of center:", 10))
        self.addParameter(IntegerPG("Number of points to the right of center:", 10))
        self.addParameter(IntegerPG("Degree of smoothing polynomial:", 3))
        
    def getResult(self):

        path = self.getParameter(0).value
        runNum_1 = self.getParameter(1).value
        runNum_2 = self.getParameter(2).value
        numRowCol = self.getParameter(3).value
        firstCol = self.getParameter(4).value
        lastCol = self.getParameter(5).value
        firstRow = self.getParameter(6).value
        lastRow = self.getParameter(7).value
        number_of_detectors = self.getParameter(8).value
        doSmoothing = self.getParameter(9).value
        pointsLeft = self.getParameter(10).value
        pointsRight = self.getParameter(11).value
        polyDegree = self.getParameter(12).value
        
        #  Obtain scaling factor from beam monitor data
        filename = path + 'TOPAZ_' + runNum_1 + '.nxs'
        ds_1 = ScriptUtil.ExecuteCommand("OneDS",[filename, 0, ""])
        ScriptUtil.send(ds_1, IOBS)
        monitorSum1 = ds_1.getAttributeValue("Total Count")
        ScriptUtil.display ("monitorSum1 = " + str(monitorSum1))
        protonCharge_1 = ds_1.getAttributeValue("proton_charge")
        
        filename = path + 'TOPAZ_' + runNum_2 + '.nxs'
        ds_2 = ScriptUtil.ExecuteCommand("OneDS",[filename, 0, ""])
        ScriptUtil.send(ds_2, IOBS)
        monitorSum2 = ds_2.getAttributeValue("Total Count")
        ScriptUtil.display ("monitorSum2 = " + str(monitorSum2))
        protonCharge_2 = ds_2.getAttributeValue("proton_charge")
        
        scale = monitorSum1 / monitorSum2
        print 'scale = %10.5f' % scale
        scalePC = protonCharge_1 / protonCharge_2
        print 'proton_charge ratio = %10.5f' % scalePC
        
        # Begin for loop for each detector.

        for bank in range(number_of_detectors):
            DSnum = bank + 1
            print 'Detector Bank = %d' % DSnum
            filename = path + 'TOPAZ_' + runNum_1 + '.nxs'
            ds_1 = ScriptUtil.ExecuteCommand("OneDS",[filename, DSnum, ""] )
            filename = path + 'TOPAZ_' + runNum_2 + '.nxs'
            ds_2 = ScriptUtil.ExecuteCommand("OneDS",[filename, DSnum, ""] )

            #
            #  Select the pixels that were requested
            #
            #  First be sure any previously selections are cleared 
            #
            ClearSelect(ds_1)
            ClearSelect(ds_2)
            #
            #  The select the pixels in the region, by index, since
            # each detector's DataSet has indices from 0 to 65535, we
            # don't need to worry about what pixel IDs are in what
            # area detector.
            #
            for col in range(firstCol,lastCol):
                print 'col = %d' % col
                first_index = (col-1)*numRowCol + firstRow-1
                last_index  = (col-1)*numRowCol + lastRow-1
                # range_string = "" & first_index & ":" & last_index
                range_string = str(first_index) + ':' + str(last_index)
                print range_string
                SelectByIndex( ds_1, range_string, "Set Selected" )
                SelectByIndex( ds_2, range_string, "Set Selected" )
            
            
            break
            
        print 'STOP!'
            
        # Return 'All done!'
        
    def __init__(self):
        Operator.__init__(self,"TOPAZ_spectrum")
        
        
