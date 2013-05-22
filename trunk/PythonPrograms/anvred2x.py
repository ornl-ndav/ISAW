#--------------------------------------------------------------------
#                             anvred2x.py
#
# Does not run from Isaw. User input read from anvred2x.inp file.
#   A. J. Schultz, February 2012
#--------------------------------------------------------------------
#
# Data reduction program:
#   Input is raw integrated intensities.
#   Output is relative Fsq's.
#
# Jython version:
#    A. J. Schultz, started December 2009
#
# anvred_py.py
#    Each spectrum is a separate file. The spectra files are created
#    by "TOPAZ_spectrum_multiple_banks.iss".
#
# anvred2.py:
#    This version reads one spectrum file containing spectra for
#    each detector. The spectra are created by "TOPAZ_spectrum.py".
#
# Modfications by Xiaoping Wang, April 2011
# Added Selection of neutron wavelengths limits wlMin, wlMax
# Omit zero intensity peaks in integrate file XP Wang 03/21/2011
# Changed to >=0 and used absolute value for minium I/sing(I) = 0  XP Wang 02/24/2011
#
#
# 
# Comments from Fortran source:
# C**************************   ANVRED  ******************************
# C
# C ARGONNE NATIONAL LABORATORY VARIABLE WAVELENGTH DATA REDUCTION PROGRAM
# C
# C		Major contributions from:
# C			P. C. W. Leung
# C			A. J. Schultz
# C			R. G. Teller
# C			L. R. Falvello
# C
# C     The data output by this program are corrected  for  variations  in
# C  spectral distribution, variations in detector efficiency  across  the
# C  face  of  the  detector,  and the pertinent geometric factors such as
# C  (SIN(THETA))**2 and LAMBDA**4.
# C
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !	Linux version:	A. Schultz   January, 2003                    !
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# !	Version 4:		A. Schultz		June, 2003
# !		1. Process data from two detectors
# !		2. Does not use an x-file.
# !		3. Gets MONCNT from integrate file. If zero, sets CMONX = 1.0.
# !		4. Corrected ALPHAP for calculation of SPECT1.
#
# !	Version 5:		A. Schultz		July, 2003
# !		This version outputs a expnam.hkl file which can be input
# !		into SHELX with HKL 2.
# !	Version 5a:
# !		Cleaned-up and removed a lot of unused code.
# !		Added a test for dmin.
# !
# !	Version 6:		L. Falvello		January, 2004
# !		Polyhedral absorption correction with two detectors.
# !
# !	Version 7:		A. Schultz		2007
# !		Use spectrum obtained from each SCD detector
# !
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !	ANVRED_SNS:		A. Schultz		2008                                     !
# !		Process SNS data.                                                    !
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !	ANVRED_SNS_v2:		A. Schultz		June, 2008
# !		New spherical absorption correction. Removed all
# !		of the old correction code.
# !	ANVRED_SNS-v2.1: read detector parameters from integrate file.
# !	ANVRED_SNS-v2.2: get filename for spectrum file.
# !       ANVRED_SNS-v2.3: everything included in one file.  8/17/2008
# !       anvredSNS_2.4: made compatible with gfortran including removal
# !                      of FREIN3 and READ133.         10/8/2008
# !	anvredSNS_2.5: the datacom_SNS.inc file is no longer used. Cleaned
# !			up the code using ftnchek.    10/13/08
# !	anvredSNS_2.6: assign a common scale factor for each
# !			crystal setting, or for each detector. 1/29/09
# !
# !	4/13/09	Number of possible spectra increased from 2 to 100.
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import os
import sys

if os.path.exists('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary'):
    sys.path.append('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary')
    sys.path.append('/SNS/software/ISAW/PythonSources/Lib')
elif os.path.exists('/SNS/MANDI/shared/PythonPrograms/PythonLibrary'):
    sys.path.append('/SNS/MANDI/shared/PythonPrograms/PythonLibrary')
    sys.path.append('/SNS/software/ISAW/PythonSources/Lib')
