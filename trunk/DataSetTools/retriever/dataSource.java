/*
 * File:  dataSource.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson,
 *                     Dennis Mikkelson
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
 * $Log$
 * Revision 1.3  2001/08/03 21:36:04  dennis
 * getUserName() now returns the user name from the System properties,
 * if no user name is specified.
 *
 * Revision 1.2  2001/08/01 21:52:52  dennis
 * Added java docs and changed methods to return blank strings instead of
 * null when fields are misssing.
 *
 *
 */

package DataSetTools.retriever;

/**
 *  This class parses a string listing information about a data source in a
 *  a string with fields separated by semi-colons.  The format is:
 *  <machine_name>;<port>;<user_name>;<password>;<filename>.  Fields other than
 *  the machine name may be omitted in some cases, but semicolons may not.  
 *  Some valid strings are:
 *  
 *  dmikk.mscs.uwstout.edu;6088;dennis;blank_password;hrcs1797.run
 *  mandrake.pns.anl.gov;;;blank_password;
 *  
 */
public class dataSource
{ 
   String Source;

  /* --------------------------- Constructor ------------------------- */
  /**
   *  Construct a dataSource parsing object for the specified data source.
   *
   *  @param data_source_name  The ";" separated list of information about
   *                           the data source.
   */
  public dataSource( String data_source_name )
  {
    Source = data_source_name;
  }

  /* ------------------------------ check ------------------------------ */
  /**
   *  Check that the string has the right number of semi-colons.
   *
   *  @ return  true if there are 4 semicolons, false otherwise.
   */
  public boolean check()
  {
    if ( getSemiColon( 4 ) < 0 )
      return false;

    if ( getSemiColon( 5 ) >= 0 )
      return false;

    return true;
  }


  /* ----------------------------- getMachine ------------------------- */
  /**
   *  Get the machine name, as the first entry in the string. 
   *
   *  @ return  The string of characters before the first semicolon in
   *            the data source String.  If there are no non-blank characters
   *            before the first semicolon, a blank string is returned.
   */
  public String getMachine()
  {
    int i = getSemiColon( 1 );

    if( i <= 0 ) 
      return "";

    return Source.substring( 0, i ).trim();
  }

  /* ----------------------------- getPort ------------------------- */
  /**
   *  Get the port number, from the second entry in the string.
   *
   *  @ return  The port number specified, or -1 if no positive integer
   *            value is listed between the first and second semicolons.
   */
  public int getPort()
  {
    String i_string = getInteriorString(1); 

    int     value = -1;
    Integer i;

    try
    {
      i = new Integer( i_string );
      value = i.intValue();
    }
    catch ( NumberFormatException e )
    {
    }
    
    return value;
  }

  /* ----------------------------- getUserName ------------------------- */
  /**
   *  Get the user name, from the third entry in the string.
   *
   *  @ return  The string, if any, listed between the second and third 
   *            semicolons or an empty string is there is no such string.
   *            
   */
  public String getUserName()
  {
    String user_name = getInteriorString( 2 );

    if ( user_name.length() <= 0 )
      user_name = System.getProperty("user.name");

    return user_name;
  }

  /* ----------------------------- getPassWord ------------------------- */
  /**
   *  Get the password, from the fourth entry in the string.
   *
   *  @ return  The string, if any, listed between the third and fourth 
   *            semicolons or an empty string is there is no such string.
   *            
   */
  public String getPassWord()
  {
    return getInteriorString( 3 );
  }

  /* ----------------------------- getFileName ------------------------- */
  /**
   *  Get the file name, from the last entry in the string.
   *
   *  @ return  The string, if any, listed after the fourth semicolons or 
   *            an empty string is there is no such string.
   *            
   */
  public String getFileName()
  {
    int i = getSemiColon(4);
   
    if ( i< 0 ) 
      return "";

    if ( i >= Source.length() )
      return "";

    return Source.substring( i+1 ).trim();
  }

  /* ----------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   * 
   */

  /* ----------------------------- getSemiColon ------------------------- */

  private int getSemiColon( int i )
  {
    if( i < 0 )
      return -1;

    if( i > 4 )
      return -1;

    int c = 0;
    for( int k = Source.indexOf(";");  k>=0; )
    {
      c++;
      if( c == i )
        return k;

      if( c > i )
        return -1;

       k = Source.indexOf( ";", k+1 );
     }
     return -1;
   }

  /* ------------------------- getInteriorString ------------------------- */

   public String getInteriorString( int index )
   {
     int i = getSemiColon( index );
     int j = getSemiColon( index + 1 );

     if ((i < 0) || (j < 0) )
       return "";

     if( (i >= j) || (j >= Source.length()) )
        return "";

     return Source.substring( i+1, j ).trim();
   }


  /* ---------------------------- main -------------------------------- */
  /**
   *  main program for testing purposes only.
   */
  public static void main( String args[])
  {
    if( args != null )
    if ( args.length > 0 )
    {
      dataSource DT = new dataSource( args[0] );
      System.out.println( DT.getMachine());
      System.out.println( DT.getPort());
      System.out.println( DT.getUserName());
      System.out.println( DT.getPassWord());
      System.out.println( DT.getFileName());
    }
    System.exit( 0 );
   }
}
