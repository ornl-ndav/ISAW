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
 * Revision 1.3  2005/02/16 02:02:55  kramer
 * Added javadoc documentation.
 *
 * Revision 1.2  2005/02/03 06:41:40  kramer
 *
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

/**
 * This class is used to read an NxLog node from a NeXus file and create a 
 * {@link DataSetTools.dataset.DataSet DataSet} from the data read.
 * @author Dominic Kramer
 */
public class Nxlog
{
   /** The name of the time node in the file. */
   public static final String TIME_PARAM_NAME = "time";
      /** The name of the time node's start time parameter in the file. */
      public static final String START_ATTRIBUTE_NAME = "start";
      /** The name of the time node's units parameter in the file. */
      public static final String UNITS_ATTRIBUTE_NAME = "units";
   /**
    * The name of the node in the file that contains the NxLog node's 
    * values.
    */
   public static final String VALUE_PARAM_NAME = "value";
   /**
    * The name of the node in the file that contains the NxLog node's 
    * raw values.
    */
   public static final String RAW_VALUE_PARAM_NAME = "raw_value";
   /** The name of the description node in the file. */
   public static final String DESC_PARAM_NAME = "description";
   /**
    * The name of the node in the file that contains the duration of 
    * an experiment.
    */
   public static final String DURATION_PARAM_NAME = "duration";
   
   //the following are not used to access data from the NxLog node.
   //Therefore, memory should not be used to hold these constants if they 
   //won't be used.  Note:  They are part of the NeXus standard for NxLog 
   //nodes and will be left as comments so they can be used if needed.
      //public static final String AVERAGE_VAL_PARAM_NAME = "average_value";
      //public static final String AVERAGE_VAL_ERR_PARAM_NAME = 
      //                               "average_value_error";
      //public static final String MIN_VAL_PARAM_NAME = "minimum_value";
      //public static final String MAX_VAL_PARAM_NAME = "maximum_value";   
   
   /**
    * Holds the message describing any errors that 
    * occur when the file is read.
    */
   private String errormessage;

   /**
    * Creates a new Nxlog object.  If the method 
    * {@link #getErrorMessage() getErrorMessage()} is called on the 
    * object it will return a blank string.
    */
   public Nxlog()
   {
     errormessage = "";
   }

   /**
    * Returns this class's currently recorded error message.  If an error 
    * occurs when the method 
    * {@link #processDS(NxNode, DataSet) processDS(NxNode, DataSet)} is 
    * invoked, this method can be invoked to learn what the error is.
    * @return The string holding any error that occured while the file was 
    *         being read.  A blank String is returned if no error has occured.
    */
   public String getErrorMessage()
   {
     return errormessage;
   }
   
   /**
    * Sets the error message recorded by this class.
    * @param message The new error message.
    * @return true if the message was successfully set and false if 
    *         it wasn't.
    */
   private boolean setErrorMessage(String message)
   {
      errormessage = message;
      return true;
   }
   
