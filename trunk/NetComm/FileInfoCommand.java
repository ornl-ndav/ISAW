/*
 * File:  FileInfoCommand.java
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
 *  Revision 1.1  2003/02/19 21:56:49  dennis
 *  Intial version of command objects for client/server communication.
 *
 */

package NetComm;

import java.io.*;
import DataSetTools.dataset.*;

/**
 *  This is the class for commands sent by clients to get information about
 *  files from the live data and file servers.  
 */
public class FileInfoCommand extends CommandObject
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
  private String filename = "";
  private String dir_name = "";

  /**
   *  Construct a FileInfoCommand using one of the get data commands.
   * 
   *  @param  command      One of GET_DS_TYPES, GET_DS_NAME, GET_DS, 
   *                       GET_DATA_BLOCKS, GET_SUMMARY
   *  @param  username     Encrypted user name
   *  @param  password     Encrypted password 
   *  @param  filename     The name of the file to get information about.
   *  @param  dir_name     The name of the directory to get information about.
   */
  public FileInfoCommand( int     command, 
                          String  username, 
                          String  password,
                          String  filename,
                          String  dir_name )
  {
    super( command, username, password );

    if ( END_DATA_CMDS < command  && 
         command       < END_FILE_CMDS )        // only allow file commands
    {
      this.filename = filename; 
      this.dir_name = dir_name;
    }
  } 

  /**
   *  Get the filename from this FileInfoCommandObject
   *
   *  @return the filename stored in this object
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   *  Get the directory name from this FileInfoCommandObject
   *
   *  @return the directory name stored in this object
   */
  public String getDir_name()
  {
    return dir_name;
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
                 "Warning:FileInfoCommand IsawSerialVersion != 1");
  }

}
