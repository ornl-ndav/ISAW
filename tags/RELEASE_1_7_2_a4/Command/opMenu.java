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
 * Revision 1.36  2005/05/25 19:37:41  dennis
 * Replaced direct call to .show() method for window,
 * since .show() is deprecated in java 1.5.
 * Now calls WindowShower.show() to create a runnable
 * that is run from the Swing thread and sets the
 * visibility of the window true.
 *
 * Revision 1.35  2005/01/07 15:00:10  rmikk
 * Directories are now at the top and in alphabetical order at each level
 *
 * Revision 1.34  2005/01/02 18:01:31  rmikk
 * Added Tool tip to the Menu items that indicate the location of the source
 *     code
 *
 * Revision 1.33  2004/07/31 23:15:15  rmikk
 * Removed unused imports
 *
 * Revision 1.32  2004/06/18 19:16:39  rmikk
 * Eliminated an unused variable
 *
 * Revision 1.31  2004/03/17 20:25:09  dennis
 * Fixed @see tag that was broken when view components, math and
 * util were moved to gov package.
 *
 * Revision 1.30  2004/03/15 23:58:31  dennis
 * Changed some references to static methods to be through the
 * class name instead of an instance.
 *
 * Revision 1.29  2004/03/15 03:30:17  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.28  2003/12/14 19:18:10  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.27  2003/06/17 18:16:31  pfpeterson
 * Does not add Operators whose category list is [HiddenOperator]
 * (case insensitive).
 *
 * Revision 1.26  2003/06/02 22:31:35  rmikk
 * -Eliminated adding IObservers to an operator.  This is
 *  done in the JParametersDialog
 *
 * Revision 1.25  2003/05/28 18:53:46  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.24  2003/03/06 22:53:05  pfpeterson
 * Sets a boolean in Script_Class_List_Handler so scripts are not
 * reloaded when asked for during its operation. Also code cleanup
 * and some reformatting (sorry).
 *
 * Revision 1.23  2003/02/21 19:35:44  pfpeterson
 * Changed calls to fixSeparator appropriate (not deprecated) method.
 *
 * Revision 1.22  2002/11/27 23:12:10  pfpeterson
 * standardized header
 *
 * Revision 1.21  2002/08/19 17:09:37  pfpeterson
 * Switched AbstractButton.getLabel() to AbstractButton.getText()
 * so is no longer using deprecated api.
 *
 * Revision 1.20  2002/08/19 17:07:11  pfpeterson
 * Reformated file to make it easier to read.
 *
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
 */
package Command;

import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Sys.*;

import javax.swing.*;
//import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import java.awt.event.*; 
import javax.swing.text.*;
import DataSetTools.operator.*;
import java.io.*;

/**
 *  A Jmenu especially for lists of operators with a getCategorylist
 */
public class opMenu extends JMenu{
    OperatorHandler op;
    MActionListener ML;
    IDataSetListHandler DS;
    IObserver iobs;

    /**
     * @param op Gets the list of operators to be placed in this menu
     * @param DS Gets the list of Data Sets that can be used for
     * parameters
     * @param logdoc the log file to place log comments
     * @param iobs an iobserver of these operations
     * @see OperatorHandler 
     * @see DataSetTools.components.ParametersGUI.IDataSetListHandler 
     * @see gov.anl.ipns.Util.Messaging.IObserver
     */
    public opMenu(OperatorHandler op , IDataSetListHandler DS, Document logdoc,
                                                              IObserver iobs){
        this(op,DS,logdoc,iobs,1);
    }

    public opMenu(OperatorHandler op , IDataSetListHandler DS, 
                                  Document logdoc , IObserver iobs , int start){
        super("Operations");
        this.op=op;
        this.DS=DS;
        this.iobs=iobs;
        this.initt( logdoc, start);
    }

