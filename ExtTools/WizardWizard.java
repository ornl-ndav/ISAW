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
 * Revision 1.1  2005/05/24 21:31:55  rmikk
 * Initial Checkin of Wizard to make Wizards
 *
 */

package ExtTools;
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
import javax.swing.event.*;
import javax.swing.text.html.*;
import gov.anl.ipns.Util.Sys.*;
/**
 * @author mikkelsonr
 *
 * This class produces a Wizard file from information entered by the user
 * or from information in the file.
 */
public class WizardWizard extends JFrame implements ActionListener, Serializable{
    JTabbedPane TabPane;
    //Script_Class_List_Handler SCL = new Script_Class_List_Handler();
    public static final long serialVersionUID =1L;
    transient DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();
    transient public static String SAVE ="Save Wizard";
    transient public static String SAVE_STATE = "Save State";
    transient public static String OPEN ="Restore State";
    transient public static String WIZ_DIR ="Wizard Directory";
    
    transient public static String USE_HTML_FILE="Use Html File";
    transient public static String ADD ="Add Operator";
    transient public static String DELETE ="Remove Operator";
    transient public static String MOVE_UP ="Move Selected Up";
    transient public static String MOVE_DOWN="Move Selected Down";
    transient public static String CONNECT="Connect";
    transient public static String CONST="<Constant";
    transient public static String RET_TYPE="<Return PG";
    
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
    boolean FormPanelChanged = true;
    LinkedList opns = new LinkedList();
    public WizardWizard(){
       super("Wizard Maker");
       TabPane = new JTabbedPane();
       setDefaultCloseOperation(EXIT_ON_CLOSE);
       infPanel=new InfoPanel(this);
       docPanel=new DocPanel(this);
       formPanel= new FormPanel(this);
       varPanel=new VarPanel(this);
       filePanel=new FilePanel(this);
       TabPane.add("Information",infPanel );
       TabPane.add("Documentation",docPanel );
       TabPane.add("Forms",formPanel);
       TabPane.add("Variables",varPanel );
       TabPane.add("File", filePanel);
       getContentPane().setLayout( new GridLayout(1,1));
       getContentPane().add( TabPane);
       LastOperator = System.getProperty("ISAW_HOME","");
       
    }
    public void actionPerformed( ActionEvent evt){
      if( evt.getActionCommand()==SAVE){
          Save( WizardDirectory);
      }else if( evt.getActionCommand() == SAVE_STATE){
          SaveState();       
      }else if( evt.getActionCommand() == OPEN){
      
          Open();
      }else if(evt.getActionCommand() == WIZ_DIR){
        WizardDirectory();
      }else if( evt.getActionCommand() == USE_HTML_FILE){
         if( !docPanel.jb.isSelected())
            HTMLDocFileName = null;
         else{
            JFileChooser jf = new JFileChooser();
            if( jf.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION)
            HTMLDocFileName = jf.getSelectedFile().getAbsolutePath();
            String Prefix = System.getProperty("Docs_Directory","");
            if( !Prefix.endsWith( File.separator))
              Prefix=Prefix+File.separator;
            if(HTMLDocFileName.indexOf( Prefix ) !=0)
              HTMLDocFileName = null;
            else
              HTMLDocFileName = HTMLDocFileName.substring( Prefix.length());
            
         }
      }else if(evt.getActionCommand() == ADD){
      
           AddOperator( OpnList);
      }else if(evt.getActionCommand() == DELETE){
           DeleteOperator( OpnList, listOps.getSelectedIndex() );
      }else if(evt.getActionCommand() == MOVE_UP){
           int selIndx =listOps.getSelectedIndex();
           if(MoveUpOp( OpnList, selIndx ))
             listOps.setSelectedIndex( selIndx-1);
      }else if(evt.getActionCommand() == MOVE_DOWN){
           int selIndx=listOps.getSelectedIndex();
           if(MoveDownOp( OpnList, selIndx))
             listOps.setSelectedIndex( selIndx+1);;
      }
    }
	public static void main(String[] args) {
      WizardWizard W = new WizardWizard();
      W.setSize(400,500);
      W.show();
    }
    
