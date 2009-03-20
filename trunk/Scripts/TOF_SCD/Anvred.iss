#                  Anvred
#@overview  This script is a front end to the Anvred executable
#@assumption  The anvred executable is named {IsawHome}/anvred/anvred_? where
#              ? depends on the operating system
#
#@param ExpName        The Name of the Experiment
#@param DataDir        Directory with Input files
#@param OutputDir       Directory for output files
#@param IntegrateFile   The Integrated peaks file
#@param OutputFile      Output reflection file  
#@param SpecFileName    File with spectrum coefficients 
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


$ ExpName         String("exp_name")			The Name of the Experiment
$ DataDir         DataDir(${Data_Directory})		Directory with Input files
$ OutputDir       DataDir(${Data_Directory})		Directory for output files
$ IntegrateFile   String("exp_name.integrate")		The Integrated peaks file
$ OutputFile      String("exp_name.hkl")			Output reflection file  
$ SpecFileName    LoadFile(${Data_Directory})	File with spectrum coefficients 
$ SMU             Float(0.0)				Linear abs coeff in cm^-1 (Total Scattering)
$ AMU             Float(0.0)				Linear abs coeffin cm^-1 (True Absorption)
$ radius          float(0.1)				Radius of sphere in cm
$ ISIG            int(0)				The minimum I/sig(I)
$ NBCH            int(5)				Peaks within NBCH channels from the border will be rejected
$ JREF            Choice(["Yes","No"])		Reject peaks for which the centroid calculation failed
$ IPKMIN          int(0)				Minimum peak count
$ DMIN            float(.5)				Minimum d-spacing (Angstroms)
$ IQ              int(1)				Assign scale factors (1) per setting or (2) per detector
$ SCALEFACTOR     float(.1)				Multiply FSQ and sig(FSQ) by SCALEFACTOR

# GOWS          FLOAT(262656) or(ngaus(1)*ngaus(2)*ngaus(3)*4 or 5)
# RalpS        float(262656) or float(ngaus(1)*ngaus(2)*ngaus(3)) ngaus starts at 1

Scratch = getSysProp("user.home")
Scratch = Scratch &"/ISAW/input.dat"

OpenLog( Scratch, false)

  LogMsg( ExpName &"\n")
  LogMsg(  DataDir &IntegrateFile &"\n")
  LogMsg(  OutputFile &"\n")
  LogMsg( "y\n")
  LogMsg( SMU &" "& AMU &"\n")
  LogMsg( "y\n")
  LogMsg( radius &"\n")
  LogMsg(SpecFileName&"\n")
  LogMsg( ISig &"\n")
  LogMsg( NBCH &"\n")

   if  JREF == "Yes"
      LogMsg("y\n");
   else
      LogMsg("n\n")
   endif

  LogMsg( IPKMIN&"\n")
  LogMsg(DMIN &"\n")
  LogMsg( IQ &"\n")
  LogMsg( SCALEFACTOR & "\n")

CloseLog()

  IsawHome = getSysProp("ISAW_HOME")
  Anvred = CreateExecFileName(IsawHome, "bin/anvred", true)
  Exec( Anvred, Scratch, OutputDir )




  


  


