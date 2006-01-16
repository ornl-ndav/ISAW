#       LeastSquares on a set of runs
#@overview This does several least squares on a set of runs, 
#producing and overall leastSquare matrix and one for each of the runs.
#The matrices are stored in files under the name ls[expName]runNum.mat
# or ls[expName].mat
#
#@param  Peaks the Vector of Peaks to work with
#@param  expName  the name of the experiment(for use in filenames)
#@param  runnums  The run numbers to not 
#@param  RestrSeqNums the sequence nums to restrict
#@param  SaveDir  The directory to save the mat files to
#@param MinIntens  th minimum intenstity threshold
#@param RowColKeep  Pixel Rows and columns to keep
#
#@return the orientation matrix for all the runs

$category=HiddenOperator
$command=Lsqrs
$Peaks    PlaceHolder    Peaks
$expName   String       Name of experiment
$runnums   Array      Run Numbers to use("" for all)
$RestrSeq   IntList     Sequence numbers to use("" for all)
$SaveDir    DataDirectoryString     Directory to save files
$MinIntens  Integer(0)  Minimum Peak Intensity Threshold
$RowColKeep  IntList(0:100) Pixel Rows and Columns to Keep
#TODO JLsqrs deletes some peaks.  Make copies each time
Display "peaks num="&ArrayLength(Peaks)
N=ArrayLength(Peaks)
for i in runnums
   filename=SaveDir&"/ls"&expName&i&".mat"
   Pk1=[]
   for j in [0:N-1]
      Pk1[j]= Peaks[j]
   endfor
   Display "peak copy="&Pk1
   JLsqrs(Pk1,""&i,RestrSeq,"[[1,0,0],[0,1,0],[0,0,1]]",filename,MinIntens,RowColKeep)
 
endfor
display "Num Peaks="&ArrayLength(Peaks) 
S=""
N1=ArrayLength(runnums)
for i in [1:N1]
  S=S&runnums[i-1]
  if i<N1
    S=S&","
  endif
endfor
filename=SaveDir&"/ls"&expName&".mat"

Pk1=[]
for j in [0:N-1]
   Pk1[j]= Peaks[j]
endfor
JLsqrs(Pk1,S,RestrSeq,"[[1,0,0],[0,1,0],[0,0,1]]",filename,MinIntens,RowColKeep)
    
return 

   
   




