#
# File:  Reduce_Form3.py
#
# Copyright (C) 2003, Ruth Mikkelson
#
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
#
# Contact : Ruth Mikkelson <MikkelsonR@uwstout.edu>
#           Advanced Photon Source Division
#           Argonne National Laboratory
#           9700 South Cass Avenue
#           Argonne, IL 60439-4845, USA
#
# This work was supported by the National Science Foundation under grant
# number DMR-0218882.
#
# Modified:
#
# $Log$
# Revision 1.3  2004/01/05 17:52:10  rmikk
# Jython class now subclass of GenericOperator
#
# Revision 1.2  2003/11/13 18:24:25  rmikk
# Added user Documentaiton
#
# Revision 1.1  2003/11/11 20:49:35  rmikk
# Initial Checkin
#
# 
from DataSetTools.operator.Generic.TOF_SAD import *
from DataSetTools.operator import *
from Wizard.TOF_SAD import *
from Command import ScriptUtil
from java.util import Vector
from java.lang import Float
from DataSetTools.util  import StringUtil

# This form operator allows for specifying the Q binning options.
class Reduce_Form3(GenericOperator):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        V= Vector()
        V.addElement("1D")
        V.addElement("2D")
        butt = RadioButtonPG("Output", V ,Boolean(3==3))
        self.addParameter( butt)
        BoolPG = BooleanPG("Use Defaults", Boolean(3==3))
        self.addParameter( BoolPG)
        #----------- 1D -----------------
        Qbins = QbinsPG("Enter Q bins", None)
        
        #-------------2D----------------
        Qxmin =FloatPG("Enter Min Qx",-.5)
        Qxmax =FloatPG("Enter Min Qx",.5)
        Qymin =FloatPG("Enter Min Qx",-.5)
        Qymax =FloatPG("Enter Min Qx",.5)
        NQxDivs = IntegerPG("Number bins in Qx dir", 200)
        NQyDivs =IntegerPG("Number bins in Qy dir", 200)
        self.addParameter( Qbins)
        self.addParameter( Qxmin)
        self.addParameter(Qxmax)
        self.addParameter( Qymin)
        self.addParameter( Qymax)
        self.addParameter(NQxDivs)
        self.addParameter( NQyDivs)  #parameter 8
        S = [self.getParameter(3),self.getParameter(4),self.getParameter(5),self.getParameter(6),self.getParameter(7),self.getParameter(8)]
        self.getParameter(0).addPropertyChangeListener( PChangeListener(self.getParameter(2),"1D"))
        self.getParameter(0).addPropertyChangeListener( PChangeListener(S,"2D"))
      
        S = [self.getParameter(2),self.getParameter(3),self.getParameter(4),self.getParameter(5),self.getParameter(6)]
        self.getParameter(1).addPropertyChangeListener( PChangeListener(S,Boolean(2==3)))
    def getResult(self):
        Dimension = self.getParameter(0).getStringValue()
        useDefaults = self.getParameter(1).value
        if Dimension == "1D":
           self.getParameter(7).setValue( Integer(-200))
           self.getParameter(8).setValue( Integer(-200))
           if useDefaults:
              Q = Vector()
              Q.addElement( Float(.0035))
              X = .0035
              for i in range(1,117,1):
                 X = X*1.05
                 Y = Float(X)
                 Q.addElement(  Float(X))
              return Q
           else:
              return self.getParameter(2).getValue()
        else:
           Q = Vector()
           Q.addElement( self.getParameter(3).getValue())
           Q.addElement( self.getParameter(4).getValue())
           Q.addElement( self.getParameter(5).getValue())
           Q.addElement( self.getParameter(6).getValue())
           return Q

    def getDocumentation( self):
           S = "@overview This Form is part of the Reduce Wizard. It allows for the entry "
           S += "the type of Reduce analysis desired(1D or 2D). Also the bin boundaries"
           S += " to be used in the analysis. <P>For the 1D analysis, the bin boundaries can be" 
           S += " practically arbitrary. They can be combinations of different lists with"
           S += " different common ratios or different common differences. In the Set Qbins or Qx,.. dialog"
           S += " box, if N Steps is 0 or less, only the entry from Start Q is added. This"
           S += " means that any list can be entered.  It should be increasing" 
           return S                 

    def __init__(self):
        Operator.__init__(self,"Get Qbins")
