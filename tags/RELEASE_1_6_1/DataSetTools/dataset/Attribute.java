/*
 * File:  Attribute.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.41  2003/11/20 18:28:11  rmikk
 *  Added Documentation to Attribute Strings
 *
 *  Revision 1.40  2003/08/11 22:17:06  dennis
 *  Added T0_SHIFT attribute, to record shift in T0 from SCD
 *  calibration.
 *
 *  Revision 1.39  2003/02/19 21:55:49  dennis
 *  Added flags for attribute "level of detail"
 *
 *  Revision 1.38  2003/02/18 18:58:26  dennis
 *  Added SAMPLE_ORIENTATION attribute name.
 *
 *  Revision 1.37  2003/02/12 20:05:14  dennis
 *  Removed SEGMENT_INFO and SEGMENT_INFO_LIST.
 *  Added RAW_DISTANCE for TOF_DG_SPECTROMETERs.
 *
 *  Revision 1.36  2003/02/07 19:45:06  dennis
 *  Added DETECTOR_DATA_GRID
 *
 *  Revision 1.35  2003/02/07 19:05:51  dennis
 *  Added PIXEL_INFO_LIST String.
 *
 *  Revision 1.34  2003/01/15 20:54:25  dennis
 *  Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 *  Revision 1.33  2002/12/20 20:24:59  pfpeterson
 *  Added a new static String for user name.
 *
 *  Revision 1.32  2002/12/04 14:53:22  pfpeterson
 *  Added string constants for the start date and start time.
 *
 *  Revision 1.31  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.30  2002/11/12 00:20:18  dennis
 *  Made immutable by:
 *  1. remove setValue() method
 *  2. add() & combine() methods now return a new Attribute
 *
 *  Also:
 *  3. Since it is now immutable, clone() method is not needed and
 *     was removed
 *  4. Default constructor is now private, since the value can't
 *     be set from outside of the class
 *
 *  Revision 1.29  2002/09/25 16:49:17  pfpeterson
 *  Added string constants for SCD calibration information.
 *
 *  Revision 1.28  2002/08/22 15:08:22  pfpeterson
 *  Added string constants for lattice parameter, orientation matrix,
 *  and unit cell volume.
 *
 *  Revision 1.27  2002/08/06 14:00:15  pfpeterson
 *  Added constants for pressure and temperature data.
 *
 *  Revision 1.26  2002/08/01 22:36:55  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *  Added NeXus string constant names for analysis types.
 *  Removed redundant "transient" qualifier from static field.
 *
 *  Revision 1.25  2002/07/10 20:57:52  pfpeterson
 *  All string constants are now transient and finally define the
 *  serialVersionUID=1L (the first version when we are counting).
 *
 *  Revision 1.24  2002/07/10 16:00:44  pfpeterson
 *  Added new string constants for GSAS handling.
 *
 *  Revision 1.23  2002/06/05 20:26:35  dennis
 *  Now implements the IXmlIO interface using "stubs" that just return false
 *  for the read and write methods.  These MUST be overidden in derived classes.
 *  Also, now includes a maximum label length that is used when concatenating
 *  label attributes.
 *
 *  Revision 1.22  2002/06/05 13:47:14  dennis
 *  Made "name" a protected field rather than private, since the name
 *  will have to be read/written to XML files by subclasses.
 *
 *  Revision 1.21  2002/03/28 19:34:39  pfpeterson
 *  Added new string constant for TIME_OFFSET attribute.
 *
 *  Revision 1.20  2002/02/26 21:17:26  pfpeterson
 *  Added some string constants for SDDS data.
 *
 *  Revision 1.19  2002/02/25 17:35:51  pfpeterson
 *  Added DETECTOR_CEN_HEIGHT string constant.
 *
 *  Revision 1.18  2002/02/25 14:25:51  dennis
 *  Un-commented "constant" names for LABEL, SEGMENT_IDS, CRATE, SLOT, etc.
 *
 *  Revision 1.17  2002/02/04 22:59:41  dennis
 *  Temporarily added strings for Sample Chi, Phi and Omega for SCD.
 *  These are currently commented out.
 *
 *  Revision 1.16  2001/12/21 17:29:33  dennis
 *  Added attribute for Segment IDS.  This is currently commented
 *  out to avoud breaking the server<->client communication.
 *
 *  Revision 1.15  2001/12/14 22:19:19  dennis
 *  Added attributes for LABEL and nominal and actual SOURCE_TO_SAMPLE_TOF.
 *  These attributes are currently commented out to avoid breaking the
 *  server<->client communication.
 *
 */

