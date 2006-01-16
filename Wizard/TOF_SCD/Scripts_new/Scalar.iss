#     Shell around Scalar  so
#  that the identity matrix and a user defined matrix can be given


$UB   Array          UB Matrix
$Delta  Float(.01)   Delta
$Constr    Choice(["Identity", "User Specified","No Restriction","Highest Symmetry","P - Cubic","F - Cubic","R - Hexagonal","I - Cubic","I - Tetragonal","I - Orthorombic","P - Tetragonal","P - Hexagonal","C - Orthorombic","C - Monoclinic","F - Orthorombic","P - Orthorombic","P - Monoclinic","P - Triclinic","R11 == R22 == R33","R11 == R22 != R33","R11 == R33 != R22","R11 != R22 != R33"])   Symmetry Constraints
$UserTransf    String("[[1,0,0],[0,1,0],[0,0,1]]")   User Specified Transformation


if Constr ="Identity"
   return "[[1,0,0],[0,1,0],[0,0,1]]"
endif


if Constr = "User Specified"
   
   return UserTransf
endif
X =JScalar( UB, Delta, Constr)


return X

