#--------------------------------------------------------
#               function readrefl_SNS
#--------------------------------------------------------
#
# !!!	Read a peaks or integrate file with the SNS format.
# !!!	A. J. Schultz	April, 2008
#
# !	Read a reflection from a reflection file.
# !	The first variable of each record is ISENT.
#
# !	Linux version, January 2002, A.Schultz
#
# !	ISENT = 0 --> variable name list for describing the histogram
# !	      = 1 --> variable values of above list
# !	      = 2 --> variable name list for a reflection
# !	      = 3 --> variable values of above list
# !	      = 4 --> variable name list for parameters for detectors
# !	      = 5 --> variable values of parameters for detectors
# !	      = 6 --> variable name list: L1    T0_SHIFT
# !	      = 7 --> variable values: L1    T0_SHIFT
#
# !	IEOF = 0 --> not end-of-file
# !	     = 1 --> end-of-file
#
#--------------------------------------------------------
#   Jython version:
#       A. J. Schultz   March, 2010
#--------------------------------------------------------

def readrefl_SNS(input, eof, nrun, dn, chi, phi, omega, moncnt):
    "Returns parameters for a peak in the integrate file."

# The current histogram parameters are passed in the argument
# list. These values are replaced with new values when peaks
# from a new histogram are being read.
    
# "input" is the input peaks or integrate file which is already open.    
# begin reading the peaks or integrate file

    while True:
        lineString = input.readline()
        lineList = lineString.split()
        eof = len(lineList)
        if eof == 0: break                    # len = 0 if EOf
        
        isent = int( lineList[0] )          # test for line type
        
        if isent == 0:
            nrun = int( lineList[1] )
            dn = int( lineList[2] )
            chi = float( lineList[3] )
            phi = float( lineList[4] )
            omega = float( lineList[5] )
            moncnt = int( lineList[7] )
            
            # read the next line with isent = 2
            lineString = input.readline()
            
            # read the next line wiht isent = 3
            lineString = input.readline()
            lineList = lineString.split()
            isent = int( lineList[0] )       # test for line type
        
        if isent == 3:
            seqnum = int( lineList[1] )         
            h = int( lineList[2] )
            k = int( lineList[3] )
            l = int( lineList[4] )
            col = float( lineList[5] )
            row = float( lineList[6] )
            chan = float( lineList[7] )
            L2 = float( lineList[8] )
            twoth = float( lineList[9] )
            az = float( lineList[10] )
            wl = float( lineList[11] )
            dsp = float( lineList[12] )
            ipkobs = int( lineList[13] )
            inti = float( lineList[14] )
            sigi = float( lineList[15] )
            reflag = int( lineList[16] )

# finished
    # return nrun, dn, chi, phi, omega, moncnt,\    0 -> 5
        # h, k, l, col, row, chan, L2, twoth,\      6 -> 13
        # az, wl, dsp, ipkobs, inti, sigi, reflag\ 14 -> 20
        # eof                                      21
        
    return nrun, dn, chi, phi, omega, moncnt,\
        h, k, l, col, row, chan, L2, twoth,\
        az, wl, dsp, ipkobs, inti, sigi, reflag,\
        eof
