from numpy.linalg import *
from pylab import *
from math import *

# open spectrum file
specInput = open( 'Spectrum_2503_2502.dat', 'r' )

for i in range(8):   # skip the first 8 lines
    lineString = specInput.readline()

lineString = specInput.readline()   # read "Bank 1" line

nod = 14     # the number of detectors

det_sum = []   # set array to zero

for i in range( nod ):
    sum = 0.0
    
    if i < 9:
        print 'Reading spectrum for ' + lineString[0:20]
    else:
        print 'Reading spectrum for ' + lineString[0:21]
    
    lineList = lineString.split()
    DetNum = lineList[3]
    
    while True:
        lineString = specInput.readline()
        lineList = lineString.split()
        if len(lineList) == 0: break     # check for the end-of-file
        if lineList[0] == 'Bank': break  # check for the start of a new spectrum
        sum = sum + float( lineList[1] )
    
    det_sum.append( sum )
    

# open detector calibration file
detCalInput = open( 'TOPAZ_2011_02_16_depths.DetCal' )

# skip to beginning of detector parameters
while True:
    lineString = detCalInput.readline()
    lineList = lineString.split()
    if lineList[0] == '4': break    # check for column labels line before detector parameters

ki = [0.0, 0.0, 1.0]
twoTheta = []
vertAng = []
for i in range( nod ):
    lineString = detCalInput.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break    # check for end of file
    detNum = int( lineList[1] )
    x = float( lineList[8] )
    y = float( lineList[9] )
    z = float( lineList[10] )
    ks = [x, y, z]
    
    cosTwoTheta = dot(ki,ks)/(norm(ki) * norm(ks))
    twoTheta.append( acos(cosTwoTheta) * 180./pi )   # two-theta angle in degrees
    
    sinVertAng = y / norm(ks)
    vertAng.append( asin(sinVertAng) * 180./pi )
    
    print 'DETNUM two-theta verticle_angle sum = %2d %7.1f %7.1f %10.1f ' % (detNum, twoTheta[i], vertAng[i], det_sum[i])

detCalInput.close()



subplot(211)
xlabel( 'Two Theta (deg)' )
ylabel( 'Counts' )
title('Counts vs. two theta angle')
grid(True)
plot( twoTheta, det_sum, '^' )


subplot(212)
xlabel( 'Vertical Angle (deg)' )
ylabel( 'Counts' )
title('Counts vs. vertical angle')
grid(True)
plot( vertAng, det_sum, 'v' )

subplots_adjust(left=0.25, bottom=None, right=0.8, top=None, wspace=None, hspace=0.5)

# subplot_tool()

savefig('Counts_vs_2theta.pdf')

show()