    private void Save( String WizardDirectory){
       
      String SaveFileName = WizardDirectory+infPanel.WizardName.getText().trim()+".java";
      FileInputStream Fop = null;
      
      FileOutputStream FSave=null;

      File FSaveFile = null;
      try{
          FSaveFile = new File( SaveFileName );
          FSave = new FileOutputStream( FSaveFile );
      }catch(Exception s){
           JOptionPane.showMessageDialog(null,"Cannot Save File:"+s.getMessage());
        return;
      }
      Vector V;
      int[] done= new int[11];
      Arrays.fill(done,0);
      int ndone=0;

      
      try{
        for( V=getNextSection( Fop ); ( ndone < done.length)&&(((Integer)(V.firstElement())).intValue()>=-1);
                V = getNextSection( Fop) ){
           int k= ((Integer)V.firstElement()).intValue();
            
           
           if( k < 0){// end of or NO open file
              if( done[ndone]>0)
                 ndone++;
              else
                k = ndone;
            } 
          
           if( k>12) 
              FSave.write(V.lastElement().toString().getBytes());
           
           else if( k==0){
           
             if(done[0] ==0)
                WriteGPL(FSave);
             done[0]=1;
           }else if( k==1){
                if( V.lastElement().toString().length()>1)
                   FSave.write(V.lastElement().toString().getBytes() );
                else
                   FSave.write("\r\n */ \r\n\r\n\r\n".getBytes());
                  done[1]=1;
           }else if(k==2){
           
              if( done[2]==0)
                 WritePackage(FSave, FSaveFile);
              done[2]=1;
           }else if(k==3){
              if( done[3]==0)
                WriteImports( FSave,V.lastElement().toString());
              done[3] =1;
           }else if( k == 4){
             if( done[4]==0)
                WriteClass(FSave, V.lastElement().toString());
             done[4] =1;
           }else if( k==5){
              if( done[5]==0)
                WriteLinks( FSave);
              done[5]=1;
           }else if( k==6){
              if( done[6]==0)
                 WriteConstr(FSave,V.lastElement().toString());
              done[6]=1;
           }else if( k==7){
             if( done[7] ==0)
                WriteForms(FSave);
             done[7] =1;
             
           }else if( k==8){
             if(done[8]==0)
              WriteHelp(FSave);
             done[8]=1;
           }
           else if( k == 9 ){
           
             if( done[9] == 0)
               FSave.write("\r\n   }\r\n\r\n".getBytes());
               done[9] = 1;
           }else if( k==10){
             if( done[10]== 0)
               WriteMain( FSave, V.lastElement().toString());
              done[10]=1;
           }
         }//for
        FSave.close();
      }catch(Exception ss){
        JOptionPane.showMessageDialog(null,"Cannot Save File "+ss.toString());
        
      }
    }
    private void SaveState(){
      try{
        FileOutputStream fout =new FileOutputStream( WizardDirectory+
                         infPanel.WizardName.getText().trim()+".wzb");
        ObjectOutputStream  Oout =new ObjectOutputStream( fout);
        Oout.writeObject( infPanel.Acknowl.getText());

        Oout.writeObject(infPanel.Address.getText());
        Oout.writeObject(infPanel.Email.getText());
        Oout.writeObject(infPanel.Instit.getText());
        Oout.writeObject(infPanel.Name.getText());
        Oout.writeObject(infPanel.WizardName.getText());
        Oout.writeObject(infPanel.WizardTitle.getText());
        Oout.writeObject(docPanel.Docum.getText());
        Oout.writeBoolean(docPanel.jb.isSelected());
        
        //LastOperator=null;
        Oout.writeObject(this.HTMLDocFileName);
        Oout.writeBoolean(this.FormPanelChanged);
        Oout.writeInt(OpnList.size());
        for( int i=0; i< OpnList.size(); i++)
           Oout.writeObject( ((ListHolder)(OpnList.elementAt(i))).filename);
        //Oout.writeObject(this.OpnList);
        
       //Oout.writeObject(this.opns);
       
       for( int i=0; i< opns.size(); i++)
          ((opLinkElement)(opns.get(i))).FormNum=i;
       Oout.writeInt(opns.size());
       for( int i=0;i<opns.size(); i++){
          opLinkElement elt = (opLinkElement)(opns.get(i));
       
          Oout.writeObject( elt.ConstList);
          Vector V = elt.ArgLinks;
         Oout.writeObject( elt.ResultPG);
          Oout.writeInt( V.size());
          
          for( int k=0; k< V.size(); k++){
             Vector W = (Vector)(V.elementAt(k));
             int f =((Integer)W.firstElement()).intValue(),
                 l =((Integer)W.lastElement()).intValue(),
                 m= ((opLinkElement)(W.elementAt(1))).FormNum;
             Oout.writeInt(f);
             Oout.writeInt(m);
             Oout.writeInt(l);
          }    
       }
        Oout.writeObject(this.WizardDirectory);
        Oout.close();
                }catch( Exception ss){
                   JOptionPane.showMessageDialog(null,"Cannot Save State:"+ss.toString());
                }
         
     
    }

    private void Open(){
      JFileChooser jf = new JFileChooser( System.getProperty("ISAW_HOME"));
      int res = jf.showOpenDialog( null);
      if( res != JFileChooser.APPROVE_OPTION)
        return;
      OpnList = new DefaultListModel();
      listOps.setModel( OpnList);
      opns = new LinkedList();
      try{
        FileInputStream finp = new FileInputStream( jf.getSelectedFile().getAbsolutePath());
        ObjectInputStream oinp= new ObjectInputStream( finp);
        Object O = oinp.readObject();
        infPanel.Acknowl.setText((String)O);
        infPanel.Address.setText((String)( oinp.readObject()));
        infPanel.Email.setText( (String)( oinp.readObject()));
        infPanel.Instit.setText((String)( oinp.readObject()));
        infPanel.Name.setText((String)( oinp.readObject()));
        infPanel.WizardName.setText((String)( oinp.readObject()));
        infPanel.WizardTitle.setText((String)( oinp.readObject()));
        docPanel.Docum.setText( (String)( oinp.readObject()));
        docPanel.jb.setSelected( ( oinp.readBoolean()));
        
        LastOperator= System.getProperty("ISAW_HOME");
        this.HTMLDocFileName=(String)( oinp.readObject());
        this.FormPanelChanged= oinp.readBoolean();
        int k = oinp.readInt();
        for( int i=0; i<k; i++){

           String filename = (String)( oinp.readObject());
           AddOperator( filename);
        }

        int opsize = oinp.readInt();
        for(int i = 0; i<opsize; i++){
          opLinkElement elt= (opLinkElement)(opns.get(i));
          elt.ConstList=((Vector)( oinp.readObject()));
          elt.ResultPG=(String)oinp.readObject();
          
          int k1 = oinp.readInt();
          for( int kk =0; kk<k1; kk++){
          
             int f= oinp.readInt(),
                 m = oinp.readInt(),
                 l=oinp.readInt();
             Vector r = new Vector();
             r.addElement(new Integer(f));
             r.addElement(opns.get(m));
             r.addElement( new Integer(l));
             elt.ArgLinks.addElement(r);
          }
        }
        this.WizardDirectory= (String)oinp.readObject();
        
      
       
        
      }catch(Throwable ss){
         JOptionPane.showMessageDialog(null,"Cannot Read the State file "+ss.toString());
      }
      
    }

