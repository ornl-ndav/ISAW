/*
 * File:  ExecuteOpCommand.java
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
 *  Revision 1.1  2003/10/21 21:14:01  dennis
 *  CommandObject for remote execution of operators.
 *
 */

package NetComm;

import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;

/**
 *  This is the class for commands sent by clients to request that an 
 *  operator be run on a remote server.
 */
public class ExecuteOpCommand extends CommandObject
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
  private Operator op        = null;
  private String   file_name = "";
  private String   dir_name  = "";

  /**
   *  Construct an  ExecuteOpCommand using one of the get execute.
   * 
   *  @param  command      One of RETURN_RESULT, RESULT_IN_FILE 
   *  @param  username     Encrypted user name
   *  @param  password     Encrypted password 
   *  @param  op           The operator to execute
   *  @param  file_name    The file name to write result to, if only status
   *                       is returned.
   *  @param  dir_name     The director to write result to, if only status
   *                       is returned.
   */
  public ExecuteOpCommand( int      command, 
                           String   username, 
                           String   password,
                           Operator op,
                           String   file_name,
                           String   dir_name )
  {
    super( command, username, password );

    if ( END_FILE_CMDS < command  && 
         command       < END_EXECUTE_CMDS )      // only allow Execute commands
    {
      this.command   = command;
      this.op        = op;
      this.file_name = file_name; 
      this.dir_name  = dir_name;
    }
  } 

  /**
   *  Get the operator from this ExecuteOpCommand object
   *
   *  @return the operator stored in this object
   */
  public Operator getOperator()
  {
    return op;
  }

  /**
   *  Get the filename from this ExecuteOpCommand object 
   *
   *  @return the filename stored in this object
   */
  public String getFilename()
  {
    return file_name;
  }

  /**
   *  Get the directory name from this ExecuteOpCommand object 
   *
   *  @return the directory name stored in this object
   */
  public String getDir_name()
  {
    return dir_name;
  }

  /**
   *  Get String form for the command, file name and directory.
   *
   *  @return a multiline String containing the command, file name and
   *          directory name.
   */
  public String toString()
  {
    return super.toString() + '\n' +
           "operator  = "   + op        + '\n' +
           "filename  = "   + file_name + '\n' +
           "directory = "   + dir_name;
  }

  /**
   *  Check whether or not the data members in this file info command object
   *  match the data members in the other specified command object.
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

    if ( ! (other instanceof ExecuteOpCommand) )  // can't match if wrong class
      return false;

    if ( !super.equals( other ) )                // fields in base class must
      return false;                              // match

    ExecuteOpCommand other_eoc = (ExecuteOpCommand)other;

    if ( file_name.equals( other_eoc.file_name )   &&
         dir_name.equals ( other_eoc.dir_name )   && 
         op.equals       ( other_eoc.op       )   )
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
                 "Warning:ExecuteOpCommand IsawSerialVersion != 1");
  }

}
