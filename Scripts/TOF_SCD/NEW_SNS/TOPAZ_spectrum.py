# TOPAZ_spectrum

# This version is stand-alone: it contains functions
# absor_V_rod and absor_V_sphere.
# A. J. Schultz, November, 2010

#  Script to obtain spectrum for each detector (Bank) and
#  write output to ASCII files. Input is a data file of
#  vanadium or TiZr, and a background file.

#  Based on TOPAZ_spectrum_multiple_banks.iss

#  Jython version:  A. J. Schultz, August 2010

#  Added option to exclude border pixels.
#  A. J. Schultz, April 2011

from DataSetTools.operator.DataSet.Math.DataSet import *
from DataSetTools.operator.DataSet.Math.Scalar import *
from Command import *
from Operators.Generic.Save.SaveASCII_calc import *
from gov.anl.ipns.Util.SpecialStrings import AttributeNameString
from math import *
# from absor_V_rod import *

def absor_V_rod( angleH, angleV, wl):
    "Returns the absorption correction for a vanadium cylinder."
    #--------------------------------------------------------
    #               function absor_V_rod
    #--------------------------------------------------------
    # Function to calculate the absorption and of a vanadium rod.
    #--------------------------------------------------------
    #   A.J. Schultz,   September, 2010
    #--------------------------------------------------------
    # Subroutine to calculate the absorption correction
    # for a vanadium rod. Based on values for cylinders in:
    # 
    # C. W. Dwiggins, Jr., Acta Cryst. A31, 146 (1975).
    # 
    # In this paper, A is the transmission and A* = 1/A is
    # the absorption correction.
    # 
    # For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
    # in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a fourth
    # order polynomial in Excel. These values are given below in the
    # data statement. (For a sphere, third order polynomials were
    # sufficient, but not for the cylinder.)

    # pc = fourth order polynomial coefficients             # theta:
    pc = [ [ 1.000,   1.6516,   1.6251,   0.2971,   0.5155 ],
           [ 1.000,   1.7483,   1.2967,   0.6254,   0.3947 ],
           [ 1.000,   1.9176,   0.6903,   1.2585,   0.1359 ],
           [ 1.000,   1.9995,   0.3439,   1.6594,  -0.0884 ],
           [ 1.000,   1.9627,   0.3897,   1.6581,  -0.2035 ],
           [ 1.000,   1.8675,   0.6536,   1.3886,  -0.2274 ],
           [ 1.000,   1.7628,   0.9709,   1.0092,  -0.1983 ],
           [ 1.000,   1.6821,   1.2256,   0.6416,  -0.1503 ],
           [ 1.000,   1.6336,   1.3794,   0.3412,  -0.1026 ],
           [ 1.000,   1.6114,   1.4430,   0.1151,  -0.0620 ],
           [ 1.000,   1.6075,   1.4387,  -0.0451,  -0.0303 ],
           [ 1.000,   1.6146,   1.3900,  -0.1534,  -0.0067 ],
           [ 1.000,   1.6278,   1.3150,  -0.2219,   0.0097 ],
           [ 1.000,   1.6406,   1.2405,  -0.2719,   0.0230 ],
           [ 1.000,   1.6566,   1.1508,  -0.2901,   0.0293 ],
           [ 1.000,   1.7516,   0.8713,  -0.1695,   0.0086 ],
           [ 1.000,   1.6771,   1.0277,  -0.3139,   0.0380 ],
           [ 1.000,   1.6826,   0.9942,  -0.3185,   0.0399 ],
           [ 1.000,   1.6851,   0.9814,  -0.3190,   0.0403 ] ]


    # From Structure of Metals by Barrett and Massalksi:
    #                vanadium is b.c.c, a = 3.0282
    # V = 27.769, Z = 2
    # From lin_abs_coef in ISAW:
    smu = 0.367     # linear absorption coeff. for total scattering in cm^-1
    amu = 0.366     # linear absorption coeff. for true absorption at 1.8 A in cm^-1
    radius = 0.407  # radius of the vanadium rod used for TOPAZ
    
    mu = smu + (amu/1.8)*wl

    muR = mu*radius
    
    theta = (angleH*180.0/pi)/2.0   # theta is the theta angle in the horizontal plane

    # ! Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
    # ! theta values below and above the actual theta value.

    i = int(theta/5.0)
    astar1 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    i = i+1
    astar2 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    # !	Do a linear interpolation between theta values.

    frac = (theta%5.0)/5.0

    astar = astar1*(1-frac) + astar2*frac	# astar is the correction

    trans1 = 1.0/astar	                        # trans is the transmission
                                                # trans = exp(-mu*tbar)

    # !	Calculate TBAR as defined by Coppens.

    tbar1 = -log(trans1)/mu

    # Calculate total path length and transmission for scattered
    # beam out of the horizontal plane.
    
    tbar2 = tbar1 / cos( angleV )  # path length
    trans2 = exp( -mu * tbar2 )    # transmission
    
    return trans2

