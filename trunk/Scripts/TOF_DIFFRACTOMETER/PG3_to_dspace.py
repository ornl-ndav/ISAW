#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from gov.anl.ipns.Util.SpecialStrings import ErrorString

class PG3_to_dspace(GenericLoad):
    def __init__(self):
        Operator.__init__(self, "PG3_to_dspace")
        self.instr = "PG3"
        try:
            self.IOBS = IOBS
        except:
            self.IOBS = None

    def getCommand(self):
        return "PG3_to_dspace"

    def getCalibPG(self, type):
        cmd = "findcalib -i pg3 --listall " + type
        print cmd
        from java.lang import Runtime
        proc = Runtime.getRuntime().exec(cmd)
        print proc
        import ExtTools.monq.stuff.Exec
        thing = ExtTools.monq.stuff.Exec(proc)
        if not thing.done():
            pass
        print thing.getOutputText()
        return None # should make a system call to findcalib

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        PG3ROOT = "/SNS/PG3/"
        self.addParameter(IntegerPG("PG3 run number", 1370))          # param 0
        self.addParameter(LoadFilePG("Configuration file name",
                    "/SNS/PG3/2010_2_11A_CAL/PG3_ISAW_1Bank_config_2010-B.py")) # param 1
        self.addParameter(FloatPG("First event to load", 0.))        # param 2
        self.addParameter(FloatPG("Number of events to load", 1e12)) # param 3
        self.addParameter(FloatPG("Min d-spacing", 0.2))             # param 4
        self.addParameter(FloatPG("Max d-spacing", 5.0))             # param 5
        self.addParameter(FloatPG("deltaD/D", 2e-4))                 # param 6
        self.addParameter(FloatPG("normalize to pcharge", 1e13))     # param 7
        self.addParameter(BooleanPG("Send all data to tree", False)) # param 8
        self.addParameter(BooleanPG("Show plots", False))            # param 9

    def getParamValue(self, index):
        param = self.getParameter(index)
        if param is None:
            return None
        return param.getValue()

    def sumGroups(self, inds, outds, groups, newId, position):
        data = inds.getData_entry_with_id(groups[0])
        if data is None:
            raise "Something went wrong with group id = %d" % groups[0]
        for i in groups[1:]:
            data = data.add(inds.getData_entry_with_id(i))
            if data is None:
                raise "Something went wrong with group id = %d" % i
        data.setGroup_ID(newId)
        data.setAttribute(position)
        outds.addData_entry(data)

    def getPosAttr(self, position):
        (bragg, L2) = position
        from gov.anl.ipns.MathTools.Geometry import Vector3D, DetectorPosition
        import math
        pos = [L2*math.cos(bragg*math.pi/180.),
               L2*math.sin(bragg*math.pi/180.),
               0., 0.] # last one is the weight
        pos = Vector3D(pos)
        pos = DetectorPosition(pos)

        from DataSetTools.dataset import DetPosAttribute
        attr = DetPosAttribute("Effective Position", pos)
        
        return attr

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

    def fixUnits(self, dataset):
        """This needs to be done if the label and units were interchanged"""
        units = dataset.getX_units()
        label = dataset.getX_label()
        if units == "Time-of-flight":
            dataset.setX_units(label)
            dataset.setX_label(units)

    def removeZeros(event, dataset):
        ids = range(dataset.getNum_entries())
        for i in ids:
            spectrum = dataset.getData_entry(i)
            y = spectrum.getY_values()
            dy = spectrum.getErrors()
            for j in xrange(len(y)):
                if y[j] == 0.:
                    y[j] = 1.
                    dy[j] = 1.
            #dataset.replaceData_entry(spectrum, i)

    def send(self, ds, showPlots, sendData):
        if ds is None:
            return
        from Command import ScriptUtil
        if showPlots:
            ScriptUtil.display(ds)
        if sendData and self.IOBS is not None:
            ScriptUtil.send(ds, self.IOBS)

    def getResult(self):
        # get the configuration file and load it
        execfile(self.getParamValue(1))
        config = getConfig()

        # whether or not to send all datasets to the tree
        sendData = self.getParamValue(8)

        # whether or not to plot intermediate results
        showData = self.getParamValue(9)

        # ghost file information
        ghostFile = config.get('GHOST_MAP', None)
        if ghostFile is None or len(ghostFile) <= 0:
            ghostFile = None

        # get the information about the run to process
        runnum = self.getParamValue(0)
        (eventfile, pcharge) = self.getRunStuff(runnum)
        print "pcharge = %.0f" % pcharge
        if float(self.getParamValue(7)) > 0.:
            pcharge = pcharge/float(self.getParamValue(7))
        else:
            pcharge = 1.
        print "scale = %.2f" % pcharge


        d_min = float(self.getParamValue(4)) # d-space min
        log_param = float(self.getParamValue(6)) # deltaD/D
        log_param = log_param * d_min # log param

        # do the initial load and time focus
        from Command import ScriptUtil
        args = []
        args.append(eventfile) # event file
        args.append(config['DET_CAL']) # detcal file
        args.append(config['TS_BANK']) # banking file
        args.append(config['TS_MAP']) # mapping file
        args.append(self.getParamValue(2)) # first event
        args.append(self.getParamValue(3)) # number of events
        args.append(d_min) # d-space min
        args.append(float(self.getParamValue(5))) # d-space max
        args.append(True) # use log binning
        args.append(log_param) # log param
        args.append(10000) # number of bins - not used
        args.append(True) # use d-space map
        args.append(config['DSPACE_MAP']) # dspace map file
        args.append(False)
        args.append("")
        op = ScriptUtil.getOperator("Make_d_DataSet", args)
        panel_ds = op.getResult()
        if isinstance(panel_ds, ErrorString): # error occured
            return panel_ds

        self.send(panel_ds, showData, sendData)

        panel_ds.setSqrtErrorsAtLeast_1()

        # create the ghost histogram and plot it
        args[-2] = (not ghostFile is None) # generate ghosts
        if ghostFile is not None:
            args[-1] = ghostFile
            op = ScriptUtil.getOperator("Make_d_DataSet", args)
            ghost_ds = op.getResult()
            if isinstance(ghost_ds, ErrorString): #error occured
                return ghost_ds
            self.send(ghost_ds, showData, sendData)
            op = panel_ds.getOperator("Subtract a DataSet")
            other = op.getParameter(0)
            other.setValue(ghost_ds)
            op.getResult()
            self.send(panel_ds, showData, sendData)

        panel_ds.clampToZero()

        (groups, positions) = getGrouping()
        for key in positions.keys():
            positions[key] = self.getPosAttr(positions[key])
        
        # sum spectra together
        gsas_ds = panel_ds.empty_clone()
        for key in groups.keys():
            self.sumGroups(panel_ds, gsas_ds, groups[key], key, positions[key])
        title = gsas_ds.getTitle() + " summed"
        gsas_ds.setTitle(title)

        self.send(gsas_ds, showData, sendData)
        
        # convert back to tof
        op = gsas_ds.getOperator("ToTof")
        if op is None:
            op = gsas_ds.getOperator("Convert d-Spacing to TOF")
        gsas_tof = op.getResult()
        if isinstance(gsas_tof, ErrorString): # error occured
            return gsas_tof
        title = gsas_tof.getTitle().replace("_d-spacing", " tof")
        gsas_tof.setTitle(title)
        self.send(gsas_tof, showData, sendData)

        # normalize the data
        if pcharge != 1.:
            op = gsas_tof.getOperator("Divide by Scalar")
            scalar = op.getParameter(0)
            import java.lang.Float
            scalar.setValue(java.lang.Float(pcharge))
            op.getResult()

        self.fixUnits(gsas_tof)
        self.removeZeros(gsas_tof)

        #return gsas_ds
        return gsas_tof
