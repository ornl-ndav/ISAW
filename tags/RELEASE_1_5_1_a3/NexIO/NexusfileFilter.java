/*
 * File: NexusfileFilter.java
 *
 * Copyright (C) 2001, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 */
package NexIO; 
import java.io.File; 
import javax.swing.*; 
import javax.swing.filechooser.*;  

public class NexusfileFilter extends FileFilter{
  public boolean accept(File f) {
    if (f.isDirectory() ) {return true;}      
    String s = f.getName();     
    int index = s.lastIndexOf('.');      
    String extension = null;     
    if ( index > 0 && index < s.length() - 1 ) {
      extension = s.substring(index +1).toLowerCase();         
    }   
    if ( extension != null ) {
      if ( extension.equals("nxs")|| extension.equals("hdf")){
        return true;            
      }else{
        return false;       
      }        
    }
    return false;      
  }  

  public String getDescription(){
    return "NeXus file (*.nxs/hdf)";      
  } 
} 
