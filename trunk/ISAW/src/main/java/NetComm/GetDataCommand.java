/*
 * File:  GetDataCommand.java
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
 *  Revision 1.4  2003/03/05 13:48:26  dennis
 *  Added method equals() that does field by field comparison.
 *
 *  Revision 1.3  2003/02/24 13:36:04  dennis
 *  Fixed bug... subclass command was not recorded properly in the
 *  constructor.
 *
 *  Revision 1.2  2003/02/20 00:08:38  dennis
 *  Added toString() method.
 *
 *  Revision 1.1  2003/02/19 21:56:49  dennis
 *  Intial version of command objects for client/server communication.
 *
 */

package NetComm;

import java.io.*;
import DataSetTools.dataset.*;

/**
 *  This is the class for commands sent by clients to retrieve data from 
 *  the live data and file servers.  
 */
public class GetDataCommand extends CommandObject
{
  /**
   *  We define the serialVersionUID so that we can upgrade and maintain basic
   *  compatibility by just adding new commands.
   */
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
  private String filename     = "";
  private int    ds_number    = 0;
  private String group_ids    = "";
  private float  min_x        = 0;
  private float  max_x        = 0;
  private int    rebin_factor = 1;  
  private int    attr_mode    = Attribute.FULL_ATTRIBUTES;

  /**
   *  Construct a GetDataCommand using one of the get data commands.
   * 
   *  @param  command      One of GET_DS_TYPES, GET_DS_NAME, GET_DS, 
   *                       GET_DATA_BLOCKS, GET_SUMMARY
   *  @param  username     Encrypted user name
   *  @param  password     Encrypted password 
   *  @param  filename     The name of the file to get.  This may be either the
   *                       fully qualified filename, or just the base file name.
   *                       If it is the base filename, the permitted directories
   *                       will be searched.  This parameter is not used by the
   *                       LiveDataServer.
   *  @param  ds_number    The number of the DataSet to get from the specified
   *                       file, or current run.
   *  @param  group_ids    String code for the list of ids to get.  If this is
   *                       not a valid sublist of ids, all ids will be used.  
   *  @param  min_x        Minimum "x" value to include (NOTE: if min_x>=max_x
   *                       all values will be included.)
   *  @param  max_x        Maximum "x" value to include (NOTE: if min_x>=max_x
   *                       all values will be included.)
   *  @param  rebin_factor 1-indicates don't rebin, 2-indicates combine 
   *                       adjacent pairs of bins, etc. 0, or any value at
   *                       least as big as the number of bins, indicates that
   *                       the total of all bins should be returned. 
   *  @param  attr_mode    One of Attribute.NO_ATTRIBUTES, 
   *                              Attribute.ANALYSIS_ATTRIBUTES
   *                              Attribute.FULL_ATTRIBUTES
   */
  public GetDataCommand( int     command, 
                         String  username, 
                         String  password,
                         String  filename,
                         int     ds_number, 
                         String  group_ids, 
                         float   min_x,
                         float   max_x,
                         int     rebin_factor,
                         int     attr_mode   )
  {
    super( command, username, password );

    if ( END_BASIC_CMDS <  command  && 
         command        <= END_DATA_CMDS )   // only allow get data commands
    {
      this.command      = command;
      this.filename     = filename; 
      this.ds_number    = ds_number;
      this.group_ids    = group_ids;
      this.min_x        = min_x;
      this.max_x        = max_x;
      this.rebin_factor = rebin_factor;
      this.attr_mode    = attr_mode;
    }
  } 

  /**
   *  Get the filename from this GetDataCommand
   *
   *  @return the filename stored in this object
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   *  Get the DataSet number from this GetDataCommand
   *
   *  @return the DataSet number requested by this command object
   */
  public int getDataSetNumber()
  {
    return ds_number;
  }

  /**
   *  Get the list of group ids from this GetDataCommand, encoded as a list
   *  of integers in a String
   *
   *  @return the group ids requested by this command object
   */
  public String getGroup_ids()
  {
    return group_ids;
  }

  /**
   *  Get the minimum x-value from this GetDataCommand.
   *
   *  @return the minimum x-value requested by this command object
   */
  public float getMin_x()
  {
    return min_x;
  }

  /**
   *  Get the maximum x-value from this GetDataCommand.
   *
   *  @return the maximum x-value requested by this command object
   */
  public float getMax_x()
  {
    return max_x;
  }

  /**
   *  Get the rebin factor from this GetDataCommand
   *
   *  @return the rebin factor requested by this command object
   */
  public int getRebin_factor()
  {
    return rebin_factor;
  }

  /**
   *  Get the attribute mode from this GetDataCommand
   *
   *  @return the attribute mode requested by this command object
   */
  public int getAttribute_mode()
  {
    return attr_mode;
  }

  /**
   *  Get String form for the command, and command parameters.
   *
   *  @return a multiline String containing the command, file name and
   *          other parameter values for this command.
   */
  public String toString()
  {
    return super.toString() + '\n' +
           "filename  = "   + filename + '\n'  +
           "ds_number = "   + ds_number + '\n' +
           "group_ids = "   + group_ids + '\n' +
           "min_x     = "   + min_x     + '\n' +
           "max_x     = "   + max_x     + '\n' +
           "rebin_factor = " + rebin_factor + '\n' +
           "attr_mode    = " + attr_mode;
  }


  /**
   *  Check whether or not the data members in this get data command 
   *  object match the data members in the other specified command object.
   *
   *  @param  other  The other CommandObject that is compared to the 
   *                 current object.
   *
   *  @return  True if the fields match, false otherwise.
   */
  public boolean equals( CommandObject other )
  {
    if ( other == this )                         // same object
      return true;

    if ( ! (other instanceof GetDataCommand) )   // can't match if wrong class
      return false;                            

    if ( !super.equals( other ) )                // fields in base class must
      return false;                              // match

    GetDataCommand other_gd = (GetDataCommand)other;

    if ( ds_number    == other_gd.ds_number     &&
         min_x        == other_gd.min_x         && 
         max_x        == other_gd.max_x         && 
         rebin_factor == other_gd.rebin_factor  && 
         attr_mode    == other_gd.attr_mode     &&
         filename.equals( other_gd.filename )   &&    
         group_ids.equals( other_gd.group_ids )  ) 
      return true;
    else
      return false;
  }


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
                 "Warning:GetDataCommand IsawSerialVersion != 1");
  }

}
