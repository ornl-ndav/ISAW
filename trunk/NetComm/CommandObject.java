/*
 * File:  CommandObject.java
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
 *  Revision 1.5  2003/03/05 13:48:26  dennis
 *  Added method equals() that does field by field comparison.
 *
 *  Revision 1.4  2003/02/24 13:35:19  dennis
 *  Added command to get the server type.
 *
 *  Revision 1.3  2003/02/20 19:45:25  dennis
 *  Now implements Serializable
 *
 *  Revision 1.2  2003/02/20 00:08:38  dennis
 *  Added toString() method.
 *
 *  Revision 1.1  2003/02/19 21:56:49  dennis
 *  Intial version of command objects for client/server communication.
 */

package NetComm;

import java.io.*;

/**
 *  This is the base class for commands sent by clients to the live data and
 *  file servers.  This type of command supports status inquiry and a request
 *  for the available directories, since no other parameters are included.
 *  Derived classes support getting data from a specified file (or current run),
 *  and getting the list of files available in a directory. 
 */
public class CommandObject implements Serializable
{
  /*
   *  The supported commands are given below.  Additional codes may be inserted
   *  in the proper category to allow for future expansion of the command set.
   */
  public static final int INVALID         =   0; // These can be used at this 
  public static final int EXIT            =  10; // level. That is, no
  public static final int GET_STATUS      =  20; // parameters are needed. 
  public static final int GET_SERVER_TYPE =  30;
  public static final int GET_DIRECTORIES =  40; 
  public static final int END_BASIC_CMDS  = 100; // BOUND ON BASIC COMMANDS

  public static final int GET_DS_TYPES    = 110; // These can only be used with
  public static final int GET_DS_NAME     = 120; // a GetDataCommandObject 
  public static final int GET_DS          = 130; // since they require
  public static final int GET_DATA_BLOCKS = 140; // additional fields
  public static final int GET_SUMMARY     = 150; // to specify the data
  public static final int END_DATA_CMDS   = 200; // BOUND ON DATA COMMANDS

  public static final int GET_FILE_NAMES   = 110;// This can only be used with a
  public static final int GET_FILE_SUMMARY = 120;// GetFileCommandObject, since 
                                                 // it requires a directory name
  public static final int END_FILE_CMDS   = 300; // BOUND ON FILE_COMMANDS

  public static final String ALL_IDS = "-1";

  public static final int[] command_codes =    { INVALID,  
                                                 EXIT,
                                                 GET_STATUS,
                                                 GET_DIRECTORIES,
                                                 GET_DS_TYPES,
                                                 GET_DS_NAME,
                                                 GET_DS,
                                                 GET_DATA_BLOCKS,
                                                 GET_SUMMARY,
                                                 GET_FILE_NAMES,
                                                 GET_FILE_SUMMARY };

  public static final String[] command_names = {"INVALID", 
                                                "EXIT",
                                                "GET_STATUS",
                                                "GET_DIRECTORIES",
                                                "GET_DS_TYPES",   
                                                "GET_DS_NAME",
                                                "GET_DS",
                                                "GET_DATA_BLOCKS",
                                                "GET_SUMMARY",
                                                "GET_FILE_NAMES",
                                                "GET_FILE_SUMMARY" };

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
  protected int    command  = INVALID;
  private   String username = "";
  private   String password = "";

  
  /**
   *  Construct a basic command object using one of the commands allowed at
   *  this level.
   * 
   *  @param  command   One of INVALID, EXIT, GET_STATUS, GET_DIRECTORIES
   *  @param  username  Encrypted user name
   *  @param  password  Encrypted password 
   */
  public CommandObject( int command, String username, String password )
  {
    if ( command <= END_BASIC_CMDS )    // only allow top level commands
      this.command  = command; 

    this.username = username;
    this.password = password;
  } 

  /**
   *  Get the command code from this CommandObject
   *
   *  @return the command code stored in this object
   */
  public int getCommand()
  {
    return command;
  }

  /**
   *  Get the user name from this CommandObject
   *
   *  @return the user name stored in this object
   */
  public String getUsername()
  {
    return username;
  }

  /**
   *  Get the password from this CommandObject
   *
   *  @return the password stored in this object
   */
  public String getPassword()
  {
    return password;
  }

  /**
   *  Get the command code and name corresponding to the command code for 
   *  this command.
   *
   *  @return String containing the command code and command name.
   */
  public String toString()
  {
    for ( int i = 0; i < command_codes.length; i++ )
      if ( command_codes[i] == command )
        return ("Command is: " + command + ":" + command_names[i] );

    return ("Command code: " + command + " is invalid" );
  }


  /**
   *  Check whether or not the data members in this command object match
   *  the data members in the other specified command object.
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
                                                 // if not same, then compare
                                                 // the data members
    if ( command == other.command           &&
         username.equals( other.username )  &&
         password.equals( other.password )  )
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
                 "Warning:CommandObject IsawSerialVersion != 1");
  }

}
