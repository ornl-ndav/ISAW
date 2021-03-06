#       LeastSquares on a set of runs
#@overview This does several least squares on a set of runs, 
# producing and overall leastSquare matrix and one for each of the runs.
# The matrices are stored in files under the name ls[expName]runNum.mat
# or ls[expName].mat
#
# File: Wizard/TOF_SCD/Script_new/LSqrs.iss
#@param  Peaks          the Vector of Peaks to work with
#@param  expName        the name of the experiment(for use in filenames)
#@param  runnums        Restrict run nums(blank for all) 
#@param  RestrSeqNums   the sequence nums to restrict
#@param  SaveDir        The directory to save the mat files to
#@param  MinIntens      the minimum intenstity threshold
#@param  RowColKeep     Pixel Rows and columns to keep
#@param  ShowLog        pops up the lsqrs.log file
#
#@return                "Success" or an ErrorString

$Title= Least Squares


$Peaks       PlaceHolder          Peaks
$expName     String               Name of experiment
$runnums     Array([])            Restrict Run Numbers ("" for all)
$RestrSeq    IntList              Sequence numbers to use("" for all)
$SaveDir     DataDirectoryString(${Data_Directory})  Directory to save files
$MinIntens   Integer(0)           Minimum Peak Intensity Threshold
$RowColKeep  IntList(0:256)       Pixel Rows and Columns to Keep
$Constr      ChoiceList( ["Triclinic","Monoclinic ( b unique )","Monoclinic ( a unique )","Monoclinic ( c unique )","Orthorhombic","Tetragonal","Rhombohedral","Hexagonal","Cubic"] )     Cell Type Constraint
$ShowLog     Boolean(false)       Pop Up lsqrs.log


N=ArrayLength(Peaks)
OpenLog( SaveDir&"lsqrs.log")
if ArrayLength(runnums)>0
  for i in runnums
     filename=SaveDir&"ls"&expName&i&".mat"
     Pk1=[]
     for j in [0:N-1]
        Pk1[j]= Peaks[j]
     endfor
    
     JLsqrs(Pk1,""&i,RestrSeq,"[[1,0,0],[0,1,0],[0,0,1]]", filename, MinIntens,RowColKeep,Constr)
     LogMsg( "-----------After run num "& i&"-----------------------\n")
   endfor

endif

S=""
#The overall matrix always uses all peaks
#N1=ArrayLength(runnums)
#for i in [1:N1]
#  S=S&runnums[i-1]
#  if i<N1
#    S=S&","
#  endif
#endfor

filename=SaveDir&"ls"&expName&".mat"

Pk1=[]
for j in [0:N-1]
   Pk1[j]= Peaks[j]
endfor

JLsqrs(Pk1,S,RestrSeq,"[[1,0,0],[0,1,0],[0,0,1]]",filename,MinIntens,RowColKeep, Constr)
   LogMsg( "-----------Finished Least Squares -----------------------\n")

CloseLog()


if ShowLog
  ViewASCII( SaveDir&"lsqrs.log")
endif
 
return  "Success"

