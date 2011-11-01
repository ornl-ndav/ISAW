#--------------------------------------------------------------------
#                             re_scale.py
#--------------------------------------------------------------------

#   Script to re-group scaling factors.

#   A. J. Schultz
#   January, 2011

# Open the instruction file
filename = 're_scale.inp'    # instruction input file
input = open(filename, 'r')

# Read the name of the input hkl file
lineString = input.readline()
lineList = lineString.split()
inputFile = lineList[0]      # the input hkl file

# Read the name of the output hkl file
lineString = input.readline()
lineList = lineString.split()
outputFile = lineList[0]     # the output hkl file

# Read the first run number
lineString = input.readline()
lineList = lineString.split()
firstRun = int(lineList[0])  # run number if the first histogram

# Read the last run number
lineString = input.readline()
lineList = lineString.split()
lastRun = int(lineList[0])   # run number of the last histogram

# Read the number of groups
lineString = input.readline()
lineList = lineString.split()
numOfGroups = int(lineList[0])
print 'numOfGroups = %d' % numOfGroups

detectorList = []
for i in range(numOfGroups):
    lineString = input.readline()
    detectorList.append(lineString.split())

hkl_output = open(outputFile, 'w')

for iRun in range(firstRun, lastRun+1):    # loop through the histograms

    for i in range(numOfGroups):
        numInGroup = len(detectorList[i])
        
        for j in range(numInGroup):
            hkl_input = open(inputFile, 'r')
            detector = int(detectorList[i][j])  # the detector number
            
            while True:
                lineString = hkl_input.readline()
                lineList = lineString.split()
                if len(lineList) == 0: break
                
                h = int(lineList[0])
                k = int(lineList[1])
                l = int(lineList[2])
                fsq = float(lineList[3])
                sigfsq = float(lineList[4])
                hstnum = int(lineList[5])
                wl = float(lineList[6])
                tbar = float(lineList[7])
                curhst = int(lineList[8])
                seqnum = int(lineList[9])
                transmission = float(lineList[10])
                dn = int(lineList[11])
                
                if curhst != iRun: continue
                
                if detector == dn:
                    iScale = numOfGroups*(iRun-firstRun) + i + 1
                    hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d\n' \
                    % (h, k, l, fsq, sigfsq, iScale, wl, tbar, curhst, seqnum, transmission, dn))
        
            hkl_input.close()
    
lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0'
hkl_output.write(lastLine)

print 'The End'
        
