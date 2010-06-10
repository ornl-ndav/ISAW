#                  Anvred
#@overview  This script is a front end to the Anvred executable
#@assumption  The anvred executable is named {IsawHome}/anvred/anvred_? where
#              ? depends on the operating system
#
#@param ExpName         The Name of the Experiment
#@param DataDir         Working directory with input and output files
#@param IntegrateFile   The Integrated peaks file
#@param OutputFile      Output reflection file 
#@param ISPEC		= 1 for fitted incident spectrum, = 2 for unfitted spectrum 
#@param SpecFileName    File with spectrum coefficients 
#@param Range		Number of time channels (+/-) to average for unfitted spectrum
#@param initBankNo	The first (initial) detector Bank number
#@param SMU             Linear abs coeff( Tot Scat)
#@param AMU             Linear abs coeff( True Abs)ection)
#@param radius          Radius of Sphere in cm(Spherical correction only)

#@param ISIG           THE MINIMUM I/SIGI TO BE CONSIDERED(INTEGER)

#@param NBCH           PEAKS WITHIN NBCH OF THE BORDER WILL BE REJECTED
#@param JREF           REJECT PEAKS FOR WHICH THE CENTROID CALCULATION FAILED(USE Yes or No, only)
#@param IPKMIN         MINIMUMPEAK COUNT FOR A PEAK TO BE CONSIDERED
#@param DMIN           MINIMUM D-SPACING FOR A PEAK TO BE CONSIDERED
#@param IQ             Assign flag for scale factor grouping. If IQ = 1, then assign for each setting. If IQ = 2, then for each detector.
#@param SCALEFACTOR    MULTIPLY FSQ AND SIG(FSQ) TO REDUCE THEIR SIZE


#@return  Result OK or error in the program. anvred.log and {ExpName}.hkl appear in the OutputDir
#         Some information also appears on the console. 

$ Category = Macros, Single Crystal

$ ExpName         String("exp_name")			The Name of the Experiment
$ DataDir         DataDir(${Data_Directory})		Working directory with input and output files
$ SMU             Float(0.0)				Linear abs coefficient in cm^-1 (Total Scattering)
$ AMU             Float(0.0)				Linear abs coefficient in cm^-1 (True Absorption)
$ radius          float(0.1)				Radius of sphere in cm
$ ISPEC		  int(1)				Incident spectrum type. ISPEC = 1 fitted; = 2 unfitted data
$ SpecFileName    LoadFile(${Data_Directory})		If ISPEC = 1, file with spectrum coefficients
$ Range		  int(5)				If ISPEC = 2, input averaging range +/-
$ initBankNo      int(1)                        	If ISPEC = 2, the number of the first detector bank
$ ISIG            int(0)				The minimum I/sig(I)
$ NBCH            int(5)				Peaks within NBCH channels from the border will be rejected
$ IPKMIN          int(5)				Minimum peak count
$ DMIN            float(.5)				Minimum d-spacing (Angstroms)
$ IQ              int(1)				Assign scale factors (1) per setting or (2) per detector
$ SCALEFACTOR     float(.1)				Multiply FSQ and sig(FSQ) by SCALEFACTOR

# GOWS          FLOAT(262656) or(ngaus(1)*ngaus(2)*ngaus(3)*4 or 5)
# RalpS        float(262656) or float(ngaus(1)*ngaus(2)*ngaus(3)) ngaus starts at 1

Scratch = getSysProp("user.home")
Scratch = DataDir & "anvred.input"

OpenLog( Scratch, false)

  LogMsg( ExpName & "\n")
  LogMsg(  ExpName & ".integrate\n")
  LogMsg(  ExpName & ".hkl\n")
  LogMsg( "y\n")
  LogMsg( SMU &" "& AMU &"\n")
  LogMsg( "y\n")
  LogMsg( radius &"\n")
  LogMsg( ISPEC &"\n" )

    if ISPEC = 1
	LogMsg(SpecFileName&"\n")
    else
	LogMsg( Range &"\n" )
        LogMsg( initBankNo &"\n" )
    endif
  
  LogMsg( ISig &"\n")
  LogMsg( NBCH &"\n")

  LogMsg( IPKMIN&"\n")
  LogMsg(DMIN &"\n")
  LogMsg( IQ &"\n")
  LogMsg( SCALEFACTOR & "\n")

CloseLog()

  IsawHome = getSysProp("ISAW_HOME")
  Anvred = CreateExecFileName(IsawHome, "bin/anvred", true)
  Exec( Anvred, Scratch, DataDir )




  


  


