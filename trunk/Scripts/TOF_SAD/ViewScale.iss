#      View Scale
#@overview  This script first calls the Scale operator then prepares the output
#    to display the New File along with the polynomial approximation in the
#    selected Graph view
#@algorithm  Scale is used to get the scale factor and the polynomial coeff.
#   Read3Col1D reads the newFile in converting it to a Data Set.
#  LoadExpr is used to create a DataSet whose x values go from Qmin to Qmax
#    and y value are the polynomial approximation.
#  These two data sets are merged and displayed
#@param Standard File   The file with the Standard distribution of Q values
#@param New File    the file with the distribution of Q values on a new instrument
#                  or  updated settings
#@param Qmin  the minimum Q to be used for fitting
#@param Qmax  the maximum Q to be used for fitting
#@param degree  The degree of the polynomial to fit the data between Qmin/max
#@return The scale factor.  The Selected Graph View will be open with the dataset
#  correpsonding to the New File and the Polynomial DataSet merged and selected


$StandFile  LoadFileString("C:\ISAW\batesnist.dat")   Standard File
$NewFile    LoadFileString("C:\ISAW\sn17.dat")        New File
$Qmin        Float(.03)                          Min Q to use
$Qmax        Float(.1)                          Max Q to use
$degree     Integer(3)                          polynomial degree for fitting
$Category=Operator,Generic,TOF_SAD,Scripts

#--------------- Call Scale Operator----------------
V =Scale(StandFile,NewFile,Qmin,Qmax,degree)

#----------- Calculate Info for Polynomial Data Set--------
expr=""
var=""
N=ArrayLength(V)-1
for i in [2:N]
  expr=expr&V[i]
  expr=expr&var
  var=var&"*x"
  if i<N then
    expr=expr&"+"
  endif
endfor

DS = LoadExpr(expr,"x","u","3",Qmin,Qmax, V[1],true)

#----------------- Read in NewFile to a DataSet-------------
Sm= Read3Col1D(NewFile, "Reduce Results")
SetField(DS,"X units", GetField(Sm,"X units"))
SetField(DS,"Y units", GetField(Sm,"Y units"))

#-----------------------Merge the two data sets------------
R=Merge(Sm,DS)

#----------------Select All Groups and Display--------------------
SelectGroups(R,"Group ID",0,20000,"Between Min and Max","Set Selected")
Display R,"Selected Graph View"

#-------------------- Return Scale Factor--------------------
return V[0]
