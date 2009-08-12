package EventTools.ShowEventsApp;

import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.*;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.Color;
import java.awt.event.*;
import java.awt.GridLayout;

import MessageTools.*;

import EventTools.ShowEventsApp.Command.*;
import EventTools.ShowEventsApp.Controls.*;
import EventTools.ShowEventsApp.Controls.Peaks.*;

public class controlsPanel extends JPanel
{
   public static final long   serialVersionUID = 1L;
   private MessageCenter      messageCenter;
   
   private JButton            loadFileBtn;
   private JButton            findPeaksBtn;
   private JButton            filterPeaksBtn;
   private JButton            indexPeaksBtn;
   private JButton            integrateBtn;
   
   private JButton            selectedPoint;
   private JButton            orientationBtn;
   private JButton            colorScaleBtn;
   private JButton            planeBtn;
   private JButton            drawOptions;
   
   private filePanel          filepanel;
   private displayColorEditor colorEditPanel;
   private peakOptionsPanel   peakPanel;
   private indexPeaksPanel    indexPeakPanel;
   private positionInfoPanel  positionPanel;
   private sliceControl       slicePanel;
   private drawingOptions     drawoptions;
   
//   private final Color background_color = new Color( 220, 230, 235 );
   private final Color background_color = new Color( 230, 232, 250 );

   /**
    * Builds the side panel in the splitpane for all the controls.
    * Also builds the corresponding panels that are passed in the 
    * message system to the other panel to be displayed.
    * 
    * @param messageCenter
    */
   public controlsPanel(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      buildPanels();
      
      this.setLayout(new GridLayout(2,1));
      this.add(buildOperationsPanel());
      this.add(buildControlsPanel());
   }
   
   public ColorScaleInfo getColorScaleInfo()
   {
      return colorEditPanel.getColorScaleInfo();
   }
   
   
   private void buildPanels()
   {
      filepanel = new filePanel(messageCenter);
      peakPanel = new peakOptionsPanel(messageCenter);
      indexPeakPanel = new indexPeaksPanel(messageCenter);
      positionPanel = new positionInfoPanel();//messageCenter);
      slicePanel = new sliceControl(messageCenter);
      drawoptions = new drawingOptions(messageCenter);
      
      //Rectangle bounds = new Rectangle(100,100,400,500);
      colorEditPanel = new displayColorEditor(messageCenter,
            Commands.SET_COLOR_SCALE, 15, 1000, true);
      /*colorEditPanel.setBounds(bounds);
      
      byte[] table = new byte[256];
      for(int i = 0; i < table.length-2; i++)
         table[i] = (byte)(i/2);
      
      colorEditPanel.
         setControlValue(new ColorScaleInfo(15, 1000, 1, "Heat 1", 
                                          true, 127, table, true));*/
   }
   
   private JPanel buildOperationsPanel()
   {

      JPanel panel = new JPanel();
      panel.setBorder(new TitledBorder("Operations"));
      panel.setLayout(new GridLayout(5,1));
      
      loadFileBtn = new JButton("Load Files");
      loadFileBtn.setBackground( background_color );
      loadFileBtn.addActionListener(new buttonListener());
      
      findPeaksBtn = new JButton("Find Peaks");
      findPeaksBtn.setBackground( background_color );
      findPeaksBtn.addActionListener(new buttonListener());
      
      filterPeaksBtn = new JButton("Filter Peaks");
      filterPeaksBtn.setBackground( background_color );
      filterPeaksBtn.addActionListener(new buttonListener());
      
      indexPeaksBtn = new JButton("Index Peaks");
      indexPeaksBtn.setBackground( background_color );
      indexPeaksBtn.addActionListener(new buttonListener());
      
      integrateBtn = new JButton("Integrate");
      integrateBtn.setBackground( background_color );
      integrateBtn.addActionListener(new buttonListener());
      
      panel.add(loadFileBtn);
      panel.add(findPeaksBtn);
      panel.add(filterPeaksBtn);
      panel.add(indexPeaksBtn);
      panel.add(integrateBtn);
      
      return panel;
   }
   
   private JPanel buildControlsPanel()
   {
      JPanel panel = new JPanel();
      panel.setBorder(new TitledBorder("Controls/Info"));
      panel.setLayout(new GridLayout(5,1));
      
      selectedPoint = new JButton("Selected Point");
      selectedPoint.setBackground( background_color );
      selectedPoint.addActionListener(new buttonListener());
      
      orientationBtn = new JButton("Orientation Info.");
      orientationBtn.setBackground( background_color );
      orientationBtn.addActionListener(new buttonListener());
      
      colorScaleBtn = new JButton("Color Scale");
      colorScaleBtn.setBackground( background_color );
      colorScaleBtn.addActionListener(new buttonListener());
      
      planeBtn = new JButton("Plane/Histogram");
      planeBtn.setBackground( background_color );
      planeBtn.addActionListener(new buttonListener());
      
      drawOptions = new JButton("Draw Options");
      drawOptions.setBackground(background_color);
      drawOptions.addActionListener(new buttonListener());
      
      panel.add(selectedPoint);
      panel.add(orientationBtn);
      panel.add(colorScaleBtn);
      panel.add(planeBtn);
      panel.add(drawOptions);
      
      return panel;
   }
   
   private class buttonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         JPanel value = null;
         
         if (e.getSource().equals(loadFileBtn))
            value = filepanel.getPanel();
         
         if (e.getSource().equals(findPeaksBtn))
            value = peakPanel;
         
         if (e.getSource().equals(indexPeaksBtn))
            value = indexPeakPanel;
         
         if (e.getSource().equals(selectedPoint))
            value = positionPanel;

         if (e.getSource().equals(colorScaleBtn))
            value = colorEditPanel.getColorPanel();
         
         if (e.getSource().equals(planeBtn))
            value = slicePanel;
         
         if (e.getSource().equals(drawOptions))
            value = drawoptions;
         
         //if (value != null)
         sendMessage(Commands.CHANGE_PANEL, value);
      }
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command,
                                    value,
                                    true);
      
      messageCenter.receive(message);
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("controlsPanel TestingMessages");
      mc.addReceiver(tc, Commands.CHANGE_PANEL);
      
      controlsPanel op = new controlsPanel(mc);
      
      JFrame View = new JFrame( "Test Controls Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 300, 275);
      View.setVisible(true);
      
      View.add(op);
      new UpdateManager(mc, null, 100);
   }
}
