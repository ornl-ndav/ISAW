#
#       Display a reduced 2D SAND/SASI file in the SANDWedgeViewer 
#
# $Date$

$Category=Macros, Instrument Type, TOF_NSAS


#@param    dataFileName   The path and name of the reduced 2D file(sn2d25.dat)

$dataFileName  LoadFileString("c:/sasi/sn2d44.dat")  Enter Run file with info

ss = SWV(dataFileName)

return "Data displayed"

