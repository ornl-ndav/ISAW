/*
 * File: Method2OperatorWizard.java 
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
 * Revision 1.9  2005/11/22 19:40:37  rmikk
 * Set Arg 0 as selected argument when a new method is first selected
 * Sets the default command name, title, and filename for the resultant
 *    operator to one derived from the static method.
 * Set line wrap on in the documentation text areas
 * Added javadocs to the resultant code.  One javadoc indicates that the
 *    file is automatically generated
 * 
 * Added more spaces to the resultant code for the operator and added 
 *    some documentation to the Method2OperatorWizard code
 * 
 * (Kurtiss Olson)
 *
 * Revision 1.8  2005/08/05 20:29:08  rmikk
 * Added a "d" to the word Acknowle[d]gement
 *
 * Revision 1.7  2005/08/04 19:43:11  kramer
 *
 * Fixed the error where the return type of a method would not be displayed
 * on the gui if the method didn't have any parameters.  Without the return
 * type displayed, an operator could not be created based on the method.
 *
 * Revision 1.6  2005/07/10 14:17:30  rmikk
 * Implemented major changes in the GUI interface
 *
 * Revision 1.5  2005/06/20 16:47:43  rmikk
 * Added Some message boxes to indicate while a file does not work
 *
 * Revision 1.4  2005/06/17 13:09:25  rmikk
 * Fixes String representation for classes that are arrays correctly
 * Fixes String representation of interfaces correctly
 * Multi line documentation for returns is now handled correctly
 * The correct? methods are now being used for the Argument
 *     ParameterGUI JComboBox
 * Exceptions the static methods are now translated correctly to Java
 *   code
 *
 * Revision 1.3  2005/06/14 23:08:36  rmikk
 * Implemented the @error documentation system.
 * Change a name to be more descriptive
 * The Documentation TextArea now wraps lines.
 *
 * Revision 1.2  2005/06/14 16:45:36  dennis
 * Minor improvements to form of output.
 *
 * Revision 1.1  2005/06/13 20:53:42  rmikk
 * Moved from ExtTools
 *
 * Revision 1.6  2005/06/13 20:43:05  rmikk
 * *'s now line up in the GPL of the resultant code.
 *
 * Revision 1.5  2005/06/10 20:47:25  rmikk
 * The getCategoryList method is now implemented and working
 *
 * Revision 1.4  2005/06/10 19:29:10  rmikk
 * Removed errors that were caused by cvs logging because of strings that
 *   happened to be in code
 * Fixed the case for methods that have void returns.
 *
 * Revision 1.3  2005/06/10 19:06:49  rmikk
 * The filename of the operator should show up in the operator dialog box.
 * The category list has a text field available for it. It does not yet
 * appear in the output.
 *
 * Revision 1.2  2005/06/09 14:56:45  rmikk
 * Fixed errors that occur when the selected class file with the static
 *   method is missing
 *
 * Revision 1.1  2005/06/04 20:46:48  rmikk
 * Initial Checkin
 */

package devTools;

import java.awt.HeadlessException;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.lang.reflect.*;
import javax.swing.event.*;
import DataSetTools.util.SharedData;
import DataSetTools.util.SysUtil;

import java.util.*;
import DataSetTools.parameter.*;
import gov.anl.ipns.Util.Sys.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;

/**
 * @author MikkelsonR
 *  This class is used to create a full java GenericOperator shell around a 
 *  static method.
 */
public class Method2OperatorWizard extends JFrame implements ActionListener {

	transient public static String OP_FILENAME = "Browse";
	
	JTabbedPane TabPane;
	InfoPanel infPanel;
	DocPanel docPanel;
	MethodPanel methPanel;
    OpPanel   opPanel;
	FilePanel filePanel;
	DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();
	MethInfData methData;
  
	/**
	 *  Constructor that initializes the Wizard elements 
	 * @throws java.awt.HeadlessException
	 */
	public Method2OperatorWizard() throws HeadlessException {

		super( "Method to Operator Wizard");
    
                
		TabPane = new JTabbedPane();
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		infPanel = new InfoPanel(this);
		methPanel = new MethodPanel(this);
        opPanel = new OpPanel( this);
		docPanel = new DocPanel(this);
		filePanel = new FilePanel(this);
		methData = null;
    
		TabPane.add("Information", infPanel);
		TabPane.add("Method Info", methPanel);
        TabPane.add("Operator Info", opPanel);
        JScrollPane jsp = new JScrollPane( docPanel);
        jsp.addComponentListener( new CompListener( this ));
		TabPane.add("Documentation", jsp );
    
		getContentPane().setLayout(new GridLayout(1, 1));
    
		getContentPane().add(TabPane);
	}

