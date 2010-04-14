#--------------------------------------------------------------------
#                             anvred.py
#--------------------------------------------------------------------
#
# Data reduction program:
#   Input is raw integrated intensities.
#   Output is relative Fsq's.
#
# Jython version:
#    A. J. Schultz, started December 2009
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

from readrefl_header import *
from readrefl_SNS import *
from readSpecCoef import *
from spectrumCalc import *
from spectrum import *
from readSpectrum import *
from absor_sphere import *
# from jarray import *

class anvred_py(GenericTOF_SCD):

    def setDefaultParameters(self):
    
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Working directory", "C:/SNS/Jython/anvred"))
        self.addParameter(StringPG("Experiment name", "ox1"))
        self.addParameter(FloatPG("Total scattering linear abs coeff in cm^-1", 1.302))
        self.addParameter(FloatPG("True absorption linear abs coeff in cm^-1", 1.686))
        self.addParameter(FloatPG("Radius of spherical crystal in cm", 0.170))
        self.addParameter(IntegerPG("Incident spectrum: iSpec = 1 fitted; = 2 data", 2))
        self.addParameter(LoadFilePG("If iSpec = 1, file with spectrum coefficients", \
        "C:/SNS/Jython/anvred/spectrum.dat"))
        self.addParameter(IntegerPG("If iSpec = 2, the initial bank number", 10))
        self.addParameter(IntegerPG("If iSpec = 2, input averaging range +/-", 5))
        self.addParameter(IntegerPG("The minimum I/sig(I)", 0))
        self.addParameter(IntegerPG("Width of border (number of channels)", 5))
        self.addParameter(IntegerPG("Minimum peak count", 5))
        self.addParameter(FloatPG("Minimum d-spacing (Angstroms)", 0.5))
        self.addParameter(IntegerPG("Assign scale factors (1) per setting or (2) per detector", 1))
        self.addParameter(FloatPG("Multiply FSQ and sig(FSQ) by scaleFactor", 0.00001))
        
    def getResult(self):

        directory_path = self.getParameter(0).value
        expName = self.getParameter(1).value
        smu = self.getParameter(2).value
        amu = self.getParameter(3).value
        radius = self.getParameter(4).value
        iSpec = self.getParameter(5).value
        specCoeffFile = self.getParameter(6).value
        initBankNo = self.getParameter(7).value
        averageRange = self.getParameter(8).value
        minIsigI = self.getParameter(9).value
        numBorderCh = self.getParameter(10).value
        ipkMin = self.getParameter(11).value
        dMin = self.getParameter(12).value
        iIQ = self.getParameter(13).value
        scaleFactor = self.getParameter(14).value
        
        # open the anvred.log file in the working directory
        fileName = directory_path + 'anvred.log'
        logFile = open( fileName, 'w' )
        
        # open the hkl file in the working directory
        fileName = directory_path + expName + '.hkl'
        hklFile = open( fileName, 'w' )
        
        # echo the input in the log file
        logFile.write('\n********** anvred **********\n')
        logFile.write('\nWorking directory: ' + directory_path)
        logFile.write('\nExperiment name: ' + expName + '\n')
        
        logFile.write('\nTotal scattering linear absorption coefficient: %6.3f cm^-1' % smu )
        logFile.write('\nTrue absorption linear absorption coefficient: %6.3f cm^-1' % amu )
        logFile.write('\nRadius of spherical crystal: %6.3f cm\n' % radius )
        
        logFile.write('\nIncident spectrum and detector efficiency correction.')
        logFile.write('\n    iSpec = 1. Spectrum fitted to 11 coefficient GSAS Type 2 function')
        logFile.write('\n    iSpec = 2. Spectrum data read from a spectrum file.')
        logFile.write('\niSpec: %i\n' % iSpec)
        
        if iSpec == 1:
            logFile.write('\nFile with spectrum coefficients: ' + specCoeffFile + '\n' )
            
        if iSpec == 2:
            logFile.write('\nInitial bank number is %i \n' % initBankNo )
            logFile.write('\nSmoothing range is +/- %i channels\n' % averageRange )
        
        logFile.write('\nThe minimum I/sig(I) ratio: %i' % minIsigI )
        logFile.write('\nWidth of border: %i channels' % numBorderCh )
        logFile.write('\nMinimum peak count: %i' % ipkMin )
        logFile.write('\nMinimum d-spacing : %4.2f Angstroms\n' % dMin )
        
        logFile.write('\nScale factor identifier:' )
        logFile.write('\n     IQ = 1. Scale factor per crystal setting.' )
        logFile.write('\n     IQ = 2. Scale factor for each detector in each setting.')
        logFile.write('\nIQ: %i\n' % iIQ )
        
        logFile.write('\nMultiply FSQ and sig(FSQ) by: %i\n' % scaleFactor )
        
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
            
        # Read spectrum for each detector bank if iSpec = 2
        if iSpec == 2:
            # specBank is an array of arrays containing the spectra in the
            # Bankxx_spectrum.asc files.
            specBank = readSpectrum(nod, initBankNo, directory_path)
            

        # C-----------------------------------------------------------------------
        # C  Calculate spectral correction at 1.0 Angstrom to normalize
        # C  spectral correction factors later on.
        spect1 = []     # spectrum value at 1.0 Angstrom for each detector
        dist = []       # sample-to-detector distance
        xtof = []       # = (L1+dist)/hom; TOF = wl * xtof
        
        wavelength = 1.0
        
        for id in range(nod):
        
            if iSpec == 1:  # The spectrum is calculated from coefficients
                
                spect = spectrumCalc(wavelength, calibParam, pj, id)
                spect1.append(spect)
                
            else:           # iSpec = 2           
                
                dist.append(calibParam[9][id])
                xtof.append((L1 + dist[id]) / hom)
                one = 1.0
                
                # numTimeChannels = len(specBank[id][0])
                # print 'numTimeChannels = %d' % numTimeChannels
                
                # specBank[id][0] are the times-of-flight
                # specBank[id][1] are the counts
                spect = spectrum( wavelength, xtof[id], averageRange, \
                    one, specBank[id][0], specBank[id][1] )
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
                
            eof = peak[21]
            if eof == 0: break
            
            nrun = peak[0]
            dn = peak[1]
            chi = float( peak[2] )
            phi = float( peak[3] )
            omega = float( peak[4] )
            moncnt = peak[5]
            h = peak[6]
            k = peak[7]
            l = peak[8]
            col = peak[9]
            row = peak[10]
            chan = peak[11]
            L2 = peak[12]
            twoth = peak[13]  # radians
            az = peak[14]  # azimuthal angle in radians
            wl = peak[15]
            dsp = peak[16]
            ipkobs = peak[17]
            inti = peak[18]
            sigi = peak[19]
            reflag = peak[20]


            # set-up for new run or detector
            if nrun != curhst or dn != idet:
            
                if nrun != curhst:
                    curhst = nrun
                    if iIQ == 1: hstnum = hstnum + 1
                    
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
                    
            if minIsigI > 0 and inti < (minIsigI * sigi): continue            
            if inti == 0: continue
            if ipkobs < ipkMin: continue
            
            nRows = calibParam[4][id]
            nCols = calibParam[5][id]
            
            if col < numBorderCh: continue
            if col > (nCols - numBorderCh): continue
            if row < numBorderCh: continue
            if row > (nRows - numBorderCh): continue
           
            if reflag == 0: continue
            if dsp < dMin: continue
            
            ncntr = ncntr + 1
            
            if iSpec == 1:
                spect = spectrumCalc(wl, calibParam, pj, id)
                spect = spect / spect1[id]
            
            if iSpec == 2:
                spect = spectrum( wl, xtof[id], averageRange, \
                  spect1[id], specBank[id][0], specBank[id][1] )
            
            sinsqt = ( wl / (2.0*dsp) )**2
            wl4 = wl**4
                
            correc = scaleFactor * (sinsqt * cmonx ) / (wl4 * spect )
                
            # absorption correction
            # trans[0] is the transmission
            # trans[1] is tbar
            trans = absor_sphere(smu, amu, radius, twoth, wl)
            
            if trans[0] < transmin: transmin = trans
            if trans[0] > transmax: transmax = trans
            
            correc = correc / trans[0]
            
            fsq = inti * correc
            sigfsq = sigi * correc
            
            # tbar is the Coppen's tbar
            tbar = trans[1]
            
            # output reflection to log file and to hkl file
            
            logFile.write(' %4d%4d%4d%10.2f%8.2f%7.3f%10.2f%8.2f%8.4f%8.4f%8.4f%8.4f\n' \
                % (h, k, l, fsq, sigfsq, wl, inti, sigi, spect, sinsqt, trans[0], tbar))
            
            
            hklFile.write(' %3d %3d %3d %7.2f %7.2f %3d %7.4f %6.4f %6.4f %6d %3d\n' \
                % (h, k, l, fsq, sigfsq, hstnum, wl, tbar, trans[0], curhst, dn))
                
        print 'eof = %d' % eof
        
        # last record all zeros for shelx
        zero = 0
        hklFile.write(' %3d %3d %3d %7.2f %7.2f %3d %7.4f %6.4f %6.4f %6d %3d\n' \
            % ( zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero ))

