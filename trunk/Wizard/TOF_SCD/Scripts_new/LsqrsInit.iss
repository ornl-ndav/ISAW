#       LeastSquares on a set of runs
#File: Wizard/TOF_SCD/Script_new/LSqrsInit.iss

#@overview This does a least squares fit between indexed peaks and Q values 
#for a set of runs producing an overall leastSquare orientation matrix.
#The matrix is stored in the specified file.  This orientation matrix
#does incorporate the transformation from scalar or the one specified 
# by the user
#
#
#@param  Peaks the Vector of Peaks to work with
#@param  expName  the name of the experiment(for use in filenames)
#@param  runnums  Restrict run nums(blank for all) 
#@param  RestrSeqNums the sequence nums to restrict
#@param  SaveDir  The directory to save the mat files to
#@param MinIntens  th minimum intenstity threshold
#@param RowColKeep  Pixel Rows and columns to keep
#
#@return the orientation matrix for all the runs

$category=HiddenOperator
$command=Lsqrs
$title=Least Squares

$Peaks    PlaceHolder    Peaks
$runnums   IntList      Restrict Run Numbers ("" for all)
$RestrSeq   IntList     Sequence numbers to use("" for all)
$useScalar   BooleanEnable([true,1,1])  Use Scalar's transf matrix
$TransMat    String       Transform Matrix fr Scalar
$TransMat1   String       User supplied Transf Matrix
$MatFileName   SaveFile    Matrix to write to
$MinIntens  Integer(0)  Minimum Peak Intensity Threshold
$RowColKeep  IntList(0:100) Pixel Rows and Columns to Keep
$Constr      ChoiceList( ["Triclinic","Monoclinic ( b unique )","Monoclinic ( a unique )","Monoclinic ( c unique )","Orthorhombic","Tetragonal","Rhombohedral","Hexagonal","Cubic"] )     Cell Type Constraint
$SaveDir    DataDirectoryString     Directory to save files
if useScalar
  tMat = TransMat
else
  tmat = TransMat1
endif
JLsqrs(Peaks,runnums,RestrSeq,tMat,MatFilename,MinIntens,RowColKeep,Constr)
    
return 

   
   




