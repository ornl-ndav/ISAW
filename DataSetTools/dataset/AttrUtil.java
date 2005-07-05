/*
 * File:  AttrUtil.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2005/07/05 15:31:39  dennis
 *  Fixed bugs in convenience methods for getting PixelInfoList,
 *  GsasCalib and SampleOrientation attribute values.
 *
 *  Revision 1.2  2005/05/27 03:08:08  dennis
 *  Added code to check that both the attribute list and the name
 *  of the attribute are non-null before trying to retrieve the
 *  attribute value.
 *
 *  Revision 1.1  2005/05/26 23:24:52  dennis
 *  This class is a collection of utility methods for getting specific
 *  attributes from DataSets and Data blocks.  The methods are "static"
 *  versions of the methods previously added to AttibuteList, Data and
 *  DataSet by Dominic Kramer.
 *
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;

import DataSetTools.gsastools.GsasCalib;
import DataSetTools.instruments.SampleOrientation;

/**
 * This class contains two kinds of static "convenience" methods for 
 * getting attribute values from an IAttributeList object such as a DataSet 
 * or a Data block.   The first kind consists of methods to get the value
 * of an attribute with a specified name, with different methods for each
 * of the possible returned data types.  The second kind consists of 
 * specific convenience functions for EACH currently supported attribute. 
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

public class AttrUtil 
{
  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a String object 
   * cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getStringValue( String         attr_name,
                                        IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof String))
          return (String)val;
      }

      return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a float value 
   * cannot be aquired from it, <code>Float.NaN</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getFloatValue( String         attr_name,
                                      IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof Float))
          return ((Float)val).floatValue();
      } 

      return Float.NaN;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a float array 
   * cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The float array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static float[] getFloatArrayValue( String         attr_name,
                                             IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof float[]))
          return (float[])val;
      } 

      return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or an integer value 
   * cannot be aquired from it, -1 is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getIntValue( String         attr_name,
                                  IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof Integer))
          return ((Integer)val).intValue();
      }
 
      return -1;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or an integer array 
   * cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getIntArrayValue( String         attr_name,
                                         IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof int[]))
          return (int[])val;
      }
 
      return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a DetectorPosition
   * object cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The DetectorPosition object aquired from the attribute with the
   * specified name or <code>null</code> if it can't be aquired.
   */
   public static DetectorPosition getDetectorPositionValue(
                                             String         attr_name,
                                             IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof DetectorPosition))
          return (DetectorPosition)val;
      }
 
      return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a IDataGrid object 
   * cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The IDataGrid object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static IDataGrid getDataGridValue( String         attr_name,
                                             IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof IDataGrid))
          return (IDataGrid)val;
      }
 
      return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a GsasCalib object 
   * cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The GsasCalib object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static GsasCalib getGsasCalibValue( String         attr_name,
                                              IAttributeList attr_list  )
   {
      if ( attr_list != null && attr_name != null )
      {
        Object val = attr_list.getAttributeValue(attr_name);
        if ((val != null) && (val instanceof GsasCalib))
          return (GsasCalib)val;
      }
 
      return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a SampleOrientation
   * object cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The SampleOrientation object aquired from the attribute with the
   * specified name or <code>null</code> if it can't be aquired.
   */
   public static SampleOrientation getSampleOrientationValue(
                                                     String         attr_name,
                                                     IAttributeList attr_list )
   {
      if ( attr_list != null && attr_name != null )
      {
         Object val = attr_list.getAttributeValue(attr_name);
         if ((val != null) && (val instanceof SampleOrientation))
           return (SampleOrientation)val;
       }

       return null;
   }


  /**
   * Looks for the attribute with the name <code>name</code> from the list 
   * of attributes.  If the attribute is not in the list or a PixelInfoList
   * object cannot be aquired from it, <code>null</code> is returned.
   *
   * @param attr_name The name of the attribute to search for.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the named attribute value is to be obtained.
   *
   * @return The PixelInfoList object aquired from the attribute with the
   * specified name or <code>null</code> if it can't be aquired.
   */
   public static PixelInfoList getPixelInfoListValue( String         attr_name,
                                                      IAttributeList attr_list )
   {
      if ( attr_list != null && attr_name != null )
      {
         Object val = attr_list.getAttributeValue(attr_name);
         if ((val != null) && (val instanceof PixelInfoList))
            return (PixelInfoList)val;
      }     
 
      return null;
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.LABEL</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getLabel( IAttributeList attr_list )
   {
      return getStringValue(Attribute.LABEL, attr_list );
   }

   
  /**
   * Get the value of the attribute specified by <code>Attribute.DS_TAG</code>
   * or -1 if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getDS_TAG( IAttributeList attr_list )
   {
      return getIntValue(Attribute.DS_TAG, attr_list );
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.USER</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getUser( IAttributeList attr_list )
   {
      return getStringValue(Attribute.USER, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.INST_NAME</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getInstrumentName( IAttributeList attr_list )
   {
      return getStringValue(Attribute.INST_NAME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.INST_TYPE</code> or -1 if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getInstrumentType( IAttributeList attr_list )
   {
      return getIntValue(Attribute.INST_TYPE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.FILE_NAME</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getFileName( IAttributeList attr_list )
   {
      return getStringValue(Attribute.FILE_NAME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.RUN_TITLE</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getRunTitle( IAttributeList attr_list )
   {
      return getStringValue(Attribute.RUN_TITLE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.RUN_NUM</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getRunNumber( IAttributeList attr_list )
   {
      return getIntArrayValue(Attribute.RUN_NUM, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.END_DATE</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getEndDate( IAttributeList attr_list )
   {
      return getStringValue(Attribute.END_DATE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.END_TIME</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getEndTime( IAttributeList attr_list )
   {
      return getStringValue(Attribute.END_TIME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.START_DATE</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getStartDate( IAttributeList attr_list )
   {
      return getStringValue(Attribute.START_DATE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.START_TIME</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getStartTime( IAttributeList attr_list )
   {
      return getStringValue(Attribute.START_TIME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.UPDATE_TIME</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getUpdateTime( IAttributeList attr_list )
   {
      return getStringValue(Attribute.UPDATE_TIME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DETECTOR_POS</code> or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The DetectorPosition object aquired from the attribute with the
   * specified name or <code>null</code> if it can't be aquired.
   */
   public static DetectorPosition getDetectorPosition(IAttributeList attr_list)
   {   
      return getDetectorPositionValue( Attribute.DETECTOR_POS, 
                                               attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.RAW_ANGLE</code> or <code>Float.NaN</code>
   * if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getRawAngle( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.RAW_ANGLE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.RAW_DISTANCE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getRawDistance( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.RAW_DISTANCE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SOLID_ANGLE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getSolidAngle( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.SOLID_ANGLE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.OMEGA</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getOmega( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.OMEGA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DELATA_2THETA</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getDelta2Theta( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.DELTA_2THETA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.EFFICIENCY_FACTOR</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getEfficiencyFactor( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.EFFICIENCY_FACTOR, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DETECTOR_IDS</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getDetectorIDs( IAttributeList attr_list )
   {
      return getIntArrayValue(Attribute.DETECTOR_IDS, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SEGMENT_IDS</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getSegmentIDs( IAttributeList attr_list )
   {
      return getIntArrayValue(Attribute.SEGMENT_IDS, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.GROUP_ID</code>
   * or -1 if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getGroupID( IAttributeList attr_list )
   {
      return getIntValue(Attribute.GROUP_ID, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.TIME_FIELD_TYPE</code>
   * or -1 if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getTimeFieldType( IAttributeList attr_list )
   {
      return getIntValue(Attribute.TIME_FIELD_TYPE, attr_list );
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.CRATE</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getCrate( IAttributeList attr_list )
   {
      return getIntArrayValue(Attribute.CRATE, attr_list );
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.SLOT</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getSlot( IAttributeList attr_list )
   {
      return getIntArrayValue(Attribute.SLOT, attr_list );
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.INPUT</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The integer array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static int[] getInput( IAttributeList attr_list )
   {
      return getIntArrayValue(Attribute.INPUT, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DETECTOR_CEN_DISTANCE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getDetectorCENDistance( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.DETECTOR_CEN_DISTANCE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DETECTOR_CEN_ANGLE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getDetectorCENAngle( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.DETECTOR_CEN_ANGLE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DETECTOR_CEN_HEIGHT</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getDetectorCENHeight( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.DETECTOR_CEN_HEIGHT, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DETECTOR_DATA_GRID</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The IDataGrid object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static IDataGrid getDetectorDataGrid( IAttributeList attr_list )
   {
      return getDataGridValue(Attribute.DETECTOR_DATA_GRID, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.INITIAL_PATH</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getInitialPath( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.INITIAL_PATH, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.ENERGY_IN</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getEnergyIn( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.ENERGY_IN, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.NOMINAL_ENERGY_IN</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getNominalEnergyIn( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.NOMINAL_ENERGY_IN, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.ENERGY_OUT</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getEnergyOut( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.ENERGY_OUT, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.NOMINAL_SOURCE_TO_SAMPLE_TOF</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getNominalSourceToSampleTOF( IAttributeList attr_list )
   {
      return getFloatValue( Attribute.NOMINAL_SOURCE_TO_SAMPLE_TOF,
                                    attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SOURCE_TO_SAMPLE_TOF</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getSourceToSampleTOF( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.SOURCE_TO_SAMPLE_TOF, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.T0_SHIFT</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getT0Shift( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.T0_SHIFT, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SAMPLE_CHI</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getSampleChi( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.SAMPLE_CHI, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SAMPLE_PHI</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getSamplePhi( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.SAMPLE_PHI, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SAMPLE_OMEGA</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getSampleOmega( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.SAMPLE_OMEGA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SAMPLE_ORIENTATION</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The SampleOrientation object aquired from the attribute with the
   * specified name or <code>null</code> if it can't be aquired.
   */
   public static SampleOrientation getSampleOrientation( 
                                            IAttributeList attr_list )
   {
      return getSampleOrientationValue( Attribute.SAMPLE_ORIENTATION, 
                                                attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SAMPLE_NAME</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getSampleName( IAttributeList attr_list )
   {
      return getStringValue(Attribute.SAMPLE_NAME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.TEMPERATURE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getTemperature( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.TEMPERATURE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.PRESSURE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getPressure( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.PRESSURE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.MAGNETIC_FIELD</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
    */
   public static float[] getMagneticField( IAttributeList attr_list )
   {
      return getFloatArrayValue(Attribute.MAGNETIC_FIELD, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.NUMBER_OF_PULSES</code>
   * or -1 if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getNumberOfPulses( IAttributeList attr_list )
   {
      return getIntValue(Attribute.NUMBER_OF_PULSES, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.TOTAL_COUNT</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getTotalCount( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.TOTAL_COUNT, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.Q_VALUE</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getQValue( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.Q_VALUE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.GSAS_CALIB</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The GsasCalib object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static GsasCalib getGSASCalib( IAttributeList attr_list )
   {
      return getGsasCalibValue(Attribute.GSAS_CALIB, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.GSAS_IPARM</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getGSASIParm( IAttributeList attr_list )
   {
      return getStringValue(Attribute.GSAS_IPARM, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.LATTICE_PARAM</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float array aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static float[] getLatticeParam( IAttributeList attr_list )
   {
      return getFloatArrayValue(Attribute.LATTICE_PARAM, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.ORIENT_MATRIX</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getOrientMatrix( IAttributeList attr_list )
   {
      return getStringValue(Attribute.ORIENT_MATRIX, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.ORIENT_FILE</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getOrientFile( IAttributeList attr_list )
   {
      return getStringValue(Attribute.ORIENT_FILE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.CELL_VOLUME</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getCellVolume( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.CELL_VOLUME, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SCD_CALIB</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getSCDCalid( IAttributeList attr_list )
   {
      return getStringValue(Attribute.SCD_CALIB, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SCD_CALIB_FILE</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getSCDCalibFile( IAttributeList attr_list )
   {
      return getStringValue(Attribute.SCD_CALIB_FILE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.PIXEL_INFO_LIST</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The PixelInfoList object aquired from the attribute with the
   * specified name or <code>null</code> if it can't be aquired.
   */
   public static PixelInfoList getPixelInfoList( IAttributeList attr_list )
   {
      return getPixelInfoListValue( Attribute.PIXEL_INFO_LIST, 
                                            attr_list );
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.DS_TYPE</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getDSType( IAttributeList attr_list )
   {
      return getStringValue(Attribute.DS_TYPE, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.TIME_OFFSET</code>
   * or <code>Float.NaN</code> if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The float value aquired from the attribute with the specified 
   * name or <code>Float.NaN</code> if it can't be aquired.
   */
   public static float getTimeOffset( IAttributeList attr_list )
   {
      return getFloatValue(Attribute.TIME_OFFSET, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.START_TIME_SEC</code>
   * or -1 if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   * @return The integer value aquired from the attribute with the specified 
   * name or -1 if it can't be aquired.
   */
   public static int getStartTimeSec( IAttributeList attr_list )
   {
      return getIntValue(Attribute.START_TIME_SEC, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.TIME_OF_DAY</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getTimeOfDay( IAttributeList attr_list )
   {
      return getStringValue(Attribute.TIME_OF_DAY, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.DAY_OF_MONTH</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getDayOfMonth( IAttributeList attr_list )
   {
      return getStringValue(Attribute.DAY_OF_MONTH, attr_list );
   }


  /**
   * Get the value of the attribute specified by <code>Attribute.UNKNOWN</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getUnknown( IAttributeList attr_list )
   {
      return getStringValue(Attribute.UNKNOWN, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.INVALID_DATA_SET</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getInvalidDataSet( IAttributeList attr_list )
   {
      return getStringValue(Attribute.INVALID_DATA_SET, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.MONITOR_DATA</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getMonitorData( IAttributeList attr_list )
   {
      return getStringValue(Attribute.MONITOR_DATA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.SAMPLE_DATA</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getSampleData( IAttributeList attr_list )
   {
      return getStringValue(Attribute.SAMPLE_DATA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.PULSE_HEIGHT_DATA</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getPulseHeightData( IAttributeList attr_list )
   {
      return getStringValue(Attribute.PULSE_HEIGHT_DATA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.TEMPERATURE_DATA</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getTemperatureData( IAttributeList attr_list )
   {
      return getStringValue(Attribute.TEMPERATURE_DATA, attr_list );
   }


  /**
   * Get the value of the attribute specified by
   * <code>Attribute.PRESSURE_DATA</code>
   * or null if it can't be determined.
   *
   * @param attr_list The IAttributeList object (eg. a DataSet or Data block)
   *                  from which the attribute value is to be obtained.
   *
   * @return The String object aquired from the attribute with the specified 
   * name or <code>null</code> if it can't be aquired.
   */
   public static String getPressureData( IAttributeList attr_list )
   {
      return getStringValue( Attribute.PRESSURE_DATA, attr_list );
   }
   
}
