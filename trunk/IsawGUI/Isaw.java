/*
 * @(#)Isaw.java     1.0  99/09/02  Alok Chatterjee
 *
 * 1.0  99/09/02  Added the comments and made this a part of package IsawGUI
 * 
 */

package IsawGUI;

import DataSetTools.gsastools.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.JTree.*;
import java.util.*;
import java.util.EventObject.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import java.util.zip.*;
import DataSetTools.viewer.*;
import ChopTools.*;

/**
 * The main class for ISAW. It is the GUI that ties together the DataSetTools, IPNS, 
 * ChopTools and graph packages.
 *
 * @version 1.0  
 */

public class Isaw extends JFrame implements Serializable
{
    
    JTreeUI jtui;
    JPropertiesUI jpui;
    JDataViewUI jdvui;
    JCommandUI jcui;

    JMenu oMenu = new JMenu("Operations");
    JPopupMenu popup = new JPopupMenu();
    boolean set_selection = false ;
    public Isaw() 
    {
       super("ISAW");
       System.out.println("This is the new package");
       setupMenuBar();
       jtui = new JTreeUI();
       jtui.setPreferredSize(new Dimension(200, 500));
       jtui.setMinimumSize(new Dimension(20, 50));
       JTree tree = jtui.getTree();
       tree.addTreeSelectionListener(new TreeSelectionHandler());
       
       jpui = new JPropertiesUI();
       jpui.setPreferredSize( new Dimension(200, 200) );
       jpui.setMinimumSize(new Dimension(20, 50));
       
       jdvui = new JDataViewUI();
       jdvui.setPreferredSize(new Dimension(700, 500));
       SwingUtilities.updateComponentTreeUI(jdvui);

       jcui = new JCommandUI();
       jcui.setPreferredSize( new Dimension( 700, 50 ) );
       jcui.setMinimumSize(new Dimension(20, 50));
       
       JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
       JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
       
       rightPane.setTopComponent(jdvui);
       rightPane.setBottomComponent(jcui);
       leftPane.setBottomComponent(jpui);
       leftPane.setTopComponent(jtui);

       JSplitPane sp= new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                    leftPane, rightPane);
       