    public void SetFormNums( LinkedList opns){
      for( int i=0; i< opns.size(); i++){
        ((opLinkElement)(opns.get( i ))).FormNum = i;
      }
    }
    private void WizardDirectory(){
      if( WizardDirectory == null){
      
        WizardDirectory = System.getProperty("ISAW_HOME");
        if( WizardDirectory == null)
           WizardDirectory ="";
        else if( !WizardDirectory.endsWith( File.separator)){
        
           WizardDirectory = WizardDirectory +File.separator;
           WizardDirectory +="Wizard";
        }
      }  
      JFileChooser jf = new JFileChooser(WizardDirectory );
      jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if( jf.showOpenDialog( null)!= JFileChooser.APPROVE_OPTION){
        WizardDirectory = null;
        return;
      }
      WizardDirectory =jf.getSelectedFile().getAbsolutePath();
      if( !WizardDirectory.endsWith( File.separator))
         WizardDirectory +=File.separator;
    }
 
 
    private void AddOperator( DefaultListModel OpnList){
      JFileChooser jf = new JFileChooser(LastOperator);
      if( jf.showOpenDialog( null ) !=  JFileChooser.APPROVE_OPTION)
         return;
      String SelFile = jf.getSelectedFile().getAbsolutePath();
      LastOperator = SelFile;
      AddOperator( SelFile);
    }
    private void AddOperator( String SelFile){
      try{
         GenericOperator op =null;
         if(SelFile.endsWith(".iss")){
            op = new ScriptOperator(SelFile);
            OpnList.addElement( new ListHolder( op ,SelFile));
         }else if( SelFile.endsWith(".py")){ 
            op= new PyScriptOperator( SelFile);         
            OpnList.addElement( new ListHolder( op, SelFile ));
         }else if( SelFile.endsWith(".class") || SelFile.endsWith(".java")){
           if( SelFile.endsWith(".java"))
             SelFile = SelFile.substring(0,SelFile.length()-5)+".class";
             op =(GenericOperator) Script_Class_List_Handler.myGetClassInst( SelFile, true);
            if(op == null){
               JOptionPane.showMessageDialog(null,"Cannot create operator/Form from "+ SelFile);
               return;
           }
           OpnList.addElement(new ListHolder(op, SelFile));
         }else{
            JOptionPane.showMessageDialog(null,"Cannot add this operator");
           return;
         }

        opns.add( new opLinkElement( op ));
           
      }catch( Exception s){
      
        JOptionPane.showMessageDialog(null,"Cannot create operator/Form from "+ SelFile);
      }   
   }
 
    private void DeleteOperator( DefaultListModel OpnList, int SelIndex ){
       if(SelIndex < 0)
         return;
       OpnList.remove( SelIndex ); 
       opns.remove(SelIndex);
       for( int i=SelIndex; i < opns.size(); i++){
          opLinkElement opElt =(opLinkElement)(opns.get(i));
          opElt.RemoveLinkRef( opElt);
       }
        
      
  }
 
    private boolean MoveUpOp( DefaultListModel OpnList, int SelIndex ){
      if(SelIndex < 1)
         return false;
      Object O = OpnList.remove( SelIndex);
      OpnList.add( SelIndex-1, O );
       opLinkElement elt=(opLinkElement)(opns.remove(SelIndex));
       opns.add(SelIndex-1, elt);
      return true;
   }
 
    private boolean  MoveDownOp( DefaultListModel OpnList , int SelIndex){
      if(SelIndex < 0)
         return  false;
      if(SelIndex+1 >= OpnList.size())
         return false;
      Object O = OpnList.remove( SelIndex);
      OpnList.add( SelIndex+1, O );
      opLinkElement elt=(opLinkElement)(opns.remove(SelIndex));
       opns.add(SelIndex+1, elt);
     
      return true;
        
    }
    private class InfoPanel extends JPanel implements Serializable{
       WizardWizard W;
       JTextField Name, Email, Instit, Address, WizardTitle,
           WizardName;
       JTextArea Acknowl;
       JButton Directory;
       
       public InfoPanel( WizardWizard W){
         super( new GridLayout( 8,1));
         this.W = W;
         JPanel JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Your Name"));
         Name = new JTextField();
         JP.add( Name );
         add( JP );JP=null;
         
         JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Your Email"));
         Email = new JTextField();
         JP.add( Email );
         add( JP );
         
         JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Institution"));
         Instit = new JTextField();
         JP.add( Instit );
         add( JP );
        
         JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Address"));
         Address = new JTextField();
         JP.add( Address );
         add( JP );
        
         JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Acknowlegements"));
         Acknowl = new JTextArea(3,30);
         JP.add( Acknowl );
         add( JP );
         
         JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Wizard Title"));
         WizardTitle = new JTextField();
         JP.add( WizardTitle );
         add( JP );
         
         JButton jb = new JButton(WIZ_DIR);
         jb.addActionListener( W );
         add(jb);
       
         JP= new JPanel( new GridLayout(1,2));
         JP.add( new JLabel("Name of Wizard File"));
         WizardName = new JTextField();
         JP.add( WizardName );
         add( JP );
                        
       }
    }
   
    private class DocPanel extends JPanel implements Serializable{
      WizardWizard W;
      JTextArea Docum;
      JCheckBox jb;
      public DocPanel( WizardWizard W){
        super( new BorderLayout());
        this.W = W;
       
        jb = new JCheckBox(USE_HTML_FILE);
        jb.addActionListener( W );
        add(jb, BorderLayout.NORTH);
        Docum = new JTextArea(3,30);
        add( new JScrollPane( Docum));
        
      }
    
    }
   
    private class FormPanel extends JPanel implements Serializable,
                                              ActionListener{
      WizardWizard W;
     
      public FormPanel( WizardWizard W){
        super( new BorderLayout());
        this.W = W;
        OpnList = new DefaultListModel();
        listOps = new JList( OpnList );
        add( (new JScrollPane(listOps)), BorderLayout.CENTER);
        JPanel JP = new JPanel( new GridLayout(5,1));
        JButton jb = new JButton( ADD);
        jb.addActionListener(W);
        JP.add(jb);
        jb = new JButton( DELETE);
        jb.addActionListener(W);
        JP.add(jb);
        jb = new JButton( MOVE_UP);
        jb.addActionListener(W);
        JP.add(jb);
        jb = new JButton( MOVE_DOWN);
        jb.addActionListener(W);
        JP.add(jb);
        jb = new JButton( "Help");
        JP.add(jb);
        jb.addActionListener( this);
        add(JP, BorderLayout.EAST);
        
        
      }
     public void actionPerformed( ActionEvent evt){
        ListHolder opHlder =(ListHolder) listOps.getSelectedValue();
        GenericOperator op = opHlder.op;
       
        Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
        int screenheight=screensize.height;
        int screenwidth=(int)(screenheight*4/3);
           
        FinishJFrame jf = new FinishJFrame( "operator "+op.getCommand());
        JEditorPane jedPane = new JEditorPane();
        jedPane.setEditable(false);
        jedPane.setEditorKit( new HTMLEditorKit() );
        jedPane.setText(SharedData.HTMLPageMaker.createHTML(op));
        JScrollPane scroll =new JScrollPane( jedPane);
        jf.getContentPane().add( scroll );
        jf.setSize( (int)(screenwidth/2), (int)(3*screenheight/4) );
        WindowShower windShow = new WindowShower( jf);
        EventQueue.invokeLater( windShow);       
           //jf.show();
   
       
     }
    }
   
