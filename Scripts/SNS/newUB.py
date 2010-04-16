from DataSetTools.operator.Generic  import *
from gov.anl.ipns.MathTools import *
from Operators.TOF_SCD import *
from DataSetTools.components.ui.Peaks import *
from gov.anl.ipns.MathTools import *
from DataSetTools.operator.Generic.TOF_SCD import *

class newUB(GenericOperator):
  
   def calculate(self,  UB, HKL1, HKL2, HKL1_new, HKL2_new):
       Q1 = LinearAlgebra.mult( UB, HKL1)
       Q2 = LinearAlgebra.mult(UB,HKL2)
#       Q1a = LinearAlgebra.double2float(Q1)
#       Q2a = LinearAlgebra.double2float(Q2)
       Q1a=Q1
       Q2a=Q2

       UBnew =  subs.getOrientationMatrix(Q1a,HKL1_new, Q2a,HKL2_new, UB)

       UBnewa = LinearAlgebra.float2double(UBnew)
       Lat2 =  lattice_calc.LatticeParamsOfUB(UBnewa)
       UBdbl = LinearAlgebra.float2double(UB)

       Lat1 =  lattice_calc.LatticeParamsOfUB(UBdbl)
       print 
       return UBnewa 

   def setDefaultParameters(self):
       self.super__clearParametersVector()

       self.addParameter(LoadFilePG("UB Matrix file",System.getProperty("Data_Directory")))

       self.addParameter(ArrayPG("UB-hkl for reflection 1","[-1,0,3]"))
       self.addParameter(ArrayPG("UB-hkl for reflection 2","[-3,-1,3]"))
       self.addParameter(ArrayPG("Desired hkl for reflection 1","[-3,-1,-9]"))
       self.addParameter(ArrayPG("Desired hkl for reflection 2","[-5,-1,6]"))
       self.addParameter(SaveFilePG("File to save result",System.getProperty("Data_Directory")))
      
    

   def getCommand( self):
       return "newUB"

   def getResult( self):
       filename = self.getParameter(0).getValue()
       hkl1  = self.getParameter(1).getValue()
       hkl2 = self.getParameter(2).getValue()
       hkl1_new  = self.getParameter(3).getValue()
       hkl2_new = self.getParameter(4).getValue()
       outfile = self.getParameter(5).getValue()
      
       UB = IndexJ.readOrient(filename)
      
       HKL1 =[ hkl1.elementAt(0), hkl1.elementAt(1),hkl1.elementAt(2)]
       HKL2 =[ hkl2.elementAt(0), hkl2.elementAt(1),hkl2.elementAt(2)]
       HKL1_new =[ hkl1_new.elementAt(0), hkl1_new.elementAt(1),hkl1_new.elementAt(2)]
       HKL2_new =[ hkl2_new.elementAt(0), hkl2_new.elementAt(1),hkl2_new.elementAt(2)]
   
     
#-----------------code -------------------------------
       X = self.calculate( UB, HKL1, HKL2,HKL1_new, HKL2_new)

       print "New UB transpose is="
       S =""
      
       S = String.format(" %10.6f  %10.6f %10.6f\n",[ X[0][0],X[1][0],X[2][0]]) 
       S = S+String.format(" %10.6f  %10.6f %10.6f\n",[ X[0][1],X[1][1],X[2][1]]) 
       S = S+String.format(" %10.6f  %10.6f %10.6f\n\n",[ X[0][2],X[1][2],X[2][2]])
       print S  
       print "Lattice parameters"
       L= lattice_calc.LatticeParamsOfUB(X)   
       print String.format("  %8.3f  %8.3f   %8.3f  %8.3f  %8.3f  %8.3f\n\n", [L[0],L[1],L[2],L[3],L[4],L[5],L[6]])
       print "Check Point1 Q values"
       Q1 = LinearAlgebra.mult(UB,HKL1)
       X1 = LinearAlgebra.double2float(X)
       Qn1 = LinearAlgebra.mult( X1,HKL1_new)
       print Q1
       print Qn1
       print "Check Point2 Qvalues"
       Q1 = LinearAlgebra.mult(UB,HKL2)
       Qn1 = LinearAlgebra.mult( X1,HKL2_new)
       print Q1
       print Qn1

       Util.WriteMatrix( outfile, X1)
       return X1


   def getDocumentation( self):
       Res = StringBuffer()
       Res.append("   Finds an equivalent UB matrix that maps two hkl values from the\n")
       Res.append(" original UB matrix to alternative hkl values\n")
       Res.append("@param filename  the name of the file with the UB matrix\n")
       Res.append("@param hkl1      The h,k ,l values from the UB matrix in the file for the first reflection \n")
       Res.append("@param hkl2      The h,k ,l values from the UB matrix in the file for the second reflection \n")
       Res.append("@param hkl1_new      The desired h,k ,l values f for the first reflection \n")
       Res.append("@param hkl1_new      The desired h,k ,l values for the second reflection \n") 

       Res.append("@param OutputFile   The name of the file to save the new matrix ton \n") 
       Res.append("@return a new UB matrix\n")
       return Res.toString()
  

   def getCategoryList( self):
       
       return ["Macros","Isaw Scripts","SNS"]

   def __init__( self ):
       Operator.__init__( self, "new UB Rotated" )
       


