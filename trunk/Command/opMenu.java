/*
 * File:  opMenu.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.19  2002/04/23 20:59:22  pfpeterson
 * Do not show operators that implement the HiddenOperator
 * interface in the menus. BatchOperators now implement the
 * interface.
 *
 * Revision 1.18  2002/02/22 20:33:48  pfpeterson
 * Operator Reorganization.
 *
 * Revision 1.17  2002/01/10 15:42:47  rmikk
 * Added an addStatusPane method.
 * Added this StatusPane to the JParametersDialog
 *
 * Revision 1.16  2001/08/16 20:31:09  rmikk
 * Fixed the javadocs @see tags
 *
 * Revision 1.15  2001/08/14 16:16:19  rmikk
 * GenericBatch operators no longer show in opMenu
 *
 * Revision 1.14  2001/08/09 14:10:24  rmikk
 * Modified  So that submenues can also start at an arbitrary
 * position in the category list (if all menu options category
 * list agrees to n positions, those first n positions can be
 * omitted from the menu)
 *
 * Revision 1.13  2001/08/06 22:14:41  rmikk
 * Fixed Error so Data Sets are now sent to the tree
 *
 * Revision 1.12  2001/07/18 16:25:25  neffk
 * changed the DataSet[] parameter to IDataSetListHandler, a more
 * dynamic solution to having a current list of DataSet objects.
 *
 * Revision 1.11  2001/06/27 18:39:13  rmikk
 * Added the setOpMenuLabel to change the label from "Operations:
 *
 * Revision 1.10  2001/06/26 14:44:44  rmikk
 * Changed DataSetListHandler to IDataSetListHandler
 *
 * Revision 1.9  2001/06/25 21:49:40  rmikk
 * Added Parameter to JParametersDialog
 *
 * Revision 1.8  2001/06/25 16:38:57  rmikk
 * Incorporated tests for improper inputs
 *
 * Revision 1.7  2001/06/05 16:50:35  rmikk
 * Changed props.dat to IsawProps.dat
 *
 * Revision 1.6  2001/06/01 21:14:13  rmikk
 * Added Documentation for javadocs etc.
 *
 
 *  5-25-2001  Created
 */
package Command;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import java.awt.event.*; 
import javax.swing.text.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.operator.Generic.Batch.*;
import java.io.*;