def absor_V_sphere(twoth, wl):
    "Returns the absorption correction for a vanadium sphere."

    #--------------------------------------------------------
    #               function absor_sphere
    #--------------------------------------------------------
    # Function to calculate the absorption and  of a sherical
    # crystal.
    #--------------------------------------------------------
    # Jython version:
    #   A.J. Schultz,   November, 2009
    #--------------------------------------------------------
    # Comments from the Fortran source code in anvredSNS.f:
    # !	Subroutine to calculate a spherical absorption correction
    # !	and tbar. Based on values in:
    # !
    # !	C. W. Dwiggins, Jr., Acta Cryst. A31, 395 (1975).
    # !
    # !	In this paper, A is the transmission and A* = 1/A is
    # !	the absorption correction.
    #
    # !	Input are the smu (scattering) and amu (absorption at 1.8 Ang.)
    # !	linear absorption coefficients, the radius R of the sample
    # !	the theta angle and wavelength.
    # !	The absorption (absn) and tbar are returned.
    #
    # !	A. J. Schultz, June, 2008
    #			
    #	real mu, muR	!mu is the linear absorption coefficient,
    #			!R is the radius of the spherical sample.
    #	
    # !	For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
    # !	in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a third
    # !	order polynomial in Excel. These values are given below in the
    # !	data statement.
    
    # pc = third order polynomial coefficients
    pc = [ [ 1.0000,  1.9368,  0.0145,  1.1386 ],
           [ 1.0000,  1.8653,  0.1596,  1.0604 ],
           [ 1.0000,  1.6908,  0.5175,  0.8598 ],
           [ 1.0000,  1.4981,  0.9237,  0.6111 ],
           [ 1.0000,  1.3532,  1.2436,  0.3798 ],
           [ 1.0000,  1.2746,  1.4308,  0.1962 ],
           [ 1.0000,  1.2530,  1.4944,  0.0652 ],
           [ 1.0000,  1.2714,  1.4635, -0.0198 ],
           [ 1.0000,  1.3093,  1.3770, -0.0716 ],
           [ 1.0000,  1.3559,  1.2585, -0.0993 ],
           [ 1.0000,  1.4019,  1.1297, -0.1176 ],
           [ 1.0000,  1.4434,  1.0026, -0.1153 ],
           [ 1.0000,  1.4794,  0.8828, -0.1125 ],
           [ 1.0000,  1.5088,  0.7768, -0.1073 ],
           [ 1.0000,  1.5317,  0.6875, -0.1016 ],
           [ 1.0000,  1.5489,  0.6159, -0.0962 ],
           [ 1.0000,  1.5608,  0.5637, -0.0922 ],
           [ 1.0000,  1.5677,  0.5320, -0.0898 ],
           [ 1.0000,  1.5700,  0.5216, -0.0892 ] ]

    # From Structure of Metals by Barrett and Massalksi:
    #                vanadium is b.c.c, a = 3.0282
    # V = 27.769, Z = 2
    # From lin_abs_coef in ISAW:
    smu = 0.367  # linear absorption coeff. for total scattering in cm_1
    amu = 0.366  # linear absorption coeff. for true absorption at 1.8 A in cm^-1
    radius = 0.15  # radius of the V/Nb sphere used for TOPAZ
    
    mu = smu + (amu/1.8)*wl

    muR = mu*radius
    
    theta = (twoth*180.0/pi)/2.0
    
    # !	Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
    # !	theta values below and above the actual theta value.

    i = int(theta/5.0)
    astar1 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    i = i+1
    astar2 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    # !	Do a linear interpolation between theta values.

    frac = (theta%5.0)/5.0
    astar = astar1*(1-frac) + astar2*frac	# astar is the correction
    trans = 1.0/astar	                        # trans is the transmission
                                                # trans = exp(-mu*tbar)

    
    return trans
    
    
