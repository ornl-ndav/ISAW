/*
 * File:  Nxlog.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * $Log$
 * Revision 1.2  2005/02/03 06:41:40  kramer
 * Now this class can read the Nxlog section of a NeXus file if it is
 * written using the standard as it is on 02/03/2005.
 *
 * Revision 1.1  2003/11/16 21:35:00  rmikk
 * Initial Checkin
 *
 * Revision 1.4  2002/11/
 */
package NexIO;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.VariableXScale;
import NexIO.NexApi.NexNode;


public class Nxlog
{
   public static final String TIME_PARAM_NAME = "time";
      public static final String START_ATTRIBUTE_NAME = "start";
      public static final String UNITS_ATTRIBUTE_NAME = "units";
   public static final String VALUE_PARAM_NAME = "value";
   public static final String RAW_VALUE_PARAM_NAME = "raw_value";
   public static final String DURATION_PARAM_NAME = "duration";
   public static final String DESC_PARAM_NAME = "description";
   
   //the following are not used to access data from the NxLog node.
   //Therefore, memory should not be used to hold these constants if they 
   //won't be used.  Note:  They are part of the NeXus standard for NxLog 
   //nodes and will be left as comments so they can be used if needed.
      //public static final String AVERAGE_VAL_PARAM_NAME = "average_value";
      //public static final String AVERAGE_VAL_ERR_PARAM_NAME = 
      //                               "average_value_error";
      //public static final String MIN_VAL_PARAM_NAME = "minimum_value";
      //public static final String MAX_VAL_PARAM_NAME = "maximum_value";
   
   
   private String errormessage;

   public Nxlog()
   {
     errormessage = "";
   }

   public String getErrorMessage()
   {
     return errormessage;
   }
   
   private boolean setErrorMessage(String message)
   {
      errormessage = message;
      return true;
   }
   
