#Ashfia Huq August 2004 
# Script to produce  gsas file.  Allows user to choose one of the five general setups.
# Standard (Five banks, focused to 145,125,107,90,53.  +ve and -ve sides summed.
# Focus_all_tth : The consecutive four banks (not 53) are focussed to 125 degrees
# Miller : All other detectors turned off except 90 degree bank.
# Kappa : Creates 8 banks of data with selected detectors
# Pressure : Creates 1 bank of data with available detectors and focus to 100 degrees.
# Users have the option to display the data.
# Choice is given to display dialog box.

# Modify Date: 2005/02/17 23:11:32 : Ashfia Huq , ID's should remain 
# the same (bad detectors are turned off using the discriminator levels)

#Write 3 column format (FXYE for GSAS) or ESD format(Original sequential format with no space)
#Resample bins(for constant binning) to start at 2000 micro sec and end at 32000 micro sec with dT = 5 micro sec.
#For dT/T binning make sure to check box so that data is not resampled

# CVS VERSION $Date$

$Category=Macros, Instrument Type, TOF_NPD

$Standard		Boolean(true)        Standard (5B, sum & T focus)
$Focus_all_tth	Boolean(false)       Focus_all_2Theta (4B focus to 125)		
$Miller		Boolean(false)       Miller (Only 90 deg detectors) 
$Kappa		Boolean(false)       Kappa (8B, sum and T focus) 
$Pressure		Boolean(false)       Pressure (1B, selected detectors)

$Current		Boolean(true)		Is this a Current Run ?

$run_numbers		Array([23117])          				Enter run numbers like [23127]
$path                	DataDirectoryString    					Inputname
$path_archive		DataDirectoryString(C:/material/user-data/)	path_archive
$instrument          	InstrumentNameString    				Instrument
$outputname			DataDirectoryString(C:/material/testisaw/)  	outputname    

$FXYE_format		Boolean(true)		3 Column Format?	
$dT_bin			Boolean(false)		dT/T binning ?
$Display_data        	Boolean(false)		Display_data?
$Prompt_detector_ID  	Boolean(false)		Prompt_detector_ID?

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
	

if Standard == true
id_val =["1:44,180:223","48:75,225:254","77:110,256:286","111:139,289:317","140:162,166:176"]
  	focus_val = [145,125,107,90,53]
  		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1","Bank 2","Bank 3","Bank 4","Bank 5"], id_val, [temp[1]] )
		endif
	m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)

	if dT_bin == false
		uniform_dsi = Resample(m_dsi,2000,32000,6001,true)
	else
		uniform_dsi = m_dsi
	endif
	m_dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
   		for j in [1:4]
      	dsi = TimeFocusGID(temp[1],id_val[j],focus_val[j],1.5,true)
		if dT_bin == false
			uniform_dsi = Resample(dsi,2000,32000,6001,true)
		else
			uniform_dsi = dsi
		endif
      	dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[j]-.000005,focus_val[j]+.000005)
      	m_dsa =Merge(m_dsa,dsa)
   		endfor 

	if FXYE_format == true 
		Save3ColGSAS(temp[0], m_dsa, outputname&i&".gsa",true)
	else
		SaveGSAS(temp[0], m_dsa, outputname&i&".gsa",true,true)
	endif
		
		if Display_data == true
		m_dsaT=ToD(m_dsa,0.15,5.3,6000)
      	dss=Sort(m_dsaT,"Group ID",true,true)
		Display dss
		send dss
		endif
	