else:
    sys.path.append('C:\ISAW_repo\PythonPrograms\PythonLibrary')
    
from readrefl_header import *
from readrefl_SNS import *
from readSpecCoef import *
from spectrumCalc import *
from spectrum2 import *
from absor_sphere import *

# Reader user input parameters
user_input = open('anvred2x.inp', 'r')
parameters = []
while True:
    lineString = user_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    parameters.append(lineList[0])
    
directory_path = parameters[0]
expName = parameters[1]
smu = float(parameters[2])
amu = float(parameters[3])
radius = float(parameters[4])
iSpec = int(parameters[5])
specCoeffFile = parameters[6]
spectraFile = parameters[7]
normToWavelength = float(parameters[8])
minIsigI = float(parameters[9])
numBorderCh = int(parameters[10])
intiMin = float(parameters[11])
dMin = float(parameters[12])
iIQ = int(parameters[13])
scaleFactor = float(parameters[14])
wlMin = float(parameters[15])   # XP Wang 02/24/2011
wlMax = float(parameters[16])   # XP Wang 02/24/2011

# open the anvred.log file in the working directory
fileName = directory_path + 'anvred2.log'
logFile = open( fileName, 'w' )

# open the hkl file in the working directory
hklFileName = directory_path + expName + '.hkl'
hklFile = open( hklFileName, 'w' )

# echo the input in the log file
logFile.write('\n********** anvred **********\n')
logFile.write('\nWorking directory: ' + directory_path)
logFile.write('\nExperiment name: ' + expName + '\n')

logFile.write('\nTotal scattering linear absorption coefficient: %6.3f cm^-1' % smu )
logFile.write('\nTrue absorption linear absorption coefficient: %6.3f cm^-1' % amu )
logFile.write('\nRadius of spherical crystal: %6.3f cm\n' % radius )

logFile.write('\nIncident spectrum and detector efficiency correction.')
logFile.write('\n    iSpec = 1. Spectrum fitted to 11 coefficient GSAS Type 2 function')
logFile.write('\n    iSpec = 0. Spectrum data read from a spectrum file.')
logFile.write('\niSpec: %i\n' % iSpec)

if iSpec == 1:   # spectrum is fitted to equation with 12 coefficients
    logFile.write('\nFile with spectrum coefficients: ' + specCoeffFile + '\n' )
    
if iSpec == 0:   # spectrum is read as TOF vs. counts
    logFile.write('\nFile with spectra: ' + spectraFile + '\n' )

logFile.write('\nNormalize spectra to a wavelength of %4.2f' % normToWavelength)
logFile.write('\nThe minimum I/sig(I) ratio: %i' % minIsigI )
logFile.write('\nWidth of border: %i channels' % numBorderCh )
logFile.write('\nMinimum integrated intensity: %i' % intiMin )
logFile.write('\nMinimum d-spacing : %4.2f Angstroms\n' % dMin )

logFile.write('\nScale factor identifier:' )
logFile.write('\n     IQ = 1. Scale factor per crystal setting.' )
logFile.write('\n     IQ = 2. Scale factor for each detector in each setting.')
logFile.write('\n     IQ = 3. Scale factor for each detector for all settings.')
logFile.write('\nIQ: %i\n' % iIQ )

logFile.write('\nMultiply FSQ and sig(FSQ) by: %f\n' % scaleFactor )

logFile.write('\nMinimum wavelength: %f\n' % wlMin )
logFile.write('Maximum wavelength: %f\n' % wlMax )

# C
# C  CHECK ON THE EXISTANCE OF THE integrate FILE
# C
fileName = directory_path + expName + '.integrate'
integFile = open(fileName, 'r')

# !  Initial read of integrate file to get instrument and detectors calibration.
calibParam = readrefl_header( integFile )
L1 = float(calibParam[0])       # initial flight path length in cm
t0_shift = float(calibParam[1]) # t-zero offest in microseconds
nod = int(calibParam[2])    # number of detectors
print '********** nod = ', nod, '\n'

