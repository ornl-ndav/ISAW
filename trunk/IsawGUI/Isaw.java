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
//import javax.swing.preview.*;
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
import DataSetTools.operator.*;
import DataSetTools.util.*;
import ChopTools.*;
import IPNS.Runfile.*;
import java.applet.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException; 


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
    JPopupMenu popup ;
    JMenu scalarMenu = new JMenu("Scalar Operations");
    JMenu datasetMenu = new JMenu("DataSet Operations");
    JMenu convertMenu = new JMenu("Axes Conversions");
    
    JMenu pscalarMenu = new JMenu("Scalar Operations");
    JMenu pdatasetMenu = new JMenu("DataSet Operations");
    JMenu pconvertMenu = new JMenu("Axes Conversions");
    
    JTree tree ;
    JMenuItem mi;
    String dirName = null;
    boolean set_selection = false ;

    public Isaw() 
    {
       super("ISAW");
       System.out.println("Loading ISAW");
       setupMenuBar();

       jtui = new JTreeUI();
       jtui.setPreferredSize(new Dimension(200, 500));
       jtui.setMinimumSize(new Dimension(20, 50));
       tree = jtui.getTree();
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
       //con.add(sp,BorderLayout.CENTER );
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
        JMenu instrumentInfoView = new JMenu("Instrument Info");
	  JMenu macro_loader = new JMenu("Load Macro");       
        JMenu macrosMenu = new JMenu("Macros");
        JMenu optionMenu = new JMenu("Options");
        JMenu wMenu = new JMenu("Window");
        JMenu hMenu = new JMenu("Help");
        
        
        
        JMenuItem fileRunfile = new JMenuItem("Load Entire Runfile(s)");
        JMenuItem Runfile = new JMenuItem("Load Runfile");
	  
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
        
        JMenuItem HRMECS = new JMenuItem("HRMECS");
        JMenuItem LRMECS = new JMenuItem("LRMECS");
        JMenuItem HIPD = new JMenuItem("HIPD");
        JMenuItem SAD = new JMenuItem("SAD");
        JMenuItem SCD = new JMenuItem("SCD");
        JMenuItem SAND = new JMenuItem("SAND");
        JMenuItem POSY1 = new JMenuItem("POSY1");
        JMenuItem POSY2 = new JMenuItem("POSY2");
        JMenuItem GLAD = new JMenuItem("GLAD");
        JMenuItem QENS = new JMenuItem("QENS");
        JMenuItem GPPD = new JMenuItem("GPPD");
        JMenuItem SEPD = new JMenuItem("SEPD");
        JMenuItem CHEXS = new JMenuItem("CHEXS");
        
        JMenuItem m_HRMECS = new JMenuItem("HRMECS ");
        JMenuItem m_LRMECS = new JMenuItem("LRMECS ");
        JMenuItem m_HIPD = new JMenuItem("HIPD ");
        JMenuItem m_SAD = new JMenuItem("SAD ");
        JMenuItem m_SCD = new JMenuItem("SCD ");
        JMenuItem m_SAND = new JMenuItem("SAND ");
        JMenuItem m_POSY1 = new JMenuItem("POSY1 ");
        JMenuItem m_POSY2 = new JMenuItem("POSY2 ");
        JMenuItem m_GLAD = new JMenuItem("GLAD ");
        JMenuItem m_QENS = new JMenuItem("QENS ");
        JMenuItem m_GPPD = new JMenuItem("GPPD ");
        JMenuItem m_SEPD = new JMenuItem("SEPD ");
        JMenuItem m_CHEXS = new JMenuItem("CHEXS ");

        JMenuItem l_HRMECS = new JMenuItem("HRMECS  ");
        JMenuItem l_LRMECS = new JMenuItem("LRMECS  ");
        JMenuItem l_HIPD = new JMenuItem("HIPD  ");
        JMenuItem l_SAD = new JMenuItem("SAD   ");
        JMenuItem l_SCD = new JMenuItem("SCD  ");
        JMenuItem l_SAND = new JMenuItem("SAND  ");
        JMenuItem l_POSY1 = new JMenuItem("POSY1  ");
        JMenuItem l_POSY2 = new JMenuItem("POSY2  ");
        JMenuItem l_GLAD = new JMenuItem("GLAD  ");
        JMenuItem l_QENS = new JMenuItem("QENS  ");
        JMenuItem l_GPPD = new JMenuItem("GPPD  ");
        JMenuItem l_SEPD = new JMenuItem("SEPD  ");
        JMenuItem l_CHEXS = new JMenuItem("CHEXS  ");

    
        fMenu.add(Runfile);
	  fMenu.add(macro_loader);
        fMenu.add(fileRunfile);
        fMenu.add(fileRunfiles);
        fMenu.add(fileLoadDataset);
        fMenu.addSeparator();
        fMenu.add(fileSaveData);
        fMenu.add(fileSaveDataAs);
        fMenu.add(imagePrint);
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
        
        instrumentInfoView.add(HRMECS);
        instrumentInfoView.add(GPPD);
        instrumentInfoView.add(SEPD);
        instrumentInfoView.add(LRMECS);
        instrumentInfoView.add(SAD);
        instrumentInfoView.add(SAND);
        instrumentInfoView.add(SCD);
        instrumentInfoView.add(GLAD);
        instrumentInfoView.add(HIPD);
        instrumentInfoView.add(POSY1);
        instrumentInfoView.add(POSY2);
        instrumentInfoView.add(QENS);
        instrumentInfoView.add(CHEXS);
        
        macrosMenu.add(m_HRMECS);
        macrosMenu.add(m_GPPD);
        macrosMenu.add(m_SEPD);
        macrosMenu.add(m_LRMECS);
        macrosMenu.add(m_SAD);
        macrosMenu.add(m_SAND);
        macrosMenu.add(m_SCD);
        macrosMenu.add(m_GLAD);
        macrosMenu.add(m_HIPD);
        macrosMenu.add(m_POSY1);
        macrosMenu.add(m_POSY2);
        macrosMenu.add(m_QENS);
        macrosMenu.add(m_CHEXS);

	  macro_loader.add(l_HRMECS);
        macro_loader.add(l_GPPD);
        macro_loader.add(l_SEPD);
        macro_loader.add(l_LRMECS);
        macro_loader.add(l_SAD);
        macro_loader.add(l_SAND);
        macro_loader.add(l_SCD);
        macro_loader.add(l_GLAD);
        macro_loader.add(l_HIPD);
        macro_loader.add(l_POSY1);
        macro_loader.add(l_POSY2);
        macro_loader.add(l_QENS);
        macro_loader.add(l_CHEXS);

        
        vMenu.add(imageView);
        vMenu.add(s_graphView);
        vMenu.add(graphView);
        //vMenu.add(viewFileSeparator);
        vMenu.add(instrumentInfoView); 
        
          
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
        Runfile.addActionListener(new LoadMenuItemHandler());
	  
        fileRunfile.addActionListener(new LoadMenuItemHandler());
        fileRunfiles.addActionListener(new LoadMenuItemHandler());

        fileSaveData.addActionListener(new MenuItemHandler());
        fileSaveDataAs.addActionListener(new MenuItemHandler());
        imagePrint.addActionListener(new MenuItemHandler());
        
        graphView.addActionListener(new MenuItemHandler()); 
       
        
        //s_graphView.addActionListener(new MenuItemHandler()); 
        iFrame_sg.addActionListener(new MenuItemHandler()); 
        eFrame_sg.addActionListener(new MenuItemHandler()); 
        
        imageView.addActionListener(new MenuItemHandler()); 
        iFrame.addActionListener(new MenuItemHandler()); 
        eFrame.addActionListener(new MenuItemHandler()); 
        
        HRMECS.addActionListener(new MenuItemHandler());
        LRMECS.addActionListener(new MenuItemHandler());
        HIPD.addActionListener(new MenuItemHandler());
        
        GPPD.addActionListener(new MenuItemHandler());
        SEPD.addActionListener(new MenuItemHandler());
        SAND.addActionListener(new MenuItemHandler());
        
        SAD.addActionListener(new MenuItemHandler());
        SCD.addActionListener(new MenuItemHandler());
        POSY1.addActionListener(new MenuItemHandler());
        
        POSY2.addActionListener(new MenuItemHandler());
        QENS.addActionListener(new MenuItemHandler());
        GLAD.addActionListener(new MenuItemHandler());
        CHEXS.addActionListener(new MenuItemHandler());
        
        
        m_HRMECS.addActionListener(new MenuItemHandler());
        m_LRMECS.addActionListener(new MenuItemHandler());
        m_HIPD.addActionListener(new MenuItemHandler());
        
        m_GPPD.addActionListener(new MenuItemHandler());
        m_SEPD.addActionListener(new MenuItemHandler());
        m_SAND.addActionListener(new MenuItemHandler());
        
        m_SAD.addActionListener(new MenuItemHandler());
        m_SCD.addActionListener(new MenuItemHandler());
        m_POSY1.addActionListener(new MenuItemHandler());
        
        m_POSY2.addActionListener(new MenuItemHandler());
        m_QENS.addActionListener(new MenuItemHandler());
        m_GLAD.addActionListener(new MenuItemHandler());
        m_CHEXS.addActionListener(new MenuItemHandler());


        l_HRMECS.addActionListener(new MenuItemHandler());
        l_LRMECS.addActionListener(new MenuItemHandler());
        l_HIPD.addActionListener(new MenuItemHandler());
        
        l_GPPD.addActionListener(new MenuItemHandler());
        l_SEPD.addActionListener(new MenuItemHandler());
        l_SAND.addActionListener(new MenuItemHandler());
       
        l_SAD.addActionListener(new MenuItemHandler());
        l_SCD.addActionListener(new MenuItemHandler());
        l_POSY1.addActionListener(new MenuItemHandler());
        
        l_POSY2.addActionListener(new MenuItemHandler());
        l_QENS.addActionListener(new MenuItemHandler());
        l_GLAD.addActionListener(new MenuItemHandler());
        l_CHEXS.addActionListener(new MenuItemHandler());

        
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
        menuBar.add(macrosMenu);
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
                           
                   //To Add more instances add following code here later----
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
    
    // final JFileChooser fc = new JFileChooser();
        final FileDialog fd = new FileDialog(new Frame(), "Choose the Folder/File to open", FileDialog.LOAD);   
     
        public void actionPerformed(ActionEvent ev) 
        
        {
            
            String s=ev.getActionCommand();
            
            if(s=="Load Runfile")
                 { 
                    try
                    {

                        fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());
                       {
                      
                        String filename =f.toString();
                        String ff = fd.getFile();
                        System.out.println("The ffis "  + ff);
                        System.out.println("The filename is "  + filename);
                       
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        RunfileRetriever r = new RunfileRetriever(filename);
                        
                        int numberOfDataSets = r.numDataSets();
                        DataSet[] dss = new DataSet[numberOfDataSets];
                        for (int i = 0; i< numberOfDataSets; i++)
                            dss[i] = r.getDataSet(i);
                            System.out.println("Tree is : " +jtui.getTree()); 
                            jtui.addDataSets(dss, ff);
		
				if(dss[1]!=null)
				    jdvui.drawImage(dss[1],"Internal Frame");
                       System.out.println("Print the dataset dss[1]  " +dss[1]);
                       setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                       }
                    } 
                    catch (Exception e){System.out.println("Choose a input file");}
                   
                }
              
                if(s=="Load Entire Runfile(s)")
                {
                    try
                    { 
                           
                            
                         fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());  
                         String filename =f.toString();
                        String ff = fd.getFile();
                            
                           // JOptionPane.showMessageDialog(null, fc.getCurrentDirectory().toString());

                        //if ( state == JFileChooser.APPROVE_OPTION){
                       // LoadFiles db = new LoadFiles(jtui, fc.getCurrentDirectory().toString());
                       LoadFiles db = new LoadFiles(jtui,fd.getDirectory(),jdvui);
                        db.setSize(new Dimension(550,300));
                       db.show(); 
                       // }
                    }
                    catch (Exception e){System.out.println("Choose a input file: "+e);}
                }
                if(s=="Load Selected Data")
                {
                    try
                    { 
                     // int state = fc.showOpenDialog(null);
                       fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());  
                         String filename =f.toString();
                        String ff = fd.getFile();
                            
                           // JOptionPane.showMessageDialog(null, fc.getCurrentDirectory().toString());

                       // if ( state == JFileChooser.APPROVE_OPTION){
                        
                       // ListFiles db = new ListFiles(jtui,fc.getCurrentDirectory().toString());
                       ListFiles db = new ListFiles(jtui,fd.getDirectory());
                        db.setSize(new Dimension(550,300));
                        db.show();  
                       // }
                    }
                    catch (Exception e){System.out.println("Choose a input file: ");}
                }
                    
        } 
   }
   
   private class MenuItemHandler implements ActionListener 
   {
        FileDialog fd = new FileDialog(new Frame(), "Choose the Folder/File to open", FileDialog.LOAD);
       // final JFileChooser fc = new JFileChooser();
       BrowserControl bc =  new BrowserControl();
        public void actionPerformed(ActionEvent ev) 
        
        {
            String s=ev.getActionCommand();
            
                if(s=="Exit")
                {
	  try
		{
		//	Runtime.getRuntime().exec("C:\\Winnt\\Notepad.exe");

		}
	     catch(Exception e){};

                    System.exit(0);
                }
                
                if(s=="Print to File")
                {
                    jdvui.printImage();
                 //  PrintUtilities.printComponent(jtui) ;
                 try {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) sm.checkPrintJobAccess();
                        // print...
                        System.out.println("Printing  allowed");
                        }
                catch (SecurityException e) {
                System.err.println("Sorry. Printing is not allowed.");
                }

                }
                
              
         
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
                                //IsawViewHelp("No DataSet selected");
                                }
                     }


                     
                     if(s == "HRMECS")
                      {  
                        String url = "http://www.pns.anl.gov/HRMECS/HRMECS_frameset.html";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
            
                        
                      }
                      if(s == "LRMECS")
                      { 
                        String url = "http://www.pns.anl.gov/lrmecs/lrmecs.html";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "HIPD")  
                      { 
                        String url = "http://www.pns.anl.gov/highpd.htm";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url); 
                      }
       
                      if(s == "QENS")
                      {
                        String url = "http://www.pns.anl.gov/qens/qens.html";
                        bc.displayURL(url);
                      }
                      
                      if(s == "POSY1")
                      {
                        String url = "http://www.pns.anl.gov/posy/posy.html";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "POSY2")
                      {
                        String url = "http://www.pns.anl.gov/posy2/posy2.htm";
                        bc.displayURL(url);
                      }
                      
                      if(s == "SCD")
                      {
                        String url = "http://www.pns.anl.gov/scd.html";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "SAND")
                      { 
                        String url = "http://www.pns.anl.gov/sand.html";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "SAD")
                      {  
                        String url = "http://www.pns.anl.gov/sad/sad.htm";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "SEPD")
                      { 
                        String url = "http://www.pns.anl.gov/sepd_yel.htm";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "GPPD")
                      {  
                        String url = "http://www.pns.anl.gov/gppd/index.htm";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      if(s == "GLAD")
                      {  
                        String url = "http://www.pns.anl.gov/glad/glad.html";
                        bc.displayURL(url);
                      }
                      
                      if(s == "CHEXS")
                      { 
                        String url = "http://www.pns.anl.gov/chex.htm";
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        bc.displayURL(url);
                      }
                      
                      
  //Menuitems for macro action below                    
                      
                      
                      
                      if(s == "HRMECS ")
                      { 
                        /////Dongfeng add the macro code in this section  
                        fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());
                        String dir = fd.getDirectory();
                           FileSeparator fs = new FileSeparator(dir);
                        fs.setSize(700,700);
                        fs.setVisible(true);
                      }
                      if(s == "LRMECS ")
                      { 
             
                       HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/lrmecs/lrmecs.html");
                       htm.setSize(600,400);
                       htm.show();
                        
                      }
                      if(s == "HIPD ")  
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/highpd.htm");
                        htm.setSize(600,400);
                        htm.show();
                      }
       
                      if(s == "QENS ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/qens/qens.html");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "POSY1 ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/posy/posy.html");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "POSY2 ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/posy2/posy2.htm");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "SCD ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/scd.html");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "SAND ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/sand.html");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "SAD ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/sad/sad.htm");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "SEPD ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/sepd_yel.htm");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "GPPD ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/gppd/index.htm");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "GLAD ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/glad/glad.html");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      if(s == "CHEXS ")
                      {  
                        HTMLPage htm  = new HTMLPage("http://www.pns.anl.gov/chex.htm");
                        htm.setSize(600,400);
                        htm.show();
                      }
                      
   // menuitem for macro loader below

			if(s == "HRMECS  ")
                      { 

                      }
                  if(s == "LRMECS  ")
                      { 
             

                        
                      }
                   if(s == "HIPD  ")  
                      {  
                        
                      }
       
                   if(s == "QENS  ")
                      {  
                        
                      }
                   if(s == "POSY1  ")
                      {  
                        
                      }
                   if(s == "POSY2  ")
                      {  
                        
                      }
                   if(s == "SCD  ")
                      {  
                        
                      }
                   if(s == "SAND  ")
                      {  

                      }
                   if(s == "SAD  ")
                      {  
                        
                      }
                   if(s == "SEPD  ")
                      {  
                        
                      }
                   if(s == "GPPD  ")
                      {  
                        System.out.println("Inside GPPd macro loader");

                        
                  try
                    {

                        fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());
                       {
                      
                        String filename =f.toString();
                        String ff = fd.getFile();
                        System.out.println("The ffis "  + ff);
                        System.out.println("The filename is "  + filename);
                       
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        RunfileRetriever r = new RunfileRetriever(filename);
                        
                        int numberOfDataSets = r.numDataSets();
                        DataSetOperator op1,op2;
                        DataSet new_ds1, new_ds2;
                        AttributeNameString attr_name = new AttributeNameString(Attribute.RAW_ANGLE);
                        DataSet[] dss = new DataSet[numberOfDataSets];

                        for (int i = 0; i< numberOfDataSets; i++)
                            dss[i] = r.getDataSet(i);
                            System.out.println("Tree is : " +jtui.getTree()); 
                        op1 = new DiffractometerTofToD(dss[1],0, 4,2000 );
                        new_ds1 = (DataSet)op1.getResult();

                        op2 = new DataSetMultiSort(new_ds1, attr_name, false,
                                                    true, attr_name, true, false,attr_name, true, false);
                        new_ds2 = (DataSet)op2.getResult();
                        jtui.addDataSet(new_ds2);
				
				    jdvui.drawImage(new_ds2,"Internal Frame");

                        //    jtui.addDataSets(dss, ff);
                       setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                       }
                    } 
                    catch (Exception e){System.out.println("Choose a input file");}


