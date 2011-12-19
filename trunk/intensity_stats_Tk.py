# File: intensity_stats_Tk.py

# A. J. Schultz, November, 2011

from Tkinter import *
import tkSimpleDialog
import tkFileDialog

import string

class MyDialog(tkSimpleDialog.Dialog):

    def body(self, master):
    
        self.b1 = Button(master, text = "Select working directory", command = self.setWorkDir)
        self.b1.grid(row=0, columnspan=2, pady=10)

        self.b2 = Button(master, text = "Select integrate file", command = self.setIntFile)
        self.b2.grid(row = 1, columnspan = 2, pady = 10)
        
        Label(master, text="Minimum d-spacing:").grid(row = 2)
        self.e1 = Entry(master)
        self.e1.grid(row = 2, column = 1, pady = 10)
        
        # self.b3 = Button(master, text = "Apply", command = self.apply)
        # self.b3.grid(row=3, columnspan=2, pady=10)
        
        # return self.e1 # initial focus

    def setWorkDir(self):
        self.workDir = tkFileDialog.askdirectory( title = 'Select working directory')

    def setIntFile(self):
        self.intFile = tkFileDialog.askopenfilename( title = 'Select the integrate file',
            initialdir = self.workDir)

    def apply(self):
    
        dmin = string.atof(self.e1.get())
        
        sumTotal = sig3Total = sig5Total = sig10Total = 0
        sum = sig3 = sig5 = sig10 = 0
        sumAll = 0      # total number of peaks in the file
        nrun = nrunCurrent = 0
        dminObs = 999.0
        
        inFilename = self.intFile
        input = open(inFilename, 'r')

        workingDirectory = self.workDir
        outFilename = workingDirectory + 'stats.lst'
        output = open(outFilename, 'w')
        
        output.write('\nInput integrate file is ' + inFilename + '\n')
        print '\nInput integrate file is ' + inFilename + '\n'
        output.write('\ndmin = %5.2f \n' % dmin)
        print 'dmin = %5.2f \n' % dmin
        output.write('\n        ***  SUMMARY OF INTENSITY STATISTICS  ***\n\n')
        print '\n        ***  SUMMARY OF INTENSITY STATISTICS  ***\n\n'
        output.write('      NRUN     TOTAL      3SIG      5SIG     10SIG\n')
        print '      NRUN     TOTAL      3SIG      5SIG     10SIG\n'

        lineString = input.readline()           # read first header line from integrate file

        # begin reading the integrate file
        while True:
            lineString = input.readline()
            lineList = lineString.split()
            if (len(lineList)) == 0: break
            
            formatFlag = int(lineList[0])       # test for line type
            
            if formatFlag == 1:                 # histogram metadata
            
                nrun = int(lineList[1])         # run number
                if nrunCurrent != nrun:
                    if nrunCurrent != 0:        # this is a new run, but not the first run
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
        print '\n\n    TOTALS' + '%10d'*4 % (sumTotal, sig3Total, sig5Total, sig10Total)
        print '\n************************\n'
        print 'Results in the stats.lst file in your working directory\n'
        print '\n************************\n'
        output.write('\n\n    TOTALS')
        output.write('%10d'*4 % (sumTotal, sig3Total, sig5Total, sig10Total))
        output.write('\n\nTotal number of peaks with dmin of %5.3f is %d.'
            % (dminObs, sumAll))
        print '\nTotal number of peaks with dmin of %5.3f is %d.' % (dminObs, 
            sumAll)

        input.close()
        output.close()
                

root = Tk()
root.withdraw()

d = MyDialog(root)
