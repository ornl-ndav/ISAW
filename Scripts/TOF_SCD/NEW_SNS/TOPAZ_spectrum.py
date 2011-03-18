# TOPAZ_spectrum

# This version is stand-alone: it contains functions
# absor_V_rod and absor_V_sphere.
# A. J. Schultz, November, 2010

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
    pc = [ [ 1.0016,  1.6444,  1.6350,  0.2919,  0.5164 ],  #  0
           [ 0.9924,  1.7836,  1.2483,  0.6507,  0.3903 ],  #  5
           [ 0.9776,  2.0219,  0.5477,  1.3329,  0.1228 ],  # 10
           [ 0.9721,  2.1296,  0.1660,  1.7523, -0.1048 ],  # 15
           [ 0.9776,  2.0673,  0.2467,  1.7328, -0.2167 ],  # 20
           [ 0.9873,  1.9268,  0.5725,  1.4309, -0.2348 ],  # 25
           [ 0.9970,  1.7768,  0.9518,  1.0192, -0.2001 ],  # 30
           [ 1.0038,  1.6645,  1.2497,  0.6291, -0.1481 ],  # 35
           [ 1.0072,  1.6001,  1.4252,  0.3173, -0.0984 ],  # 40
           [ 1.0082,  1.5730,  1.4955,  0.0877, -0.0572 ],  # 45
           [ 1.0078,  1.5712,  1.4883, -0.0711, -0.0257 ],  # 50
           [ 1.0066,  1.5838,  1.4323, -0.1755, -0.0028 ],  # 55
           [ 1.0051,  1.6041,  1.3474, -0.2388,  0.0127 ],  # 60
           [ 1.0035,  1.6242,  1.2628, -0.2835,  0.0250 ],  # 65
           [ 1.0023,  1.6459,  1.1654, -0.2978,  0.0306 ],  # 70
           [ 0.9911,  1.7932,  0.8144, -0.1398,  0.0033 ],  # 75
           [ 1.0005,  1.6746,  1.0311, -0.3157,  0.0384 ],  # 80
           [ 1.0001,  1.6823,  0.9946, -0.3187,  0.0400 ],  # 85
           [ 0.9998,  1.6858,  0.9803, -0.3185,  0.0402 ] ] # 90


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
    pc = [ [ 0.9369,  2.1217, -0.1304,  1.1717 ],
           [ 0.9490,  2.0149,  0.0423,  1.0872 ],
           [ 0.9778,  1.7559,  0.4664,  0.8715 ],
           [ 1.0083,  1.4739,  0.9427,  0.6068 ],
           [ 1.0295,  1.2669,  1.3112,  0.3643 ],
           [ 1.0389,  1.1606,  1.5201,  0.1757 ],
           [ 1.0392,  1.1382,  1.5844,  0.0446 ],
           [ 1.0338,  1.1724,  1.5411, -0.0375 ],
           [ 1.0261,  1.2328,  1.4370, -0.0853 ],
           [ 1.0180,  1.3032,  1.2998, -0.1088 ],
           [ 1.0107,  1.3706,  1.1543, -0.1176 ],
           [ 1.0046,  1.4300,  1.0131, -0.1177 ],
           [ 0.9997,  1.4804,  0.8820, -0.1123 ],
           [ 0.9957,  1.5213,  0.7670, -0.1051 ],
           [ 0.9929,  1.5524,  0.6712, -0.0978 ],
           [ 0.9909,  1.5755,  0.5951, -0.0914 ],
           [ 0.9896,  1.5913,  0.5398, -0.0868 ],
           [ 0.9888,  1.6005,  0.5063, -0.0840 ],
           [ 0.9886,  1.6033,  0.4955, -0.0833 ] ]

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
        # self.addParameter(IntegerPG("Number of detectors:", 14))
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
        # number_of_detectors = self.getParameter(3).value
        DetCalFilename = self.getParameter(3).value
        doSmoothing = self.getParameter(4).value
        pointsLeft = self.getParameter(5).value
        pointsRight = self.getParameter(6).value
        polyDegree = self.getParameter(7).value
        V_rod = self.getParameter(8).value
        V_sphere = self.getParameter(9).value
        outPath = self.getParameter(10).value
        
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
        print 'incident beam monitor detector scale = %10.5f' % scale
        logFile.write('\n\nScale factor from incident beam monitor detector = %10.5f' % scale)
        scalePC = protonCharge_1 / protonCharge_2
        print 'proton_charge ratio = %10.5f' % scalePC
        logFile.write('\nScale factor from proton_charge = %10.5f' % scalePC)

        # Read the detector calibration file
        DetCalFile = open(DetCalFilename, 'r')
        number_of_detectors = 0
        DetNum = []
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
        print 'The End!'
            
        
    def getCategoryList( self):      
        return ["Macros","Single Crystal"] 
        
    def __init__(self):
        Operator.__init__(self,"TOPAZ_spectrum")
        
        
