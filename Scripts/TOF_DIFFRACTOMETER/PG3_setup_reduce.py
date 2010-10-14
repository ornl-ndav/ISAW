#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.TOF_Diffractometer import GenericTOF_Diffractometer
from gov.anl.ipns.Parameters import * # parameters

class PG3_setup_reduce(GenericTOF_Diffractometer):
    def __init__(self):
        Operator.__init__(self, "PG3_setup_reduce")
        self.instr = "PG3"
        
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/PG3/"

        self.addParameter(IntegerPG("Background runnumber", 794))
        self.addParameter(IntegerPG("Vanadium runnumber",733))

        #self.addParameter(LoadFilePG("Event file name (Sample)",
        #                             PG3ROOT + "2010_2_11_SCI/3/666/preNeXus/PG3_666_neutron_event.dat"))
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
        param = self.getParameter(index)
        return param.getValue()

    def getRunDir(self, runnumber):
        cmd = "findnexus -i pg3 -A %d" % runnumber
        import os
        f = os.popen(cmd)
        nxs = f.readline()
        nxs = nxs.strip()
        if not os.path.exists(nxs):
            raise Error, "Failed to find run \"%d\": %s"  % (runnumber, nxs)
        temp = os.path.split(nxs)[0] # NeXus directory
        temp = os.path.split(temp)[0] # run directory
        return (nxs, os.path.join(temp, "preNeXus"))

    def getRunStuff(self, runnumber):
        import os
        # get the nexus file and prenexus directory
        (nxs, prenxs) = self.getRunDir(runnumber)

        # determine the event file
        event = "%s_%d_neutron_event.dat" % (self.instr, runnumber)
        event = os.path.join(prenxs, event)
        if not os.path.exists(event):
            raise Error, "%s does not exist" % event

        # determine the cvinfo file
        cvinfo = "%s_%d_cvinfo.xml" % (self.instr, runnumber)
        cvinfo = os.path.join(prenxs, cvinfo)
        if not os.path.exists(cvinfo):
            raise Error, "%s does not exist" % cvinfo

        # get the protoncharge
        pcharge = 1.
        cvinfo = open(cvinfo, "r")
        temp = cvinfo.readline()
        while not "protoncharge" in temp:
            temp = cvinfo.readline()
        if len(temp) > 0:
            start = temp.index("value=\"") + len("value=\"")
            stop = temp.index("\"", start + 1)
            pcharge = float(temp[start:stop])
        cvinfo.close()

        return (event, pcharge)

    def process(self, eventfile, pcharge):
        from Command import ScriptUtil

        # do the time focusing
        args = []
        args.append(eventfile)
        for i in xrange(2,self.getNum_parameters()):
            args.append(self.getParamValue(i))
        op = ScriptUtil.getOperator("PG3_to_dspace", args)
        dataset = op.getResult()

        # normalize by the scalar
        op = dataset.getOperator("Divide by Scalar")
        scalar = op.getParameter(0)
        import java.lang.Float
        scalar.setValue(java.lang.Float(pcharge))
        op.getResult()

        return dataset

    def getResult(self):
        from Command import ScriptUtil
        # turn the run numbers into something useful
        try:
            backRun = self.getRunStuff(self.getParamValue(0))
            normRun = self.getRunStuff(self.getParamValue(1))
        except Error, e:
            return e

        # process the background
        back_ds = self.process(backRun[0], backRun[1])

        # process the vanadium
        norm_ds = self.process(normRun[0], normRun[1])

        return (back_ds, norm_ds)
