/*
 * File:  GsasCalib.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2002/11/27 23:15:00  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/08/01 22:37:50  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.1  2002/07/10 16:02:49  pfpeterson
 *  Added to CVS.
 *
 *
 */
package DataSetTools.gsastools;

import java.io.*;
import DataSetTools.dataset.*;
/*import DataSetTools.util.*; 
  import java.awt.*;
  import javax.swing.*;
  import java.text.DateFormat;
  import java.text.*;
  import DataSetTools.math.*;
  import DataSetTools.operator.*;
  import DataSetTools.operator.Generic.Special.*;
  import DataSetTools.retriever.RunfileRetriever;*/

/**
 * Structure to hold the time-of-flight to d-spacing conversion
 * factors (DIFA, DIFC, TZERO).
 */

public class GsasCalib implements IXmlIO,
                                  Serializable
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
  private float dif_c;
  private float dif_a; 
  private float t_zero;

    /**
     * The constructor creates an immutable GsasCalib object. It's
     * values can be accessed through the methods provided.
     */
    public GsasCalib(float c, float a, float tz){
        this.dif_c  = c;
        this.dif_a  = a;
        this.t_zero = tz;
    }
    
    /**
     * Returns the value of DIFC. DIFC is the geometric contribution
     * to the conversion.
     */
    public float dif_c(){
        return this.dif_c;
    }

    /**
     * Returns the value of DIFA. DIFA is the quadratic term of the
     * empirical conversion relation.
     */
    public float dif_a(){
        return this.dif_a;
    }

    /**
     * Returns the value of TZERO. TZERO is the static offset of the
     * empirical conversion relation.
     */
    public float t_zero(){
        return this.t_zero;
    }

    /**
     * The toString method required of all objects.
     */
    public String toString(){
        return "DIFC="+this.dif_c+" DIFA="+this.dif_a+" TZERO="+this.t_zero;
    }

    /**
     * The clone method required of all objects.
     */
    public Object clone(){
        return new GsasCalib( this.dif_c, this.dif_a, this.t_zero );
    }
    
    /**
     * @param stream The OutputStream to which the data is written.
     * @param mode   Either IXmlIO.BASE64 or IXmlIO.NORMAL. This object
     *               ignores the mode selection, always using
     *               IXmlIO.NORMAL.
     * @return       True if successful, false if not.
     */
    public boolean XMLwrite( OutputStream stream, int mode ){
        StringBuffer S=new StringBuffer(100);
        S.append( "<GsasCalib>\n" );
        S.append( "<dif_c>"  + this.dif_c  + "</dif_c>\n" );
        S.append( "<dif_a>"  + this.dif_a  + "</dif_a>\n" );
        S.append( "<t_zero>" + this.t_zero + "</t_zero>\n" );
        S.append( "</GsasCalib>\n" );
        try{
            stream.write(S.toString().getBytes());
            return true;
        }catch( IOException e){
            return xml_utils.setError("IOException="+e.getMessage());
        }
    }

    /**
     * @param stream The InStream the data is read from.
     * @return       True if successful, otherwise false.
     */
    public boolean XMLread( InputStream stream ){
        try{
            String Tag=xml_utils.getTag(stream);
            if( Tag==null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            else if( !Tag.equals("dif_c"))
                return xml_utils.setError("Wrong tag order in GsasCalib"+Tag);
            String vString = xml_utils.getValue(stream);
            if(vString == null)
                return xml_utils.setError(xml_utils.getErrorMessage());
            this.dif_c =(new Float(vString)).floatValue();
            
            Tag = xml_utils.getTag(stream);
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            else if( !Tag.equals("dif_a"))
                return xml_utils.setError("Wrong tag order in GsasCalib"+Tag);
            vString = xml_utils.getValue(stream);
            if(vString == null)
                return xml_utils.setError(xml_utils.getErrorMessage());
            this.dif_a =(new Float(vString)).floatValue();
            
            Tag = xml_utils.getTag(stream);
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            else if( !Tag.equals("t_zero"))
                return xml_utils.setError("Wrong tag order in GsasCalib"+Tag);
            vString = xml_utils.getValue(stream);
            if(vString == null)
                return xml_utils.setError(xml_utils.getErrorMessage());
            this.t_zero =(new Float(vString)).floatValue();
            
            Tag = xml_utils.getTag(stream);
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("/GsasCalib"))
                return xml_utils.setError("No End tag in GsasCalib");
            return true;
        }catch(Exception e){
            return xml_utils.setError( "Exception="+e.getMessage());
        }
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
      System.out.println("Warning:GsasCalib IsawSerialVersion != 1");
  }

}
