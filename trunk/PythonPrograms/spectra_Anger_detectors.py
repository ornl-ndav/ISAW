# spectra_Anger_detectors.py
""" Anger_spectrum uses Mantid algorithms rather than ISAW operators.
It is based on the ISAW script TOPAZ_SNAP_spectrum.py

A. J. Schultz
March, 2013

Version 2:
In version 1, detector data are loaded one at a time and processed.
In version 2, all the entire run with all the detectors are loaded at one time.


"""

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


import os
import sys

from time import time
start = time()

if os.path.exists('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary'):
    sys.path.append('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary')
else:
    sys.path.append('C:\ISAW_repo\PythonPrograms\PythonLibrary')
import ReduceDictionary

if os.path.exists("/opt/Mantid/bin"):
    sys.path.append("/opt/Mantid/bin")
    # sys.path.append("/opt/mantidnightly/bin")
else:
    sys.path.append("C:/MantidInstall/bin")

from mantid.simpleapi import *
from MantidFramework import mtd
mtd.initialise()

from math import *
import locale
locale.setlocale(locale.LC_ALL, '')

def absor_V_rod( angleH, angleV, wl, radius):
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
    # radius = 0.407  # radius of the vanadium rod used for TOPAZ, 0.15 for SNAP
    
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

def absor_V_sphere(twoth, wl, radius):
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
    # radius = 0.15  # radius of the V/Nb sphere used for TOPAZ
    
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
    
#
# Load the parameter names and values from the specified configuration file 
# into a dictionary and set all the required parameters from the dictionary.
#
params_dictionary = ReduceDictionary.LoadDictionary( 'spectra_Anger_detectors.config' )

raw_data_path             = params_dictionary[ "raw_data_path" ]
runNum_1                  = params_dictionary[ "runNum_1" ]
runNum_2                  = params_dictionary[ "runNum_2" ]
filename_prefix           = params_dictionary[ "filename_prefix" ]
filename_suffix           = params_dictionary[ "filename_suffix" ]
nBorder                   = int( params_dictionary[ "nBorder" ] )
DetCalFilename            = params_dictionary[ "DetCalFilename" ]
doSmoothing               = params_dictionary[ "doSmoothing" ]
numPoints                 = int( params_dictionary[ "numPoints" ] )
V_rod                     = params_dictionary[ "V_rod" ]
V_sphere                  = params_dictionary[ "V_sphere" ]
radius                    = float( params_dictionary[ "radius" ] )
outPath                   = params_dictionary[ "outPath" ]
omitZeros                 = params_dictionary[ "omitZeros" ]
min_tof                   = params_dictionary[ "min_tof" ]
max_tof                   = params_dictionary[ "max_tof" ]
rebin_step                = params_dictionary[ "rebin_step" ]

rebin_parameters = min_tof + "," + rebin_step + "," + max_tof

print ''
    
# Write input instructions to the log file.
filename = outPath + 'Spectrum_' + runNum_1 + '_' + runNum_2 + '.log'
logFile = open( filename, 'w' )
logFile.write('\n********** spectrum_Anger_detectors **********\n')
logFile.write('\nRaw data path: ' + raw_data_path)
logFile.write('\nRun number of data file: ' + runNum_1)
logFile.write('\nRun number of background file: ' + runNum_2)
logFile.write('\nDetCal file: ' + DetCalFilename)

if doSmoothing:
    logFile.write('\n\nApply smoothing? Yes')
    logFile.write('\n  Number of points to average: %d' % numPoints)
else:
    logFile.write('\n\nApply smoothing? No')

if V_rod:
    logFile.write('\n\nIs the spectrum from the vanadium rod? Yes')
    logFile.write('\n  Total scattering linear absorption coefficient: 0.367 cm^-1')
    logFile.write('\n  True absorption linear absorption coefficient: 0.366 cm^-1')
    logFile.write('\n  Radius of rod: %5.3f cm' % radius)
elif V_sphere:
    logFile.write('\n\nIs the spectrum from the V/Nb sphere? Yes')
    logFile.write('\n  Total scattering linear absorption coefficient: 0.367 cm^-1')
    logFile.write('\n  True absorption linear absorption coefficient: 0.366 cm^-1')
    logFile.write('\n  Radius of sphere: %5.3f cm' % radius)

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

