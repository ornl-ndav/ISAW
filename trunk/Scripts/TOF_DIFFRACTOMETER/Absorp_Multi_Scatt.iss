#
# Define the variables for the script
# $Date$
#
$ filepath   DataDirectoryString(c:/gppd_newdas_runs/)      Path
$ inst       String(gppd)             Instrument
$ vfile   Integer(8236)           Vanadium File number
$ bfile   Integer(8237)           Background File number
$ rod_radius Float(0.3175)            Vanadium rod radius in cm
$ dn_t_min   Float(0.0)               Tmin for monitor integration
$ dn_t_max   Float(35000.0)           Tmax for monitor integration
$ summed     boolean(false)         Sum detector banks?

#  Bring in the run file and Display the sample data
#

runfile=filepath&inst&vfile&".run"
Display "Vanadium runfile: "&runfile
Load runfile,"vds"
#Display vds[0]


# load the background file
brunfile=filepath&inst&bfile&".run"
Display "Background run: "&brunfile
Load brunfile,"bds"
#Display bds[0]

#
#  Calculate and display the up stream monitor sum for vanadium.  
#  NOTE: the time interval is the start time of the first bin 
#        used in the integral to the end time of the last bin
#        used in the integral.

up_mon_id = UpMonitorID(vds[0])
sum_up_rawv = IntegGrp( vds[0], up_mon_id, dn_t_min, dn_t_max )
Display "Sum up raw vanadium monitor= "&sum_up_rawv

#
#  Calculate and display the up stream monitor sum for background.  
#  NOTE: the time interval is the start time of the first bin 
#        used in the integral to the end time of the last bin
#        used in the integral.

up_mon_id = UpMonitorID(bds[0])
sum_up_rawb = IntegGrp( bds[0], up_mon_id, dn_t_min, dn_t_max )
Display "Sum up raw background monitor= "&sum_up_rawb
Ratio =  sum_up_rawv/sum_up_rawb
Display "Ratio = "&Ratio
 id_val =["2:24,26:48,89:92,105:112,125:128","49:88","93:104,113:124","129:136","137:144"]
  focus_val = [147.865,90.0,60.0,19.227,30.0]
  focus_val = [147.8649750,90.0000534,59.9999123,19.2268600,29.9999828]
  # id_val =["1:45,180:224","46:76,225:254","77:110,255:288","111:139,289:318","140:177"]
  # focus_val = [145.435,124.279,107.251,89.707,60.324]

 if summed == true

  InputBox( "Enter bank info for vanadium GPPD"&vfile, [ "Bank 1","Bank 2","Bank 3","Bank 4","Bank 5"], id_val, [vds[1]] )
  InputBox( "Enter bank info for background  GPPD"&vfile, [ "Bank 1","Bank 2","Bank 3","Bank 4","Bank 5"], id_val, [bds[1]] )

  m_dsi = TimeFocusGID(vds[1],id_val[0],focus_val[0],1.5,true)
  m_dsa = SumAtt(m_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)

  b_dsi = TimeFocusGID(bds[1],id_val[0],focus_val[0],1.5,true)
  b_dsa = SumAtt(b_dsi, "Effective Position",true, focus_val[0]-.000005,focus_val[0]+.000005)
  y0 = m_dsa - Ratio*b_dsa
  van0 = Abs_MScatt_Correct(y0,focus_val[0], rod_radius)

  for j in [1:4]
      dsi = TimeFocusGID(vds[1],id_val[j],focus_val[j],1.5,true)
      dsa = SumAtt(dsi, "Effective Position",true, focus_val[j]-0.5,focus_val[j]+0.5)
      bdsi = TimeFocusGID(bds[1],id_val[j],focus_val[j],1.5,true)
      bdsa = SumAtt(bdsi, "Effective Position",true, focus_val[j]-0.5,focus_val[j]+0.5)
      y1 = dsa - Ratio*bdsa
      van1 = Abs_MScatt_Correct(y1,focus_val[j], rod_radius)
     van0 =Merge(van0,van1)  
  endfor
    SaveGSAS( vds[0], van0, filepath&inst&vfile&".gda",false,true)
 else

  y0 = vds[1] - Ratio*bds[1]
  van0 = Abs_MScatt_Correct(y0,focus_val[0], rod_radius)
  for j in [1:4]
   van_ds = Abs_MScatt_Correct(y0,focus_val[j], rod_radius)
   van0 = Merge(van0,van_ds)
  endfor
   SaveGSAS(vds[0],van0,filepath&inst&vfile&".gda",false,true)
 endif










