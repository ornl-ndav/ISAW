/*
 * File:  Wizard.java
 *
 * Copyright (C) 2002, Dennis Mikkelson, 2003 Chris Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by 
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.23  2003/06/09 14:51:23  bouzekc
 * Added code to handle ErrorStrings as well as a Boolean
 * "false" for Form invalidation.
 *
 * Revision 1.22  2003/06/05 22:17:52  bouzekc
 * Incremental improvement to more carefully define what
 * should be listed in the Wizard's View menu.
 *
 * Revision 1.21  2003/06/04 14:13:21  bouzekc
 * Improved the parameter checking for the <View> menu.
 *
 * Revision 1.20  2003/06/02 21:57:35  bouzekc
 *
 * Revision 1.19  2003/05/08 15:10:30  pfpeterson
 * Added a FileFilter to the save and load dialogs. (Chris Bouzek)
 *
 * Revision 1.18  2003/04/29 14:08:37  pfpeterson
 * Generate help page for Form from HTMLizer. (Chris Bouzek)
 *
 * Revision 1.17  2003/04/28 16:17:58  pfpeterson
 * Now recalls save/load filename. (Chris Bouzek)
 *
 * Revision 1.16  2003/04/24 18:56:06  pfpeterson
 * Added functionality to save Wizards. (Chris Bouzek)
 *
 * Revision 1.15  2003/04/02 14:56:51  pfpeterson
 * Changed to work with new Forms. (Chris Bouzek)
 *
 * Revision 1.14  2003/03/19 15:03:24  pfpeterson
 * Better implementation of the view menu. It is dynamically created
 * to contain only valid parameters. (Chris Bouzek)
 *
 * Revision 1.13  2003/03/13 15:31:20  pfpeterson
 * The next generation of parameter viewing. This allows any parameter
 * to be viewed. (Chris Bouzek).
 *
 * Revision 1.12  2003/03/11 19:51:20  pfpeterson
 * First implementation of the 'View DataSet' button (Chris Bouzek).
 *
 * Revision 1.11  2003/03/06 15:50:18  pfpeterson
 * Changed to work with SharedData's private StatusPane.
 *
 * Revision 1.10  2003/02/27 18:01:46  pfpeterson
 * Fixed bug when values are changed on forms, enlarged the status_pane,
 * and added some more debug statements.
 *
 * Revision 1.9  2003/02/26 21:43:47  pfpeterson
 * Changed reference to Form.setCompleted(false) to From.invalidate().
 *
 * Revision 1.8  2003/02/26 17:20:43  rmikk
 * Now uses DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.7  2003/02/24 20:47:29  pfpeterson
 * Added a debug flag and made exec_forms smarter. Now properly invalidates
 * and sets progress bar on failure.
 *
 * Revision 1.6  2003/02/11 15:09:38  dennis
 * Bugfix...CommandHandler constructor can't be private.
 * (Chris Bouzek)
 *
 * Revision 1.5  2002/11/27 23:26:33  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/06/12 13:36:54  pfpeterson
 * invalidate method now sets the value of the progress bar.
 *
 * Revision 1.3  2002/06/11 14:56:18  pfpeterson
 * Small updates to documentation.
 *
 * Revision 1.2  2002/06/06 16:15:45  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:36:00  pfpeterson
 * Moved files
 *
 * Revision 1.4  2002/04/12 20:53:06  pfpeterson
 * More updates to the GUI.
 *
 * Revision 1.3  2002/04/11 22:35:32  pfpeterson
 * Big changes including:
 *   - new GUI (layout works better)
 *   - execute button runs previous forms if not already done.
 *   - invalidates forms after the one being executed.
 *
 * Revision 1.2  2002/03/12 16:09:45  pfpeterson
 * Now automatically disable constant and result parameters.
 *
 * Revision 1.1  2002/02/27 17:27:52  dennis
 * Wizard class for controlling a sequence of "Forms" that
 * determine a calculation
 */

package DataSetTools.wizard;

