/*
 * File:  AttributeList.java
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
 *  Revision 1.23  2004/05/25 20:15:51  kramer
 *  Added methods that get attribute values.
 *
 *  Revision 1.22  2004/05/10 22:46:08  dennis
 *  Removed unused import: java.lang.reflect.
 *
 *  Revision 1.21  2004/04/26 13:00:33  rmikk
 *  Incorporated support for PixelInfoListAttribute's grids.
 *
 *  Revision 1.20  2004/03/19 17:22:05  dennis
 *  Removed unused variable(s)
 *
 *  Revision 1.19  2004/03/15 03:28:04  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.18  2003/10/15 23:50:12  dennis
 *  Fixed javadocs to build cleanly with jdk 1.4.2
 *
 *  Revision 1.17  2003/03/03 16:49:16  pfpeterson
 *  Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 *  Revision 1.16  2003/02/14 20:41:24  dennis
 *  Added method trimToSize().
 *
 *  Revision 1.15  2002/11/27 23:14:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.14  2002/11/12 22:03:12  dennis
 *  Made clone() method more efficient by creating the Vector of
 *  Attributes to be of the correct size.
 *
 *  Revision 1.13  2002/11/12 02:10:32  dennis
 *  No longer clones attributes.  Since attributes are immutable, just use
 *  the same reference to the attribute.
 *
 *  Revision 1.12  2002/08/01 22:33:35  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.11  2002/06/17 22:43:49  rmikk
 *  Made a code segment more debuggable
 *
 *  Revision 1.10  2002/06/14 20:48:37  rmikk
 *  Implements the IXmlIO interface
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.Util.File.*;
import java.util.*;
import java.io.*;

import DataSetTools.gsastools.GsasCalib;
import DataSetTools.instruments.SampleOrientation;

/**
 * Class that maintains a list of attributes. 
 *
 * @see DataSetTools.dataset.Attribute
 * @see DataSetTools.dataset.DetPosAttribute
 * @see DataSetTools.dataset.DoubleAttribute
 * @see DataSetTools.dataset.FloatAttribute
 * @see DataSetTools.dataset.IntAttribute
 * @see DataSetTools.dataset.IntListAttribute
 * @see DataSetTools.dataset.StringAttribute
 *
 * @version 1.0  
 */

