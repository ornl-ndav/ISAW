/*
 * @(#)InstrumentType.java     0.1  99/07/08  Dennis Mikkelson
 *
 *
 *  $Log$
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
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.2  2000/05/11 16:42:51  dennis
 *  added RCS logging
 *
 */
package DataSetTools.instruments;

import java.io.*;
import DataSetTools.util.*;

/**
 *  This class defines constants for various instrument types and provides
 *  static methods for determining the instrument type from the file name
 *  for instruments at IPNS.
 */


public class InstrumentType implements Serializable
{
  public static final int  UNKNOWN              = 0;
  public static final int  TOF_DIFFRACTOMETER   = 1;    // powder & amorphous
  public static final int  TOF_SCD              = 2;    // single crystal
  public static final int  TOF_SAD              = 3;    // small angle

  public static final int  TOF_REFLECTROMETER   = 4;

  public static final int  TOF_DG_SPECTROMETER  = 5;    // direct geometry
  public static final int  TOF_IDG_SPECTROMETER = 6;    // indirect geometry
  



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