package  DataSetTools.dataset;

import  java.io.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
 * The abstract root class for attributes used in data objects.  Attribute
 * is an abstract class that bundles a name and value together.  Each concrete
 * subclass will use a particular type of value.  That is, the value may
 * be a float, a character string, an array of floats, etc.  A method to 
 * compare the values of two attribute objects is also provided. 
 *  
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntListAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 */


abstract public class Attribute implements Serializable, 
                                           IXmlIO
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public static final long serialVersionUID = 1L;

  /**
   * MAX_LABEL_LENGTH = "80" is the name of an integer Attribute indicating that
   * the maximum length for a label is 80 characters.
   */
  public static final int     MAX_LABEL_LENGTH  = 80; 

  // integer codes for the type of attributes that should be included in a
  // DataSet or Data block.
  /**
   * NO_ATTRIBUTES = "0" is the name of an integer argument indicating that
   * no Attributes should be included in a DataSet or Data block.
   */  
  public static final int  NO_ATTRIBUTES       = 0; 
  
  /**
   * ANALYSIS_ATTRIBUTES = "1" is the name of an integer argument indicating 
   * that Analysis Attributes should be included in a DataSet or Data block.
   */   
  public static final int  ANALYSIS_ATTRIBUTES = 1; 
  
  /**
   * FULL_ATTRIBUTES = "2" is the name of an integer argument indicating 
   * that Full Attributes should be included in a DataSet or Data block.
   */    
  public static final int  FULL_ATTRIBUTES     = 2; 


  // Suggested names for attributes for neutron scattering data sets:
  /**
   * TITLE = "DataSet Name" is the name of a String Attribute giving the Title
   * for this DataSet.
   */
  public static final String  TITLE             = "DataSet Name";
  
  /**
   * LABEL = "Label" is the name of a String Attribute giving the Label
   * for this DataSet.
   */  
  public static final String  LABEL             = "Label";
  
  /**
   * DS_TAG = "DataSet Tag" is the name of an integer Attribute giving the 
   * DataSet Tag for this DataSet. <br><br>
   *
   * Each DataSet has this unique tag.
   */    
  public static final String  DS_TAG            = "DataSet Tag";
  
  /**
   * USER = "User Name" is the name of a String Attribute giving the User Name
   * for this DataSet. 
   */    
  public static final String  USER              = "User Name";

  /**
   * INST_NAME = "Instrument Name" is the name of a String Attribute giving the 
   * Instrument Name for this DataSet.
   */  
  public static final String  INST_NAME         = "Instrument Name";
  
            
  
  /**
   * INST_TYPE = "Instrument Type" is the name of an integer Attribute giving 
   * the Instrument Type for this DataSet. <br><br>
   *
   * INST_TYPE is an integer ranging from 1 to 11 (0 is UNKNOWN).  Each of these
   * integers corresponds to an instrument type.  The integer values are listed
   * below.
   *
   * TOFNPD (1), Time-of-flight Neutron Powder Diffractometer
   * TOFNSCD (2), Time-of-flight Neutron Single Crystal Diffractometer
   * TOFNSAS (3), Time-of-flight Neutron Small Angle Scattering
   * TOFNREF (4), Time-of-flight Neutron Reflectometer
   * TOFNDGS (5), Time-of-flight Neutron Direct Geometry Spectrometer
   * TOFNIGS (6), Time-of-flight Neutron Inverse Direct Geometry Spectrometer
   * MONONXTAS (7), Monochromatic Neutron Triple Axis Spectrometer
   * MONONXPD (8), Monochromatic Neutron Powder Diffractometer
   * MONONXSCD (9), Monochromatic Neutron Single Crystal Diffractometer
   * MONONXSAS (10), Monochromatic Neutron Small Angle Scattering
   * MONONXREF (11), Monochromatic Neutron Reflectrometer   
   */    
  public static final String  INST_TYPE         = "Instrument Type";

  /**
   * FILE_NAME = "File" is the name of a String Attribute giving the File Name.
   */  
  public static final String  FILE_NAME         = "File";
  
  /**
   * RUN_TITLE = "Run Title" is the name of a String Attribute giving the Run
   * Title.
   */   
  public static final String  RUN_TITLE         = "Run Title";
  
  /**
   * RUN_NUM = "Run Number" is the name of an integer array Attribute giving the
   * Run Number.
   */     
  public static final String  RUN_NUM           = "Run Number";
  
  /**
   * END_DATE = "End Date" is the name of a String Attribute giving the End
   * Date for the experiment.
   */     
  public static final String  END_DATE          = "End Date";
  
  /**
   * END_TIME = "End Time" is the name of a String Attribute giving the End     
   * Time for the experiment.
   */     
  public static final String  END_TIME          = "End Time";
  
  /**
   * START_DATE = "Start Date" is the name of a String Attribute giving the     
   * Start Date for the experiment.
   */     
  public static final String  START_DATE        = "Start Date";
  
  /**
   * START_TIME = "Start Time" is the name of a String Attribute giving the
   * Start Time for the experiment.
   */    
  public static final String  START_TIME        = "Start Time";
  
  /**
   * UPDATE_TIME = "Update Time" is the name of a String Attribute giving the
   * Update Time for the experiment.
   */    
  public static final String  UPDATE_TIME       = "Update Time";

  /**
   * DETECTOR_POS = "Effective Position" is the name of a DetectorPosition 
   * Attribute giving the effective position of the detector (or detector
   * pixel) for the Data block.
   */  
  public static final String  DETECTOR_POS      = "Effective Position";
  
  /**
   * RAW_ANGLE = "Raw Detector Angle" is the name of a float Attribute giving
   * the Raw Detector Angle.
   *
   * This is the only Attribute angle measured in degrees (all others are 
   * measurd in radians).
   */    
  public static final String  RAW_ANGLE         = "Raw Detector Angle";
  
  /**
   * RAW_DISTANCE = "Ave. Raw Detector Distance" is the name of a float  
   * Attribute giving the Average Raw Detector Distance in meters.
   */   
  public static final String  RAW_DISTANCE      = "Ave. Raw Detector Distance";
  
  /**
   * SOLID_ANGLE = "Total Solid Angle" is the name of a float Attribute 
   * giving the Total Solid Angle subtended by a detector element in steradians.
   */    
  public static final String  SOLID_ANGLE       = "Total Solid Angle";
  
  /**
   * OMEGA = "Omega" is the name of a float Attribute giving Omega.
   */   
  public static final String  OMEGA             = "Omega";
  
  /**
   * DELTA_2THETA = "\u0394"+"2"+"\u03b8" is the name of a float Attribute 
   * giving the range of scattering angles covered by a detector.  Delta and
   * theta are angles measured in degrees.  '\u0394' is unicode for delta and 
   * '\u03b8' is unicode for theta.
   */    
  public static final String  DELTA_2THETA      = "\u0394"+"2"+"\u03b8";  

  /**
   * EFFICIENCY_FACTOR = "Efficiency" is the name of a float Attribute giving
   * the relative efficiency of a detector element.
   */    
  public static final String  EFFICIENCY_FACTOR = "Efficiency";
  
  /**
   * DETECTOR_IDS = "Detector IDs" is the name of an integer array Attribute 
   * giving the list of IDs of detectors that were used for this spectrum.
   */   
  public static final String  DETECTOR_IDS      = "Detector IDs";
  
  /**
   * SEGMENT_IDS = "Segment IDs" is the name of an integer array Attribute 
   * giving the list of IDs of segments that were used for this spectrum.
   */    
  public static final String  SEGMENT_IDS       = "Segment IDs";
  
  /**
   * GROUP_ID = "Group ID" is the name of an integer Attribute giving
   * the Group ID for the current spectrum.
   */   
  public static final String  GROUP_ID          = "Group ID";

  /**
   * TIME_FIELD_TYPE = "Time Field Type" is the name of an integer Attribute 
   * giving the Time Field Type. (obsolete)
   */   
  public static final String  TIME_FIELD_TYPE   = "Time Field Type";
  
  /**
   * CRATE = "Crate" is the name of an integer array Attribute giving the Crates
   * that were used for this spectrum.
   */   
  public static final String  CRATE             = "Crate";
  
  /**
   * SLOT = "Slot" is the name of an integer array Attribute giving the Slots
   * that were used for this spectrum.   
   */    
  public static final String  SLOT              = "Slot";
  
  /**
   * INPUT = "Input" is the name of an integer array Attribute giving the Inputs
   * that were used for this spectrum.   
   */   
  public static final String  INPUT             = "Input";

  /**
   * DETECTOR_CEN_DISTANCE = "Detector center distance" is the name of a float 
   * Attribute giving the distance from the sample to the detector center in 
   * meters.
   */ 
  public static final String  DETECTOR_CEN_DISTANCE = 
                                                  "Detector center distance";
						  
  /**
   * DETECTOR_CEN_ANGLE = "Detector center angle" is the name of a float 
   * Attribute giving the angle to the Detector center in degrees.
   *
   * This is the angle between the direction of the incident beam and a vector
   * from the sample to the detector center.
   */ 						  
  public static final String  DETECTOR_CEN_ANGLE = "Detector center angle";
						  
  /**
   * DETECTOR_CEN_HEIGHT = "Detector center height" is the name of a float 
   * Attribute giving the height of the detector center above (or below) the
   * "scattering plane" in meters.
   */ 						  
  public static final String  DETECTOR_CEN_HEIGHT = "Detector center height";
						  
  /**
   * DETECTOR_DATA_GRID = "Detector data grid" is the name of a DataGrid 
   * Attribute giving the Detector data grid object.
   * <br><br>
   * @see DataSetTools.dataset.IDataGrid
   * @see DataSetTools.dataset.UniformGrid
   */ 							  
  public static final String  DETECTOR_DATA_GRID = "Detector data grid";

  /**
   * INITIAL_PATH = "Initial Path" is the name of a float Attribute giving the 
   * Initial Path length from the source to the sample.
   */ 
  public static final String  INITIAL_PATH      = "Initial Path";
  
  /**
   * ENERGY_IN = "Energy In" is the name of a float Attribute giving the
   * incident energy for a direct geometry spectrometer.
   *
   * The unit of measurement is a meV (milli electron volts).      
   */   
  public static final String  ENERGY_IN         = "Energy In";
  
  /**
   * NOMINAL_ENERGY_IN = "Nominal Energy In" is the name of a float Attribute 
   * giving the Nominal incident energy for a direct geometry spectrometer.
   *
   * The unit of measurement is a meV (milli electron volts).
   */   
  public static final String  NOMINAL_ENERGY_IN = "Nominal Energy In";
							   
  /**
   * ENERGY_OUT = "Energy Out" is the name of a float Attribute giving the 
   * energy for the scattered neutrons for a direct geometry spectrometer.
   *
   * The unit of measurement is a meV (milli electron volts).   
   */   							   
  public static final String  ENERGY_OUT        = "Energy Out";
  
  /**
   * NOMINAL_SOURCE_TO_SAMPLE_TOF = "Nominal Source to Sample TOF" is the name 
   * of a float Attribute giving the Nominal TOF (Time-of-flight) from the 
   * Source to the Sample, measured in microseconds.
   */    
  public static final String  NOMINAL_SOURCE_TO_SAMPLE_TOF = 
                                                "Nominal Source to Sample TOF";
						
  /**
   * SOURCE_TO_SAMPLE_TOF = "Source to Sample TOF" is the name of a float
   * Attribute giving the TOF from the Source to the Sample, measured in 
   * microseconds.
   */   						
  public static final String  SOURCE_TO_SAMPLE_TOF = 
                                                  "Source to Sample TOF";
	
  /**
   * T0_SHIFT = "T0 shift" is the name of a float Attribute giving an offset for
   * the pulse, measured in microseconds.
   */ 						  
  public static final String  T0_SHIFT          = "T0 shift";

  /**
   * SAMPLE_CHI = "Sample Chi" is the name of a float Attribute giving the
   * Sample Chi angle in radians.
   */ 	
  public static final String  SAMPLE_CHI        = "Sample Chi";
  
  /**
   * SAMPLE_PHI = "Sample Phi" is the name of a float Attribute giving the
   * Sample Phi angle in radians.
   */   
  public static final String  SAMPLE_PHI        = "Sample Phi";
  
  /**
   * SAMPLE_OMEGA = "Sample Omega" is the name of a float Attribute giving the
   * Sample Omega angle in radians.
   */   
  public static final String  SAMPLE_OMEGA      = "Sample Omega";
  
  /**
   * SAMPLE_ORIENTATION = "Sample Orientation" is the name of an Attribute
   * specifying the orientation of teh sample as a SampleOrientation object.
   *
   * @see DataSetTools.instruments.SampleOrientation
   */   
  public static final String  SAMPLE_ORIENTATION = "Sample Orientation";
  
  /**
   * SAMPLE_NAME = "Sample Name" is the name of a String Attribute giving the
   * Sample Name.
   */    
  public static final String  SAMPLE_NAME       = "Sample Name";
  
  /**
   * TEMPERATURE = "Temperature" is the name of a float Attribute giving the
   * Temperature.
   */   
  public static final String  TEMPERATURE       = "Temperature";
  
  /**
   * PRESSURE = "Pressure" is the name of a float Attribute giving the Pressure.
   */    
  public static final String  PRESSURE          = "Pressure";
  
  /**
   * MAGNETIC_FIELD = "Magnetic Field" is the name of a float array Attribute 
   * giving the Magnetic Field vector.
   */   
  public static final String  MAGNETIC_FIELD    = "Magnetic Field";
  
  /**
   * NUMBER_OF_PULSES = "Number of Pulses" is the name of an integer Attribute 
   * giving the Number of Pulses on target for this spectrum.
   */   
  public static final String  NUMBER_OF_PULSES  = "Number of Pulses";
  
  /**
   * TOTAL_COUNT = "Total Count" is the name of a float Attribute giving the
   * total of all counts in all bins of this histogram. <br><br>
   *
   * Note: This is not automatically updated when operations are performed on
   * the spectrum.
   */   
  public static final String  TOTAL_COUNT       = "Total Count";

  /**
   * Q_VALUE = "Q(invA)" is the name of a float Attribute giving the Q Value
   * corresponding to a slice through a I(Q,E) display in inverse Algorithms.
   */ 
  public static final String  Q_VALUE           = "Q(invA)";
  
  /**
   * GSAS_CALIB = "GSAS calibration" is the name of an Attribute storing the
   * GSAS calibration information in a GsasCalib object.
   *
   * @see DataSetTools.gsastools.GsasCalib
   */   
  public static final String  GSAS_CALIB        = "GSAS calibration";
  
  /**
   * GSAS_IPARM = "GSAS Instrument Parameter File" is the name of a String 
   * Attribute giving the GSAS Instrument Parameter File.
   */     
  public static final String  GSAS_IPARM        =
                                              "GSAS Instrument Parameter File";
					      
  /**
   * LATTICE_PARAM = "Lattice Parameters" is the name of a float array Attribute
   * giving the Lattice Parameters.
   */   					      
  public static final String LATTICE_PARAM      = "Lattice Parameters";
  
  /**
   * ORIENT_MATRIX = "Orientation Matrix" is the name of a String Attribute 
   * giving the Orientation Matrix.
   */   
  public static final String ORIENT_MATRIX      = "Orientation Matrix";
  
  /**
   * ORIENT_FILE = "Orientation File" is the name of a String Attribute giving
   * the Orientation File.
   */   
  public static final String ORIENT_FILE        = "Orientation File";
  
  /**
   * CELL_VOLUME = "Unit Cell Volume" is the name of a float Attribute giving
   * the Unit Cell Volume in angstroms cubed.
   */   
  public static final String CELL_VOLUME        = "Unit Cell Volume";
  
  /**
   * SCD_CALIB = "SCD Calibration" is the name of a String Attribute giving
   * the name of the SCD Calibration file.
   */   
  public static final String SCD_CALIB          = "SCD Calibration";
  
  /**
   * SCD_CALIB_FILE = "SCD Calibration File" is the name of a String Attribute 
   * giving the SCD Calibration File.
   */   
  public static final String SCD_CALIB_FILE     = "SCD Calibration File";

  /**
   * PIXEL_INFO_LIST = "Pixel Info List" is the name of an Attribute whose value
   * is a PixelInfoList. <br><br>
   *
   * The row and column, position, and solid angle of a detector can be obtained
   * from this PixelInfoList.
   */ 
  public static final String  PIXEL_INFO_LIST   = "Pixel Info List";

  /**
   * DS_TYPE = "Data Set Type" is the name of a String Attribute giving the 
   * Data Set Type. <br><br>
   * 
   * DS_TYPE is an integer ranging from 0 to 6.  Each of these integers
   * corresponds to a Data Set type.  The integer values are listed below.
   *
   * "Unknown" (0)
   * "Invalid Data Set" (1)
   * "Monitor Data" (2)
   * "Sample Data" (3)
   * "Pulse Height" (4)
   * "Temperature Data" (5)
   * "Pressure Data" (6)    
   */ 
  public static final String  DS_TYPE            = "Data Set Type";

  // software grouping and time focusing
  /**
   * TIME_OFFSET = "Time Offset" is the name of a float Attribute giving the 
   * Time Offset in microseconds. (used for software grouping and time focusing)
   */   
  public static final String  TIME_OFFSET        = "Time Offset";

  // stuff for SDDS files
  /**
   * START_TIME_SEC = "Start Time(sec)" is the name of an integer Attribute 
   * giving the Start Time in seconds. (used for SDDS files)
   */   
  public static final String  START_TIME_SEC     = "Start Time(sec)";
  
  /**
   * TIME_OF_DAY = "Time of Day" is the name of a String Attribute giving
   * the Time of the Day. (used for SDDS files)
   */    
  public static final String  TIME_OF_DAY        = "Time of Day";
  
  /**
   * DAY_OF_MONTH = "Day of Month" is the name of a String Attribute giving
   * the Day of the Month. (used for SDDS files)
   */   
  public static final String  DAY_OF_MONTH       = "Day of Month";

  // Suggested value Strings for DataSet attributes:

  /**
   * UNKNOWN = "Unknown" is the name of a String Attribute which is a value
   * String for DataSet attributes (Unknown is an instrument type and a DataSet
   * type).
   *  <br><br>
   * Its corresponding integer value is 0.
   *  <br><br>
   * @see #INST_TYPE <br>
   * @see #DS_TYPE
   */ 
  public static final String  UNKNOWN            = "Unknown";
  
  /**
   * INVALID_DATA_SET = "Invalid Data Set" is the name of a String Attribute 
   * which is a value String for DataSet attributes (Invalid Data Set is a 
   * DataSet type).  It is indicating that the DataSet is invalid.
   *  <br><br>
   * Its corresponding integer value is 1.
   *  <br><br>
   * @see #DS_TYPE   
   */   
  public static final String  INVALID_DATA_SET   = "Invalid Data Set";
  
  /**
   * MONITOR_DATA = "Monitor Data" is the suggested value String for a Monitor
   * Data Set.
   *
   * @see #DS_TYPE      
   */   
  public static final String  MONITOR_DATA       = "Monitor Data";
  
  /**
   * SAMPLE_DATA = "Sample Data" is the suggested value String for a Sample
   * Data Set.
   *
   * @see #DS_TYPE     
   */   
  public static final String  SAMPLE_DATA        = "Sample Data";
  
  /**
   * PULSE_HEIGHT_DATA = "Pulse Height" is the suggested value String for a 
   * Pulse Height Data Set.
   *
   * @see #DS_TYPE     
   */    
  public static final String  PULSE_HEIGHT_DATA  = "Pulse Height";
  
  /**
   * TEMPERATURE_DATA = "Temperature Data" is the suggested value String for a 
   * Temperature Data Set.
   *
   * @see #DS_TYPE     
   */   
  public static final String  TEMPERATURE_DATA   = "Temperature Data";
  
  /**
   * PRESSURE_DATA = "Pressure Data" is the suggested value String for a
   * Pressure Data Set.
   *
   * @see #DS_TYPE     
   */   
  public static final String  PRESSURE_DATA      = "Pressure Data";
  

  /**
   * TOF_DIFFRACTOMETER_S = "TOFNPD" is the suggested value String for a 
   * Time-of-flight Neutron Powder Diffractometer.
   */ 
  public static final String TOF_DIFFRACTOMETER_S        = "TOFNPD";
  
  /**
   * TOF_SCD_S = "TOFNSCD" is the suggested value String for a
   * Time-of-flight Neutron Single Crystal Diffractometer.
   */   
  public static final String TOF_SCD_S                   = "TOFNSCD";
  
  /**
   * TOF_SAD_S = "TOFNSAS" is the suggested value String for a
   * Time-of-flight Neutron Small Angle Scattering.   
   */     
  public static final String TOF_SAD_S                   = "TOFNSAS";
  
  /**
   * TOF_REFLECTROMETER_S = "TOFNREF" is the suggested value String for a
   * Time-of-flight Neutron Reflectrometer.    
   */    
  public static final String TOF_REFLECTROMETER_S        = "TOFNREF";

  /**
   * TOF_DG_SPECTROMETER_S = "TOFNDGS" is the suggested value String for a 
   * Time-of-flight Neutron Direct Geometry Scattering.    
   */   
  public static final String TOF_DG_SPECTROMETER_S       = "TOFNDGS";

  /**
   * TOF_IDG_SPECTROMETER_S = "TOFNIGS" is the suggested value String for a
   * Time-of-flight Neutron Inverse Direct Geometry Scattering.   
   */    
  public static final String TOF_IDG_SPECTROMETER_S      = "TOFNIGS";


  /**
   * TRIPLE_AXIS_SPECTROMETER_S = "MONONXTAS" is the suggested value String for
   * a Triple Axis Spectrometer.
   */ 
  public static final String TRIPLE_AXIS_SPECTROMETER_S  = "MONONXTAS";

  /**
   * MONO_CHROM_DIFFRACTOMETER_S = "MONONXPD" is the suggested value String for
   * a Monochromatic Powder Diffractometer.
   */   
  public static final String MONO_CHROM_DIFFRACTOMETER_S = "MONONXPD";
  
  /**
   * MONO_CHROM_SCD_S = "MONONXSCD" is the suggested value String for a
   * Monochromatic Single Crystal Diffractometer.
   */   
  public static final String MONO_CHROM_SCD_S            = "MONONXSCD";
  
  /**
   * MONO_CHROM_SAD_S = "MONONXSAS" is the suggested value String for a
   * Monochromatic Small Angle Scattering.
   */    
  public static final String MONO_CHROM_SAD_S            = "MONONXSAS";
  
  /**
   * MONO_CHROM_REFLECTROMETER_S = "MONONXSAS" is the suggested value String for
   * a Monochromatic Reflectrometer.
   */      
  public static final String MONO_CHROM_REFLECTROMETER_S = "MONONXREF";
 
  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  protected String name;

  /**
   *  Since Attribute is an abstract class, this constructor is never called
   *  directly.
   *
   *  @param name  The name to give to this attribute.
   */

  protected Attribute( String name )
  {
    this.name = name;
  }

  /**
   * Returns an integer flag to indicate if the value of the current Attribute
   * is "greater", "equal" or "less" than the value of the specified 
   * second_attribute.  The meaning of "greater", "equal" and "less" will 
   * depend on the specific concrete attributes.  The attribute parameter
   * "second_attribute" should have the same class as the current attribute.
   *
   * @param second_attribute  The second attribute whose value is compared to
   *                          the current attribute value.
   *
   * @return 1,0, or -1 based on comparing the value of the current attribute
   *         with the value of the second attribute.
   * The return value is:
   *         +1       if the current value is greater than the second value
   *          0       if the current value is equal to the second value
   *         -1       if the current value is less than the second value
   */

  public int compare( Attribute second_attribute )

  {                                               // compare strings, if that's
                                                  // what we have
    if ( this  instanceof StringAttribute && 
         second_attribute instanceof StringAttribute      )
      return ((String)getValue()).compareTo(
              (String)second_attribute.getValue() );

                                              // otherwise compare attributes 
                                              // based on numeric value

    double this_key   = getNumericValue(); 
    double second_key = second_attribute.getNumericValue();

    if ( this_key > second_key )
      return( 1 );
    else if ( this_key == second_key )
      return( 0 );
    else
      return( -1 );
  }


  /**
   * Returns the name of the attribute 
   *
   * @return A reference to the String containing the attribute name. 
   */
  public String getName() { return name; }


  /**
   * Set the name for the attribute
   *
   * @param  name  The new name to use for this attribute. 
   */
  public void setName( String name )
  {
    this.name = name;
  }


  /**
   * Returns the value of the attribute, as a generic object
   *
   * @return the value of this attribute as a generic object.
   */
  abstract public Object getValue(); 


  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  Any 
   * non-trivial behavior will be specified in derived classes.  For example,
   * combining StringAttributes could mean concatenating the strings while 
   * combining FloatAttributes could mean averaging the float values. 
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   *                  
   */
  public Attribute combine( Attribute attr )
  {
     // by default, no operation is done... the value of the combined attribute
     // is just the current value
     return this;
  }


  /**
   * Add the value of the specified attribute to the value of this
   * attribute obtain a new value for this attribute.  The default behavior
   * is just to call combine().  This will be overidden in derived classes,
   * when special behavior needed. 
   *
   *  @param   attr   An attribute whose value is to be "added" to the
   *                  value of the this attribute.
   *
   */
  public Attribute add( Attribute attr )
  {
    // by default, add will be the same as combine, unless this is overridden
    // in derived classes.
    return combine(attr);
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   *
   * @return Double.MAX_VALUE, unless this method is overridden in the
   *         concrete derived class.
   */
  public double getNumericValue()
  {
    return Double.MAX_VALUE;
  }


  /**
   * Returns a string representation of the value for this attribute
   *
   * @return String form of attribute value only.
   */
  abstract public String getStringValue();


  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute.
   *
   * @return String form of attribute name, value.
   */
  abstract public String toString();


  /**
   *  Build an Attribute object, if possible, given a title string and an
   *  object holding the value for the Attribute.
   *
   *  @param  name     String holding the name of the attribute.
   *  @param  value    An Object holding the value for the attribute.  
   *                   Integer, Float, Double, String, Integer[] and
   *                   DetectorPosition are supported.
   *
   *  @return  If it is possible to construct an Attribute using the specified
   *           Object, this returns an Attribute of the correct type.  If not,
   *           this returns an ErrorString object. 
   */
  public static Object Build( String name, Object value )
  {
    Attribute A = null;

    if ( value == null )
      return new ErrorString(" null value object");

    if ( name == null )
      return new ErrorString(" null name object");

    else if( value instanceof Integer)
      A = new IntAttribute(name, ((Integer)value).intValue());

    else if( value instanceof Float)
      A = new FloatAttribute( name , ((Float)value).floatValue());

    else if( value instanceof Double)
      A = new DoubleAttribute( name , ((Double)value).doubleValue());

    else if( value instanceof String)
      A = new StringAttribute( name , (String) value);

  
    else if( value instanceof DetectorPosition )
      A = new DetPosAttribute( name, (DetectorPosition)value );

    else if( value instanceof int[] )
      A = new IntListAttribute( name , (int[])value );
    
    else if( value instanceof IntListString)
	{ 
          A = new IntListAttribute( name, 
            IntList.ToArray(((IntListString)value).toString()));
        }
    else if( value instanceof Integer[] )          // copy values into an int[]
    {
      int n_vals = ((Integer[])value).length;
      int vals[] = new int[ n_vals ];
      for ( int i = 0; i < n_vals; i++ )
        vals[i] = ((Integer[])value)[i].intValue();
      A = new IntListAttribute( name , vals );
    }

    else if( value instanceof SpecialString)
      A = new StringAttribute( name, value.toString());

    else
      return new ErrorString(" can't build Attribute for " + 
                               value.getClass().getName()  );
   
    return A;
  }

  /**
   *  Write the state of this object out to the specified stream in XML format.
   *
   *  @param  stream   The stream to write to.
   *  @param  mode     Flag indicating whether or not to write the value in
   *                   base 64 encoding.
   *
   *  @return false, derived classes must override this to actually be written
   *          to the stream. 
   */
  public boolean XMLwrite( OutputStream stream, int mode )
  {
    return false;
  }


  /**
   *  Read the state of this object from the specified stream in XML format.
   *
   *  @param  stream   The stream to read from.
   *
   *  @return false, derived classes must override this to actually be written
   *          to the stream. 
   */
  public boolean XMLread( InputStream stream )
  {
    return false;
  }

/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information
    
    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:Attribute IsawSerialVersion != 1");
  }
}