#
# Load the run data and find the total monitor counts
#
# First load the vanadium data run file beam monitor spectrum
full_name = raw_data_path + filename_prefix + runNum_1 + filename_suffix 
monitor_ws_1 = 'monitor_' + runNum_1
integrated_monitor_ws_1 = 'integrated_monitor_' + runNum_1
LoadNexusMonitors( Filename = full_name, OutputWorkspace = monitor_ws_1 )
# LoadNexusMonitorsDialog()
Integration( InputWorkspace = monitor_ws_1, OutputWorkspace = integrated_monitor_ws_1,
             RangeLower = 500, RangeUpper = 16500, 
             StartWorkspaceIndex = 0, EndWorkspaceIndex = 0 )
monitor_count_1 = mtd[integrated_monitor_ws_1].dataY(0)[0]
print '\nMonitor counts for ' + runNum_1 + ' are ' + locale.format( '%d', monitor_count_1, grouping = True )
print ''
DeleteWorkspace(monitor_ws_1)
DeleteWorkspace(integrated_monitor_ws_1)


# Then load the no-sample background run file beam monitory spectrum
full_name = raw_data_path + filename_prefix + runNum_2 + filename_suffix
monitor_ws_2 = 'monitor_' + runNum_2
integrated_monitor_ws_2 = 'integrated_monitor_' + runNum_2
LoadNexusMonitors( Filename = full_name, OutputWorkspace = monitor_ws_2 )
Integration( InputWorkspace = monitor_ws_2, OutputWorkspace = integrated_monitor_ws_2,
             RangeLower = 500, RangeUpper = 16500, 
             StartWorkspaceIndex = 0, EndWorkspaceIndex = 0 )
monitor_count_2 = mtd[integrated_monitor_ws_2].dataY(0)[0]
print '\nMonitor counts for ' + runNum_2 + ' are ' + locale.format( '%d', monitor_count_2, grouping = True )
print ''
DeleteWorkspace(monitor_ws_2)
DeleteWorkspace(integrated_monitor_ws_2)

bkg_scaling_factor = monitor_count_1 / monitor_count_2
print '\nIncident beam monitor detector scale = %10.5f\n' % bkg_scaling_factor
logFile.write('\n\nScale factor from incident beam monitor detector = %10.5f' % bkg_scaling_factor)

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

# Load, sum and rebin the vanadium data
#
filename = raw_data_path + filename_prefix + runNum_1 + filename_suffix
event_ws1 = filename_prefix + runNum_1 + '_event'

wksp_1 = Load(Filename = filename, OutputWorkspace = event_ws1,
    FilterByTofMin = min_tof, FilterByTofMax = max_tof)
    # FilterByTofMin = 400, FilterByTofMax = 16650, BankName = bank_name)
    # FilterByTofMin = 400, FilterByTofMax = 16600, FilterByTimeStop = 3600, BankName = bank)
print '\nwksp_1 = %s\n' % wksp_1
# SmoothNeighbors will sum the time slice
wksp_1 = SmoothNeighbours(InputWorkspace = wksp_1, OutputWorkspace = 'SmoothNeighbors_1',
    SumPixelsX=256, SumPixelsY=256, ZeroEdgePixels = nBorder)
print '\nwksp_1 = %s\n' % wksp_1
wksp_1 = Rebin(InputWorkspace = wksp_1, OutputWorkspace = "Rebin_1", Params = rebin_parameters)
print '\nwksp_1 = %s\n' % wksp_1
# full_filename = outPath + 'vanadium_raw.dat'    
# SaveAscii(InputWorkspace = wksp_1, Filename = full_filename,
    # Separator = "Space", ColumnHeader = False)
DeleteWorkspace(event_ws1)
DeleteWorkspace('SmoothNeighbors_1')

        
# Load, sum and rebin the no-sample background data
#
filename = raw_data_path + filename_prefix + runNum_2 + filename_suffix 
event_ws2 = filename_prefix + runNum_2 + '_event'
wksp_2 = Load(Filename = filename, OutputWorkspace = event_ws2,
    FilterByTofMin = min_tof, FilterByTofMax = max_tof)
    # FilterByTofMin = 400, FilterByTofMax = 16650, BankName = bank_name)
print '\nwksp_2 = %s\n' % wksp_2
# SmoothNeighbors will sum the time slice
wksp_2 = SmoothNeighbours(InputWorkspace = wksp_2, OutputWorkspace = 'SmoothNeighbors_2',
    SumPixelsX = 256, SumPixelsY = 256, ZeroEdgePixels = nBorder)