import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import java.beans.*;
import java.io.*;
import DataSetTools.dataset.DataSet;
import IsawHelp.HelpSystem.HTMLizer;
import DataSetTools.wizard.util.*;

/**
 *  The Wizard class provides the top level control for a sequence of
 *  operations to be carried out with user interaction.  The
 *  quantities the the user interacts with are stored in a master list
 *  of all quantities used in the Wizard.  The quantites are stored as
 *  IParameterGUI objects.  The operations that are to be carried out
 *  are described by Form objects.  The Wizard also controls a
 *  sequence of forms and allows the user to step back and forth
 *  between forms. Each form defines an execute() method to carry out
 *  the action determined by the form. The Wizard also manages a
 *  simple help system. Help messages should be set for each form
 *  that is used in a particular Wizard application, as well as for
 *  the Wizard itself.
 *
 *  @see Form
 */

public abstract class Wizard implements PropertyChangeListener{
    // size of the window
    private static final int FRAME_WIDTH   = 650;
    private static final int FRAME_HEIGHT  = 500;

    // string constants for the menus and buttons
    private static final String EXIT_COMMAND        = "Exit";
    private static final String FIRST_COMMAND       = "First";
    private static final String BACK_COMMAND        = "Back";
    private static final String NEXT_COMMAND        = "Next";
    private static final String LAST_COMMAND        = "Last";
    private static final String CLEAR_COMMAND       = "Clear All";
    private static final String EXEC_ALL_COMMAND    = "Exec All";
    private static final String EXEC_COMMAND        = "Execute";
    private static final String HELP_ABOUT_COMMAND  = "About";
    private static final String WIZARD_HELP_COMMAND = "on Wizard";
    private static final String FORM_HELP_COMMAND   = "on Current Form";
    private static final String SAVE_FORM_COMMAND   = "Save Current Form";
    private static final String LOAD_FORM_COMMAND   = "Load Current Form";
    private static final String SAVE_WIZARD_COMMAND = "Save Wizard";
    private static final String LOAD_WIZARD_COMMAND = "Load Wizard";
    private static final String VIEW_MENU           = "View";

    // default help and about messages
    private String help_message  = "Help not available for Wizard";
    private String about_message = "Default Help About Message";
    private boolean standalone   = true;

    // debugging
    private static final boolean DEBUG=false;

    // instance variables
    private JFrame       frame;
    private String       title;
    protected Vector       forms;
    private int          form_num;
    private JPanel       form_panel;
    private JButton      exec_all_button;
    private JButton      first_button;
    private JButton      back_button;
    private JButton      next_button;
    private JButton      last_button;
    private JLabel       form_label;
    private JProgressBar progress;
    private JMenu        view_menu;
    private CommandHandler command_handler;
    private boolean modified = false;
    private JFrame save_frame;
    private JFileChooser fileChooser;
    private ObjectOutputStream  output;
    private ObjectInputStream input;
    private File save_file;

    /**
     * The legacy constructor
     *
     * @param title Name displayed at top of window.
     */
    public Wizard( String title ){
        this(title,true);
    }

    /**
     * The full constructor
     *
     * @param title Name displayed at top of window
     * @param standalone If is running by itself
     */
    public Wizard( String title, boolean standalone){
        this.title  = title;
        forms       = new Vector();
        form_num    = -1;
        frame       = new JFrame( title );
        form_panel  = new JPanel();
        form_label  = new JLabel(" ",SwingConstants.CENTER);
        progress    = new JProgressBar();
        command_handler = new CommandHandler(this);
        save_frame = new JFrame("Save Form as...");
    }

