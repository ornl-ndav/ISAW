

$ ExpName         String("Oox80")       The Name of the Experiment
$ IntegrateFile   LoadFile(C:\SCD\Oxalic acid\Oox80.integrate)     The Integrated peaks file
$ MatFile         LoadFile(C:\SCD\Oxalic acid\0x80.mat)     Matrix file
$ LogFile         SaveFile(c:\x.dat)     The the name of the log file
$ SpecFileName    LoadFile(C:\SCD\spec_coeff.dat)      File with sectrum coefficients 
$ ISPC           int(13)                  spectrum number to use from the spec file
$ OutFile         SaveFile(C:\SCD\Oxalic acid\Oox80A.hkl)     The Name of the output File
$ IABS            Integer(1)       NonZero means do and Absorption
$ NGaus           Array([8,8,8,12,12,12])   Number of Grid points
$ N               int(0)                  Number of Gaus point changed(max 3)
$ ICell           Choice(["P","A","B","C","F","R"])   Cell Type
$ SMU             Float( 1.30157733)          Linear abs coeff( Tot Scat)
$ AMU             Float( 1.68592)          Linear abs coeff( True Abs)
$ IAType          Choice(["Spherical Correction(original)", "Sqherical Correction(Gaussian)","Numerical correction, crystal reference frame","Numerical correctiohn, orthogonal ref frame","Test"]) Type of absorption correction
$ zomega          float               for test program(test absorption correction)
$ zchi            float               for test program(test absorption correction)
$ zphi            float               for test program(test absorption correction)
$ radius          float(.17)               Rad of Sphere in cm(Spherical correction only)
$ Nface           int(0)                 Number of faces( Numerical corrections only)
$ Face1            array                Tag for faces
$ Faceh            array                h value for faces
$ Facek            array                k value for faces
$ Facel            array                l value for faces
$ SEPD             Choice(["0.79448 * WL","1.00928 * WL"])  Select a detector efficiency(Incident spectrum is derived from vanadium data on SEPD)

$ Eff            Choice(["mu = 10.012*WL + 0.486","mu =  9.614*WL + 0.266"])  detector efficiencies
$ ISIG           int(0)              THE MINIMUM I/SIGI TO BE CONSIDERED(INTEGER)
$ XNUM1           int(100)                  THE  NUMBER OF X DETECTOR CHANNELS
$ YNUM1           int(100)                  THE  NUMBER OF Y DETECTOR CHANNELS
$ NBCH           int(5)                    PEAKS WITHIN NBCH OF THE BORDER WILL BE REJECTED
$ JREF            Choice(["Yes","No"])                   REJECT PEAKS FOR WHICH THE CENTROID CALCULATION FAILED
$ IPKMIN        int(0)                   MINIMUMPEAK COUNT FOR A PEAK TO BE CONSIDERED
$ DMIN           float(.5)      MINIMUM D-SPACING FOR A PEAK TO BE CONSIDERED
$ SCALEFACTOR    float(1.0)           MULTIPLY FSQ AND SIG(FSQ) TO REDUCE THEIR SIZE
# GOWS          FLOAT(262656) or(ngaus(1)*ngaus(2)*ngaus(3)*4 or 5)
# RalpS        float(262656) or float(ngaus(1)*ngaus(2)*ngaus(3)) ngaus starts at 1

Scratch = getSysProp("user.home")
Anvred  = Scratch&"/ISAW/anvred"
Scratch = Scratch &"/ISAW/input.dat"
OpenLog( Scratch, false)
  LogMsg( ExpName &"\n")
  LogMsg( IntegrateFile &"\n")
  LogMsg( OutFile &"\n")
  LogMsg( MatFile & "\n")
     ICellN =0
     if  ICell =="P"
         ICellN =1
     elseif  ICell =="A"
         ICellN =2
     elseif  ICell =="B" 
         ICellN =3
     elseif  ICell =="C" 
         ICellN =4
     elseif  ICell =="V" 
         ICellN =5
     elseif  ICell =="R"
         ICellN =6
     endif
  LogMsg( ICellN &"\n")
    Abs ="N"
    if  IABS >0
      Abs ="Y"
    endif
  LogMsg( Abs &"\n")

  if  IABS > 0 
     LogMsg( SMU &" "& AMU &"\n")

     LogMsg( "y\n")

   endif 

   AbsType =0
     if  IAType =="Spherical Correction(original)"
       AbsType = 1
     elseif  IAType =="Sqherical Correction(Gaussian)"
       AbsType = 2
     elseif  IAType =="Numerical correction, crystal reference frame"
       AbsType =3
     elseif  IAType =="Numerical correctiohn, orthogonal ref frame"
       AbsType = 4
     else
       AbsType =5
     endif

   LogMsg( AbsType&"  ")
   if  N > 0
      for i in [0:N-1]
         LogMsg( NGaus[i]&"  ")
      endfor
   endif
   LogMsg("\n")


   if  AbsType <=2
      LogMsg( radius &"\n")
   elseif  AbsType < 5
# TODO check for lengths
     if Nface > 0
     for i in [0:Nface-1]
        LogMsg( Face1[i]&"  "&Faceh[i]&"   "&Facek[i]&"   "&Facel[i]&"\n")
     endfor
     LogMsg("\n")
     endif 
   endif
#Todo Check for tests

   ISepd = 2
   if SEPD == "0.79448 * WL"
     ISepd = 1
   endif

   LogMsg(ISepd &"\n")
   LogMsg(ISPC &"\n")
   
   IEff = 2
   if  Eff =="mu = 10.012*WL + 0.486"
     IEff = 1
   endif

   LogMsg(IEff&"\n")

   LogMsg( ISig &"\n")

   LogMsg(Xnum1 &","&Ynum1&"\n")

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
  Exec( Anvred , Scratch)




  


  


