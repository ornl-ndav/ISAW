package DataSetTools.viewer;
import javax.swing.*;
import DataSetTools.dataset.*;
import DataSetTools.components.View.*;
import DataSetTools.components.View.OneD.*;
import java.awt.event.*;
import java.awt.*;
import DataSetTools.viewer.*;
import DataSetTools.components.containers.*;

import DataSetTools.viewer.*;

public class DataSetViewerMaker  extends DataSetViewer
  {
   DataSet ds;
   ViewerState state;
   IVirtualArray1D viewArray;
   FunctionViewComponent viewComp;

   public DataSetViewerMaker( DataSet ds, ViewerState state, IVirtualArray1D viewArray, 
                               FunctionViewComponent viewComp)
     {
      super( ds, state);
      this.viewArray = viewArray;
      this.viewComp = viewComp;
      this.ds = ds;
      this.state = state;
      //viewComp.setData( viewArray);
      JPanel East = new JPanel( new GridLayout( 1,1));
      
      BoxLayout blayout = new BoxLayout( East,BoxLayout.Y_AXIS);
     
      East.setLayout( blayout);
       JComponent[] ArrayScontrols =viewArray.getSharedControls();
      if( ArrayScontrols != null)
        for( int i=0; i< ArrayScontrols.length; i++)
          East.add( ArrayScontrols[i]);

      JComponent[] Arraycontrols =viewArray.getPrivateControls();
      if( Arraycontrols != null)
        for( int i=0; i< Arraycontrols.length; i++)
          East.add( Arraycontrols[i]);

      JComponent[] Compcontrols = viewComp.getSharedControls();
      if( Compcontrols != null)
        for( int i=0; i< Compcontrols.length; i++)
          East.add( Compcontrols[i]);    
      JComponent[] CompPcontrols = viewComp.getPrivateControls();
      if( CompPcontrols != null)
        for( int i=0; i< CompPcontrols.length; i++)
          East.add( CompPcontrols[i]);      
      viewArray.addActionListener( new ArrayActionListener());
      viewComp.addActionListener( new CompActionListener());
      setLayout( new GridLayout( 1,1));
      add( new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
          viewComp.getDisplayPanel(), East, .70f));
      invalidate();
 
 

     }
  public void redraw( String reason)
    {

    }
  public class ArrayActionListener  implements ActionListener
    {

     public void actionPerformed( ActionEvent evt)
       {



       }

     }

  public class CompActionListener implements ActionListener
    {

     public void actionPerformed( ActionEvent evt)
       {



       }



    }
 

  }//DataSetViewerMaker
