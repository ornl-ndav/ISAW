# 
# File:  integrate_multiple_runs2.py
#
# Copyright (C) 2003, Chris M. Bouzek
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
# Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
#           Department of Mathematics, Statistics and Computer Science
#           University of Wisconsin-Stout
#           Menomonie, WI 54751, USA
#
#           Chris M. Bouzek <coldfusion78@yahoo.com>
#
# This work was supported by the National Science Foundation under grant
# number DMR-0218882.
#
# $Log$
# Revision 1.1  2003/07/08 23:43:19  bouzekc
# Added to CVS.
#
#

# Script to integrate peaks in multiple SCD files. 
# $Date$
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
#
# Assumptions:
#  - Data of interest is in histogram 2
#  - The run number requires a '0' before it
#  - There is a "lsxxxx.mat" file for each xxxx run.


class integrate_multiple_runs2( GenericOperator ):
    def setDefaultParameters( self ):
        self.addParameter( DataDirPG( "Raw Data Path", None ) )
        self.addParameter( DataDirPG( "Output Data Path", None ) )
        self.addParameter( ArrayPG( "Run Numbers", None ) )
        self.addParameter( StringPG( "Experiment Name", "" ) )
        choices = ChoiceListPG( "Centering Type", "primitive" )
        choices.addItem( "primitive" ); 
        choices.addItem( "a centered" );
        choices.addItem( "b centered" );
        choices.addItem( "c centered" );
        choices.addItem( "[f]ace centered" );
        choices.addItem( "[i] body centered" );
        choices.addItem( "[r]hombohedral centered" );
        self.addParameter( choices )
        self.addParameter( LoadFilePG( "SCD Calibration File", "" ) )
        self.addParameter( StringPG( "Time Slice Range", "-1:3" ) )
        self.addParameter( IntegerPG("Increase Slice Size By", 1 ) )

    def getResult( self ):
        in_path = self.getParameter( 0 ).value
        out_path = self.getParameter( 1 ).value
        run_numbers = self.getParameter( 2 ).value
        expname = self.getParameter( 3 ).value
        cType = self.getParameter( 4 ).value
        calibfile = self.getParameter( 5 ).value
        range = self.getParameter( 6 ).value
        delta = self.getParameter( 7 ).value

        inst = "SCD"
        SharedData.addmsg( "Instrument = " + inst )
        SharedData.addmsg( "Calibration File = " + calibfile )
        dsnum = 1 # DataSet is number one
        first = 1 # i.e. first = true
        append = 0 # i.e. append = false
        util=Util() # get a handle on the utilities class
        for i in run_numbers:
            # load data
            fileName = "%s%s%05d.RUN" % ( in_path, inst, i )
            integName = "%s%s.integrate" % ( out_path, exp_name )
            matName = "%sls05d.mat" % ( out_path, i )
            SharedData.addmsg( "Loading " + fileName );
            echo = EchoObject("Integrating peaks in " + fileName )# Echo operator
            echo.getResult()
            # equivalent of below would be nn = load( filename, ds )
            ds = util.loadRunfile(fileName)               # Load
            # The calibration file "instprm.dat" must be in the outpath directory.
            loadCalib = LoadSCDCalib( ds[dsnum], calibfile , -1, None )
            loadCalib.getResult()

            #Integrate
            #Gets matrix file "lsxxxx.mat" for each run
            #The "1" means that every peak will be written to the integrate.log file.
            scdInteg = SCDIntegrate( ds[dsnum], integName, matName, cType,
                                     range, delta, 1, append )

            if ( first ):
                first = 0
                append = Boolean( 1 )

        echo = EchoObject( "--- integrate_multiple_runs is done ---") # Echo
        echo.getResult()
        # show the integrate file
        viewASCII = ViewASCII( out_path + expname + ".integrate") # ViewASCII
        viewASCII.getResult()
        SharedData.addmsg( "Peaks are listed in %s%s.integrate" % 
                         ( out_path, expname ) )
        # close the dialog automatically (broken)
        ExitDialog().getResult()

    # Constructor
    def __init__( self ):
        Operator.__init__( self, "Integrate Multiple Runs" )