class TOPAZ_spectrum(GenericTOF_SCD):

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Raw data path:", "/SNS/users/ajschultz/spectrum/TOPAZ_2503_2502_sphere/"))
        self.addParameter(StringPG("Run number of data file:", "2503"))
        self.addParameter(StringPG("Run number of background file:", "2502"))
        self.addParameter(IntegerPG("Number of border channels to exclude:", 16))
        self.addParameter(LoadFilePG("DetCal file:", "/SNS/users/ajschultz/DetCal/TOPAZ_2011_02_16.DetCal"))
        self.addParameter(BooleanEnablePG("Apply Savitzky-Golay smoothing Filter?", "[1,3,0]"))
        self.addParameter(IntegerPG("Number of points to the left of center:", 20))
        self.addParameter(IntegerPG("Number of points to the right of center:", 20))
        self.addParameter(IntegerPG("Degree of smoothing polynomial:", 3))
        self.addParameter(BooleanEnablePG("Is the spectrum from the vanadium rod?", "[0,0,1]"))
        self.addParameter(BooleanPG("Or is the spectrum from the V/Nb sphere?", "True"))
        self.addParameter(DataDirPG("Directory for output spectrum and log files:", "/SNS/users/ajschultz/spectrum/TOPAZ_2503_2502_sphere/"))
        
    def getResult(self):

        path = self.getParameter(0).value
        runNum_1 = self.getParameter(1).value
        runNum_2 = self.getParameter(2).value
        nBorder = self.getParameter(3).value
        DetCalFilename = self.getParameter(4).value
        doSmoothing = self.getParameter(5).value
        pointsLeft = self.getParameter(6).value
        pointsRight = self.getParameter(7).value
        polyDegree = self.getParameter(8).value
        V_rod = self.getParameter(9).value
        V_sphere = self.getParameter(10).value
        outPath = self.getParameter(11).value
        
        # Write input instructions to the log file.
        filename = outPath + 'Spectrum_' + runNum_1 + '_' + runNum_2 + '.log'
        logFile = open( filename, 'w' )
        logFile.write('\n********** TOPAZ_spectrum **********\n')
        logFile.write('\nRaw data path: ' + path)
        logFile.write('\nRun number of data file: ' + runNum_1)
        logFile.write('\nRun number of background file: ' + runNum_2)
        # logFile.write('\nNumber of detectors: %d' % number_of_detectors)
        logFile.write('\nDetCal file: ' + DetCalFilename)
        
        if doSmoothing:
            logFile.write('\n\nApply Savitzky-Golay smoothing Filter? Yes')
            logFile.write('\n  Number of points to the left of center: %d' % pointsLeft)
            logFile.write('\n  Number of points to the right of center: %d' % pointsRight)
            logFile.write('\n  Degree of smoothing polynomial: %d' % polyDegree)
        else:
            logFile.write('\n\nApply Savitzky-Golay smoothing Filter? No')
            
        if V_rod:
            logFile.write('\n\nIs the spectrum from the vanadium rod? Yes')
            logFile.write('\n  Total scattering linear absorption coefficient: 0.367 cm^-1')
            logFile.write('\n  True absorption linear absorption coefficient: 0.366 cm^-1')
            logFile.write('\n  Radius of rod: 0.407 cm')
        elif V_sphere:
            logFile.write('\n\nIs the spectrum from the V/Nb sphere? Yes')
            logFile.write('\n  Total scattering linear absorption coefficient: 0.367 cm^-1')
            logFile.write('\n  True absorption linear absorption coefficient: 0.366 cm^-1')
            logFile.write('\n  Radius of sphere: 0.15 cm')
        
        logFile.write('\n\nDirectory for output spectrum and log files: ' + outPath)
                
        hom = 0.39559974    # h over m: Planck's constant divided by neutron mass
        
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
        print 'incident beam monitor detector scale = %10.5f' % scale
        logFile.write('\n\nScale factor from incident beam monitor detector = %10.5f' % scale)
        scalePC = protonCharge_1 / protonCharge_2
        print 'proton_charge ratio = %10.5f' % scalePC
        logFile.write('\nScale factor from proton_charge = %10.5f' % scalePC)

        # Read the detector calibration file
        DetCalFile = open(DetCalFilename, 'r')
        number_of_detectors = 0
        DetNum = []
        nRows = []
        nCols = []
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
                    DetNum.append( int( lineList[1] ) )
                    nRows.append( int( lineList[2] ) )
                    nCols.append( int( lineList[3] ) )
                    DetD.append( float( lineList[7] ) )
                    CenterX.append( float( lineList[8] ) )
                    CenterY.append( float( lineList[9] ) )
                    CenterZ.append( float( lineList[10] ) )
                    number_of_detectors = number_of_detectors + 1
        logFile.write('\n\nNumber of detectors = %d\n\n' % number_of_detectors)
        
        # Begin 'for' loop for each detector.

        for i in range(number_of_detectors):