    private class VarPanel extends JPanel implements ItemListener, 
                                           ListSelectionListener,
                                           ActionListener, Serializable{
      WizardWizard W;
      int FromForm = -1, toForm = -1;
      JComboBox Fromm, To, RetTypes;
      JList FrVars, ToVars;
      DefaultListModel FrModel, ToModel;
      int FrSel =-1;
      int ToSel =-1;
      public  String[] RetStrings ={"Array","String","LoadFile",
      "SaveFile","Boolean","DataSet","Float","InstName","Integer",
      "Material","MonitorDataSet","SampleDataSet","PulseHeight","PrinterName",
      };
      public VarPanel( WizardWizard W){
        super();
       
        BoxLayout BL = new BoxLayout( this, BoxLayout.X_AXIS);
        this.setLayout(BL);
        
        Fromm = new JComboBox();
        Fromm.addItemListener( this );
        To = new JComboBox();
        Fromm.addItemListener( this );
        To.addItemListener(this);
        FrModel = new DefaultListModel();
        ToModel = new DefaultListModel();
        FrVars = new JList( FrModel); 
        ToVars = new JList( ToModel);
        FrVars.addListSelectionListener( this);
        ToVars.addListSelectionListener( this );
        JPanel JP= new JPanel();
        BoxLayout BL1 = new BoxLayout( JP, BoxLayout.Y_AXIS);
        JP.setLayout(BL1);
        JP.add( new JLabel("From Form"));
        JP.add( Fromm);
        JP.add( new JScrollPane( FrVars));
        JP.add(Box.createVerticalGlue());
        add(JP);
        JP = new JPanel();
        BL1 = new BoxLayout( JP, BoxLayout.Y_AXIS);
        JP.setLayout(BL1);
        JP.add( Box.createVerticalGlue());
        JButton jb = new JButton( CONNECT);
        jb.setToolTipText("Toggle Connection");
        jb.addActionListener( this);
        JP.add(jb);
        jb = new JButton( CONST);
        jb.setToolTipText("Toggle Constant property ");
        jb.addActionListener( this);
        JP.add(jb);
        RetTypes = new JComboBox(RetStrings);
        RetTypes.setToolTipText("Set Parameter for Result(Left)");
        
        RetTypes.addActionListener( this);
        JP.add(RetTypes);
      
        jb= new JButton("Info");
        jb.addActionListener( this);
        JP.add(jb);
        JP.add( Box.createVerticalGlue());
        add(JP);
        JP= new JPanel();
        BL1 = new BoxLayout( JP, BoxLayout.Y_AXIS);
        JP.setLayout(BL1);
        JP.add( new JLabel("To Form"));
        JP.add( To);
        JP.add( new JScrollPane( ToVars));
        JP.add(Box.createVerticalGlue());
        add(JP);
        Fromm.setEnabled( true );
        To.setEnabled( true );
        this.addComponentListener( new CompListener( this ))  ;
      }
      int LastSelList = -1;
      int LastSelItem =-1;
      public void valueChanged( ListSelectionEvent evt){
        if(evt.getSource() == FrVars)
          LastSelList =0;
        else if( evt.getSource() == ToVars)
          LastSelList =1;
        else 
          LastSelList =-1;
        LastSelItem = evt.getFirstIndex();
          
      }
      public void itemStateChanged( ItemEvent evt ){
        if( evt.getSource() == RetTypes){
           int k = Fromm.getSelectedIndex();
           if( k <0) return;
           ((opLinkElement)(opns.get(k))).ResultPG= RetTypes.
                        getSelectedItem().toString();
           return;
        }
         boolean change = false;
         if( Fromm.getSelectedIndex() != FrSel){
            FrSel = Fromm.getSelectedIndex();
            FrModel.clear();
            change=true;
         }
        if( To.getSelectedIndex() != ToSel){
           ToSel = To.getSelectedIndex();
           ToModel.clear();
           change = true;
           
        }
       
       if( change)
         SetUpLists();
       
  
      }
      public void actionPerformed( ActionEvent evt){
        if( evt.getSource() == RetTypes){
            int k = Fromm.getSelectedIndex();
            if( k <0) return;
            opLinkElement elt = (opLinkElement)opns.get(k);
            elt.ResultPG= RetTypes.getSelectedItem().toString();
            ParListInf paramInf = (ParListInf)FrModel.get( FrModel.size()-1);
            paramInf.param = (new Command.ParameterClassList()).getInstance( elt.ResultPG);
            if( paramInf.param !=null)
              paramInf.param.setName("Result");
            SetUpLists();
            return;
            
        }
        if( evt.getActionCommand() == CONST){
            int kLeft = Fromm.getSelectedIndex();
            if(kLeft < 0)
               return;
        
            opLinkElement opp = (opLinkElement)(opns.get( kLeft));
            if( opp == null)
              return;
           kLeft = FrVars.getSelectedIndex();
           if( kLeft <0)
              return;
           if( kLeft >= FrModel.size())  //The Result Type cannot be a Constant
              return;
           if(!opp.ConstList.contains( new Integer(kLeft)))
               opp.addConst( kLeft);
           else
               opp.removeConst( kLeft);
           SetUpLists(); 
        }
        else if( evt.getActionCommand() == CONNECT){
          SetFormNums( opns);
          int kLeft =  Fromm.getSelectedIndex();
          int kRight = To.getSelectedIndex();
          if( kLeft < 0 )
            return;
          if( kRight < 0 )
            return;
          if( kLeft == kRight)
            return;
        
          opLinkElement oppL =(opLinkElement)(opns.get(kLeft));
          opLinkElement oppR =(opLinkElement)(opns.get(kRight));
          int LParNum= FrVars.getSelectedIndex();
          int RParNum =ToVars.getSelectedIndex();
          if( !oppL.addLink( LParNum, oppR, RParNum)){
              oppL.removeLink( LParNum, oppR, RParNum);
              oppR.removeLink( RParNum, oppL, LParNum);
              kLeft=kRight = -1;
          }else{
          
              oppR.addLink( RParNum, oppL, LParNum);
              ApplyTransitive( oppL, kLeft, oppR, kRight);
              ApplyTransitive( oppR, kRight, oppL, kLeft);
          }
          SetUpLists();
        }else if( evt.getActionCommand().equals("Info")){
            ParListInf Par = (ParListInf)FrVars.getSelectedValue();
            if( Par == null){
              JOptionPane.showMessageDialog( null, "No Selection in Left List");
              return;
            }
            if( Par.param == null){
               JOptionPane.showMessageDialog( null, "Parameter is null");
               return;
            }
            if( !(Par.param instanceof ParameterGUI)){
              JOptionPane.showMessageDialog( null, "Parameter is not a ParameterGUI");
              return;
            }
            FinishJFrame jf = new FinishJFrame(Fromm.getSelectedItem().toString()+
                    "-Param"+Par.param.getName());
            JEditorPane jed = new JEditorPane();
            String S ="ParamGUI:"+Par.param.getType();
            Object Val =Par.param.getValue();
            S +="\r\n Value Class:";
            if(Val == null)
               S +="(null";
            else
               S +=Val.getClass();
            
            jed.setText( S);
             jf.getContentPane().add(jed);
             jf.setSize( new Dimension(200,300));
             jf.show();
              
              
          
        }
      }
      //ToDO: Make sure any par in a form with a connection to a Result parameter
      //       is not connected to any form previous to the Result  
      private void ApplyTransitive( opLinkElement opp1,int Par1Num,
                            opLinkElement opp2, int Par2Num){
          Vector V = opp2.ArgLinks;
          for( int i =0; i< V.size(); i++){
            Vector W = (Vector)(V.elementAt(i));
            if( W.firstElement().equals(new Integer(Par2Num)))
              if( !W.elementAt(1).equals( opp1)){
                 if( opp1.addLink( Par1Num,(opLinkElement)(W.elementAt(1)),
                               ((Integer)(W.lastElement())).intValue()) )
                    ApplyTransitive( opp1,Par1Num,(opLinkElement)(W.elementAt(1)),
                ((Integer)(W.lastElement())).intValue());
              }
          }
         
      }
      
      /**
       *  Checks to see if adding opp2 /Par2Num to opp1 will cause a Result
       *  parameter to be linked with a parameter in a form previous to that
       *  form.  Returns true if it is ok to add , otherwise it return false.
       * @param opp1
       * @param Par1Num
       * @param opp2
       * @param Par2Num
       * @return
       */
      private int CheckResultProblem(opLinkElement opp1,int Par1Num,
      opLinkElement opp2, int Par2Num ){
        
       /* Vector V = new Vector();
        boolean done = false;
        int MaxResultForm =-1;
        while( Par2Num >=0){
           if( Par2Num == opp2.op.getNum_parameters())
             if( Par2Num >MaxResultForm)
               MaxResultForm =Par2Num;
            
        }
        */
        return -1;
      }
      private void SetUpLists(){
        if(Fromm.getSelectedIndex() < 0)
           return;
        if( To.getSelectedIndex() < 0)
           return;
        FrModel.clear();
        ToModel.clear();
        GenericOperator FrOp = ((ListHolder)(Fromm.getSelectedItem())).op;
        GenericOperator ToOp = ((ListHolder)(To.getSelectedItem())).op;
        for( int i=0; i< FrOp.getNum_parameters(); i++)
           FrModel.addElement( new ParListInf(FrOp.getParameter( i )));
        ParListInf Res = new ParListInf(null);
        opLinkElement elt= (opLinkElement)opns.get( Fromm.getSelectedIndex());
        if( elt.ResultPG !=null)
           if( elt.ResultPG.length()>1){
           
              Res.param = (new Command.ParameterClassList()).getInstance( elt.ResultPG);
              if( Res.param != null)
                 Res.param.setName("Result");
           }
        FrModel.addElement(Res);
        for( int i=0; i< ToOp.getNum_parameters(); i++)
           ToModel.addElement( new ParListInf(ToOp.getParameter( i )));
        opLinkElement FrInf = (opLinkElement)(opns.get(Fromm.getSelectedIndex()));
        opLinkElement ToInf = (opLinkElement)(opns.get(To.getSelectedIndex()));
        for(int i=0; i< FrInf.ConstList.size(); i++){
          int argNum= ((Integer)(FrInf.ConstList.elementAt(i))).intValue();
          if( argNum >=0)
            ((ParListInf)(FrModel.get( argNum ))).setCONST(true);
        }
        for(int i=0; i< ToInf.ConstList.size(); i++){
          int argNum= ((Integer)(ToInf.ConstList.elementAt(i))).intValue();
          ((ParListInf)(ToModel.get( argNum ))).setCONST(true);
        }
        int kk=0;
        for( int i=0; i<FrInf.ArgLinks.size(); i++){
          Vector V = (Vector)(FrInf.ArgLinks.elementAt(i));
          if( V.elementAt(1) == ToInf){
             int k= ((Integer)(V.firstElement())).intValue();
             int k2 =((Integer)(V.lastElement())).intValue();
             if( (k >=0) &&(k2>=0)){
                 ((ParListInf)(FrModel.get(k))).setConnectNum(kk);
                 ((ParListInf)(ToModel.get(k2))).setConnectNum(kk);
                 kk++;
             }
          }
        }  
      }
    
    }
    
