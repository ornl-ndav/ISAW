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
# Revision 1.2  2003/11/13 18:24:08  rmikk
# Added user documentation
#
# Revision 1.1  2003/11/11 20:49:19  rmikk
# Initial Checkin
#
#
# 
from DataSetTools.operator.Generic.TOF_SAD import *
from Wizard.TOF_SAD import *
from Command import ScriptUtil
from java.util  import Vector
from DataSetTools.util import ErrorString
# This form groups all the other nonCalibration type inputs to the Reduce 
# program except for the Q binning method. This includes the Sample Run,
# Cadmium run, Background run, and the sample Transmission run 
class Reduce_Form2(GenericTOF_SAD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        FilePG =LoadFilePG("Sample Runfile",None)
        FF = GenericFileFilter(".run","Run File(*.run)")
        FilePG.addFilter(FF)
        self.addParameter(FilePG )

        FilePG =LoadFilePG("Background Run file",None)
        FilePG.addFilter(FF)
        self.addParameter(FilePG )
        
        BoolPG = BooleanPG("Use Cadmium Run",  Boolean(3==3))
        self.addParameter( BoolPG)

        
        File1PG=LoadFilePG("Cadmium Run File",None)
        File1PG.addFilter(FF)
        self.addParameter(File1PG)

        BoolPG1 = BooleanPG( "Calculate Sample Transmission", Boolean(3==2))
        self.addParameter( BoolPG1)


        FilePG2= LoadFilePG("Sample Transmission Run", None)
        FilePG2.addFilter( GenericFileFilter(".cf","Transmission(*.cf)"))
        self.addParameter( FilePG2)
        
        FloatPG1 =FloatPG("Neutron Delay fraction",  Float(.0011))
        self.addParameter( FloatPG1)
        IntPG1= IntegerPG("Start Time Chan(Polyn fit)",  Integer(11))
        self.addParameter(IntPG1)
        IntPG2= IntegerPG("End Time Chan(Polyn fit)",  Integer(68))
        self.addParameter(IntPG2) 
        IntPG3=IntegerPG("Degree for Polynomial",  Integer(3))
        self.addParameter(IntPG3) 
        BoolPG3 = BooleanPG("Use 1/sqrt y weights",  Boolean(3==2))
        self.addParameter(BoolPG3)
        self.getParameter(2).addPropertyChangeListener( PChangeListener( self.getParameter(3),Boolean(3==3)))
        S =[self.getParameter(6),self.getParameter(7),self.getParameter(8),self.getParameter(9),self.getParameter(10)]
        self.getParameter(4).addPropertyChangeListener(  PChangeListener( S,Boolean(3==3)))
        self.getParameter(4).addPropertyChangeListener( PChangeListener(self.getParameter(5),Boolean(3==2)))
    def getResult(self):
        
        SampleFileName = self.getParameter(0).value
        BackGroundFileName = self.getParameter(1).value
        useCadmium =self.getParameter(2).getbooleanValue()
        if useCadmium:
           CadmiumFileName = self.getParameter(3).value
        else:
           CadmiumFileName = ""
        calcTransmission = self.getParameter(4).getbooleanValue()
        if calcTransmission:
          TransmissionFileName = ""
        else:
          TransmissionFileName = self.getParameter(5).value

        SampleDataSets = ScriptUtil.load( SampleFileName)
        if SampleDataSets == None:
           return ErrorString("Could not load Sample File")

        BackGroundDataSets = ScriptUtil.load( BackGroundFileName)
        if BackGroundDataSets == None:
           return ErrorString("Could not load BackGroundFile")
        if useCadmium:
          CadmiumDataSets = ScriptUtil.load( CadmiumFileName)
          if CadmiumDataSets == None:
             return ErrorString("Cannot load Cadmium File")
          else:
             pass
        else:
          CadmiumDataSets = [ DataSet.EMPTY_DATA_SET,DataSet.EMPTY_DATA_SET]

        n = SampleDataSets[1].getData_entry(0).getX_scale().getNum_x()-1
        if calcTransmission:
          NeutronDelay = self.getParameter(6).getfloatValue()
          polyfitIndx1 = self.getParameter(7).getintValue()
          polyfitIndx2 = self.getParameter(8).getintValue()
          polyDegree = self.getParameter(9).getintValue()
          sqrtWeight =self.getParameter(10).getbooleanValue()
          TransmissionDataSet = CalcTransmission(SampleDataSets[0],BackGroundDataSets[0],CadmiumDataSets[0],SampleDataSets[1],useCadmium,NeutronDelay,polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight).getResult()
          if isinstance( TransmissionDataSet, ErrorString):
             return TransmissionDataSet
        else:
           TransmissionDataSet = ReadTransmission( TransmissionFileName, n).getResult()
           if isinstance(TransmissionDataSet, ErrorString):
              return TransmissionDataSet

        V = Vector()
        V.addElement( SampleDataSets[0])
        V.addElement(SampleDataSets[1])
        V.addElement(BackGroundDataSets[0])
        V.addElement(BackGroundDataSets[1])
        V.addElement(CadmiumDataSets[0])
        V.addElement(CadmiumDataSets[1])
        V.addElement(Boolean( useCadmium))
        V.addElement( TransmissionDataSet)
        return V

    def getDocumentation( self):
        S = "@overview This Form is part of the Reduce Wizard. It allows for the entry "
        S += "of filenames that will be processed by the Reduce analysis."
        S += " These files include the Sample, Background, and Cadmium raw run files."
        S += " The background sample transmission information can also be calculated from"
        S += " the raw run file. The Neutron Delay Fraction and time channels to use for"
        S += " fitting a polynomial to the result are needed for this calculation "
        return S      
    def __init__(self):
        Operator.__init__(self,"Select Run files")
