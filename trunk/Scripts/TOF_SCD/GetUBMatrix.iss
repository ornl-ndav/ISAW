# 
#    Finds an orientation Matrix from a Peaks file.
#    Uses a combination of autocorrelation and fraction of peaks that will fall on a plane within .2. The"
#      fraction is weighted twice the autocorrelation. The resultant UB matrix has been run through blind to 
#      get standardized with the common conventions for the orientation matrices
#
#
# Assumptions:
# There are peaks in the peak file and that there is enough information to get legitimate Q values for
#@param  filename  a file with a vector of peaks
#@param  MaxXtalLength  The maximum length of crystal lattice in real space or -1 for default and adjustable
#@param   MinNewDXDy Minimum distance in x direction or y direction of the projection of a unit direction 
#                      on he xy plane for 2 directions to be considered different
#@param  saveFile   The name of the file to save the UB matrix
#@return  an orientation matrix or null
#@error Not enough directions have been found to create an orientation matrix


$filename  LoadFile    Enter filename with peaks
$MaxXtalLength    Float(20)  Enter maximum length of crystal lattice in real space or -1 for default
$MinNewDxDy     Float(.3)  Enter minimum distance in x direction or y direction for a direction to be different
$Svfilename  SaveFile   Enter filename where orientation matrix is to be saved

$category=operator,Instrument Type,TOF_NSCD
Peaks =ReadPeaks( filename)
UB= GetUBMatrix( Peaks,MaxXtalLength, MinNewDxDy)
WriteMatrix(Svfilename, UB)



