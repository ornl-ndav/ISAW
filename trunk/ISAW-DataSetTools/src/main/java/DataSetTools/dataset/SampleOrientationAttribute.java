/*
 * File:  SampleOrientationAttribute.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2006/02/12 21:39:30  dennis
 *  Now reads the chi, phi and omega values from the XML file and
 *  constructs a SampleOrientation object after all three have been
 *  read, rather than using a default constructor and setting the
 *  values one at a time as they are read.
 *
 *  Revision 1.3  2004/06/22 15:37:45  rmikk
 *  Added the XMLread and XMLwrite methods
 *  Added a null constructor for the above methods
 *
 *  Revision 1.2  2004/03/15 06:10:38  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.1  2003/02/18 19:00:20  dennis
 *  Initial version.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.instruments.*;
import java.io.*;

/**
 * This class is an Attribute whose value records information about the 
 * sample orientation angles, phi chi and omega.
 */

public class SampleOrientationAttribute extends Attribute
                                                   
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS
  private SampleOrientation value = null;

  /**
   * Constructs a SampleOrientation object using the specified name 
   * and SampleOrientation object.
   */
  public SampleOrientationAttribute( String name, SampleOrientation value )
  {
    super( name );

    this.value = value;
  }

   /**
     *  Needed for the XML-IO system
     */
   public SampleOrientationAttribute(  )
  {
    super( Attribute.SAMPLE_ORIENTATION );

    this.value = null;
  }

  /**
   * Returns reference to the SampleOrientation object kept by this attribute,
   * as a generic object. 
   */
  public Object getValue( )
  {
    return( value );
  } 

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  Since
   * there is no meaningful way to combine the attributes, this just returns
   * a reference to the current attribute.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   */
  public Attribute combine( Attribute attr )
  {
    return this;                                          
  }

  /**
   * Get a numeric value to be used for sorting based on this attribute.
   * In this case we return the sum of the angles.
   */
   public double getNumericValue()
   { 
     SampleOrientation so = (SampleOrientation)value;
     return so.getPhi() + so.getChi() + so.getOmega();
   }

  /**
   * Returns a string representation of the list of PixelInfo objects 
   */
  public String getStringValue()
  {
    SampleOrientation so = (SampleOrientation)value;
    return "phi="    + so.getPhi() + 
           ",chi="   + so.getChi() + 
           ",omega=" + so.getOmega();
  }

  /**
   * Returns a string representation of the (name,value) pair for this
   * attribute
   */
  public String toString()
  {
    return this.getName() + ": " + this.getStringValue();
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
      System.out.println(
                 "Warning:SampleOrientationAttribute IsawSerialVersion != 1");
  }

 
  public boolean XMLwrite( OutputStream stream, int mode )
  {
    SampleOrientation or = (SampleOrientation) getValue();   
    if( or == null)
       return true;

    try{
      StringBuffer sb=new StringBuffer(30 );
      sb.append("<SampleOrientationAttribute>\n<name>" );
      sb.append( this.name );

      sb.append( "</name>\n<value>\n" );
      sb.append( or.getPhi() +" "+or.getChi()+" "+or.getOmega() );

      sb.append( "</value>\n</SampleOrientationAttribute>\n" );

      stream.write( sb.substring(0).getBytes() );
      return true;
    }catch( IOException e ){
      return xml_utils.setError("IO Err="+e.getMessage() );
    }
  }


  public boolean XMLread( InputStream stream )
  {
    //-----------------get name field--------------------
    String Tag = xml_utils.getTag( stream );
    if( Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage() );
    if(!xml_utils.skipAttributes( stream))
      return xml_utils.setError( xml_utils.getErrorMessage() );
    if( !Tag.equals( "name" ) )
      return xml_utils.setError( "name Tag Missing in SampleOrientation" );
    name = xml_utils.getValue( stream );
    if( name == null)
      return xml_utils.setError( "name Tag Missing in SampleOrientation" );
        
    Tag =xml_utils.getEndTag( stream );

    if( Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if( !Tag.equals("/name" ) )
      return xml_utils.setError( "name Tag not nested in SampleOrientation" );
    if(!xml_utils.skipAttributes( stream))
      return xml_utils.setError( xml_utils.getErrorMessage() );
        
    //----------------  get value field----------------
    Tag =xml_utils.getTag( stream );
    if( Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage() );
    if( !Tag.equals("value"))
      return xml_utils.setError( "missing value tag in SampleOrientation"+Tag); 
    if(!xml_utils.skipAttributes( stream))
      return xml_utils.setError( xml_utils.getErrorMessage());


    //-----------actual values--------
    String pcom=xml_utils.getValue( stream);
    String err = xml_utils.getErrorMessage();
    if( pcom == null ) 
      return xml_utils.setError( xml_utils.getErrorMessage() );
    if( err != null)
      return xml_utils.setError( xml_utils.getErrorMessage() );
      
    pcom = pcom.trim();
    try{
      int j = pcom.indexOf(' ');
      String S = pcom.substring(0,j).trim();

      Float F = new Float( S );
      float phi = F.floatValue();

      pcom = pcom.substring(j).trim();
      j=pcom.indexOf(' ');
      float chi   = (new Float( pcom.substring(0,j).trim())).floatValue();
      float omega = (new Float( pcom.substring(  j).trim())).floatValue();

      IPNS_SCD_SampleOrientation samp_orientation = 
                            new IPNS_SCD_SampleOrientation( phi, chi , omega);
      value = samp_orientation;

    }catch( Exception ss ){
           
      DataSetTools.util.SharedData.addmsg( 
                        "Improper format for Sample orientation values" );
      String S ;
      if( !xml_utils.skipAttributes( stream ) )
        return false;
      S = xml_utils.skipBlock( stream );
      if( S == null )
        return false;
      if( !S.equals( "/value" ) )
        return false;
      S = xml_utils.skipBlock( stream );
      if( S == null )
        return false;
      if( !S.equals("/SampleOrientationAttribute") )
        return false;

      if( !xml_utils.skipAttributes( stream ) )
        return false;
      return true;
    }
        
    //--------- Read to end of SampleOrientationAttribute block-----------------
    Tag = xml_utils.getEndTag( stream ); 
    if( Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage() );
    if( !Tag.equals("/value") )
      return xml_utils.setError("Tags not nested in Sample orientation" + Tag );
    if(!xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage() );
        
    Tag =xml_utils.getTag( stream ); 
    if( Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage() );
    if( !Tag.equals( "/SampleOrientationAttribute" ) )
      return xml_utils.setError( "Tags not nested in Sample orientation" + Tag );
    if( !xml_utils.skipAttributes( stream ) )
      return xml_utils.setError( xml_utils.getErrorMessage() );
    return true;
  }

/* ---------------------------- main --------------------------------- */
/*
 *  Basic main program for testing
 */
 
  public static void main( String args[] )
  {
    IPNS_SCD_SampleOrientation so = new IPNS_SCD_SampleOrientation(20,30,40); 
    SampleOrientationAttribute attr = 
          new SampleOrientationAttribute( Attribute.SAMPLE_ORIENTATION, so ); 
    System.out.println("SampleOrientationAttribute is:");
    System.out.println(attr); 
  }

}