#!        for i in range(12, number_of_detectors):
            bank = i + 1
            print 'Detector Bank = %d     DetNum = %d' % (bank, DetNum[i])
            filename = path + 'TOPAZ_' + runNum_1 + '.nxs'
#!            print filename
#!            ds_1 = ScriptUtil.ExecuteCommand("OneDS",[filename, DetNum[i], ""] )
            ds_1 = ScriptUtil.ExecuteCommand("OneDS",[filename, bank, ""] )
            filename = path + 'TOPAZ_' + runNum_2 + '.nxs'
#!            ds_2 = ScriptUtil.ExecuteCommand("OneDS",[filename, DetNum[i], ""] )
            ds_2 = ScriptUtil.ExecuteCommand("OneDS",[filename, bank, ""] )

            #
            #  Select the pixels that were requested
            #
            #  First be sure any previously selections are cleared 
            #
            ClearSelect(ds_1).getResult()
            ClearSelect(ds_2).getResult()
            #
            #
            if nBorder == 0:
                # all pixels are included in the spectrum
                SelectByIndex(ds_1, "0:65535", "Set Selected").getResult()
                SelectByIndex(ds_2, "0:65535", "Set Selected").getResult()
            else:
                # border channels are excluded
                first_col = nBorder
                last_col = nCols[i] - nBorder
                first_row = nBorder
                last_row = nRows[i] - nBorder
                
                for col in range(first_col,(last_col+1)):
                    first_index = (col-1)*nCols[i] + first_row-1
                    last_index  = (col-1)*nCols[i] + last_row-1
                    range_string = str(first_index) + ':' + str(last_index)
                    SelectByIndex( ds_1, range_string, "Set Selected" ).getResult()
                    SelectByIndex( ds_2, range_string, "Set Selected" ).getResult()

            
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
            
            # Calculate detector two-theta angle
            twoth = abs( acos( CenterZ[i] / DetD[i] ) )
            
            # loop through the spectrum to obtain corrected counts
            outFile.write( 'Bank %d     DetNum %d\n' % (bank, DetNum[i]) )
            logFile.write( 'Bank %d     DetNum %d\n' % (bank, DetNum[i]) )
            for j in range(lenCounts):
                wl = hom * time[j] / ( L1 + DetD[i] )  # wavelength
                
                if V_rod:
                    transmission = absor_V_rod( angleH, angleV, wl )
                
                if V_sphere:
                    transmission = absor_V_sphere( twoth, wl )
                
                countsCorr = counts[j] / transmission
                outFile.write( ' %12.3f %12.3f %12.4f %12.3f %12.4f\n' \
                    % ( time[j], countsCorr, wl, counts[j], transmission ) )
                counts[j] = countsCorr
                    
            ScriptUtil.send(newspec, IOBS)
            ScriptUtil.display(newspec, "Selected Graph View")
            
        outFile.close()    
        logFile.close()
        
        return 'The End!'
            
        
    def getCategoryList( self):      
        return ["Macros","Single Crystal"] 
        
    def __init__(self):
        Operator.__init__(self,"TOPAZ_spectrum")
        
        
