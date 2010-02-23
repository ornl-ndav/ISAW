#
#     Convert UB matrix to u vector, v vector and angle( mslice input)
#@overview   This converts a UB matrix to a form another program needs.
#            The user can specify the hkl values that correspond to the u and v vectors.
#@algorithm  
#     multiply UB time hkl1 then hkl2 to get u' and v'.
#     Project u' and v' to the scattering plane( sets last component to 0(IPNS))
#     to determine u and v.
#     Determine the angle between the beam(x-IPNS) and vector u.
#@param filename  the name of the file with the UB matrix
#@param hkl1      The h,k ,l values for the vector that u' that corresponds to u
#@param hkl2      The h,k ,l values for the vector that v' that corresponds to v

#@return a Vector containing u', v', and the angle between the beam in u' in degrees

$filename      LoadFile(${Data_Directory})   Enter file with orientation matrix

$hkl1          Array([1,0,0])                Enter hkl values that will correspond to u
$hkl2          Array([0,1,0])                Enter hkl values that will correspond to v

UB = readOrient( filename)
UB1 = ToVec(UB)
return UB2uva(UB1,hkl1,hkl2)
