/*
 * File:  FloatArrayPG.java 
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
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by 
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 *
 * $Log$
 * Revision 1.6  2003/06/23 20:18:14  bouzekc
 * Added GPL info.
 *
 */
package DataSetTools.parameter;

import DataSetTools.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import DataSetTools.util.PGActionListener;

public class FloatArrayPG extends VectorPG{

  public FloatArrayPG( String Prompt, Object value){ 
    super( new FloatPG("Enter Float",0.0f),"Enter Float List");
    setValue( value);
  }

  public static void main( String args[] ){
    JFrame jf = new JFrame("Test");
    jf.getContentPane().setLayout( new GridLayout( 1,2));
    FloatArrayPG IaPg = new FloatArrayPG( "Enter Int list", null);
    IaPg.init();
    jf.getContentPane().add(IaPg.getGUIPanel());
    JButton  jb = new JButton("Result");
    jf.getContentPane().add(jb);
    jb.addActionListener( new PGActionListener( IaPg));
    jf.setSize( 500,100);
    jf.invalidate();
    jf.show();
  }    
  public Object clone(){
    FloatArrayPG faap = new FloatArrayPG( getName(), getValue());
    return (Object)faap;
  }       
}