    /**
     * 
     * @author MikkelsonR
     *  This class is a wrapper around a parameter that gives a toString() 
     * value that indicates its connect number( to see corresponding connect
     * in other list) and whether it is a constant.
     */
    private class ParListInf implements Serializable{
      IParameter param;
      int ConnectNum;
      boolean CONST;
      
      public ParListInf( IParameter param){
         this.param = param;
         ConnectNum=-1;
         CONST = false;
      }
      public void setCONST( boolean val){
          CONST = val;
      }
     public void setConnectNum( int ConnectNum){
        this.ConnectNum =  ConnectNum;
     }
     
     public String toString(){
       String S="";
       if(CONST)
         S ="C";
       if(ConnectNum >=0)
         S += ConnectNum;
       if( S.length()>=1)
         S+="    :";
       if( param == null)
          S+="Result";
       else
          S+= param.getName();
       return S;
     }
    }
    
    /**
     * 
     * @author MikkelsonR
     * Listens to when the Variable panel becomes shown.  The whole
     * Panel is redrawn from data.
     */
    private class CompListener extends ComponentAdapter implements Serializable{
      VarPanel vPanel;
      public CompListener( VarPanel vPanel){
        this.vPanel = vPanel;
      }
      public void componentShown(ComponentEvent e){
        vPanel.Fromm.removeAllItems();
        vPanel.To.removeAllItems();
        for(int i=0; i< OpnList.size(); i++){
           vPanel.Fromm.addItem( OpnList.elementAt(i));
           vPanel.To.addItem(OpnList.elementAt(i));
        }
        vPanel.FrModel.clear();
        vPanel.ToModel.clear();
        
      }
    }
    private class FilePanel extends JPanel implements Serializable{
 
