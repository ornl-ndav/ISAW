package EventTools.ShowEventsApp.Controls;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;


public class displayColorEditor 
{
  private JFrame         cEditPanel;
  private ColorEditPanel  colorEditPanel;
  private Rectangle       bounds          = new Rectangle(100,100,400,500);
  private MessageCenter  message_center;
  private String         command;
  
  public displayColorEditor(MessageCenter inMessage_Center,
                  String inCommand, int min, int max, 
                  boolean addListener)
  {
    message_center = inMessage_Center;
    command = inCommand;
    
    colorEditPanel = new ColorEditPanel(min, max, false, false);
    
    byte[] table = new byte[256];
    for(int i = 0; i < table.length-2; i++)
       table[i] = (byte)(i/2);
    
    colorEditPanel.
       setControlValue(new ColorScaleInfo(min, max, 1, "Heat 1", 
                                        true, 127, table, true));
    if (addListener)
       colorEditPanel.addActionListener( new ColorListener() );
  }
  
  private displayColorEditor(MessageCenter inMessage_Center,
      String inCommand)
  {
    message_center = inMessage_Center;
    command = inCommand;
  }

  public float getDataMin()
  {
     return colorEditPanel.getMin();
  }
  
  public float getDataMax()
  {
     return colorEditPanel.getMax();
  }
    
  public ColorScaleInfo getColorScaleInfo()
  {
     return (ColorScaleInfo)colorEditPanel.getControlValue();
  }   
  
  public JPanel getColorPanel()
  {
     return colorEditPanel;
  }
  
  public void setColorScale(float min, float max)
  {
     byte[] table = new byte[256];
      for(int i = 0; i < table.length-2; i++)
         table[i] = (byte)(i/2);
      
     colorEditPanel.
      setControlValue(new ColorScaleInfo(min, max, 1, "Heat 1", 
                                       true, 127, table, true));
  }
  
  private class ColorListener implements ActionListener
  {
    public void actionPerformed (ActionEvent ae)
    {      
      if( ae.getActionCommand().equals(ColorEditPanel.cancelMessage))
      {
         if (cEditPanel != null)
            cEditPanel.dispose();
      }
      
      if( ae.getActionCommand().equals(ColorEditPanel.doneMessage))
      {
        sendMessage(command, getColorScaleInfo());
        if (cEditPanel != null)
           cEditPanel.dispose();
      }
      
      if (ae.getActionCommand().equals(ColorEditPanel.updateMessage))
      {
        sendMessage(command, getColorScaleInfo());
      }
    }
  }
  
  private void sendMessage(String command, Object value)
  {
     Message message = new Message( command,
                                    value,
                                    false );
     
     message_center.receive( message );
  }
  
  public void createColorEditor()
  {
    cEditPanel = new JFrame("Color Editor");
    cEditPanel.setBounds(bounds);
    cEditPanel.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    colorEditPanel.addActionListener( new ColorListener() );
    cEditPanel.getContentPane().add(colorEditPanel);
    cEditPanel.setVisible(true);
  }
  
  public static void main(String[] args) 
  {
    MessageCenter messageC = new MessageCenter("ColorEditor Center");
    TestReceiver tr = new TestReceiver("Test ColorEditor");
    messageC.addReceiver(tr, Commands.SET_COLOR_SCALE);
    
    displayColorEditor display = new displayColorEditor(messageC, 
                    Commands.SET_COLOR_SCALE, 15, 1000, false);
    display.createColorEditor();
    
    new UpdateManager(messageC, null, 100);
  }

}
