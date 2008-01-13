/*
 * File:  WizardWizard.java 
 *             
 * Copyright (C) 2005, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2008/01/13 17:35:24  rmikk
 * Added a lot of new changes to this class
 *
 * Revision 1.5  2005/06/17 13:35:37  rmikk
 * closed a file after reading and writing
 * Added the PlaceHolderPG for possible Result ParameterGUI
 *
 * Revision 1.4  2005/05/25 18:53:12  rmikk
 * The ResultPG's now have different names so the wizard can detect them.
 * The constants are now always given when there is a resultPG even if they
 *    are new int[0];
 *
 * Revision 1.3  2005/05/25 03:10:35  rmikk
 * Fixed some writing errors so the code that is produced now compiles
 *
 * Revision 1.2  2005/05/24 21:39:25  rmikk
 * Fixed error when cvs add a log message at an improper place(hopefully)
 *
 * Revision 1.1  2005/05/24 21:31:55  rmikk
 * Initial Checkin of Wizard to make Wizards
 *
 */

package devTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import Command.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.util.SharedData;
import DataSetTools.parameter.*;
import gov.anl.ipns.Parameters.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.text.html.*;
import gov.anl.ipns.Util.Sys.*;
//import gov.anl.ipns.Parameters.*;
//import gov.anl.ipns.


/**
 * @author mikkelsonr
 * 
 * This class produces a Wizard file from information entered by the user or
 * from information in the file.
 */