    private  void initt(Document logdoc, int start){
        int cat_index;
        int comp_index;                       // index of submenu components
        int num_components;                   // number of submenu components
        JMenuItem comp;  
        String    categories[];
        boolean found;
        MActionListener ML= new MActionListener( op , DS , logdoc, iobs);
                                                  // correct submenu
        found = false;  
        
        Operator myOperator=null;
        if( op instanceof Script_Class_List_Handler)
          Script_Class_List_Handler.reload_scripts=false;
        for ( int i = 0; i < op.getNum_operators(); i++ ){
                                               // the list starts two entries, 
                                               // "Operator", "DataSetOperator"
                                               // that we ignore.
          myOperator=op.getOperator(i);
          if( !( myOperator instanceof HiddenOperator) ){
            categories = myOperator.getCategoryList();
                                                // step down the category tree,
                                                // at each level, if we don't
                                                // find the current category,
                                                // add it.  
            if(categories.length==1
                         && categories[0].equalsIgnoreCase("HIDDENOPERATOR") ){
              continue; // don't do anything
            }

            JMenu current_menu = this;          // current_menu pointer steps  
                                                // down the tree of menus
            if( categories == null){
              categories = new String[1];
              categories[0]=Operator.OPERATOR; 
            }
            found = true;
            int insert_index;
            for( cat_index=start ; (cat_index < categories.length)&&(found) ; cat_index++ ){
              num_components = current_menu.getMenuComponentCount();
              found = false;
              comp_index = 0;
              insert_index = -1;
             
              while ( comp_index < num_components && !found ){
                comp = (JMenuItem)(current_menu.getItem( comp_index) );
                if(comp instanceof JMenu)
                  if( comp.getText().compareTo( categories[cat_index])< 0)
                     insert_index = comp_index+1;
                  if ( comp.getText().equalsIgnoreCase(categories[cat_index])){
                    found = true;
                    current_menu = (JMenu)comp;
                  }
                // we found the category, advance the current menu pointer
                comp_index++;
              }
              if ( !found ){ // if we don't find it, add it
                JMenu new_menu = new JMenu( categories[cat_index] );
                if( new_menu == null){
                  System.out.println("Could not create a JMenu"
                                     +cat_index+","+categories.length);
                  found = false;             
                }else{
                  int mNum = current_menu.getMenuComponentCount();
                  if(mNum <= 0)
                     current_menu.add( new_menu);
                  else if( (insert_index < 0) )
                     current_menu.add( new_menu, 0 );
                  
                  else if( insert_index < mNum )
                      current_menu.add( new_menu, insert_index );
                 
                  else 
                       current_menu.add( new_menu);
                       
                  current_menu = new_menu; // advance the
                                           // current menu
                                           // pointer
                  found = true;
                }
              }
            }
                                            // after stepping through the meun
                                            // tree, add the new operator title
            if( found){
              String Title=myOperator.getTitle();
              if( Title.equals("UNKNOWN"))
                Title = myOperator.getCommand();
              MJMenuItem item = new MJMenuItem( Title,i );
              if( item == null){
                System.out.println("Could not create a JMenuItem");
              }else{
                item.addActionListener( ML );
                String FileName = myOperator.getSource();
                if( FileName != null)
                   item.setToolTipText( FileName );
                current_menu.add( item );
              }
            }
          }
        }
        if( op instanceof Script_Class_List_Handler)
          Script_Class_List_Handler.reload_scripts=true;
    }//constructor

    public void setOpMenuLabel( String newText){
        setText( newText );
    }
    
    private class MActionListener implements ActionListener{
        OperatorHandler op;
        IDataSetListHandler DS;
        Document logdoc;
        IObserver iobs;

        public MActionListener( OperatorHandler op, IDataSetListHandler DS,
                                Document logdoc, IObserver iobs){
            this.op = op;
            this.DS = DS;
            this.logdoc=logdoc;  
            this.iobs=iobs;
        }

        public void actionPerformed(ActionEvent e){
            if( !(e.getSource() instanceof MJMenuItem))
                return;
            if( op == null ) 
                return;
            
            MJMenuItem x =(MJMenuItem)( e.getSource());
            int opnum = x.getopnum();
            
            //DataSet dss[];
            
            if( DS == null ) 
              {} // dss = null;
            else 
              {} //dss = DS.getDataSets();
            
            if( opnum >=0 ){
                Operator opn = op.getOperator( opnum );  
                 
               // if( opn instanceof IObservable)
                //    if( iobs != null)
                       // ((IObservable)opn).addIObserver( iobs );
                //JParametersDialog JP = 
                new JParametersDialog( opn, DS, logdoc, 
                                                              iobs,false);
               // if( opn instanceof IObservable)
                    //if( iobs != null)
                       // ((IObservable)opn).deleteIObserver( iobs );
            }
            
        }
    }

    private class MJMenuItem extends JMenuItem{
        int opnum;

        public MJMenuItem( String Title ,int opnum ){
            super(Title);
            this.opnum = opnum;
        }

        public int getopnum(){
            return opnum;
        }
    }

    public static void main( String args[] ){
        java.util.Properties isawProp;
        isawProp = new java.util.Properties(System.getProperties());
        String path = SharedData.getProperty("user.home")+"\\";
        path = StringUtil.setFileSeparator(path);
        try {
	    FileInputStream input = new FileInputStream(path+"IsawProps.dat");
            isawProp.load( input );
            System.setProperties(isawProp);  
            input.close();
        }catch (IOException ex){
            System.out.println("Properties file could not be loaded due "+
                               "to error :" +ex);
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
        WindowShower.show(JF);
    }
}
