#!/bin/env jython

from DataSetTools.operator import Operator
from DataSetTools.operator.Generic.Load import GenericLoad
from gov.anl.ipns.Parameters import * # parameters
from Command import ScriptUtil

class PG3_vanadium(GenericLoad):
    def __init__(self):
        Operator.__init__(self, "PG3_vanadium")

    def getCommand(self):
        return "PG3_vanadium"

    def setDefaultParameters(self):
        self.super__clearParametersVector()

        # copy all of the parameters from the main script
        self.focus_op = ScriptUtil.getOperator("PG3_to_dspace", None)
        self.CopyParametersFrom(self.focus_op)
        runnum = self.getParameter(0)
        runnum.setValue(733)

        # add the peak removal stuff
        self.remove_op = ScriptUtil.getOperator("RemovePeaks", None)
        numParam = self.remove_op.getNum_parameters()
        for i in xrange(1, numParam):
            self.addParameter(self.remove_op.getParameter(i).clone())

    def getParamValue(self, index):
        param = self.getParameter(index)
        return param.getValue()

    def fixUnits(self, dataset):
        """This needs to be done if the label and units were interchanged"""
        units = dataset.getX_units()
        label = dataset.getX_label()
        if units == "Time-of-flight":
            dataset.setX_units(label)
            dataset.setX_label(units)

    def send(self, ds, showPlots, sendData):
        if ds is None:
            return
        from Command import ScriptUtil
        if showPlots:
            ScriptUtil.display(ds)
        if sendData:
            pass # should do something here

    def getResult(self):
        # whether or not to send all datasets to the tree
        sendData = self.getParamValue(12)

        # whether or not to plot intermediate results
        showData = self.getParamValue(13)

        # load and time focus
        focus_num_param = self.focus_op.getNum_parameters()
        for i in xrange(focus_num_param):
            self.focus_op.setParameter(self.getParameter(i), i)
        dataset = self.focus_op.getResult()
        self.fixUnits(dataset)

        # remove peaks
        param = self.remove_op.getParameter(0)
        param.setValue(dataset)
        for i in xrange(4):
            self.remove_op.setParameter(self.getParameter(i + 12), i)
        result = self.remove_op.getResult()
        #result = ScriptUtil.ExecuteCommand(dataset, "RemovePeaks", None)
        if "failed" in str(result):
            return result

        return dataset