elseif Focus_all_tth == true
  	id_val =["1:44,180:223,48:75,225:254,77:110,256:286,111:139,289:317","140:162,166:176"]
  	focus_val = [125,53]
  		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1","Bank 2], id_val, [temp[1]] )
		endif
	m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)

	if dT_bin == false
		uniform_dsi = Resample(m_dsi,2000,32000,6001,true)
	else
		uniform_dsi = m_dsi
	endif
	m_dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
   		for j in [1:1]
      	dsi = TimeFocusGID(temp[1],id_val[j],focus_val[j],1.5,true)
		if dT_bin == false
			uniform_dsi = Resample(dsi,2000,32000,6001,true)
		else
			uniform_dsi = dsi
		endif
      	dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[j]-.000005,focus_val[j]+.000005)
      	m_dsa =Merge(m_dsa,dsa)
   		endfor 

	if FXYE_format == true 
		Save3ColGSAS(temp[0], m_dsa, outputname&i&".gsa",true)
	else
		SaveGSAS(temp[0], m_dsa, outputname&i&".gsa",true,true)
	endif
		
		if Display_data == true
		m_dsaT=ToD(m_dsa,0.15,5.3,6000)
      	dss=Sort(m_dsaT,"Group ID",true,true)
		Display dss
		send dss
		endif


elseif Miller == true
  	id_val =["2:60"]
  	focus_val = [90]
		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1"], id_val, [temp[1]] )
		endif
   	m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)

	if dT_bin == false
   		uniform_dsi = Resample(m_dsi,2000,32000,6001,true) 
	else
		uniform_dsi = m_dsi
	endif
   	m_dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  		
	if FXYE_format == true 
		Save3ColGSAS(temp[0], m_dsa, outputname&i&".gsa",true)
	else
		SaveGSAS(temp[0], m_dsa, outputname&i&".gsa",true,true)
	endif
		if Display_data == true
		m_dsaT=ToD(m_dsa,0.15,3.2,6000)
      	Display m_dsaT
		send m_dsaT
		endif
  
elseif Kappa == true
  	id_val =["15:34","194:213","48:67","227:246","82:101","260:279","115:134","293:312"]
  	focus_val = [144,144,126,126,108,108,90,90]
		if Prompt_detector_ID == true
		InputBox("Bank info"&i,["Bank 1","Bank 2","Bank 3","Bank 4","Bank 5","Bank 6","Bank 7","Bank 8"],id_val,[temp[1]])
		endif
   	m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)

	if dT_bin == false
   		uniform_dsi = Resample(m_dsi,2000,32000,6001,true)
	else
		uniform_dsi = m_dsi 
	endif
   	m_dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  		for j in [1:7]
      	dsi = TimeFocusGID(temp[1],id_val[j],focus_val[j],1.5,true)

		if dT_bin == false
		uniform_dsi = Resample(dsi,2000,32000,6001,true)
		else
		uniform_dsi = dsi 
		endif
      	dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[j]-.000005,focus_val[j]+.000005)
      	m_dsa =Merge(m_dsa,dsa)  
		endfor

	if FXYE_format == true 
		Save3ColGSAS(temp[0], m_dsa, outputname&i&".gsa",true)
	else
		SaveGSAS(temp[0], m_dsa, outputname&i&".gsa",true,true)
	endif
		if Display_data == true
		m_dsaT=ToD(m_dsa,0.15,2.3,6000)
      	dss=Sort(m_dsaT,"Omega",true,true)
		Display dss
		send dss	
		endif

elseif Pressure == true
 	id_val =["69:110,111:139,244:287,289:316"]
  	focus_val = [100]
		if Prompt_detector_ID == true
		InputBox( "Enter bank info for GPPD"&i, [ "Bank 1"], id_val, [temp[1]] )
		endif

  	m_dsi = TimeFocusGID(temp[1],id_val[0],focus_val[0],1.5,true)

	if dT_bin == false
  	uniform_dsi = Resample(m_dsi,2000,32000,6001,true)
	else
		uniform_dsi = m_dsi 
	endif
 	m_dsa = SumAtt(uniform_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  		
	if FXYE_format == true 
		Save3ColGSAS(temp[0], m_dsa, outputname&i&".gsa",true)
	else
		SaveGSAS(temp[0], m_dsa, outputname&i&".gsa",true,true)
	endif	
		if Display_data == true
		m_dsaT=ToD(m_dsa,0.15,2.8,6000)
      	Display m_dsaT
		send m_dsaT
		endif
  

endif
endfor
