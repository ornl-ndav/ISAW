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
  private DetectorInfo[] values;

  /**
   * Constructs a DetInfoListAttribute object using the specified name 
   * and values.
   */
  public DetInfoListAttribute( String name, DetectorInfo values[] )
  {
    super( name );
    setValue( values );
  }

  public DetInfoListAttribute()
  {
    super("");
    setValue(new DetectorInfo[0]);
 
  }

  /**
   * Returns reference to the list of DetectorInfo objects for this attribute,
   * as a generic object.
   */
  public Object getValue( )
  {
    return( values );
  } 

  /**
   * Set the value for the int list attribute using a generic object.  The 
   * actual class of the object must be an array of DetectorInfo objects.
   */
  public boolean setValue( Object obj )
  {
    if ( obj == null )
      System.out.println("ERROR: null object in DetInfoListAttribute.setValue");

    if ( obj instanceof DetectorInfo[] )
    {
      DetectorInfo  new_vals[] = (DetectorInfo[])obj;

      int length  = new_vals.length;
      values = new DetectorInfo[ length ];

      for ( int i = 0; i < values.length; i++ )
        values[i] = new DetectorInfo( new_vals[i] );

      boolean in_order = true;
      for ( int i = 0; i < values.length - 1; i++ )
        if ( values[i].getSeg_num() > values[i+1].getSeg_num() )
          in_order = false;

      if ( !in_order )
      {
        System.out.println("Warning: " + 
                           "values not ordered in DetInfoListAttribute");
       // #### need yet another sort method!!!   arrayUtil.sort( this.values );
      }
    }
    else
      return false;
    
    return true;
  }   

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is the result of merging the two lists of DetectorInfo objects.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   */
  public void combine( Attribute attr )
  {
    if ( !(attr instanceof DetInfoListAttribute) )      // can't do it so don't 
      return;                                          
       
    DetInfoListAttribute other_attr = (DetInfoListAttribute)attr; 

//    if ( values.length+other_attr.values.length > 20 )// ineffiecient for
//      return;                                         // long lists, just keep
                                                        // up to 20.

    DetectorInfo temp[] = new DetectorInfo[
                                       values.length+other_attr.values.length ];
    int i        = 0,
        j        = 0,
        num_used = 0;
                                                             // merge the lists 
    while ( i < values.length && j < other_attr.values.length )    
    {
      if ( values[ i ].getSeg_num() < other_attr.values[ j ].getSeg_num() )
      {
        temp[ num_used ] = values[i];
        i++;
      }
      else if ( values[ i ].getSeg_num() > other_attr.values[ j ].getSeg_num() )
      {
        temp[ num_used ] = other_attr.values[ j ];
        j++;
      }
      else
      {
        temp[ num_used ] = values[ i ];
        i++;
        j++;
      }
      num_used++;
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
    values = new DetectorInfo[ num_used ];        // a new array for this
                                                  // attribute
    System.arraycopy( temp, 0, values, 0, num_used );
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
          return xml_utils.setError("A"+xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError("B"+xml_utils.getErrorMessage());
        if(!Tag.equals("name"))
           return xml_utils.setError(" No Name tag in DetInfoList");
        name = xml_utils.getValue( stream );
        if( name == null)
          return xml_utils.setError("C"+xml_utils.getErrorMessage()); 
         
        Tag = xml_utils.getEndTag( stream);
         if( Tag == null)
          return xml_utils.setError("D"+xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError("E"+xml_utils.getErrorMessage());
        if(!Tag.equals("/name"))
           return xml_utils.setError(" No /Name tag in DetInfoList");

        Tag = xml_utils.getTag( stream );
        if( Tag == null)
          return xml_utils.setError("F"+xml_utils.getErrorMessage());
        if( !xml_utils.skipAttributes( stream ))
          return xml_utils.setError("G"+xml_utils.getErrorMessage());
        if(!Tag.equals("value"))
           return xml_utils.setError(" No Name tag in DetInfoList");
         
//---------------------------------------
        Tag = xml_utils.getTag( stream );
        if( Tag == null)
          return xml_utils.setError(xml_utils.getErrorMessage());
        if(!Tag.equals("DetectorInfoList"))
           return xml_utils.setError(" Need DetectorInfoList tag in DetInfoList");
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
          return xml_utils.setError("Improper DetInfoList Attr end Tag in DetInfoList");
         
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

  /**
   * Returns a copy of the current attribute
   */
  public Object clone()
  {
    return new DetInfoListAttribute( this.getName(), values );
  }
}
