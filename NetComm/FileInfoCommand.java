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
 *  Revision 1.5  2004/03/15 19:39:18  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
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
      this.command  = command;
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

  /**
   *  Get String form for the command, file name and directory.
   *
   *  @return a multiline String containing the command, file name and
   *          directory name.
   */
  public String toString()
  {
    return super.toString() + '\n' +
           "filename  = "   + filename + '\n' +
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

    if ( ! (other instanceof FileInfoCommand) )  // can't match if wrong class
      return false;

    if ( !super.equals( other ) )                // fields in base class must
      return false;                              // match

    FileInfoCommand other_fic = (FileInfoCommand)other;

    if ( filename.equals( other_fic.filename )   &&
         dir_name.equals( other_fic.dir_name )   )
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
                 "Warning:FileInfoCommand IsawSerialVersion != 1");
  }

}
