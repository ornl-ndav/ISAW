#
# File:  find_multiple_peaks2.py
#
# Copyright (C) 2003, Peter F. Peterson
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
# Contact : Peter F. Peterson <pfpeterson@ornl.gov>
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
# Revision 1.3  2003/09/22 22:03:09  bouzekc
# Added call to clearParametersVector().
#
# Revision 1.2  2003/07/14 20:37:41  bouzekc
# Added parameters and code to trim out border pixels.  Now
# returns the name of the peaks file.
#
# Revision 1.1  2003/07/08 23:42:44  bouzekc
# Added to CVS (Chris Bouzek).
#
# 
class find_multiple_peaks2(GenericOperator):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(DataDirPG("Raw Data Path",None))
        self.addParameter(DataDirPG("Output Data Path",None))
        self.addParameter(IntArrayPG("Run Numbers",None))
        self.addParameter(StringPG("Experiment Name",""))
        self.addParameter(IntegerPG("Number of Peaks",50))
        self.addParameter(IntegerPG("Miniumum Peak Intensity",3))
        self.addParameter(BooleanPG("Append",1))
        self.addParameter(LoadFilePG("SCD Calibration File",None))
        self.addParameter(IntegerPG("SCD Calibration File Line",-1))
        self.addParameter(IntArrayPG("Pixel Rows and Columns to Keep","0:100"))

    def getResult(self):
        in_path=self.getParameter(0).value
        out_path=self.getParameter(1).value
        run_numbers=self.getParameter(2).getArrayValue()
        expname=self.getParameter(3).value
        num_peaks=self.getParameter(4).value
        min_int=self.getParameter(5).value
        append=self.getParameter(6).value
        calibfile=self.getParameter(7).value
        calibLine=self.getParameter(8).value
        #get the detector border range
        border = self.getParameter(9).getArrayValue()

        if border != None:
            lowerLimit = border[0]  #lower limit of range
            #upper limit of range
            upperLimit = border[len(border) - 1]
        else:  
            #shouldn't happen, but default to 0:MAX_VALUE
            lowerLimit = 0
            upperLimit = Integer.MAX_VALUE
    
        inst="SCD"
        SharedData.addmsg("Instrument="+inst)
        SharedData.addmsg("Calibration File="+calibfile)
        dsnum=1 # DataSet is number one
        first=1
        util=Util() # get a handle on the utilities class
        for i in run_numbers:
            # load data
            filename="%s%s%05d.RUN" % (in_path,inst,i)
            SharedData.addmsg("Loading "+filename);
            echo=EchoObject("Finding peaks in "+filename) # Echo operator
            echo.getResult()
            ds=util.loadRunfile(filename)                 # Load
            integGrp=IntegrateGroup(ds[0],1,0,50000)      # IntegGroup operator
            monct=integGrp.getResult()
            loadCalib=LoadSCDCalib(ds[dsnum],calibfile,-1,None) # LoadSCDCalib
            loadCalib.getResult()
            #find peaks
            findPeaks=FindPeaks(ds[dsnum],monct,num_peaks,min_int) # FindPeaks
            peaks=findPeaks.getResult()
            #trim out edge peaks (defined by the border parameter)
            peaksArray = peaks.toArray( )
            i = 0
            for k in peaksArray:
                peak = peaksArray[i]
                #see if the peak pixels are within the user defined array.  We are
                #assuming a SQUARE detector, so we'll reject it if the x or y position
                #is not within our range
                if peak.x() > upperLimit or peak.x() < lowerLimit or peak.y() > upperLimit or peak.y() < lowerLimit:
                    peaksVec.remove( k )
                i = i + 1
            centroidPeaks=CentroidPeaks(ds[dsnum],peaks)  # CentroidPeaks
            peaks=centroidPeaks.getResult()
            # write out the results
            writePeaks=WritePeaks(out_path+expname+".peaks",peaks,append) # op
            writePeaks.getResult()
            writeExp=WriteExp(ds[dsnum],ds[0],             # WriteSCDExp
                              out_path+expname+".x",1,append)
            if(first):
                first=0
                append=Boolean(1)
        echo=EchoObject("--- Find Multiple Peaks is done ---") # Echo
        echo.getResult()
        # show the peaks file
        viewASCII=ViewASCII(out_path+expname+".peaks") # ViewASCII
        viewASCII.getResult()
        SharedData.addmsg("Peaks are listed in %s%s.peaks"%(out_path,expname))
        # close the dialog automatically (broken)
        ExitDialog().getResult()
        return out_path+expname+".peaks"

    def __init__(self):
        Operator.__init__(self,"Find Multiple Peaks")
