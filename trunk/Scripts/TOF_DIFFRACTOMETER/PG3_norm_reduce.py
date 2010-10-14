#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.TOF_Diffractometer import GenericTOF_Diffractometer
from gov.anl.ipns.Parameters import * # parameters

class PG3_norm_reduce(GenericTOF_Diffractometer):
    def __init__(self):
        Operator.__init__(self, "PG3_norm_reduce")
        
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/PG3/"
        import DataSetTools
        EMPTY_DS = DataSetTools.dataset.DataSet.EMPTY_DATA_SET


        self.addParameter(LoadFilePG("Event file name (Sample)",
                                     PG3ROOT + "2010_2_11_SCI/3/666/preNeXus/PG3_666_neutron_event.dat"))
        self.addParameter(SampleDataSetPG("Background", EMPTY_DS))
        self.addParameter(SampleDataSetPG("Vanadium", EMPTY_DS))
        self.addParameter(LoadFilePG("DelCal file name",
                                     "/SNS/users/pf9/NEW_PG3.DetCal"))
        self.addParameter(LoadFilePG("TS Banking file name",
                                     PG3ROOT + "2010_2_11_CAL/calibrations/PG3_bank_2010_03_11.xml"))
        self.addParameter(LoadFilePG("TS Mapping file name",
                                     PG3ROOT + "2009_2_11A_CAL/calibrations/PG3_TS_2009_04_17.dat"))
        self.addParameter(LoadFilePG("d-space mapping file",
                                     PG3ROOT + "2010_2_11_CAL/PG3_D664_dspacemap_20100404.dat"))
        self.addParameter(FloatPG("First event to load", 0.))
        self.addParameter(FloatPG("Number of events to load", 1e12))
        self.addParameter(FloatPG("Min d-spacing", 0.2))
        self.addParameter(FloatPG("Max d-spacing", 10.))
        self.addParameter(FloatPG("deltaD/D", 2e-4))

    def getParamValue(self, index):
        return self.getParameter(index).getValue()

    def getResult(self):
        
        return None
