/* 
 * File: SetNewInstrumentCommand.java
 *
 * Copyright (C) 2009, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp.Command;

/**
 *  This class carries the information needed by the class that maps
 *  tof,id event information to vector Q with corrections for the
 *  incident spectrum.
 */
public class SetNewInstrumentCmd
{
   private String instrument_name;         // the instrument name MUST be as
                                           // listed in SNS_Tof_to_Q_map.java
   private String detector_file_name;
   private String incident_spectrum_file_name;

   
  /**
   *  Create a new set instrument command object, containing the specified
   *  information.
   *
   *  @param instrument_name  The name of the instrument, as listed in 
   *                          SNS_Tof_to_Q_map.java
   *  @param detector_file_name  The name of the file containing the 
   *                             detector mapping information.  This file
   *                             is of the form of .DetCal files, which is
   *                             the same as the information listed at the
   *                             start of a peaks file.  If none is specified
   *                             the default file in ISAW/InstrumentInfo/SNS
   *                             will be used, if the name is the name of
   *                             a supported instrument.
   *  @param incident_spectrum_file_name  
   *                             The name of the file containing the incident
   *                             spectrum information.  This file is a simple
   *                             ASCII file with some basic header information
   *                             as in ISAW/InstrumentInfo/SNAP_Spectrum.dat
   *                             If none is specified the default file in 
   *                             ISAW/InstrumentInfo/SNS will be used, if 
   *                             it is present.  Otherwise, no correction
   *                             for the incident spectrum will be used.
   */
   public SetNewInstrumentCmd( String instrument_name, 
                               String detector_file_name,
                               String incident_spectrum_file_name )
   {
      this.instrument_name = instrument_name; 
      this.detector_file_name = detector_file_name; 
      this.incident_spectrum_file_name = incident_spectrum_file_name; 
   }


   public String getDetectorFileName()
   {
      if (detector_file_name == null || detector_file_name.trim().equals(""))
        return null;

      return detector_file_name;
   }


   public String getInstrumentName()
   {
      if (instrument_name == null || instrument_name.trim().equals(""))
        return null;

      return instrument_name;
   }


   public String getIncidentSpectrumFileName()
   {
      if ( incident_spectrum_file_name == null || 
           incident_spectrum_file_name.trim().equals("") )
        return null;

      return incident_spectrum_file_name;
   }

   
   public String toString()
   {
      return "\nInstrument Name    : " + getInstrumentName()   +
             "\nDetector File Name : " + getDetectorFileName() +
             "\nIncident Spectrum File Name : " +getIncidentSpectrumFileName();
   }

}
