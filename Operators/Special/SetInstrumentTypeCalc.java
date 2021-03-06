/*
 * File:  SetInstrumentTypeCalc.java
 *
 * Copyright (C) 2007, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2007/07/05 14:33:54  dennis
 * This file was moved from SetInstrumentType.java to
 * SetInstrumentTypeCalc.java, to fix problem due to Windows
 * lack of case-sensitivity for file names.
 *
 * Revision 1.4  2007/04/26 21:48:07  dennis
 * Now adds a log message to the DataSet log, indicating that the
 * operators and instrument type attribute have been changed.
 *
 * Revision 1.3  2007/04/26 21:41:41  dennis
 * Now throws an IllegalArgumentException when the DataSet to modify
 * is the immutable EMPTY_DATA_SET.
 *
 * Revision 1.2  2007/04/26 20:46:56  dennis
 * The setInstrumentType() method now throws an IllegalArgumentException
 * if an input parameter is null, or if the specified instrument type
 * is not supported.
 *
 * Revision 1.1  2007/04/18 21:14:29  dennis
 * This class has a static method to set approppriate operators
 * and the INST_TYPE attribute, for a specified instrument type.
 * The instrument type is specified by one of the Strings:
 * TOF_NPD, TOF_NGLAD, TOF_NSCD, TOF_NSAS, TOF_NDGS, TOF_NIGS, TOF_NREFL.
 * Not all of the specified types are fully supported at this time.
 */
package Operators.Special;

import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;

/**
 *  This class contains a static method to reconfigure a DataSet to 
 *  have the operators appropriate to a specified instrument type,
 *  and to set the instrument type attribute to the new type.
 *  NOTE: It will often be necessary to do additional work to properly
 *  change all aspects of the DataSet to the new instrument type.
 *  For example, if the type is changed to a direct geometry 
 *  spectrometer, the incident energy information will also have to
 *  be set in appropriate attributes, using the setAttribute() operator.
 *  IF some needed attributes are not set, some operators will not
 *  work!.
 */
public class SetInstrumentTypeCalc
{
  /**
   *  This method will attempt to configure the specified DataSet with the
   *  operators required for the specified instrument type.  Support for
   *  some of the listed instrument types is NOT complete.  In addition,
   *  other methods may need to be used to set appropriate attributes.  For
   *  example, if configuring a DataSet as a direct geometry spectrometer,
   *  it will be necessary to set the incident energy as an attribute.
   *
   *  The time-of-flight neutron scattering instrument types that could 
   *  eventually be supported are: 
   *  
   *  Diffractometers:                 TOF_NPD, TOF_NGLAD, 
   *  Single crystal diffractometers:  TOF_NSCD
   *  Small angle diffractometers:     TOF_NSAS
   *  Direct geometry spectrometers:   TOF_NDGS
   *  Indirect geometry spectrometers: TOF_NIGS
   *  Reflectometers:                  TOF_NREFL 
   *  
   *  The instrument type should be specified by one of the strings:
   *  TOF_NPD, TOF_NGLAD, TOF_NSCD, TOF_NSAS, TOF_NDGS, TOF_NIGS, TOF_NREFL
   *
   *  @see DataSetTools.instruments.InstrumentType
   *
   *  @param  type  String specifying the instrument type.
   *  @param  ds    The DataSet to modify
   *
   *  @return A string indicating that the operation was completed
   *          successfully.
   *
   *  @throws IllegalArgumentException if the input parameters are null,
   *          or the instrument type is not supported, or the DataSet is
   *          the immutable EMPTY_DATA_SET.
   */
  public static Object setInstrumentType( DataSet ds, String type )
  {
                                // clear out all existing operators and 
                                // reconfigure with general operators and
                                // those specific to the specified instrument

    if ( ds == null )
      throw new IllegalArgumentException( "DataSet is null" );

    if ( ds == DataSetTools.dataset.DataSet.EMPTY_DATA_SET )
      throw new IllegalArgumentException( 
                 "DataSet is special EMPTY_DATA_SET, which can't be changed" );

    if ( type == null )
      throw new IllegalArgumentException( "String specifying 'type' is null" );

    int type_code = InstrumentType.getTypeCodeFromName( type );
    ds.removeAllOperators();
    DataSetFactory.addOperators( ds );
    if ( type_code > 0 )                            // supported instrument
      DataSetFactory.addOperators( ds, type_code );

    ds.setAttribute( new IntAttribute( Attribute.INST_TYPE, type_code ) );
    String message = "Operators and INST_TYPE attribute set to " + type;
    ds.addLog_entry( message );
    return new String(message );
  }


  /**
   *  This is a crude main program to check the basic functionality of
   *  the setInstrumentType() method.
   */
  public static void main( String args[] )
  {
     String directory = "/usr2/ARGONNE_DATA/";
     String file_name = "gppd12358.run";
     RunfileRetriever rr = new RunfileRetriever( directory + file_name );
     DataSet ds = rr.getDataSet( 1 );

     setInstrumentType( ds, "TOF_NDGS" );
     setInstrumentType( ds, "TOF_NPD" );
     new ViewManager( ds, IViewManager.IMAGE );

     file_name = "hrcs2447.run";
     rr = new RunfileRetriever( directory + file_name );
     DataSet ds_2 = rr.getDataSet( 1 );

  //   setInstrumentType( ds_2, "TOF_NPD" );
  //   setInstrumentType( ds_2, "TOF_NDGS" );
     new ViewManager( ds_2, IViewManager.IMAGE );
  }

}