logFile.write('\nInitial flight path length: %10.4f cm' % L1 )
logFile.write('\nT-zero offset: %8.3f microseconds' % t0_shift )
logFile.write('\nNumber of detectors: %i' % nod )

# Initial values.
transmin = 1.0
transmax = 0.0
hom = 0.39559974    # Planck's constant divided by neutron mass

# Read spectrum coefficients if iSpec = 1
if iSpec == 1:
    specInput = open( specCoeffFile, 'r')
    # pj is a list of lists with dimensions (nod, 11)
    pj = readSpecCoef(specInput, logFile, nod)
    
# Read spectrum for each detector bank if iSpec = 0
if iSpec == 0:
    # spectra is an array of arrays containing the spectra in the
    # Spectrum_run1_run2.dat file.
    specInput = open( spectraFile, 'r' )
    
    for i in range(8):   # skip the first 8 lines
        lineString = specInput.readline()
    
    # "spectra" is an array spectra[i][j] where i is the number
    # of the detector bank starting at zero, and j = 0 for
    # the array of times and j = 1 for the array of counts
    spectra = []
    
    lineString = specInput.readline()   # read "Bank 1" line
    
    for i in range( nod ):
        # set arrays to zero
        time = []
        counts = []
        
        print 'Reading spectrum for ' + lineString,
        while True:
            lineString = specInput.readline()
            lineList = lineString.split()
            if len(lineList) == 0: break
            if lineList[0] == 'Bank': break
            time.append( float( lineList[0] ) )
            counts.append( float( lineList[1] ) )
            
        spectra.append( [time, counts] )
    
    specInput.close()
    

# C-----------------------------------------------------------------------
# C  Calculate spectral correction at normToWavelength to normalize
# C  spectral correction factors later on.
spect1 = []     # spectrum value at normToWavelength for each detector
dist = []       # sample-to-detector distance
xtof = []       # = (L1+dist)/hom; TOF = wl * xtof

wavelength = normToWavelength
one = 1.0       # denominator in spectrum to calculate spect1

for id in range(nod):


    if iSpec == 1:  # The spectrum is calculated from coefficients
        
        spect = spectrumCalc(wavelength, calibParam, pj, id)
        spect1.append(spect)
        
    else:           # iSpec = 2           
        
        dist.append(calibParam[9][id])
        xtof.append((L1 + dist[id]) / hom)
        
        # spectra[id][0] are the times-of-flight
        # spectra[id][1] are the counts
        spectx = spectrum2( wavelength, xtof[id], \
            one, spectra[id][0], spectra[id][1] )
        spect = spectx[0]            # the spectral normalization parameter
        relSigSpect = spectx[1]      # the relative sigma of spect
        if spect == 0.0:
            print '*** Wavelength for normalizing to spectrum is out of range.'
        spect1.append(spect)
                          
# C-----------------------------------------------------------------------

# C
# C  SET THE CURRENT HISTOGRAM NUMBER TO 0 AND INITIALIZE THE MONITOR COUN
# C
curhst = 0
idet = 0
hstnum = 0
cmon = 100e+6
ncntr = 0      #!Number of processed reflections

nrun = 0
dn = 0
chi = 0.0
phi = 0.0
omega = 0.0
moncnt = 1000000.
eof = 999