   /**
    * 
    * @param nxlogNode
    * @param ds
    * @return The error status:  true if an error occured and false otherwise.
    */
   public boolean processDS(  NxNode nxlogNode,  DataSet ds)
   {
      System.out.println("Inside Nxlog.processDS(....)");
      
     errormessage = "Improper inputs to Nxlog";
     if( nxlogNode == null)
       return true;
     if( ds == null)
       return true;
     if( !NxlogLocator.isNxLog(nxlogNode))
       return true;
     errormessage = "";
     
     
     //now to get the time information
     NxNode timeNode = nxlogNode.getChildNode(TIME_PARAM_NAME);
     if (timeNode == null)
        return setErrorMessage("Cannot find time node found in NxLog node.");
     
     //used to convert Objects read from the node into Strings
     NxData_Gen stringGenerator = new NxData_Gen();
     
     Object timeStartOb = timeNode.getAttrValue(START_ATTRIBUTE_NAME);
     if (timeStartOb == null)
        return setErrorMessage("Time node's \"start\" attribute cannot " +
                                  "be found.");     
     String timeStartValue = stringGenerator.cnvertoString(timeStartOb);
     if (timeStartValue == null)
        return setErrorMessage("The value of the time node's \"start\" " +
                                  "attribute cannot be determined.");
     
     //now to get the times written in the log file as well as well as the 
     //start time and the units for the time
     Object times = timeNode.getNodeValue();
     if (times == null)
        return setErrorMessage("The time node in the NxLog node could not " +
            "be found.");
     float[] timeArr = NXData_util.Arrayfloatconvert(times);
     if (timeArr == null)
        return setErrorMessage("The value of the time node in the NxLog " +
            "node could not be found.");     
     
     Object timeUnitsOb = timeNode.getAttrValue(UNITS_ATTRIBUTE_NAME);
     if (timeUnitsOb == null)
        return setErrorMessage("Time node's \"units\" attribute cannot " +
                                  "be found.");
     String timeUnitsValue = stringGenerator.cnvertoString(timeUnitsOb);
     if (timeStartValue == null)
        return setErrorMessage("The value of the time node's \"units\" " +
                                  "attribute cannot be determined.");
     
     //now to convert the start time from a String into a more useful 
     //Object and set the start date and time
     Date timeStartDate = NxNodeUtils.parseISO8601(timeStartValue);
     if (timeStartDate == null)
        return setErrorMessage("Parsing of the start time into its " +
                                  "corresponding date and/or time has failed.");
     String[] startDateTime = getDateAndTimeStrings(timeStartDate);
        String startDateStr = startDateTime[0];
        String startTimeStr = startDateTime[1];
     ds.setAttribute(new StringAttribute(Attribute.START_DATE,startDateStr));
     ds.setAttribute(new StringAttribute(Attribute.START_TIME,startTimeStr));
     
     
     //now to get the duration so that the end date and time can be set
     NxNode durationNode = nxlogNode.getChildNode(DURATION_PARAM_NAME);
     if (durationNode == null)
        return setErrorMessage("Cannot find the duration node in NxLog node.");
     Object durationOb = durationNode.getNodeValue();
     if (durationOb == null)
        return setErrorMessage("Cannot determine duration node's value in " +
                               "NxLog node.");
     NxData_Gen dataGenerator = new NxData_Gen();
     Float durationFloatValue = dataGenerator.cnvertoFloat(durationOb);
     if (durationFloatValue == null)
        return setErrorMessage("Cannot resolve the duration node's value's " +
                               "float value.");
     //TODO This assumes that the duration from the file is in seconds
     long durationMilliseconds = (long)(durationFloatValue.floatValue()*1000);
     long startDateMilliseconds = timeStartDate.getTime();
     Date timeEndDate = new Date(startDateMilliseconds+durationMilliseconds);
     String[] endDateTime = getDateAndTimeStrings(timeEndDate);
        String endDateStr = endDateTime[0];
        String endTimeStr = endDateTime[1];
     ds.setAttribute(new StringAttribute(Attribute.END_DATE,endDateStr));
     ds.setAttribute(new StringAttribute(Attribute.END_TIME,endTimeStr));
     
     
     //now to make the Data objects for the DataSet
     //first to get the data from the node
     NxNode valueArrNode = nxlogNode.getChildNode(VALUE_PARAM_NAME);
     NxNode rawValueArrNode = nxlogNode.getChildNode(RAW_VALUE_PARAM_NAME);
     
     if (valueArrNode == null || rawValueArrNode == null)
        return setErrorMessage("Cannot find data nodes in NxLog node.");
     
     Object valueArrOb = valueArrNode.getNodeValue();
     Object rawValueArrOb = rawValueArrNode.getNodeValue();
     if (valueArrOb == null || rawValueArrOb == null)
        return setErrorMessage("Cannot determine the data nodes' values " +
                                  "in NxLog node.");
     
     float[] valueArr = NXData_util.Arrayfloatconvert(valueArrOb);
     float[] rawValueArr = NXData_util.Arrayfloatconvert(rawValueArrOb);
     
     if (valueArr == null || rawValueArr == null)
        return setErrorMessage("Cannot determine the data nodes' values' " +
                                  "arrays in NxLog node.");
     
     //these are used to make the XScale objects used in the Data objects
     //and were needed when UniformXScale objects were used
     //now they shouldn't be needed
     //float startTimeSeconds = timeStartDate.getTime()/1000f;
     //float endTimeSeconds = timeEndDate.getTime()/1000f;
     
     //now to make the Data object for the regular values read from the node
     VariableXScale valueXScale = new VariableXScale(timeArr);
     ds.addData_entry(Data.getInstance(valueXScale,valueArr,0));
     
     //now to make the Data object for the raw values read from the node
     VariableXScale rawValueXScale = new VariableXScale(timeArr);
     ds.addData_entry(Data.getInstance(rawValueXScale,rawValueArr,1));
     
     
     //now to read the description from the NxLog node
     //this information is not absolutely necessary and thus 
     //this method won't fail if the data can't be found
     Object descriptionOb = nxlogNode.getChildNode(DESC_PARAM_NAME);
     if (descriptionOb != null)
     {
        String descriptionStr = dataGenerator.cnvertoString(descriptionOb);
        if (descriptionStr != null)
           ds.setAttribute(new StringAttribute(Attribute.LABEL,descriptionStr));
     }
     
     return false;
  }//processDS
  
  private static String[] getDateAndTimeStrings(Date date)
  {
     GregorianCalendar calendar = new GregorianCalendar();
     calendar.setTime(date);
     //these specifications are made so that the Date is interpreted to follow 
     //the ISO8601 format
        calendar.setMinimalDaysInFirstWeek(4);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
     String dateStr = ""+calendar.get(Calendar.YEAR)+"-"+
                      (1+calendar.get(Calendar.MONTH))+"-"+
                      calendar.get(Calendar.DAY_OF_MONTH);
     String timeStr = ""+calendar.get(Calendar.HOUR)+":"+
                      calendar.get(Calendar.MINUTE)+":"+
                      calendar.get(Calendar.SECOND);
     String[] result = new String[2];
        result[0] = dateStr;
        result[1] = timeStr;
     return result;
  }
   
  public static void main(String[] args)
  {
     // Not tested
     // Assume the NXlog is in  file aaa.nxs, NXentry name is GPPD12358, 
     // NXsample name  Coal, and NXlog name Temp
     // Use NXvalid to see what the names really are
     NxNode node =(NxNode) new NexNode(args[0]);// include path if aaa.nxs 
                                                   // is not in directory
                                                   // or use args[0] 
                                                   // (my favorite)
     node = node.getChildNode("Entry0");// or use args[1]
     node = node.getChildNode("downstream");
     node= node.getChildNode("Log_1");
     System.out.println("Using "+node.getNodeClass()+" as an Nxlog node");
     System.out.println("  Is it an Nxlog node:  "+NxlogLocator.isNxLog(node));
     DataSet ds = new DataSet();
     Nxlog log = new Nxlog();
     if(!log.processDS( node, ds))
       Command.ScriptUtil.display(ds);  //Sets up a view
     System.out.println("Error Message="+log.getErrorMessage());
  }
}
