/*
 * File:  PlaceHolder.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2005/06/13 14:30:58  rmikk
 * Added clone method
 * set the Entry widget in the initGUI method
 *
 * Revision 1.2  2005/06/08 19:56:53  rmikk
 * Took the handling of the value out of the ParameterGUI superclass
 *    so it is not changed to a String for some reason
 *
 * Revision 1.1  2004/07/14 16:59:01  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.parameter;

import java.util.Vector;
import DataSetTools.components.ParametersGUI.*;
import javax.swing.*;

/**
 * This ParameterGUI saves an Object value which cannot be alterred by the GUI.
 * @author MikkelsonR
 
 */
public class PlaceHolderPG extends ParameterGUI {
  
  Object value = null;
  public PlaceHolderPG( String name, Object val){
    super( name, val);
    value = val;
    super.setType("PlaceHolder");
   
  }

  public Object getValue(){
     if( value == null)
        return new Object();
     return value;
  }
  
  
  public void setValue( Object value){
    this.value = value;
  }
  

  public void validateSelf(){
    setValid(true);
     
  }
  
  
  public void clear(){
    value = null;
  }
  
  public void initGUI( Vector V){
    if( getInitialized())
      return;
     setEntryWidget( new EntryWidget(new JLabel()));
    super.initGUI();
    
  }
  
  public Object clone(){
    PlaceHolderPG p = (PlaceHolderPG)super.clone();
    p.value = value;
    return p;
  }
}
