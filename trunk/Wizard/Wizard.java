/*
 * File:  Wizard.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
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
 *
 *
 */

package Wizard;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 *  The Wizard class provides the top level control for a sequence of 
 *  operations to be carried out with user interaction.  The quantities
 *  the the user interacts with are stored in a master list of all quantities 
 *  used in the Wizard.  The quantites are stored as WizardParameter objects.
 *  The operations that are to be carried out are described by Form objects.
 *  The Wizard also controls a sequence of forms and allows the user to
 *  step back and forth between forms.  Each form defines an execute() method
 *  to carry out the action determined by the form.  The Wizard also manages
 *  a simple help system.  Help messages should be set for each form that is
 *  used in a particular Wizard application, as well as for the Wizard itself.
 *
 *  @see Form
 *  @see WizardParameter
 */

public class Wizard implements Serializable{
    // size of the window
    private static final int FRAME_WIDTH   = 600;
    private static final int FRAME_HEIGHT  = 500;
    
    // string constants for the menus and buttons
    private static final String EXIT_COMMAND        = "Exit";
    private static final String FIRST_COMMAND       = "First";
    private static final String BACK_COMMAND        = "Back";
    private static final String NEXT_COMMAND        = "Next";
    private static final String LAST_COMMAND        = "Last";
    private static final String CLEAR_COMMAND       = "Clear All";
    private static final String EXEC_COMMAND        = "Execute";
    private static final String HELP_ABOUT_COMMAND  = "About";
    private static final String WIZARD_HELP_COMMAND = "on Wizard";
    private static final String FORM_HELP_COMMAND   = "on Current Form";
    private static final String SAVE_FORM_COMMAND   = "Save Current Form";
    private static final String LOAD_FORM_COMMAND   = "Load Current Form";
    private static final String SAVE_WIZARD_COMMAND = "Save Wizard";
    private static final String LOAD_WIZARD_COMMAND = "Load Wizard";

    // default help and about messages
    private String help_message  = "Help not available for Wizard";
    private String about_message = "Default Help About Message";
    private boolean standalone   = true;
    
    // the status pane
    public static TextArea status_display = new TextArea();

    // instance variables
    private JFrame       frame;
    private String       title;
    private Hashtable    master_list;
    private Vector       forms;
    private int          form_num;
    private JPanel       form_panel;
    private JButton      first_button;
    private JButton      back_button;
    private JButton      next_button;
    private JButton      last_button;
    private JLabel       form_label;
    private JProgressBar progress;
    
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
        status_display = new TextArea();
        this.title  = title;
        master_list = new Hashtable();
        forms       = new Vector();
        form_num    = -1;
        form_panel  = new JPanel();
        form_label = new JLabel(" ",SwingConstants.CENTER);
        
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

        frame = new JFrame( title );
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
        
        JMenuItem save_form   = new JMenuItem( SAVE_FORM_COMMAND ); 
        JMenuItem load_form   = new JMenuItem( LOAD_FORM_COMMAND ); 
        JMenuItem save_wizard = new JMenuItem( SAVE_WIZARD_COMMAND ); 
        JMenuItem load_wizard = new JMenuItem( LOAD_WIZARD_COMMAND ); 
        JMenuItem exit_item   = new JMenuItem( EXIT_COMMAND ); 
        save_form.setEnabled(false);
        load_form.setEnabled(false);
        save_wizard.setEnabled(false);
        load_wizard.setEnabled(false);
        file_menu.add( save_form );
        file_menu.add( load_form );
        file_menu.addSeparator();
        file_menu.add( save_wizard );
        file_menu.add( load_wizard );
        file_menu.addSeparator();
        file_menu.add( exit_item );
        menu_bar.add(file_menu);
        menu_bar.add(help_menu);
        
        // add the title to the panel
        work_area.add( form_label,gbc );

        // add the form to the panel
        gbc.weighty=50.0;
        gbc.fill=GridBagConstraints.BOTH;
        form_panel.setLayout( new GridLayout(1,1) );
        work_area.add(form_scrollpane,gbc);
        