# C-----------------------------------------------------------------------
        logFile.close()
        hklFile.close()
        
        return 'All done!'

        
    def getDocumentation( self):
        S =StringBuffer()
        S.append("ANVRED reduces the raw intensities to relative structure factor amplitudes.")
        S.append("Inputs to anvred:\n")
        S.append("@param directory_path: The working directory with all of the files.")
        S.append("@param expName: The experiment name.")
        S.append("@param  smu: The scattering linear absorption coefficient in cm^-1.")
        S.append("@param  amu: The absorption linear absorption coefficient in cm^-1 at 1.* Angstroms. smu and amu can be 0.0 for no absorption correction;")
        S.append("@param  radius: Radius of the spherical crystal in cm.")
        S.append("@param  iSpec: If 1, the incident spectrum is fitted. If 2, use the raw spectrum.")
        S.append("@param  specCoeffFile: If iSpec = 1, the file containing the fitted coefficients.")
        S.append("@param  initBankNo: If iSpec = 2, the number of the first detector bank.")
        S.append("@param  averageRange: If iSpec = 2, the +/- range of data points in the spectrum for averaging.")
        S.append("@param  minIsigI: I/sigI threshold for saving a peak.")
        S.append("@param  numBorderCh: width of border. Peaks in border are rejected.")
        S.append("@param  ipkMin: minimum peak count at peak max.")
        S.append("@param  dMin: The minimum d-spacing in Angstrom units.")
        S.append("@param  iIQ: If iIQ = 1, a scale factor for each crystal setting. If iIQ = 2, a scale factor for each detector of each crystal setting.")
        S.append("@param  scaleFactor: multiply Fsq and sigFsq by this factor.")
        
        S.append("@return the number of peaks processed.")
        return S.toString()

    def getCategoryList( self):
       
        return ["Macros", "Single Crystal"]
        
    def __init__(self):
        Operator.__init__(self,"anvred_py")

