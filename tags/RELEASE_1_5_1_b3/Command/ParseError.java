/*
 * File:  ParseError.java 
 *             
 * Copyright (C) 2003, Peter Peterson
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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/06/13 14:58:34  pfpeterson
 * Added to CVS.
 *
 */
package Command;

public class ParseError extends Error{
  private int linenum=0;
  private int colnum=0;

  public ParseError(String message){
    super(message);
  }

  public ParseError(String message, int linenum){
    this(message);
    this.linenum=linenum;
  }

  public ParseError(String message, int linenum, int offset){
    this(message,linenum);
    this.colnum=offset;
  }

  /**
   * Zero indexed line number.
   */
  public int linenum(){
    return this.linenum;
  }

  /**
   * Zero indexed column number (offset).
   */
  public int offset(){
    return this.colnum;
  }
}