   /**
    * Reads an NxLog node and fills a {@link DataSet DataSet} with the 
    * data is has read.
    * @param nxlogNode The NxLog node to read.
    * @param ds        The {@link DataSet DataSet} where the data read is 
    *                  stored.
    * @return The error status:  true if an error occured and false otherwise.
    */
   public boolean processDS(  NxNode nxlogNode,  DataSet ds)
   {
     errormessage = "Improper inputs to Nxlog";
     if( nxlogNode == null)
       return true;
     if( ds == null)
       return true;
     if( !NxlogLocator.isNxLog(nxlogNode))
       return true;
     errormessage = "";
     
     //first look for the 'units' attribute.  If this value cannot be 
     //determined, it is impossible to create a DataSet.  To get the 'units' 
     //attribute the 'time' node needs to be located.
     NxNode timeNode = nxlogNode.getChildNode(TIME_PARAM_NAME);
        if (timeNode == null)
           return setErrorMessage("Cannot find time node found in NxLog node.");
     
        //used to convert Objects read from the node into Strings
        NxData_Gen stringGenerator = new NxData_Gen();
      
        //now to get the 'units' attribute
        Object timeUnitsOb = timeNode.getAttrValue(UNITS_ATTRIBUTE_NAME);
        if (timeUnitsOb == null)
           return setErrorMessage("Time node's \"units\" attribute cannot " +
                                  "be found.");
        String timeUnitsValue = stringGenerator.cnvertoString(timeUnitsOb);
        if (timeUnitsValue == null) //fail if the units cannot be found
           return setErrorMessage("The value of the time node's \"units\" " +
                                  "attribute cannot be determined.");
        
        //now to read the start time.  From this point on, a 'null' value is 
        //used to signal other code that the value couldn't be determined
        Object timeStartOb = timeNode.getAttrValue(START_ATTRIBUTE_NAME);
        String timeStartValue = null;
        if (timeStartOb != null)
           timeStartValue = stringGenerator.cnvertoString(timeStartOb);
        Date timeStartDate = null; //this will be used later if its defined 
                                   //i.e. non-null
        if (timeStartValue != null)
        {
           //now to convert the start time from a String into a more useful 
           //Object and set the start date and time
           timeStartDate = NxNodeUtils.parseISO8601(timeStartValue);
           if (timeStartDate != null)
           {
              String[] startDateTime = getDateAndTimeStrings(timeStartDate);
                 String startDateStr = startDateTime[0];
                 String startTimeStr = startDateTime[1];
              ds.setAttribute(
                    new StringAttribute(Attribute.START_DATE,startDateStr));
              ds.setAttribute(
                    new StringAttribute(Attribute.START_TIME,startTimeStr));
           }
        }
        
        //now to get the times written in the log file
        float[] timeArr = null;
        Object times = timeNode.getNodeValue();
        if (times != null)
           timeArr = NXData_util.Arrayfloatconvert(times);
        
        //now to set the DataSet's end time
        if (timeArr != null)
        {
           //the last value in the time array is used to determine 
           //the duration of the experiment
           float lastTime = timeArr[timeArr.length-1];
           long numMilliseconds = getNumMilliseconds(lastTime,timeUnitsValue);
           if (timeStartDate != null && numMilliseconds != -1)
           {
              Date timeEndDate = 
                 new Date(timeStartDate.getTime()+numMilliseconds);
              String[] endDateTime = getDateAndTimeStrings(timeEndDate);
                 String endDateStr = endDateTime[0];
                 String endTimeStr = endDateTime[1];
              ds.setAttribute(
                    new StringAttribute(Attribute.END_DATE,endDateStr));
              ds.setAttribute(
                    new StringAttribute(Attribute.END_TIME,endTimeStr));
           }
        }
     
     //now to make the Data objects for the DataSet
     //first to get the data from the node
     NxNode valueArrNode = nxlogNode.getChildNode(VALUE_PARAM_NAME);
     if (valueArrNode != null)
     {
        Object valueArrOb = valueArrNode.getNodeValue();
        if (valueArrOb != null)
        {
           float[] valueArr = NXData_util.Arrayfloatconvert(valueArrOb);
           if (valueArr != null && timeArr != null)
           {
              //now to make the Data object for the regular 
              //values read from the node
              VariableXScale valueXScale = new VariableXScale(timeArr);
              ds.addData_entry(Data.getInstance(valueXScale,valueArr,0));
           }
        }
     }
     
     //now to get the raw data from the node
     NxNode rawValueArrNode = nxlogNode.getChildNode(RAW_VALUE_PARAM_NAME);
     if (rawValueArrNode != null)
     {
        Object rawValueArrOb = rawValueArrNode.getNodeValue();
        if (rawValueArrOb != null)
        {
           float[] rawValueArr = NXData_util.Arrayfloatconvert(rawValueArrOb);
           if (rawValueArr != null && timeArr != null)
           {
              //now to make the Data object for the raw values read from the node
              VariableXScale rawValueXScale = new VariableXScale(timeArr);
              ds.addData_entry(Data.getInstance(rawValueXScale,rawValueArr,1));
           }
        }
     }
     
     //now to read the description from the NxLog node
     //this information is not absolutely necessary and thus 
     //this method won't fail if the data can't be found
     Object descriptionOb = nxlogNode.getChildNode(DESC_PARAM_NAME);
     if (descriptionOb != null)
     {
        String descriptionStr = (new NxData_Gen()).cnvertoString(descriptionOb);
        if (descriptionStr != null)
           ds.setAttribute(new StringAttribute(Attribute.LABEL,descriptionStr));
     }
     
     return false;
  }//processDS
  
  /**
   * Converts a Date object into a date string and a time string.
   * @param date The date to analyze.
   * @return A two element String array.  
   *         <ul>
   *            <li>
   *               The first element is a String representing the date's 
   *               day, month, and year in the format yy-mm-dd.
   *            </li>
   *            <li>
   *               The second element is a String representing the 
   *               date's hours, minutes, and seconds in the format 
   *               hh-mm-ss.
   *            </li>
   *         </ul>
   */
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
  
  //TODO Determine if there are any more possible units that could be read 
  //     from a NeXus file.
  /**
   * Converts a time from its recorded units into milliseconds.
   * @param timeValue 
   * @param units The string representing the units that the time 
   *              <code>timeValue</code> is recorded in.
   *              <br>
   *              Possible values are:
   *              <ul>
   *                 <li>second</li>
   *                 <li>microseconds</li>
   *              </ul>
   * @return The number of milliseconds equivalent of the 
   *         time <code>timeValue</code> or -1 if <code>units</code> 
   *         is not recognized.
   */
  private long getNumMilliseconds(float timeValue, String units)
  {
     if (units == null)
        return -1;
     else if (units.equals("second"))
        return (long)(timeValue*1000);
     else if (units.equals("microseconds"))
        return (long)(timeValue*10);
     else
        return -1;
  }
  
  /**
   * Testbed.
   * @param args args[0] should hold the name of the NeXus file to read.
   */
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
