package DataSetTools.viewer.Table;

import DataSetTools.dataset.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.viewer.*;
import IsawGUI.*;
import java.io.*;
import javax.swing.table.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;
import DataSetTools.util.*;

public class QuickTableGrInCols  extends STableView
  { boolean showerrs, showind;
    public QuickTableGrInCols( DataSet ds, ViewerState st)
      {super( ds, st, new DS_XY_TableModel( ds,
                           ds.getSelectedIndices(), false, false));
       showerrs=false;
       showind = false;
       }
    public TableViewModel fixTableModel( ViewerState state , TableViewModel table_model, 
                            boolean showerrors, boolean showIndices)
      { showerrs= showerrors;
        showind = showIndices;
        return new DS_XY_TableModel( ds,
                           ds.getSelectedIndices(), showerrors, showIndices);
      }
    public void redraw( String reason)
      {

       if( reason.equals(IObserver.POINTED_AT_CHANGED))
         super.redraw( reason);
       else if (reason.equals( IObserver.SELECTION_CHANGED) ||
               reason.equals( IObserver.DATA_REORDERED)||
               reason.equals( IObserver.DATA_DELETED) || 
               reason.equals( IObserver.DATA_CHANGED)||
               reason.equals( IObserver.GROUPS_CHANGED))
         { 
          table_model = new  DS_XY_TableModel( ds,
                           ds.getSelectedIndices(), showerrs, showind);
           jtb.setModel( table_model);
           jtb.repaint();
          

          }

      }
   }
