/*
 * File: OperatorMenu.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  Provides a method to construct a hierarchical menu for a list of operators,
 *  organized according to the categories of the operators.
 *
 *  $Log$
 *  Revision 1.7  2004/03/15 03:27:59  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.6  2002/11/27 23:13:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/02/22 20:34:50  pfpeterson
 *  Operator Reorganization.
 *
 */
package DataSetTools.components.ui;  

import gov.anl.ipns.ViewTools.UI.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.operator.Generic.Load.*;
import DataSetTools.operator.Generic.Batch.*;

public class OperatorMenu
{

/**
 *  Static method to add a hierarchy of submenus and menu items corresponding 
 *  to a list of operators to an existing menu.  The submenu hierarchy is 
 *  determined by the categories of the operators.  Each operator will be added
 *  as a menu item to the appropriate submenu. 
 *
 *  @param  main_menu  An existing JMenu to which the submenu hierarchy is to
 *                     be added.
 *  @param  operators  List of operators that will be added to the menu.
 *  @param  listener   The ActionListener that will be used for each of the
 *                     operator menu items. 
 * 
 */
public static void build( JMenu           main_menu, 
                          Operator        operators[],
                          ActionListener  listener     )
{
  String    categories[];                     // list of categories for the
                                              // current operator

  int       cat_index;                        // index into the list of
                                              // categories

  int       comp_index;                       // index of submenu components
  int       num_components;                   // number of submenu components
  JMenuItem comp;                             // a menu component
 
                                              // for each operator, get its
                                              // list of categories and add
                                              // the new menu item to the 
                                              // correct submenu
  for ( int i = 0; i < operators.length; i++ )
  {
                                              // the list starts two entries, 
                                              // "Operator", "DataSetOperator"
                                              // that we ignore.
    categories = operators[i].getCategoryList();
                                              // step down the category tree,
                                              // at each level, if we don't
                                              // find the current category,
                                              // add it.  
    JMenu current_menu = main_menu;           // current_menu pointer steps  
                                              // down the tree of menus
    for ( cat_index = 2; cat_index < categories.length; cat_index++ ) 
    {
       num_components = current_menu.getMenuComponentCount();
       boolean found = false;
       comp_index = 0;
       while ( comp_index < num_components && !found )
       {
         comp = current_menu.getItem( comp_index );
         if ( comp.getText().equalsIgnoreCase( categories[cat_index] ) )
         {
           found = true;
           current_menu = (JMenu)comp;        // we found the category, advance 
         }                                    // the current menu pointer
         comp_index++;
       }
       if ( !found )                          // if we don't find it, add it
       {
         JMenu new_menu = new JMenu( categories[cat_index] );
         current_menu.add( new_menu );
         current_menu = new_menu;            // advance the current menu pointer
       }
    }
                                             // after stepping through the meun
                                             // tree, add the new operator title
    JMenuItem item = new JMenuItem( operators[i].getTitle() );
    item.addActionListener( listener );
    current_menu.add( item );
  }
}

/* ----------------------------- main ------------------------------------ */
/*
 *  For testing purposes only
 */
public static void main(String[] args)
{ 
  //
  // get a DataSet....
  //
  DataSetFactory ds_factory = new DataSetFactory("Sample DataSet");
  DataSet ds = ds_factory.getTofDataSet( InstrumentType.TOF_DG_SPECTROMETER );

  //
  // get list of DataSet operators....
  //
  int num_ops = ds.getNum_operators(); 
  Operator ds_ops[] = new Operator[num_ops];
  for ( int i = 0; i < num_ops; i++ )
    ds_ops[i] = ds.getOperator(i);

  //
  // build list of Generic operators for testing purposes...
  //
  Operator generic_ops[] = new Operator[7];
  generic_ops[0] = new SumRunfiles(); 
  generic_ops[1] = new LoadMonitorDS(); 
  generic_ops[2] = new LoadOneHistogramDS(); 
  generic_ops[3] = new LoadOneRunfile(); 
  generic_ops[4] = new EchoObject(); 
  generic_ops[5] = new pause(); 
  generic_ops[6] = new DataSetPrint(); 

  //
  // put up a frame with menu bar....
  //
  JFrame f = new JFrame("test for OperatorMenu.build()" );

  JMenuBar menu_bar = new JMenuBar();
  JMenu generic_opMenu = new JMenu("Generic Operators" );
  JMenu ds_opMenu      = new JMenu("DataSet Operators" );

  f.setJMenuBar( menu_bar );
  menu_bar.add( generic_opMenu );
  menu_bar.add( ds_opMenu );

  //
  // add the menu items for the operators to the menus....
  //
  ActionListener listener = new TestMenuHandler();
  OperatorMenu.build( ds_opMenu,      ds_ops,      listener );
  OperatorMenu.build( generic_opMenu, generic_ops, listener );

  //
  //  pop up the frame.... 
  //
  f.setBounds( 0, 0, 600, 400 );
  f.setVisible( true );
}


}


