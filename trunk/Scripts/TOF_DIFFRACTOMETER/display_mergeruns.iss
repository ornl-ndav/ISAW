#Ashfia Huq August 2004 
# Script to load and merge a specified Bank in a set of run files.   
# Standard (Five banks, focused to 145,125,107,90,53.  +ve and -ve sides summed.
# Focus_all_tth : The consecutive four banks (not 53) are focussed to 125 degrees
# Miller : All other detectors turned off except 90 degree bank.
# Kappa : 8 banks of data with selected detectors
# Pressure : 1 bank of data with available detectors and focus to 100 degrees.
#
# CVS VERSION $Date$
#
# Date: 2004/01/16 16:03:04 

# Modify Date: 2005/1/18 16:03:04 : Ashfia Huq , ID's should remain the same (bad detectors are turned off using the discriminator levels

$Standard	Boolean(true)          (1) Standard (5B, sum & T focus)
$Focus_all_tth	Boolean(false)         (2) Focus_all_2Thet (4B focus to 125)		
$Miller		Boolean(false)         (3) Miller (Only 90 deg detectors) 
$Kappa		Boolean(false)         (4) Kappa (8B, sum and T focus) 
$Pressure	Boolean(false)         (5) Pressure (1B, selected detectors)

$Current	Boolean(true)		Is this a Current Run ?

$run_numbers		Array([21378])          				Enter run numbers like [21378]
$path                	DataDirectoryString    					Inputname
$path_archive		DataDirectoryString(/IPNShome/gppduser/archive_data/)	path_archive
$instrument          	InstrumentNameString    				Instrument
$ Message			String			Please check one Bank at a time
	$Bank_1		Boolean(true)		Bank_1 (1)145(2)125(3)90(4)144(5)100
	$Bank_2		Boolean(false)		Bank_2 (1)125(2)60(4)144
	$Bank_3		Boolean(false)		Bank_3 (1)107(4)126
	$Bank_4		Boolean(false)		Bank_4 (1)90(4)126
	$Bank_5		Boolean(false)		Bank_5 (1)60(4)108
	$Bank_6		Boolean(false)		Bank_6 (4)108
	$Bank_7		Boolean(false)		Bank_7 (4)90
	$Bank_8		Boolean(false)		Bank_8 (4)90

$Prompt_detector_ID  Boolean(false)        Prompt_detector_ID?


first=true

for i in run_numbers

if Current == true
   file_name = path&instrument&i&".RUN"
   Echo(file_name)
   load file_name,"temp"
   num_pulses=GetAttr(temp[1],"Number of Pulses")

elseif Current == false
   file_name = path_archive&instrument&i&".RUN"
   Echo(file_name)
   load file_name,"temp"
   num_pulses=GetAttr(temp[1],"Number of Pulses")

endif

     
if Standard == true

	if Bank_1 == true      
		id_val =["1:44,180:223"]
      		focus_val = [145]
		lowAng = 134
        	highAng = 156 
	
  	elseif Bank_2 == true
		id_val =["48:75,225:254"]
      		focus_val = [125] 
		lowAng = 117
        	highAng = 132
	
  	elseif Bank_3 == true
		id_val =["77:110,256:286"]
      		focus_val = [107]
		lowAng = 99
        	highAng = 114
	
	elseif Bank_4 == true
		id_val =["111:139,289:317"]
      		focus_val = [90]
		lowAng = 83
        	highAng = 96
	
	elseif Bank_5 == true
		id_val =["140:162,166:176"]
      		focus_val = [53]
		lowAng = 44
        	highAng = 62
        endif 	
 
  elseif Kappa == true

	if Bank_1 == true      
		id_val = ["15:34"]
      		focus_val = [144]
		lowAng = 139
        	highAng = 149 

  	elseif Bank_2 == true
		id_val = ["194:213"]
      		focus_val = [144] 
		lowAng = -149
        	highAng = -139

  	elseif Bank_3 == true
		id_val =["48:67"]
      		focus_val = [126]
		lowAng = 121
        	highAng = 131

	elseif Bank_4 == true
		id_val =["227:246"]
      		focus_val = [126]
		lowAng = -131
        	highAng = -121

	elseif Bank_5 == true
		id_val =["82:101"]
      		focus_val = [108]
		lowAng = 103
        	highAng =113

	elseif Bank_6 == true
		id_val =["260:279"]
      		focus_val = [108]
		lowAng = -113
        	highAng = -103

	elseif Bank_7 == true
		id_val =["115:134"]
      		focus_val = [90]
		lowAng = 85
        	highAng = 95

	elseif Bank_8 == true
		id_val =["293:312"]
      		focus_val = [90]
		lowAng = -95
        	highAng = -85
        endif 	
  
   elseif Focus_all_tth == true
	if Bank_1 == true      
  	id_val =["1:44,180:223,48:75,225:254,77:110,256:286,111:317"]
		focus_val = [125]
		lowAng = 83
        	highAng = 156 

	elseif Bank_2 == true
		id_val =["140:162,166:176"]
      		focus_val = [53]
		lowAng = 52
        	highAng = 62
        endif 	


   elseif Miller == true
  	id_val =["2:60"]
  		focus_val = [90]
		lowAng = 83
        	highAng = 96

   elseif Pressure == true
 	id_val =["69:110,111:139,244:287,289:316"]
  		focus_val = [100]
		lowAng = 134
        	highAng = 156 
endif

if Prompt_detector_ID == true
	InputBox( "Enter bank info for GPPD"&i, [ "Bank","Low Angle","High Angle"], [id_val,lowAng,highAng], [temp[1]] )
		
endif

      if first
      merged_ds=ExtAtt(temp[1],"Raw Detector Angle",true,lowAng,highAng)
      first=false
      first_num_pulses=num_pulses
        
      merged_ds = TimeFocusGID(merged_ds,id_val[0],focus_val[0],1.5,true)
      merged_ds = SumAtt(merged_ds, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
      
      elseif num_pulses<>0

      part_ds =ExtAtt(temp[1],"Raw Detector Angle",true,lowAng,highAng)
      scale_factor= first_num_pulses/(1.0*num_pulses)
      Mult(part_ds,scale_factor,false)
      part_ds_f = TimeFocusGID(part_ds,id_val[0],focus_val[0],1.5,true)
      m_dsi = SumAtt(part_ds_f, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005) 
      
      merged_ds = Merge(merged_ds,m_dsi)
      

    endif
#   Echo(instrument&" run "&i&" loaded.")
endfor

SetDataLabel(merged_ds,"Run Number","")
Display merged_ds
d_ds=ToD(merged_ds,.2,5.4,6000)
SetDataLabel(d_ds,"Run Number","")
Send d_DS