      WizardWizard W;
      public FilePanel( WizardWizard W){
        super( new FlowLayout());
        this.W = W;
        JButton jb = new JButton( SAVE);
        jb.addActionListener(W);
        add( jb);

        jb = new JButton( SAVE_STATE);
        jb.addActionListener(W);
        add( jb);
        jb = new JButton( OPEN);
        jb.addActionListener(W);
        add( jb);
      }
    
    }

   /**
    * 
    * @author MikkelsonR
    *
    *  A wrapper around a Generic Operator to be placed in a list so that the
    *  resultant string gives both the command and title of the operator
    */
   private class ListHolder implements Serializable{
      GenericOperator op;
      String filename;
      public ListHolder( GenericOperator op, String filename) throws IllegalArgumentException{
        this.op =op;
        this.filename = filename;
        if( op == null)
           throw new IllegalArgumentException();
      }
      public GenericOperator getOp(){
        return op;
      }
        
      public String toString(){
        return op.getCommand()+"::"+op.getTitle();
      }
      
   }
  
  /**
   * 
   * @author MikkelsonR
   * An element in a linked list of forms.  It stores the operator, the list of
   * constant parameters, and the list of linking of variables.
   */
  private class opLinkElement implements Serializable{
    public GenericOperator op;
    int FormNum =-1;
    Vector ConstList;
    Vector ArgLinks;
    String ResultPG;
    public opLinkElement( GenericOperator op){
       this.op = op;
       ConstList = new Vector();
       ArgLinks = new Vector();
       ResultPG="";
    }
    
    public boolean addConst( int argNum){
       if( argNum < 0)
         return false;
       Integer N = new Integer(argNum);
       
       if( !ConstList.contains(N))
          ConstList.addElement( N);
       return true;
    }
    private boolean ShowReturn( String Message){
      JOptionPane.showMessageDialog( null, Message);
      return false;
    }
    public boolean addLink( int Arg1Num, opLinkElement opp, int ArgNum ){
       Vector V = new Vector();
       if( (Arg1Num <0) || (Arg1Num > op.getNum_parameters()))
         return false;
       if( (ArgNum < 0) ||( ArgNum  > opp.op.getNum_parameters()))
         return false;
        if( Arg1Num < op.getNum_parameters())
           if( op.getParameter( Arg1Num).getType()==null)//Not ParameterGUI
              return ShowReturn("From Parameter "+Arg1Num+" is not a ParameterGUI");

       if((Arg1Num>= op.getNum_parameters())&&(ResultPG.length()<1))
           return ShowReturn(" ParameterGUI for Resul must be set to connect");
       if( ArgNum < opp.op.getNum_parameters())
       if( opp.op.getParameter( ArgNum).getType()==null)
        return ShowReturn("To Parameter "+ArgNum+" is not a ParameterGUI");
       if((ArgNum >= opp.op.getNum_parameters()) &&(opp.ResultPG.length()<1))
         return ShowReturn(" ParameterGUI for Resul must be set to connect");
       // Test if they have same ParameterGUI's
       if( Arg1Num >= op.getNum_parameters())//ResultPG
         if( ResultPG ==null)
            return ShowReturn("Must Set a ParameterGUI got the Result");
         else if( ResultPG.length() <1)
              return ShowReturn("Must Set a ParameterGUI got the Result");
         else if( !opp.op.getParameter(ArgNum).getType().equals(ResultPG))
            return ShowReturn("Parameters are incompatible");
         else{}
       else if( ArgNum >= opp.op.getNum_parameters()){
         if(!op.getParameter(Arg1Num).getType().equals(opp.ResultPG))
           return ShowReturn("Parameters are incompatible");
       }
       else if( !op.getParameter(Arg1Num).getType().equals(
                  opp.op.getParameter(ArgNum).getType()))
            return ShowReturn("Parameters are incompatible");
       if( AlreadyLinked(Arg1Num, opp, ArgNum))
         return ShowReturn("Already Linked");
       if(opp.AlreadyLinked(ArgNum, this, Arg1Num ))
          return ShowReturn("Already Linked");
       
       //-----------End Check if can add -------------------
       V.addElement( new Integer(Arg1Num) );
       V.addElement( opp );
       V.addElement( new Integer(ArgNum));
       if(!ArgLinks.contains( V ))
          ArgLinks.addElement( V );
       else 
          return false;
        return true;
       
    }
    public boolean removeConst( int argNum){
      Integer N = new Integer( argNum);
      ConstList.remove(N);
      return true;
    }
    
    public void removeLink( int Arg1Num, opLinkElement opp, int ArgNum ){
      Vector V = new Vector();
      V.addElement( new Integer(Arg1Num) );
      V.addElement( opp );
      V.addElement( new Integer(ArgNum));
      ArgLinks.remove( V );     
      
    }
    public void RemoveLinkRef( opLinkElement opp){
       for( int i=ArgLinks.size()-1; i>=0; i--){
          Vector V = (Vector)(ArgLinks.elementAt( i ));
          if( V.elementAt(1) == opp)
            ArgLinks.remove( i );
       }
    }
    
