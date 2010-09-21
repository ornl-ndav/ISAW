# TOPAZ spectrum

#  Script to obtain spectrum for each detector (Bank) and
#  write output to ASCII files. Input is a data file of
#  vanadium or TiZr, and a background file.

#  Based on TOPAZ_spectrum_multiple_banks.iss

#  Jython version:  A. J. Schultz, August 2010

from DataSetTools.operator.DataSet.Math.DataSet import *
from DataSetTools.operator.DataSet.Math.Scalar import *
from Command import *
from Operators.Generic.Save.SaveASCII_calc import *
from gov.anl.ipns.Util.SpecialStrings import AttributeNameString
from math import *
from absor_V_rod import *

class TOPAZ_spectrum(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Raw data path:", "C:/Users/Arthur/Desktop/Topaz/spectrum/1268"))
        self.addParameter(StringPG("Run number of data file:", "1268"))
        self.addParameter(StringPG("Run number of background file:", "1270"))
        self.addParameter(IntegerPG("Number of detectors:", 14))
        self.addParameter(LoadFilePG("DetCal file:", "C:/ISAW/InstrumentInfo/SNS/TOPAZ/TOPAZ.DetCal"))
        self.addParameter(BooleanEnablePG("Apply Savitzky-Golay smoothing Filter?", "[1,3,0]"))
        self.addParameter(IntegerPG("Number of points to the left of center:", 20))
        self.addParameter(IntegerPG("Number of points to the right of center:", 20))
        self.addParameter(IntegerPG("Degree of smoothing polynomial:", 3))
        self.addParameter(DataDirPG("Directory for output spectrum file:", "C:/Users/Arthur/Desktop/Topaz/spectrum/1268"))
        
    def getResult(self):

        path = self.getParameter(0).value
        runNum_1 = self.getParameter(1).value
        runNum_2 = self.getParameter(2).value
        number_of_detectors = self.getParameter(3).value
        DetCalFilename = self.getParameter(4).value
        doSmoothing = self.getParameter(5).value
        pointsLeft = self.getParameter(6).value
        pointsRight = self.getParameter(7).value
        polyDegree = self.getParameter(8).value
        outPath = self.getParameter(9).value
        
        hom = 0.39559974    # Planck's constant divided by neutron mass
        
        # Open spectrum output file
        filename = outPath + 'Spectrum_' + runNum_1 + '_' + runNum_2 + '.dat'
        outFile = open( filename, 'w' )
        outFile.write('# Column  Unit    Quantity\n')
        outFile.write('# ------  ------  --------\n')
        outFile.write('#      1  us      time-of-flight\n')
        outFile.write('#      2  counts  counts per us corrected for vanadium rod absorption\n')
        outFile.write('#      3  A       wavelength\n')
        outFile.write('#      4  counts  counts per us uncorrected for absorption\n')
        outFile.write('#      5          transmission\n')
        outFile.write('#\n')
       
        
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
        
        # Read the detector calibration file
        DetCalFile = open(DetCalFilename, 'r')
        DetD = []
        CenterX = []
        CenterY = []
        CenterZ = []
        for line in DetCalFile:
            # print line
            lineList = line.split()
            # print lineList
            listLength = len( lineList )
            if listLength > 0:
                if lineList[0] == '7':
                    L1 = float( lineList[1] )
                    T0_shift = float( lineList[2] )
                if lineList[0] == '5':
                    DetD.append( float( lineList[7] ) )
                    CenterX.append( float( lineList[8] ) )
                    CenterY.append( float( lineList[9] ) )
                    CenterZ.append( float( lineList[10] ) )
        
        # Begin 'for' loop for each detector.

        for i in range(number_of_detectors):
            bank = i + 1
            print 'Detector Bank = %d' % bank
            filename = path + 'TOPAZ_' + runNum_1 + '.nxs'
            ds_1 = ScriptUtil.ExecuteCommand("OneDS",[filename, bank, ""] )
            filename = path + 'TOPAZ_' + runNum_2 + '.nxs'
            ds_2 = ScriptUtil.ExecuteCommand("OneDS",[filename, bank, ""] )

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
            

            # Correct for vanadium absorption
            
            # First store the spectrum in the time and counts arrays
            D = newspec.getData_entry(0)
            time = D.getX_scale().getXs()
            counts = D.getY_values()
            lenCounts = len( counts )
            
            # Calculate detector horizontal and vertical angles
            diagonal = sqrt( CenterX[i]**2 + CenterZ[i]**2 )
            angleH = atan( CenterX[i] / CenterZ[i] )  # the scattering angle in the horizontal plane
            angleV = atan( CenterY[i] / diagonal )  # angle out of the horizontal plane
            
            # loop through the spectrum to obtain corrected counts
            outFile.write( 'Bank %d\n' % bank )
            for j in range(lenCounts):
                wl = hom * time[j] / ( L1 + DetD[i] )  # wavelength
                transmission = absor_V_rod( angleH, angleV, wl )
                countsCorr = counts[j] / transmission
                outFile.write( ' %12.3f %12.3f %12.4f %12.3f %12.4f\n' \
                    % ( time[j], countsCorr, wl, counts[j], transmission ) )
                counts[j] = countsCorr
                    
            ScriptUtil.send(newspec, IOBS)
            ScriptUtil.display(newspec, "Selected Graph View")
            
            
        print 'The End!'
            
        
    def getCategoryList( self):      
        return ["Macros","Single Crystal"] 
        
    def __init__(self):
        Operator.__init__(self,"TOPAZ_spectrum")
        
        
