#
# File:  ReducePy.py
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
# Revision 1.2  2003/11/13 18:23:03  rmikk
# Added user documentation
#
# Revision 1.1  2003/11/11 20:49:54  rmikk
# Initial Checkin
#
#
# 
from DataSetTools.operator.Generic.TOF_SAD import *
from Wizard.TOF_SAD import *
from Command import ScriptUtil
from DataSetTools.util import *
# This Jython operator form Runs the Reduce operation and prints out the results.
class ReducePy(GenericTOF_SAD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter( ArrayPG("Calibration DataSets",None))
        self.addParameter( ArrayPG("Other DataSets",None))
        self.addParameter( ArrayPG("Qbins",None))
        self.addParameter( BooleanPG("Use Background Transmission", Boolean(3==3)))
        self.addParameter( IntegerPG("#Qx divsions",Integer(-200)))
        self.addParameter( IntegerPG("#Qy divisions", Integer(-200)))
        self.addParameter( FloatPG("X offset of beam in cm",Float(0.0)))
        self.addParameter( FloatPG("Y offset of beam in cm",Float(0.0)))
        self.addParameter( FloatPG("Scale Factor",Float(1.0)))
        self.addParameter( FloatPG("Thickness",Float(1.0)))
        self.addParameter( FloatPG("Neutron Delay fraction",Float(.0011)))
        self.addParameter( DataDirPG("Directory for output", None))
        
        
    def getResult(self):
        Calib = self.getParameter(0).getValue()
        DataS = self.getParameter(1).getValue()
        Qbins = self.getParameter(2).getValue()
        useBackground = self.getParameter(3).getValue()
        NQxdivs = self.getParameter(4).getintValue()
        NQydivs = self.getParameter(5).getintValue()
        Xoff = self.getParameter(6).getfloatValue()
        Yoff = self.getParameter(7).getfloatValue()
        Scale = self.getParameter(8).getfloatValue()
        Thick = self.getParameter(9).getfloatValue()
        NeutronDelay = self.getParameter(10).getfloatValue()
        Path = self.getParameter(11).getStringValue()
        if isinstance( Qbins.elementAt(0), Qbins.elementAt(1).__class__):
           pass
        else:
           Qbins.removeElementAt(0)
        # Cannot change parameter variables anymore so need to require Qbins.size()
        #    to be larger than 5 if  the 1D choice is given
        
        V = Reduce_KCL(DataS[7],Calib[2],Calib[1],Calib[0],Qbins,DataS[0],DataS[1],DataS[2],DataS[3],DataS[4],DataS[5],
          NeutronDelay,Scale,Thick,Xoff,Yoff,NQxdivs,NQydivs, useBackground).getResult()
        if isinstance( V, ErrorString):
          return V
        for i in range(0,3,1):
           filename = Path+V.elementAt(i).getTitle()+".dat"
           if NQxdivs < 0:
              R = Print3Col1D(V[i], filename,"Reduce Results", NeutronDelay).getResult()
              if isinstance( R, ErrorString):
                 return R 
           else:
             R = Print4Col2D1Chan(V[i], filename).getResult()
             if isinstance( R, ErrorString):
                 return R
        ScriptUtil.display( V[0])
        return "Success"

    def getDocumentation(self):
        S = "@overview This Form is part of the Reduce Wizard. The actual analysis is done "
        S += "in this form.  See the Wizard Help for more information "
        S += " @param  Xoffset   The x(horizontal) offset the beam center is from the area detector center"
        S += " @param  yoffset   The y(vertical)offset the beam center is from the area detector center"
        S += " @param  Scale     A scale factor to standardize the results."
        S += " @param  Thickness   the thickness of the area detector center."
        S += " @param  Neutron Delay   the fraction of the neutrons that are delayed neutrons and are subtracted"
        S +=  " from the count."
        S += " @param  output directory   The directory where the three results are saved. Their"
        S += "  names will be s****.dat, b****.dat, sn****.dat for the 1D analysis and"
        S += " for the 2D analysis the names will be s2d****.dat, b2d****.dat, sn2d****.dat."
        S += " The **** represents the run number corresponding to the sample"
 
        return S

    def __init__(self):
        Operator.__init__(self,"Reduce")