    /**
     *  Opens a file for input or output.
     *  
     *  @param saving  A boolean indicating whether you want to open the
     *                 file for saving (true) or loading (false)                      
     */
    private File getFile(boolean saving)
    {
      int result;
      String save_file_abs_path;
      if(fileChooser == null)
      {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(
          JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new WizardFileFilter());
      }

      if((save_file !=null) && !save_file.toString().equals(""))
        fileChooser.setSelectedFile(save_file);

      if(saving)
        result = fileChooser.showSaveDialog(save_frame);
      else
        result = fileChooser.showOpenDialog(save_frame);

      if( result == JFileChooser.CANCEL_OPTION )
        return null;

      save_file = fileChooser.getSelectedFile();

      if(saving)
      {
        save_file_abs_path = save_file.toString();
        save_file_abs_path = FileExtension.appendExtension(save_file_abs_path, 
                           fileChooser.getFileFilter());
        save_file = new File(save_file_abs_path);
      }

      if(saving && save_file.exists())
      {
        String temp;
        StringBuffer s = new StringBuffer();
        s.append("You are about to overwrite ");
        s.append(save_file.toString());
        s.append(".\n  If this is OK, press ");
        s.append("<Enter> or click the <OK> button.\n  Otherwise, please ");
        s.append("enter a new name or click <Cancel>.");
        temp = JOptionPane.showInputDialog(s.toString());
        if(temp != null && !temp.equals(""))
        {
          temp = FileExtension.appendExtension(temp, fileChooser.getFileFilter());
          save_file = new File(fileChooser.getCurrentDirectory() + "/" + temp);
        }
      }

      if( save_file== null || save_file.getName().equals(""))
      {
        JOptionPane.showMessageDialog(save_frame,
          "Please enter a valid file name",
          "ERROR",
          JOptionPane.ERROR_MESSAGE);
        return null;
      }
      else
        return save_file;
    }

    /**
     *  Closes a file.
     */
    private void closeFile(Object stream)
    {
      try
      {
        if(stream instanceof ObjectInputStream)
          ((ObjectInputStream)stream).close();
        else if (stream instanceof ObjectOutputStream)
          ((ObjectOutputStream)stream).close();
        save_frame.dispose();
      }
      catch(Exception e)
      {
        JOptionPane.showMessageDialog(save_frame,
          "Could not close the file.",
          "ERROR",
          JOptionPane.ERROR_MESSAGE);
        save_frame.dispose();
      }
    }

    /**
     *
     *  Write the Forms to a file, using the conc_forms Vector.
     *  The only things actually written are the Form's 
     *  IParameterGUI types, name, and value in XML format along with 
     *  XML tags for the Form index.
     *
     *  @param conc_forms The Vector of Forms to write to a file.
     *  @param file the File to write to.
     */
    private void writeForms(Vector conc_forms, File file)
    {
      StringBuffer s = new StringBuffer();
      Form f;
      String temp;
      IParameterGUI ipg;
      FileWriter fw = null;

      try
      {
        fw = new FileWriter(file);
        for(int i = 0; i < conc_forms.size(); i++)
        {
          s.append("<Form number=");
          s.append(i);
          s.append(">\n");

          f = (Form)conc_forms.elementAt(i);
          for(int j = 0; j < f.getNum_parameters(); j++)
          {
            ipg = (IParameterGUI)f.getParameter(j);
            s.append("<");
            s.append(ipg.getType());
            s.append(">\n");
            s.append("<Name>");
            s.append(ipg.getName());
            s.append("</Name>\n");
            s.append("<Value>");
            temp = ipg.getValue().toString();
            if((temp == null) || (temp.equals("")))
              s.append("emptyString");
            else
            {
              s.append(temp);
              s.append("");
            }
            s.append("</Value>\n");
            s.append("</");
            s.append(ipg.getType());
            s.append(">\n");
          }
          s.append("</Form>\n");
        }
        fw.write(s.toString());
      }
      catch(IOException e)
      {
        e.printStackTrace();
        JOptionPane.showMessageDialog(save_frame,
          "Error saving file.  Please rerun the wizard and try again.",
          "ERROR",
          JOptionPane.ERROR_MESSAGE); 
      }
      finally
      {
        if(fw != null)
        {
          try
          {
            fw.close();
            modified = false;
            
          }
          catch(IOException e)
          {
            //let it drop on the floor
          }
        }
      }
    }

