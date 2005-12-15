#                  Fix for LANSCE TOF Diffractometer preNeXus data
#@overview  This script fixes Lansce Diffractometer DataSets that were retrieved
#        for old-NeXus files.  Other files are needed to provide missing information
#@assumptions  The file giving the detector positions in in ISAW_HOME/InstrumentInfo/LANSCE
#          its name is a concatenation of InstName,Bank, Version and has an extension .dat\n
#        The GSAS file is also in the same directory. Only lines with lanl extensions are read.
#        Its name is a concatenation of the Instrument name and gsasVersion name and ".prm".
#@algorithm  it uses the operators Transpose,LoadDetectorInfo,and/or  LoadGSASlanl
#@param DS  The DataSet that is to be transposed
#@param Instr  the name of the instrument(lower case or case sensitive)
#@param Bank   the name of the bank or space if there is only one bank
#@param detVersion  the version for the detector information file. This is appended to the end
#                   the default name.
#@param useGSAS   if true the information in a GSAS parameter file will be incorporated into the
#                 data set
#@param gsasVersion The version of the gsas param file( its extension must be .prm)
#@return  Success.  The DataSet is changed
#


$category=Macros,File,Load,LANSCE
$Title=Fix LANSCE old NeXus Files

$DS       SampleDataSet       Data Set
$Instr    InstName           Instrument Name
$Bank     String("")          Bank name(or blank)
$detVersion   String("")      Detector File Version(tail on filename)
$useGSAS      boolean(false)  Add GSAS info
$gsasVersion   String("")     Version for GSAS(tail on gsas file)
#------------------------ Transpose Data--------------------
if Instr=="loq"
   nrows=128
   ncols=128
   Xlate= cnvrt2intArray1D([3,0,1,2])
   times=[2156.8]
   r=1.015992898828800637
   for i in [1:198]
     times[i]=times[i-1]*r
   endfor
   XScale=VariableXScale(times)
   DS1=Transpose( DS,Xlate,nrows,ncols,1,XScale,true)
   addDataSetOp(DS,"DataSetTools.operator.DataSet.EditList.copy.class")
   copy(DS,DS1)
endif

#----------------------- Incorporate Det Info -------------------
S= Bank& detVersion & ".dat"
if Instr=="smarts"
   S="_detectors.dat"
endif
filename = getSysProp("ISAW_HOME")&"/InstrumentInfo/LANSCE/"&Instr&S
LoadDetectorInfo(DS, filename)


#-------------------- Incorporate GSAS info-----------------------
if  useGSAS
   filename=getSysProp("ISAW_HOME")&"/InstrumentInfo/LANSCE/"&Instr&gsasVersion&".prm"
   LoadGSASlanl(ds,filename)
endif


