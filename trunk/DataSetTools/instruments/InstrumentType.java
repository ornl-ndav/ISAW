/*
 * File:  InstrumentType.java
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.8  2001/04/25 20:28:10  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.7  2001/03/01 20:50:12  dennis
 *  Now takes the instrument type codes from the class
 *  IPNS.Runfile.InstrumentType
 *
 *  Revision 1.6  2001/02/16 21:53:49  dennis
 *  Added instrument types for triple axis spectrometer and four
 *  mono-chromatic instrument types. ( For compatibility with NeXus.)
 *
 *  Revision 1.5  2000/08/03 21:50:32  dennis
 *  Added methods to get the path and file name separately from the fully
 *  qualified file name.
 *
 *  Revision 1.4  2000/07/13 14:28:28  dennis
 *  Changed formIPNSFileName() method to NOT include the path
 *
 *  Revision 1.3  2000/07/12 18:33:29  dennis
 *  Added method formIPNSFileName() to construct a IPNS runfile
 *  name from the path, instrument name and run number.
 *
 *  Revision 1.2  2000/07/10 22:24:44  dennis
 *  Now Using CVS 
 *
 *  Revision 1.2  2000/05/11 16:42:51  dennis
 *  added RCS logging
 *
 */
package DataSetTools.instruments;

import java.io.*;
import DataSetTools.util.*;
import IPNS.Runfile.*;

/**
 *  This class defines constants for various instrument types and provides
 *  static methods for determining the instrument type from the file name
 *  for instruments at IPNS.
 */


public class InstrumentType implements Serializable
{
  public static final int UNKNOWN                   = 
                          IPNS.Runfile.InstrumentType.UNKNOWN;

  public static final int TOF_DIFFRACTOMETER        = 
                          IPNS.Runfile.InstrumentType.TOF_DIFFRACTOMETER;

  public static final int TOF_SCD                   =    
                          IPNS.Runfile.InstrumentType.TOF_SCD;

  public static final int TOF_SAD                   =    
                          IPNS.Runfile.InstrumentType.TOF_SAD;

  public static final int TOF_REFLECTROMETER        =
                          IPNS.Runfile.InstrumentType.TOF_REFLECTROMETER;

  public static final int TOF_DG_SPECTROMETER       =      // direct geometry
                          IPNS.Runfile.InstrumentType.TOF_DG_SPECTROMETER;

  public static final int TOF_IDG_SPECTROMETER      =      // inverse geometry
                          IPNS.Runfile.InstrumentType.TOF_IDG_SPECTROMETER;


  public static final int TRIPLE_AXIS_SPECTROMETER  = 
                          IPNS.Runfile.InstrumentType.TRIPLE_AXIS_SPECTROMETER;

  public static final int MONO_CHROM_DIFFRACTOMETER =
                          IPNS.Runfile.InstrumentType.MONO_CHROM_DIFFRACTOMETER;

  public static final int MONO_CHROM_SCD            =   
                          IPNS.Runfile.InstrumentType.MONO_CHROM_SCD;

  public static final int MONO_CHROM_SAD            =     
                          IPNS.Runfile.InstrumentType.MONO_CHROM_SAD;

  public static final int MONO_CHROM_REFLECTROMETER = 
                          IPNS.Runfile.InstrumentType.MONO_CHROM_REFLECTROMETER;



  /**
   *  Strip the path from a fully qualified file name and return the base
   *  file name, without the extension.  
   *
   *  @param   file_name   The initial file name complete with the path.
   *                       The characters "/" or "\\" must be used to
   *                       describe the path.
   *
   *  @return              The basic file name without the path or extension.
   */

  public static String getBaseFileName( String file_name )
  {
    int    last_slash,
           dot;

    String temp = file_name.trim();
    file_name   = StringUtil.fixSeparator( file_name );
                                          // Strip the path from the file name
    last_slash = temp.lastIndexOf( File.separator );
    temp = temp.substring( last_slash + 1, temp.length() );

                                          // Strip the "run" from the file name
    dot = temp.indexOf( "." );
    if ( dot == -1 )
      dot = temp.length();

    temp = temp.substring( 0, dot );
    
    return temp;
  }


