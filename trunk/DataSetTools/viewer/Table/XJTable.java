package DataSetTools.viewer.Table;
import javax.swing.*;
import javax.swing.table.*;

public class XJTable extends JTable
  {
   public XJTable( TableViewModel tbl)
    {
      super( (TableModel)tbl);
     }

   public boolean isCellEditable(int row,
                              int column)
   {return false;
   }


  }