       sp.setOneTouchExpandable(true);
       Container con = getContentPane();
       con.add(sp);
    }//Isaw
    /**
    * Sets up the menubar that is used for all operations on DataSets
    * 
    */
    private void setupMenuBar() 
     {
       
        JMenuBar menuBar = new JMenuBar();
        JMenu fMenu = new JMenu("File");
        JMenu eMenu = new JMenu("Edit");
        JMenu vMenu = new JMenu("View");
        
        JMenu imageView = new JMenu("Image View");
        JMenu s_graphView = new JMenu("Scrolled Graph View");
        
        JMenu optionMenu = new JMenu("Options");
        JMenu wMenu = new JMenu("Window");
        JMenu hMenu = new JMenu("Help");
        
        JMenuItem fileRunfile = new JMenuItem("Load Entire Runfile(s)");
        JMenuItem fileRunfiles = new JMenuItem("Load Selected Data");
        JMenuItem fileLoadDataset = new JMenuItem("Load ISAW Data");
        JMenuItem fileSaveData = new JMenuItem("Save ISAW Data");
        JMenuItem fileSaveDataAs = new JMenuItem("Export GSAS File");
        JMenuItem imagePrint = new JMenuItem("Print to File");
        JMenuItem fileExit = new JMenuItem("Exit");

       
        JMenuItem removeSelectedNode = new JMenuItem("Remove Selected Node");
        removeSelectedNode.setAccelerator(KeyStroke.getKeyStroke('X', KeyEvent.CTRL_MASK, true));
        JMenuItem editAttributes = new JMenuItem("Edit Attributes");
        JMenuItem editSetAttribute = new JMenuItem("Set Attributes");
        JMenuItem setGroupAttributes = new JMenuItem("Set Attribute For All Groups");
        JMenuItem viewFileSeparator =  new JMenuItem("File Separator");
        JMenuItem viewLogView =  new JMenuItem("Log View");
        
        JMenuItem optionwindowsLook =  new JMenuItem("Windows Look");
        JMenuItem optionmetalLook =  new JMenuItem("Metal Look");
        JMenuItem optionmotifLook =  new JMenuItem("Motif Look");
        
        JMenuItem windowRestoreView =  new JMenuItem("Restore Views");
        JMenuItem windowMinimizeView =  new JMenuItem("Minimize Views");
        JMenuItem windowMaximizeView =  new JMenuItem("Maximize Views");
        JMenuItem windowCloseView =  new JMenuItem("Close Views");
        JMenuItem windowtileView = new JMenuItem("Tile Views Vertically");
        JMenuItem windowCascadeView = new JMenuItem("Cascade Views");
        JMenuItem helpISAW = new JMenuItem("About ISAW");
        
        JMenuItem iFrame = new JMenuItem("Internal Frame");
        JMenuItem eFrame = new JMenuItem("External Frame");

        JMenuItem graphView = new JMenuItem("Graph View");
        
        JMenuItem iFrame_sg = new JMenuItem("Scrolled Graph Internal Frame");
        JMenuItem eFrame_sg = new JMenuItem("Scrolled Graph External Frame");
    
        fMenu.add(fileRunfile);
        fMenu.add(fileRunfiles);
        fMenu.add(fileLoadDataset);
        fMenu.addSeparator();
        fMenu.add(fileSaveData);
        fMenu.add(fileSaveDataAs);
        //fMenu.add(imagePrint);
        fMenu.addSeparator();
        fMenu.add(fileExit);

        eMenu.add(removeSelectedNode);
        eMenu.add(editAttributes);
        eMenu.add(editSetAttribute);
        eMenu.add(setGroupAttributes);
          
        imageView.add(iFrame);
        imageView.add(eFrame);
        
        s_graphView.add(iFrame_sg);
        s_graphView.add(eFrame_sg);
        
        vMenu.add(imageView);
        vMenu.add(s_graphView);
        vMenu.add(graphView);
        vMenu.add(viewFileSeparator);
        vMenu.add(viewLogView); 
        
          
        optionMenu.add(optionwindowsLook);
        optionMenu.add(optionmetalLook);
        optionMenu.add(optionmotifLook);
        
        wMenu.add(windowRestoreView);
        wMenu.add(windowMinimizeView);
        wMenu.add(windowMaximizeView);
        wMenu.add(windowCloseView);
        wMenu.add(windowCascadeView);
        wMenu.add(windowtileView);
        
        
        hMenu.add(helpISAW);
        fileExit.addActionListener(new MenuItemHandler());
        fileRunfile.addActionListener(new LoadMenuItemHandler());
        fileRunfiles.addActionListener(new LoadMenuItemHandler());

        fileSaveData.addActionListener(new MenuItemHandler());
        fileSaveDataAs.addActionListener(new MenuItemHandler());
        imagePrint.addActionListener(new MenuItemHandler());
        
        graphView.addActionListener(new MenuItemHandler()); 
        
        s_graphView.addActionListener(new MenuItemHandler()); 
        iFrame_sg.addActionListener(new MenuItemHandler()); 
        eFrame_sg.addActionListener(new MenuItemHandler()); 
        
        imageView.addActionListener(new MenuItemHandler()); 
        iFrame.addActionListener(new MenuItemHandler()); 
        eFrame.addActionListener(new MenuItemHandler()); 
        
        viewFileSeparator.addActionListener(new MenuItemHandler());
        viewLogView.addActionListener(new MenuItemHandler());
        optionmetalLook.addActionListener(new MenuItemHandler());
        optionmotifLook.addActionListener(new MenuItemHandler());
        optionwindowsLook.addActionListener(new MenuItemHandler());
        
        fileLoadDataset.addActionListener(new MenuItemHandler());
        
        removeSelectedNode.addActionListener(new MenuItemHandler());
        editAttributes.addActionListener(new AttributeMenuItemHandler());
        editSetAttribute.addActionListener(new AttributeMenuItemHandler());
        setGroupAttributes.addActionListener(new AttributeMenuItemHandler());
        
        windowRestoreView.addActionListener(new MenuItemHandler());
        windowMinimizeView.addActionListener(new MenuItemHandler());
        windowMaximizeView.addActionListener(new MenuItemHandler());
        windowCloseView.addActionListener(new MenuItemHandler());
        windowCascadeView.addActionListener(new MenuItemHandler());
        windowtileView.addActionListener(new MenuItemHandler());
        helpISAW.addActionListener(new MenuItemHandler());
       
        menuBar.add(fMenu);
        menuBar.add(eMenu);
        menuBar.add(vMenu);
        menuBar.add(optionMenu);
        menuBar.add(oMenu);
        menuBar.add(wMenu);
        menuBar.add(hMenu);
        
        setJMenuBar(menuBar);
     }
     
         
   private class AttributeMenuItemHandler implements ActionListener 
   {
        public void actionPerformed(ActionEvent ev) 
        
        { 
            String s=ev.getActionCommand();
            if(s=="Edit Attributes")
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                   
                    JTree tree = jtui.getTree();
                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                    
                     if(  mtn.getUserObject() instanceof DataSet || 
                          mtn.getUserObject() instanceof Data    )
                     
                      {
                         Object obj = mtn.getUserObject();

                        JAttributesDialog  jad = new JAttributesDialog(((IAttributeList)obj).getAttributeList(), s);
                        ((IAttributeList)obj).setAttributeList(jad.getAttributeList());
                        
                      }
                }
                
                if(s=="Set Attributes")
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                   
                    JTree tree = jtui.getTree();
                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                    
                     if(  mtn.getUserObject() instanceof DataSet || 
                          mtn.getUserObject() instanceof Data    )
                     
                      {
                        Object obj = mtn.getUserObject();
                        AttributeList new_list = makeNewAttributeList();
                        JAttributesDialog  jad = new JAttributesDialog(new_list,s);
                        AttributeList current_list = ((IAttributeList)obj).getAttributeList();
                        new_list = jad.getAttributeList();
                        
                        for (int i = 0; i<new_list.getNum_attributes(); i++)
                        {
                           Attribute attr = new_list.getAttribute(i);
                           
                   //Add more instances add following code here later----
                           if(attr instanceof FloatAttribute)
                           {
                            float val = ((FloatAttribute)attr).getFloatValue();
                            if(!Float.isNaN(val))
                               current_list.setAttribute(attr);
                           }
                        }
                        ((IAttributeList)obj).setAttributeList(current_list);
                        
                      }
                }
                
                if(s=="Set Attribute For All Groups")
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                   
                    JTree tree = jtui.getTree();
                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                    
                     if(  mtn.getUserObject() instanceof DataSet  ) 
                      {
                        Object obj = mtn.getUserObject();
                        DataSet ds = (DataSet)obj;
                        AttributeList new_list = makeNewAttributeList();
                        JAttributesDialog  jad = new JAttributesDialog(new_list,s);
                        new_list = jad.getAttributeList();
                        
                        for (int k=0; k<ds.getNum_entries(); k++)
                        {
                            Data data = ds.getData_entry(k);
                            AttributeList current_list = data.getAttributeList();
                            for (int i = 0; i<new_list.getNum_attributes(); i++)
                                {
                                Attribute attr = new_list.getAttribute(i);
                                   
                        //Add more instances and following code here later----
                                if(attr instanceof FloatAttribute)
                                {   float val = ((FloatAttribute)attr).getFloatValue();
                                    if(!Float.isNaN(val))
                                    current_list.setAttribute(attr);
                                }
                            }
                            data.setAttributeList(current_list);
                        } 
                      }
                }
                
       }
   }
     
   private AttributeList makeNewAttributeList()
   {
        AttributeList new_list = new AttributeList();
        FloatAttribute attr = new FloatAttribute(Attribute.TEMPERATURE, Float.NaN);
        new_list.addAttribute(attr);
        attr = new FloatAttribute(Attribute.PRESSURE, Float.NaN);
        new_list.addAttribute(attr);
        attr = new FloatAttribute(Attribute.MAGNETIC_FIELD, Float.NaN);
        new_list.addAttribute(attr);
        return new_list;
   }
   
   
   private class LoadMenuItemHandler implements ActionListener 
   {
        public void actionPerformed(ActionEvent ev) 
        
        {
            String s=ev.getActionCommand();
              
                if(s=="Load Entire Runfile(s)")
                {
                    try
                    { 
                        LoadFiles db = new LoadFiles(jtui);
                        db.setSize(new Dimension(550,300));
                        db.show();    
                    }
                    catch (Exception e){System.out.println("Choose a input file: ");}
                }
                if(s=="Load Selected Data")
                {
                    try
                    {        
                        ListFiles db = new ListFiles(jtui);
                        db.setSize(new Dimension(550,300));
                        db.show();         
                    }
                    catch (Exception e){System.out.println("Choose a input file: ");}
                }
                    
        } 
   }
   
   private class MenuItemHandler implements ActionListener 
   {
        FileDialog fd = new FileDialog(new Frame(), "Please choose the File to open", FileDialog.LOAD);
          
        public void actionPerformed(ActionEvent ev) 
        
        {
            String s=ev.getActionCommand();
            
                if(s=="Exit")
                {
                    System.exit(0);
                }
                
                if(s=="Print to File")
                {
                    jdvui.printImage();
                }
                
             /*   if(s=="Load Entire Runfile(s)")
                { 
                    try
                    {
                       fd.setDirectory("H:\\UPLOAD\\DATA");
                        fd.setDirectory("C:");
                        fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());
                       
                        String filename =f.toString();
                        String ff = fd.getFile();
   
                        RunfileRetriever r = new RunfileRetriever(filename);
                        
                        int numberOfDataSets = r.numDataSets();
                        DataSet[] dss = new DataSet[numberOfDataSets];

                        for (int i = 0; i< numberOfDataSets; i++)
                            dss[i] = r.getDataSet(i);
                            System.out.println("Tree is : " +jtui.getTree()); 
                            jtui.addDataSets(dss, ff);   
                    } 
                    catch (Exception e){System.out.println("Choose a input file: ");}
                    LoadFiles db = new LoadFiles(jtui);
                                db.setSize(new Dimension(550,300));
                                db.show();    
                    
                   
                }
                
                if(s=="Load Selected Data")
                {        
                                ListFiles db = new ListFiles(jtui);
                                db.setSize(new Dimension(550,300));
                                db.show();         
                }
               */ 
                if(s == "Windows Look")
                {
                    try
                    {
                        UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
                        SwingUtilities.updateComponentTreeUI(jtui);
                        SwingUtilities.updateComponentTreeUI(jpui);
                        SwingUtilities.updateComponentTreeUI(jdvui);
                        SwingUtilities.updateComponentTreeUI(jcui);
                    
                    }
                    catch(Exception e){ System.out.println("ERROR: setting windows look"); }
                }
                if(s == "Metal Look")
                {
                    try
                    {
                        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                        SwingUtilities.updateComponentTreeUI(jtui);
                        SwingUtilities.updateComponentTreeUI(jpui);
                        SwingUtilities.updateComponentTreeUI(jdvui);
                        SwingUtilities.updateComponentTreeUI(jcui);
                    }
                    catch(Exception e){System.out.println("ERROR: setting metal look "); }
                }
                    if(s == "Motif Look")
                    {
                        try
                        {
                            UIManager.setLookAndFeel("javax.swing.plaf.motif.MotifLookAndFeel");
                            SwingUtilities.updateComponentTreeUI(jtui);
                            SwingUtilities.updateComponentTreeUI(jpui);
                            SwingUtilities.updateComponentTreeUI(jdvui);
                            SwingUtilities.updateComponentTreeUI(jcui);
                        }
                        catch(Exception e){System.out.println("ERROR: setting motif look" ); }
                    }
                   
                   if(s == "Save ISAW Data")
                    {
                        DefaultMutableTreeNode dn = jtui.getSelectedNode();
                        
                        try
                        {
                            FileDialog fc = new FileDialog(new Frame(), "Please choose the File to save", FileDialog.SAVE);
                            //JFileChooser fd = new JFileChooser();
                            // fd.setCurrentDirectory(null);
                            //fc.setDirectory("C:\\");
                            fc.show();
                            // int state  = fd.showSaveDialog(null);
                            // File f = fd.getSelectedFile();
                            File f = new File(fc.getDirectory(), fc.getFile()+".dsz");
                            //System.out.println("cccc"+f.toString());
                            
                            FileOutputStream fos = new FileOutputStream(f);
                            GZIPOutputStream gout = new GZIPOutputStream(fos);
                            ObjectOutputStream oos = new ObjectOutputStream(gout);
                            oos.writeObject(dn);
                            oos.close();
                        }
                        catch(Exception e){System.out.println("Choose a DataSet to Save");}   
                      }
                      
                      if(s=="Load ISAW Data")
                      {
                        
                        try
                        {
                           // FileDialog fd = new FileDialog(new Frame(), "Please choose the File to load", FileDialog.LOAD);
                            //JFileChooser fd = new JFileChooser(new Frame(), "Please choose the File to load", FileDialog.LOAD);
                            //JFileChooser fd = new JFileChooser("H:\\UPLOAD\\Data");
                            //int state = fd.showOpenDialog(null);
                            // fd.addChoosableFileType("Datasets(*.ds)");
                            //File f = fd.getSelectedFile();
                            
                            //fd.setDirectory("C:\\");
                            fd.show();
                
                            File f = new File(fd.getDirectory(), fd.getFile());
                            String filename = fd.getFile();
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            FileInputStream fis = new FileInputStream(f);
                            GZIPInputStream gin = new GZIPInputStream(fis);
                            ObjectInputStream ois = new ObjectInputStream(gin);
                            
                            DefaultMutableTreeNode dn = (DefaultMutableTreeNode)ois.readObject();
                            ois.close();
                           
                            //if(
                         DataSet ds = (DataSet) dn.getUserObject();
                            //add dn to to the tree as a child of the root
                            jtui.openDataSet(ds, filename);
                        }
                        catch(Exception e){System.out.println("Choose a input DataSet");} 
                      }
                      
                      if(s == "Log View")
                      {
                            DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                            if (mtn.getUserObject() instanceof DataSet)
                            { 
                                DataSet ds = (DataSet)mtn.getUserObject();
                                JTree logTree =  jcui.showLog(ds);
                                logTree.hasFocus();
                            }
                            else {
                                System.out.println("View is Selected");
                                IsawViewHelp("No DataSet selected");
                                }
                     }
                     
                     if(s == "File Separator")
                      {
                        System.out.println("inside the menuitem");
                           FileSeparator fs = new FileSeparator();
                           fs.setSize(700,700);
                           fs.setVisible(true);
                     }
                
                if(s == "Export GSAS File")
                {

                    FileDialog fc = new FileDialog(new Frame(), "Please choose the File to save", FileDialog.SAVE);
                    //fc.setDirectory("H:\\UPLOAD\\DATA");
                    fc.show();
                    File f = new File(fc.getDirectory(), fc.getFile());
                    String filename =f.toString();
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                    DataSet ds = (DataSet)mtn.getUserObject();
                    gsas_filemaker.gsasfilemaker(ds, filename);
                 }
    
                if(s=="Internal Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                      
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                    if(  mtn.getUserObject() instanceof DataSet)
                    {
                        DataSet ds = (DataSet)mtn.getUserObject();
                        jdvui.drawImage(ds,"Internal Frame");
                        jpui.showAttributes(ds.getAttributeList());
                    }
                      
                      
                    else if(  mtn.getUserObject() instanceof Data)
                  
                    {
                       
                        Data data = (Data)mtn.getUserObject();
                        
                        DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                        DataSet ds = (DataSet)parent.getUserObject();
                        //DataSet ds = (DataSet)mtn.getParent().getParent();
                        jdvui.drawImage(ds,"Internal Frame");
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
                                IsawViewHelp("No DataSet selected");
                            
                            }
                  
                }
                
                 if(s=="External Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                    if(mtn.getUserObject() instanceof DataSet)
                    {
                        DataSet ds = (DataSet)mtn.getUserObject();
                        jdvui.drawImage(ds,"External Frame");
                       
                        jpui.showAttributes(ds.getAttributeList());
                    }
                      
                    else if(mtn.getUserObject() instanceof Data)
                    {
                        Data data = (Data)mtn.getUserObject();
                        DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                        DataSet ds = (DataSet)parent.getUserObject();
                      //DataSet ds = (DataSet)mtn.getUserObject();
                        jdvui.drawImage(ds,"External Frame");
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
                                IsawViewHelp("No DataSet selected");
                         }
                  
                }
                
           /*     if(s=="Graph Internal Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is inside GIFRAME"  +mtn.getUserObject());
                    if(  mtn.getUserObject() instanceof DataSet)
                    {
                        
                        DataSet ds = (DataSet)mtn.getUserObject();
                        
                        chop_MacroTools fg = new chop_MacroTools();
                        GraphFrame ff = (GraphFrame)fg.drawAlldata (ds);
                        JInternalFrame f = new JInternalFrame();
                        f.setBounds(0,0,600,400);
                        f.getContentPane().add(ff);
                        f.setVisible(true);
                        jdvui.add(f);   
                    }
                    if(  mtn.getUserObject() instanceof Data)
                    {
                        Data data = (Data)mtn.getUserObject();
                        jpui.showAttributes(data.getAttributeList());
                    }
                  
                }
            */ 
                
                 if(s=="Graph View" )
                {   
                    
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                    if(  mtn.getUserObject() instanceof DataSet)
                    {
                        
                        DataSet ds = (DataSet)mtn.getUserObject();
                        
                         chop_MacroTools fg = new chop_MacroTools();
                            fg.drawAlldata (ds);  
                    }
                        
                    else if(  mtn.getUserObject() instanceof Data)
                    {
                        //DataSet ds  = (DataSet) mtn.getParent();
                        DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                        DataSet ds = (DataSet)parent.getUserObject();
                        
                         chop_MacroTools fg = new chop_MacroTools();
                           fg.drawAlldata (ds); 
                        Data data = (Data)mtn.getUserObject();
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
                                IsawViewHelp("No DataSet selected");
                         }
                  
                }
                
                if(s=="Scrolled Graph Internal Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                    if(  mtn.getUserObject() instanceof DataSet)
                    {
                        DataSet ds = (DataSet)mtn.getUserObject();
                           GraphView graph_view =  new GraphView(ds);
                          JInternalFrame f = new JInternalFrame();
                        f.setBounds(0,0,600,400);
                        f.setClosable(true);
                        f.setResizable(true);
                        f.setMaximizable(true);
                        f.setIconifiable(true);
                        f.getContentPane().add(graph_view);
                        f.setVisible(true);
                        jdvui.add(f); 
                    }
                    
                    
                    
                   else if(  mtn.getUserObject() instanceof Data)
                    {
                        DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                        DataSet ds = (DataSet)parent.getUserObject();
                       // DataSet ds = (DataSet)mtn.getUserObject();
                        GraphView graph_view =  new GraphView(ds);
                        JInternalFrame f = new JInternalFrame();
                        f.setBounds(0,0,600,400);
                        f.setClosable(true);
                        f.setResizable(true);
                        f.setMaximizable(true);
                        f.setIconifiable(true);
                        f.getContentPane().add(graph_view);
                        f.setVisible(true);
                        jdvui.add(f); 
                    }
                    else {
                                System.out.println("View is Selected");
                                IsawViewHelp("No DataSet selected");
                         }
                }
                
                if(s=="Scrolled Graph External Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                    if(  mtn.getUserObject() instanceof DataSet)
                    {
                        DataSet ds = (DataSet)mtn.getUserObject();
                           GraphView graph_view =  new GraphView(ds);
                        JFrame f = new JFrame("Test for GraphView class");
                        f.setBounds(0,0,600,400);
                       
                       // f.setJMenuBar( graph_view.getMenuBar() );
                        f.getContentPane().add(graph_view);
                        f.setVisible(true);
                     
                    }                   
                    else if(  mtn.getUserObject() instanceof Data)
                    {
                        DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                        DataSet ds = (DataSet)parent.getUserObject();
                        GraphView graph_view =  new GraphView(ds);
                        JFrame f = new JFrame("Test for GraphView class");
                        f.setBounds(0,0,600,400);
                       
                       // f.setJMenuBar( graph_view.getMenuBar() );
                        f.getContentPane().add(graph_view);
                        f.setVisible(true);
                        
                        Data data = (Data)mtn.getUserObject();
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
                                IsawViewHelp("No DataSet selected");
                         }
                  
                }
                
                
                if(s=="Restore Views")
                {    
                    jdvui.openAll();
                } 
                
                if(s=="Minimize Views")
                {   
                    jdvui.closeAll();
                }
                
                 if(s=="Maximize Views")
                {   
                    jdvui.MaxAll();
                } 
                
                if(s=="Tile Views Vertically")
                {   
                    jdvui.tile_Vertically();
                }
                
                 if(s=="Cascade Views")
                {   
                    jdvui.cascade();
                }
                if(s=="Close Views")
                {   
                    jdvui.closeViews();
                }
                
                if(s=="About ISAW")
                {
                      JFrame hh = new JFrame("ISAW Help");
                      hh.setSize(new Dimension(500,400));
               
                             
                            //Center the opdialog frame 
	                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	                        Dimension size = hh.getSize();
	                        screenSize.height = screenSize.height/2;
	                        screenSize.width = screenSize.width/2;
	                        size.height = size.height/2;
	                        size.width = size.width/2;
	                        int y = screenSize.height - size.height;
	                        int x = screenSize.width - size.width;
	                        hh.setLocation(x, y);

 
               JTextArea textArea = new JTextArea(
               
               "\n"+"Mouse Operations on Image and Graph views: "+    
                "\n"+"                    Any region on the Image or Graph views can be zoomed into"+
                "\n"+"                    or zoomed out by making a rectangular region with middle button"+
                "\n"+"                    on a three button mouse or in the case of a two button mouse"+
                "\n"+"                    a region is traced out by holding the two button down simultaneously."+
                "\n"+"                    Zooming out requires a double click with the left mouse button."+
                "\n"+"                    For the Graph View menu item 'c' gives the cordinates of the cursor on the graph"+
                "\n"+"                    and 'Shift+R' returns one to the previous zoom level. Key 'm' allows"+
                "\n"+"                    a range selection for the graph displayed."+
                "\n"+
                
                 
                "\n"+"Add a Scalar:"+    
                "\n"+"                    This operator adds a constant value to all y-values of all"+
                "\n"+"                    Data blocks in a data set and places the result in a new"+
                "\n"+"                    DataSet."+"\n"+

                "\n"+"Subtract a Scalar:"+
                "\n"+"                   Similar to 'Add a Scalar', except the constant value is"+
                "\n"+"                   subtracted from each y-value."+"\n"+

                "\n"+"Multiply by Scalar:"+
                "\n"+"                   Similar to 'Add a Scalar', except the constant value is"+
                "\n"+"                  multiplied times each y-value."+"\n"+

                "\n"+"Divide by Scalar:"+
                "\n"+"                   Similar to 'Add a Scalar', except the constant value is"+
                "\n"+"                   divided into each y-value."+"\n"+
                  
                  
                "\n"+"Add a DataSet:"+
                "\n"+"                   This operator adds two DataSets by adding the corresponding"+
                "\n"+"                   Data blocks in the DataSets to form a new DataSet.  A Data"+
                "\n"+"                   block in the current DataSet will be added to the first Data"+
                "\n"+"                   block in the second DataSet with the same Group ID provided"+
                "\n"+"                   that they have the same units, the same number of data values"+
                "\n"+"                   and extend over the same X-interval."+"\n"+

                "\n"+"Subtract a DataSet:"+
                "\n"+"                   Similar to 'Add a DataSet', except the Data blocks in the"+
                "\n"+"                   specified DataSet are subtracted from the corresponding"+
                "\n"+"                   Data blocks in the current DataSet."+"\n"+

                "\n"+"Multiply by a DataSet:"+
                "\n"+"                   Similar to 'Add a DataSet', except the Data blocks in the"+
                "\n"+"                   specified DataSet are multiplied times the corresponding"+
                "\n"+"                   Data blocks in the current DataSet."+"\n"+

                "\n"+"Divide by a DataSet:"+
                "\n"+"                   Similar to 'Add a DataSet', except the Data blocks in the"+
                "\n"+"                   specified DataSet are divided into the corresponding"+
                "\n"+"                   Data blocks in the current DataSet."+"\n"+

                "\n"+"Integrate:"+
                "\n"+"                   This operator calculates the integral of the data values"+
                "\n"+"                   of one Data block.  The Group ID of the Data block to be"+
                "\n"+"                   integrated is specified by the parameter 'Group ID'.  The"+
                "\n"+"                   interval [a,b] over which the integration is done is"+
                "\n"+"                   specified by the two endpoints a, b where it is assumed that"+
                "\n"+"                   a < b.  This operator just produces a numerical result that"+
                "\n"+"                   is displayed in the operator dialog box."+"\n"+

                "\n"+"Calculate Moment:"+
                "\n"+"                   This operator calculates the integral of the data values"+
                "\n"+"                   times a power of x.  The Group ID of the Data block to be"+
                "\n"+"                   integrated is specified by the parameter 'Group ID'.  The"+
                "\n"+"                   power of x is specified by the parameter 'Moment'.  The"+
                "\n"+"                   interval [a,b] over which the integration is done is"+
                "\n"+"                   specified by the two endpoints a, b where it is assumed that"+
                "\n"+"                   a < b.  This operator just produces a numerical result that"+
                "\n"+"                   is displayed in the operator dialog box."+"\n"+

                "\n"+"Integrated Cross Section:"+
                "\n"+"                   This operator integrates each Data block in a DataSet over"+
                "\n"+"                   a specified interval [a,b] and forms a new DataSet with one"+
                "\n"+"                   entry: a Data block whose value at each of the original Data"+
                "\n"+"                   blocks is the value of the integral for the original Data"+
                "\n"+"                   block.  The new Data block will have an X-Scale taken from"+
                "\n"+"                   an attribute of one of the original Data blocks.  The"+
                "\n"+"                   integral values will be ordered according to increasing"+
                "\n"+"                   attribute value.  If several Data blocks have the same value"+
                "\n"+"                   of the attribute, their integral values are averaged.  For"+
                "\n"+"                   example, if the 'Raw Detector Angle' attribute is used and"+
                "\n"+"                   several Data blocks have the same angle value, the integral"+
                "\n"+"                   values for that angle are averaged to form the y-value that"+
                "\n"+"                   corresponds to that angle in the new Data block."+"\n"+

                "\n"+"Sort:"+
                "\n"+"                   This operator constructs a new DataSet that contains the"+
                "\n"+"                   same Data blocks as the original DataSet, but ordered by"+
                "\n"+"                   some attribute of the Data blocks.  For example, if the"+
                "\n"+"                   attribute chosen is the 'Raw Detector Angle', the Data blocks"+
                "\n"+"                   can be ordered in increasing or decreasing order based on"+
                "\n"+"                   the physical angle of the first detector in the group for"+
                "\n"+"                   that Data block.  On the other hand, if the 'Detector Position'"+
                "\n"+"                   attribute is used, the Data blocks can be ordered"+
                "\n"+"                   based on the effective scattering angle to which the data"+
                "\n"+"                   was time-focused.  In this case a group at -30 degrees and"+
                "\n"+"                   .1 meter above the beam plane and another group at +30"+
                "\n"+"                   degrees and .1 meter above or below the beam plane would"+
                "\n"+"                   appear next to each other since the scattering angle for"+
                "\n"+"                   each group is the same."+

                "\n"+"                   Although the Data blocks can be sorted based on any"+
                "\n"+"                   attribute, the most useful attributes are probably 'Group ID',"+
                "\n"+"                   'Detector Position', 'Raw Detector Angle' or user specified"+
                "\n"+"                   attributes such as temperature or pressure."+"\n"+

                "\n"+"Merge:"+
                "\n"+"                  This operator creates a new DataSet by combining the Data"+
                "\n"+"                  blocks from the current DataSet with the Data blocks of"+
                "\n"+"                  a specified DataSet.  This can only be done if the two"+
                "\n"+"                  DataSets have the same X and Y units.  If a DataSet with"+
                "\n"+"                  N Data blocks is merged with a DataSet with M Data blocks,"+
                "\n"+"                  the new DataSet will have N+M Data blocks."+"\n"+

                "\n"+"Convert to Energy Loss:"+
                "\n"+"                   This operator creates a new DataSet from a time-of-flight"+
                "\n"+"                   DataSet for direct geometry spectrometer.  The new DataSet"+
                "\n"+"                   contains Data blocks giving counts vs"+"\n"+

                "\n"+"                   energy loss = energy_in - energy( tof )."+"\n"+

                "\n"+"Convert to d-Spacing:"+
                "\n"+"                   This operator creates a new DataSet from a time-of-flight"+
                "\n"+"                   DataSet for diffractometer.  The new DataSet contains Data"+
                "\n"+"                   blocks giving counts vs d-Spacing."+"\n"+

                "\n"+" Convert to Q:"+
                "\n"+"                    This operator creates a new DataSet from a time-of-flight"+
                "\n"+"                    DataSet for diffractometer.  The new DataSet contains Data"+
                "\n"+"                    blocks giving counts vs Q."+"\n"+

                "\n"+" Convert to Energy:"+
                "\n"+"                    This operator creates a new DataSet from a time-of-flight"+
                "\n"+"                    DataSet for either diffractometers or spectrometers.  The"+
                "\n"+"                    new DataSet contains Data blocks giving counts vs energy."+"\n"+

                "\n"+" Convert to Wavelength:"+
                "\n"+"                    This operator creates a new DataSet from a time-of-flight"+
                "\n"+"                    DataSet for either diffractometers or spectrometers.  The"+
                "\n"+"                    new DataSet contains Data blocks giving counts vs wavelength.");
             
           textArea.setLineWrap(true);
                
           JScrollPane helpScroll = new JScrollPane(textArea);
       
         
           hh.getContentPane().add(helpScroll);
           
           hh.setVisible(true);
                } 
                
                 if(s=="Remove Selected Node")
                {  
                    DefaultMutableTreeNode dmtn = null;
	                TreePath[] paths = null;
	                JTree tree = jtui.getTree();
	                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	                TreePath[] tp = tree.getSelectionPaths();
	                System.out.println("The selected files are in JTREEUI "+tp.length );
	                for (int i=0; i<tp.length; i++)
	                {
                        dmtn = (DefaultMutableTreeNode)tp[i].getLastPathComponent();
                        System.out.println("The selected files are in JTREEUI " +dmtn.toString());
                        try
                        {
                               
                                if(  dmtn.getUserObject() instanceof DataSet)
                                {
                                    DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)dmtn.getParent();
                                    model.removeNodeFromParent(dmtn); 
                                    System.gc();
		                            System.runFinalization();
                                }
                                
                                else if(  dmtn.getUserObject() instanceof Data)
                                {
                                    DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)dmtn.getParent();
                                    DataSet ds = (DataSet)parent.getUserObject();
                                    int child_position = parent.getIndex(dmtn);
                                    System.out.println("THe child index is   :" +child_position);
                                    ds.removeData_entry(child_position);
                                    System.out.println("Removed from DS  :" +child_position);
                                    model.removeNodeFromParent(dmtn);
                                    System.gc();
		                            System.runFinalization();
                                    System.out.println("Removed from treemodel  :" +child_position);
                                }
                                
                                else if (dmtn.getLevel() == 1)
                                {
                                    model.removeNodeFromParent(dmtn);
                                    System.gc();
		                            System.runFinalization();
                                }
                                
                                else if (dmtn == null)
                                {
                                    System.out.println("Select a tree node to delete");
                                    System.gc();
		                            System.runFinalization();
                                }
                                repaint();
                            }
         
                        catch(Exception e){System.out.println("Select a tree node to delete"+e);}
                }
                
                }
          }
     }

    public void IsawViewHelp(String info)
    {
        
        JFrame mm = new JFrame();
        JDialog hh = new JDialog(mm, "ISAW View Help");
        hh.setSize(188,70);
        //Center the opdialog frame 
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension size = hh.getSize();
	    screenSize.height = screenSize.height/2;
	    screenSize.width = screenSize.width/2;
	    size.height = size.height/2;
	    size.width = size.width/2;
	    int y = screenSize.height - size.height;
	    int x = screenSize.width - size.width;
	    hh.setLocation(x-200, y-200);
        JTextArea textArea = new JTextArea(info);
        textArea.setLineWrap(true);
                
        JScrollPane helpScroll = new JScrollPane(textArea);
        hh.getContentPane().add(helpScroll);   
        hh.setVisible(true);
    }

    private class TreeSelectionHandler implements TreeSelectionListener
    {
        public void valueChanged(TreeSelectionEvent e)
        {
              JTree tree = jtui.getTree();        
              if(tree.getSelectionCount() < 1) return;
                                              
              DefaultMutableTreeNode mtn = jtui.getSelectedNode();
              if(  mtn.getUserObject() instanceof DataSet)
              {
                DataSet ds = (DataSet)mtn.getUserObject();
                
                JTree logTree =  jcui.showLog(ds);
                logTree.hasFocus();
                
                jpui.showAttributes(ds.getAttributeList());
                int num = ds.getNum_operators();
                String[] mmm = new String[num];
                    
                oMenu.removeAll();
   
                    JMenuItem mitem;
                    InstrumentType it = new InstrumentType();
                    int kk = it.getIPNSInstType(ds.getTitle());
             
                        for (int k=0; k<num; k++)
                       {
                            mmm[k] = ds.getOperator(k).getTitle(); 
                            mitem = new JMenuItem(mmm[k]);
                            oMenu.add(mitem);
                           
                          
                            if(  k == 3 || k == 7 || k == 15 )
                            oMenu.addSeparator();
                            mitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                        }

                 
               }
               else if(  mtn.getUserObject() instanceof Data)
               {
                  oMenu.removeAll();
                  Data data = (Data)mtn.getUserObject();
                  
                  DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)mtn.getParent();
                  DataSet ds = (DataSet)parent.getUserObject();
                  JTree logTree =  jcui.showLog(ds);
                  logTree.hasFocus();
               
                  jpui.showAttributes(data.getAttributeList());
                  System.out.println("No DataSet selected");
               }
               else
               {
                oMenu.removeAll();
                //IsawViewHelp("No DataSet selected");
                System.out.println("No DataSet selected");
                
               }
             
                   
            }
      } 
 
    
        public static void main(String[] args) 
        {
        
        JFrame Isaw = new Isaw();
        
        
        Isaw.pack();
        Isaw.setVisible(true);
        Isaw.setSize(950,750);
        
        Isaw.addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e) {System.exit(0);} 
        });  
        
    }//main
    


}
