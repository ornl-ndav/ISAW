# File: deltaI1_I3.py

# Script to plot delta(I1-I3)/sigmaI1, where I1 is the 1D qplot
# integrated intensity, and I3 is the IsawEV spherical integrated
# intensity.
# A. J. Schultz, January 2012

from Tkinter import *
import tkSimpleDialog
import tkFileDialog

import pylab

import string

class MyDialog(tkSimpleDialog.Dialog):

    def body(self, master):
    
        self.b1 = Button(master, text = "Select working directory", command = self.setWorkDir)
        self.b1.grid(row=0, columnspan=2, pady=10)

        self.b2 = Button(master, text = "Select 1D integrate file", 
                         command = self.setIntFile1D)
        self.b2.grid(row = 1, columnspan = 2, pady = 10)
        
        self.b3 = Button(master, text = "Select 3D integrate file", 
                         command = self.setIntFile3D)
        self.b3.grid(row = 2, columnspan = 2, pady = 10)
        
        Label(master, text="Minimum d-spacing:").grid(row = 3)
        self.e1 = Entry(master)
        self.e1.grid(row = 3, column = 1, pady = 10)
        
        # self.b3 = Button(master, text = "Apply", command = self.apply)
        # self.b3.grid(row=3, columnspan=2, pady=10)
        
        # return self.e1 # initial focus

    def setWorkDir(self):
        self.workDir = tkFileDialog.askdirectory( title = 'Select working directory')

    def setIntFile1D(self):
        self.intFile1D = tkFileDialog.askopenfilename( title = 'Select the integrate file',
            initialdir = self.workDir)

    def setIntFile3D(self):
        self.intFile3D = tkFileDialog.askopenfilename( title = 'Select the integrate file',
            initialdir = self.workDir)

    def apply(self):
    
        # dmin = string.atof(self.e1.get())
        dmin = 0.5
                
        # workingDirectory = self.workDir
        # inFilename1D = self.intFile1D
        inFilename1D = '3681qplot.integrate'
        input1D = open(inFilename1D, 'r')
        # inFilename3D = self.intFile3D
        inFilename3D = '3681EV_020.integrate'
        input3D = open(inFilename3D, 'r')

        lineString = input1D.readline() # read first header line from integrate file
        lineString = input3D.readline() # read first header line from integrate file

        wdmin = 99.  # minimum weighted_delta
        wdmax = -99. # maximum weighted_delta
        wd = []      # list of weighted deltas
        
        # Begin reading the 1D integrate file.
        while True:
            lineString = input1D.readline()
            lineList1D = lineString.split()
            if (len(lineList1D)) == 0: break
            
            formatFlag = int(lineList1D[0])  # test for line type
            if formatFlag != 3: continue
                
            dspacing = float(lineList1D[12])
            if dspacing < dmin: continue
                
            h1 = int(lineList1D[2])
            k1 = int(lineList1D[3])
            l1 = int(lineList1D[4])
            intI1D = float(lineList1D[14])  # intI is the integrated intensity
            sigI1D = float(lineList1D[15])  # sigI is the standard deviation

            # Begin reading the 3D integrate file for each 1D peak.
            while True:
                lineString = input3D.readline()
                lineList3D = lineString.split()
                if (len(lineList3D)) == 0: break
                
                formatFlag = int(lineList3D[0])       # test for line type
                if formatFlag != 3: continue
                
                h3 = int(lineList3D[2])
                k3 = int(lineList3D[3])
                l3 = int(lineList3D[4])
                
                if h1 == h3 and k1 == k3 and l1 == l3:
                    intI3D = float(lineList3D[14])
                    weighted_delta = (intI1D - intI3D) / sigI1D
                    if weighted_delta < wdmin: wdmin = weighted_delta
                    if weighted_delta > wdmax: wdmax = weighted_delta
                    wd.append(weighted_delta)
                    # print h1, k1, l1, weighted_delta
                    break

        # end reading the integrate files

        input1D.close()
        input3D.close()
        
        numPeaks = len(wd)
        print '\numPeaks = ', numPeaks

        print '\nMinimum delta(I1 - I3)/sigI1 = ', wdmin
        print '\nMaximum delta(I1 - I3)/sigI1 = ', wdmax
        
        numSteps = int(5 * (wdmax - wdmin ) + 2)
        print '\nnumSteps = ', numSteps
        
        # Truncate wdmin to nearest 0.1
        wdmin = round(wdmin, 1)
        
        x = []
        y = []
        for i in range(numSteps):
            x.append(wdmin + (i * 0.2))
            y.append(0)
        
        for i in range(numPeaks):
            for j in range(numSteps):
                if wd[i] < x[j]:
                    print i, wd[i], j, x[j]
                    y[j] = y[j] + 1
                    break
                    
        pylab.plot(x, y)
        pylab.title('1D qplot (I1) vs. 3D IsawEV (I3) Integration')
        pylab.xlabel('Weighted difference, (I1 - I3)/sig(I1)')
        pylab.ylabel('Number of peaks')
        pylab.show()
        

root = Tk()
root.withdraw()

d = MyDialog(root)
