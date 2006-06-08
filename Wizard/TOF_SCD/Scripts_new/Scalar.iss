#  Shell around Scalar  so
#  that the identity matrix and a user defined matrix can be given
#@overview  Scalar provides a transformation matrix to convert the given unit
#           cell to one of the 14 Bravais Lattices
#
#File: Wizard/TOF_SCD/Scripts_new/Scalar.iss
#
#@algorithm  Uses the Scalar algorithm from IPNS
#@param UB   The current orientation matrix
#@param Delta  The error in the measurements
#@param Constr  Thetype of symmetry the cell should have
#@param UserTransf  Used if the choice in Constr is "User Specified"
#@param path          Path where output information is saved
#@param ShowLog       Show scalar.log. Note that this file can also be
#                     easily accessed via the View menu, the View Text option
#@return      The transformation matrix needed 

$title=Scalar ( Generate Possible Transformations to Desired Cell ) 

$UB   Array          UB Matrix
$Delta  Float(.01)   Delta
$Constr    Choice(["Identity", "No Restriction","Highest Symmetry","P - Cubic","F - Cubic","R - Hexagonal","I - Cubic","I - Tetragonal","I - Orthorombic","P - Tetragonal","P - Hexagonal","C - Orthorombic","C - Monoclinic","F - Orthorombic","P - Orthorombic","P - Monoclinic","P - Triclinic","R11 == R22 == R33","R11 == R22 != R33","R11 == R33 != R22","R11 != R22 != R33"])   Symmetry Constraints
$path      DataDirectoryString    Output Data Path 
$ShowLog   Boolean( false)        Show scalar.log


if Constr ="Identity"
   return "[[1,0,0],[0,1,0],[0,0,1]]"
endif


if Constr = "User Specified"
   
   return UserTransf
endif

OpenLog( path&"scalar.log")
X =JScalar( UB, Delta, Constr)
CloseLog()

if ShowLog
  ViewASCII( path & "scalar.log")
else
  Display "Log information is in scalar.log.  Use the View menu to view"
endif

Display "------------------------Finished Scalar------------------"

return X