/**  A Jmenu especially for lists of operators with a getCategorylist
*/
public class opMenu extends JMenu 
{OperatorHandler op;
 MActionListener ML;
 IDataSetListHandler DS;
 IObserver iobs;
 StatusPane statPane;
/**
* @param op   Gets the list of operators to be placed in this menu
* @param DS  Gets the list of Data Sets that can be used for parameters
* @param logdoc the log file to place log comments
* @param iobs  an iobserver of these operations
*@see OperatorHandler 
*@see DataSetTools.components.ParametersGUI.IDataSetListHandler 
*@see DataSetTools.util.IObserver
*/
public opMenu(OperatorHandler op , IDataSetListHandler DS, Document logdoc , 
           IObserver iobs)
    {
       super("Operations");
       initt( op, DS, logdoc, iobs, 1,null);
    }
public opMenu(OperatorHandler op , IDataSetListHandler DS, 
        Document logdoc , IObserver iobs , int start)
    { super("Operations");
      initt( op, DS, logdoc, iobs, start,null);
    }

private  void initt(OperatorHandler op , IDataSetListHandler DS, 
        Document logdoc , IObserver iobs , int start, StatusPane stPane)
  { statPane=stPane;
   this.op = op;
   this.DS = DS;
   this.iobs = iobs;
   int cat_index;
   int       comp_index;                       // index of submenu components
  int       num_components;                   // number of submenu components
  JMenuItem comp;  
  String    categories[];
  boolean found;
  MActionListener ML= new MActionListener( op , DS , logdoc, iobs);
                                                 // correct submenu
  found = false;  
  
  for ( int i = 0; i < op.getNum_operators(); i++ )
  {
                                              // the list starts two entries, 
                                              // "Operator", "DataSetOperator"
                                              // that we ignore.
    categories = op.getOperator(i).getCategoryList();
                                              // step down the category tree,
                                              // at each level, if we don't
                                              // find the current category,
                                              // add it.  
    JMenu current_menu = this;           // current_menu pointer steps  
                                              // down the tree of menus
    if( categories == null)
       {categories = new String[1];
        categories[0]=Operator.OPERATOR; 
       }
    found = true;
    if( !(op.getOperator(i) instanceof HiddenOperator) )
    {for ( cat_index = start; (cat_index < categories.length) &&(found); cat_index++ ) 
    {
       num_components = current_menu.getMenuComponentCount();
       found = false;
       comp_index = 0;
     while ( comp_index < num_components && !found )
       {
         comp = (JMenuItem)(current_menu.getItem( comp_index) );
         if(comp instanceof JMenu)
         if ( comp.getLabel().equalsIgnoreCase( categories[cat_index] ) )
           {
            found = true;
            current_menu = (JMenu)((JMenuItem)comp);        // we found the category, advance 
           }                                    // the current menu pointer
         comp_index++;
       }
       if ( !found )                          // if we don't find it, add it
       {
         JMenu new_menu = new JMenu( categories[cat_index] );
         if( new_menu == null)
             {System.out.println("Could not create a JMenu"+cat_index+","+categories.length);
	      found = false;             
             }
         else
         {
          new_menu.setDelay(200);
          current_menu.add( new_menu );
          current_menu = new_menu;            // advance the current menu pointer
          found = true;
	 }
       }
    }
                                             // after stepping through the meun
                                             // tree, add the new operator title
    if( found)
     { String Title=op.getOperator(i).getTitle();
       if( Title.equals("UNKNOWN"))
          Title = op.getOperator(i).getCommand();
       MJMenuItem item = new MJMenuItem( Title,i );
       if( item == null)
	   {System.out.println("Could not create a JMenuItem");
           }
       else
         {item.addActionListener( ML );
          current_menu.add( item );
         }
     }
    }//if op.getOperator( i) not instanceof Generic Batch
  }
  }//constructor
public void setOpMenuLabel( String newText)
  {setText( newText );
  }
public void addStatusPane( StatusPane stPane)
  {statPane= stPane;
   }
private class MActionListener implements ActionListener
  {OperatorHandler op;
   IDataSetListHandler DS;
  Document logdoc;
  IObserver iobs;
    public MActionListener( OperatorHandler op,IDataSetListHandler DS , Document logdoc, IObserver iobs)
       {this.op = op;
        this.DS = DS;
        this.logdoc=logdoc;  
        this.iobs=iobs;
       }
   public void actionPerformed(ActionEvent e) 
      {
        if( !(e.getSource() instanceof MJMenuItem))
           return;
       if( op == null ) 
            return;
      
         MJMenuItem x =(MJMenuItem)( e.getSource());
         int opnum = x.getopnum();
         
         DataSet dss[];
        
         if( DS == null ) 
             dss = null;
         else 
             dss = DS.getDataSets();
         
         if( opnum >=0 )
          { Operator opn = op.getOperator( opnum );  
                 
            if( opn instanceof IObservable)
               if( iobs != null)
                 ((IObservable)opn).addIObserver( iobs );
           /*  if( opn instanceof IusesStatusPane)
                 if( statPane !=null)
                  ((IusesStatusPane)opn).addStatusPane( statPane);
             else if(opn instanceof java.beans.Customizer)
                if( statPane != null)
                  ((java.beans.Customizer)opn).addPropertyChangeListener( statPane);
             */
            JParametersDialog JP = new JParametersDialog(  opn, 
                                                           DS, 
                                                           logdoc, 
                                                           iobs,false,
                                                            statPane  );
             

          /*   if(opn instanceof java.beans.Customizer)
                if( statPane != null)
                  ((java.beans.Customizer)opn).removePropertyChangeListener( statPane);
           //done in JParametersDialog
             
            if( opn instanceof IusesStatusPane)
               ((IusesStatusPane)opn).addStatusPane( null);
          */
            if( opn instanceof IObservable)
               if( iobs != null)
                 ((IObservable)opn).deleteIObserver( iobs );
           }
          
       }
               

  }
private class MJMenuItem extends JMenuItem
 {int opnum;
   public MJMenuItem( String Title ,int opnum )
     {super(Title);
      this.opnum = opnum;
      }
   public int getopnum()
     {return opnum;
     }

  }
public static void main( String args[] )
  { java.util.Properties isawProp;
     isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
          isawProp.load( input );
	   // Script_Path = isawProp.getProperty("Script_Path");
         // Data_Directory = isawProp.getProperty("Data_Directory");
          //Default_Instrument = isawProp.getProperty("Default_Instrument");
	    //Instrument_Macro_Path = isawProp.getProperty("Instrument_Macro_Path");
	    //User_Macro_Path = isawProp.getProperty("User_Macro_Path");
          System.setProperties(isawProp);  
    //    System.getProperties().list(System.out);
          input.close();
       }
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }


    JFrame JF =  new JFrame();
   JF.setSize( 300,300);
   Script_Class_List_Handler SH = new Script_Class_List_Handler();
   System.out.println( "operators="+SH.getNum_operators());
   opMenu opm = new opMenu( SH, null , null, null );
   opm.setOpMenuLabel("Woops");
   JMenuBar bar= new JMenuBar();
   bar.add(opm);
   JF.setJMenuBar( bar );
   JF.show();
  }
}