print '\nwksp_2 = %s\n' % wksp_2
wksp_2 = Rebin(InputWorkspace = wksp_2, OutputWorkspace = "Rebin_2", Params = rebin_parameters)
print '\nwksp_2 = %s\n' % wksp_2
# full_filename = outPath + 'background_raw.dat'    
# SaveAscii(InputWorkspace = wksp_2, Filename = full_filename,
    # Separator = "Space", ColumnHeader = False)
DeleteWorkspace(event_ws2)
DeleteWorkspace('SmoothNeighbors_2')

#
#  Scale the background spectrum
wksp_2 = Scale(InputWorkspace = wksp_2, OutputWorkspace = 'ScaledBKG_2',
    Factor = bkg_scaling_factor, Operation = 'Multiply')
DeleteWorkspace('Rebin_2')
    
#
#  Subtract the background from the TiZr or V data to 
#  obtain the spectrum
spectrum = Minus(LHSWorkspace = wksp_1, RHSWorkspace = wksp_2,
    OutputWorkspace = 'Spectrum')
DeleteWorkspace(wksp_1)
DeleteWorkspace(wksp_2)
    
#  Convert counts to counts per microsecond
spectrum = Scale(InputWorkspace = 'Spectrum', OutputWorkspace = 'Spectrum',
    Factor = '0.1', Operation = 'Multiply')

#
# Do smoothing (averaging) if requested.
if doSmoothing:
    #  Mean average of NPoints
    print ""
    print "SmoothData is applied"
    print ""
    spectrum = SmoothData(InputWorkspace = spectrum, OutputWorkspace = spectrum,
        NPoints = numPoints)

#
# Correct for vanadium absorption
#  
full_filename = outPath + 'spectrum_no_abs.dat'    
SaveAscii(InputWorkspace = "Spectrum", Filename = full_filename,
    Separator = "Space", ColumnHeader = False)

print '\nBegin absorption correction.\n'
for i in range(number_of_detectors):
    bank = i + 1
    tof = []
    counts = []
    spec_no_abs = open(full_filename, 'r')
    ii = (2 * i) + 1
    for line in spec_no_abs:
        lineList = line.split()
        tof.append( float( lineList[0] ))
        counts.append( float( lineList[ii] ))
    lenCounts = len( counts )
    # print lenCounts
    
    # Check for all zeros
    sumTotal = 0.0
    for j in range(lenCounts):
        sumTotal = sumTotal + counts[j]
    if sumTotal == 0.0:
        print '***Bank %d  DetNum %d is all zeros and is not written to the spectrum file.' % (bank, DetNum[i])
        logFile.write( 'Bank %d     DetNum %d is all zeros and is not written to the spectrum file\n' % (bank, DetNum[i]) )
        if omitZeros: continue
    
    # Calculate detector horizontal and vertical angles
    diagonal = sqrt( CenterX[i]**2 + CenterZ[i]**2 )
    angleH = atan( CenterX[i] / CenterZ[i] )  # the scattering angle in the horizontal plane
    angleV = atan( CenterY[i] / diagonal )  # angle out of the horizontal plane
    
    # Calculate detector two-theta angle
    twoth = abs( acos( CenterZ[i] / DetD[i] ) )
    
    # loop through the spectrum to obtain corrected counts
    outFile.write( 'Bank %d     DetNum %d\n' % (bank, DetNum[i]) )
    logFile.write( 'Bank %d     DetNum %d\n' % (bank, DetNum[i]) )
    print 'Bank %d     DetNum %d' % (bank, DetNum[i])
    # for j in range(lenCounts):             ****************************
    for j in range(lenCounts - 1):
        wl = hom * tof[j] / ( L1 + DetD[i] )  # wavelength
        
        if V_rod:
            transmission = absor_V_rod( angleH, angleV, wl, radius )
        
        if V_sphere:
            transmission = absor_V_sphere( twoth, wl, radius )
        
        counts[j] = counts[j] / (tof[j+1] - tof[j])   # counts per microsecond
        countsCorr = counts[j] / transmission
        outFile.write( ' %12.3f %12.3f %12.4f %12.3f %12.4f\n' \
            % ( tof[j], countsCorr, wl, counts[j], transmission ) )
        # counts[j] = countsCorr
    
    spec_no_abs.close()


print '\nEnd of absorption correction.\n'  

os.remove( full_filename )
    
outFile.close()    
logFile.close()

print 'The End!'
            
        
        
