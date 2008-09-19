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
#@param radius          Rad of Sphere in cm(Spherical correction only)

#@param ISIG           THE MINIMUM I/SIGI TO BE CONSIDERED(INTEGER)

#@param NBCH           PEAKS WITHIN NBCH OF THE BORDER WILL BE REJECTED
#@param JREF           REJECT PEAKS FOR WHICH THE CENTROID CALCULATION FAILED(USE Yes or No, only)
#@param IPKMIN         MINIMUMPEAK COUNT FOR A PEAK TO BE CONSIDERED
#@param DMIN           MINIMUM D-SPACING FOR A PEAK TO BE CONSIDERED
#@param SCALEFACTOR    MULTIPLY FSQ AND SIG(FSQ) TO REDUCE THEIR SIZE

#@return  Result OK or error in the program. anvred.log and {ExpName}.hkl appear in the OutputDir
#         Some information also appears on the console. 






$ ExpName         String("oxalic")                                                    The Name of the Experiment
$ DataDir         DataDir(${Data_Directory})                                         Directory with Input files
$ OutputDir       DataDir("${Data_Directory}")                                   Directory for output files
$ IntegrateFile   String("oxalic.integrate")                                          The Integrated peaks file
$ OutputFile      String("oxalic.hkl")                                                Output reflection file  
$ SpecFileName    LoadFile("${ISAW_HOME}/anvred/")                                    File with spectrum coefficients 
$ SMU             Float( 1.30157733)                                                  Linear abs coeff( Tot Scat)
$ AMU             Float( 1.68592)                                                     Linear abs coeff( True Abs)ection)
$ radius          float(.17)                                                          Rad of Sphere in cm(Spherical correction only)

$ ISIG           int(0)                                                               THE MINIMUM I/SIGI TO BE CONSIDERED(INTEGER)

$ NBCH           int(5)                                                               PEAKS WITHIN NBCH OF THE BORDER WILL BE REJECTED
$ JREF            Choice(["Yes","No"])                                                REJECT PEAKS FOR WHICH THE CENTROID CALCULATION FAILED
$ IPKMIN        int(0)                                                                MINIMUMPEAK COUNT FOR A PEAK TO BE CONSIDERED
$ DMIN           float(.5)                                                            MINIMUM D-SPACING FOR A PEAK TO BE CONSIDERED
$ SCALEFACTOR    float(.1)                                                            MULTIPLY FSQ AND SIG(FSQ) TO REDUCE THEIR SIZE
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

   LogMsg( SCALEFACTOR & "\n")

  CloseLog()
  IsawHome = getSysProp("ISAW_HOME")
  Anvred = CreateExecFileName(IsawHome,"anvred/anvred",true)
  Exec( Anvred,Scratch, OutputDir )




  


  