        // add the progress bar to the panel
        gbc.weighty=1.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        work_area.add(progress_panel,gbc);
        JButton clear_button = new JButton( CLEAR_COMMAND );
        progress = new JProgressBar();
        progress.setString("Execute Progress");
        progress.setStringPainted(true);
        progress.setMaximum(2);
        progress.setValue(0);
        gbc.fill=GridBagConstraints.NONE;
        gbc.gridwidth=GridBagConstraints.RELATIVE;
        progress_panel.add( clear_button, gbc );
        gbc.weightx=20.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.gridwidth=GridBagConstraints.REMAINDER;
        progress_panel.add( progress,     gbc );
        
        // add the navigation buttons to the panel
        gbc.weightx=1.0;
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
        gbc.gridwidth=1;
        gbc.fill=GridBagConstraints.NONE;
        button_panel.add( first_button, gbc );
        button_panel.add( back_button,  gbc );
        gbc.weightx=20;
        gbc.anchor=GridBagConstraints.CENTER;
        button_panel.add( exec_button,  gbc );
        gbc.weightx=1;
        gbc.anchor=GridBagConstraints.EAST;
        button_panel.add( next_button,  gbc );
        gbc.gridwidth=GridBagConstraints.REMAINDER;
        button_panel.add( last_button,  gbc );

        // add the status to the panel
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.anchor=GridBagConstraints.WEST;
        work_area.add( status_display,gbc );
        
        CommandHandler command_handler = new CommandHandler(this);
        save_form   .addActionListener( command_handler );
        load_form   .addActionListener( command_handler );
        save_wizard .addActionListener( command_handler );
        load_wizard .addActionListener( command_handler );
        first_button.addActionListener( command_handler );
        back_button .addActionListener( command_handler );
        next_button .addActionListener( command_handler );
        last_button .addActionListener( command_handler );
        clear_button.addActionListener( command_handler );
        exec_button .addActionListener( command_handler );
        help_about  .addActionListener( command_handler );
        wizard_help .addActionListener( command_handler );
        form_help   .addActionListener( command_handler );
        exit_item   .addActionListener( command_handler );
        
