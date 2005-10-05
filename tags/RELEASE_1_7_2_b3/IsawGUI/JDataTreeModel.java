/*
 * File: JDataTreeModel.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * Revision 1.3  2005/08/17 02:06:11  dennis
 * Overide removeNodeFromParent() inherited from DefaultTreeModel,
 * to allow catching the case where a node no longer has a parent.
 * This happens if a selected range crosses DataSet and Experiment
 * boundaries.  Deleting such a range leads to attempts to delete
 * child nodes AFTER the parent is already deleted.
 *
 * Revision 1.2  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 */

package IsawGUI;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * extends DefaultTreeModel by including the 'extinguish' method
 * which completely disolves tree branches, instead of simply
 * removing the branch from its parent.
 */ 
public class JDataTreeModel extends DefaultTreeModel
{
  boolean  debug_flag = false;    // set true to get stack dump for
                                  // IllegalArgumentExceptions, like
                                  // node has no parent
  /**
   * initialize super class
   */ 
  public JDataTreeModel( TreeNode root )
  {
    super( root );
  }

  /**
   *  This method overrides the removeNodeFromParent() method
   *  of the DefaultTreeModel, to catch the case where a 
   *  node does not have a parent.
   *
   *  @param node  The node to be removed
   */
  public void removeNodeFromParent( MutableTreeNode node )
  {
    try
    {
       super.removeNodeFromParent( node );
    }
    catch ( IllegalArgumentException e )
    {
      if ( debug_flag )
        throw e;
    }
  }

  /**
   * removes 'node', setting that parent of each leaf to null
   * to ensure that System.gc can do its work.
   */ 
  public boolean extinguishNode( MutableTreeNode node )
  {
    if( node instanceof Experiment )
    {
      ( (Experiment)node ).extinguish();
      return true;
    }

    else if( node instanceof DataSetMutableTreeNode )
    {
      ( (DataSetMutableTreeNode)node ).extinguish();
      return true;
    }

    else if( node instanceof DataMutableTreeNode )
    {
      ( (DataMutableTreeNode)node ).extinguish();
      return true;
    }
 
    else
      return false;
  }
}

