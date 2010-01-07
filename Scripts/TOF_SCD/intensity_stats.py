#-----------------------------------
#           intensity_stats.py
#-----------------------------------

# Fortran program comments:
# !!!   Linux version:  A.J. Schultz		June, 2004
#
# C	Program to print a summary of intensity statistics.
# C	A. Schultz   6/26/85
# C       Modified by R. Goyette 9/21/89 to create an EasyPlot File
# C       to display count above 3*Sigma versus Wavelength.
#
# !!!   SNS version: September, 2009

# Jython version:
# A. J. Schultz --  November, 2009

class intensity_stats(GenericTOF_SCD):

    def setDefaultParameters(self):
    
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Directory containing the integrate file", "C:\SNS\FortranPrograms\stats"))
        self.addParameter(StringPG("Experiment name", "ox"))
        self.addParameter(FloatPG("Minimum d-spacing (Angstroms)", 0.7))

        
    def getResult(self):
    
        in_path = self.getParameter(0).value    # working directory
        expname = self.getParameter(1).value    # experiment name, as in expname.integrate
        dmin = self.getParameter(2).value       # minimum d-spacing

        sumTotal = sig3Total = sig5Total = sig10Total = 0
        sum = sig3 = sig5 = sig10 = 0
        sumAll = 0      # total number of peaks in the file
        nrun = nrunCurrent = 0
        dminObs = 999.0
        
        inFilename = in_path + expname + '.integrate'
        input = open(inFilename, 'r')
        
        outFilename = in_path + 'stats.lst'
        output = open(outFilename, 'w')
        output.write('\nInput integrate file is ' + inFilename + '\n')
        output.write('\ndmin = %5.2f \n' % dmin)
        output.write('\n        ***  SUMMARY OF INTENSITY STATISTICS  ***\n\n')
        output.write('      NRUN     TOTAL      3SIG      5SIG     10SIG\n')
        
        lineString = input.readline()           # read first header line from integrate file
        print lineString
        
        # begin reading the integrate file
        while True:
            lineString = input.readline()
            lineList = lineString.split()
            if (len(lineList)) == 0: break
            
            formatFlag = int(lineList[0])       # test for line type
            
            if formatFlag == 1:                 # histogram metadata
            
                nrun = int(lineList[1])         # run number
                if nrunCurrent != nrun:
                    if nrunCurrent != 0:            # this is a new run, but not the first run
                        # print the statistics of nrunCurrent
                        print '%10d'*5 % (nrunCurrent, sum, sig3, sig5, sig10)                    
                        output.write('%10d'*5 % (nrunCurrent, sum, sig3, sig5, sig10))
                        output.write('\n')
                        # add nrunCurrent statistics to total statics
                        sumTotal = sumTotal + sum
                        sig3Total = sig3Total + sig3
                        sig5Total = sig5Total + sig5
                        sig10Total = sig10Total + sig10
                    
                    nrunCurrent = nrun          # set the current nrun equal to the new nrun
                    sum = sig3 = sig5 = sig10 = 0
            
            elif formatFlag == 3:
                
                sumAll = sumAll + 1
                seqnum = int(lineList[1])        # get number of all peaks
                dspacing = float(lineList[12])
                if dspacing < dminObs: dminObs = dspacing   # get observed dmin
                
                if dspacing > dmin:             # test for dspacing less than dmin
                    intI = float(lineList[14])  # intI is the integrated intensity
                    sigI = float(lineList[15])  # sigI is the standard deviation
                    sum = sum + 1
                    if intI > (3.0 * sigI): sig3 = sig3 + 1
                    if intI > (5.0 * sigI): sig5 = sig5 + 1
                    if intI > (10.0 * sigI): sig10 = sig10 + 1
            

        # end reading the integrate file


        # Print the results.
        
        outString = '%10d'*5 % (nrunCurrent, sum, sig3, sig5, sig10)
        print outString                                     # last run
        output.write(outString)
    
        sumTotal = sumTotal + sum                           # final sum
        sig3Total = sig3Total + sig3
        sig5Total = sig5Total + sig5
        sig10Total = sig10Total + sig10

        # totals
        print '\n TOTALS'
        print '%10d'*4 % (sumTotal, sig3Total, sig5Total, sig10Total)
        print '\n************************\n'
        print 'Results in the stats.lst file in your working directory\n'
        print '\n************************\n'
        output.write('\n\n    TOTALS')
        output.write('%10d'*4 % (sumTotal, sig3Total, sig5Total, sig10Total))
        output.write('\n\nTotal number of peaks with dmin of %5.3f is %d.'\
        % (dminObs, sumAll))
        
        input.close()
        output.close()
        
        return sumTotal, sig3Total, sig5Total, sig10Total

    def  getDocumentation( self):
        S =StringBuffer()
        S.append("Program prints a summary of the intensity statistics.\n")
        S.append("@param  dmin  The minimum d-spacing for an accepted peak.")
        S.append("@return an integer array with 4 entries: sumTotal, sig3Total,\
        sig5Total and sig10TOtal")
        return S.toString()

    def getCategoryList( self):
       
        return ["Macros","Single Crystal"]
        
    def __init__(self):
        Operator.__init__(self,"intensity_stats")


