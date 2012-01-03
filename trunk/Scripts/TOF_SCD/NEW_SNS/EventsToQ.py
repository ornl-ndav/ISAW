#  EventsToQ.py

#  Script to write a file of Qx,Qy,Qx of each event.

#  A. J. Schultz, December 2011

from Operators.TOF_Diffractometer import *
from Command import *

class EventsToQ(GenericTOF_SCD):

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(LoadFilePG("Event file:", ""))
        self.addParameter(LoadFilePG("Detector calibration file (.DetCal):", ""))
        self.addParameter(LoadFilePG("Detector bank file (_bank_.xml):", ""))
        self.addParameter(LoadFilePG("Detector ID map file(_TS_.dat)", ""))
        self.addParameter(FloatPG("First event:", "1.0"))
        self.addParameter(FloatPG("Number of events:", "1.0E08"))
        self.addParameter(LoadFilePG("Output file:", ""))
        choicelist = ChoiceListPG("Output file type:", "ASCII")
        choicelist.addItem("Binary_Little_Endian")
        choicelist.addItem("Binary_Big_Endian")
        self.addParameter(choicelist)

    def getResult(self):

        event_file = self.getParameter(0).value
        DetCal_file = self.getParameter(1).value
        bank_file = self.getParameter(2).value
        ID_file = self.getParameter(3).value
        first_event = self.getParameter(4).value
        number_of_events = self.getParameter(5).value
        output_file = self.getParameter(6).value
        output_file_type = self.getParameter(7).value
        
        ScriptUtil.ExecuteCommand("EventToEventQ",[ event_file, DetCal_file, bank_file, ID_file,
            first_event, number_of_events, output_file, output_file_type ])
        
        return "The End"

    def getCategoryList( self):      
        return ["Macros","Single Crystal"] 
        
    def __init__(self):
        Operator.__init__(self,"EventsToQ")
        