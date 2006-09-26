#
#       Display the sum or difference of two reduced 2D SAND/SASI files in the SANDWedgeViewer 
#
# $Date$

$Category=Macros, Instrument Type, TOF_NSAS


#@param    dataFileName   The path and name of the reduced 2D file 1 (sn2d25.dat)
#@param    dataFileName   The path and name of the reduced 2D file 2 (sn2d25.dat)
#@param    boolean		  Subtract (default is true. If toggled, will do "sum" instead)
#@param    float		  multiplying data from file 1 with this number;
#@param    float		  multiplying data from file 2 with this number;

$fin1  LoadFileString("/IPNShome/taoj/tmp/sand/sn2d2534.dat")  Enter data file 1
$fin2  LoadFileString("/IPNShome/taoj/tmp/sand/sn2d2536.dat")  Enter data file 2
$doSubtraction Boolean(true) 1 - 2 ?
$a     Float(1.0) a*(1)
$b     Float(1.0) b*(2)

ss = SWV2(fin1, fin2, doSubtraction, a, b)

return "Data displayed"

