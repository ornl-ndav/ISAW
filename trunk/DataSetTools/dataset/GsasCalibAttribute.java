/*
 * File:  GsasCalibAttribute.java
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
 *  Revision 1.3  2002/11/12 18:13:37  dennis
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
 *  Revision 1.2  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.1  2002/07/10 15:58:16  pfpeterson
 *  Added to CVS.
 *
 */

package  DataSetTools.dataset;

import   java.text.*;
import   DataSetTools.math.*;
import   java.io.*;
import   DataSetTools.gsastools.GsasCalib;
/**
 * The concrete class for an attribute whose value is a
 * GsasCalibration object.
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 */

public class GsasCalibAttribute extends   Attribute
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
  private GsasCalib value;

    /**
     * Constructs a GsasCalibAttribute object using the specified name
     * and value.
     */
    public GsasCalibAttribute( String name, GsasCalib value ){
        super( name );
        this.value = value;
    }

    private GsasCalibAttribute(){
        super("");
        this.value = new GsasCalib(0f,0f,0f);
    }

    /**
     * Returns a copy the GsasCalib object that is the value of this
     * attribute, as a generic object.
     */
    public Object getValue( ){
        return value.clone();
    } 

    /**
     * Returns a copy the DetectorPosition object that is the value of this
     * attribute.
     */
    public GsasCalib getGsasCalib( ){
        return (GsasCalib)value.clone();
    }

    public boolean XMLwrite( OutputStream stream, int mode ){
        try{StringBuffer SS = new StringBuffer(1000);
        SS.append("<GsasCalibAttribute>\n<name>");
        SS.append(name);
        SS.append("</name>\n");
        SS.append( "<value>\n");
        stream.write( SS.substring(0).getBytes());
        if(!((GsasCalib)this.value).XMLwrite( stream, mode))
            return false;
        stream.write( "</value>\n</GsasCalibAttribute>\n".getBytes());
        return true;
        }
        catch( Exception s)
            {return xml_utils.setError( "IO Err="+s.getMessage());
            }
        
    }

    public boolean XMLread( InputStream stream ){
        try{
            //-----------------get name v
            String Tag = xml_utils.getTag( stream );
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if(!xml_utils.skipAttributes( stream))
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("name"))
                return xml_utils.setError("name Tag Missing in GsasCalib");
            name = xml_utils.getValue( stream);
            if( name == null)
                return xml_utils.setError("name Tag Missing in GsasCalib");
            
            Tag =xml_utils.getEndTag( stream );
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("/name"))
                return xml_utils.setError("name Tag not nested in GsasCalib");
            if(!xml_utils.skipAttributes( stream))
                return xml_utils.setError( xml_utils.getErrorMessage());
            
            //----------------  get value field----------------
            Tag =xml_utils.getTag( stream );
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("value"))
                return xml_utils.setError("missing value tag in GsasCalib"+Tag); 
            if(!xml_utils.skipAttributes( stream))
                return xml_utils.setError( xml_utils.getErrorMessage());
            //-----------actual values
            Tag =xml_utils.getTag( stream );
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("GsasCalib"))
                return xml_utils.setError("missing GsasCalib tag in GsasCalib"+
                                           Tag); 
            if(!xml_utils.skipAttributes( stream))
                return xml_utils.setError( xml_utils.getErrorMessage());
            if(!((GsasCalib)this.value).XMLread( stream))
                return false;
            //-------------------- get End tags
            Tag =xml_utils.getTag( stream ); 
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("/value"))
                return xml_utils.setError("Tags not nested in GsasCalib"+Tag);
            if(!xml_utils.skipAttributes( stream ))
                return xml_utils.setError( xml_utils.getErrorMessage());
            
            Tag =xml_utils.getTag( stream ); 
            if( Tag == null)
                return xml_utils.setError( xml_utils.getErrorMessage());
            if( !Tag.equals("/GsasCalibAttribute"))
                return xml_utils.setError("Tags not nested in GsasCalib"+Tag);
            if(!xml_utils.skipAttributes( stream ))
                return xml_utils.setError( xml_utils.getErrorMessage());
            return true;
        }catch( Exception s){
            return xml_utils.setError( "Exception ="+s.getMessage());
        }
    }

    /*
     * Average the values of dif_c, dif_a, and t_zero from the current Attribute
     * with the values from the specified Attribute to obtain values for a 
     * new Attribute.
     *
     *  @param attr An attribute whose values are to be averaged
     *              with the values of the this attribute.
     *
     *  @return A new Attribute whose values are the average of the values of 
     *          the current and specified Attribute.
     */
      public Attribute combine( Attribute attr ){
      if ( !(attr instanceof GsasCalibAttribute) )       // can't combine
      return this;

      float dif_c  = (value.dif_c() + 
                      ((GsasCalibAttribute)attr).value.dif_c())/2;
      float dif_a  = (value.dif_a() + 
                      ((GsasCalibAttribute)attr).value.dif_a())/2;
      float t_zero = (value.t_zero() + 
                      ((GsasCalibAttribute)attr).value.t_zero())/2;
      
      return new GsasCalibAttribute(name, new GsasCalib(dif_c, dif_a, t_zero));
      }
    
    
    /*
     * Add the values of dif_c, dif_a, and t_zero from the current Attribute
     * to the values from the specified Attribute to obtain values for a
     * new Attribute. 
     *
     *  @param attr An attribute whose values are to be "added"
     *              to the values of the this attribute.
     *
     *  @return A new Attribute whose values are the sum of the values of 
     *          the current and specified Attribute.
     */
      public Attribute add( Attribute attr ){
      if ( !(attr instanceof GsasCalibAttribute) )       // can't combine
      return this;
      
      float dif_c  = value.dif_c() + ((GsasCalibAttribute)attr).value.dif_c();
      float dif_a  = value.dif_a() + ((GsasCalibAttribute)attr).value.dif_a();
      float t_zero = value.t_zero() + ((GsasCalibAttribute)attr).value.t_zero();

      return new GsasCalibAttribute(name, new GsasCalib(dif_c, dif_a, t_zero));
      }
      
    
    /**
     * Get a numeric value to be used for sorting based on this
     * attribute.
     */
    public double getNumericValue(){
        return this.value.dif_c();
    }
    
    
    /**
     * Returns a string representation of the value of this attribute
     */
    public String getStringValue(){
        return this.value.toString();
    }
    
    
    /**
     * Returns a string representation of the (name,value) pair for this
     * attribute
     */
    public String toString(){
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
      System.out.println("Warning:GsasCalibAttribute IsawSerialVersion != 1");
  }

}
