

package DataSetTools.viewer.Table;

import javax.swing.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
public class TableViewMenuComponents  
  { boolean error, index;
    JCheckBoxMenuItem jmErr=null; 
    JCheckBoxMenuItem jmInd=null;

   public TableViewMenuComponents( )
     {error = false;
      index = false;
  
     }
  public JCheckBoxMenuItem  getErrorItem()
     {jmErr = new JCheckBoxMenuItem("Show Errors");
      jmErr.addActionListener( new MyActionListener(0));
      return jmErr;
      }
  public JCheckBoxMenuItem  getIndexItem()
     {jmInd = new JCheckBoxMenuItem("Show Indicies");
      jmInd.addActionListener( new MyActionListener(1));
      return jmInd;
      }
  public boolean getErrors()
    {return error;}

  public boolean getIndex()
    {return index;}

  public static int getNMenuItems()
    { return 3;
    }
   public static String getNameMenuItem( int i)
    { if( i < 0)
         return null;
      if( i == 0)
         return "Gr,Time vs Field";
      if( i == 1)
         return "Time vs Gr,Field";
      if( i == 2 )
         return "Time,Row vs Col";
      else return null;
    }

 public void addMenuItems( JMenu Tables , ActionListener view_menu_handler)
   { JMenuItem button;
    for( int i = 0; i < getNMenuItems(); i++) 
     {button = new JMenuItem( getNameMenuItem(i));
      button.addActionListener( view_menu_handler );
      Tables.add( button);
      }
    
    Tables.addSeparator();
    Tables.add( getErrorItem());
    Tables.add( getIndexItem());

   }
 public TableView getDataSetViewer( String view_type, DataSet DS, ViewerState state)
   {
    table_view tv= new table_view(0);
    String state1 = "X values;Y values;";
    if( error || (view_type.indexOf("err")>=0))
      state1 += "Error values;";
    if( index || (view_type.indexOf("indx") >=0))
      state1 += "XY index;";
   
    DataSet[] DSS = new DataSet[1];
    DSS[0] = DS;
    tv.setDataSets( DSS );
    tv.restoreState( state1);
    DefaultListModel LM = tv.getListModel();
   // for( int j = 0; j<LM.size();j++)
   //   System.out.println("LM+"+LM.elementAt(j));
    if( DS.getSelectedIndices().length<1)
       {DataSetTools.util.SharedData.addmsg("No data sets selected");
        return null;
       }
    if( view_type.indexOf("Gr,Time vs Field")==0)
      return new TableView( DS, state,tv.getGenTableModel( DS,LM,"HGT,F",DS.getSelectedIndices() ));
    if( view_type.indexOf("Time vs Gr,Field")==0)
       return new TableView( DS, state,tv.getGenTableModel( DS,LM,"HT,FG",DS.getSelectedIndices() ));
    if(view_type.indexOf("Time,Row vs Col")==0)
       return new TableView( DS, state,tv.getGenTableModel( DS,LM,"HTI,JF",DS.getSelectedIndices() ));
    
    return null;

   }
 class MyActionListener implements ActionListener
    {int action;
     public MyActionListener( int action)
       { this.action = action;}

     public void actionPerformed( ActionEvent evt )
       { 
        if( action ==0 ) // error handler
          { error = jmErr.getState();
            
           }
        else if( action == 1) //
          {index = jmInd.getState();
           
          }
  
        

        }
    }

   }