# C
# C   SET UP LOOP TO PROCESS THE REFLECTION DATA
# C
while True:

    peak = readrefl_SNS( integFile, eof, nrun, dn, chi, phi, omega,\
        moncnt)
    eof = peak[22]
    if eof == 0: break
    
    nrun = peak[0]
    dn = peak[1]
    chi = float( peak[2] )
    phi = float( peak[3] )
    omega = float( peak[4] )
    moncnt = peak[5]
    seqnum = peak[6]
    h = peak[7]
    k = peak[8]
    l = peak[9]
    col = peak[10]
    row = peak[11]
    chan = peak[12]
    L2 = peak[13]
    twoth = peak[14]  # radians
    az = peak[15]  # azimuthal angle in radians
    wl = peak[16]
    dsp = peak[17]
    ipkobs = peak[18]
    inti = peak[19]
    sigi = abs(peak[20])
    reflag = peak[21]
    

    # set-up for new run or detector
    if nrun != curhst or dn != idet:
        if nrun != curhst:
            curhst = nrun
            if iIQ != 2: hstnum = hstnum + 1
            
        idet = dn  #IDET and DN is the arbitrary detector number.
                   #ID is a sequential number in the order they are listed.
     
        for id in range(nod):
            detNum = calibParam[3][id]
            if detNum == dn: break
            
        if iIQ == 2: hstnum = hstnum + 1
        
        mnsum = moncnt
        
        if mnsum == 0:
            cmonx = 1.0
        else:
            cmonx = cmon / mnsum
            if cmonx == 0: cmonx = 1.0
        
        logFile.write('\n\nHISTOGRAM NUMBER %5d' % nrun)      
        logFile.write('\nDETECTOR BANK NUMBER %2d     DETECTOR SEQUENTIAL NUMBER %2d'\
            % (dn, id))
        logFile.write('\nANGLES ARE CHI =%7.2f   PHI =%7.2f   OMEGA=%7.2f\n'\
            % ( chi, phi, omega ))                        
        logFile.write('TOTAL MONITOR COUNTS ELAPSED%10d   CMONX =%10.4f\n'\
            % ( mnsum, cmonx ))
        logFile.write('* DATA SCALED TO 100 MILLION MONITOR COUNTS *\n')
        logFile.write('CORREC = SCALEFACTOR * CMONX * SINSQT /' + \
            '( SPECT * (DET EFF) * WL4 * ABTRANS )\n')
        logFile.write('\n    H   K   L       FSQ     SIG     WL      INTI' + \
            '    SIG   SPECT  SINSQT  ABTRANS   TBAR\n')
    # end of set-up for new run or detector
   
    # Omit zero intensity peaks from integrate file XP Wang 03/21/2011
    # Changed to >=0 and absolute value  XP Wang 02/24/2011
    if inti == 0.0 :
        logFile.write(' %4d%4d%4d *** intI = 0.0 \n' \
            % (h, k, l))
        continue  

    if minIsigI >= 0 and inti < abs(minIsigI * sigi):
        logFile.write(' %4d%4d%4d *** inti < (minIsigI * sigi) \n' \
            % (h, k, l))
        continue
        
    if inti < intiMin:
        logFile.write(' %4d%4d%4d *** inti < intiMin \n' \
            % (h, k, l))
        continue

    # Set-up limits for neutron wavelentgh XP Wang 02/24/2011
    if wl < wlMin:
        logFile.write(' %4d%4d%4d *** wl < wlMin \n' \
            % (h, k, l))
        continue

    if wl > wlMax:
        logFile.write(' %4d%4d%4d *** wl > wlMax \n' \
            % (h, k, l))
        continue

    nRows = calibParam[4][id]
    nCols = calibParam[5][id]
    
    if col < numBorderCh:
        logFile.write(' %4d%4d%4d *** col < numBorderCh \n' \
            % (h, k, l))
        continue
        
    if col > (nCols - numBorderCh):
        logFile.write(' %4d%4d%4d *** col > (nCols - numBorderCh)\n' \
            % (h, k, l))
        continue
        
    if row < numBorderCh:
        logFile.write(' %4d%4d%4d *** row < numBorderCh \n' \
            % (h, k, l))
        continue
        
    if row > (nRows - numBorderCh):
        logFile.write(' %4d%4d%4d *** row > (nRows - numBorderCh)\n' \
            % (h, k, l))
        continue
                        
    if dsp < dMin:
        logFile.write(' %4d%4d%4d *** dsp < dMin \n' \
            % (h, k, l))
        continue
    
    ncntr = ncntr + 1
    
    if iSpec == 1:
        spect = spectrumCalc(wl, calibParam, pj, id)
        spect = spect / spect1[id]
    
    if iSpec == 0:
        spectx = spectrum2( wl, xtof[id], \
          spect1[id], spectra[id][0], spectra[id][1] )
        spect = spectx[0]
        relSigSpect = spectx[1]
    if spect == 0.0:
        logFile.write(' %4d%4d%4d *** spect == 0.0 \n' \
            % (h, k, l))
        continue
    
    # correct for the slant path throught the scintillator glass
    mu = (9.614 * wl) + 0.266    # mu for GS20 glass
    depth = calibParam[8][id]
    eff_center = 1.0 - exp(-mu * depth)  # efficiency at center of detector
    cosA = dist[id] / L2
    pathlength = depth / cosA
    eff_R = 1.0 - exp(-mu * pathlength)   # efficiency at point R
    sp_ratio = eff_center / eff_R  # slant path efficiency ratio
    
    sinsqt = ( wl / (2.0*dsp) )**2
    wl4 = wl**4
        
    correc = scaleFactor * sinsqt * cmonx * sp_ratio / (wl4 * spect )
        
    # absorption correction
    # trans[0] is the transmission
    # trans[1] is tbar
    trans = absor_sphere(smu, amu, radius, twoth, wl)
    
    transmission = trans[0]
    if trans[0] < transmin: transmin = trans[0]
    if trans[0] > transmax: transmax = trans[0]
    
    correc = correc / trans[0]
    
    fsq = inti * correc

    sigfsq = sigi * correc
    
    # Add normalization error to sigma
    sigfsq = sqrt( sigfsq**2 + (relSigSpect*fsq)**2 )  # not sure if last term is squared
    
    # tbar is the Coppen's tbar
    tbar = trans[1]
    
    # output reflection to log file and to hkl file
    logFile.write(' %4d%4d%4d%10.2f%8.2f%7.3f%10.2f%8.2f%8.4f%8.4f%8.4f%8.4f\n' \
        % (h, k, l, fsq, sigfsq, wl, inti, sigi, spect, sinsqt, trans[0], tbar))
    
    hklFile.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d%9.5f%9.4f\n' \
        % (h, k, l, fsq, sigfsq, hstnum, wl, tbar, curhst, seqnum, transmission, dn, twoth, dsp))
        
