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
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PlaceHolderPG extends ParameterGUI {
  
  public PlaceHolderPG( String name, Object val){
    super( name, val);
    super.setType("PlaceHolder");
    super.setEntryWidget( new EntryWidget(new JLabel()));
  }

  public Object getValue(){
     return super.getValue();
  }
  
  
  public void setValue( Object value){
    super.setValue( value);
  }
  

  public void validateSelf(){
    setValid(true);
     
  }
  
  public void clear(){
    super.setValue( null);
  }
  
  public void initGUI( Vector V){
    
    super.initGUI();
    
  }
}
