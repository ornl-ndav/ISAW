/*
 * File:  JDataTreeCellRenderer.java
 *
 * Copyright (C) 2004 Dominic Kramer
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
 *           Dominic Kramer <kramerd@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * $Log$
 * Revision 1.1  2004/07/07 16:23:11  kramer
 * This is the renderer used to render the components added to a JDataTree.
 *
 */
package IsawGUI;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * This is used to render the nodes in JDataTree.
 * @author Dominic Kramer
 */
public class JDataTreeCellRenderer extends DefaultTreeCellRenderer
{
   /**
    * Get the configured component which is to be placed in the tree.
    */
   public Component getTreeCellRendererComponent(JTree tree, Object value, 
                        boolean sel, boolean expanded, boolean leaf, 
                           int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
            row, hasFocus);
      
      if (value instanceof DataMutableTreeNode)
      {
         if (((DataMutableTreeNode)value).isSelected())
            setForeground(Color.BLUE);
         else
            setForeground(Color.BLACK);
      }
      
      return this;
   }
}
