package Command;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import java.awt.event.*; 
import javax.swing.text.*;
import DataSetTools.operator.*;
import java.io.*;
public class opMenu extends JMenu 
{OperatorHandler op;
 MActionListener ML;
  DataSetListHandler DS;
public opMenu(OperatorHandler op , DataSetListHandler DS, Document logdoc , IObserver iobs)
  {super("Operations");
   this.op = op;
   this.DS = DS;
   int cat_index;
   int       comp_index;                       // index of submenu components
  int       num_components;                   // number of submenu components
  JMenuItem comp;  
  String    categories[];
  MActionListener ML= new MActionListener( op , DS , logdoc, iobs);
                                                 // correct submenu
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
    for ( cat_index = 1; cat_index < categories.length; cat_index++ ) 
    {
       num_components = current_menu.getMenuComponentCount();
       boolean found = false;
       comp_index = 0;
       while ( comp_index < num_components && !found )
       {
         comp = (JMenuItem)(current_menu.getItem( comp_index) );
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
         current_menu.add( new_menu );
         current_menu = new_menu;            // advance the current menu pointer
       }
    }
                                             // after stepping through the meun
                                             // tree, add the new operator title
    String Title=op.getOperator(i).getTitle();
    if( Title.equals("UNKNOWN"))
       Title = op.getOperator(i).getCommand();
    MJMenuItem item = new MJMenuItem( Title,i );
    item.addActionListener( ML );
    current_menu.add( item );
  }
   
  }//constructor

private class MActionListener implements ActionListener
  {OperatorHandler op;
   DataSetListHandler DS;
  Document logdoc;
  IObserver iobs;
    public MActionListener( OperatorHandler op,DataSetListHandler DS , Document logdoc, IObserver iobs)
       {this.op = op;
        this.DS = DS;
        this.logdoc=logdoc;  
        this.iobs=iobs;
       }
   public void actionPerformed(ActionEvent e) 
       { if( !(e.getSource() instanceof MJMenuItem))
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
            JParametersDialog JP= new JParametersDialog( opn , dss, logdoc );
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
	    FileInputStream input = new FileInputStream(path + "props.dat" );
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
   JMenuBar bar= new JMenuBar();
   bar.add(opm);
   JF.setJMenuBar( bar );
   JF.show();
  }
}


=======
package Command;

import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import java.awt.event.*; 
import javax.swing.text.*;
import DataSetTools.operator.*;
import java.io.*;
public class opMenu extends JMenu 
{OperatorHandler op;
 MActionListener ML;
  DataSetListHandler DS;
public opMenu(OperatorHandler op , DataSetListHandler DS, Document logdoc , IObserver iobs)
  {super("Operations");
   this.op = op;
   this.DS = DS;
   int cat_index;
   int       comp_index;                       // index of submenu components
  int       num_components;                   // number of submenu components
  JMenuItem comp;  
  String    categories[];
  MActionListener ML= new MActionListener( op , DS , logdoc, iobs);
                                                 // correct submenu
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
    for ( cat_index = 1; cat_index < categories.length; cat_index++ ) 
    {
       num_components = current_menu.getMenuComponentCount();
       boolean found = false;
       comp_index = 0;
       while ( comp_index < num_components && !found )
       {
         comp = (JMenuItem)(current_menu.getItem( comp_index) );
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
         current_menu.add( new_menu );
         current_menu = new_menu;            // advance the current menu pointer
       }
    }
                                             // after stepping through the meun
                                             // tree, add the new operator title
    String Title=op.getOperator(i).getTitle();
    if( Title.equals("UNKNOWN"))
       Title = op.getOperator(i).getCommand();
    MJMenuItem item = new MJMenuItem( Title,i );
    item.addActionListener( ML );
    current_menu.add( item );
  }
   
  }//constructor

private class MActionListener implements ActionListener
  {OperatorHandler op;
   DataSetListHandler DS;
  Document logdoc;
  IObserver iobs;
    public MActionListener( OperatorHandler op,DataSetListHandler DS , Document logdoc, IObserver iobs)
       {this.op = op;
        this.DS = DS;
        this.logdoc=logdoc;  
        this.iobs=iobs;
       }
   public void actionPerformed(ActionEvent e) 
       { if( !(e.getSource() instanceof MJMenuItem))
           return;
         MJMenuItem x =(MJMenuItem)( e.getSource());
         int opnum = x.getopnum();
         if( opnum >=0 )
          { Operator opn = op.getOperator( opnum );
            if( opn instanceof IObservable)
               if( iobs != null)
                 ((IObservable)opn).addIObserver( iobs );
            JParametersDialog JP= new JParametersDialog( opn , DS.getDataSets(), logdoc );
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
	    FileInputStream input = new FileInputStream(path + "props.dat" );
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
   JMenuBar bar= new JMenuBar();
   bar.add(opm);
   JF.setJMenuBar( bar );
   JF.show();
  }
}