//			DefaultMutableTreeNode mtn = jtui.getSelectedNode();
 /*                       DataSet ds = (DataSet)mtn.getUserObject();
                        System.out.println("Inside GPPd macro loader");
                        DataSetOperator op1,op2;
                        DataSet new_ds1, new_ds2;
                        AttributeNameString attr_name = new AttributeNameString(Attribute.RAW_ANGLE);
                        op1 = new DiffractometerTofToD(ds,0, 5,1000 );
                        new_ds1 = (DataSet)op1.getResult();

                        op2 = new DataSetMultiSort(new_ds1, attr_name, false,
                                                    true, attr_name, true, false,attr_name, true, false);
                        new_ds2 = (DataSet)op2.getResult();
                        jtui.addDataSet(new_ds2);
*/

                      }
                   if(s == "GLAD  ")
                      {  

                      }
                   if(s == "CHEXS  ")
                      {  
                        
                      }

                     if(s == "File Separator")
                      {
                      
                        fd.show();
                        File f = new File(fd.getDirectory(), fd.getFile());
                        String dir = fd.getDirectory();
                           FileSeparator fs = new FileSeparator(dir);
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
                      if(  mtn.getLevel()==1)
                     {  
                           int num_child =  mtn.getChildCount();
                       
                           DataSet mergedDS1 = null;
                           DataSet mergedDS2 = null;
                           DataSetOperator  op1, op2;
                   
                         
                           DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                           DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
           
                           DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                           DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                  
                       if(num_child == 2)
                        {   
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            jdvui.drawImage(mergedDS1,"Internal Frame");
                            //jtui.addDataSet(mergedDS1);
                        }
                     if(num_child == 3)
                          
                        {  
                            DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                            DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            op2 = new DataSetMerge( mergedDS1, ds2 );
                            mergedDS2 = (DataSet)op2.getResult(); 
                            jdvui.drawImage(mergedDS2,"Internal Frame");
                           // jtui.addDataSet(mergedDS2);
  
                        }
                     }
                     
                     
                   // System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
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
                        TreePath[] paths = null;
	                    JTree tree = jtui.getTree();
	                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	                    TreePath[] tp = tree.getSelectionPaths();
                        Data ggg = (Data)mtn.getUserObject();
                        int start_id =  ggg.getGroup_ID();
                        DataSetOperator  op1;
                        AttributeNameString attr_name = new AttributeNameString("Group ID");
                        op1 = new SelectData(ds, attr_name , true, start_id, start_id+tp.length-1);
                        DataSet new_ds = (DataSet)op1.getResult(); 

                        jdvui.drawImage(new_ds,"Internal Frame");
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
                               // IsawViewHelp("No DataSet selected");
                            
                            }
                  
                }
                
                 if(s=="External Frame" )
                {   
                  /*  DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                     if(  mtn.getLevel()==1)
                     {
                     System.out.println("Selected object is :"+mtn.getUserObject()); 
                       int num_child =  mtn.getChildCount();
                         for(int i=0; i<num_child; i++)
                      {DefaultMutableTreeNode child_dataset = (DefaultMutableTreeNode) mtn.getChildAt(i);
                       DataSet ds = (DataSet)child_dataset.getUserObject();
                       System.out.println("Child Dataset are" +ds);
                      }
                     }
                     */
                     DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                      if(  mtn.getLevel()==1)
                     {  
                           int num_child =  mtn.getChildCount();
                       
                           DataSet mergedDS1 = null;
                           DataSet mergedDS2 = null;
                           DataSetOperator  op1, op2;
                   
                         
                           DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                           DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
           
                           DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                           DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                  
                       if(num_child == 2)
                        {   
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            jdvui.drawImage(mergedDS1,"External Frame");
                          //  jtui.addDataSet(mergedDS1);
                        }
                     if(num_child == 3)
                          
                        {  
                            DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                            DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            op2 = new DataSetMerge( mergedDS1, ds2 );
                            mergedDS2 = (DataSet)op2.getResult(); 
                            jdvui.drawImage(mergedDS2,"External Frame");
                          //  jtui.addDataSet(mergedDS2);
  
                        }
                     }
                    
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
                        TreePath[] paths = null;
	                    JTree tree = jtui.getTree();
	                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	                    TreePath[] tp = tree.getSelectionPaths();
                        Data ggg = (Data)mtn.getUserObject();
                        int start_id =  ggg.getGroup_ID();
                        DataSetOperator  op1;
                        AttributeNameString attr_name = new AttributeNameString("Group ID");
                        op1 = new SelectData(ds, attr_name , true, start_id, start_id+tp.length-1);
                        DataSet new_ds = (DataSet)op1.getResult(); 
                        jdvui.drawImage(new_ds,"External Frame");
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
                               // IsawViewHelp("No DataSet selected");
                         }
                  
                }
                
                
                 if(s=="Graph View" )
                {   
                    
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());

                      if(  mtn.getLevel()==1)
                     {  
                           int num_child =  mtn.getChildCount();
                       
                           DataSet mergedDS1 = null;
                           DataSet mergedDS2 = null;
                           DataSetOperator  op1, op2;
                   
                         
                           DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                           DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
           
                           DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                           DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                  
                       if(num_child == 2)
                        {   
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            //jdvui.drawImage(mergedDS1,"Internal Frame");
                           // jtui.addDataSet(mergedDS1);
                            chop_MacroTools fg = new chop_MacroTools();
                         fg.drawAlldata (mergedDS1); 
                           
                        }
                     if(num_child == 3)
                          
                        {  
                            DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                            DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            op2 = new DataSetMerge( mergedDS1, ds2 );
                            mergedDS2 = (DataSet)op2.getResult(); 
                           // jdvui.drawImage(mergedDS2,"Internal Frame");
                          //  jtui.addDataSet(mergedDS2);
                          
                          chop_MacroTools fg = new chop_MacroTools();
                         fg.drawAlldata (mergedDS2); 
  
                        }
                     }           
                    
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
                        
                        TreePath[] paths = null;
	                    JTree tree = jtui.getTree();
	                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	                    TreePath[] tp = tree.getSelectionPaths();
                        Data ggg = (Data)mtn.getUserObject();
                        int start_id =  ggg.getGroup_ID();
                        DataSetOperator  op1;
                        AttributeNameString attr_name = new AttributeNameString("Group ID");
                        op1 = new SelectData(ds, attr_name , true, start_id, start_id+tp.length-1);
                        DataSet new_ds = (DataSet)op1.getResult(); 
                       
                        
                         chop_MacroTools fg = new chop_MacroTools();
                           fg.drawAlldata (new_ds); 
                        Data data = (Data)mtn.getUserObject();
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
//                                IsawViewHelp("No DataSet selected");
                         }
                  
                }
                
                if(s=="Scrolled Graph Internal Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                   if(  mtn.getLevel()==1)
                     {  
                           int num_child =  mtn.getChildCount();
                       
                           DataSet mergedDS1 = null;
                           DataSet mergedDS2 = null;
                           DataSetOperator  op1, op2;
                   
                         
                           DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                           DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
           
                           DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                           DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                  
                       if(num_child == 2)
                        {   
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            //jdvui.drawImage(mergedDS1,"Internal Frame");
                           // jtui.addDataSet(mergedDS1);
                            GraphView graph_view =  new GraphView(mergedDS1);
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
                     if(num_child == 3)
                          
                        {  
                            DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                            DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            op2 = new DataSetMerge( mergedDS1, ds2 );
                            mergedDS2 = (DataSet)op2.getResult(); 
                           // jdvui.drawImage(mergedDS2,"Internal Frame");
                          //  jtui.addDataSet(mergedDS2);
                          
                          GraphView graph_view =  new GraphView(mergedDS2);
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
                     }           
                     
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
                        TreePath[] paths = null;
	                    JTree tree = jtui.getTree();
	                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	                    TreePath[] tp = tree.getSelectionPaths();
                        Data ggg = (Data)mtn.getUserObject();
                        int start_id =  ggg.getGroup_ID();
                        DataSetOperator  op1;
                        AttributeNameString attr_name = new AttributeNameString("Group ID");
                        op1 = new SelectData(ds, attr_name , true, start_id, start_id+tp.length-1);
                        DataSet new_ds = (DataSet)op1.getResult(); 
                        GraphView graph_view =  new GraphView(new_ds);  
                        
                        
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
//                                IsawViewHelp("No DataSet selected");
                         }
                }
                
                if(s=="Scrolled Graph External Frame" )
                {   
                    DefaultMutableTreeNode mtn = jtui.getSelectedNode();
                       
                    System.out.println("The Selected Node in ISaw is "  +mtn.getUserObject());
                    if(  mtn.getLevel()==1)
                     {  
                           int num_child =  mtn.getChildCount();
                       
                           DataSet mergedDS1 = null;
                           DataSet mergedDS2 = null;
                           DataSetOperator  op1, op2;
                   
                         
                           DefaultMutableTreeNode child_dataset0= (DefaultMutableTreeNode) mtn.getChildAt(0);
                           DefaultMutableTreeNode child_dataset1 = (DefaultMutableTreeNode) mtn.getChildAt(1);
           
                           DataSet ds0 = (DataSet)child_dataset0.getUserObject();
                           DataSet ds1 = (DataSet)child_dataset1.getUserObject();
                  
                       if(num_child == 2)
                        {   
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            //jdvui.drawImage(mergedDS1,"Internal Frame");
                           // jtui.addDataSet(mergedDS1);
                            GraphView graph_view =  new GraphView(mergedDS1);
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
                     if(num_child == 3)
                          
                        {  
                            DefaultMutableTreeNode child_dataset2 = (DefaultMutableTreeNode) mtn.getChildAt(2);
                            DataSet ds2 = (DataSet)child_dataset2.getUserObject();
                            op1 = new DataSetMerge( ds0, ds1 );
                            mergedDS1 = (DataSet)op1.getResult(); 
                            op2 = new DataSetMerge( mergedDS1, ds2 );
                            mergedDS2 = (DataSet)op2.getResult(); 
                           // jdvui.drawImage(mergedDS2,"Internal Frame");
                          //  jtui.addDataSet(mergedDS2);
                          
                          GraphView graph_view =  new GraphView(mergedDS2);
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
                     }           
                    
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
                        //GraphView graph_view =  new GraphView(ds);
                        JFrame f = new JFrame("Test for GraphView class");
                        f.setBounds(0,0,600,400);
                       
                       // f.setJMenuBar( graph_view.getMenuBar() );
                       
                       
                    TreePath[] paths = null;
	                JTree tree = jtui.getTree();
	                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	                TreePath[] tp = tree.getSelectionPaths();
	                System.out.println("The number of selected files is  "+tp.length );
	                
	                Data ggg = (Data)mtn.getUserObject();
                    int start_id =  ggg.getGroup_ID();
                    DataSetOperator  op1;
                    AttributeNameString attr_name = new AttributeNameString("Group ID");
                    
	                for (int i=0; i<tp.length; i++)
	                {
                      // dmtn = (DefaultMutableTreeNode)tp[i].getLastPathComponent();
                      //  System.out.println("The selected files are in JTREEUI " +dmtn.toString());
                    }
	                
                    
                    op1 = new SelectData(ds, attr_name , true, start_id, start_id+tp.length-1);
                    
                    DataSet new_ds = (DataSet)op1.getResult(); 
                    GraphView graph_view =  new GraphView(new_ds); 
                        
                        f.getContentPane().add(graph_view);
                        f.setVisible(true);
                        
                        Data data = (Data)mtn.getUserObject();
                        jpui.showAttributes(data.getAttributeList());
                    }
                    else {
                                System.out.println("View is Selected");
//                                IsawViewHelp("No DataSet selected");
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
                "\n"+"                    For the Graph View menu item 'c' gives the cordinates of the cursor on "+
                "\n"+"                    the graph and 'Shift+R' returns one to the previous zoom level. Key 'm' "+
                "\n"+"                    allows a range selection for the graph displayed."+
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
	                System.out.println("The number of selected files is  "+tp.length );
	               
	                for (int i=0; i<tp.length; i++)
	                {
                        dmtn = (DefaultMutableTreeNode)tp[i].getLastPathComponent();
                        System.out.println("The selected files are in JTREEUI " +dmtn.toString());
                        try
                        {
                               
                                if(  dmtn.getUserObject() instanceof DataSet)
                                {
                                    DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)dmtn.getParent();
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    model.removeNodeFromParent(dmtn); 
                                    
                                    System.gc();
		                            System.runFinalization();
		                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                }
                                
                                else if(  dmtn.getUserObject() instanceof Data)
                                {
                                    DefaultMutableTreeNode  parent = (DefaultMutableTreeNode)dmtn.getParent();
                                    DataSet ds = (DataSet)parent.getUserObject();
                                
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    int child_position = parent.getIndex(dmtn);
                                    System.out.println("THe child index is   :" +child_position);
                                    ds.removeData_entry(child_position);
            
                                    System.out.println("Removed from DS  :" +child_position);
                                    model.removeNodeFromParent(dmtn);
                                    ds.addLog_entry( "Removed " +dmtn.getUserObject().toString());
                                    System.gc();
		                            System.runFinalization();
		                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                    System.out.println("Removed from treemodel  :" +child_position);
                                }
                                
                                else if (dmtn.getLevel() == 1)
                                {
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    model.removeNodeFromParent(dmtn);
                                    System.gc();
		                            System.runFinalization();
		                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
     
     
         
                    

    public void IsawViewHelp(String [] info)
    {
        
        JFrame mm = new JFrame();
        //JDialog hh = new JDialog(mm, "ISAW View Help");
        JDialog hh = new JDialog();
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
	    JTextArea textArea = new JTextArea();
	    for (int i=0;i<info.length; i++)
        textArea.setText(info[i]);
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
              popup = new JPopupMenu();
              if(tree.getSelectionCount() < 1) return;
                 
               
               
              DefaultMutableTreeNode mtn = jtui.getSelectedNode();
              if(  mtn.getUserObject() instanceof DataSet)
              {
                DataSet ds = (DataSet)mtn.getUserObject();  
                
                JTree logTree =  jcui.showLog(ds);
                logTree.hasFocus();
                
               
                //DetectorInfo df = new DetectorInfo();
                //df.showAttributes(attr_list,ds);
                JTable table = jcui.showDetectorInfo(ds);
                table.hasFocus();
                
                jpui.showAttributes(ds.getAttributeList());
                int num = ds.getNum_operators();
                String[] mmm = new String[num];
                    
                oMenu.removeAll();
                popup.removeAll();
                scalarMenu.removeAll();
                datasetMenu.removeAll();
                convertMenu.removeAll();
                
                oMenu.removeAll();
                pscalarMenu.removeAll();
                pdatasetMenu.removeAll();
                pconvertMenu.removeAll();

                JMenuItem mitem = null;
                JMenuItem pmitem = null;
                
                   
                    for (int k=0; k<num; k++)
                    {
                            mmm[k] = ds.getOperator(k).getTitle(); 
                           
                         
                            if(k==4||k==8||k==16 )
                            {//oMenu.addSeparator();
                            //popup.addSeparator();
                            }
                            if(mmm[k].endsWith("Scalar"))
                            {   mitem = new JMenuItem(mmm[k]);
                                pmitem = new JMenuItem(mmm[k]);
                                
                                scalarMenu.add(mitem);
                                oMenu.add(scalarMenu);
                               // oMenu.add(mitem);
                               // popup.add(pmitem);
                                
                                pscalarMenu.add(pmitem);
                                popup.add(pscalarMenu);
                                
                                mitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                                pmitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                             }
                        
                        
                            else if(mmm[k].startsWith("Convert"))
                             {  mitem = new JMenuItem(mmm[k]);
                                pmitem = new JMenuItem(mmm[k]);
                                
                                convertMenu.add(mitem);
                                oMenu.add(convertMenu);
                                
                                pconvertMenu.add(pmitem);
                                popup.add(pconvertMenu);
                               
                               //oMenu.add(mitem);
                                //popup.add(pmitem);
                                
                                mitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                                pmitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                             }
       
                               else if(mmm[k].endsWith("DataSet"))
                             {  mitem = new JMenuItem(mmm[k]);
                                pmitem = new JMenuItem(mmm[k]);
                                
                                datasetMenu.add(mitem);
                                oMenu.add(datasetMenu);
                                
                                pdatasetMenu.add(pmitem);
                                popup.add(pdatasetMenu);
                               
                              // oMenu.add(mitem);
                               // popup.add(pmitem);
                                
                                mitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                                pmitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                             }
                             else //if(!mmm[k].endsWith("Scalar") && !mmm[k].endsWith("DataSet") && !mmm[k].startsWith("Convert"))
                           {    mitem = new JMenuItem(mmm[k]);
                                pmitem = new JMenuItem(mmm[k]);
                                popup.add(pmitem);
                                oMenu.add(mitem);
                                
                                mitem.addActionListener(new JOperationsMenuHandler(ds,jtui));
                                pmitem.addActionListener(new JOperationsMenuHandler(ds,jtui));      
                                    
                           } 
                            
                       }
                       
                      tree.add(popup);//popup   
                      tree.addMouseListener(new PopupTrigger());//popup 
                      
                }
               else if(  mtn.getUserObject() instanceof Data)
               {
                  oMenu.removeAll();
                  popup.removeAll();
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
                popup.removeAll();
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
    
    class PopupTrigger extends MouseAdapter 
     {
        public void mouseReleased(MouseEvent e) 
        {
            if (e.isPopupTrigger()) 
            {
                int x = e.getX();
                int y = e.getY();
                TreePath path = tree.getPathForLocation(x, y);
                popup.show(tree, x, y);

            }
        }
    }

}
