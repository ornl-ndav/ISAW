package DataSetTools.viewer;
import DataSetTools.dataset.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
//import javax.swing.*;

public abstract class DataSetViewer extends JPanel
{
    /** The Data Set being viewed */
    protected DataSet dataSet;
    
    /** Title of DataSet being viewed*/
    protected String title;
    
    /** Viewer Menubar -- Note that it is the responsibility of the user of i
                          this class to add the MenuBar to whatever frame the 
                          viewer is placed in.*/

    protected JMenuBar menuBar;
    
    /** The menus */
    JMenu fileMenu = new JMenu("File");
    JMenu editMenu = new JMenu("Edit");
    JMenu optionsMenu = new JMenu("Options");
    
    
    /** Accepts a dataSet and title and creates an instance of a viewer
     *  for the data set.  
     */
    public DataSetViewer(DataSet dataSet, String title)
    {
        this.dataSet = dataSet;
        if (title != null)
        {
            this.title = title;
        }
        else
        {
          this.title = dataSet.getTitle();
        }
        //neccesary so popup menus show up above heavyweight containers
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        menuBar = new JMenuBar();
        menuBar.add(fileMenu); menuBar.add(editMenu); menuBar.add(optionsMenu);
        fileMenu.add("Exit"); editMenu.add("Delete Selected Data ... ");
        editMenu.add("Delete Unselected Data ... ");         
    }

    public String getTitle( )
    {
      return title;
    }
    
    public DataSet getDataSet()
    {
      return dataSet;
    }
    
    public JMenuBar getMenuBar(){ return menuBar; }

    public abstract int[] getSelectedIndices();
}