  /**
   *  Strip the path from a fully qualified file name and return the base
   *  file name, with the extension.
   *
   *  @param   file_name   The initial file name complete with the path.
   *                       The characters "/" or "\\" must be used to
   *                       describe the path.
   *
   *  @return              The basic file name without the path but
   *                       with the extension.
   */

  public static String getFileName( String file_name )
  {
    int    last_slash,
           dot;

    String temp = file_name.trim();
    file_name   = StringUtil.fixSeparator( file_name );
                                          // Strip the path from the file name
    last_slash = temp.lastIndexOf( File.separator );
    temp = temp.substring( last_slash + 1, temp.length() );
                              
    return temp;
  }


  /**
   *  Get the path from a fully qualified file name and return the path 
   *  without the file name.
   *
   *  @param   file_name   The initial file name complete with the path.
   *                       The characters "/" or "\\" must be used to
   *                       describe the path.
   *
   *  @return              the path without the file name.
   */

  public static String getPath( String file_name )
  {
    int    last_slash,
           dot;

    String temp = file_name.trim();
    file_name   = StringUtil.fixSeparator( file_name );
                                          // Strip the path from the file name
    last_slash = temp.lastIndexOf( File.separator );
    temp = temp.substring( 0, last_slash+1 );

    return temp;
  }




  /**
   *  Extract the IPNS instrument name ( such as HRCS ) from a fully qualified
   *  runfile name, including the path.
   *
   *  @param   file_name   The initial file name complete with the path.
   *                       The characters "/" or "\\" must be used to
   *                       describe the path.
   *
   *  @return              The leading alphabetical characters in the basic
   *                       file name.  For IPNS runfiles, this will be the
   *                       IPNS instrument name.
   */

  public static String getIPNSInstrumentName( String file_name )
  {
    String temp = getBaseFileName( file_name );

    temp = temp.toUpperCase();

    int digit = 0;
    while ( digit < temp.length() && !Character.isDigit(temp.charAt(digit)) ) 
      digit++;

    temp = temp.substring( 0, digit );
    return temp;    
  }


  /**
   *  Determine the type of an IPNS instrument based on the name of a
   *  runfile.
   *
   *  @param   file_name   The initial file name complete with the path.
   *                       The characters "/" or "\\" must be used to
   *                       describe the path.
   *
   *  @return              An integer code for the instrument type.  The
   *                       integer codes are defined above.
   */

  public static int getIPNSInstrumentType( String file_name )
  {
    String inst_name = getIPNSInstrumentName( file_name );

    if ( inst_name.equalsIgnoreCase( "GPPD" )  ||
         inst_name.equalsIgnoreCase( "SEPD" )  ||
         inst_name.equalsIgnoreCase( "HIPD" )  ||
         inst_name.equalsIgnoreCase( "GLAD")    ) 
      return TOF_DIFFRACTOMETER;
    
    else if (inst_name.equalsIgnoreCase( "SCD" ) )
      return TOF_SCD;

    else if ( inst_name.equalsIgnoreCase( "SAND" )  ||
              inst_name.equalsIgnoreCase( "SAD" )     )
      return TOF_SAD;

    else if ( inst_name.equalsIgnoreCase( "POSY1" )  ||   // ##### fix this
              inst_name.equalsIgnoreCase( "POSY2" )    )
      return TOF_REFLECTROMETER;

    else if ( inst_name.equalsIgnoreCase( "HRCS" )  ||
              inst_name.equalsIgnoreCase( "LRCS" )    )
      return TOF_DG_SPECTROMETER;
 
    else if ( inst_name.equalsIgnoreCase( "QENS" ) ||
              inst_name.equalsIgnoreCase( "CHEX" )  )
      return TOF_IDG_SPECTROMETER;

    else
      return UNKNOWN;
  }


