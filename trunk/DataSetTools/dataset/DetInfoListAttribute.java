/*
 * File:  DetInfoListAttribute.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  Revision 1.9  2002/11/12 00:47:20  dennis
 *  Made immutable by:
 *  1. remove setValue() method
 *  2. add() & combine() methods now return a new Attribute
 *  3. getValue() returns copy of the array value
 *
 *  Also:
 *  4. Since it is now immutable, clone() method is not needed and
 *     was removed
 *  5. Default constructor is now private, since the value can't
 *     be set from outside of the class
 *
 *  Revision 1.8  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.7  2002/07/17 20:38:18  dennis
 *  Clarified javadoc comment.
 *
 *  Revision 1.6  2002/06/18 19:34:38  rmikk
 *  *** empty log message ***
 *
 *  Revision 1.5  2002/06/18 19:31:09  rmikk
 *  Eliminated some debug prints
 *
 *  Revision 1.4  2002/06/14 20:59:24  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.3  2002/06/05 20:35:59  dennis
 *  The toString method now only converts the first 5 DetectorInfo objects
 *  to a string.
 *
 *  Revision 1.2  2001/07/10 19:37:14  dennis
 *  Now imports DataSetTools.instruments.*
 *
 *  Revision 1.1  2001/07/10 18:48:49  dennis
 *  Attribute for infomation on list of individual
 *  detector segment positions, sizes, efficiency, etc.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.util.*;
import DataSetTools.instruments.*;
import DataSetTools.dataset.*;
import java.io.*;
import java.util.*;

/**
 * The concrete class for an attribute whose value is a list of DetectorInfo
 * objects.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 * @see DataSetTools.dataset.DetInfoAttribute
 */

