package Command;

import javax.swing.*;
import DataSetTools.dataset.*;
import java.awt.event.*; 


public class opMenu extends JMenu 
{OperatorHandler op;
 MActionListener ML;
  DataSetListHandler DS;
public opMenu(OperatorHandler op , DataSetListHandler DS, Document logdoc , IObserver iobs)
  {this.op = op;
   this.DS = DS;
   int cat_index;
   int       comp_index;                       // index of submenu components
  int       num_components;                   // number of submenu components
  MJMenuItem comp;  
  String    categories[];
  MActionListener ML= new MActionListener( op , DS , logdoc, iobs);
                                                 // correct submenu
  for ( int i = 0; i < op.getNum_0perators; i++ )
  {
                                              // the list starts two entries, 
                                              // "Operator", "DataSetOperator"
                                              // that we ignore.
    categories = op.getOperator[i].getCategoryList();
                                              // step down the category tree,
                                              // at each level, if we don't
                                              // find the current category,
                                              // add it.  
    JMenu current_menu = main_menu;           // current_menu pointer steps  
                                              // down the tree of menus
    for ( cat_index = 1; cat_index < categories.length; cat_index++ ) 
    {
       num_components = current_menu.getMenuComponentCount();
       boolean found = false;
       comp_index = 0;
       while ( comp_index < num_components && !found )
       {
         comp = current_menu.getItem( comp_index );
         if ( comp.getLabel().equalsIgnoreCase( categories[cat_index] ) )
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
    MJMenuItem item = new JMenuItem( operators[i].getTitle(),i );
    item.addActionListener( MActionListener );
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
    void actionPerformed(ActionEvent e) 
       { if( !(e insanceof MJMenuItem))
           return;
         MJMenuItem x =(MJMenuItem) e;
         int opnum = x.getopnum();
         if( opnum >=0 )
          { Operator opn = op.getOperator( opnum );
            if( opn instanceof IObservable)
               if( iobs != null)
                 opn.addIObserver( iobs );
            JParameterDialog JP= new JParametersDialog( op , DS, logdoc );
            opn.removeIObserver( iobs );
           }

       }

  }
private class MJMenuItem extends JMenuItem
 {int opnum;
   public MJMenuItem( int opnum )
     {super();
      this.opnum = opnum;
      }
   public int getopnum()
     {return opnum;
     }

  }
public static void main( String args[] )
  {JFrame JF =  new JFrame();
   JF.setSize( 300,300);
   Script_Class_List_Handler SH = new Script_Class_List_Handler();
   opMenu opm = new opm( SH, null , null, null )
   JMenuBar bar= new JMenuBAR();
   bar.add(opm);
   JF.setJMenuBar( bar );
   JF.show();
  }
}

