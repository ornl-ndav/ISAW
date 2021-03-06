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
 *  Revision 1.18  2007/04/18 21:10:33  dennis
 *  Added method, getTypeCodeFromName() that accepts one of the names:
 *  TOF_NPD, TOF_NGLAD, TOF_NSCD, TOF_NSAS, TOF_NDGS, TOF_NIGS, TOF_NREFL,
 *  and returns the corresponding integer type code defined in the
 *  IPNS package.
 *
 *  Revision 1.17  2004/06/02 20:30:48  dennis
 *  formIPNSFilename() now pads instrument name SAD with "1" and all
 *  other three character instrument names with "0".  Also, the file
 *  name is converted to all uppercase or all lower case, depending on
 *  the case of the first character of the instrument name.
 *  (Mixed case names are not supported.)
 *
 *  Revision 1.16  2004/05/21 19:09:02  dennis
 *  Changed method formIPNSFileName() to extend all instrument name
 *  strings to four characters by padding zeros.  Eg. SCD is extended
 *  to SCD0.  Also, formIPNSFileName now forces the name to be either
 *  all uppercase, or all lowercase.   The name length change fixes
 *  a bug with SCD file names when the run number changed from 9999
 *  to 10000.  The file names changed from SCD09999.RUN to SCD010000.RUN
 *  not SCD10000.RUN
 *
 *  Revision 1.15  2004/03/19 17:22:05  dennis
 *  Removed unused variable(s)
 *
 *  Revision 1.14  2004/03/15 06:10:40  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.13  2004/03/15 03:28:15  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.12  2003/10/15 01:40:46  hammonds
 *  Fix spelling for reflectometer
 *
 *  Revision 1.11  2003/02/18 22:08:46  pfpeterson
 *  Fixed reference to deprecated method.
 *
 *  Revision 1.10  2002/11/27 23:15:15  pfpeterson
 *  standardized header
 *
 *  Revision 1.9  2002/08/02 22:54:57  dennis
 *  Code to form runfile name from instument name + run number
 *  now also works for SCD.
 *
 */
package DataSetTools.instruments;

import gov.anl.ipns.Util.Sys.*;

import java.io.*;

/**
 *  This class defines constants for various instrument types and provides
 *  static methods for determining the instrument type from the file name
 *  for instruments at IPNS.
 */


public class InstrumentType implements Serializable
{
  public static final String TOF_NPD   = "TOF_NPD";

  public static final String TOF_NGLAD = "TOF_NGLAD";

  public static final String TOF_NSCD  = "TOF_NSCD";

  public static final String TOF_NSAS  = "TOF_NSAS";

  public static final String TOF_NDGS  = "TOF_NDGS";

  public static final String TOF_NIGS  = "TOF_NIGS";

  public static final String TOF_NREFL = "TOF_NREFL";

  public static final int UNKNOWN                   = 
                          IPNS.Runfile.InstrumentType.UNKNOWN;

  public static final int TOF_DIFFRACTOMETER        = 
                          IPNS.Runfile.InstrumentType.TOF_DIFFRACTOMETER;

  public static final int TOF_SCD                   =    
                          IPNS.Runfile.InstrumentType.TOF_SCD;

  public static final int TOF_SAD                   =    
                          IPNS.Runfile.InstrumentType.TOF_SAD;

  public static final int TOF_REFLECTOMETER        =
                          IPNS.Runfile.InstrumentType.TOF_REFLECTOMETER;

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

  public static final int MONO_CHROM_REFLECTOMETER = 
                          IPNS.Runfile.InstrumentType.MONO_CHROM_REFLECTOMETER;



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
    file_name   = StringUtil.setFileSeparator( file_name );
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
    int    last_slash;

    String temp = file_name.trim();
    file_name   = StringUtil.setFileSeparator( file_name );
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
    int    last_slash;

    String temp = file_name.trim();
    file_name   = StringUtil.setFileSeparator( file_name );
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
      return TOF_REFLECTOMETER;

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
  *  Get the integer instrument type code from a String specifying the
  *  name of the instrument type.  The String must be one of:
  *  TOF_NPD, TOF_NGLAD, TOF_NSCD, TOF_NSAS, TOF_NDGS, TOF_NIGS, TOF_NREFL.
  *
  *  @param  type   A String giving the name of the instrument type.
  *  
  *  @return An integer code for the instrument types as used in the IPNS
  *          runfile package.
  */
  public static int getTypeCodeFromName( String type )
  {
    int type_code = UNKNOWN;

    if ( type.equalsIgnoreCase( TOF_NPD )  ||
         type.equalsIgnoreCase( TOF_NGLAD ) )
      type_code = TOF_DIFFRACTOMETER;

    else if ( type.equalsIgnoreCase( TOF_NSCD ) )
      type_code = TOF_SCD;

    else if ( type.equalsIgnoreCase( TOF_NSAS ) )
      type_code = TOF_SAD;

    else if ( type.equalsIgnoreCase( TOF_NDGS ) )
      type_code = TOF_DG_SPECTROMETER;

    else if ( type.equalsIgnoreCase( TOF_NIGS ) )
      type_code = TOF_IDG_SPECTROMETER;

    else if ( type.equalsIgnoreCase( TOF_NREFL ) )
      type_code = TOF_REFLECTOMETER;

    return type_code;
  }
  

  /**
   *  Get the String instrument name from a int type code specifying the
   *  the instrument name of the form:
   *  TOF_NPD, TOF_NGLAD, TOF_NSCD, TOF_NSAS, TOF_NDGS, TOF_NIGS, TOF_NREFL.
   *
   *  @param  type   Isaw type code for an instrument.
   *  
   *  @return The string name corresponding to the ISAW typeCode.
   */
   public static String getNameFromTypeCode( int type )
   {
     
      if( type ==  TOF_DIFFRACTOMETER)
         return TOF_NPD;

      if( type ==  TOF_SCD)
         return TOF_NSCD;

      if( type ==  TOF_SAD)
         return TOF_NSAS;
      
      if( type ==  TOF_DG_SPECTROMETER)
         return TOF_NDGS;

      
      if( type ==  TOF_DG_SPECTROMETER)
         return TOF_NDGS;
      
      if( type ==  TOF_IDG_SPECTROMETER)
         return TOF_NIGS;
      
      if( type ==  TOF_REFLECTOMETER)
         return TOF_NREFL;
     
   
     return null;
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
      return TOF_REFLECTOMETER;

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
 
    while ( num.length() < 4 )         // use at least 4 digits for number
      num = "0"+num;

    if ( instrument.length() == 3 )    // use at least 4 digits for name
    {
      String prefix = instrument.substring(0,2);
      if ( prefix.equalsIgnoreCase( "SAD" ) )
        instrument = instrument+"1";
      else
        instrument = instrument+"0";
    }

    String file_name = instrument+num+".RUN";
                                       // force all upper or all lower case
                                       // depending on the first character
    if ( Character.isUpperCase( instrument.charAt(0) ) )
      file_name = file_name.toUpperCase();
    else
      file_name = file_name.toLowerCase();

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