        frame.show();
    }
    
    
    /**
     *  This method adds a new parameter to the master list of
     *  parameters maintained by the Wizard.
     *
     *  @param  name   A descriptive name for this parameter.  
     *  @param  param  The parameter to be added to the master list
     */
    public void setParameter( String name, WizardParameter param ){
        master_list.put( name, param );
    }
    
    
    /**
     *  This method gets a parameter from the master list of
     *  parameters maintained by the Wizard.
     *
     *  @param  name   the name of the paramter to be retrieved from the 
     *                 master list.
     *  @return param  The parameter from the master list that is identified 
     *                 by the given name.  If there is no such parameter, this
     *                 returns null. 
     */ 
    public WizardParameter getParameter( String name ){
        Object obj = master_list.get( name );
        if ( obj == null || !(obj instanceof WizardParameter )){
            status_display.append("name not found in Wizard.getParameter()"
                                  +name+"\n");
            return null;
        }else{
            return (WizardParameter)obj; 
        }
    }

    /**
     *  Add another form to the list of forms maintained by this
     *  wizard.
     * 
     *  @param f  The form to be added to the list.
     */
    public void add( Form f ){
        forms.addElement( f );
        progress.setMaximum(forms.size());
    }
    
    /**
     *  Get the form that is currently displayed by the wizard.
     * 
     *  @return  The currently displayed form.
     */ 
    public Form getCurrentForm(){
        return getForm(form_num);
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
    public void show( int index ){
        if ( index < 0 || index > forms.size()-1 ){  // invalid index
            status_display.append("Error: invalid form number in Wizard.show()"
                                  + index + "\n");
            return;
        }
        
        Form f = getCurrentForm();          // get rid of any current form
        if ( f != null ){
            //form_panel.remove(f.getPanel());
            f.hide();
        }
        
        form_panel.removeAll();
        
        f = getForm(index);           // show the specified form
        form_panel.add( f.getPanel() );
        f.show();
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
        
        form_label.setText("Form "+(index+1)+": "+f.getTitle());
    }
    
    
    /**
     *  Save the state of the wizard then exit the wizard application.
     */
    public void close(){
        status_display.append("close(): Not Fully Implemented\n");
        save();
        frame.dispose();
        if(standalone) System.exit(0); // this should only be done if this is
                                       // running as a stand alone application
    }
    
    
    /**
     *  Save the state of the wizard to a file
     */
    public void save(){
        status_display.append("Wizard State save() Not Implemented\n");
    }

    /**
     *  Load the state of the wizard from a file
     */ 
    public boolean load(){
        status_display.append("Wizard State load() Not Implemented\n");
        return false;
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

    /* ---------------- Internal Event Handler Classes --------------------- */
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
     *  This class handles all of the commands from buttons and menu
     *  items.
     */
    private class CommandHandler implements ActionListener{
        private Wizard wizard;
        private CommandHandler(Wizard wiz){
            this.wizard=wiz;
        }
        public void actionPerformed( ActionEvent event ){
            String command = event.getActionCommand();
            
            if ( command.equals( FIRST_COMMAND) ){
                form_num=0;
                show(form_num);
            }else if ( command.equals( BACK_COMMAND ) ){
                if ( form_num-1 >= 0 ){
                    form_num--;
                    show(form_num);
                }else{
                    status_display.append( "FORM 0 SHOWN, CAN'T STEP BACK\n" );
                }
            }else if ( command.equals( NEXT_COMMAND ) ){
                if ( form_num+1 < forms.size() ){
                    form_num++;
                    show(form_num);
                }else{
                    status_display.append( "NO MORE FORMS, CAN'T ADVANCE\n" );
                }
            }else if ( command.equals( LAST_COMMAND ) ){
                form_num=forms.size()-1;
                show(form_num);
            }else if ( command.equals( CLEAR_COMMAND ) ){
                invalidate(0);
                progress.setValue(0);
            }else if ( command.equals( EXEC_COMMAND ) ){
                Form f = getCurrentForm();
                // execute the previous forms
                for( int i=0 ; i<=form_num ; i++ ){
                    progress.setValue(i);
                    f=getForm(i);
                    if(!f.done()){ 
                        f.setCompleted(f.execute());
                        if(!f.done()) break;
                    }
                    progress.setValue(i+1);
                }
                
                // invalidate subsequent forms
                invalidate(form_num+1);
            }else if ( command.equals( HELP_ABOUT_COMMAND ) ){
                ShowHelpMessage( about_message, "About: "+wizard.title );
            }else if ( command.equals( WIZARD_HELP_COMMAND ) ){
                ShowHelpMessage( help_message, "Help: "+wizard.title );
            }else if ( command.equals( FORM_HELP_COMMAND ) ){
                Form f = getCurrentForm();
                if ( f != null )
                    ShowHelpMessage(f.getHelpMessage(),"Help: "+f.getTitle());
            }else if ( command.equals( SAVE_WIZARD_COMMAND ) ){
                save();
            }else if ( command.equals( LOAD_WIZARD_COMMAND ) ){
                load();
            }else if ( command.equals( SAVE_FORM_COMMAND ) ){
                Form f = getCurrentForm();
                if ( f != null )
                    f.save();
            }else if ( command.equals( LOAD_FORM_COMMAND ) ){
                Form f = getCurrentForm();
                if ( f != null )
                    f.load();
            }else if ( command.equals( EXIT_COMMAND ) ){
                close();
            }
        } 
        /**
         * Invalidate all forms starting with the number specified.
         */
        private void invalidate(int start){
            for( int i=start ; i<forms.size() ; i++ ){
                getForm(i).setCompleted(false);
            }
        }

    }
    
    /**
     *  Main program for preliminary testing only.
     */
    public static void main( String args[] ){
        System.out.println("Wizard Main");
        
        Wizard w = new Wizard( "Wizard Test" ); 
        
        w.setParameter( "Height", 
                  new WizardParameter( "Height_Name", new Float(5.0), true ));
        w.setParameter( "Name", 
                  new WizardParameter( "Name_Name","Dennis", false ));
        
        Parameter p = w.getParameter( "Height" );
        System.out.println("P1=" + p.getName() + ", " + p.getValue() );
        
        p = w.getParameter( "Name" );
        System.out.println("P2=" + p.getName() + ", " + p.getValue() );
        
        w.show(0);
    }
}
