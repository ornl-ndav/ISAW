
/*
 * File:  IntegerArrayPG.java 
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
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.6  2003/06/23 14:52:44  bouzekc
 * Removed duplicate inner ActionListener class.  Now uses
 * PGActionListener.
 *
 * Revision 1.5  2003/06/18 20:36:41  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.4  2003/06/09 22:30:06  rmikk
 * Added a clone method
 *
 * Revision 1.3  2003/05/25 18:42:49  rmikk
 * Added GPL
 *
 */
package DataSetTools.parameter;

import DataSetTools.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import DataSetTools.util.PGActionListener;

public class IntegerArrayPG extends VectorPG
  {

   public IntegerArrayPG( String Prompt, Object value)
      { super( new IntegerPG("Enter Integer",0),"Enter Integer List");
        setValue( value);
       }

   public Object clone()
      {

       IntegerArrayPG iapg = new IntegerArrayPG( getName(), getValue());
       return (Object) iapg;

      }
      public static void main( String args[] )
      {
         JFrame jf = new JFrame("Test");
         jf.getContentPane().setLayout( new GridLayout( 1,2));
         IntegerArrayPG IaPg = new IntegerArrayPG( "Enter Int list", null);
         IaPg.init();
         jf.getContentPane().add(IaPg.getGUIPanel());
         JButton  jb = new JButton("Result");
         jf.getContentPane().add(jb);
         jb.addActionListener( new PGActionListener( IaPg));
         jf.setSize( 500,100);
         jf.invalidate();
         jf.show();




      }      
  }
