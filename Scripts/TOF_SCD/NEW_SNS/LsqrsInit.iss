#       LeastSquares on a set of runs
#@overview This does several least squares on a set of runs, 
# producing an overall leastSquare matrix and optionallhy one for each of the runs.
# The matrices are stored in files under the name ls[expName]runNum.mat
# or ls[expName].mat.  The peaks file must be named [expName].peaks
#
# File: Wizard/TOF_SCD/Script_new/LSqrs.iss

#@param  SaveDir        The directory to save the mat files to
#@param  expName        the name of the experiment(part of peak filename-extension must be .peaks)
#@param  runnums        Restrict run nums(blank for all)
#@param  detnums        Detector Nums to use("" for all)

#@param TransMat1    Enter Transformation Matrix
#@param  PerRunMats     If true an orientation matrix for each run will be produced
#@param  RestrSeqNums   the sequence nums to restrict
#@param  MinIntens      the minimum intenstity threshold
#@param  RowColKeep     Pixel Rows and columns to keep
#@param  Constr         Unit cell constraints. Use Triclinic if none are known
#@param ShowLog         Pop up lsqrs.log

#
#@return                "Success" or an ErrorString

$Title= Least Squares


$SaveDir        DataDirectoryString(${Data_Directory})  Directory to save files
#  $PeakFileName   String               Peaks filename(exclude path)
$expName        String               Name of experiment/peak filename base
$runnums        Array([])            Restrict Run Numbers ("" for all)
$detnums        Array([])            Detector Nums to use("" for all)

$TransMat1      String("[[1,0,0],[0,1,0],[0,0,1]]") Enter Transformation Matrix
$PerRunMat      boolean              Make per run orientation matrices
$RestrSeq       IntList              Sequence numbers to use("" for all)
$MinIntens      Integer(0)           Minimum Peak Intensity Threshold
$RowColKeep     IntList(0:256)       Pixel Rows and Columns to Keep
$Constr         ChoiceList( ["Triclinic","Monoclinic ( b unique )","Monoclinic ( a unique )","Monoclinic ( c unique )","Orthorhombic","Tetragonal","Rhombohedral","Hexagonal","Cubic"] )     Cell Type Constraint
$ShowLog        Boolean(true)       Pop Up lsqrs.log


$ Title = Least Squares ( Optimize Orientation Matrix )
$ command = LsqrsInit
$category=operator,Instrument Type,TOF_NSCD,NEW_SNS

peakfilename = SaveDir &expName&".peaks"
Peaks1 = ReadPeaks( peakfilename)
N=ArrayLength(Peaks1)
NDets = ArrayLength( detnums)
#---------Eliminate all Peaks with the given detector number
if NDets >0
  Peaks =[]
  keep = false
  for i in [0:N-1]
     detNum = getPeakInfo( Peaks1[i],"detnum")
     for j in detnums
       if detNum =j
          keep = true
       endif
     endfor
     if keep
       Peaks =Peaks&[Peaks1[i]]
       Display i
     endif
  endfor
else
  Peaks = Peaks1
endif
#------------------------------------------------------------

N= ArrayLength(Peaks)
N1=ArrayLength(runnums)
OpenLog( SaveDir&"lsqrs.log")

#-------------------------Per Run orientation Matrices -------------------
if PerRunMat AND  N1 > 0
  for i in runnums
     filename=SaveDir&"ls"&expName&i&".mat"
     Pk1=[]
     for j in [0:N-1]
        Pk1[j]= Peaks[j]
     endfor
   
     JLsqrs(Pk1,""&i,RestrSeq, TransMat1 , filename, MinIntens,RowColKeep,Constr)
     LogMsg( "-----------After run num "& i&"-----------------------\n")
  endfor
endif
#---------------------------------------------------------

#---------------------------Overall orientation matrix--------------------
S=""
#    ---------------Get list of run numbers-----------------
if N1 >0  
   for i in [1:N1]
     S=S&runnums[i-1]
     if i<N1
       S=S&","
     endif
   endfor
endif
#     ---------------------------------------------

filename=SaveDir&"ls"&expName&".mat"
Pk1=[]
for j in [0:N-1]
   Pk1[j]= Peaks[j]
endfor

JLsqrs(Pk1,S,RestrSeq,TransMat1,filename,MinIntens,RowColKeep, Constr)
   LogMsg( "-----------Finished Least Squares -----------------------\n")

CloseLog()


if ShowLog
  ViewASCII( SaveDir&"lsqrs.log")
endif
 
return  "Success"

