#
# File:  Reduce_Form1.py
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
# Contact : Ruth Mikkelsonn <MikkelsonR@uwstout.edu>
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
# Revision 1.2  2003/11/13 18:23:51  rmikk
# Added user documentation
#
# Revision 1.1  2003/11/11 20:48:28  rmikk
# Initial Checkin
#
#
# 
from DataSetTools.operator.Generic.TOF_SAD import *
from DataSetTools.operator import *
from Wizard.TOF_SAD import *
from java.util import Vector
from DataSetTools.wizard import *
# This Jython operator form groups the Calibration type files inputs needed to run 
# run the Reduce operation.  These files include the sensitivity run,the 
# efficiency run and the background transmission run
class Reduce_Form1(GenericOperator):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        FilePG =LoadFilePG("Sensitivity File(*.dat)",None)
        FF = GenericFileFilter(".dat","Sensitivity File(*.dat)")
        FilePG.addFilter(FF)
        self.addParameter(FilePG )
         
        FilePG =LoadFilePG("Efficiency File",None)
        FF = GenericFileFilter(".dat","Efficiency File(*.dat)")
       
        
        FilePG.addFilter(FF)
        self.addParameter(FilePG )

        useBackTransmissionPG = BooleanPG("Use Background Transm", Boolean(3==3))
        
        self.addParameter( useBackTransmissionPG)
        FilePG=LoadFilePG("BackGround Transmission File",None)
        FilePG.addFilter(GenericFileFilter(".cf","Transmission File(*.cf)"))
        self.addParameter(FilePG)
        useBackTransmission = self.getParameter(2)
        FilePG = self.getParameter(3)
        useBackTransmission.addPropertyChangeListener( PChangeListener( FilePG))
        
    def getTitle(self):
        return "Select Calibration files"
    def getResult(self):
        SensitivityFilename=self.getParameter(0).value
        EfficiencyFilename=self.getParameter(1).value
        
        useBackGroundTransmission =self.getParameter(2).value
        if useBackGroundTransmission: 
           BackGroundTransmissionFilename=self.getParameter(3).value
        else:
           BackGroundTransmissionFilename =""
        SensitivityDataSets = ReadFlood(SensitivityFilename, 128,128).getResult()
        if isinstance( SensitivityDataSets, ErrorString):
           return SensitivityDataSets
        EfficiencyDataSet =Read3Col1D( EfficiencyFilename,"Efficiency").getResult()
        if isinstance( EfficiencyDataSet, ErrorString):
           return EfficiencyDataSet
        D = EfficiencyDataSet.getData_entry(0)
        xsc = D.getX_scale()
        n=xsc.getNum_x()
        
        if useBackGroundTransmission: 
           BackGroundTransmissionDataSet =ReadTransmission(BackGroundTransmissionFilename,n).getResult()
           if isinstance( BackGroundTransmissionDataSet, ErrorString):
              return BackGroundTransmissionDataSet
           else:
              pass
        else:
           BackGroundTransmissionDataSet = DataSet.EMPTY_DATA_SET
        V = Vector()
        V.addElement( SensitivityDataSets.elementAt(0))
        V.addElement( EfficiencyDataSet)
        V.addElement( BackGroundTransmissionDataSet)
        return V;
  
    def getDocumentation(self):
        S = ""+"@overview This Form is part of the Reduce Wizard. It allows for the entry "
        S += "of filenames of preprocessed Calibration type information for the Reduce analysis."
        S += " These files include the efficiency, sensitivity, and background tranmission files"
        return S
    def __init__(self):
        Operator.__init__(self,"Select Calibration files")
