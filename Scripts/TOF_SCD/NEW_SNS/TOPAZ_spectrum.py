# TOPAZ spectrum

#  Script to obtain spectrum for each detector (Bank) and
#  write output to ASCII files. Input is a data file of
#  vanadium or TiZr, and a background file.

#  Based on TOPAZ_spectrum_multiple_banks.iss

#  Jython version:  A. J. Schultz, August 2010

from DataSetTools.operator.DataSet.Math.DataSet import *
from DataSetTools.operator.DataSet.Math.Scalar import *
# from DataSetTools.operator.DataSet.Attribute import *
from Command import *
from Operators.Generic.Save.SaveASCII_calc import *
from gov.anl.ipns.Util.SpecialStrings import AttributeNameString
# from Operators.Special import *
# from gov.anl.ipns.Util.Numeric.IntList import *
# from DataSetTools.dataset import AttrUtil

# import sys


class TOPAZ_spectrum(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Raw data path:", "C:/Users/Arthur/Desktop/Topaz/spectrum/1268"))
        self.addParameter(StringPG("Run number of data file:", "1268"))
        self.addParameter(StringPG("Run number of background file:", "1270"))
        self.addParameter(IntegerPG("Number of detectors:", 14))
        self.addParameter(BooleanEnablePG("Apply Savitzky-Golay smoothing Filter?", "[1,3,0]"))
        self.addParameter(IntegerPG("Number of points to the left of center:", 20))
        self.addParameter(IntegerPG("Number of points to the right of center:", 20))
        self.addParameter(IntegerPG("Degree of smoothing polynomial:", 3))
        
    def getResult(self):

        path = self.getParameter(0).value
        runNum_1 = self.getParameter(1).value
        runNum_2 = self.getParameter(2).value
        number_of_detectors = self.getParameter(3).value
        doSmoothing = self.getParameter(4).value
        pointsLeft = self.getParameter(5).value
        pointsRight = self.getParameter(6).value
        polyDegree = self.getParameter(7).value
        
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
        
        # Begin 'for' loop for each detector.

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
            ClearSelect(ds_1).getResult()
            ClearSelect(ds_2).getResult()
            #
            #  The select the pixels in the region, by index, since
            # each detector's DataSet has indices from 0 to 65535, we
            # don't need to worry about what pixel IDs are in what
            # area detector.
            #
            SelectByIndex(ds_1, "0:65535", "Set Selected").getResult()
            SelectByIndex(ds_2, "0:65535", "Set Selected").getResult()
            
            #  Sum up the spectra from the region and send the data
            #  to the Isaw tree
            #
            sum_ds_1 = SumCurrentlySelected(ds_1, 1, 1).getResult()
            sum_ds_2 = SumCurrentlySelected(ds_2, 1, 1).getResult()
            
            #
            #  Scale the background spectrum
            sum_ds_2_scaled = DataSetScalarMultiply(sum_ds_2, scale, 1).getResult()

            #  Subtract the background from the TiZr or V data to 
            #  obtain the spectrum
            spectrum = DataSetSubtract(sum_ds_1, sum_ds_2_scaled, 1).getResult()

            #  Convert counts to counts per microsecond
            newspec = DivideByDeltaX(spectrum, 1).getResult()
            
            if doSmoothing:
            #  Apply Savitzky-Golay Filter
                print ""
                print "Savitzky-Golay Filter is being applied."
                print ""
                Specindex = 0
                GroupID = GetDataAttribute( newspec, Specindex, \
                    AttributeNameString("Group ID")).getResult()
                sGroupID = str(GroupID)
                ScriptUtil.ExecuteCommand("SavitzkyGolayFilter", \
                    [newspec, pointsLeft, pointsRight, polyDegree, sGroupID, \
                    0., 20000., Boolean(0), Boolean(0)])
            
            ScriptUtil.send(newspec, IOBS)
            ScriptUtil.display(newspec, "Selected Graph View")

            #
            #  Save spectrum to an ASCII text file
            #
            # title = newspec.getTitle()
            # print title
            # xUnits = newspec.getX_units()
            # print xUnits
            # dataEntry = newspec.getData_entry(0)
            # print dataEntry     # prints "Group ID: 65536"
            # xValues = dataEntry.getX_scale().getXs()
            # print xValues
            # yValues = dataEntry.getY_values()
            # print yValues
            
            filename = path + "Bank" + str(DSnum) + "_spectrum.asc"
            SaveASCII(newspec, 0, "%12.3f %12.3f", filename)
            
            
            
        print 'The End!'
            
        # Return "All done!"
        
    def getCategoryList( self):      
        return ["Macros","Single Crystal"] 
        
    def __init__(self):
        Operator.__init__(self,"TOPAZ_spectrum")
        
        
