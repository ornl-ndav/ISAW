#       LeastSquares on a set of runs
#File: Wizard/TOF_SCD/Script_new/LSqrsInit.iss

#@overview This does a least squares fit between indexed peaks and Q values 
#for a set of runs producing an overall leastSquare orientation matrix.
#The matrix is stored in the specified file.  This orientation matrix
#does incorporate the transformation from scalar or the one specified 
# by the user
#
#
#@param  Peaks        The Vector of Peaks to work with
#@param  expName      The name of the experiment(for use in filenames)
#@param  runnums      Restrict run nums(blank for all) 
#@param  RestrSeqNums The sequence nums to restrict
#@param  SaveDir      The directory to save the mat files to
#@param  MinIntens    The minimum intenstity threshold
#@param  RowColKeep   Pixel Rows and columns to keep
#
#@return the orientation matrix for all the runs

$category=HiddenOperator
$command=Lsqrs
$title=Least Squares ( Optimize Orientation Matrix )

$Peaks       PlaceHolder    Peaks
$runnums     IntList        Only Use Run Numbers ( "" for all )
$RestrSeq    IntList        Only Use Sequence Numbers( "" for all )
$useScalar   BooleanEnable([true,1,1])  Use Scalar's Transformation Matrix
$TransMat    String         Transformation Matrix From Scalar
$TransMat1   String([1,0,0],[0,1,0],[0,0,1]) Enter Transformation Matrix
$MatFileName SaveFile       Matrix to write to
$MinIntens   Integer(0)     Minimum Peak Intensity Threshold
$RowColKeep  IntList(0:128) Pixel Rows and Columns to Keep
$Constr      ChoiceList( ["Triclinic","Monoclinic ( b unique )","Monoclinic ( a unique )","Monoclinic ( c unique )","Orthorhombic","Tetragonal","Rhombohedral","Hexagonal","Cubic"] )     Cell Type Constraint
$SaveDir    DataDirectoryString     Directory to save files
if useScalar
  tMat = TransMat
else
  tmat = TransMat1
endif
JLsqrs(Peaks,runnums,RestrSeq,tMat,MatFilename,MinIntens,RowColKeep,Constr)
    
return 

   
   