print '\nMinimum and maximum transmission = %6.4f, %6.4f\n' % (transmin, transmax)

logFile.write('\n\n***** Minimum and maximum transmission = %6.4f, %6.4f' \
    % (transmin, transmax))

# last record all zeros for shelx
zero = 0
hklFile.write(' %3d %3d %3d %7.2f %7.2f %3d %7.4f %6.4f %6d %6d %6.4f %3d\n' \
    % ( zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero ))

# C-----------------------------------------------------------------------
logFile.close()
hklFile.close()

# Set scale ID equal to detector number.
# This code is from scale_by_detnum.py.
if iIQ == 3:
    hklFileName1 = hklFileName + '1'
    os.rename(hklFileName, hklFileName1)
    hkl_output = open(hklFileName, 'w')
    
    for i in range(nod):
        hkl_input = open(hklFileName1, 'r')
        detNum = calibParam[3][i]

        while True:
            lineString = hkl_input.readline()
            lineList = lineString.split()
            if len(lineList) == 0: break
            
            h = int(lineString[0:5])
            k = int(lineString[4:8])
            l = int(lineString[8:12])
            fsq = float(lineString[12:20])
            sigfsq = float(lineString[20:28])
            hstnum = int(lineString[28:32])
            wl = float(lineString[32:40])
            tbar = float(lineString[40:47])
            curhst = int(lineString[47:54])
            seqnum = int(lineString[54:61])
            transmission = float(lineString[61:68])
            dn = int(lineString[68:72])
            
            if dn == detNum:
                iScale = i + 1
                hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d\n' \
                % (h, k, l, fsq, sigfsq, iScale, wl, tbar, curhst, seqnum, transmission, dn))

        hkl_input.close()
        
    # last record all zeros for shelx
    hkl_output.write(' %3d %3d %3d %7.2f %7.2f %3d %7.4f %6.4f %6d %6d %6.4f %3d\n' \
        % ( zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero ))

print 'All done!'

        