    /**
     *  Loads Forms from a file.  It actually just loads the saved
     *  IParameterGUI values into the Wizard's Forms' parameters.
     */
    private void loadForms(File file)
    {
      char ca;
      StringBuffer s = new StringBuffer();
      int good = -1;
      FileReader fr = null;

      try
      {
        fr = new FileReader(file);

        good = fr.read();

        while(good >=0)
        {
          ca = (char)good;
          s.append(ca);
          good = fr.read();
        }

        convertXMLtoParameters(s);
      }
      catch(IOException e)
      {
        e.printStackTrace();
        JOptionPane.showMessageDialog(save_frame,
          "Error loading file.  Does the file match the Wizard?",
          "ERROR",
          JOptionPane.ERROR_MESSAGE);
      }
      finally
      {
        if(fr != null)
        {
          try
          {
            fr.close();
            modified = false;
          }
          catch(IOException e)
          {
            //let it drop on the floor
          }
        }
      }
        
    }

    /**
     *  Converts a StringBuffer which holds an XML String of
     *  IParameterGUI and Form information into data that
     *  the Wizard can understand.  
     *
     *  @param s The StringBuffer that holds the parameter
     *           information.
     */
    private void convertXMLtoParameters(StringBuffer s)
    {
      String x = s.toString(), token_string;
      StringBuffer f = new StringBuffer(), temp = new StringBuffer();
      StringTokenizer st;
      int rightbracket, leftbracket, index, num_params;
      Form cur_form;
      IParameterGUI ipg;

      //trim up the newline characters
      st = new StringTokenizer(x, "\n");
      while(st.hasMoreTokens())
        temp.append(st.nextToken());
      x = temp.toString();

      //trim the string to remove all <> stuff
      rightbracket = x.indexOf('>');
      leftbracket = x.indexOf('<');

      while( (rightbracket >= 0) && (leftbracket >=0) )
      {
        if(leftbracket >= 1)
        {
          //found something useful.  Keep it.
          f.append((x.substring(0, leftbracket)).trim());
          f.append(";");
        }

        x = x.substring(rightbracket + 1, x.length());
        rightbracket = x.indexOf('>');
        leftbracket = x.indexOf('<');
      }
      x = f.toString();
      st = new StringTokenizer(x, ";");

      for( int j = 0; j < this.getNumForms(); j ++)
      {
        index = 0;
        cur_form = this.getForm(j);
        num_params = cur_form.getNum_parameters();
        while((index < num_params) && (st.hasMoreTokens()))
        {
          ipg = (IParameterGUI)(cur_form.getParameter(index));
          if(ipg.getName().equals(st.nextToken()));
          {
            token_string = st.nextToken();
            //catch the case where user entered no value
            if(!token_string.equals("emptyString"))
              if(!(ipg instanceof ArrayPG))  //don't enter a value for ArrayPGs
                ipg.setValue(token_string);
          }
          index++;
        }
      }
    }

    /**
     *  Load the state of the wizard from a file
     */
    public boolean load()
    {
      Vector loadedforms;
      File f;
      Form temp;
      f = getFile(false);

      if( f == null ) return false;

      loadForms(f);
      showForm(0);
      return true;
    }

    /**
     *  Save the state of the wizard to a file
     */
    public void save()
    {
      if(forms == null) return;

      File file;
      if(modified)
      {
        file = getFile(true);

        if( file == null ) return;

        writeForms(forms, file);
      }
    }

    /**
     *  Save the state of the wizard then exit the wizard application.
     */
    public void close()
    {
      int save_me = 1;
      if(modified)
      {
       save_me = JOptionPane.showConfirmDialog(null, 
                   "Would you like to save your changes?",
                   "Would you like to save your changes?",
                    JOptionPane.YES_NO_OPTION);
        if(save_me == 0)
          save();
      }
      System.exit(0);
    }

    public int getNumForms()
    {
      return forms.size();
    }