public class AttributeList implements Serializable,
                                      IXmlIO 
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
  private Vector attributes;


  /**
   * Constructs an initial empty list of Attributes.
   */
  public AttributeList( )
  {
    this.attributes = new Vector();
  }

  /**
   * Adds a new attribute to the list of attributes, if that attribute is
   * not already present in the list.  If the attribute is already present
   * in the list, this method returns false.
   *
   *  @param  attribute    The new attribute to be added to the list of
   *                       attributes.
   *
   *  @return              Returns true if the attribute was added to the list.
   *                       Returns false if the attribute was already in the
   *                       list and the new attribute was not added to the list.
   */
  public boolean addAttribute( Attribute attribute )
  {
    if ( getAttribute( attribute.getName() ) == null )
    {
      attributes.addElement( attribute );
      return true;
    }
   else
      return false;
  }

  /**
   * Adds a new attribute to the list of attributes at at the specified 
   * position.  If the attribute is already present in the list, the attribute
   * is not added and this method returns false.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   *
   *  @param  index        The position where the attribute is to be
   *                       inserted.
   *
   *  @return              Returns true if the attribute was added to the list.
   *                       Returns false if the attribute was already in the
   *                       list and the new attribute was not added to the list.
   */
  public boolean addAttribute( Attribute attribute, int index )
  {
    if ( index >= 0                   && 
         index <= attributes.size()   &&
         getAttribute( attribute.getName() ) == null )
    {
      attributes.insertElementAt( attribute, index );
      return true;
    }
    else
      return false;
  }


  /**
   * Set the value of the specified attribute in the list of attributes.
   * If the attribute is already present in the list, the value is changed 
   * to the value of the new attribute.  If the attribute is not already 
   * present in the list, the new attribute is added to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   */
  public void setAttribute( Attribute attribute )
  {
    boolean attr_set = false;
    String  new_name = attribute.getName();
    String  attr_name;

    for ( int i = 0; i < attributes.size(); i++ )
    {
      attr_name = ((Attribute)attributes.elementAt( i )).getName();

      if ( new_name.equalsIgnoreCase( attr_name) )
      {
        attributes.setElementAt( attribute, i );
        attr_set = true;
      }
    }

    if ( !attr_set )
      addAttribute( attribute );
  }

  /**
   * Set the value of the specified attribute in the list of attributes at 
   * at the specified position.  If the attribute is already present in the 
   * list, the value is changed to the value of the new attribute.  If the 
   * attribute is not already present in the list, the new attribute is added 
   * to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   *
   *  @param  index        The position where the attribute is to be 
   *                       inserted.
   */
  public void setAttribute( Attribute attribute, int index )
  {
    removeAttribute( attribute.getName() );
    addAttribute( attribute, index );
  }


  /**
   * Copy the attributes from the specified AttributeList into the current
   * AttributeList.  If the same named attribute is already present in the 
   * list, the attribute is not added.
   */
  public void addAttributes( AttributeList new_list )
  {
    Attribute attribute;
    for ( int i = 0; i < new_list.getNum_attributes(); i++ )
    {
      attribute = new_list.getAttribute(i);
      addAttribute( attribute );
    }
  }

  /**
   * Set the attributes from the specified AttributeList into the current
   * AttributeList.  If the same named attribute is already present in the
   * list, the attribute is changed to the same value.
   */
  public void setAttributes( AttributeList new_list )
  {
     Attribute attribute;
    for ( int i = 0; i < new_list.getNum_attributes(); i++ )
    {
      attribute = new_list.getAttribute(i);
      setAttribute( attribute );
    }
  }


  /**
   * Gets the number of attributes in this AttributeList object
   */
  public int getNum_attributes()
  {
    return( attributes.size() );
  }


  /**
   * Get the attribute at the specified index from the list of attributes.
   * attributes. If the index is invalid, this returns null.
   *
   *  @param  index    The index in the list of attributes of the attribute
   *                   that is to be returned. 
   */
  public Attribute getAttribute( int index )
  {
    if ( index >= 0 && index < getNum_attributes() )
      return (Attribute)attributes.elementAt( index );
    else
      return null;
  }

  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index The index of the attribute whose value is to be returned.
   */
  public Object  getAttributeValue( int index )
  {
    Attribute attribute;

    attribute = getAttribute( index );

    if ( attribute != null )
      return attribute.getValue();
    else                                          // if not found, return null
      return null;
  }


  /**
   * Get the attribute with the specified "name" from the list of attributes.
   * If the specified attribute is not in the list of attributes, null is 
   * returned.
   *
   *  @param  name    The "name" field of the attribute that is to be returned. 
   */
  public Attribute getAttribute( String name )
  {
    String attr_name;

    for ( int i = 0; i < attributes.size(); i++ )
    {
      attr_name = ((Attribute)attributes.elementAt( i )).getName();

      if ( name.equalsIgnoreCase( attr_name) )
        return (Attribute)attributes.elementAt( i );
    }

    return null;   // didn't find it
  }

  /**
   * Get the value of the attribute with the specified name from this list of
   * attributes.  If the named attribute is not in the list, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object getAttributeValue( String name )
  {
    Attribute attribute;
    

    attribute = getAttribute( name );
    
    if ( attribute != null )
      return attribute.getValue();
    else                                          // if not found, return null
      return null;
  }

  /**
   * Removes the attribute at the specified index from this list of attributes.
   *
   *  @param  index    The index in the list of attributes of the attribute
   *                   that is to be removed. 
   */
  public void removeAttribute( int index )
  {
    if ( index >= 0 && index < getNum_attributes() )
      attributes.removeElementAt( index );
  }

  /**
   * Removes any attribute with the specified name from this list of attributes.
   *
   *  @param  name    The "name" field of the attribute that is to be removed. 
   */
  public void removeAttribute( String name )
  {
    String attr_name;

    for ( int i = 0; i < attributes.size(); i++ )
    {
      attr_name = ((Attribute)attributes.elementAt( i )).getName();

      if ( name.equalsIgnoreCase( attr_name) )
        attributes.removeElementAt( i );
    }
  }


  /**
   *  Trim the Vector storing the attribute list to be of just the right size
   *  to hold the current attributes.  This can be invoked after adding and/or
   *  removing attributes, to guarantee that the vector of attributes is
   *  no longer than needed. 
   */
   public void trimToSize()
   {
     attributes.trimToSize();
   }


  /**
   * Combine the attributes from another AttributeList with the current
   * attribute list.  Any attribute that is not in both lists will be deleted
   * from the current list.  If an attribute is present in both lists, the
   * combine() method is typically used to obtain the new value for the
   * attribute in the new list.   If an attribute is one of the specific 
   * named attributes defined in class Attribute, it may be treated in a 
   * special way determined by what it represents.  Other attributes will 
   * be combined using a combine operation for the appropriate class.
   *
   *  @param  attr_list  The attribute list that is to be combined with the
   *                      current attribute list.
   */
   public void combine( AttributeList attr_list )
   {
      Attribute     this_attr,
                    other_attr;
      String        attr_name;  
      AttributeList new_list = new AttributeList();

      for ( int i = 0; i < getNum_attributes(); i++ )
      {
        this_attr  = getAttribute( i );
        attr_name  = this_attr.getName();
        other_attr = attr_list.getAttribute( attr_name );

        if ( other_attr == null )             // there is no matching attribute
          new_list.addAttribute( this_attr ); // just use the current attribute

        else                              // combine them and append the result 
        {  
          if ( attr_name.equals( Attribute.RUN_TITLE ))
            new_list.addAttribute( this_attr ); 
              // keep the run title from the first attribute list, it would
              // be too long if concatenated

          else if ( attr_name.equals( Attribute.END_TIME ))
            ; // omit the End Time, it doesn't make sense to average it

          else if ( attr_name.equals( Attribute.END_DATE ))
            ; // omit the End Date, it doesn't make sense to average it

          else if ( attr_name.equals( Attribute.GROUP_ID ))
            new_list.addAttribute( this_attr );
              // keep the Group ID from the first attribute list

          else
          { 
           this_attr.combine( other_attr );     // alter this_attr by combining
           new_list.addAttribute( this_attr ); 
          }
        }
      }
   
      this.attributes = new_list.attributes;
   }

  /**
   * Add the attribute values with the specified name, from two attribute 
   * lists and set the resulting sum as the attribute value in the current 
   * attribute list.
   * This method is intended to be called after combining attribute lists to
   * implement special behavior for those attributes where the default 
   * "combine" behavior is not correct.
   *
   *  @param  attr_name    The name of the attributes that are to be added.
   *  @param  attr_list_1  The first attribute list from which a value for
   *                       the named attribute is obtained.
   *  @param  attr_list_2  The second attribute list from which a value for
   *                       the named attribute is obtained.
   */
   public void add( String        attr_name,
                    AttributeList attr_list_1,
                    AttributeList attr_list_2 )
   {
      Attribute     attr_1,
                    attr_2;
                                         // get references to attributes from
                                         // the lists
      attr_1 = attr_list_1.getAttribute( attr_name );
      attr_2 = attr_list_2.getAttribute( attr_name );

      if ( attr_1 != null  && attr_2 != null ) 
        setAttribute( attr_1.add( attr_2 ) ); 
                                         // the attributes are in the lists
                                         // so put the new Attribute that
                                         // results from adding them, in
                                         // the list
   }

  /**
  * Implements the IXmlIO interface to let an AttributeList read itself
  *
  * @param  stream  the InputStream from which the data comes
  * 
  * @return  true if successful otherwise false
  *
  * Note: This routine assumes that the <AttributeList> tag has been read.
  * Note: This routine reads the </AttributeList> tag
  */
  public boolean XMLread( InputStream stream )
  {
    try
    {   
      String Tag= xml_utils.getTag( stream);
        
      boolean done= Tag==null;
      if(Tag == null)
        { return xml_utils.setError( xml_utils.getErrorMessage() );
            
        }
         
      if( Tag != null)
        done= Tag.equals("/AttributeList");
         
      while( !done)
      { try
        {
          Class AT = Class.forName( "DataSetTools.dataset."+Tag);
          Attribute A = (Attribute)(AT.newInstance());
          if( A instanceof PixelInfoListAttribute)
             ((PixelInfoListAttribute)A).setGridIds(gridIDs);
          boolean OK= A.XMLread( stream );
          if(!OK )
            { return xml_utils.setError("ximproper read for "+Tag+","+
                   A.getClass());
            }
          setAttribute( A);
               
        }
        catch( Exception s)
        { xml_utils.setError("No class DataSetTools.dataset."+Tag 
                   +" err="+s.getClass()+s.getMessage());
                
          xml_utils.skipBlock(stream);
        }
            
        Tag= xml_utils.getTag( stream);
        done= Tag==null;
        if(Tag == null)
        { return xml_utils.setError(xml_utils.getErrorMessage());
              
        }
            
        if( Tag != null)
          done= Tag.equals("/AttributeList");
      }
         
      if(!xml_utils.skipAttributes( stream))
        return xml_utils.setError( xml_utils.getErrorMessage()); 

      return true;
    }
    catch(Exception s)
    { DataSetTools.util.SharedData.addmsg("Exception="+s.getMessage());
       
      return false;
    }
     
  }
  Hashtable gridIDs = null;
  public void setGridIDs( Hashtable gridIDs){
  	this.gridIDs = gridIDs;
  }

  /**
  * Implements the IXmlIO interface to let an AttributeList write itself
  *
  * @param  stream  the OutputStream from which the data comes
  * 
  * @return  true if successful otherwise false
  *
  * NOTE: This routine writes the <AttributeList> and </AttributeList> tags
  */
  public boolean XMLwrite( OutputStream stream, int mode )
  { try
    {
      stream.write(("<AttributeList size= \""+attributes.size()+
                    "\">\n").getBytes());
      for(int i=0 ;i<attributes.size(); i++)
      { Attribute A =(Attribute)(attributes.elementAt(i));
         
        if(!A.XMLwrite(stream,mode))
          return false;
              
        stream.write("\n".getBytes());
      }
      stream.write("</AttributeList>\n".getBytes());
      
    }
    catch(Exception s)
    { 
      return xml_utils.setError("Exception="+s.getClass()+","+
                   s.getMessage());
    }
     
    return true;
  }

  /**
   * Return a new AttributeList containing references to the same Attributes
   * as the current list.
   */
  public Object clone()
  {
    AttributeList  temp = new AttributeList( );

    int num_attributes = attributes.size();
    temp.attributes = new Vector( num_attributes );

    for ( int i = 0; i < num_attributes; i++ )
      temp.attributes.add( attributes.elementAt(i) );

    return temp;
  }
  
  public String toString()
  {
    String buffer = "Attribute list: \n";
    
    for(int i = 0; i<attributes.size(); i++)
      buffer = buffer + attributes.elementAt(i).toString()+ "\n";

    return buffer;
  }
  
  /*----------------------------------------------------------------------
   * These methods are used to obtain attributes
   */
   public String getAttributeTitle()
   {
	 return resolveStringAttribute(Attribute.TITLE);
   }

   public String getAttributeLabel()
   {
	 return resolveStringAttribute(Attribute.LABEL);
   }

   public int getDS_TAG()
   {
	 return resolveIntAttribute(Attribute.DS_TAG);
   }

   public String getUser()
   {
	 return resolveStringAttribute(Attribute.USER);
   }

   public String getInstrumentName()
   {
	 return resolveStringAttribute(Attribute.INST_NAME);
   }

   public int getInstrumentType()
   {
	 return resolveIntAttribute(Attribute.INST_TYPE);
   }

   public String getFileName()
   {
	 return resolveStringAttribute(Attribute.FILE_NAME);
   }

   public String getRunTitle()
   {
	 return resolveStringAttribute(Attribute.RUN_TITLE);
   }

   public int[] getRunNumber()
   {
	 return resolveIntArrayAttribute(Attribute.RUN_NUM);
   }

   public String getEndDate()
   {
	 return resolveStringAttribute(Attribute.END_DATE);
   }

   public String getEndTime()
   {
	 return resolveStringAttribute(Attribute.END_TIME);
   }

   public String getStartDate()
   {
	 return resolveStringAttribute(Attribute.START_DATE);
   }

   public String getStartTime()
   {
	 return resolveStringAttribute(Attribute.START_TIME);
   }

   public String getUpdateTime()
   {
	 return resolveStringAttribute(Attribute.UPDATE_TIME);
   }

   public DetectorPosition getDetectorPosition()
   {
	 return resolveDetectorPositionAttribute(Attribute.DETECTOR_POS);
   }

   public float getRawAngle()
   {
	 return resolveFloatAttribute(Attribute.RAW_ANGLE);
   }

   public float getRawDistance()
   {
	 return resolveFloatAttribute(Attribute.RAW_DISTANCE);
   }

   public float getSolidAngle()
   {
	 return resolveFloatAttribute(Attribute.SOLID_ANGLE);
   }

   public float getOmega()
   {
	 return resolveFloatAttribute(Attribute.OMEGA);
   }

   public float getDelta2Theta()
   {
	 return resolveFloatAttribute(Attribute.DELTA_2THETA);
   }

   public float getEfficiencyFactor()
   {
	 return resolveFloatAttribute(Attribute.EFFICIENCY_FACTOR);
   }

   public int[] getDetectorIDs()
   {
	 return resolveIntArrayAttribute(Attribute.DETECTOR_IDS);
   }

   public int[] getSegmentIDs()
   {
	 return resolveIntArrayAttribute(Attribute.SEGMENT_IDS);
   }

   public int getGroupID()
   {
	 return resolveIntAttribute(Attribute.GROUP_ID);
   }

   public int getTimeFieldType()
   {
	 return resolveIntAttribute(Attribute.TIME_FIELD_TYPE);
   }

   public int[] getCrate()
   {
	 return resolveIntArrayAttribute(Attribute.CRATE);
   }

   public int[] getSlot()
   {
	 return resolveIntArrayAttribute(Attribute.SLOT);
   }

   public int[] getInput()
   {
	 return resolveIntArrayAttribute(Attribute.INPUT);
   }

   public float getDetectorCENDistance()
   {
	 return resolveFloatAttribute(Attribute.DETECTOR_CEN_DISTANCE);
   }

   public float getDetectorCENAngle()
   {
	 return resolveFloatAttribute(Attribute.DETECTOR_CEN_ANGLE);
   }

   public float getDetectorCENHeight()
   {
	 return resolveFloatAttribute(Attribute.DETECTOR_CEN_HEIGHT);
   }

   public IDataGrid getDetectorDataGrid()
   {
	 return resolveDataGridAttribute(Attribute.DETECTOR_DATA_GRID);
   }

   public float getInitialPath()
   {
	 return resolveFloatAttribute(Attribute.INITIAL_PATH);
   }

   public float getEnergyIn()
   {
	 return resolveFloatAttribute(Attribute.ENERGY_IN);
   }

   public float getNominalEnergyIn()
   {
	 return resolveFloatAttribute(Attribute.NOMINAL_ENERGY_IN);
   }

   public float getEnergyOut()
   {
	 return resolveFloatAttribute(Attribute.ENERGY_OUT);
   }

   public float getNominalSourceToSampleTOF()
   {
	 return resolveFloatAttribute(Attribute.NOMINAL_SOURCE_TO_SAMPLE_TOF);
   }

   public float getSourceToSampleTOF()
   {
	 return resolveFloatAttribute(Attribute.SOURCE_TO_SAMPLE_TOF);
   }

   public float getT0Shift()
   {
	 return resolveFloatAttribute(Attribute.T0_SHIFT);
   }

   public float getSampleChi()
   {
	 return resolveFloatAttribute(Attribute.SAMPLE_CHI);
   }

   public float getSamplePhi()
   {
	 return resolveFloatAttribute(Attribute.SAMPLE_PHI);
   }

   public float getSampleOmega()
   {
	 return resolveFloatAttribute(Attribute.SAMPLE_OMEGA);
   }

   public SampleOrientation getSampleOrientation()
   {
	 return resolveSampleOrientationAttribute(Attribute.SAMPLE_ORIENTATION);
   }

   public String getSampleName()
   {
	 return resolveStringAttribute(Attribute.SAMPLE_NAME);
   }

   public float getTemperature()
   {
	 return resolveFloatAttribute(Attribute.TEMPERATURE);
   }

   public float getPressure()
   {
	 return resolveFloatAttribute(Attribute.PRESSURE);
   }

   public float[] getMagneticField()
   {
	 return resolveFloatArrayAttribute(Attribute.MAGNETIC_FIELD);
   }

   public int getNumberOfPulses()
   {
	 return resolveIntAttribute(Attribute.NUMBER_OF_PULSES);
   }

   public float getTotalCount()
   {
	 return resolveFloatAttribute(Attribute.TOTAL_COUNT);
   }

   public float getQValue()
   {
	 return resolveFloatAttribute(Attribute.Q_VALUE);
   }

   public GsasCalib getGSASCalib()
   {
	 return resolveGsasCalibAttribute(Attribute.GSAS_CALIB);
   }

   public String getGSASIParm()
   {
	 return resolveStringAttribute(Attribute.GSAS_IPARM);
   }

   public float[] getLatticeParam()
   {
	 return resolveFloatArrayAttribute(Attribute.LATTICE_PARAM);
   }

   public String getOrientMatrix()
   {
	 return resolveStringAttribute(Attribute.ORIENT_MATRIX);
   }

   public String getOrientFile()
   {
	 return resolveStringAttribute(Attribute.ORIENT_FILE);
   }

   public float getCellVolume()
   {
	 return resolveFloatAttribute(Attribute.CELL_VOLUME);
   }

   public String getSCDCalid()
   {
	 return resolveStringAttribute(Attribute.SCD_CALIB);
   }

   public String getSCDCalibFile()
   {
	 return resolveStringAttribute(Attribute.SCD_CALIB_FILE);
   }

   public PixelInfoList getPixelInfoList()
   {
	 return resolvePixelInfoListAttribute(Attribute.PIXEL_INFO_LIST);
   }

   public String getDSType()
   {
	 return resolveStringAttribute(Attribute.DS_TYPE);
   }

   public float getTimeOffset()
   {
	 return resolveFloatAttribute(Attribute.TIME_OFFSET);
   }

   public int getStartTimeSec()
   {
	 return resolveIntAttribute(Attribute.START_TIME_SEC);
   }

   public String getTimeOfDay()
   {
	 return resolveStringAttribute(Attribute.TIME_OF_DAY);
   }

   public String getDayOfMonth()
   {
	 return resolveStringAttribute(Attribute.DAY_OF_MONTH);
   }

   public String getUnknown()
   {
	 return resolveStringAttribute(Attribute.UNKNOWN);
   }

   public String getInvalidDataSet()
   {
	 return resolveStringAttribute(Attribute.INVALID_DATA_SET);
   }

   public String getMonitorData()
   {
	 return resolveStringAttribute(Attribute.MONITOR_DATA);
   }

   public String getSampleData()
   {
	 return resolveStringAttribute(Attribute.SAMPLE_DATA);
   }

   public String getPulseHeightData()
   {
	 return resolveStringAttribute(Attribute.PULSE_HEIGHT_DATA);
   }

   public String getTemperatureData()
   {
	 return resolveStringAttribute(Attribute.TEMPERATURE_DATA);
   }

   public String getPressureData()
   {
	 return resolveStringAttribute(Attribute.PRESSURE_DATA);
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
      System.out.println("Warning:AttributeList IsawSerialVersion != 1");
  }
  
  /*--------------------------------------------------------------------------------------
   * These are helper methods for obtaining attributes.  These methods
   * do the actual work.
   */
   private String resolveStringAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof String))
	   return (String)val;
	 else
	   return null;
   }

   private float resolveFloatAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof Float))
	   return ((Float)val).floatValue();
	 else
	   return Float.NaN;
   }

   private float[] resolveFloatArrayAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof float[]))
	   return (float[])val;
	 else
	   return null;
   }

   private int resolveIntAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof Integer))
	   return ((Integer)val).intValue();
	 else
	   return -1;
   }

   private int[] resolveIntArrayAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof int[]))
	   return (int[])val;
	 else
	   return null;
   }

   private DetectorPosition resolveDetectorPositionAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof DetectorPosition))
	   return (DetectorPosition)val;
	 else
	   return null;
   }

   private IDataGrid resolveDataGridAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof IDataGrid))
	   return (IDataGrid)val;
	 else
	   return null;
   }

   private GsasCalib resolveGsasCalibAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof GsasCalibAttribute))
	   return ((GsasCalibAttribute)val).getGsasCalib();
	 else
	   return null;
   }

   private SampleOrientation resolveSampleOrientationAttribute(String name)
   {
	 Object val = getAttributeValue(name);
	 if ((val != null) && (val instanceof SampleOrientationAttribute))
	   return (SampleOrientation)((SampleOrientationAttribute)val).getValue();
	 else
	   return null;
   }   
   
   private PixelInfoList resolvePixelInfoListAttribute(String name)
   {
	  Object val = getAttributeValue(name);
	  if ((val != null) && (val instanceof PixelInfoListAttribute))
		return (PixelInfoList)((PixelInfoListAttribute)val).getValue();
	  else
		return null;
   }
}
