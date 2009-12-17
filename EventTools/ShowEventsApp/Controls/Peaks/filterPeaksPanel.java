package EventTools.ShowEventsApp.Controls.Peaks;

import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;
import gov.anl.ipns.Parameters.IParameterGUI;
import gov.anl.ipns.Parameters.IntArrayPG;
import gov.anl.ipns.Util.Numeric.ClosedInterval;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import gov.anl.ipns.ViewTools.UI.TextRangeUI;

public class filterPeaksPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private MessageCenter message_center;
	
	private JTabbedPane tabPane;
	private TextRangeUI dIntervals;
	private TextRangeUI qIntervals;
	private IntArrayPG  detNumbers;
	private IntArrayPG  rowRange;
	private IntArrayPG  colRange;
	private JButton     indexedFile;
	private JTextField  indexed;
	private JCheckBox   notIndexed;
	private JButton     clearBtn;
	private JButton     applyBtn;
	private JButton     saveBtn;
	private int         OFFSETQ;
	private int         OFFSETD;
	
	public filterPeaksPanel(MessageCenter mc)
	{
		message_center = mc;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(buildPanel());
		this.add(buildButton());
	}
	
	private JPanel buildPanel()
	{
		JPanel panel = new JPanel(new GridLayout(1,1));
		tabPane = new JTabbedPane();
		tabPane.add("Detector", buildDetectorPanel());
		tabPane.add("Q-Values", buildQPanel());
		tabPane.add("D-Values", buildDPanel());
		tabPane.add("Indexed Peaks", buildIndexedPeaks());
		
		panel.add(tabPane);
		return panel;
	}
	
	private JPanel buildDetectorPanel()
	{
		JPanel panel = new JPanel(new GridLayout(3,1));
		
		JLabel detectorLbl = new JLabel("Discard by Det. Number(s)");
		detNumbers = new IntArrayPG(detectorLbl.getText(), 0);
		panel.add(((IParameterGUI)detNumbers).getGUIPanel(false));
		
		String row = "Discard by Row Number(s):";
		String col = "Discard by Col Number(s):";
		rowRange = new IntArrayPG(row, "0:0");
		colRange = new IntArrayPG(col, "0:0");
		
		panel.add(((IParameterGUI)rowRange).getGUIPanel(false));
		panel.add(((IParameterGUI)colRange).getGUIPanel(false));;
		
		return panel;
	}
	
	private JPanel buildQPanel()
	{
		JPanel panel = new JPanel(new GridLayout(1,1));
		
		String qInt = "Discard by q-value Range:";
		OFFSETQ = qInt.length();
		qIntervals = new TextRangeUI(qInt, 0.1f, 1.5f);
		qIntervals.setTextFont(FontUtil.LABEL_FONT2);

		panel.add(qIntervals);
		return panel;
	}
	
	private JPanel buildDPanel()
	{
		JPanel panel = new JPanel(new GridLayout(1,1));
		
		String dInt = "Discard by d-value Range:";
		OFFSETD = dInt.length();
		dIntervals = new TextRangeUI(dInt, 0.3f, 1.7f);
		dIntervals.setTextFont(FontUtil.LABEL_FONT2);
		
		panel.add(dIntervals);
		return panel;
	}
	
	private JPanel buildIndexedPeaks()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2,2));
		
		indexedFile = new JButton("Discard by Indexed Peaks...");
		indexedFile.addActionListener(new buttonListener());
		indexed = new JTextField();
		indexed.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel nonIndexedLbl = new JLabel("Discard by Non-Indexed Peaks");
		notIndexed = new JCheckBox();
		notIndexed.setHorizontalAlignment(JCheckBox.CENTER);
		
		panel.add(indexedFile);
		panel.add(indexed);
		panel.add(nonIndexedLbl);
		panel.add(notIndexed);
		return panel;
	}
	
	private JPanel buildButton()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,3));
		
		clearBtn = new JButton("Clear");
		clearBtn.addActionListener(new buttonListener());
		
		applyBtn = new JButton("Apply");
		applyBtn.addActionListener(new buttonListener());
		
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(new buttonListener());
		
		buttonPanel.add(clearBtn);
		buttonPanel.add(applyBtn);
		buttonPanel.add(saveBtn);
		return buttonPanel;
	}
	
	private class buttonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == clearBtn)
			{
				ResetFields();
			}
			
			if(e.getSource() == applyBtn)
			{
				DetectorFilterCmd detCmd 
				    = new DetectorFilterCmd(buildDetVec(),
				                            buildRowVec(),
				                            buildColVec());
				
				QDFilterCmd qdCmd
				    = new QDFilterCmd(buildQVec(), buildDVec());
				
				IndexedPeaksFilterCmd indCmd =
				    new IndexedPeaksFilterCmd(indexed.getText(),
				                            notIndexed.isSelected());
				
				sendMessage(Commands.FILTER_DETECTOR, detCmd);
				sendMessage(Commands.FILTER_QD, qdCmd);
				sendMessage(Commands.FILTER_PEAKS, indCmd);
			}
			
			if(e.getSource() == saveBtn)
			{
	            /*final JFileChooser fc = new JFileChooser();
	            File file = null;
	            int returnVal = fc.showSaveDialog(null);
	            
	            if (returnVal == JFileChooser.APPROVE_OPTION) 
	            {
	               file = fc.getSelectedFile();
	               if (!file.exists())
	               {
	                  String error = "File does not exist!";
	                  JOptionPane.showMessageDialog(null, error, "Invalid Input",
	                                                JOptionPane.ERROR_MESSAGE);
	                  return;
	               }
	            } 
	            /*else if (returnVal == JFileChooser.CANCEL_OPTION)
	            {
	               //System.out.println("Open command cancelled by user.");
	               return;
	            }
	            else if (returnVal == JFileChooser.ERROR_OPTION)
	            {
	               JOptionPane.showMessageDialog( null, 
	                                             "Error opening file", 
	                                             "Error Opening File!", 
	                                              JOptionPane.ERROR_MESSAGE);
	               return;
	            }*/
			}
		}
	}
	
   /**
    * Sends a message to the messagecenter
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true );
      
      message_center.send( message );
   }
	
	private Vector buildDetVec()
	{
	    Vector vec = new Vector();
	    
	    int[] values = detNumbers.getArrayValue();
        
        for(int i = 0; i < values.length; i++)
        {
            vec.add(values[i]);
        }
	        
	    return vec;
	}
	
	private Vector buildRowVec()
	{
	    Vector vec = new Vector();
        
	    int[] values = rowRange.getArrayValue();
	    
        for(int i = 0; i < values.length; i++)
        {
            vec.add(values[i]);
        }
        
        return vec;
	}
	
	private Vector buildColVec()
    {
        Vector vec = new Vector();
        
        int[] values = colRange.getArrayValue();
        
        for(int i = 0; i < values.length; i++)
        {
            vec.add(values[i]);
        }
        
        return vec;
    }
	
   private Vector<ClosedInterval> buildQVec()
    {
        Vector<ClosedInterval> vec = new Vector<ClosedInterval>();
        
        try
        {
            int length = qIntervals.getText().length();
           
            StringTokenizer st
                = new StringTokenizer(
                        qIntervals.getText(OFFSETQ, (length - OFFSETQ)), ",");
            
            while(st.hasMoreTokens())
            {
                String[] tempInput = st.nextToken().split("[:]");
                for (int x=0; x<tempInput.length; x++)
                {
                    tempInput[x] = tempInput[x].replace('[', ' ');
                    tempInput[x] = tempInput[x].replace(']', ' ');
                    tempInput[x] = tempInput[x].replace(';', ' ');
                }
                
                float a = Float.parseFloat(tempInput[0]);
                float b = Float.parseFloat(tempInput[1]);
                vec.add(new ClosedInterval(a,b));
            }   
        } 
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        
        return vec;
    }
   
    private Vector<ClosedInterval> buildDVec()
    {
        Vector<ClosedInterval> vec = new Vector<ClosedInterval>();
        
        try
        {
            int length = dIntervals.getText().length();
            StringTokenizer st
                = new StringTokenizer(
                        dIntervals.getText(OFFSETD, (length - OFFSETD)), ",");
            
            while(st.hasMoreTokens())
            {
                String[] tempInput = st.nextToken().split("[:]");
                for (int x=0; x<tempInput.length; x++)
                {
                    tempInput[x] = tempInput[x].replace('[', ' ');
                    tempInput[x] = tempInput[x].replace(']', ' ');
                    tempInput[x] = tempInput[x].replace(';', ' ');
                }
                
                float a = Float.parseFloat(tempInput[0]);
                float b = Float.parseFloat(tempInput[1]);
                vec.add(new ClosedInterval(a,b));
            }  
        } 
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        
        return vec;
    }
    
    private void ResetFields()
    {
        detNumbers.setValue("0");
        rowRange.setValue("0:0");
        colRange.setValue("0:0");
        dIntervals.setMin(0.0f);
        dIntervals.setMax(1.0f);
        qIntervals.setMin(0.0f);
        qIntervals.setMax(1.0f);
        indexed.setText("");
    }
    
	public static void main(String[] args) 
	{
	      MessageCenter mc = new MessageCenter("Test Peak Filters");
	      TestReceiver tr = new TestReceiver("Testing Peak Filters");
	      
	      mc.addReceiver(tr, Commands.FILTER_DETECTOR);
	      mc.addReceiver(tr, Commands.FILTER_QD);
	      mc.addReceiver(tr, Commands.FILTER_PEAKS);
	      
	      filterPeaksPanel fp = new filterPeaksPanel(mc);
	      
	      JFrame View = new JFrame("Test Peak Options");
	      View.setBounds(10, 10, 300, 300);
	      View.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	      View.setVisible(true);
	      
	      View.add(fp);
	      
	      new UpdateManager(mc, null, 100);
	}
}
