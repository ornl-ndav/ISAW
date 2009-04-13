#       LeastSquares on a set of runs

#@overview This does a least squares fit between indexed peaks and Q values 
#for a set of runs producing an overall leastSquare orientation matrix.
#The matrix is stored in the specified file.  This orientation matrix
#does incorporate the transformation from scalar or the one specified 
# by the user

#File: Wizard/TOF_SCD/Script_new/LSqrsInit.iss
#
#
#@param  Peaks        The Vector of Peaks to work with
#@param runnums       Only Use Run Numbers ( "" for all )
#@param  RestrSeqNums The sequence nums to restrict
#@param useUserMat   Enter Matrix( instead of Scalar's)
#@param TransMat1    Enter Transformation Matrix
#@param TransMat    Transformation Matrix From Scalar
#@param MatFileName   Matrix to write to
#@param  MinIntens    The minimum intenstity threshold
#@param  RowColKeep   Pixel Rows and columns to keep
#@param Constr        Cell Type Constraint
#@param  SaveDir      The directory to save the mat files to
#@param ShowLog      Pop up log file
#
#@return the orientation matrix for all the runs

$category=HiddenOperator
$command=Lsqrs
$title=Least Squares ( Optimize Orientation Matrix )

$Peaks       PlaceHolder                         Peaks
$runnums     IntList                             Only Use Run Numbers ( "" for all )
$RestrSeq    IntList                             Only Use Sequence Numbers( "" for all )
$useUserMat   BooleanEnable([true,1,0])          Enter Matrix( instead of Scalar's)
$TransMat1   String([[1,0,0],[0,1,0],[0,0,1]])   Enter Transformation Matrix
$TransMat    String                              Transformation Matrix From Scalar
$MatFileName String                              Matrix to write to
$MinIntens   Integer(0)                          Minimum Peak Intensity Threshold
$RowColKeep  IntList(0:256)                      Pixel Rows and Columns to Keep
$Constr      ChoiceList( ["Triclinic","Monoclinic ( b unique )","Monoclinic ( a unique )","Monoclinic ( c unique )","Orthorhombic","Tetragonal","Rhombohedral","Hexagonal","Cubic"] )     Cell Type Constraint
$SaveDir     DataDirectoryString(${Data_Directory})                 Directory to save files
$ShowLog     Boolean(false)                      Pop up log file


if useUserMat
  tMat = TransMat1
else
  tmat = TransMat
endif

MatFileName = SaveDir & MatFileName

Display "runnums = :"&runnums&":"
Display "seqnums = :"&RestrSeq&":"

OpenLog( SaveDir&"lsqrs.log")
R = JLsqrs(Peaks,runnums,RestrSeq,tMat,MatFilename,MinIntens,RowColKeep,Constr)
CloseLog()   
if ShowLog
   ViewASCII( SaveDir & "lsqrs.log" )
endif
return R

   
   