 /**
  *  Determine the type of an IPNS instrument based on the instrument name
  *  alone.
  *
  *  @param  inst_name  The name of the instrument, such as "HRCS" or "GPPD"
  *
  *  @return            An integer code for the instrument type.  The
  *                     integer codes are defined above.
  */
 public static int getIPNSInstType( String  inst_name)
  {
    if ( inst_name.equalsIgnoreCase( "GPPD" )  ||
         inst_name.equalsIgnoreCase( "SEPD" )  ||
         inst_name.equalsIgnoreCase( "GLAD")    ) 
      return TOF_DIFFRACTOMETER;
    
    else if (inst_name.equalsIgnoreCase( "SCD" ) )
      return TOF_SCD;

    else if ( inst_name.equalsIgnoreCase( "SAND" )  ||
              inst_name.equalsIgnoreCase( "SAD" )     )
      return TOF_SAD;

    else if ( inst_name.equalsIgnoreCase( "POSY1" )  ||   // ##### fix this
              inst_name.equalsIgnoreCase( "POSY2" )    )
      return TOF_REFLECTROMETER;

    else if ( inst_name.equalsIgnoreCase( "HRCS" )  ||
              inst_name.equalsIgnoreCase( "LRCS" )    )
      return TOF_DG_SPECTROMETER;
 
    else if ( inst_name.equalsIgnoreCase( "QENS" ) ||
              inst_name.equalsIgnoreCase( "CHEX" )  )
      return TOF_IDG_SPECTROMETER;

    else
      return UNKNOWN;
  }

 /**
  *  Form an IPNS runfile name from an instrument name and run number.
  *
  *  @param  instrument   The instrument name, such as HRCS or gppd.  If the
  *                       first letter of the name is capitalized, all letters
  *                       in the file name will be capitalized, otherwise,
  *                       all letters in the file name will be lower case.
  *  @param  run_num      Integer giving the run number.  If this integer has
  *                       less than 4 digits, leading zeros will prepended on
  *                       number when forming the file name.
  *
  *  @return  The full file name for the run file, such as HRCS0978.RUN
  */
  
  public static String formIPNSFileName( String instrument,
                                         int    run_num )
  {
    String num = ""+run_num;
    String file_name;
 
    while ( num.length() < 4 )
      num = "0"+num;

    if ( Character.isUpperCase( instrument.charAt(0) ) )
      file_name = instrument+num+".RUN";
    else
      file_name = instrument+num+".run";

    return file_name;
  }

  /* ----------------------------- main ------------------------------------ */

  public static void main(String[] args)
  {
    System.out.println("Test program for class InstrumentType" );

    System.out.println( getBaseFileName( "A:/junk/test/hrcs12345.run" ) );
    System.out.println( getPath( "A:/junk/test/hrcs12345.run" ) );
    System.out.println( getFileName( "A:/junk/test/hrcs12345.run" ) );
    System.out.println( getBaseFileName( "A:\\junk\\test\\hrcs12345.run" ));
    
    System.out.println( getIPNSInstrumentName("A:/junk/test/hrcs12345.run" ) );
    System.out.println( getIPNSInstrumentName("A:\\junk\\test\\hrcs12345.run"));


    System.out.println( "Type of GPPD = " + 
                        getIPNSInstrumentType("A:/junk/test/gppd12345.run"));
    System.out.println( "Type of SEPD = " + 
                        getIPNSInstrumentType("A:/junk/test/sepd12345.run"));
    System.out.println( "Type of GLAD  = " + 
                        getIPNSInstrumentType("A:/junk/test/glad12345.run"));
    System.out.println( "Type of SCD  = " + 
                        getIPNSInstrumentType("A:/junk/test/scd12345.run"));
    System.out.println( "Type of SAD  = " + 
                        getIPNSInstrumentType("A:/junk/test/sad12345.run"));
    System.out.println( "Type of SAND  = " + 
                        getIPNSInstrumentType("A:/junk/test/sand12345.run"));
    System.out.println( "Type of POSY1  = " + 
                        getIPNSInstrumentType("A:/junk/test/POSY112345.run"));
    System.out.println( "Type of POSY2  = " + 
                        getIPNSInstrumentType("A:/junk/test/POSY212345.run"));
    System.out.println( "Type of HRCS  = " + 
                        getIPNSInstrumentType("A:/junk/test/hrcs12345.run"));
    System.out.println( "Type of LRCS  = " + 
                        getIPNSInstrumentType("A:/junk/test/lrcs12345.run"));
    System.out.println( "Type of QENS  = " + 
                        getIPNSInstrumentType("A:/junk/test/qens12345.run"));
    System.out.println( "Type of CHEX  = " + 
                        getIPNSInstrumentType("A:/junk/test/chex12345.run"));
    System.out.println( "Type of JUNK  = " + 
                        getIPNSInstrumentType("A:/junk/test/junk12345.run"));
  }

}
