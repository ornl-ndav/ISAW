 
# Displays summed data as a function of D
# Standard (Five banks, focused to 145,125,107,90,60.  +ve and -ve sides summed.
# Focus_all_tth : The consecutive four banks (not 60) are focussed to 125 degrees
# Miller : All other detectors turned off except 90 degree bank.
# Kappa : Creates 8 banks of data with selected detectors
# Pressure : Creates 1 bank of data with available detectors and focus to 100 degrees.
# Choice is given to display dialog box.
# $Date$

$Category=Operator, Instrument Type, TOF_NPD

$Sort_d_by_Omega	Boolean(true)	Sort_d_by_Omega (Indiv detectors)
$Standard        	Boolean(false)	Standard (5B, sum & T focus)
$Focus_all_tth	    	Boolean(false)  Focus_all_2Theta (4B focus to 125)	
$Miller             	Boolean(false)  Miller (Only 90 deg detectors) 
$Kappa               	Boolean(false)  Kappa (8B, sum and T focus) 
$Pressure             	Boolean(false)  Pressure (1B with selected detectors)

$Current		Boolean(true)		Is this a Current Run ?

$run_numbers   		Array([21378])		Enter run numbers like [1,2:5]
$path                	DataDirectoryString    	Path
$path_archive		DataDirectoryString(/IPNShome/gppduser/archive_data/)	path_archive
$instrument          	InstrumentNameString   	Instrument
$outputname		DataDirectoryString(/IPNShome/gppduser/aaaUSER_data/)  outputname    

$Create_GSAS		Boolean(false)        Create GSAS data?
$Prompt_detector_ID	Boolean(false)        Prompt_detector_ID?

for i in run_numbers
  
if Current == true
  		file_name = path&instrument&i&".RUN"
  		Echo(file_name)
  		load file_name,"temp"

	elseif Current == false
		file_name = path_archive&instrument&i&".RUN"
  		Echo(file_name)
  		load file_name,"temp"
	endif
   if Sort_d_by_Omega == true
	ds=ToD(temp[1],0,4.0,2000)
	dss=Sort(ds,"Omega",true,true)
	Display dss
	send dss
  		
   elseif Standard == true
  	id_val =["1:44,180:223","48:75,225:254","77:91,93:110,256:287","111:139,289:298,300:317","140:159"]
  	focus_val = [145,125,107,90,60]
  		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1","Bank 2","Bank 3","Bank 4","Bank 5"], id_val, [temp[1]] )
		endif
	m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)
  	m_dsa = SumAtt(m_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
   for j in [1:4]
      	dsi = TimeFocusGID(temp[1],id_val[j],focus_val[j],1.5,true)
      	dsa = SumAtt(dsi, "Effective Position",true, focus_val[j]-.000005,focus_val[j]+.000005)
      	m_dsa =Merge(m_dsa,dsa)
   endfor 

	if Create_GSAS == true
	SaveGSAS( m_dsa, m_dsa, outputname&i&".gsa",false,true)
	endif

      	m_dsaT=ToD(m_dsa,0.15,4.0,2000)
       	dss=Sort(m_dsaT,"Omega",true,true)
		Display dss
       	  
   elseif Focus_all_tth == true
  	id_val =["1:30,32:44,48:75,77:91,93:139,180:223,225:254,256:287,289:305,307:317","140:159"]
	focus_val = [125,60]
		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1","Bank 2"], id_val, [temp[1]] )
		endif
   m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)
   m_dsa = SumAtt(m_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  
	for j in [1:1]
      		dsi = TimeFocusGID(temp[1],id_val[j],focus_val[j],1.5,true)
      		dsa = SumAtt(dsi, "Effective Position",true, focus_val[j]-.000005,focus_val[j]+.000005)
      		m_dsa =Merge(m_dsa,dsa)  
  	endfor

	if Create_GSAS == true
	SaveGSAS( m_dsa, m_dsa, outputname&i&".gsa",false,true)
	endif

  		m_dsaT=ToD(m_dsa,0.15,4.0,2000)
       		dss=Sort(m_dsaT,"Omega",true,true)
		Display dss
		
	
   elseif Miller == true
  	id_val =["1:60"]
  	focus_val = [90]
		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1"], id_val, [temp[1]] )
		endif
   m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)
   m_dsa = SumAtt(m_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  

	if Create_GSAS == true
	SaveGSAS( m_dsa, m_dsa, outputname&i&".gsa",false,true)
	endif

	m_dsaT=ToD(m_dsa,0.15,2.5,2000)
      Display m_dsaT
 
   elseif Kappa == true
  	id_val =["15:30,32:34","194:213","48:59,61:67","227:234,236:246","82:91,93:101","260:279","115:134","293:305,307:312"]
  	focus_val = [144,144,126,126,108,108,90,90]
		if Prompt_detector_ID == true
		InputBox("Bank info"&i,["Bank 1","Bank 2","Bank 3","Bank 4","Bank 5","Bank 6","Bank 7","Bank 8"],id_val,[temp[1]])
		endif
   m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)
   m_dsa = SumAtt(m_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  	for j in [1:7]
      		dsi = TimeFocusGID(temp[1],id_val[j],focus_val[j],1.5,true)
      		dsa = SumAtt(dsi, "Effective Position",true, focus_val[j]-.000005,focus_val[j]+.000005)
      		m_dsa =Merge(m_dsa,dsa)  
	endfor
	
	if Create_GSAS == true
	SaveGSAS( m_dsa, m_dsa, outputname&i&".gsa",false,true)
	endif
		
	m_dsaT=ToD(m_dsa,0.15,3.3,2000)
       	dss=Sort(m_dsaT,"Omega",true,true)
	Display dss

  elseif Pressure == true
 	id_val =["69:75,77:91,93:110,111:139,244:254,256:287,289:305,307:316"]
  	focus_val = [100]
		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1"], id_val, [temp[1]] )
		endif

  m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)
  m_dsa = SumAtt(m_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  	
	if Create_GSAS == true
	SaveGSAS( m_dsa, m_dsa, outputname&i&".gsa",false,true)
	endif
		
	m_dsaT=ToD(m_dsa,0.15,2.8,2000)
       	Display m_dsaT


endif	
endfor


