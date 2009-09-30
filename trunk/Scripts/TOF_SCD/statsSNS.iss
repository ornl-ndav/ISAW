#                  STATS
#@overview  This script is a front end for the statsSNS executable
#@assumption  The statsSNS executable is named {IsawHome}/stats/stats_? where
#              ? depends on the operating system
#
#@param DataDir         Directory with Input files
#@param OutputDir       Directory for output files
#@param IntegrateFile   The Integrated peaks file
#@param DMIN            Minimum d-spacing in Angstroms

#@return  Result OK or error in the program. anvred.log and {ExpName}.hkl appear in the OutputDir
#         Some information also appears on the console. 

$ Category = Macros, Single Crystal

$ DataDir         DataDir(${Data_Directory})		Directory with Input files
$ OutputDir       DataDir(${Data_Directory})		Directory for output files
$ IntegrateFile   String("exp_name.integrate")		The Integrated peaks file
$ OutputFile      String("exp_name.hkl")			Output reflection file  
$ DMIN            float(.5)				Minimum d-spacing (Angstroms)

Scratch = getSysProp("user.home")
Scratch = Scratch &"/ISAW/input.dat"

OpenLog( Scratch, false)

  LogMsg( ExpName &"\n")
  LogMsg(  DataDir &IntegrateFile &"\n")
  LogMsg(  OutputFile &"\n")
  LogMsg(DMIN &"\n")

CloseLog()

  IsawHome = getSysProp("ISAW_HOME")
  stats = CreateExecFileName(IsawHome, "bin/stats", true)
  Exec( stats, Scratch, OutputDir )




  


  


