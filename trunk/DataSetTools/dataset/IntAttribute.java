/*
 * File:  IntAttribute.java
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
 *  Revision 1.7  2002/06/14 21:12:31  rmikk
 *  Implements IXmlIO interface
 *
 *  Revision 1.6  2002/06/05 13:49:58  dennis
 *  Interface IXmlIO partially implemented.
 *
 *  Revision 1.5  2001/04/25 19:03:54  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.4  2000/07/26 14:52:25  dennis
 *  Now includes method to add() attributes.
 *
 *  Revision 1.3  2000/07/10 22:24:01  dennis
 *  Now Using CVS 
 *
 *  Revision 1.4  2000/05/12 15:50:13  dennis
 *  removed DOS TEXT  ^M
 *
 *  Revision 1.3  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 */

package  DataSetTools.dataset;
import   java.io.*;

/**
 * The concrete class for an attribute whose value is an int.  
 *
 * @see DataSetTools.dataset.Data
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.StringAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.DetPosAttribute
 *
 * @version 1.0  
 */

public class IntAttribute extends    Attribute
                          implements IXmlIO 
{
  private int value;

  /**

   *  Write the state of this object out to the specified stream in XML format.
   *
   *  @param  stream   The stream to write to.
   *  @param  mode     Flag indicating whether or not to write the value in
   *                   base 64 encoding.
   *
   *  @return true if the write was successful, false otherwise.
   */
  public boolean XMLwrite( OutputStream stream, int mode )
    {return xml_utils.AttribXMLwrite( stream, mode, this);

     }
  public boolean XMLread( InputStream stream )
    {return xml_utils.AttribXMLread(stream, this);
    }
  public boolean XMLwrite1( OutputStream stream, int mode )
  {
    try
    { 
      String output = "<IntAttribute>\n";
      output += "<name>"  + name  +"</name>\n";
      output += "<value>" + value +"</value>\n";
      output += "</IntAttribute>\n";
      stream.write( output.getBytes() );
    }
    catch ( IOException e )
    {
      return false; 
    } 
    return true;
  }


  /**
   *  Read the state of this object from the specified stream in XML format.
   *
   *  @param  stream   The stream to read from.
   *
   *  @return true if the read was successful, false otherwise.
   */
 
  public boolean XMLread1( InputStream stream )
  { boolean done=false;
    String fd="";
    String key=  xml_utils.getTag( stream );
    if( key.trim().equals("/IntAttribute"))
      {xml_utils.skipAttributes( stream );
       done = true;
       if(fd.equals("nv"))
         return true;
       else
          return false;
       }
    while(!done)
     {
      if( !xml_utils.skipAttributes( stream ) )
       {DataSetTools.util.SharedData.status_pane.add(
        xml_utils.getErrorMessage());
        return false;
        }
      String v= xml_utils.getValue( stream);
      if( v== null)
        {DataSetTools.util.SharedData.status_pane.add(
                xml_utils.getErrorMessage());
         return false;
        }
      if( key.equals( "name"))
       {if( fd.indexOf('n')>=0)
          return false;
        name=v;
        fd="n"+fd;
        }
      else if( key.equals("value"))
       try{if(fd.indexOf('v')>=0)
             return false;
          fd=fd+"v";
          value = (new Integer( v)).intValue();
          }
       catch( Exception s)
          { DataSetTools.util.SharedData.status_pane.add(
              "Error"+s.getMessage()); 
             return false;
           
           }
       key=  xml_utils.getTag( stream );
       if( key == null)
         {DataSetTools.util.SharedData.status_pane.add(
                    xml_utils.getErrorMessage());
          return false;
          }
       if( key.trim().equals("/IntAttribute"))
        {xml_utils.skipAttributes( stream );
         done = true;
         if(fd.equals("nv"))
           return true;
         else
          {DataSetTools.util.SharedData.status_pane.add(
              "Did not Set both field in IntAttribute");
           return false;
          }
       }
      }
      return true;
      
       
  }

  public IntAttribute()
    {super("");
     this.value=0;
    }
  /**
   * Constructs an IntAttribute object using the specified name and value.
   */
  public IntAttribute( String name, int value )
  {
    super( name );
    this.value = value;
  }


  /**
   * Returns the int value of this attribute, as a generic object.
   */
  public Object getValue( )
  {
    return( new Integer(value) );
  } 

  /**
   * Set the value for the int attribute using a generic object.  The actual
   * class of the object must be an Int.
   */
  public boolean setValue( Object obj )
  {
    if ( obj instanceof Double )
      value = ((Double)obj).intValue();
    else if ( obj instanceof Float )
      value = ((Float)obj).intValue();
    else if ( obj instanceof Integer )
      value = ((Integer)obj).intValue();
    else
      return false;

    return true;
  }   

  /**
   * Returns the int value of this attribute as an int.
   */
   public int getIntegerValue( )
   {
     return value;
   }

  /**
   * Set the value for the int attribute using an int.
   */
  public void setIntValue( int value )
  {
    this.value = value;
  }

  /**
   * Combine the value of this attribute with the value of the attribute
   * passed as a parameter to obtain a new value for this attribute.  The
   * new value is just the average of the values of the two attributes.
   *
   *  @param   attr   An attribute whose value is to be "combined" with the 
   *                  value of the this attribute.
   *
   */
  public void combine( Attribute attr )
  {
     this.value = (int)( this.value + attr.getNumericValue() ) / 2;
  }

  /**
   * Add the value of the specified attribute to the value of this
   * attribute obtain a new value for this attribute.  
   *
   *  @param   attr   An attribute whose value is to be "added" to the
   *                  value of the this attribute.
   *
   */
  public void add( Attribute attr )
  {
     this.value = (int)( this.value + attr.getNumericValue() );
  }


  /**
   * Get a numeric value to be used for sorting based on this attribute.
   */
   public double getNumericValue()
   {
     return value;
   }

  /**
   * Returns a string representation of the int value of this attribute
   */
  public String getStringValue()
  {
     return Integer.toString(value);
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
    return new IntAttribute( this.getName(), value );
  }
}
