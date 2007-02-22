# 
# Script to find peaks in multiple files with Single Crystal data. 
# $Date: 2006/06/09 $
#
# File: Wizard/TOF_SCD/Scripts_new/FindMultPeaks_Fine.iss
#
# This script is the findMultiplePeaks.iss scripts where several of the
# operations are broken down into finer steps so these steps can be
# replaced.  FindMultiplePeaks find peaks from multiple SCD runs.
#
# Assumptions:
#  - Data of interest is in the last histogram 
#  - The filenames are concatenations of the path, inst, run_numbers, then
#    the DatFileExtension. Make sure that the /,\, and . are there
#  - The Generic Calib operator is used to read the calibration file

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ Title=Find Peaks

#---------------------------Parameters---------------------
$ path                DataDirectoryString(C:/SCD/Oxalic acid/Nov03//)    Raw Data Path
$ outpath             DataDirectoryString(C:/New Folder)    Output Data Path
$ run_numbers         Array([9164:9166])                 Run Numbers
$ expname             String(0x80)                Experiment Name
$ num_peaks           Integer(10)            Maximum Number of Peaks
$ min_int             Integer(3)             Minimum Peak intensity
$ min_time_chan       Integer(0)             Minimum Time Channel
$ max_time_chan       Integer(1000)          Maximum Time Channel 
$ useCalibFile        BooleanEnable( [true,1,0])  Use the calibration file below
$ calibfile           LoadFileString(c:/SCD/instprm.dat)        Calibration File
$ RowColKeep          IntList(1:100)         Pixel Rows and Columns to Keep 
$ inst                String("SCD0")  Instrument
$ DatFileExtension    String(".run")         Extension on Data Files

Display "Instrument="&inst
if  useCalibFile
  Display "Calibration File ="&calibfile
endif
# DataSet is number two
# dsnum=1   //Changes depending on when the run file was made
first=true
append = false
Peaks =[]
for i in run_numbers
  # load data
  filename=path&inst&i&DatFileExtension
  Display "Loading "&filename
  Echo("Finding peaks in "&filename)
  nn=load(filename,"dss")
  dsnum = nn-1
  DS =dss[dsnum]
  monct=100000.0
  #monct=IntegGrp(ds[0],1,0,50000)
  if useCalibFile
    Calib(DS, calibfile)
  endif

   G= ToVec(getAreaGridIDs(DS))
   Display G
   
   Seq = 1
   for gridID in G

     V = findDetectorPeaks( DS,gridID, min_time_chan, max_time_chan, num_peaks, min_int, RowColKeep))
# ******** array = getDSArray(DS, gridID, "FORTRAN") ********  can pass this C-array into C  code via jni.
    
     N = ArrayLength( V)
     for i in [1:N]
        P = V[ i-1]
      
        x= getPeakInfo( P,"x")
        y= getPeakInfo( P,"y")
        z= getPeakInfo( P,"z") 

        #-------Result is a float[3] . Create one     
        U=[1:3]
        Result = VectorTo_floatArray(U)
        #------------
        CentroidPeakDS( DS,gridID,y,x,z,Result, -1)
#*********  CentroidPeakF_Array(array,nrows,ncols,nchans, y,x,z,Result,-1 ) ******** plug in your own written in C, via jni

        VF = ToVec( Result )
        Pk =getNewPeak_xyz( DS, gridID, VF[1], VF[0], VF[2], Seq)
        Seq = Seq + 1
       
        Peaks = Peaks&[Pk]
      endfor
        
    endfor
endfor
# write out the results
SetAttr( DS,"User Name","George User")
WritePeaks(outpath&expname&".peaks",peaks,append)
WriteSCDExp(DS,dss[0],outpath&expname&".x",1,append)

# show the peaks file
ViewASCII(outpath&expname&".peaks")
Display("Peaks are listed in "&outpath&expname&".peaks")

return peaks