public class DetInfoListAttribute extends Attribute implements  IXmlIO
                                                   
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
  private DetectorInfo[] values;

  /**
   * Constructs a DetInfoListAttribute object using the specified name 
   * and values.
   */
  public DetInfoListAttribute( String name, DetectorInfo value[] )
  {
    super( name );

    if ( value == null )
    {
      values = null;
      return;
    }

    int length  = value.length;
    values = new DetectorInfo[ length ];

    for ( int i = 0; i < values.length; i++ )
      values[i] = new DetectorInfo( value[i] );

    boolean in_order = true;
    for ( int i = 0; i < values.length - 1; i++ )
      if ( values[i].getSeg_num() > values[i+1].getSeg_num() )
        in_order = false;

    if ( !in_order )
    {
      System.out.println("Sorting DetectorInfoListAttribute");
      Arrays.sort( values, new DetectorInfoComparator() );
    }
  }


  private DetInfoListAttribute()
  {
    super("");
    values = new DetectorInfo[0];
 
  }

  /**
   * Returns reference to the array of DetectorInfo objects for this attribute,
   * as a generic object. 
   */
  public Object getValue( )
  {
    return( values );
  } 

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is the result of merging the two lists of DetectorInfo objects.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   */
  public Attribute combine( Attribute attr )
  {
    if ( !(attr instanceof DetInfoListAttribute) )      // can't do it so don't 
      return this;                                          
       
    DetInfoListAttribute other_attr = (DetInfoListAttribute)attr; 

//    if ( values.length+other_attr.values.length > 20 )// ineffiecient for
//      return;                                         // long lists, just keep
                                                        // up to 20.

    DetectorInfo temp[] = new DetectorInfo[
                                       values.length+other_attr.values.length ];
    int i        = 0,
        j        = 0,
        num_used = 0;
                                               // merge the lists, only keeping
                                               // keeping distinct segment ids
    int last_seg = -1;
    int seg_i;
    int seg_j;
    while ( i < values.length && j < other_attr.values.length )    
    {
      seg_i = values[ i ].getSeg_num();
      seg_j = other_attr.values[ j ].getSeg_num();
      if ( seg_i < seg_j )
      {
        if ( seg_i != last_seg )
        {
          temp[ num_used ] = values[i];
          last_seg = seg_i;
          num_used++;
        }
        i++;
      }
      else if ( seg_i > seg_j )
      {
        if ( seg_j != last_seg )
        {
          temp[ num_used ] = other_attr.values[ j ];
          last_seg = seg_j;
          num_used++;
        }
        j++;
      }
      else     // seg_i == seg_j, so use seg_i if it is not already in the list
      {
        if ( seg_i != last_seg )
        {
          temp[ num_used ] = values[i];
          last_seg = seg_i;
          num_used++;
        }
        i++;
        j++;
      }
    }

    if ( i < values.length )
    {
      System.arraycopy( values, i, temp, num_used, values.length - i );
      num_used += values.length - i;
    }
 
    else
    {
      System.arraycopy( other_attr.values, j, temp, num_used, 
                        other_attr.values.length - j );
      num_used += other_attr.values.length - j;
    }
                                                  // now copy the values into
                                                  // a new array for this
                                                  // attribute
    DetectorInfo new_values[] = new DetectorInfo[ num_used ];
    System.arraycopy( temp, 0, new_values, 0, num_used );
    
    return new DetInfoListAttribute( name, new_values );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {                                   // use first entry as "numeric value"
     if ( values.length > 0 )          // if there is one.
       return values[0].getSeg_num();
     else
       return Double.MAX_VALUE;
   }

  /**
   * Returns a string representation of the list of DetectorInfo objects 
   */
  public String getStringValue()
  {
    String s = new String();
    if ( values.length < 5 )
      for ( int i = 0; i < values.length; i++ )
        s += values[i].toString();
    else
    {
      for ( int i = 0; i < 5; i++ )
        s += values[i].toString();
      s += "...";
    }
    return s;
  }

 /**
  * Implements the IXmlIO interface.  This routine "writes" the
  * DetectorInfoListAttribute. In standalone mode it writes the xml header.
  *
  * @param  stream  the OutputStream to which the xml data is to be written
  * @param  mode    Either IXmlIO.Base64 to write spectra information 
  *                 efficiently or IXmlIO.Normal to produce ASCII values
  *
  * @returns  true if successful otherwise false
  */
  public boolean XMLwrite( OutputStream stream, int mode )
  {try
     {StringBuffer SS = new StringBuffer(1000);
     if( values == null)
       return true;
     if( values.length <1 )
       return true;
     SS.append( "<DetInfoListAttribute>\n");
     SS.append( "<name>");
     SS.append(name);
     SS.append("</name>\n");
     SS.append( "<value>"+"\n");
     SS.append( "<DetectorInfoList size =\"");
      SS.append(""+values.length+"\">\n");
     stream.write(SS.substring(0).getBytes());
     for( int i=0; i<values.length ; i++)
        values[i].XMLwrite( stream, mode);
     stream.write("</DetectorInfoList>\n".getBytes());
     stream.write( ("</value>"+"\n").getBytes());
     stream.write("</DetInfoListAttribute>\n".getBytes());
     return true;
     }
    catch( Exception s)
     {return xml_utils.setError("Exception="+s.getMessage());
      }
  }
 
 
 /**
  * Implements the IXmlIO interface.  This routine "reads" the
  * DetInfoListAttribute. In standalone mode it writes the xml header.
  *
  * @param  stream  the OutputStream to which the xml data is to be written
  * @param  mode    Either IXmlIO.Base64 to write spectra information 
  *                 efficiently or IXmlIO.Normal to produce ASCII values
  *
  * @returns  true if successful otherwise false
  *
  */
  public boolean XMLread( InputStream stream )
  {
   Vector V = new Vector();
   try
     {  
        String Tag = xml_utils.getTag( stream );
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        if(!Tag.equals("name"))
           return xml_utils.setError(" No Name tag in DetInfoList");
        name = xml_utils.getValue( stream );
        if( name == null)
          return xml_utils.setError(xml_utils.getErrorMessage()); 
         
        Tag = xml_utils.getEndTag( stream);
         if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        if(!Tag.equals("/name"))
           return xml_utils.setError(" No /Name tag in DetInfoList");

        Tag = xml_utils.getTag( stream );
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        if(!Tag.equals("value"))
           return xml_utils.setError(" No Name tag in DetInfoList");
         
//---------------------------------------
        Tag = xml_utils.getTag( stream );
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if(!Tag.equals("DetectorInfoList"))
           return xml_utils.setError(
                             " Need DetectorInfoList tag in DetInfoList");
        V= xml_utils.getNextAttribute( stream);
        if(V== null)
          return xml_utils.setError( xml_utils.getErrorMessage());
        if( V.size() < 1) 
           return xml_utils.setError( "Array tag needs a class attribute");
       
        int size= -1;
       
        if( V.firstElement().equals("size"))
           try{
             size = (new Integer( (String)(V.lastElement()))).intValue();
              }
           catch( Exception ss)
              {return xml_utils.setError("size attribute of Array not an int");
              }
        else
           {V= xml_utils.getNextAttribute( stream);
            if(V== null)
              return xml_utils.setError( xml_utils.getErrorMessage());
            if( V.size() < 1) 
              return xml_utils.setError( "Array tag needs a class attribute");
            V = (Vector)(V.elementAt(0));
            try{
              size = (new Integer( (String)(V.lastElement()))).intValue();
              }
             catch( Exception ss)
              {return xml_utils.setError("size attribute of Array not an int");
              }
            }
         
         if(size <=0)
           return xml_utils.setError( "size attribute in Array must be set");
          
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        
//--------------------------------------
        values= new DetectorInfo[ size ];
       
        for( int i = 0; i< size; i++)
         { 
           Tag = xml_utils.getTag( stream );
           if( Tag == null)
             return xml_utils.setError(xml_utils.getErrorMessage());
           String cl_name ="";
           if(!Tag.equals("DetectorInfo"))
             return xml_utils.setError( "Tag must be Struct in Det Info");
           
           if( !xml_utils.skipAttributes( stream ))
              return xml_utils.setError(xml_utils.getErrorMessage());
           values[i] = new DetectorInfo();
           if(!values[i].XMLread(stream))
              {
               return false;
              }
          }
         
        Tag = xml_utils.getTag( stream);
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !Tag.equals("/DetectorInfoList"))
          return xml_utils.setError("Improper end Tag in DetInfoList");
        
        Tag = xml_utils.getTag( stream);
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !Tag.equals("/value"))
          return xml_utils.setError(
                           "Improper DetInfoList Attr end Tag in DetInfoList");
         
        Tag = xml_utils.getTag( stream );
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError(xml_utils.getErrorMessage());
        if( !Tag.equals("/DetInfoListAttribute"))
          return xml_utils.setError("Improper value end Tag in DetInfoList"+Tag+
            "/DetInfoListAttribute");
         
        return true;       
     }
    catch( Exception s)
     {return xml_utils.setError("Exception="+s.getMessage());
     }
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
      System.out.println("Warning:DetInfoListAttribute IsawSerialVersion != 1");
  }

}