	public void actionPerformed(ActionEvent evt) {

		if (evt.getActionCommand() == OP_FILENAME) {
			JFileChooser jf = new JFileChooser(opPanel.fileName.getText());
			if ((new File(opPanel.fileName.getText()).exists()))
				jf.setSelectedFile(new File(opPanel.fileName.getText()));
			if (jf.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			String OpfileName = jf.getSelectedFile().getAbsolutePath();
            opPanel.setDefaults( ForceExtension(OpfileName,"java"));
		}
	}

  
    /**
     *  Initializes everything when a new method is chosen
     *
     */
	public void MethodChanged() {
        
		methPanel.MethodList.removeAllItems();
		methPanel.meth = null;
		methData = null;
        ClearDocs();
		ArgsChanged();
	}


    /**
     *  Clears the Documentation information when a new method is selected
     *
     */
    public void ClearDocs(){

      docPanel.newOp=true;
      docPanel.OverView.setText("");
      docPanel.Algorithm.setText("");
      docPanel.Assump.setText("");
      docPanel.Error.setText("");
      docPanel.Return.setText("");
      docPanel.Params=null;
    }
    
    
    /**
     *  Clears the arguments and other settings when a new method is selected
     *
     */
	public void ArgsChanged() {

		methPanel.ArgListModel.clear();
        methPanel.Arguments.setEnabled(false);
		methPanel.Prompt.setText("");
        methPanel.Prompt.setEnabled(false);
        
		methPanel.VarName.setText("");
        methPanel.VarName.setEnabled(false);
		methPanel.InitValue.setText("");
        methPanel.InitValue.setEnabled(false);
		methPanel.ResInf.setText("");
	
		
		methPanel.ParamGUI.removeAllItems();
        methPanel.ParamGUI.setEnabled( false );
	}
  
  
  
    /**
     *  Forces the given extension on the file
     * @param filename    The filename to have its extension changed
     * @param extension   The new extension( case sensitive)
     * @return     The new filename with the correct extension
     */
    public static String ForceExtension( String filename,  String extension){
       if( filename == null)
          return null;
       if( extension == null)
          return filename;
          
       int i=filename.lastIndexOf(".");
       extension= extension.trim();
       if( extension.startsWith("."))
         extension= extension.substring(1, extension.length());
       if( i < 0)
          return filename+"."+extension;
       return filename.substring(0,i)+"."+extension;
    }
    
    
    /**
     *  Finds the class name corresponding to the file.  This can be used be the method Class.forName
     * @param fileName   The filename containing a possible class( or its java file)
     * @return   The class name that can be used by Class.forName
     */

	public static String getClassName(String fileName) {
        fileName= (new File(fileName)).getAbsolutePath();
		String ClassPth = System.getProperty("java.class.path");
		int k = 0;
		int k1 = ClassPth.indexOf(File.pathSeparator);

		if (k1 < 0)
			k1 = ClassPth.length();
		int i = fileName.lastIndexOf(".");

		if (i < 0){
		    JOptionPane.showMessageDialog(null, "Filename has no dots :"+fileName);
			return null;
        }
        
		fileName = fileName.substring(0, i);
		for (; k < ClassPth.length();) {
			String S = ClassPth.substring(k, k1);
            S = (new File(S)).getAbsolutePath();     
			if (!S.endsWith(File.separator))
				S += File.separator;
			if (fileName.indexOf(S) == 0) {
				return fileName.substring(S.length()).replace(File.separatorChar, '.');
			}
			k = k1 + 1;
			k1 = ClassPth.indexOf(File.pathSeparator, k);
			if (k1 < 0)
				k1 = ClassPth.length();
		}
        JOptionPane.showMessageDialog(null, "No ClassPath choice hits this filename:"
                         +fileName);
		return null;
	}



	/**
	 *  Returns a pretty name for a class. It eliminates the leading "class "
	 *  and converts L]]  to arrays(woops it will)
	 * @param O  A class name or a string that represents a class name
	 * @return   A string with the leading "class " missing and also arrays
	 *          look like regular arrays( soon)
	 */
	public static String FixUpClassName(Object O) {

		String S = null;

		if (O instanceof Class)
			S = O.toString();
		else if (O instanceof String)
			S = (String) O;
		else
			return null;

		if (S == null)
			return null;
		S = S.trim();
		if (S.startsWith("class "))
			S = S.substring(5);
        else if( S.startsWith("interface "))
            S=S.substring(9);
        if(S != null)
           S=S.trim();
        String Res="";
        boolean Arr=false;
        while((S != null)&&( S.startsWith("["))){
        
          Res +="[]";
          S=S.substring(1);
          Arr=true;
        }
        
        
        if(!Arr)
          return S;
        if(S.equals("I"))
          return "int"+Res;
        if( S.equals("F"))
          return "float"+Res;
        if(S.equals("J"))
          return "long"+Res;
        if( S.equals("B"))
          return "byte"+Res;
        if( S.equals("S"))
          return "short"+Res;
        if( S.startsWith("L"))
          return S.substring(1)+Res;
        else
          return S+Res;
        
	
	}



	/**
	 * Fixes up a String with possible \n's. A line( trimmed) is replaced
	 * by LinePrefix followed by the line the LinePostFix.  For example,
	 * to get html pages linePostFix can be "<BR>".
	 * @param S     The string that is to be broken up on lines
	 * @param LinePrefix  A String to be placed before each line
	 * @param  LinePostFix A String to be placed after the line
	 * @return The concatenation of all the lines with prefix and postfix
	 *         strings included.
	 */
	public static String FixUpMultiLines(
		String S,
		String LinePrefix,
		String LinePostFix) {

		int k = 0;
		int k1 = S.indexOf('\n');
		String Res = "";
		LinePrefix = StringUtil.replace(LinePrefix, "\"", "\\\"");
		LinePostFix = StringUtil.replace(LinePostFix, "\"", "\\\"");
		if (k1 < 0)
			k1 = S.length();
		while (k < S.length()) {
			Res += LinePrefix
				+ StringUtil.replace(S.substring(k, k1).trim(), "\"", "\\\"")
				+ LinePostFix;
			k = k1 + 1;
			if (k1 < S.length())
				k1 = S.indexOf('\n', k1 + 1);
		}
		return Res;
	}




	/**
	 * Produces a string to prepend the string "getParameter(i)" to get the
	 * resultant class C.  Used with GetAppend.  This is used when writing out the
     * code in the operator file to retrieve the values from the parameters.  
	 * @param paramGUIName  The name of the ParameterGUI
	 * @param C   The resultant Class  
	 * @return   A string that represents java code that must prepend 
	 *          "getParameter(i)" commonly invoked after the getResult method
	 *          in an operator.
	 * @see #GetAppend(String,Class) 
	 */

	public static String GetPrepend(String paramGUIName, Class C) {

		if (";LoadFile;SaveFile;String;DataDir;FuncString;InstName;Material;RadioButton;ChoiceList;;"
			.indexOf(";" + paramGUIName + ";")
			>= 0)
			return "";
		if (!C.isPrimitive())
			return "(" + FixUpClassName(C) + ")(";
		if (";Float;Boolean;Integer;".indexOf(paramGUIName) >= 0)
			return "((" + paramGUIName + "PG)(";
		return "";
	}

	/**
	 * Produces a string to append the string "getParameter(i)" to get the
	 * resultant class C.  Used with GetPrePend
	 * @param paramGUIName  The name of the ParameterGUI
	 * @param C   The resultant Class  
	 * @return   A string that represents java code that must append 
	 *          "getParameter(i)" commonly invoked after the getResult method
	 *          in an operator.
	 * @see #GetPrepend(String,Class)
	 */
	public static String GetAppend(String paramGUIName, Class C) {

		if (";LoadFile;SaveFile;String;DataDir;FuncString;InstName;RadioButton;ChoiceList;"
			.indexOf(";" + paramGUIName + ";")
			>= 0)
			return ".getValue().toString()";
		if (!C.isPrimitive())
			return ".getValue())";
		if (";Float;Boolean;Integer;".indexOf(paramGUIName) >= 0) {
			String S = ")).";

			if (paramGUIName.startsWith("Float"))
				S += "getfloatValue()";
			else if (paramGUIName.startsWith("Int"))
				S += "getintValue()";
			else
				S += "getbooleanValue()";
			return S;
		}
		return "";
	}




	/**
	 *  Creates java code that is to be placed in a java file that will convert
	 *  a variable called Xres that has a primitive class(Name) like int,boolean,
	 *  char, etc. to the corresponding Object wrapper around this primitive class.  
	 * @param className  the SHORT name of the primitive or nonprimitive class. 
	 * @return    if it is not a primitive class name "Xres" will be returned, 
	 *            otherwise "new Integer(Xres)" will be returned(replace Integer
	 *            by corresponding wrapper)
	 */
	public static Object MakeObj(String className) {

		if (";boolean;byte;short;long;float;double".indexOf(className) >= 0) {
			String S = className.toUpperCase();

			return "new " + S.charAt(0) + className.substring(1) + "(Xres)";
		}
		if (className.equals("int"))
			return "new Integer(Xres)";
		if (className.equals("char"))
			return "new Character(Xres)";
		return "Xres";
	}



	/**
	 * Used to start this wizard
	 * @param args   are required for this method but are not used yet
	 */
	public static void main(String[] args) {

		Method2OperatorWizard W = new Method2OperatorWizard();

		W.setSize(600, 400);
        W.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		W.show();
    
	}



	/**
	 *  This class takes care of the Information tab of the tabbed pane
	 * 
	 */
	private class InfoPanel extends JPanel {

		Method2OperatorWizard W;
		JTextField Name,
			Email,
			Instit;
		JTextArea  Address,Acknowl;
		

		public InfoPanel(Method2OperatorWizard W) {

			super();
            int Height, Width= 580;
			BoxLayout BL = new BoxLayout(this, BoxLayout.Y_AXIS);

			setLayout(BL);
			this.W = W;
			JPanel JP = new JPanel(new GridLayout(1, 2));

			JP.add(new JLabel("Your Name"));
			Name = new JTextField();
            Height = Name.getPreferredSize().height*5;
            
            JP.setMaximumSize( new Dimension(Width ,Height ));
			JP.add(Name);
           
			add(JP);
			JP = null;

			JP = new JPanel(new GridLayout(1, 2));
			JP.add(new JLabel("Your Email"));
			Email = new JTextField();
			JP.add(Email);
            JP.setMaximumSize( new Dimension(Width,Height ));
			add(JP);

			JP = new JPanel(new GridLayout(1, 2));
			JP.add(new JLabel("Institution"));
			Instit = new JTextField();
			JP.add(Instit);
            JP.setMaximumSize( new Dimension(Width,Height ));
			add(JP);

			JP = new JPanel();
            BL = new BoxLayout(JP, BoxLayout.X_AXIS);
            JP.setLayout(BL);
			JP.add(new JLabel("Address                     "));
			Address = new JTextArea(4,20);      
			JP.add(new JScrollPane(Address));
            JP.setMaximumSize( new Dimension(Width,(int)(4.5*Height) ));
			add(JP);

			JP = new JPanel();
			BL = new BoxLayout(JP, BoxLayout.X_AXIS);
			JP.setLayout(BL); 
			JP.add(new JLabel("Acknowledgements  "));
			Acknowl = new JTextArea(5, 30);
			Acknowl.setLineWrap(true);
			JP.add(new JScrollPane(Acknowl));
            JP.setMaximumSize( new Dimension(Width,5*Height ));
			add(JP);
      
			add(Box.createVerticalGlue());
         
		}
	}



	/**
	 * This class takes care of tab concerning information about the static
	 * method that the new operator will wrap
	 * @author mikkelsonr
	 *
	 *
	 */
	class MethodPanel
		extends JPanel
		implements ActionListener, ListSelectionListener, ItemListener {

		Method2OperatorWizard W;
		String fileName="" ;
    
		JTextField FileName;
		JList Arguments;
		DefaultListModel ArgListModel;
		JComboBox MethodList;
		JLabel ResInf;
		String Param;
		JComboBox ParamGUI;
		JTextField Prompt, VarName, InitValue;
		Method meth;
		JButton ClassFileName, ViewFile;
		ParameterInfo pinf = new ParameterInfo();
		String methodName = "";
    
		public MethodPanel(Method2OperatorWizard W) {

			super();
			meth = null;
			BoxLayout BL = new BoxLayout(this, BoxLayout.Y_AXIS);

			setLayout(BL);
			this.W = W;
			JPanel JP = new JPanel();
            BL = new BoxLayout(JP, BoxLayout.X_AXIS);
            JP.setLayout(BL);
            FileName= new JTextField(25);
			ClassFileName = new JButton("Browse");
			ClassFileName.addActionListener(this);
            JP.add(FileName);
			JP.add(ClassFileName);

			ViewFile = new JButton("View File");
			ViewFile.addActionListener(this);
			JP.add(ViewFile);
            JP.setBorder( BorderFactory.createTitledBorder("Select Class with static Method"));
			add(JP);

			JP = new JPanel(new GridLayout(1, 2));
			JP.add(new JLabel("Methods"));
			MethodList = new JComboBox();
			MethodList.addActionListener(this);
			MethodList.addItemListener(this);
            MethodList.setEnabled(false);
			JP.add(MethodList);
            JP.setBorder( BorderFactory.createTitledBorder("Select Method"));
			add(JP);

			ArgListModel = new DefaultListModel();
			Arguments = new JList(ArgListModel);
			Arguments.setBorder(
				new TitledBorder(new LineBorder(Color.black), "Arguments"));
			Arguments.addListSelectionListener(this);
            Arguments.setEnabled(false);
			JP = new JPanel(new GridLayout(1, 2));
			JP.add(new JScrollPane(Arguments));
			JPanel JP1 = new JPanel(new GridLayout(4, 2));
			JButton LabelPGui = new JButton("Param GUI");

			LabelPGui.setToolTipText("Click for info on selected Parameter");
			LabelPGui.addActionListener(this);
			JP1.add(LabelPGui);
			ParamGUI = new JComboBox( ); //paramList );
            ParamGUI.setEnabled(false);
			fileName = System.getProperty("ISAW_HOME", "");
			JP1.add(ParamGUI);
			JP1.add(new JLabel("Prompt"));
			Prompt = new JTextField(12);
            Prompt.setEnabled(false);
			JP1.add(Prompt);
			JP1.add(new JLabel("Param Name"));
			VarName = new JTextField(12);
            VarName.setEnabled(false);
			JP1.add(VarName);
			JP1.add(new JLabel("Init Value"));
			InitValue = new JTextField(12);
            InitValue.setEnabled(false);
			JP1.add(InitValue);
			JP.add(JP1);     
           

            JP.setBorder( BorderFactory.createTitledBorder("Set GUI Info for Each Parameter"));
			add(JP);

            JP= new JPanel( new GridLayout(1,2));
            JP.add(new JLabel("Res Data type"));
            ResInf = new JLabel("          ");
            JP.add(ResInf);
            add(JP);
            
            JP= new JPanel();
            JP.setPreferredSize( new Dimension(50,5000));
			add(JP);
		}
		

		/**
		 * Sets up the list of arguments for a given method in the Arguments 
		 * JListbox
		 *
		 */
		public void SetUpArgList() {
           
			W.ArgsChanged();
            Arguments.setEnabled(true);
            this.ParamGUI.setEnabled( true);
            InitValue.setEnabled( true);
            Prompt.setEnabled(true);
            VarName.setEnabled( true);
			if (MethodList.getSelectedIndex() >= 0) {

				meth = ((MethHolder) MethodList.getSelectedItem()).method();
				opPanel.setFileName(MethodList.getSelectedItem().toString());
				
			} else if (meth == null)
				return;

            //display the method's return type on the gui
            ResInf.setText(meth.getReturnType().toString());
         
            //acquire and display the method's parameters on the gui
			Class[] ArgClass = meth.getParameterTypes();
			MethInfData mm = null;

			for (int i = 0; i < ArgClass.length; i++) {

				ArgListModel.addElement(ArgClass[i]);
				MethInfData md = new MethInfData(i);

				if (i == 0)
					W.methData = md;
				else
					mm.Next = md;
				mm = md;
			}
			Arguments.setSelectedIndex(0);
		}






		/**
		 *   Handles all action listening for MethodPanel class.  These
		 *   include
		 *   - Popping up the file chooser to find the file containing the
		 *      static method.  
		 *   -Setting up the argument list in the Listbox for 
		 *      arguments in case this event is called instead of and Item event,
		 *   -Popping up a JFrame with the contents of the file with the static 
		 *    method
		 *   -Creating help for a selected ParameterGUI
		 */
		public void actionPerformed(ActionEvent evt) {

			if (evt.getActionCommand().equals("Browse")) {
				JFileChooser jf = new JFileChooser(fileName);

				if (jf.showOpenDialog(null) != JFileChooser.APPROVE_OPTION){
				  
					return;
                 }
				fileName = jf.getSelectedFile().getAbsolutePath();
                FileName.setText(ForceExtension(fileName,"class"));
                this.MethodList.setEnabled(true);
				SetMethodList(fileName);
                

			} else if (evt.getSource() == MethodList) {
               
                this.Arguments.setEnabled( true);
                ClearDocs();
				SetUpArgList();

			} else if (evt.getSource() == ViewFile) {
				if (fileName == null)
					return;
				if (fileName.length() < 1)
					return;
				int k = fileName.lastIndexOf(".");
				String S = fileName;

				if (k >= 0) {
					S = fileName.substring(0, k) + ".java";
				}
				(new DataSetTools.operator.Generic.Special.ViewASCII(S)).getResult();
			} else if (evt.getActionCommand().equals("Param GUI")) {
				String T = (String) ParamGUI.getSelectedItem();

				if (T == null)
					return;

				boolean f = false;

				for (int k = 0;(k < pinf.getNParamTypes()) && !f; k++)
					if (pinf.getType(k).equals(T)) {
						f = true;
						JFrame jf = new JFrame("Help:" + T);

						jf.setSize(300, 300);
						jf.getContentPane().add(
							new JEditorPane("text/html", pinf.getToolTip(k, false)));
						WindowShower.show(jf);
					}
			}
		}





		/**
		 * Sets up the list of static methods from a class file
		 * @param fileName  the name of the class file
		 * NOTE: A stack trace will occur if the filename does not represent a
		 *          class or causes other exceptions when looking at it 
		 */
		public void SetMethodList(String fileName) {
            MethodChanged();
			try {
				if (W.getClassName(fileName) == null){
				
					return;
                }
				Class C = Class.forName(W.getClassName(fileName));

				MethodList.setBorder(
					BorderFactory.createTitledBorder(
						BorderFactory.createLineBorder(Color.black),
						C.getName().toString()));

				Method[] Meths = C.getMethods();

				W.MethodChanged();

				for (int i = 0; i < Meths.length; i++) {

					int mod = Meths[i].getModifiers();

					if (!Modifier.isAbstract(mod))
						if (Modifier.isPublic(mod))
							if (Modifier.isStatic(mod))
								if (!Modifier.isNative(mod))
									if (!Modifier.isInterface(mod)) {
										MethodList.addItem(new MethHolder(Meths[i]));
									}
				}
              MethodList.setEnabled( true);
			} catch (Exception s) {
				s.printStackTrace();
                JOptionPane.showMessageDialog(null, "improper class:"+
                          Command.ScriptUtil.GetExceptionStackInfo( s,true,1)[0]);
                
			}
		}





		/**
		 *  Sets up the list of arguments corresponding to a new Method
		 */
		public void itemStateChanged(ItemEvent e) {
            
			SetUpArgList();
		}



		int lastSelection = -1;// Saving the new value is triggered by changing
                               // the selected argument.

		/**
		 *  Saves the information about the previously selected argument to a
		 *  method and loads in the information from the new choice.
		 */
		public void valueChanged(ListSelectionEvent evt) {

			int i = ((JList) (evt.getSource())).getSelectedIndex();

			if (i < 0)
				return;
            this.ParamGUI.setEnabled( true);
            this.Prompt.setEnabled(true);
            this.InitValue.setEnabled(true);
            this.VarName.setEnabled(true);
			Class C = (Class) ((JList) (evt.getSource())).getSelectedValue();
			MethInfData m1 = W.methData.get(lastSelection);

			if (m1 != null) {
				m1.varName = VarName.getText().trim();
				m1.Prompt = Prompt.getText().trim();
				if (ParamGUI.getSelectedIndex() >= 0)
					m1.GUIParm = (ParamGUI.getSelectedItem()).toString();
				m1.InitValue = InitValue.getText();
			}

			//Now set up the possible Entries in the ParamGUI ComboBox
			ParamGUI.removeAllItems();

			for (int k = 0; k < pinf.getNParamTypes(); k++) {
				if (pinf.isEqual(k, C)) {
					ParamGUI.addItem(pinf.getType(k));
				}
			}

			// Now select the proper entry in the ParamGUI ComboBox
			m1 = W.methData.get(i);
			if (m1 != null) {
				VarName.setText(m1.varName);
				Prompt.setText(m1.Prompt);

				for (int k = 0; k < ParamGUI.getItemCount(); k++)
					if (ParamGUI.getItemAt(k).toString().equals(m1.GUIParm))
						ParamGUI.setSelectedIndex(k);
				InitValue.setText(m1.InitValue);
				lastSelection = i;
			}
		}
	}



	/**
	 * A class to hold a method variable.  The toString method is used to
	 * display a nicer form of the method instead of the method's toString 
	 * method.
	 * @author mikkelsonr
	 *
	 *
	 */
	class MethHolder {

		Method meth;
		public MethHolder(Method m) {
			meth = m;
		}

		public Method method() {
			return meth;
		}

		public String toString() {
			return meth.getName();
		}
	}


   /**
    *  The Panel contains the information about the operator that is to be
    * created
    *
    */
    class OpPanel extends JPanel implements ActionListener{
       Method2OperatorWizard W;
       JTextField OperatorTitle,
                  CommandName,
                  CategoryList;
       JButton Directory;
       JTextField fileName;
      
       public OpPanel( Method2OperatorWizard W){
         super();
         this.W=W;
         BoxLayout bl = new BoxLayout(this, BoxLayout.Y_AXIS);
         setLayout( bl);
         
         JPanel JP = new JPanel();
         bl= new BoxLayout( JP,BoxLayout.X_AXIS);
         JP.setLayout(bl);
         
         JP.setBorder( BorderFactory.createTitledBorder("Select Operator Output File"));
         fileName= new JTextField(30);
         String OpfileName = System.getProperty("ISAW_HOME", "");
         if (!OpfileName.endsWith(File.separator))
              OpfileName += File.separator;
         OpfileName += "Operators"; 

         fileName.setText(OpfileName);
         fileName.addActionListener( this);
         Directory = new JButton(OP_FILENAME);
         Directory.addActionListener(W);
         JP.add(fileName);
         JP.add(Directory);
         add(JP);
         add(Box.createVerticalGlue());
         JP= new JPanel( new GridLayout(3,2));
         JP.setBorder( BorderFactory.createTitledBorder("Set Operator Descriptions"));
      
         JP.add(new JLabel("Operator Title"));
         OperatorTitle = new JTextField();
         JP.add(OperatorTitle);      
         
         
         JP.add(new JLabel("Command for Scripts"));
         CommandName = new JTextField();
         JP.add(CommandName);
       
                
         JP.add(new JLabel("Category( for menu)"));
         CategoryList = new JTextField("Macros,MyMenu");
         JP.add(CategoryList);
        
         add(JP);

         JP=new JPanel();
         JP.setPreferredSize( new java.awt.Dimension(100,5000));
         add(JP);

         
       }



       /**
        * Sets up the information in this tab when the name of the Operator
        *  file is changed
        * @param opFileName  The new name of the operation file
        */
       public void setDefaults( String opFileName){
         if( opFileName == null)
            return;
         
         fileName.setText( opFileName );
         int i= opFileName.lastIndexOf('.');
         if( i<0)
             i = opFileName.length();
         opFileName= opFileName.substring(0, i);
         i=opFileName.lastIndexOf( File.separatorChar);
         if( i <0)
           i=-1;
        String S = opFileName.substring(i+1);
        OperatorTitle.setText(S);
        CommandName.setText(S);
        
        
        
       }
       
       public void setFileName( String operatorName )
       {
    	   String s = System.getProperty("ISAW_HOME", "");
           if (!s.endsWith(File.separator))
                s += File.separator;
           s += "Operators"; 
           s += File.separator;
    	   s += operatorName;
    	   s += ".java";
    	   fileName.setText(s);
    	   OperatorTitle.setText( operatorName );
    	   CommandName.setText( operatorName );
       }
      
      
      
      /**
       *  Invoked when the filename for the operator is changed in the text 
       *  field and a return is pressed.
       * @evt  The action event
       */
       public void actionPerformed( ActionEvent evt){
          if( evt.getSource() == fileName){
            fileName.setText( ForceExtension( fileName.getText(),"java"));
            setDefaults( fileName.getText());
          }
       }
    }
       

	/**
	 * Takes care of handling the Documentation tab in this application
	 * @author mikkelsonr
	 *
	 *
	 */
	class DocPanel extends JPanel implements  ComponentListener {

		Method2OperatorWizard W;
		JTextArea Assump,OverView,Algorithm,Return, Error;
        JTextArea[] Params = null;
        JScrollPane[] Pjscr= null;
	    boolean newOp=true;
  
		public DocPanel(Method2OperatorWizard W) {

			super();
			this.W = W;
           
            BoxLayout bl = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(bl); 
            OverView = new JTextArea(5,60);
            OverView.setLineWrap(true);
            OverView.setWrapStyleWord(true);
            Assump = new JTextArea(5,60);
            Assump.setLineWrap(true);
            Assump.setWrapStyleWord(true);
            Algorithm = new JTextArea(5,60);
            Algorithm.setLineWrap(true);
            Algorithm.setWrapStyleWord(true);
            Return = new JTextArea(3,60);
            Return.setLineWrap(true);
            Return.setWrapStyleWord(true);
            Error = new JTextArea(3,60);
            Error.setLineWrap(true);
            Error.setWrapStyleWord(true);
			newOp=true;
		}


	    public void componentHidden(ComponentEvent e) {}

		public void componentMoved(ComponentEvent e) {}

		public void componentResized(ComponentEvent e) {}

		/**
		 *  When this component is shown and there is a new operation
         *  All the data fields are initialized 
		 *
		 * @param e   The component Event
		 */
		public void componentShown(ComponentEvent e) {
          if( newOp){
            
            this.removeAll();
            newOp = false;
            W.methPanel.valueChanged(new ListSelectionEvent( W.methPanel.Arguments,
                                  0,1000,false)); 
            
            if(Params == null){
            
              Params = new JTextArea[ W.methPanel.Arguments.getModel().getSize()];
              for( int i=0; i<Params.length; i++)
                Params[i]= new JTextArea(2,60);
            }  
            add( JScrollwBorder( OverView,"OverView"));
            add( JScrollwBorder( Assump,"Assumption"));
            add( JScrollwBorder( Algorithm,"Algorithm"));
            add( JScrollwBorder( Return,"Return"));
            Pjscr= new JScrollPane[Params.length];
            for( int i=0;i<Params.length;i++){
              String S=W.methData.get(i).varName;
             if((S==null)||(S.length()<1))
                 S=""+i;
              Pjscr[i]=JScrollwBorder(Params[i], "Param:"+ S);
              add(Pjscr[i]);
            }
            add( JScrollwBorder(Error,"Error"));
            validate();
          }else if(Pjscr != null){//set Params[i] border title 
            for( int i=0; i< Pjscr.length; i++){
              String S=W.methData.get(i).varName;
              if((S==null)||(S.length()<1))
                  S=""+i;
              Pjscr[i].setBorder(BorderFactory.createTitledBorder("Param:"+ S));
            }
          }
        
		}
    
    
       private JScrollPane JScrollwBorder( JComponent comp, String Border){
            JScrollPane jscr= new JScrollPane(comp);
            jscr.setBorder( BorderFactory.createTitledBorder( Border));
            return jscr;
       }
       
    

	}






	/**
	 *  Handles saving the new operator, saving and retrieving the information
	 *  from this wizard.  This has moved to the menu bar
	 * @author mikkelsonr
	 *
	 */
	class FilePanel  implements ActionListener {

		Method2OperatorWizard W;

		public FilePanel(Method2OperatorWizard W) {

			
			this.W = W;
            JMenuBar jmenBar= new JMenuBar();
            JMenu  jmenFile = new JMenu("File");
            jmenBar.add( jmenFile);
            
            JMenuItem jmenContactLoad = new JMenuItem("Load Contact Info");
            jmenContactLoad.addActionListener(this);
            JMenuItem jmenContactSave = new JMenuItem("Save Contact Info");
            jmenContactSave.addActionListener(this);
            JMenuItem jb ;
           
			jmenFile.add( jmenContactLoad);
			jb = new  JMenuItem("Load Session");
			jb.addActionListener(this);
            jmenFile.add(jb);

           
            jb = new  JMenuItem("Create Operator");
            jb.addActionListener(this);
			jmenFile.add(jb);
      
            jmenFile.add( jmenContactSave);
            
            jb = new  JMenuItem("Save Session");
            jb.addActionListener(this);
            jmenFile.add(jb);
            
            jb = new JMenuItem("Exit");
            jb.addActionListener( this);
            jmenFile.add( jb );
            W.setJMenuBar( jmenBar);
		}


        /**
         *   Handles the menu events from the file menubar item
         */
		public void actionPerformed(ActionEvent evt) {

			if (evt.getActionCommand().equals("Save Session"))
				SaveState(W);
        
			else if (evt.getActionCommand().equals("Load Session"))
				RestoreState(W);
        
			else if (evt.getActionCommand().equals("Create Operator"))
				Save(W);
        
            else if( evt.getActionCommand().equals("Save Contact Info"))
               { 
                 String filname = System.getProperty("user.home","");
                 if(!filname.endsWith(File.separator))
                     filname+=File.separator;
                     
                JFileChooser jf = new JFileChooser( filname+"ContactInfo.xml");
                
                jf.setSelectedFile(new File(filname+"ContactInfo.xml"));
                if( jf.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
                     SaveContact( jf.getSelectedFile(),
                                  W.infPanel.Name.getText(),
                                  W.infPanel.Email.getText(),
                                   W.infPanel.Instit.getText(),
                                  W.infPanel.Address.getText(),
                                  W.infPanel.Acknowl.getText()
                           );
                }
            else if( evt.getActionCommand().equals("Load Contact Info"))
               {
                 String filname = System.getProperty("user.home","");
                 if(!filname.endsWith(File.separator))
                     filname+=File.separator;
                 JFileChooser jf = new JFileChooser( filname+"ContactInfo.xml");
                 jf.setSelectedFile(new File(filname+"ContactInfo.xml"));
                 
                 if( jf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                      GetContact( jf.getSelectedFile(),W.infPanel );                     
               
               }
            else if( evt.getActionCommand().equals("Exit")){
              
              W.dispose();
              try{
                 W.finalize();
              }catch( Throwable s){
                JOptionPane.showMessageDialog(null, "Could not finish closing the window:"+s.toString());
              }
              
            }
               
		}
    
    

		private boolean ShowMess(String message) {

			JOptionPane.showMessageDialog(null, message);
			return false;
		}



		private boolean check() {

			if (W.opPanel.OperatorTitle.getText() == null)
				return ShowMess("No Operator Title is Specified");

			if (W.opPanel.OperatorTitle.getText().length() < 1)
				return ShowMess("No Operator Title is Specified");

			if (W.opPanel.CommandName.getText() == null)
				return ShowMess("No Command Name for the operator is Specified");

			if (W.opPanel.CommandName.getText().length() < 1)
				return ShowMess("No Command Name for the operator is Specified");

			if (!(new File(W.methPanel.fileName)).exists())
				return ShowMess("The filename with the method does not exist");
        
			if (W.methPanel.meth == null)
				return ShowMess("No method from the Method File has been selected");
        
			W.methPanel.valueChanged(
				new ListSelectionEvent(
					W.methPanel.Arguments,
					0,
					W.methPanel.Arguments.getComponentCount(),
					false));

			for (MethInfData m = methData; m != null; m = m.Next) {
				if ((m.GUIParm == null) || (m.GUIParm.length() < 1))
					return ShowMess("No ParamGUI set for arg " + m.argnum);
				if ((m.varName == null) || (m.varName.length() < 1))
					return ShowMess("Variable name not given for parameter " + m.argnum);
				if ((m.InitValue == null) || (m.InitValue.length() < 1))
					m.InitValue = "null";
			}
			 
			return true;
		}
    
    

        private void GetContact( File F, InfoPanel InfPanel){
          try{
    
           Document D= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( F);
           Node child = D.getFirstChild();
           if( child== null)
              return;
           if( child.getNodeType() != Node.ELEMENT_NODE){
             System.out.println(" First Child is not the Element NOde");
             return;
           }
           Element E =(Element)child;
           infPanel.Name.setText( E.getAttribute("Name"));
           infPanel.Email.setText( E.getAttribute("Email"));
           infPanel.Instit.setText( E.getAttribute("Instit"));
           infPanel.Address.setText( E.getAttribute("Address"));
           infPanel.Acknowl.setText( E.getAttribute("Acknowl"));
           
           
          }catch(Exception s){
            JOptionPane.showMessageDialog(null, "Could not read file:"+s.toString());
          }
        
       }
       
       
       
       /**
        *  Saves Contact information
        */
       private void SaveContact( File F, String Name, String  Email, 
                              String  Instit, String  Address,  String Acknowl  ){
           DocumentBuilderFactory DFact= DocumentBuilderFactory.newInstance();
           DocumentBuilder DocBuilder=null;
           try{
           
               DocBuilder = DFact.newDocumentBuilder();
           }catch( Exception ss){
             JOptionPane.showMessageDialog(null, "Cannot Save Contact Info:"+ss.toString());
             return;
           }
           Document D1=null;
           Element D = null;
           try{
           
           D1 = DocBuilder.newDocument();
           D= D1.createElement("ContactInfo");
           D1.appendChild(D);
           
           
           D.setAttribute("Name",Name);
           
     
           D.setAttribute("Email",Email);
    
           D.setAttribute("Instit",Instit);
 
           D.setAttribute("Address",Address);

           D.setAttribute("Acknowl",Acknowl);
         
           }catch(Exception s3){
             JOptionPane.showMessageDialog( null,"Cannot Save:"+s3.toString());
             return;
           }
           
          StreamResult output = new StreamResult( F);
          DOMSource DSource = new DOMSource( D1);
          Transformer Trans=null;
          try{
          
            Trans = TransformerFactory.newInstance().newTransformer();
            Trans.transform(DSource, output);
            JOptionPane.showMessageDialog (null, "Saved Contact Info to "+ F.getAbsolutePath());
          }catch(Exception s2){
            JOptionPane.showMessageDialog(null, "Cannot Save ContactInfo :"+s2.toString());
            return;
          }
          
          
           
           
                               
                                
       }                               
                               
		/**
		 * Saves the information currently in the wizard to the operator file
		 * @param W
		 */
		public void Save(Method2OperatorWizard W) {

			if (!check())
				return;
            W.opPanel.fileName.setText( ForceExtension(W.opPanel.fileName.getText(),"java"));
			String packge = W.getClassName(W.opPanel.fileName.getText().trim());

			if (packge == null) {

				ShowMess("Cannot find package name for new operator file");
				return;
			}
			String clsName;

			if (packge.endsWith(".java"))
				packge = packge.substring(0, packge.length() - 5);
			int k = packge.lastIndexOf(".");

			if (k < 0)
				return;

			clsName = packge.substring(k + 1);
			packge = packge.substring(0, k);
			try {
				FileOutputStream fout = new FileOutputStream(W.opPanel.fileName.getText());

				fout.write(("/* \r\n * File: " + clsName + ".java\r\n *  \r\n").getBytes());
				fout.write(
					(" * Copyright (C) "
						+ Calendar.getInstance().get(Calendar.YEAR)
						+ "     "
						+ W.infPanel.Name.getText().trim()
						+ "\r\n" + " *" + "\r\n")
						.getBytes());
				fout.write(
					" * This program is free software; you can redistribute it and/or\r\n"
						.getBytes());
				fout.write(
					" * modify it under the terms of the GNU General Public License\r\n"
						.getBytes());
				fout.write(
					" * as published by the Free Software Foundation; either version 2\r\n"
						.getBytes());
				fout.write(
					" * of the License, or (at your option) any later version.\r\n * \r\n"
						.getBytes());
				fout.write(
					" * This program is distributed in the hope that it will be useful,\r\n"
						.getBytes());
				fout.write(
					" * but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n"
						.getBytes());
				fout.write(
					" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\r\n"
						.getBytes());
				fout.write(
					" * GNU General Public License for more details.\r\n * \r\n"
						.getBytes());
				fout.write(
					" * You should have received a copy of the GNU General Public License\r\n"
						.getBytes());
				fout.write(
					" * along with this library; if not, write to the Free Software\r\n"
						.getBytes());
				fout.write(
					" * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.\r\n"
						.getBytes());
				fout.write(" *\r\n * Contact :  ".getBytes());
				fout.write(
					(W.infPanel.Name.getText().trim()
						+ "<"
						+ W.infPanel.Email.getText().trim()
						+ ">\r\n")
						.getBytes());
				String S = W.infPanel.Address.getText().trim();

				k = 0;
				int k1 = S.indexOf('\n');

				if (k1 < 0)
					k1 = S.length();
				while (k < S.length()) {

					fout.write(
						(" *            " + S.substring(k, k1).trim() + "\r\n").getBytes());
					k = k1 + 1;
					if (k1 < S.length())
						k1 = S.indexOf('\n', k1 + 1);
                    if( k1 < 0)
                       k1= S.length();
				}

				fout.write((" *\r\n").getBytes());

				S = W.infPanel.Acknowl.getText().trim();
				k = 0;
				k1 = S.indexOf('\n');
				if (k1 < 0)
					k1 = S.length();
				while (k < S.length()) {
					fout.write((" * " + S.substring(k, k1).trim() + "\r\n").getBytes());
					k = k1 + 1;
					if (k1 < S.length())
						k1 = S.indexOf('\n', k1 + 1);
					if (k1 < 0)
						k1 = S.length(); 
				}

				fout.write(
					(" *\r\n *\r\n * Modified:\r\n *\r\n * $" + "Log:" + "$\r\n" + " *" + "\r\n */\r\n\r\n")
						.getBytes());

				//Write out package information
				fout.write(("package " + packge + ";\r\n").getBytes());
				fout.write("import DataSetTools.operator.*;\r\n".getBytes());
				fout.write("import DataSetTools.operator.Generic.*;\r\n".getBytes());
				fout.write("import DataSetTools.parameter.*;\r\n\r\n".getBytes());
				fout.write(
					"import gov.anl.ipns.Util.SpecialStrings.*;\r\n\r\n".getBytes());
				fout.write("import Command.*;\r\n\r\n".getBytes());

				// Documentation for the getResult method
				fout.write("/**\r\n".getBytes());
				fout.write(" * This class has been dynamically created using the Method2OperatorWizard\r\n".getBytes());
				fout.write(" * and usually should not be edited.\r\n".getBytes());
				fout.write(" */\r\n".getBytes());				
				
				//Write out the class header
				fout.write(
					("public class " + clsName + " extends GenericOperator{\r\n\r\n\r\n")
						.getBytes());
				
				// Documentation for the constructor
				fout.write("   /**\r\n".getBytes());
				fout.write("	* Constructor for the operator.  Calls the super class constructor.\r\n".getBytes());
				fout.write("    */\r\n".getBytes());
				
				fout.write(
					("   public "
						+ clsName
						+ "(){\r\n     super(\""
						+ W.opPanel.OperatorTitle.getText().trim()
						+ "\");\r\n     }\r\n\r\n\r\n")
						.getBytes());

				// Documentation for the getCommand method
				fout.write("   /**\r\n".getBytes());
				fout.write("    * Gives the user the command for the operator.\r\n".getBytes());
				fout.write("    *\r\n".getBytes());
				fout.write("	* @return  The command for the operator, a String.\r\n".getBytes());
				fout.write("    */\r\n".getBytes());
				
				//Write out the getCommand     
				fout.write(("   public String getCommand(){\r\n").getBytes());
				fout.write(
					("      return \""
						+ W.opPanel.CommandName.getText().trim()
						+ "\";\r\n   }\r\n\r\n\r\n")
						.getBytes());

				// Documentation for the setDefaultParameters method
				fout.write("   /**\r\n".getBytes());
				fout.write("    * Sets the default parameters for the operator.\r\n".getBytes());
				fout.write("    */\r\n".getBytes());
				
                // Write out the setDefaultParameters 
				fout.write("   public void setDefaultParameters(){\r\n".getBytes());
				fout.write("      clearParametersVector();\r\n".getBytes());
				for (int i = 0; i < W.methPanel.meth.getParameterTypes().length; i++) {

					MethInfData m = W.methData.get(i);

					fout.write(
						("      addParameter( new "
							+ m.GUIParm
							+ "PG(\""
							+ m.Prompt
							+ "\","
							+ m.InitValue
							+ "));\r\n")
							.getBytes());
				}
				fout.write("   }\r\n\r\n\r\n".getBytes());

				// Documentation for the getDocumentation method
				fout.write("   /**\r\n".getBytes());
				fout.write("    * Writes a string for the documentation of the operator provided by\r\n".getBytes());
				fout.write("    * the user.\r\n".getBytes());
				fout.write("    *\r\n".getBytes());
				fout.write("	* @return  The documentation for the operator.\r\n".getBytes());
				fout.write("    */\r\n".getBytes());
				
				// Write out the documentation
				fout.write("   public String getDocumentation(){\r\n".getBytes());
				fout.write("      StringBuffer S = new StringBuffer();\r\n".getBytes());
				
				//Write out the contents of the Documentation TextArea
				fout.write("      S.append(\"@overview    \"); \r\n".getBytes());
				fout.write(
					MultiLine_ify(W.docPanel.OverView.getText(), "      S.append(\"", "\");\r\n")
						.getBytes());
				
				//Write out the contents of the Algorithm TextArea
				fout.write("      S.append(\"@algorithm    \"); \r\n".getBytes());				
				fout.write(
					MultiLine_ify(W.docPanel.Algorithm.getText(), "      S.append(\"", "\");\r\n")
						.getBytes());
				
				//Write out the contents of the Assumptions TextArea
				fout.write("      S.append(\"@assumptions    \"); \r\n".getBytes());				
				fout.write(
					MultiLine_ify(W.docPanel.Assump.getText(), "      S.append(\"", "\");\r\n")
						.getBytes());
				
				for (MethInfData m = W.methData; m != null; m = m.Next) {
					fout.write("      S.append(\"@param   \");\r\n".getBytes());
					fout.write(
						MultiLine_ify(W.docPanel.Params[m.argnum].getText(), 
                                                         "      S.append(\"", "\");\r\n").getBytes());
				}
				
                String ret=StringUtil.replace(W.docPanel.Return.getText(), "\"", "\\\"");
                if( ret != null)
                  ret=ret.trim();
                int kz=-1;
                if( ret != null){
                
                   kz=ret.indexOf("\n");
                   if( kz<0)
                     k=ret.length();
                }
                if( kz>0){
                
				fout.write(
					("      S.append(\"@return "
						+ ret.substring(0,kz)
						+ "\");\r\n")
						.getBytes());
                 if(kz < ret.length())
                 fout.write(MultiLine_ify(ret.substring(kz),
                                "      S.append(\"","\");\r\n").
                                getBytes());
                }      			

                //Write out the contents of the Error TextArea
				fout.write(("      S.append(\"@error \");\r\n"+
				                 MultiLine_ify(	W.docPanel.Error.getText(),
							      "      S.append(\"","\");\r\n"))
							    .getBytes());		
 
				fout.write("      return S.toString();\r\n   }\r\n\r\n\r\n".getBytes());

				// Documentation for the getCategoryList method
				fout.write("   /**\r\n".getBytes());
				fout.write("    * Returns a string array with the category the operator is in.\r\n".getBytes());
				fout.write("    *\r\n".getBytes());
				fout.write("    * @return  An array containing the category the operator is in.\r\n".getBytes());
				fout.write("    */\r\n".getBytes());				
				
				// Write out getCategoryList method;
				WriteCategoryList(fout, W.opPanel.CategoryList.getText().trim());
				
				// Documentation for the getResult method
				fout.write("   /**\r\n".getBytes());
				fout.write("    * Returns the result of the operator, otherwise and ErrorString.\r\n".getBytes());
				fout.write("    *\r\n".getBytes());
				fout.write("    * @return  The result of the operator, or an ErrorString.\r\n".getBytes());
				fout.write("    */\r\n".getBytes());	

				//Write out the getResult method       
				fout.write("   public Object getResult(){\r\n".getBytes());

				Class[] CCS = W.methPanel.meth.getParameterTypes();
				fout.write("      try{\r\n\r\n".getBytes());
				for (int i = 0; i < CCS.length; i++) {
					MethInfData m = W.methData.get(i);

					fout.write(
						("         " + FixUpClassName(CCS[i]) + " " + m.varName + " = ")
							.getBytes());
					fout.write(
						(GetPrepend(m.GUIParm, CCS[i])
							+ "getParameter("
							+ i
							+ ")"
							+ GetAppend(m.GUIParm, CCS[i])
							+ ";\r\n")
							.getBytes());
				}
				String MethClassName =
					W.FixUpClassName(W.methPanel.meth.getDeclaringClass())
						+ "."
						+ W.methPanel.meth.getName();

				boolean MethReturnsVoid = false;
				if (W.methPanel.ResInf.getText().indexOf("void") >= 0)
					MethReturnsVoid = true;
				if (!MethReturnsVoid)
					fout.write(
						("         "
							+ FixUpClassName(W.methPanel.ResInf.getText().trim())
							+ " Xres=")
							.getBytes());
				else
					fout.write("         ".getBytes());
				fout.write((MethClassName + "(").getBytes());
				for (int i = 0; i < CCS.length; i++)
					fout.write(
						(W.methData.get(i).varName + sepChar(i, CCS.length)).getBytes());
				if (!MethReturnsVoid)
					fout.write(
						(";\r\n         return "
							+ MakeObj(FixUpClassName(W.methPanel.ResInf.getText().trim()))
							+ ";\r\n       }catch(")
							.getBytes());
				else
					fout.write(
						";\r\n         return \"Success\";\r\n      }catch(".getBytes());
				Class[] Exceptions = W.methPanel.meth.getExceptionTypes();
				for (int kk = 0; kk < Exceptions.length; kk++) {
					fout.write(
						(FixUpClassName(Exceptions[kk]) + " S" + kk + "){\r\n         ")
							.getBytes());
					fout.write(
						("return new ErrorString(S" + kk + ".getMessage());\r\n      }catch(")
							.getBytes());
				}
				fout.write(
					" Throwable XXX){\r\n         return new ErrorString( XXX.toString()+\":\"\r\n             +ScriptUtil"
						.getBytes());
				fout.write(
					".GetExceptionStackInfo(XXX,true,1)[0]);\r\n      }\r\n   }\r\n\r\n"
						.getBytes());

				fout.write("}\r\n\r\n\r\n".getBytes());

				fout.close();
                JOptionPane.showMessageDialog(null, "Saved Operator to"+W.opPanel.fileName.getText());
			} catch (Exception s) {

				JOptionPane.showMessageDialog(null, "Cannot Save file " + s.toString());
			}

		}


		private void WriteCategoryList(FileOutputStream fout, String CatList) {
			if (fout == null)
				return;
			if (CatList == null)
				return;
			if (CatList.length() < 1)
				return;
			String[] S = CatList.split(",");
			if (S == null)
				return;
			try {
				fout.write(
					"   public String[] getCategoryList(){\r\n      ".getBytes());
				fout.write("      return new String[]{\r\n".getBytes());
				for (int i = 0; i < S.length; i++) {
					fout.write(("                     \"" + S[i] + "\"").getBytes());
					if (i + 1 < S.length)
						fout.write(",\r\n".getBytes());
					else
						fout.write(
							"\r\n                     };\r\n   }\r\n\r\n\r\n".getBytes());
				}
			} catch (Exception s) {
				return;
			}
		}


		private String MultiLine_ify(String text, String prepend, String append) {

			if ((text == null) || (text.length() < 1))
				return prepend + append;
			int j1 = 0;
			int j2 = text.indexOf("\n");

			if (j2 < 0)
				j2 = text.length();
			String Res = "";
			
		    int num = 0;

			for (; j1 < text.length();) {
				if(num == 1) {
					prepend = prepend + " ";
				}
				Res += prepend
					+ StringUtil.replace(text.substring(j1, j2).trim(), "\"", "\\\"")
					+ append;
				j1 = j2 + 1;
				if (j1 + 1 < text.length())
					j2 = text.indexOf("\n", j1);
                else
                    j2 = text.length();
				if (j2 < 0)
					j2 = text.length();
				
				num++;
			}
			return Res;
		}


		private char sepChar(int i, int length) {

			if (i + 1 < length)
				return ',';
			else
				return ')';
		}



		/**
		 * Saves the information from the current wizard so this state can be
		 * recreated by RestoreState operation
          * 
		 * @param W  This wizard
		 */
		public void SaveState(Method2OperatorWizard W) {
      
			check();
			String fileName = ForceExtension(W.opPanel.fileName.getText(),"cls");

			if (fileName == null)
				return;
			
			try {

				FileOutputStream fout = new FileOutputStream(fileName);
				ObjectOutputStream Oout = new ObjectOutputStream(fout);

				Oout.writeObject((Object) W.infPanel.Acknowl.getText());
				Oout.writeObject((Object) W.infPanel.Address.getText());
				Oout.writeObject((Object) W.infPanel.Email.getText());
				Oout.writeObject((Object) W.infPanel.Instit.getText());
				Oout.writeObject((Object) W.infPanel.Name.getText());
				Oout.writeObject((Object) W.opPanel.CommandName.getText());
				Oout.writeObject((Object) W.opPanel.OperatorTitle.getText());

				Oout.writeObject((Object) W.opPanel.fileName.getText());

				Oout.writeObject((Object) W.methPanel.fileName);
				Oout.writeObject((Object) W.methPanel.meth.getName());
				Class[] CC = W.methPanel.meth.getParameterTypes();

				Oout.writeInt(CC.length);
				for (int i = 0; i < CC.length; i++)
					Oout.writeObject(CC[i]);
				Oout.writeObject((Object) W.docPanel.OverView.getText());
				Oout.writeObject((Object) W.docPanel.Algorithm.getText());
				Oout.writeObject((Object) W.docPanel.Assump.getText());
				Oout.writeObject((Object) W.docPanel.Error.getText());
				Oout.writeObject((Object) W.docPanel.Return.getText());
				Oout.writeObject((Object) W.docPanel.Algorithm.getText());
				int C = 1;

				if (W.methData != null)
					for (MethInfData m = W.methData; m.Next != null; m = m.Next)
						C++;
				else
					C = 0;
				Oout.writeInt(C);
				MethInfData m = null;

				for (int i = 0; i < C; i++) {
					if (m == null)
						m = W.methData;
					else
						m = m.Next;
					Oout.writeInt(m.argnum);
					Oout.writeObject(m.varName);
					Oout.writeObject(m.Prompt);
					Oout.writeObject(m.InitValue);
					Oout.writeObject(m.GUIParm);
                    if((W.docPanel.Params != null) && (W.docPanel.Params.length >i ))
					  Oout.writeObject(W.docPanel.Params[i].getText());
                    else
                      Oout.writeObject("");

				}
				Oout.writeObject(W.opPanel.CategoryList.getText().trim());
                JOptionPane.showMessageDialog(null,"Saved Session to "+fileName);
			} catch (Exception s) {

				JOptionPane.showMessageDialog(null, "Could not Save:" + s.toString());
			}
		}
    
    

		/**
		 * Restores the state of the wizard to the state stored in the .cls file
		 * 
		 * @param W  this wizard
		 */
		public void RestoreState(Method2OperatorWizard W) {

			JFileChooser jf = new JFileChooser(System.getProperty("ISAW_HOME", ""));

			if (jf.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			String fileName = jf.getSelectedFile().getAbsolutePath();
            fileName= ForceExtension( fileName, "cls");
			if (fileName == null)
				return;
			if (fileName.length() < 1)
				return;

			try {
				FileInputStream fout = new FileInputStream(fileName);
				ObjectInputStream Oout = new ObjectInputStream(fout);

				W.infPanel.Acknowl.setText((String) Oout.readObject());
				W.infPanel.Address.setText((String) Oout.readObject());
				W.infPanel.Email.setText((String) Oout.readObject());
				W.infPanel.Instit.setText((String) Oout.readObject());
				W.infPanel.Name.setText((String) Oout.readObject());
				W.opPanel.CommandName.setText((String) Oout.readObject());
				W.opPanel.OperatorTitle.setText((String) Oout.readObject());

				String OpfileName = (String) Oout.readObject();
                W.opPanel.fileName.setText( OpfileName);
				W.methPanel.fileName = (String) Oout.readObject();
                W.methPanel.FileName.setText( W.methPanel.fileName );
				String Name = (String) Oout.readObject();
				int CC = Oout.readInt();
				Class[] Cl = new Class[CC];

				for (int i = 0; i < Cl.length; i++)
					Cl[i] = (Class) Oout.readObject();
				

				if (W.methPanel.fileName == null)
					W.MethodChanged();
          
				else if (W.methPanel.fileName.length() < 1)
					W.MethodChanged();
          
				else {
					W.methPanel.SetMethodList(W.methPanel.fileName);
					W.methPanel.meth = null;
					for (int i = 0;
						(i < W.methPanel.MethodList.getItemCount())
							&& (W.methPanel.meth == null);
						i++) {
						Method M =
							((MethHolder) W.methPanel.MethodList.getItemAt(i)).method();

						if (M.getName().equals(Name)) {
							Class[] CM = M.getParameterTypes();

							W.methPanel.meth = M;
							if (CM.length == Cl.length)
								for (int kk = 0; kk < CM.length; kk++) {

									if (!CM[kk].equals(Cl[kk]))
										W.methPanel.meth = null;

								} else
								W.methPanel.meth = null;
							if (W.methPanel.meth != null)
								W.methPanel.MethodList.setSelectedIndex(i);
						}
					}
				}

                W.docPanel.newOp=true;
				W.docPanel.OverView.setText( (String) Oout.readObject());
				W.docPanel.Algorithm.setText( (String) Oout.readObject());
				W.docPanel.Assump.setText( (String) Oout.readObject());
				W.docPanel.Error.setText( (String) Oout.readObject());
				W.docPanel.Return.setText( (String) Oout.readObject());
				W.docPanel.Algorithm.setText( (String) Oout.readObject());
				int C = Oout.readInt();
				MethInfData m, mlast = null;
                
				W.methData = null;
                W.docPanel.Params= new JTextArea[C];
                W.methPanel.lastSelection = -1;
				for (int i = 0; i < C; i++) {
          
					m = new MethInfData(i);
					m.argnum = Oout.readInt();
					m.varName = (String) Oout.readObject();
					m.Prompt = (String) Oout.readObject();
					m.InitValue = (String) Oout.readObject();
					m.GUIParm = (String) Oout.readObject();
                    W.docPanel.Params[i]= new JTextArea(2,60);
                    W.docPanel.Params[i].setWrapStyleWord(true);
					W.docPanel.Params[i].setText( (String) Oout.readObject());
					if (mlast == null) {
						W.methData = m;
					} else
						mlast.Next = m;
					mlast = m;
				}

				W.opPanel.CategoryList.setText((String) Oout.readObject());
			} catch (Exception s) {

				JOptionPane.showMessageDialog(null, "Could not Load:" + s.toString());
			}
		}
	}



	/**
	 *  Linked list that stores information on the arguments
	 * @author mikkelsonr
	 *
	 *
	 */
	class MethInfData implements Serializable {

		int argnum;
		String varName;
		String Prompt;
		String GUIParm;
		String InitValue;
		MethInfData Next;
		String Docum;
		
		public MethInfData(
			int argnum,
			String varName,
			String Prompt,
			String GUIParm) {

			this.argnum = argnum;
			this.varName = varName;
			this.Prompt = Prompt;
			this.GUIParm = GUIParm;
			Next = null;
		}

		public MethInfData(int argnum) {

			this(argnum, "", "", "");
		}

		public void setData(String varName, String Prompt, String GUIParm, String Method) {

			this.varName = varName;
			this.Prompt = Prompt;
			this.GUIParm = GUIParm;
		}

		public MethInfData getNext() {

			return Next;
		}

		public MethInfData get(int argNum) {

			if (argNum < argnum)
				return null;

			if (argNum == argnum)
				return this;

			if (Next == null)
				return null;

			return Next.get(argNum);
		}
	}



	class CompListener extends ComponentAdapter {

		Method2OperatorWizard W;
		public CompListener(Method2OperatorWizard W) {

			this.W = W;
		}

		public void componentShown(ComponentEvent evt) {
           
			W.docPanel.componentShown(evt);
		}
	}
}