   public boolean AlreadyLinked( int ParNum, opLinkElement opp, int ParNumopp){
       for( int i =0; i< ArgLinks.size(); i++){
         Vector V =(Vector)(ArgLinks.elementAt(i));
         if( V.firstElement() .equals( new Integer(ParNum)))
            if( V.elementAt(1)== opp)
              if( !V.lastElement().equals( new Integer(ParNumopp)))
              return true;
       }
       return false;
   }
  }
  
 
  private void WriteGPL(FileOutputStream FSave)throws IOException{
     FSave.write(("/*\r\n *File:  "+infPanel.WizardName.getText().trim()+".java\r\n").getBytes());
     FSave.write((" *\r\n * Copyright (C) "+infPanel.Name.getText().trim()+"\r\n *\r\n").getBytes());
     FSave.write(" * This program is free software; you can redistribute it and/or \r\n".getBytes());
    FSave.write(" *modify it under the terms of the GNU General Public License\r\n".getBytes());
    FSave.write(" *as published by the Free Software Foundation; either version 2\r\n".getBytes());
    FSave.write(" *of the License, or (at your option) any later version.\r\n".getBytes());
    FSave.write(" *\r\n".getBytes());
    FSave.write(" *This program is distributed in the hope that it will be useful,\r\n".getBytes());
    FSave.write(" *but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n".getBytes());
    FSave.write(" *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\r\n".getBytes());
    FSave.write(" *GNU General Public License for more details.\r\n".getBytes());
    FSave.write(" *\r\n".getBytes());
    FSave.write(" * You should have received a copy of the GNU General Public License\r\n".getBytes());
    FSave.write(" * along with this library; if not, write to the Free Software\r\n".getBytes());
    FSave.write(" * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.\r\n".getBytes());
    FSave.write(" *\r\n".getBytes());
    FSave.write((" *Contact:"+infPanel.Name.getText().trim()+","+
                            infPanel.Email.getText().trim()+"\r\n").getBytes());
    int k=0; 
    String Addr = infPanel.Address.getText().trim();
    int k1 = Addr.indexOf('\n');
    if( k1 < 0) k1 = Addr.length();
    while( k < Addr.length()){
    
       FSave.write((" *        "+Addr.substring(k, k1)+"\r\n").getBytes() );
       k=k1+1;
       if( k < Addr.length())
          k1= Addr.indexOf('\n',k1+1);
       if(k1 < 0) k1=Addr.length();
    }
    FSave.write(" *\r\n".getBytes());
    
    k=0; 
    String Acknow = infPanel.Acknowl.getText().trim();
    System.out.println("Acknow="+Acknow);
    k1 = Acknow.indexOf('\n');
    if( k1 < 0) k1 = Acknow.length();
    while( k < Acknow.length()){
       System.out.println("   k,k1="+k+","+k1);
       try{
       FSave.write((" *"+Acknow.substring(k, k1)+"\r\n").getBytes() );
       }catch(Exception sss){
          sss.printStackTrace();
          throw new IOException("sss");
      }
       System.out.println("A");
          k=k1+1;
          if( k < Acknow.length())
           if( k1+1 < Acknow.length())
             k1= Acknow.indexOf('\n',k1+1);
           else 
             k1 =-1;
          System.out.println("B");
          if(k1<0) k1=Acknow.length();
          System.out.println("C");
    }
    System.out.println("Got here---------");
    FSave.write(" *\r\n".getBytes());
       
    FSave.write(" *\r\n".getBytes());
    FSave.write(" *\r\n".getBytes());
    FSave.write(" * Modified:".getBytes());
    FSave.write(" *\r\n".getBytes());
    FSave.write((" * $Log$
    FSave.write((" * Revision 1.1  2005/05/24 21:31:55  rmikk
    FSave.write((" * Initial Checkin of Wizard to make Wizards
    FSave.write((" *").getBytes());
  
    
    }
  private void WritePackage(FileOutputStream FSave, File FSaveFile)throws IOException{
    String classpath = System.getProperty("java.class.path");
    if( classpath == null)
      return;
    if( ! classpath.endsWith(File.pathSeparator))
       classpath +=File.pathSeparatorChar;
    int k=classpath.indexOf( File.pathSeparatorChar);
    int k1 = 0;
    int l =-1;
    for( ;(l<0)&& (k1+1 < classpath.length()); k=classpath.indexOf( File.pathSeparatorChar)){
      String CPath = classpath.substring( k1, k-k1);
      l= FSaveFile.getAbsolutePath().indexOf((new File(CPath)).getAbsolutePath());
      if( l==0){
         String pack = FSaveFile.getAbsolutePath().substring( (new File(CPath).getAbsolutePath().length()));
         if( !( Character.isJavaLetterOrDigit(pack.charAt(0))))
           pack = pack.substring(1);
         int kk = pack.lastIndexOf( File.separatorChar);
         if( kk >=0) pack = pack.substring(0, kk);
         pack = pack.replace( File.separatorChar, '.');
         FSave.write( ("package "+pack +";\r\n\r\n").getBytes());
      }
      k1=k+1;
    }
    
    
  }
  private void WriteImports( FileOutputStream FSave,String rest)throws IOException{
   
      FSave.write("import DataSetTools.wizard.*;\n\r".getBytes());
      FSave.write("import DataSetTools.parameter.*;\r\n".getBytes());
  }
  private void WriteClass(FileOutputStream FSave, String rest)throws IOException{
    FSave.write( ("public class "+ infPanel.WizardName.getText().trim()+" extends Wizard \r\n").getBytes());
    if( rest != null) if(rest.indexOf("implements")>=10){
      int k=rest.indexOf("implements");
      int k3 = rest.indexOf("extends",k+1);
      if( k3<0) k3= rest.length();
      FSave.write( (rest.substring(k,k3-k)+"{\r\n").getBytes());
       
    }
    FSave.write("{\r\n".getBytes());
    
  }
  private void WriteLinks(FileOutputStream  FSave)throws IOException{
    
  }
  

  int[] NConstants = null;
  private void WriteConstr(FileOutputStream FSave,String rest)throws IOException{
    //Write the links and const
    NConstants = new int[ OpnList.size()];
    FSave.write("     int[][] ParamTable= {".getBytes());
    int NLinks = 0;
    for(int i=0; i<opns.size(); i++){
    
      ((opLinkElement)opns.get(i)).FormNum=i;
      NLinks+=((opLinkElement)opns.get(i)).ArgLinks.size();
    }
    int[][] inf  = new int[ NLinks ][4];
    NLinks = 0;
    for( int i=0; i<opns.size(); i++){
       Vector ArgLinks = ((opLinkElement)(opns.get(i))).ArgLinks;
      
       for( int j=0; j< ArgLinks.size(); j++){
         Vector oneLink =(Vector)( ArgLinks.elementAt(j));
         int Form2Num=((opLinkElement)(oneLink.elementAt(1))).FormNum;
         int Form1Parm =((Integer)(oneLink.firstElement())).intValue();
         int Form2Parm =((Integer)(oneLink.lastElement())).intValue();
         int Form1Num = j;
         int k = NLinks-1;
         while( (k >= 0) &&(Form1Num <= inf[k][3]) &&
             ( (Form1Num == inf[k][4]) ||(Form1Parm < inf[k][1]))&&
             (Form2Num <= inf[k][0])&&
             ((Form2Num == inf[k][0]) ||(Form2Parm < inf[k][2]))){
              inf[k+1]=inf[k];
             k--;
          }
         k++;
        inf[k][0]=Form2Num;
        inf[k][1]=Form1Parm;
        inf[k][2] = Form2Parm;
        inf[k][3]= Form1Num;
        NLinks++;
                    
      
       }
    }
    System.out.println("inf list");Command.ScriptUtil.display(inf);
    String SLink ="    int[][] ParamTable ={\r\n";
    int[] Line = new int[OpnList.size()];
    Arrays.fill(Line,-1);
    boolean start= true;
 
    for(int i =0; i< inf.length ; i++){
      if( Line[inf[i][3]]==inf[i][1]){
          Line[inf[i][0]]=inf[i][2];
         
      }
      else{
        if(!start)
          SLink +="      ,{";
        else
           SLink = "     {\r\n";
        for( int k=0; k< Line.length; k++){
          SLink += Line[k];
          if( k+1 < Line.length)
             SLink+=",";
          else SLink +="}\r\n";
          Arrays.fill(Line,-1);     
        }
        
      }      
        
    }
    SLink +="   }\r\n";
    

    FSave.write((SLink+"\r\n            };\r\n\r\n" ).getBytes());
    
    SLink ="     int[][]ConstList ={\r\n";
    start = true;
    Arrays.fill(NConstants, 0);
    for(int i=0; i< OpnList.size();i++){
      if(start)
        SLink ="        {";
      else
        SLink ="        ,{";
      Vector elt = ((opLinkElement)(opns.get(i))).ConstList;  
      for( int k=0; k<elt.size(); k++){
         if(k>0)
           SLink +=",";
         SLink += ((Integer)(elt.elementAt(k))).intValue();
         NConstants[i]++;
      }
      SLink +="\r\n";
    }
    SLink +="     };\r\n";
    FSave.write( SLink.getBytes());
    
    FSave.write(("   public "+infPanel.WizardName.getText().trim()+"( ){\r\n").getBytes());

    FSave.write(("        this( false);\r\n      };\r\n\r\n".getBytes()));
   

    FSave.write(("   public "+infPanel.WizardName.getText().trim()+
                  "(boolean standalone ){\r\n").getBytes());

    FSave.write(("        super(\""+
       infPanel.WizardTitle.getText().trim()+"\",standalone);\r\n     \r\n\r\n").getBytes());
    
   
  }
  private void WriteForms(FileOutputStream FSave)throws IOException{
    
    for( int i=0; i < OpnList.size(); i++ ){
      GenericOperator op= ((ListHolder)(OpnList.elementAt(i))).op;
     
      
      String ResParamGui = ((opLinkElement)(opns.get(i))).ResultPG;
      if( ResParamGui == null)
         ResParamGui ="";
      if( ResParamGui.length()>1)
         ResParamGui =", new "+ResParamGui+"PG(\"Result\",null)";
      String ConstList ="";
      if( (ResParamGui.length()>1) && (NConstants[i]>0)){
        ConstList =",ConstList[i]";
      }
      if(op instanceof ScriptOperator){
        FSave.write(("   addForm( new ScriptForm(\" "+ 
                  op.getSource().trim().replace('\\','/' )
                  +"\""+ResParamGui+ConstList+"));\r\n").getBytes());
      }
      else if( op instanceof PyScriptOperator){
         FSave.write(("   addForm( new JyScriptForm( \""+
              op.getSource().trim().replace('\\','/')
              +"\""+ResParamGui+ConstList+"));\r\n").getBytes());
        
      }else if( op instanceof JavaWrapperOperator){
        FSave.write(("    addForm( new OperatorForm( new JavaWrapperOperator( new "+
                  op.getSource().substring(6).trim()+"())));\r\n").getBytes());
      }
      else{
        FSave.write(("    addForm( new OperatorForm(  new "+
                         op.getSource().substring(6).trim()+"()"+
                         ResParamGui+ConstList+"));\r\n").getBytes());
      }
    }
    FSave.write("      linkFormParameters( ParamTable );\r\n".getBytes());
  }
  
  
  private void WriteHelp(FileOutputStream FSave)throws IOException{
    if(this.HTMLDocFileName == null ){
      FSave.write("   String S=\"\" ;\r\n".getBytes());
      String T =docPanel.Docum.getText(); 
      int k=0, k1= T.indexOf('\n');
      if(k1 < 0) k1 = T.length();
      while( k < T.length()){
        FSave.write(("    S+=\""+T.substring(k,k1)+"\";\n").getBytes());
        k=k1+1;
        k1= T.indexOf('\n',k);
        if(k1 < 0) k1 = T.length();
      }
      FSave.write("    setHelpMessage( S);\r\n".getBytes());
      
     
    }else{
     
      FSave.write("     String Prefix = System.getProperty(\"Docs_Directory\",\"\");  \r\n ".getBytes());
      FSave.write("     if( !Prefix.endsWith( File.separator))\r\n".getBytes());
      FSave.write("         Prefix=Prefix+File.separator;\r\n".getBytes());
      FSave.write("     Prefix=Prefix.replace(':','|');\r\n".getBytes());
      FSave.write("     Prefix=Prefix.replace('\\','/');\r\n".getBytes());
      FSave.write( ("   setHelpURL(\"file://+Prefix+\""+HTMLDocFileName+"\");\r\n").getBytes());
    }
  }
  private void WriteMain( FileOutputStream FSave, String rest){
     try{
     
     FSave.write(("   public void main( String[] args){\r\n").getBytes());
     FSave.write(("      " +  infPanel.WizardName.getText().trim()+" Wiz= new "+
                  infPanel.WizardName.getText().trim()+"(true);\r\n").getBytes());
     FSave.write( "    Wiz.wizardLoader( args );\r\n   }\r\n\r\n}\r\n".getBytes());
     }catch(Exception s){}  
  }
  boolean inComment1Line,inCommentMultiLine, inQuotes, inMethod, inClass;
  private Vector getNextSection( FileInputStream fin) throws IOException{
    inComment1Line=inCommentMultiLine= inQuotes= inMethod= inClass= false;
    if( true){
      Vector V = new Vector();
      V.addElement( new Integer(-1));
      V.addElement( "");
      return V;
    }
    
    
    return null;
  }
            

}
