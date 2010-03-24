from DataSetTools.retriever  import *
from gov.anl.ipns.Util.SpecialStrings  import *
from DataSetTools.operator.Generic  import *
from jarray import array  
from jarray import zeros 
from gov.anl.ipns.MathTools.LinearAlgebra import *

class UB2uvAng(GenericOperator):

   
   def calculate(self,  UB, HKL1, HKL2):
      
      Q1 = mult(UB,HKL1)
      Q2 = mult(UB,HKL2)
      u= Q1
      u[2]=0
      v=Q2
      v[2]=0
      
      Ang = Math.atan2(u[1],u[0])
       
      return [u,v,Ang]    

   def setDefaultParameters(self):
      self.super__clearParametersVector()

      self.addParameter(ArrayPG("UB Matrix","[[1,0,0],[0,1,0],[0,0,1]]"))

      self.addParameter(ArrayPG("hkl for u","[1,0,0]"))
      self.addParameter(ArrayPG("hkl for v","[0,1,0]"))
      
      

   def getCommand( self):
      return "UB2uva"

   def getResult( self):
      UB1 = self.getParameter(0).getValue()
      hkl1  = self.getParameter(1).getValue()
      hkl2 = self.getParameter(2).getValue()

# convert to python types( need matrix arithmetic so use jarrays)
      
      UBr0 = array([UB1.get(0).get(0),UB1.get(0).get(1),UB1.get(0).get(2)], 'f')
     
      UBr1 = array([UB1.get(1).get(0),UB1.get(1).get(1),UB1.get(1).get(2)], 'f')
      UBr2 = array([UB1.get(2).get(0),UB1.get(2).get(1),UB1.get(2).get(2)], 'f')
     
      UB = [UBr0,UBr1,UBr2]
      HKL1= array([hkl1.get(0),hkl1.get(1),hkl1.get(2)],'f')
      HKL2 = array([hkl2.get(0),hkl2.get(1),hkl2.get(2)],'f')
      
#------------------code -------------------------------
      X = self.calculate( UB, HKL1, HKL2)

      
      V = Vector()
      V.add(X[0])
      V.add(X[1])
      V.add(X[2])
      
      return V
  
  

   def getDocumentation( self):
      Res = StringBuffer()
      Res.append("   Convert UB matrix to u vector, v vector and angle( mslice input)\n")
      Res.append("@overview   This converts a UB matrix to a form another program needs.\n")
      Res.append("            The user can specify the hkl values that correspond to the u and v vectors.")
      Res.append("@algorithm  \n")
      Res.append("     multiply UB time hkl1 then hkl2 to get u' and v'.\n")
      Res.append("     Project u' and v' to the scattering plane( sets last component to 0(IPNS))\n")
      Res.append("     to determine u and v.\n")
      Res.append("     Determine the angle between the beam(x-IPNS) and vector u.\n")
      Res.append("@param filename  the name of the file with the UB matrix\n")
      Res.append("@param hkl1      The h,k ,l values for the vector that u' that corresponds to u\n")
      Res.append("@param hkl2      The h,k ,l values for the vector that v' that corresponds to v\n")

      Res.append("@return a Vector containing u', v', and the angle between the beam in u' in degrees\n")
      return Res.toString()
     # Constructor
   def __init__( self ):
       Operator.__init__( self, "UB to old mslice input form" )

   def getCategoryList( self):
       
       return ["Macros","Isaw Scripts","SNS"]