    protected void makeGUI(){
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.weightx   = 1.0;
        gbc.anchor    = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JPanel   work_area      = new JPanel( new GridBagLayout() );
        JPanel   button_panel   = new JPanel( new GridBagLayout() );
        JPanel   progress_panel = new JPanel( new GridBagLayout() );
        JMenuBar menu_bar       = new JMenuBar();
        JMenu    file_menu      = new JMenu("File");
        JMenu    help_menu      = new JMenu("Help");
                 view_menu      = new JMenu(VIEW_MENU);

        frame.setJMenuBar( menu_bar );
        frame.addWindowListener( new CloseWizardWindow() );
        frame.getContentPane().add( work_area );
        {
            int screenheight=(int)
                Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            frame.setBounds( (screenheight*4/3-FRAME_WIDTH)/2,
                             (screenheight-FRAME_HEIGHT)*3/10,
                             FRAME_WIDTH, FRAME_HEIGHT );
        }

        JMenuItem help_about  = new JMenuItem( HELP_ABOUT_COMMAND );
        JMenuItem wizard_help = new JMenuItem( WIZARD_HELP_COMMAND );
        JMenuItem form_help   = new JMenuItem( FORM_HELP_COMMAND );
        help_menu.add( help_about );
        help_menu.addSeparator();
        help_menu.add( wizard_help );
        help_menu.add( form_help );

        JScrollPane form_scrollpane=new JScrollPane(form_panel,
                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JMenuItem save_wizard = new JMenuItem( SAVE_WIZARD_COMMAND );
        JMenuItem load_wizard = new JMenuItem( LOAD_WIZARD_COMMAND );
        JMenuItem exit_item   = new JMenuItem( EXIT_COMMAND );
        save_wizard.setEnabled(true);
        load_wizard.setEnabled(true);
        file_menu.addSeparator();
        file_menu.add( save_wizard );
        file_menu.add( load_wizard );
        file_menu.addSeparator();
        file_menu.add( exit_item );

        menu_bar.add(file_menu);
        menu_bar.add(view_menu);
        menu_bar.add(help_menu);

        // add the title to the panel
        work_area.add( form_label,gbc );

        // add the form to the panel
        gbc.weighty=50.0;
        gbc.fill=GridBagConstraints.BOTH;
        form_panel.setLayout( new GridLayout(1,1) );
        work_area.add(form_scrollpane,gbc);

        // add the progress bar to the panel
        JButton clear_button = new JButton( CLEAR_COMMAND );
        exec_all_button=new JButton(EXEC_ALL_COMMAND);
        if(forms.size()>1){
            gbc.weighty=1.0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            work_area.add(progress_panel,gbc);
            progress.setString("Execute Progress");
            progress.setStringPainted(true);
            progress.setMaximum(forms.size());
            progress.setValue(0);
            gbc.fill=GridBagConstraints.NONE;
            gbc.gridwidth=1;
            progress_panel.add( clear_button, gbc );
            gbc.weightx=20.0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.gridwidth=GridBagConstraints.RELATIVE;
            progress_panel.add( progress,     gbc );
            gbc.weightx=1.0;
            gbc.fill=GridBagConstraints.NONE;
            gbc.gridwidth=GridBagConstraints.REMAINDER;
            gbc.anchor=GridBagConstraints.EAST;
            progress_panel.add( exec_all_button, gbc );
        }

        // add the navigation buttons to the panel
        gbc.weightx=1.0;
        gbc.anchor=GridBagConstraints.WEST;
        gbc.gridwidth=GridBagConstraints.REMAINDER;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        work_area.add(button_panel,gbc);
        first_button         = new JButton( FIRST_COMMAND );
        back_button          = new JButton( BACK_COMMAND  );
        next_button          = new JButton( NEXT_COMMAND  );
        last_button          = new JButton( LAST_COMMAND  );
        JButton exec_button  = new JButton( EXEC_COMMAND  );

        back_button.setEnabled(false);
        next_button.setEnabled(false);
        if(forms.size()>1){
            gbc.gridwidth=1;
            gbc.fill=GridBagConstraints.NONE;
            if(forms.size()>2){
                button_panel.add( first_button, gbc );
            }
            button_panel.add( back_button,  gbc );
            gbc.weightx=20;
            gbc.anchor=GridBagConstraints.CENTER;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            button_panel.add( exec_button,  gbc );
            gbc.weightx=1;
            gbc.anchor=GridBagConstraints.EAST;
            gbc.fill=GridBagConstraints.NONE;
            if(forms.size()>2){
                button_panel.add( next_button,  gbc );
                gbc.gridwidth=GridBagConstraints.REMAINDER;
                button_panel.add( last_button,  gbc );
            }else{
                gbc.gridwidth=GridBagConstraints.REMAINDER;
                button_panel.add( next_button,  gbc );
            }
        }else{
            gbc.anchor=GridBagConstraints.CENTER;
            gbc.gridwidth=GridBagConstraints.REMAINDER;
            button_panel.add( exec_button,  gbc );
        }


        // add the status to the panel
        gbc.fill=GridBagConstraints.BOTH;
        gbc.anchor=GridBagConstraints.WEST;
        gbc.weighty=5.0;
        if( standalone)
          work_area.add(SharedData.getStatusPane(), gbc);

        //CommandHandler command_handler = new CommandHandler(this);
        save_wizard    .addActionListener( command_handler );
        load_wizard    .addActionListener( command_handler );
        first_button   .addActionListener( command_handler );
        back_button    .addActionListener( command_handler );
        next_button    .addActionListener( command_handler );
        last_button    .addActionListener( command_handler );
        clear_button   .addActionListener( command_handler );
        exec_all_button.addActionListener( command_handler );
        exec_button    .addActionListener( command_handler );
        help_about     .addActionListener( command_handler );
        wizard_help    .addActionListener( command_handler );
        form_help      .addActionListener( command_handler );
        exit_item      .addActionListener( command_handler );
        view_menu      .addActionListener( command_handler );
    }

    protected void showGUI(){
        frame.show();
    }

    /**
     *  Add another form to the list of forms maintained by this
     *  wizard.
     *
     *  @param f  The form to be added to the list.
     */
    public void addForm( Form f ){
        forms.add( f );
        progress.setMaximum(forms.size());
    }

    /**
     *  Get the form that is currently displayed by the wizard.
     *
     *  @return  The currently displayed form.
     */
    public Form getCurrentForm(){
        return getForm(getCurrentFormNumber());
    }

    /**
     * Get the number of the form currently being shown.
     */
    public int getCurrentFormNumber(){
        return form_num;
    }

    /**
     * Get the form at specified index.
     *
     * @return The form at the specified index.
     */
    public Form getForm(int index){
        if( index>=0 && index<forms.size() ){
            return (Form)forms.elementAt(index);
        }else{
            return null;
        }
    }

    /**
     *  Show the form at the specified position in the list of forms.
     *  If the index is invalid, an error message will be displayed in
     *  the status pane.
     *
     *  @param  index  The index of the form to show.
     */
    public void showForm( int index ){
        if( !frame.isShowing()){
            this.makeGUI();
            this.showGUI();
        }

        if ( index < 0 || index > forms.size()-1 ){  // invalid index
            DataSetTools.util.SharedData.addmsg("Error: invalid form number in Wizard.show("
                                  + index + ")\n");
            return;
        }

        Form f = getCurrentForm();          // get rid of any current form
        if ( f != null ){
            f.setVisible(false);
        }

        form_panel.removeAll();

        f = getForm(index);           // show the specified form
        form_panel.add( f.getPanel() );
        f.setVisible(true);
        f.addParameterPropertyChangeListener(this);
        form_panel.validate();
        form_num = index;

        // enable/disable the navigation buttons
        if( index>=forms.size()-1 ){
            next_button.setEnabled(false);
        }else{
            next_button.setEnabled(true);
        }
        if( index>=forms.size()-2){
            last_button.setEnabled(false);
        }else{
            last_button.setEnabled(true);
        }
        if( index<=0 ){
            back_button.setEnabled(false);
        }else{
            back_button.setEnabled(true);
        }
        if(index<=1){
            first_button.setEnabled(false);
        }else{
            first_button.setEnabled(true);
        }

        if(forms.size()==1){
            form_label.setText(f.getTitle());
        }else{
            form_label.setText("Form "+(index+1)+": "+f.getTitle());
        }

    }

    /**
     *  Set the help message that will be displayed when the user
     *  requests help with this wizard.
     *
     *  @param help_message  String giving the help message to use for this
     *                       wizard.
     */
    public void setHelpMessage( String help_message ){
        this.help_message = help_message;
    }

    /**
     *  Get the help message for this wizard.
     *
     *  @return the String giving the help message for this wizard.
     */
    public String getHelpMessage(){
        return help_message;
    }

    /**
     *  Set the message that will be displayed when the user chooses
     *  the help about option.
     *
     *  @param about_message  String giving the message to use for the
     *                        "Help About" option.
     */
    public void setAboutMessage( String about_message ){
        this.about_message = about_message;
    }

    /**
     *  Get the help about message for this wizard.
     *
     *  @return the String giving the help about message for this wizard.
     */
    public String getAboutMessage(){
        return about_message;
    }

    /**
     *  Show the specified String in the help frame.
     *
     *  @param str   The message to display in a dialog.
     *  @param title The title of the dialog.
     */
    private void ShowHelpMessage( String str, String title ){
        JOptionPane.showMessageDialog(this.frame,str,title,
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *  Shows the JavaHelp HTML page for the current form.
     *
     */
    private void ShowFormHelpMessage(){
        HTMLizer form_htmlizer = new HTMLizer();
        String html = form_htmlizer.createHTML(this.getCurrentForm());
        JFrame help_frame = new JFrame(title);
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        help_frame.setSize(new Dimension((int)(screen_size.getWidth() / 2), 
                                         (int)(screen_size.getHeight() / 2)));
        help_frame.getContentPane().add(
          new JScrollPane(new JEditorPane("text/html", html)));
        help_frame.show();
    }

    /**
     * Invalidate all forms starting with the number specified.
     */
    protected void invalidate(int start){
        for( int i=start ; i<forms.size() ; i++ ){
          getForm(i).invalidate();
        }
        if(progress.getValue()>start)
          progress.setValue(start);
    }

    /**
     *  This class closes down the application when the user closes
     *  the frame.
     */
    private class CloseWizardWindow extends WindowAdapter{
        public void windowClosing( WindowEvent event ){
            close();
        }
    }

    /**
     * Execute all forms up to the number specified.
     */
    protected void exec_forms(int end){
      modified = true;
      Form f = getCurrentForm();
      // execute the previous forms
      for( int i=0 ; i<=end ; i++ ){
        progress.setValue(i);
        f=getForm(i);
        if(!f.done()){
          if(DEBUG) System.out.print("EXECUTING "+i);
          Object worked=f.getResult();
          if(DEBUG) System.out.println("  W="+worked+" D="+f.done());
          if( (worked instanceof ErrorString) ||  
              (worked instanceof Boolean && (!((Boolean)worked).booleanValue())) ){
            if(DEBUG) System.out.println("BREAKING "+i);
            end=i-1;
            break;
          }
        }
        progress.setValue(end+1);
      }

      // invalidate subsequent forms
      invalidate(end+1);
    }

    /**
     *
     * Creates the view menu and listeners for all of the currently
     * validated parameters in the current Form.  
     */
    private void populateViewMenu()
    {
      JMenuItem jmi;
      Form f;
      IParameterGUI iparam;
      Object val;
      
      f = this.getCurrentForm();

      view_menu.removeAll();
      for( int i = 0; i < f.getNum_parameters(); i++ )
      {
        iparam = (IParameterGUI)f.getParameter(i);
        if( iparam.getValid() )
        {
          val = iparam.getValue();
          /*semi-sophisticated attempt at being able to view
          DataSets, Vectors of items, and files.  Things like 
          Strings and ints, which are easily viewable on the
          Form, should not be sent to the ParameterViewer. */
          if( (iparam instanceof DataSetPG)  ||
              (iparam instanceof ArrayPG)    ||
              /*(iparam instanceof LoadFilePG) ||
              (iparam instanceof SaveFilePG) ||*/
              (iparam instanceof BrowsePG) )
          {
            jmi = new JMenuItem(iparam.getName());
            view_menu.add(jmi);
            jmi.addActionListener( command_handler );
          }
        }
      }

    }
    
    /**
     * Method to depopulate part of the view list if the 
     * parameters change.
     */
    public void propertyChange(PropertyChangeEvent ev){
        modified = true;
        this.invalidate(this.getCurrentFormNumber());
        this.populateViewMenu();
    }

    /**
     * Method to call a ParameterViewer.  Since the only "oddball" events
     * that currently happen are for the view menu, the only commands to 
     * listen for are the ones for the current form.
     */
     private void displayParameterViewer(String com)
     {
       Form f;
       IParameterGUI iparam;
       boolean done;
       int index, num_params;

       f = this.getCurrentForm();
       done = false;
       index = 0;
       num_params = f.getNum_parameters();
       
       while( !done && index < num_params )
       {
         iparam = (IParameterGUI)f.getParameter(index);
         //does the command match up to a current form parameter name?
         done = com.equals(iparam.getName());
         if( done )
           new ParameterViewer(iparam).showParameterViewer();
         index++;
       }  
    }

    
    /* ---------------- Internal Event Handler Classes --------------------- */
    /**
     *  This class handles all of the commands from buttons and menu
     *  items.
     */
    private class CommandHandler implements ActionListener{
        private Wizard wizard;
        public CommandHandler(Wizard wiz){
            this.wizard=wiz;
        }
        public void actionPerformed( ActionEvent event ){
            String command = event.getActionCommand();

            if ( command.equals( FIRST_COMMAND) ){
                form_num=0;
                showForm(form_num);
            }else if ( command.equals( BACK_COMMAND ) ){
                if ( form_num-1 >= 0 ){
                    form_num--;
                    showForm(form_num);
                    populateViewMenu();
                }else{
                    DataSetTools.util.SharedData.addmsg( "FORM 0 SHOWN, CAN'T STEP BACK\n" );
                }
            }else if ( command.equals( NEXT_COMMAND ) ){
                if ( form_num+1 < forms.size() ){
                    form_num++;
                    showForm(form_num);
                    populateViewMenu();
                }else{
                    DataSetTools.util.SharedData.addmsg( "NO MORE FORMS, CAN'T ADVANCE\n" );
                }
            }else if ( command.equals( LAST_COMMAND ) ){
                form_num=forms.size()-1;
                showForm(form_num);
            }else if ( command.equals( CLEAR_COMMAND ) ){
                invalidate(0);
            }else if ( command.equals( EXEC_ALL_COMMAND) ){
                exec_forms(forms.size()-1);
                populateViewMenu();                
            }else if ( command.equals( EXEC_COMMAND ) ){
                exec_forms(form_num);
                populateViewMenu();
            }else if ( command.equals( HELP_ABOUT_COMMAND ) ){
                ShowHelpMessage( about_message, "About: "+wizard.title );
            }else if ( command.equals( WIZARD_HELP_COMMAND ) ){
                ShowHelpMessage( help_message, "Help: "+wizard.title );
            }else if ( command.equals( FORM_HELP_COMMAND ) ){
                Form f = getCurrentForm();
                if ( f != null )
                    ShowFormHelpMessage();
            }else if ( command.equals( SAVE_WIZARD_COMMAND ) ){
                save();
            }else if ( command.equals( LOAD_WIZARD_COMMAND ) ){
                load();
            }else if ( command.equals( VIEW_MENU ) ){
                populateViewMenu();
            }else if ( command.equals( EXIT_COMMAND ) ){
                close();
            }else{
              displayParameterViewer(command);
            }
        }

    }
}
