#--------------------------------------------------------------------
#                             setNRUN.py
#--------------------------------------------------------------------

#   A. J. Schultz
#   November, 2011

#   Script to set the run number in the integrate file from IsawEV

#*** Input the run numbers here:
RunNumbers = ['7413', '7414', '7415', '7416', '7421']
numOfRuns = len(RunNumbers)

#*** Input the output file name here:
outputFile = 'natrolite.integrate'
output = open(outputFile, 'w')

# write header info from first integrate file to the output file
inputFile = RunNumbers[0] + 'EV.integrate'
input = open(inputFile, 'r')
for line in input:
    if line[0] == '0': break
    output.write(line)
input.close()    # close first input file

# Begin reading and writing
for i in range(numOfRuns):
    inputFile = RunNumbers[i] + 'EV.integrate'
    input = open(inputFile, 'r')
    
    for line in input:
        if line[0:6] == '1    0':
            line = line[0:2] + RunNumbers[i] + line[6:]
    
        if line[0] != 'V':
            lineType = int(line[0])
            if lineType < 4:
                output.write(line)
        
output.close()


print 'The End'
   