public class WizardWizard extends JFrame implements ActionListener,
        Serializable {
    JTabbedPane TabPane;

    // Script_Class_List_Handler SCL = new Script_Class_List_Handler();
    public static final long serialVersionUID = 1L;

    transient DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();

    transient public static String SAVE = "Save Wizard";

    transient public static String SAVE_STATE = "Save State";
    transient public static String OPEN = "Restore State";
    transient public static String WIZ_DIR = "Wizard Directory";
    transient public static String USE_HTML_FILE = "Use Html File";
    transient public static String ADD = "Add Operator";
    transient public static String DELETE = "Remove Operator";
    transient public static String MOVE_UP = "Move Selected Up";
    transient public static String MOVE_DOWN = "Move Selected Down";
    transient public static String CONNECT = "Connect";
    transient public static String CONST = "<Constant";
    transient public static String DISPLAY = "Display";
    transient public static String RET_TYPE = "<Return PG";
    public String WizardDirectory = null;
    public String LastOperator = null;
    public String HTMLDocFileName = null;
    public String OpenFileName = null;
    DefaultListModel OpnList;
    JList listOps;
    InfoPanel infPanel;
    DocPanel docPanel;
    FormPanel formPanel;
    VarPanel varPanel;
    FilePanel filePanel;
    LinkParameters lp;
    boolean FormPanelChanged = true;
    //LinkedList opns = new LinkedList();
    private int maxParams;

    public WizardWizard() {
        super("Wizard Maker");
        lp = null;
        TabPane = new JTabbedPane();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        infPanel = new InfoPanel(this);
        docPanel = new DocPanel(this);
        formPanel = new FormPanel(this);
        varPanel = new VarPanel(this);
        filePanel = new FilePanel(this);
        TabPane.add("Information", infPanel);
        TabPane.add("Documentation", docPanel);
        TabPane.add("Forms", formPanel);
        TabPane.add("Variables", varPanel);
        TabPane.add("File", filePanel);
        getContentPane().setLayout(new GridLayout(1, 1));
        getContentPane().add(TabPane);
        LastOperator = System.getProperty("ISAW_HOME", ""); 
        
        WizardDirectory = System.getProperty("ISAW_HOME");
        if (WizardDirectory == null)
           WizardDirectory = "";
       else if (!WizardDirectory.endsWith(File.separator)) {

           WizardDirectory = WizardDirectory + File.separator;
           WizardDirectory += "Wizard";
       }

    }

    public void initLinkParameters(Vector<GenericOperator> opns) {
        lp = new LinkParameters(opns);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand() == SAVE) {
            Save(WizardDirectory);
        } else if (evt.getActionCommand() == SAVE_STATE) {
           // System.out.println("xxxxx");
            SaveState();
        } else if (evt.getActionCommand() == OPEN) {

            Open();
        } else if (evt.getActionCommand() == WIZ_DIR) {
            WizardDirectory();
        } else if (evt.getActionCommand() == USE_HTML_FILE) {
            if (!docPanel.jb.isSelected())
                HTMLDocFileName = null;
            else {
                JFileChooser jf = new JFileChooser();
                if (jf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                    HTMLDocFileName = jf.getSelectedFile().getAbsolutePath();
                String Prefix = System.getProperty("Docs_Directory", "");
                if (!Prefix.endsWith(File.separator))
                    Prefix = Prefix + File.separator;
                if (HTMLDocFileName.indexOf(Prefix) != 0)
                    HTMLDocFileName = null;
                else
                    HTMLDocFileName = HTMLDocFileName
                            .substring(Prefix.length());

            }
        } else if (evt.getActionCommand() == ADD) {

            AddOperator(OpnList);
            setUpVariableForm( OpnList);
        } else if (evt.getActionCommand() == DELETE) {
            DeleteOperator(OpnList, listOps.getSelectedIndex());

            setUpVariableForm( OpnList);
        } else if (evt.getActionCommand() == MOVE_UP) {
            int selIndx = listOps.getSelectedIndex();
            if (MoveUpOp(OpnList, selIndx))
                listOps.setSelectedIndex(selIndx - 1);

            setUpVariableForm( OpnList);
        } else if (evt.getActionCommand() == MOVE_DOWN) {
            int selIndx = listOps.getSelectedIndex();
            if (MoveDownOp(OpnList, selIndx))
                listOps.setSelectedIndex(selIndx + 1);

            setUpVariableForm( OpnList);
            ;
        }
    }

    public void setUpVariableForm( DefaultListModel OpnList){
        Vector<GenericOperator> opns = new Vector<GenericOperator>();
        for( int i=0; i< OpnList.size(); i++){
           ListHolder list=(ListHolder) OpnList.elementAt(i);
           opns.addElement( list.getop());
        }
       this.initLinkParameters( opns);
    }
    public static void main(String[] args) {
        WizardWizard W = new WizardWizard();
        W.setSize(645, 400);
        gov.anl.ipns.Util.Sys.WindowShower.show(W);

    }

    private void Save(String WizardDirectory) {

        String SaveFileName = WizardDirectory+"/"
                + infPanel.WizardName.getText().trim() + ".java";
        FileInputStream Fop = null;

        FileOutputStream FSave = null;

        File FSaveFile = null;
        try {
            FSaveFile = new File(SaveFileName);
            FSave = new FileOutputStream(FSaveFile);
        } catch (Exception s) {
            JOptionPane.showMessageDialog(null, "Cannot Save File:"
                    + s.getMessage());
            return;
        }
        Vector V;
        int[] done = new int[11];
        Arrays.fill(done, 0);
        int ndone = 0;

        try {
            for (V = getNextSection(Fop); (ndone < done.length)
                    && (((Integer) (V.firstElement())).intValue() >= -1); V = getNextSection(Fop)) {
                int k = ((Integer) V.firstElement()).intValue();

                if (k < 0) {// end of or NO open file
                    if (done[ndone] > 0)
                        ndone++;
                    else
                        k = ndone;
                }

                if (k > 12)
                    FSave.write(V.lastElement().toString().getBytes());

                else if (k == 0) {

                    if (done[0] == 0)
                        WriteGPL(FSave);
                    done[0] = 1;
                } else if (k == 1) {
                    if (V.lastElement().toString().length() > 1)
                        FSave.write(V.lastElement().toString().getBytes());
                    else
                        FSave.write("\r\n */ \r\n\r\n\r\n".getBytes());
                    done[1] = 1;
                } else if (k == 2) {

                    if (done[2] == 0)
                        WritePackage(FSave, FSaveFile);
                    done[2] = 1;
                } else if (k == 3) {
                    if (done[3] == 0)
                        WriteImports(FSave, V.lastElement().toString());
                    done[3] = 1;
                } else if (k == 4) {
                    if (done[4] == 0)
                        WriteClass(FSave, V.lastElement().toString());
                    done[4] = 1;
                } else if (k == 5) {
                    if (done[5] == 0)
                        WriteLinks(FSave);
                    done[5] = 1;
                } else if (k == 6) {
                    if (done[6] == 0)
                        WriteConstr(FSave, V.lastElement().toString());
                    done[6] = 1;
                } else if (k == 7) {
                    if (done[7] == 0)
                        WriteForms(FSave);
                    done[7] = 1;

                } else if (k == 8) {
                    if (done[8] == 0)
                        WriteHelp(FSave);
                    done[8] = 1;
                } else if (k == 9) {

                    if (done[9] == 0)
                        FSave.write("\r\n   }\r\n\r\n".getBytes());
                    done[9] = 1;
                } else if (k == 10) {
                    if (done[10] == 0)
                        WriteMain(FSave, V.lastElement().toString());
                    done[10] = 1;
                }
            }
            FSave.close();
        } catch (Exception ss) {
            JOptionPane.showMessageDialog(null, "Cannot Save File "
                    + ss.toString());

        }
    }

    private void SaveState() {
        try {
            FileOutputStream fout = new FileOutputStream(WizardDirectory
                    +"/"+ infPanel.WizardName.getText().trim() + ".wzb");
            ObjectOutputStream Oout = new ObjectOutputStream(fout);
            Oout.writeObject(infPanel.Acknowl.getText());

            Oout.writeObject(infPanel.Address.getText());
            Oout.writeObject(infPanel.Email.getText());
            Oout.writeObject(infPanel.Instit.getText());
            Oout.writeObject(infPanel.Name.getText());
            Oout.writeObject(infPanel.WizardName.getText());
            Oout.writeObject(infPanel.WizardTitle.getText());
            Oout.writeObject(docPanel.Docum.getText());
            Oout.writeBoolean(docPanel.jb.isSelected());

            Oout.writeObject(this.HTMLDocFileName);
            Oout.writeBoolean(this.FormPanelChanged);
            Oout.writeInt(OpnList.size());
            for (int i = 0; i < OpnList.size(); i++)
                Oout.writeObject(((ListHolder) (OpnList.elementAt(i))).filename);
            int[][] tableB = lp.getLinks();

            Oout.writeObject( lp.ResultPG );
            if( tableB == null)
               Oout.writeInt( 0 );
            else{
               Oout.writeInt( tableB.length );
               Oout.writeObject( tableB ); 
            }
            Oout.writeObject(this.WizardDirectory);
            Oout.close();
        } catch (Exception ss) {
            JOptionPane.showMessageDialog(null, "Cannot Save State:"
                    + ss.toString());
        }

    }

    private void Open() {
        JFileChooser jf = new JFileChooser(System.getProperty("ISAW_HOME"));
        int res = jf.showOpenDialog(null);
        if (res != JFileChooser.APPROVE_OPTION)
            return;
        OpnList = new DefaultListModel();
        listOps.setModel(OpnList);
        
        try {
            FileInputStream finp = new FileInputStream(jf.getSelectedFile()
                    .getAbsolutePath());
            ObjectInputStream oinp = new ObjectInputStream(finp);
            Object O = oinp.readObject();
            infPanel.Acknowl.setText((String) O);
            infPanel.Address.setText((String) (oinp.readObject()));
            infPanel.Email.setText((String) (oinp.readObject()));
            infPanel.Instit.setText((String) (oinp.readObject()));
            infPanel.Name.setText((String) (oinp.readObject()));
            infPanel.WizardName.setText((String) (oinp.readObject()));
            infPanel.WizardTitle.setText((String) (oinp.readObject()));
            docPanel.Docum.setText((String) (oinp.readObject()));
            docPanel.jb.setSelected((oinp.readBoolean()));

            LastOperator = System.getProperty("ISAW_HOME");
            this.HTMLDocFileName = (String) (oinp.readObject());
            this.FormPanelChanged = oinp.readBoolean();
            int k = oinp.readInt();
            
            for (int i = 0; i < k; i++) {

                String filename = (String) (oinp.readObject());
                AddOperator(filename);
            }

            Vector<GenericOperator> ops= new Vector<GenericOperator>();
            for( int i=0; i<OpnList.size(); i++){
              ListHolder lo =(ListHolder) OpnList.elementAt(i);
              ops.addElement(lo.getop());
            }

            setUpVariableForm( OpnList);
            this.lp = new LinkParameters( ops);
            lp.ResultPG= (String[])oinp.readObject();
            int N =oinp.readInt();
            int[][] tableB = (int[][])oinp.readObject();
            int[] links = new int[4];
            java.util.Arrays.fill( links ,-1 );
            for( int i=0; i< N; i++){
               java.util.Arrays.fill( links ,-1 );
               for( int j=0; j< tableB[i].length;j++){
                  if( tableB[i][j] >=0)
                     if( links[0]<0){
                        links[0]=j;
                        links[1]=tableB[i][j];
                     }else {
                        links[2]=j;
                        links[3] =tableB[i][j];
                     }
                        
               }
               lp.addLink(links[0],links[1],links[2],links[3]);
            }
            this.WizardDirectory = (String) oinp.readObject();
            oinp.close();

        } catch (Throwable ss) {
            JOptionPane.showMessageDialog(null, "Cannot Read the State file "
                    + ss.toString());
        }

    }

   /* public void SetFormNums(LinkedList opns) {
        for (int i = 0; i < opns.size(); i++) {
            ((opLinkElement) (opns.get(i))).FormNum = i;
        }
    }
   */
    private void WizardDirectory() {
        if (WizardDirectory == null) {

            WizardDirectory = System.getProperty("ISAW_HOME");
            if (WizardDirectory == null)
                WizardDirectory = "";
            else if (!WizardDirectory.endsWith(File.separator)) {

                WizardDirectory = WizardDirectory + File.separator;
                WizardDirectory += "Wizard";
            }
        }
        JFileChooser jf = new JFileChooser(WizardDirectory);
        jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (jf.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            WizardDirectory = null;
            return;
        }
        WizardDirectory = jf.getSelectedFile().getAbsolutePath();
        if (!WizardDirectory.endsWith(File.separator))
            WizardDirectory += File.separator;
    }

    private void AddOperator(DefaultListModel OpnList) {
        JFileChooser jf = new JFileChooser(LastOperator);
        if (jf.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            return;
        String SelFile = jf.getSelectedFile().getAbsolutePath();
        LastOperator = SelFile;
        AddOperator(SelFile);
    }

    private void AddOperator(String SelFile) {
        try {
            GenericOperator op = null;
            if (SelFile.endsWith(".iss")) {
                op = new ScriptOperator(SelFile);
                OpnList.addElement(new ListHolder(op, SelFile));
            } else if (SelFile.endsWith(".py")) {
                op = new PyScriptOperator(SelFile);
                OpnList.addElement(new ListHolder(op, SelFile));
            } else if (SelFile.endsWith(".class") || SelFile.endsWith(".java")) {
                if (SelFile.endsWith(".java"))
                    SelFile = SelFile.substring(0, SelFile.length() - 5)
                            + ".class";
                op = (GenericOperator) Script_Class_List_Handler
                        .myGetClassInst(SelFile, true);
                if (op == null) {
                    JOptionPane.showMessageDialog(null,
                            "Cannot create operator/Form from " + SelFile);
                    return;
                }
                OpnList.addElement(new ListHolder(op, SelFile));
            } else {
                JOptionPane.showMessageDialog(null, "Cannot add this operator");
                return;
            }

            //lp=null
            setUpVariableForm(OpnList);
            

        } catch (Exception s) {
            s.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Cannot create operator/Form from " + SelFile);
        }
    }

    private void DeleteOperator(DefaultListModel OpnList, int SelIndex) {
        if (SelIndex < 0)
            return;
        OpnList.remove(SelIndex);
        
        setUpVariableForm(OpnList);
        
      

    }

    private boolean MoveUpOp(DefaultListModel OpnList, int SelIndex) {
        if (SelIndex < 1)
            return false;
        Object O = OpnList.remove(SelIndex);
        OpnList.add(SelIndex - 1, O);
        
        lp= null;
        return true;
    }

    private boolean MoveDownOp(DefaultListModel OpnList, int SelIndex) {
        if (SelIndex < 0)
            return false;
        if (SelIndex + 1 >= OpnList.size())
            return false;
        Object O = OpnList.remove(SelIndex);
        OpnList.add(SelIndex + 1, O);
      
        lp = null;

        return true;

    }

    private class InfoPanel extends JPanel implements Serializable {
        WizardWizard W;

        JTextField Name, Email, Instit, Address, WizardTitle, WizardName;

        JTextArea Acknowl;

        JButton Directory;

        public InfoPanel(WizardWizard W) {
            super(new GridLayout(8, 1));
            this.W = W;
            JPanel JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Your Name"));
            Name = new JTextField();
            JP.add(Name);
            add(JP);
            JP = null;

            JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Your Email"));
            Email = new JTextField();
            JP.add(Email);
            add(JP);

            JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Institution"));
            Instit = new JTextField();
            JP.add(Instit);
            add(JP);

            JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Address"));
            Address = new JTextField();
            JP.add(Address);
            add(JP);

            JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Acknowlegements"));
            Acknowl = new JTextArea(3, 30);
            JP.add(Acknowl);
            add(JP);

            JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Wizard Title"));
            WizardTitle = new JTextField();
            JP.add(WizardTitle);
            add(JP);

            JButton jb = new JButton(WIZ_DIR);
            jb.addActionListener(W);
            add(jb);

            JP = new JPanel(new GridLayout(1, 2));
            JP.add(new JLabel("    Name of Wizard File"));
            WizardName = new JTextField();
            JP.add(WizardName);
            add(JP);

        }
    }

    private class DocPanel extends JPanel implements Serializable {
        WizardWizard W;

        JTextArea Docum;

        JCheckBox jb;

        public DocPanel(WizardWizard W) {
            super(new BorderLayout());
            this.W = W;

            jb = new JCheckBox(USE_HTML_FILE);
            jb.addActionListener(W);
            add(jb, BorderLayout.NORTH);
            Docum = new JTextArea(3, 30);
            add(new JScrollPane(Docum));

        }

    }

    private class FormPanel extends JPanel implements Serializable,
            ActionListener {
        WizardWizard W;

        public FormPanel(WizardWizard W) {
            super(new BorderLayout());
            this.W = W;
            OpnList = new DefaultListModel();
            listOps = new JList(OpnList);
            add((new JScrollPane(listOps)), BorderLayout.CENTER);
            JPanel JP = new JPanel(new GridLayout(5, 1));
            JButton jb = new JButton(ADD);
            jb.addActionListener(W);
            JP.add(jb);
            jb = new JButton(DELETE);
            jb.addActionListener(W);
            JP.add(jb);
            jb = new JButton(MOVE_UP);
            jb.addActionListener(W);
            JP.add(jb);
            jb = new JButton(MOVE_DOWN);
            jb.addActionListener(W);
            JP.add(jb);
            jb = new JButton("Help");
            JP.add(jb);
            jb.addActionListener(this);
            add(JP, BorderLayout.EAST);

        }

        public void actionPerformed(ActionEvent evt) {
            ListHolder opHlder = (ListHolder) listOps.getSelectedValue();
            GenericOperator op = opHlder.op;

            Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenheight = screensize.height;
            int screenwidth = (int) (screenheight * 4 / 3);

            FinishJFrame jf = new FinishJFrame("operator " + op.getCommand());
            JEditorPane jedPane = new JEditorPane();
            jedPane.setEditable(false);
            jedPane.setEditorKit(new HTMLEditorKit());
            jedPane.setText(SharedData.HTMLPageMaker.createHTML(op));
            JScrollPane scroll = new JScrollPane(jedPane);
            jf.getContentPane().add(scroll);
            jf.setSize((int) (screenwidth / 2), (int) (3 * screenheight / 4));
            WindowShower windShow = new WindowShower(jf);
            EventQueue.invokeLater(windShow);
            

        }
    }
   

    private class VarPanel extends JPanel implements ItemListener,
            ListSelectionListener, ActionListener, Serializable {

        WizardWizard W;

        JPanel IP;

        int FrommForm = -1, toForm = -1;

        JList FrVars;

        JList ToVars;

        JComboBox Fromm;

        JComboBox To;

        JComboBox RetTypes;

        JLabel from, to, param, infoLabel;

        DefaultListModel FrModel, ToModel;

        int FrSel = -1;

        int ToSel = -1;

        public String[] RetStrings = { "Array", "String", "LoadFile",
                "SaveFile", "Boolean", "DataSet", "Float", "InstName",
                "Integer", "IntArray", "Material", "MonitorDataSet",
                "SampleDataSet", "PulseHeight", "PrinterName", "PlaceHolder" };

        public VarPanel(WizardWizard W) {
            super();
            IP = new JPanel();

            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints layoutConstraints = new GridBagConstraints();
            IP.setLayout(layout);
            from = new JLabel("  From:");
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 0;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.insets = new Insets(12, 12, 3, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = 1.0;
            layoutConstraints.weighty = 1.0;// change 0.0
            layout.setConstraints(from, layoutConstraints);

            IP.add(from);
            to = new JLabel("To:");
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 0;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(12, 12, 3, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(to, layoutConstraints);
            IP.add(to);

            Fromm = new JComboBox();
            FrModel = new DefaultListModel();
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 1;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(3, 12, 3, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(Fromm, layoutConstraints);
            IP.add(Fromm);
            Fromm.addItemListener(this);

            FrVars = new JList(FrModel);
            FrVars.addListSelectionListener(this);
            // This line sets the default width (in character units) of the list
            FrVars.setPrototypeCellValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            JScrollPane scrollPane = new JScrollPane(FrVars,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 2;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(3, 12, 3, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(scrollPane, layoutConstraints);
            IP.add(scrollPane);

            JButton connectButton = new JButton("Connect");
            connectButton.setMnemonic('C');
            connectButton.setToolTipText("Toggle Connection");
            connectButton.addActionListener(this);
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 2;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.ipadx = 5;
            layoutConstraints.ipady = 0;
            layoutConstraints.insets = new Insets(12, 3, 3, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(connectButton, layoutConstraints);
            IP.add(connectButton);
            
            JButton displayButton = new JButton("Display");
            displayButton.setMnemonic('D');
            displayButton.setToolTipText("Toggle Connection");
            displayButton.addActionListener(this);
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 2;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.CENTER;
            layoutConstraints.ipadx = 5;
            layoutConstraints.ipady = 0;
            layoutConstraints.insets = new Insets(80, 3, 3, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(displayButton, layoutConstraints);
            IP.add(displayButton);

            To = new JComboBox();
            To.addItemListener(this);
            ToModel = new DefaultListModel();
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 1;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(3, 3, 3, 12);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(To, layoutConstraints);
            IP.add(To);

            // This line sets the default width (in character units) of the list
            ToVars = new JList(ToModel);
            ToVars.addListSelectionListener(this);
            ToVars.setPrototypeCellValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            JScrollPane scrollPane1 = new JScrollPane(ToVars,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            ToVars.setSelectedIndex( 1 );
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 2;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(3, 3, 3, 12);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(scrollPane1, layoutConstraints);
            IP.add(scrollPane1);

            param = new JLabel("  Set Parameter for Result:");
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 3;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(3, 12, 0, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(param, layoutConstraints);
            IP.add(param);

            RetTypes = new JComboBox(RetStrings);
            RetTypes.setToolTipText("Set Parameter for Result(Left)");
            RetTypes.addActionListener(this);
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 4;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.BOTH;
            layoutConstraints.insets = new Insets(0, 12, 12, 3);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(RetTypes, layoutConstraints);
            IP.add(RetTypes);

            JButton infoButton = new JButton("Info");
            infoButton.setMnemonic('i');
            layoutConstraints.gridx = 2;
            layoutConstraints.gridy = 3;
            layoutConstraints.gridwidth = 1;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.NONE;
            layoutConstraints.ipadx = 10;
            layoutConstraints.ipady = 0;
            layoutConstraints.insets = new Insets(3, 3, 3, 12);
            layoutConstraints.anchor = GridBagConstraints.WEST;
           
            layoutConstraints.weightx = layoutConstraints.weighty = 1.0;
            layout.setConstraints(infoButton, layoutConstraints);
            infoButton.addActionListener(this);
            IP.add(infoButton);
            add(IP);

            infoLabel = new JLabel("  ");
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 4;
            layoutConstraints.gridwidth = 2;
            layoutConstraints.gridheight = 1;
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.insets = new Insets(3, 12, 12, 12);
            layoutConstraints.anchor = GridBagConstraints.CENTER;
            layoutConstraints.weightx = 1.0;
            layoutConstraints.weighty = 1.0;
            layout.setConstraints(infoLabel, layoutConstraints);
            IP.add(infoLabel);

            //Fromm.setEnabled(true);
            To.setEnabled(true);
            this.addComponentListener(new CompListener(this));
            
            SetUpLists();
        }

        int LastSelList = -1;

        int LastSelItem = -1;

        public void valueChanged(ListSelectionEvent evt) {
            if (evt.getSource() == FrVars)
                LastSelList = 0;
            else if (evt.getSource() == ToVars)
                LastSelList = 1;
            else
                LastSelList = -1;
            LastSelItem = evt.getFirstIndex();

        }

        public void itemStateChanged(ItemEvent evt) {
            if (evt.getSource() == RetTypes) {
                int k = Fromm.getSelectedIndex();
                if (k < 0)
                    return;
                
                
                  lp.ResultPG[k]= RetTypes.getSelectedItem().toString();
                  //SetUpLists();
                return;
            }
            boolean change = false;
            if( Fromm.getSelectedIndex() >= To.getSelectedIndex())
              if( Fromm.getSelectedIndex() >=0 && To.getSelectedIndex() >=0){
               JOptionPane.showMessageDialog(null , " The From form must be before "+
                          "the To form--Ignored");
               return;
            }
            
            if (Fromm.getSelectedIndex() != FrSel) {
               if( FrSel >=0 && FrSel < lp.ResultPG.length)
                  if(lp.ResultPG[FrSel] == null)
                     lp.ResultPG[FrSel]= RetTypes.getSelectedItem().toString();
                FrSel = Fromm.getSelectedIndex();
        
                FrModel.clear();
                change = true;
            }
            if (To.getSelectedIndex() != ToSel) {
                ToSel = To.getSelectedIndex();
                ToModel.clear();
                change = true;

            }

            if (change)
                SetUpLists();

        }
        
        
        

        public void actionPerformed(ActionEvent evt) {
           
            if( evt.getActionCommand().equals( "refresh" )){
               SetUpLists();
               return;
            }
            if (evt.getSource() == RetTypes) {

                int k = Fromm.getSelectedIndex();
                if (k < 0)
                    return;
                
                lp.ResultPG[k] = RetTypes.getSelectedItem().toString();
                SetUpLists();
                return;

            }
            if (evt.getActionCommand() == DISPLAY) {
                lp.displayTable();
               
            } else if (evt.getActionCommand() == CONNECT) {
               
                //SetFormNums(opns);
               int kLeft = FrVars.getSelectedIndex();
               
               
                int kRight = ToVars.getSelectedIndex();
          
                if( kLeft == FrVars.getModel().getSize()-1)
                   if(lp.ResultPG[FrSel] == null)
                      lp.ResultPG[FrSel] =RetTypes.getSelectedItem().toString();
              //  System.out.println("getItemCount"+To.getItemCount());
               
                if(lp.addLink(FrSel, kLeft, ToSel, kRight)){
              //  System.out.println("frsel = "+FrSel +" kleft " + kLeft);
              
                //SetUpLists();
                lp.createTable();
                lp.GetLinkTable();
                }else{
                   lp.removeLink( ToSel , kRight);
                }
             
                
            } else if (evt.getActionCommand().equals("Info")) {
                ParListInf Par = (ParListInf) FrVars.getSelectedValue();
                if (Par == null) {
                    
                    infoLabel.setText("No Selection in Left List");
                    return;
                }
                if (Par.param == null) {
                   
                    infoLabel.setText("Parameter is null");
                    return;
                }
                if (!(Par.param instanceof ParameterGUI)) {
                    
                    infoLabel.setText("Parameter is not a ParameterGUI");
                    return;
                }
                infoLabel.setText(Fromm.getSelectedItem().toString() + "-Param"
                        + Par.param.getName());
                
                String S = "ParamGUI:" + Par.param.getType();
                Object Val = Par.param.getValue();
                S += "\r\n Value Class:";

                if (Val == null)

                    S += ("null");
                else
                    S += Val.getClass();

                infoLabel.setText(S);
               

            }
        }

       // ToDO: Make sure any par in a form with a connection to a Result
        // parameter
        // is not connected to any form previous to the Result
    
        
        /**
         * Checks to see if adding opp2 /Par2Num to opp1 will cause a Result
         * parameter to be linked with a parameter in a form previous to that
         * form. Returns true if it is ok to add , otherwise it return false.
         * 
         * @param opp1
         * @param Par1Num
         * @param opp2
         * @param Par2Num
         * @return
         */
      
        private void SetUpLists() {
            FrSel= ToSel = -1;
            if( Fromm != null)
               FrSel = Fromm.getSelectedIndex();
            if( To != null )
               ToSel = To.getSelectedIndex();
            FrModel.clear();
            
            
            if( FrSel >=0){
               
               GenericOperator FrOp = ((ListHolder) (Fromm.getSelectedItem())).op;
               for (int i = 0; i < FrOp.getNum_parameters(); i++)
                   FrModel.addElement(new ParListInf(FrOp.getParameter(i)));
            
               String Res = lp.ResultPG[ FrSel];
               FrModel.addElement(  new ParListInf( MakeParameter("Result", 
                                      Res) ));
               if(Res != null)
                  RetTypes.setSelectedItem( Res );
            }

            ToModel.clear();
            if( ToSel >=0){

               ToModel.clear();
               GenericOperator ToOp = ((ListHolder) (To.getSelectedItem())).op;
               for (int i = 0; i < ToOp.getNum_parameters(); i++)
                  ToModel.addElement(new ParListInf(ToOp.getParameter(i)));   
            }
          
        }
        
        private IParameter MakeParameter(String Prompt, String ResType){
           if( Prompt == null)
              Prompt="";
           
           
          return new PlaceHolderPG(Prompt, null);
          
           
         
        }

    }

    /**
     * 
     * @author MikkelsonR This class is a wrapper around a parameter that gives
     *         a toString() value that indicates its connect number( to see
     *         corresponding connect in other list) and whether it is a
     *         constant.
     */
    private class ParListInf implements Serializable {
        IParameter param;

        String ConnectNum;

        boolean CONST;

        public ParListInf(IParameter param) {
            this.param = param;
            ConnectNum = "";
            CONST = false;
        }

        public void setCONST(boolean val) {
            CONST = val;
        }

        public void setConnectNum(String ConnectNum) {
            this.ConnectNum = ConnectNum;
        }

        public String toString() {
            String S = "";
            if (CONST)
                S = "C";
            //if (ConnectNum >= 0)
                S += ConnectNum;
            if (S.length() >= 1)
                S += "    :";
            if (param == null)
                S += "Result";
            else
                S += param.getName();
            return S;
        }
    }

    /**
     * 
     * @author MikkelsonR Listens to when the Variable panel becomes shown. The
     *         whole Panel is redrawn Fromm data.
     */
    private class CompListener extends ComponentAdapter implements Serializable {
        VarPanel vPanel;

        public CompListener(VarPanel vPanel) {
            this.vPanel = vPanel;
        }

        public void componentShown(ComponentEvent e) {
            vPanel.Fromm.removeAllItems();
            vPanel.To.removeAllItems();
            
            for (int i = 0; i < OpnList.size(); i++) {
                vPanel.Fromm.addItem(OpnList.elementAt(i));
                vPanel.To.addItem(OpnList.elementAt(i));
            }
            if( vPanel.Fromm.getItemCount()>0){
               vPanel.Fromm.setSelectedIndex( 0);
               vPanel.FrSel = 0;
            }
            if( vPanel.To.getItemCount()>1){
               vPanel.To.setSelectedIndex( 1 );
               vPanel.ToSel=1;
            }
           
           vPanel.FrModel.clear();
           vPanel.ToModel.clear();
           vPanel.actionPerformed( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "refresh") );
           vPanel.Fromm.show();
           vPanel.To.show();
        }

    }

    private class FilePanel extends JPanel implements Serializable {

        WizardWizard W;

        public FilePanel(WizardWizard W) {
            super(new FlowLayout());
            this.W = W;
            JButton jb = new JButton(SAVE);
            jb.addActionListener(W);
            add(jb);

            jb = new JButton(SAVE_STATE);
            jb.addActionListener(W);
            add(jb);
            jb = new JButton(OPEN);
            jb.addActionListener(W);
            add(jb);
        }

    }

    /**
     * 
     * @author MikkelsonR
     * 
     * A wrapper around a Generic Operator to be placed in a list so that the
     * resultant string gives both the command and title of the operator
     */
    private class ListHolder implements Serializable {  
        GenericOperator op;

        String filename;

        public ListHolder(GenericOperator op, String filename)
                throws IllegalArgumentException {
            this.op = op;
            this.filename = filename;
            if (op == null)
                throw new IllegalArgumentException();
            
        }

        public GenericOperator getop() {
            return op;
        }

        public String toString() {
            return op.getCommand() + "::" + op.getTitle();
        }

    }

    public class LinkParameters implements Serializable{

        Vector<GenericOperator> opns;

        Vector<int[]> ArgLinks;

        String[] ResultPG;

        int[][] LinkInfoData;
        int[][] tableB;
        

        public LinkParameters(Vector<GenericOperator> opns) {
            this.opns = opns;
            ArgLinks = new Vector<int[]>();
            ResultPG = new String[opns.size()];
            LinkInfoData = null;
            tableB = null;
            maxParams = 0;
            for(int i=0; i<opns.size();i++)
                if( opns.elementAt(i).getNum_parameters() +1 >maxParams)
                    maxParams = opns.elementAt(i).getNum_parameters()+1;
        }

        public int[][] getLinks() {
            if( tableB == null)
             
                createTable();
            return tableB;
        }

        public boolean addLink(int fromForm, int fromParamNum, int toForm,
                int toParamNum) {
       
            // check if parameters are valid
            
           if (fromForm < 0 || toForm > opns.size() || toForm < 0
                    || fromForm > opns.size()) {
        
                JOptionPane.showMessageDialog(null,
                        "number of form < 0 or > than ...", "Invalid argument",
                        JOptionPane.ERROR_MESSAGE);

                return true;
            }
            
           
            GenericOperator from = (GenericOperator)(opns.elementAt(fromForm));
            GenericOperator to = opns.elementAt(toForm);
          
           
 
            int N = from.getNum_parameters();
            int M = to.getNum_parameters();
          //  System.out.println("number of parameters in fromform " +from.getNum_parameters() + " N= " +N);
           // System.out.println("number of parameters in toform "+to.getNum_parameters() +" M= "+M);
            String first_par;
            String second_par = null;

           if (fromForm >= toForm) {
             
                JOptionPane.showMessageDialog(null, "first form > second form",
                        "Invalid argument", JOptionPane.ERROR_MESSAGE);
             
                return  true;
            }
            
           

           if (fromParamNum < 0 || toParamNum < 0 || fromParamNum > N + 1
                    || toParamNum >= M) {
            
                JOptionPane.showMessageDialog(null,
                        "wrong parameters: check first or second argument",
                        "Invalid argument", JOptionPane.ERROR_MESSAGE);
                
                return true;
           }
            

            // ---------Check parameterGUI's should be the same
            // class--------------------
           
           if ( fromParamNum <=N && toParamNum < M ) {
               if( fromParamNum < N)
                first_par = from.getParameter(fromParamNum).getType();
              
               else
                   first_par = ResultPG[fromForm];
               
                if( first_par == null)
                   first_par ="";
                
                if( to != null){
                   
                second_par = to.getParameter(toParamNum).getType();
                }
           
                if (!first_par.equals(second_par)) {
             
                    JOptionPane.showMessageDialog(null,
                            "parameters are different data types",
                            "Invalid argument", JOptionPane.ERROR_MESSAGE);
                 
                    return  true;
                }
                
            }
            // ------if result parameter cannot be a to parameter

            if (toParamNum == M) {
             
                JOptionPane.showMessageDialog(null,
                        "second parameter cannot be a result",
                        "Invalid argument", JOptionPane.ERROR_MESSAGE);
                
                return  true;

            }
         

            // -------------- that toForm and toParamNum are not already listed
            // as to's in ArgLink
            for (int i = 0; i < ArgLinks.size(); i++) {

                int[] elt = ArgLinks.elementAt(i);
                if (elt[2] == toForm && elt[3] == toParamNum) {
              
                    JOptionPane.showMessageDialog(null, "already listed",
                            "Will be REMOVED", JOptionPane.INFORMATION_MESSAGE);
                  
                    return false;
                }
               
            }
            
          //  System.out.println("MAX: " + maxParams);
           
            int[] item = new int[4];
            item[0] = fromForm;
            item[1] = fromParamNum;
            item[2] = toForm;
            item[3] = toParamNum;
            
            ArgLinks.add(item);
            LinkInfoData = null;
            return true;
        }

        // remove link 
        public void removeLink(int FormNum, int ParamNum) {
            tableB = null;
            boolean removed = false;
            for (int i = 0; i < ArgLinks.size(); i++) {
                int[] elt = ArgLinks.elementAt(i);
                if (elt[2] == FormNum && elt[3] == ParamNum) {
                    ArgLinks.remove(i);
                    removed = true;
                    LinkInfoData = null;
                }
            }

            if (removed == false) {

                JOptionPane.showMessageDialog(null, "no such link present: "
                        + FormNum + " " + ParamNum, "Invalid argument",
                        JOptionPane.ERROR_MESSAGE);
            }

        } // end method

        public void createTable() {

            tableB = new int[ArgLinks.size()][opns.size()];
            
            Object[] sortArgs = ArgLinks.toArray();
            try {
               // System.out.println(sortArgs.getClass());
                java.util.Arrays.sort(sortArgs, new MyComparator());
                
            } catch (Throwable s) {
                System.out.println("Error=" + s.toString());
                s.printStackTrace();
            }
          for (int i = 0; i < ArgLinks.size(); i++) {
                    for (int j = 0; j < opns.size(); j++) {
              
                     tableB[i][j] = -1;
                  }
           }

              
         
             for (int i=0; i<sortArgs.length; i++){
            
            tableB[i][((int[])sortArgs[i])[0]] = ((int[])sortArgs[i])[1];
            tableB[i][((int[])sortArgs[i])[2]] = ((int[])sortArgs[i])[3];
        }
        

             
        }// end createTable
       
        public String[][] GetLinkTable(){
            
            int numForms = opns.size();
            String alphabet[] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
            
         
            Object[] sortArgs = ArgLinks.toArray();
            try {
               
                java.util.Arrays.sort(sortArgs, new MyComparator());
              
            } catch (Throwable s) {
                System.out.println("Error=" + s.toString());
                s.printStackTrace();
            }
            
         
        String newTable[][] = new String[maxParams][numForms];
        
        for(int i=0; i<maxParams; i++){
            for(int j=0; j<numForms; j++){
                newTable[i][j]= alphabet[j]+i;
                newTable[i][j]=newTable[i][j].toLowerCase();
                
            }
        }
        try{
            for (int i=0; i<sortArgs.length; i++){
               
               
                newTable[((int[])sortArgs[i])[3]] [((int[])sortArgs[i])[2]]= 
                           newTable[((int[])sortArgs[i])[1]][((int[])sortArgs[i])[0]].toUpperCase();
                newTable[((int[])sortArgs[i])[1]] [((int[])sortArgs[i])[0]]=
                   newTable[((int[])sortArgs[i])[1]] [((int[])sortArgs[i])[0]].toUpperCase();
                
            }
         
           }catch(Throwable s){
              
               s.printStackTrace();
           }
          
            return newTable;
        
        }
          
     
        public void displayTable(){
            JFrame displayPanel = new JFrame("Connection");
            
            String[][] newTable = GetLinkTable(); 
            String[] colNames = new String[ newTable[0].length];
            String colnames[] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
            
           
            for(int i=0;i<colNames.length;i++){
                colnames[i] = ""+(i+1);
            
                colNames[i]=""+(i+1);
            }
            JTable displayTable = new JTable(newTable,colNames);
           
            displayTable.setFont( new Font("Default", Font.PLAIN, 20)) ; 
            displayTable.setRowHeight(40); 
            
            //displayPanel.getContentPane().add(displayNames);
            displayPanel.getContentPane().add(displayTable);
          
           displayPanel.setSize(400,200);
           displayPanel.setVisible(true);
           displayPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          
            
        }
    

        public int[][] createLinkInfo() {
            if (LinkInfoData != null)
                return LinkInfoData;
            return null;
        }
        
      

    }

    class MyComparator implements Comparator {
        public int compare(Object a, Object b) {
            if (!(a instanceof int[]))
                throw new ClassCastException("must be int[] elts");

            if (!(b instanceof int[]))
                throw new ClassCastException("must be int[] elts");
            int[] aa, bb;
            aa = (int[]) a;
            bb = (int[]) b;
            if (aa[0] < bb[0])
                return -1;
            if (aa[0] > bb[0])
                return 1;
            if (aa[1] < bb[1])
                return -1;
            if (aa[1] > bb[1])
                return 1;
            if (aa[2] < bb[2])
                return -1;
            if (aa[2] > bb[2])
                return 1;
            if (aa[3] < bb[3])
                return -1;
            if (aa[3] > bb[3])
                return 1;
            return 0;
        }

        public boolean equals(Object o) {
            if (o instanceof MyComparator)
                return true;
            return false;
        }

    }

    /**
     * 
     * @author Mikkelson An element in a linked list of forms. It stores the
     *         operator, the list of constant parameters, and the list of
     *         linking of variables.
     */
 

         


    private void WriteGPL(FileOutputStream FSave) throws IOException {
        FSave
                .write(("/*\r\n *File:  "
                        + infPanel.WizardName.getText().trim() + ".java\r\n")
                        .getBytes());
        FSave
                .write((" *\r\n * Copyright (C) "
                        + infPanel.Name.getText().trim() + "\r\n *\r\n")
                        .getBytes());
        FSave
                .write(" * This program is free software; you can redistribute it and/or \r\n"
                        .getBytes());
        FSave
                .write(" *modify it under the terms of the GNU General Public License\r\n"
                        .getBytes());
        FSave
                .write(" *as published by the Free Software Foundation; either version 2\r\n"
                        .getBytes());
        FSave
                .write(" *of the License, or (at your option) any later version.\r\n"
                        .getBytes());
        FSave.write(" *\r\n".getBytes());
        FSave
                .write(" *This program is distributed in the hope that it will be useful,\r\n"
                        .getBytes());
        FSave
                .write(" *but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n"
                        .getBytes());
        FSave
                .write(" *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\r\n"
                        .getBytes());
        FSave.write(" *GNU General Public License for more details.\r\n"
                .getBytes());
        FSave.write(" *\r\n".getBytes());
        FSave
                .write(" * You should have received a copy of the GNU General Public License\r\n"
                        .getBytes());
        FSave
                .write(" * along with this library; if not, write To the Free Software\r\n"
                        .getBytes());
        FSave
                .write(" * Foundation, Inc., 59 Temple Place, Suite 330, BosTon, MA  02111-1307, USA.\r\n"
                        .getBytes());
        FSave.write(" *\r\n".getBytes());
        FSave.write((" *Contact:" + infPanel.Name.getText().trim() + ","
                + infPanel.Email.getText().trim() + "\r\n").getBytes());
        int k = 0;
        String Addr = infPanel.Address.getText().trim();
        int k1 = Addr.indexOf('\n');
        if (k1 < 0)
            k1 = Addr.length();
        while (k < Addr.length()) {

            FSave.write((" *        " + Addr.substring(k, k1) + "\r\n")
                    .getBytes());
            k = k1 + 1;
            if (k < Addr.length())
                k1 = Addr.indexOf('\n', k1 + 1);
            if (k1 < 0)
                k1 = Addr.length();
        }
        FSave.write(" *\r\n".getBytes());

        k = 0;
        String Acknow = infPanel.Acknowl.getText().trim();
        System.out.println("Acknow=" + Acknow);
        k1 = Acknow.indexOf('\n');
        if (k1 < 0)
            k1 = Acknow.length();
        while (k < Acknow.length()) {
            try {
                FSave.write((" *" + Acknow.substring(k, k1) + "\r\n")
                        .getBytes());
            } catch (Exception sss) {
                sss.printStackTrace();
                throw new IOException("sss");
            }
            k = k1 + 1;
            if (k < Acknow.length())
                if (k1 + 1 < Acknow.length())
                    k1 = Acknow.indexOf('\n', k1 + 1);
                else
                    k1 = -1;

            if (k1 < 0)
                k1 = Acknow.length();

        }

        FSave.write(" *\r\n".getBytes());

        FSave.write(" *\r\n".getBytes());
        FSave.write(" *\r\n".getBytes());
        FSave.write(" * Modified:   Log:".getBytes());
        FSave.write((infPanel.WizardName.getText().trim() + ".java,")
                .getBytes());
        FSave.write(" v$\r\n".getBytes());

        FSave.write(" *\r\n".getBytes());
        FSave.write((" *").getBytes());

    }

    private void WritePackage(FileOutputStream FSave, File FSaveFile)
            throws IOException {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null)
            return;
        if (!classpath.endsWith(File.pathSeparator))
            classpath += File.pathSeparatorChar;
        int k = classpath.indexOf(File.pathSeparatorChar);
        int k1 = 0;
        int l = -1;
        for (; (l < 0) && (k1 + 1 < classpath.length()); k = classpath
                .indexOf(File.pathSeparatorChar)) {
            String CPath = classpath.substring(k1, k - k1);
            l = FSaveFile.getAbsolutePath().indexOf(
                    (new File(CPath)).getAbsolutePath());
            if (l == 0) {
                String pack = FSaveFile.getAbsolutePath().substring(
                        (new File(CPath).getAbsolutePath().length()));
                if (!(Character.isJavaLetterOrDigit(pack.charAt(0))))
                    pack = pack.substring(1);
                int kk = pack.lastIndexOf(File.separatorChar);
                if (kk >= 0)
                    pack = pack.substring(0, kk);
                pack = pack.replace(File.separatorChar, '.');
                FSave.write(("package " + pack + ";\r\n\r\n").getBytes());
            }
            k1 = k + 1;
        }

    }

    private void WriteImports(FileOutputStream FSave, String rest)
            throws IOException {

        FSave.write("import DataSetTools.wizard.*;\n\r".getBytes());
        FSave.write("import DataSetTools.parameter.*;\r\n".getBytes());
    }

    private void WriteClass(FileOutputStream FSave, String rest)
            throws IOException {
        FSave
                .write(("public class " + infPanel.WizardName.getText().trim() + " extends Wizard \r\n")
                        .getBytes());
        if (rest != null)
            if (rest.indexOf("implements") >= 10) {
                int k = rest.indexOf("implements");
                int k3 = rest.indexOf("extends", k + 1);
                if (k3 < 0)
                    k3 = rest.length();
                FSave.write((rest.substring(k, k3 - k) + "{\r\n").getBytes());

            }
        FSave.write("{\r\n".getBytes());

    }

    private void WriteLinks(FileOutputStream FSave) throws IOException {

    }

    int[] NConstants = null;

    private void WriteConstr(FileOutputStream FSave, String rest)
            throws IOException {
        // Write the links and const
        NConstants = new int[OpnList.size()];
        FSave.write("     int[][] ParamTable= {".getBytes());
        int NLinks = 0;
       
                       
       
        String SLink="";
        int[][] tableB = lp.getLinks();
        //To do  print out the table in a nice form
        for( int i=0; i< tableB.length;i++){
           FSave.write("{".getBytes());
           for( int j=0; j< tableB[i].length; j++ ){
              FSave.write( (""+tableB[i][j]).getBytes());
              if( j+1 < tableB[i].length)
                 FSave.write(",".getBytes());
              else
                 FSave.write("}".getBytes());
                 
           }
           if( i+1 < tableB.length)
              FSave.write( ",\r\n                          ".getBytes());
           
              
        }
        FSave.write((SLink + "\r\n            };\r\n\r\n").getBytes());

       SLink = "     int[][]ConstList ={\r\n";
        //ToDo Find the constants
        boolean start = true;
        Arrays.fill(NConstants, 0);
       
       // FSave.write(SLink.getBytes());

        FSave
                .write(("   public " + infPanel.WizardName.getText().trim() + "( ){\r\n")
                        .getBytes());

        FSave.write(("        this( false);\r\n      };\r\n\r\n".getBytes()));

        FSave
                .write(("   public " + infPanel.WizardName.getText().trim() + "(boolean standalone ){\r\n")
                        .getBytes());

        FSave
                .write(("        super(\""
                        + infPanel.WizardTitle.getText().trim() + "\",standalone);\r\n     \r\n\r\n")
                        .getBytes());

    }

    private void WriteForms(FileOutputStream FSave) throws IOException {

        for (int i = 0; i < OpnList.size(); i++) {
            GenericOperator op = ((ListHolder) (OpnList.elementAt(i))).op;

            String ResParamGui = lp.ResultPG[i];
            if (ResParamGui == null)
                ResParamGui = "";
            if (ResParamGui.length() > 1)
                ResParamGui = ", new " + ResParamGui + "PG(\"Result" + i
                        + "\",null)";
            String ConstList = "";
            if ((ResParamGui.length() > 1)) {
                if (NConstants[i] > 0)
                    ConstList = ",ConstList[" + i + "]";
                else
                    ConstList = ",new int[0]";
            }
            if (op instanceof ScriptOperator) {
                String filename = op.getSource().trim().replace('\\', '/');
                String Prefix = System.getProperty("ISAW_HOME", "").replace(
                        '\\', '/');
                if (!Prefix.endsWith("/"))
                    Prefix += "/";
                Prefix = Prefix + "Scripts/";
                if (filename.indexOf(Prefix) != 0) {
                    JOptionPane
                            .showMessageDialog(null,
                                    "ISAW Scripts must be in ISAWHOME's Script direcTory");
                    System.exit(0);
                }

                FSave.write(("   addForm( new ScriptForm(\""
                        + filename.substring(Prefix.length()) + "\""
                        + ResParamGui + ConstList + "));\r\n").getBytes());
            } else if (op instanceof PyScriptOperator) {
                FSave.write(("   addForm( new JyScriptForm( \""
                        + op.getSource().trim().replace('\\', '/') + "\""
                        + ResParamGui + ConstList + "));\r\n").getBytes());

            } else if (op instanceof JavaWrapperOperator) {
                FSave
                        .write(("    addForm( new OperaTorForm( new JavaWrapperOperaTor( new "
                                + op.getSource().substring(6).trim() + "())));\r\n")
                                .getBytes());
            } else {
                if ((ResParamGui.length() < 1) || (ConstList.length() < 1))
                    ResParamGui = ConstList = "";
                FSave.write(("    addForm( new OperaTorForm(  new "
                        + op.getSource().substring(6).trim() + "()"
                        + ResParamGui + ConstList + "));\r\n").getBytes());
            }
        }
        FSave.write("      linkFormParameters( ParamTable );\r\n".getBytes());
    }

    private void WriteHelp(FileOutputStream FSave) throws IOException {
        if (this.HTMLDocFileName == null) {
            FSave.write("   String S=\"\" ;\r\n".getBytes());
            String T = docPanel.Docum.getText();
            int k = 0, k1 = T.indexOf('\n');
            if (k1 < 0)
                k1 = T.length();
            while (k < T.length()) {
                FSave.write(("    S+=\"" + T.substring(k, k1) + "\";\n")
                        .getBytes());
                k = k1 + 1;
                k1 = T.indexOf('\n', k);
                if (k1 < 0)
                    k1 = T.length();
            }
            FSave.write("    setHelpMessage( S);\r\n".getBytes());

        } else {

            FSave
                    .write("     String Prefix = System.getProperty(\"Docs_DirecTory\",\"\");  \r\n "
                            .getBytes());
            FSave.write("     if( !Prefix.endsWith( File.separaTor))\r\n"
                    .getBytes());
            FSave
                    .write("         Prefix=Prefix+File.separaTor;\r\n"
                            .getBytes());
            FSave.write("     Prefix=Prefix.replace(':','|');\r\n".getBytes());
            FSave.write("     Prefix=Prefix.replace('\\','/');\r\n".getBytes());
            FSave
                    .write(("   setHelpURL(\"file://+Prefix+\""
                            + HTMLDocFileName + "\");\r\n").getBytes());
        }
    }

    private void WriteMain(FileOutputStream FSave, String rest) {
        try {

            FSave.write(("   public static void main( String[] args){\r\n")
                    .getBytes());
            FSave
                    .write(("      " + infPanel.WizardName.getText().trim()
                            + " Wiz= new "
                            + infPanel.WizardName.getText().trim() + "(true);\r\n")
                            .getBytes());
            FSave.write("    Wiz.wizardLoader( args );\r\n   }\r\n\r\n}\r\n"
                    .getBytes());
        } catch (Exception s) {
        }
    }

    boolean inComment1Line, inCommentMultiLine, inQuotes, inMethod, inClass;

    private Vector getNextSection(FileInputStream fin) throws IOException {
        inComment1Line = inCommentMultiLine = inQuotes = inMethod = inClass = false;
        if (true) {
            Vector V = new Vector();
            V.addElement(new Integer(-1));
            V.addElement("");
            return V;
        }

        return null;
    }

    public static void main1(String[] args) {
        WizardWizard w = new WizardWizard();
        Vector<GenericOperator> opns = new Vector<GenericOperator>();
        opns
                .addElement(new ScriptOperator(
                        "C:/ISAW/Scripts/Examples/Form1.iss"));

        opns
                .addElement(new ScriptOperator(
                        "C:/ISAW/Scripts/Examples/Form2.iss"));
        opns
                .addElement(new ScriptOperator(
                        "C:/ISAW/Scripts/Examples/Form3.iss"));
        //TODO replace all lp=null by initLinkParameters//done
        w.initLinkParameters(opns);
        w.lp.addLink(1, 3, 2, 2);
        w.lp.addLink(1, 3, 2, 2); //already listed
       // w.lp.printVector();
        w.lp.addLink(1, 2, 2, 0);
        //w.lp.printVector();
        // w.lp.removeLink(2,0);
     //   w.lp.removeLink(1, 1); //no such link
        w.lp.addLink(0, 3, 2, 1);
        w.lp.addLink(0, 2, 1, 1);
       // w.lp.printVector();
       // w.lp.removeLink(1, 1);
      //  w.lp.printVector();
        // w.lp.printVector();
        //w.lp.createTable();
      //  w.lp.GetLinkTable();
        //w.lp.addLink(1,1,2,3);
        //  System.out.println("")
        //  System.out.println("Through");
        //  gov.anl.ipns.Util.Sys.WindowShower.show( lp);

    }

}
