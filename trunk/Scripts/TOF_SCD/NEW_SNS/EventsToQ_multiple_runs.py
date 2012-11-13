#  EventsToQ.py

#  Script to write a file of Qx,Qy,Qx of each event.

#  A. J. Schultz, December 2011

from Operators.TOF_Diffractometer import *
from Command import *
from time import clock


class EventsToQ_multiple_runs(GenericTOF_SCD):

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Working directory", "C:\data\TOPAZ\Triphylite"))
        # self.addParameter(LoadFilePG("Event file:", ""))
        self.addParameter(IntArrayPG("Run Numbers", "3131:3145"))
        self.addParameter(LoadFilePG("Detector calibration file (.DetCal):", "C:\data\TOPAZ\2011_05_11_CAL\TOPAZ_2011_02_16.DetCal"))
        self.addParameter(LoadFilePG("Detector bank file (_bank_.xml):", "C:\data\TOPAZ\2011_05_11_CAL\TOPAZ_bank_2011_02_16.xml"))
        self.addParameter(LoadFilePG("Detector ID map file(_TS_.dat)", "C:\data\TOPAZ\2011_05_11_CAL\TOPAZ_TS_2011_02_16.dat"))
        self.addParameter(FloatPG("First event:", "1.0"))
        self.addParameter(FloatPG("Number of events:", "1.0E09"))
        # self.addParameter(LoadFilePG("Output file:", ""))
        choicelist = ChoiceListPG("Output file type:", "Binary_Little_Endian(PC)")
        choicelist.addItem("Binary_Big_Endian(Java)")
        choicelist.addItem("ASCII")
        self.addParameter(choicelist)

    def getResult(self):
        start = clock()
        directory = self.getParameter(0).value
        # event_file = self.getParameter(0).value
        run_numbers=self.getParameter(1).getArrayValue()
        DetCal_file = self.getParameter(2).value
        bank_file = self.getParameter(3).value
        ID_file = self.getParameter(4).value
        first_event = self.getParameter(5).value
        number_of_events = self.getParameter(6).value
        # output_file = self.getParameter(6).value
        output_file_type = self.getParameter(7).value
        
        number_of_runs = len(run_numbers)
        for i in range(number_of_runs):
            srun = str(run_numbers[i])
            print 'run number ' + srun
            event_file = directory + 'TOPAZ_' + srun + '_neutron_event.dat'
            output_file = directory + 'TOPAZ_' + srun + '_EventsToQ.bin'
            ScriptUtil.ExecuteCommand("EventToEventQ",[ event_file, DetCal_file, bank_file, ID_file,
                first_event, number_of_events, output_file, output_file_type ])
        
        end = clock()
        elapsed = end - start
        print '\nElapsed time is %f seconds.' % elapsed
        print '\nAll done!' 

        return "The End"

    def getCategoryList( self):      
        return ["Macros","Single Crystal"] 
        
    def __init__(self):
        Operator.__init__(self,"EventsToQ_multiple_runs")
        