/*
 * File:  IAttributeList.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 *  $Log$
 *  Revision 1.10  2004/05/25 20:17:02  kramer
 *  Added methods that are used to get attribute values.
 *
 *  Revision 1.9  2004/03/15 06:10:37  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.8  2003/02/10 13:28:36  dennis
 *  getAttributeList() now returns a reference to the attribute list,
 *  not a clone.
 *
 *  Revision 1.7  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.6  2002/07/10 16:02:25  pfpeterson
 *  Added removeAttribute() methods.
 *
 */

package  DataSetTools.dataset;

import DataSetTools.gsastools.GsasCalib;
import DataSetTools.instruments.SampleOrientation;
import gov.anl.ipns.MathTools.Geometry.DetectorPosition;

/**
 * IAttributeList provides the interface to get/set a List of attributes 
 * in an object.
 *
 * @see Data  
 * @see DataSet
 */  

public interface IAttributeList 
{

  /**
   *  Get a reference to the whole list of attributes for an object.
   */
  public AttributeList getAttributeList();



  /**
   *  Set the whole list of attributes for an object to be a COPY of the 
   *  specified list of attributes.
   */
  public void setAttributeList( AttributeList attr_list );


  /**
   * Gets the number of attributes set for the attribute list.
   */
  public int getNum_attributes();


  /**
   * Set the value of the specified attribute in the list of attributes.
   * If the attribute is already present in the list, the value is changed
   * to the value of the new attribute.  If the attribute is not already
   * present in the list, the new attribute is added to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   */
  public void setAttribute( Attribute attribute );
  

  /**
   * Set the value of the specified attribute in the list of attributes at
   * at the specified position.  If the attribute is already present in the
   * list, the value is changed to the value of the new attribute.  If the
   * attribute is not already present in the list, the new attribute is added
   * to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   *
   *  @param  index        The position where the attribute is to be
   *                       inserted.
   */
  public void setAttribute( Attribute attribute, int index );


  /**
   * Remove the attribute at the specified index from the list of
   * attributes. If the index is invalid, this does nothing.
   *
   * @param index The position of the attribute to remove.
   */
  public void removeAttribute( int index );

  /**
   * Remove the attribute with the specified name from the list of
   * attributes. If the named attribute is not in the list, this does
   * nothing.
   *
   * @param name The name of the attribute to remove.
   */
  public void removeAttribute( String name );

  /**
   * Get the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
   */
  public Attribute getAttribute( int index );


  /**
   * Get the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this 
   * returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Attribute getAttribute( String name );


  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
   */
  public Object  getAttributeValue( int index );


  /**
   * Get the value of the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   */
  public Object getAttributeValue( String name );
  
  //These methods are used to get Attribute values.
  /**Specified by {@link AttributeList#getAttributeTitle() getAttributeTitle()} from {@link AttributeList AttributeList}*/
  public String getAttributeTitle();
  /**Specified by {@link AttributeList#getAttributeLabel() getAttributeLabel()} from {@link AttributeList AttributeList}*/
  public String getAttributeLabel();
  /**Specified by {@link AttributeList#getDS_TAG() getDS_TAG()} from {@link AttributeList AttributeList}*/
  public int getDS_TAG();
  /**Specified by {@link AttributeList#getUser() getUser()} from {@link AttributeList AttributeList}*/
  public String getUser();
  /**Specified by {@link AttributeList#getInstrumentName() getInstrumentName()} from {@link AttributeList AttributeList}*/
  public String getInstrumentName();
  /**Specified by {@link AttributeList#getInstrumentType() getInstrumentType()} from {@link AttributeList AttributeList}*/
  public int getInstrumentType();
  /**Specified by {@link AttributeList#getFileName() getFileName()} from {@link AttributeList AttributeList}*/
  public String getFileName();
  /**Specified by {@link AttributeList#getRunTitle() getRunTitle()} from {@link AttributeList AttributeList}*/
  public String getRunTitle();
  /**Specified by {@link AttributeList#getRunNumber() getRunNumber()} from {@link AttributeList AttributeList}*/
  public int[] getRunNumber();
  /**Specified by {@link AttributeList#getEndDate() getEndDate()} from {@link AttributeList AttributeList}*/
  public String getEndDate();
  /**Specified by {@link AttributeList#getEndTime() getEndTime()} from {@link AttributeList AttributeList}*/
  public String getEndTime();
  /**Specified by {@link AttributeList#getStartDate() getStartDate()} from {@link AttributeList AttributeList}*/
  public String getStartDate();
  /**Specified by {@link AttributeList#getStartTime() getStartTime()} from {@link AttributeList AttributeList}*/
  public String getStartTime();
  /**Specified by {@link AttributeList#getUpdateTime() getUpdateTime()} from {@link AttributeList AttributeList}*/
  public String getUpdateTime();
  /**Specified by {@link AttributeList#getDetectorPosition() getDetectorPosition()} from {@link AttributeList AttributeList}*/
  public DetectorPosition getDetectorPosition();
  /**Specified by {@link AttributeList#getRawAngle() getRawAngle()} from {@link AttributeList AttributeList}*/
  public float getRawAngle();
  /**Specified by {@link AttributeList#getRawDistance() getRawDistance()} from {@link AttributeList AttributeList}*/
  public float getRawDistance();
  /**Specified by {@link AttributeList#getSolidAngle() getSolidAngle()} from {@link AttributeList AttributeList}*/
  public float getSolidAngle();
  /**Specified by {@link AttributeList#getOmega() getOmega()} from {@link AttributeList AttributeList}*/
  public float getOmega();
  /**Specified by {@link AttributeList#getDelta2Theta() getDelta2Theta()} from {@link AttributeList AttributeList}*/
  public float getDelta2Theta();
  /**Specified by {@link AttributeList#getEfficiencyFactor() getEfficiencyFactor()} from {@link AttributeList AttributeList}*/
  public float getEfficiencyFactor();
  /**Specified by {@link AttributeList#getDetectorIDs() getDetectorIDs()} from {@link AttributeList AttributeList}*/
  public int[] getDetectorIDs();
  /**Specified by {@link AttributeList#getSegmentIDs() getSegmentIDs()} from {@link AttributeList AttributeList}*/
  public int[] getSegmentIDs();
  /**Specified by {@link AttributeList#getGroupID() getGroupID()} from {@link AttributeList AttributeList}*/
  public int getGroupID();
  /**Specified by {@link AttributeList#getTimeFieldType() getTimeFieldType()} from {@link AttributeList AttributeList}*/
  public int getTimeFieldType();
  /**Specified by {@link AttributeList#getCrate() getCrate()} from {@link AttributeList AttributeList}*/
  public int[] getCrate();
  /**Specified by {@link AttributeList#getSlot() getSlot()} from {@link AttributeList AttributeList}*/
  public int[] getSlot();
  /**Specified by {@link AttributeList#getInput() getInput()} from {@link AttributeList AttributeList}*/
  public int[] getInput();
  /**Specified by {@link AttributeList#getDetectorCENDistance() getDetectorCENDistance()} from {@link AttributeList AttributeList}*/
  public float getDetectorCENDistance();
  /**Specified by {@link AttributeList#getDetectorCENAngle() getDetectorCENAngle()} from {@link AttributeList AttributeList}*/
  public float getDetectorCENAngle();
  /**Specified by {@link AttributeList#getDetectorCENHeight() getDetectorCENHeight()} from {@link AttributeList AttributeList}*/
  public float getDetectorCENHeight();
  /**Specified by {@link AttributeList#getDetectorDataGrid() getDetectorDataGrid()} from {@link AttributeList AttributeList}*/
  public IDataGrid getDetectorDataGrid();
  /**Specified by {@link AttributeList#getInitialPath() getInitialPath()} from {@link AttributeList AttributeList}*/
  public float getInitialPath();
  /**Specified by {@link AttributeList#getEnergyIn() getEnergyIn()} from {@link AttributeList AttributeList}*/
  public float getEnergyIn();
  /**Specified by {@link AttributeList#getNominalEnergyIn() getNominalEnergyIn()} from {@link AttributeList AttributeList}*/
  public float getNominalEnergyIn();
  /**Specified by {@link AttributeList#getEnergyOut() getEnergyOut()} from {@link AttributeList AttributeList}*/
  public float getEnergyOut();
  /**Specified by {@link AttributeList#getNominalSourceToSampleTOF() getNominalSourceToSampleTOF()} from {@link AttributeList AttributeList}*/
  public float getNominalSourceToSampleTOF();
  /**Specified by {@link AttributeList#getSourceToSampleTOF() getSourceToSampleTOF()} from {@link AttributeList AttributeList}*/
  public float getSourceToSampleTOF();
  /**Specified by {@link AttributeList#getT0Shift() getT0Shift()} from {@link AttributeList AttributeList}*/
  public float getT0Shift();
  /**Specified by {@link AttributeList#getSampleChi() getSampleChi()} from {@link AttributeList AttributeList}*/
  public float getSampleChi();
  /**Specified by {@link AttributeList#getSamplePhi() getSamplePhi()} from {@link AttributeList AttributeList}*/
  public float getSamplePhi();
  /**Specified by {@link AttributeList#getSampleOmega() getSampleOmega()} from {@link AttributeList AttributeList}*/
  public float getSampleOmega();
  /**Specified by {@link AttributeList#getSampleOrientation() getSampleOrientation()} from {@link AttributeList AttributeList}*/
  public SampleOrientation getSampleOrientation();
  /**Specified by {@link AttributeList#getSampleName() getSampleName()} from {@link AttributeList AttributeList}*/
  public String getSampleName();
  /**Specified by {@link AttributeList#getTemperature() getTemperature()} from {@link AttributeList AttributeList}*/
  public float getTemperature();
  /**Specified by {@link AttributeList#getPressure() getPressure()} from {@link AttributeList AttributeList}*/
  public float getPressure();
  /**Specified by {@link AttributeList#getMagneticField() getMagneticField()} from {@link AttributeList AttributeList}*/
  public float[] getMagneticField();
  /**Specified by {@link AttributeList#getNumberOfPulses() getNumberOfPulses()} from {@link AttributeList AttributeList}*/
  public int getNumberOfPulses();
  /**Specified by {@link AttributeList#getTotalCount() getTotalCount()} from {@link AttributeList AttributeList}*/
  public float getTotalCount();
  /**Specified by {@link AttributeList#getQValue() getQValue()} from {@link AttributeList AttributeList}*/
  public float getQValue();
  /**Specified by {@link AttributeList#getGSASCalib() getGSASCalib()} from {@link AttributeList AttributeList}*/
  public GsasCalib getGSASCalib();
  /**Specified by {@link AttributeList#getGSASIParm() getGSASIParm()} from {@link AttributeList AttributeList}*/
  public String getGSASIParm();
  /**Specified by {@link AttributeList#getLatticeParam() getLatticeParam()} from {@link AttributeList AttributeList}*/
  public float[] getLatticeParam();
  /**Specified by {@link AttributeList#getOrientMatrix() getOrientMatrix()} from {@link AttributeList AttributeList}*/
  public String getOrientMatrix();
  /**Specified by {@link AttributeList#getOrientFile() getOrientFile()} from {@link AttributeList AttributeList}*/
  public String getOrientFile();
  /**Specified by {@link AttributeList#getCellVolume() getCellVolume()} from {@link AttributeList AttributeList}*/
  public float getCellVolume();
  /**Specified by {@link AttributeList#getSCDCalid() getSCDCalid()} from {@link AttributeList AttributeList}*/
  public String getSCDCalid();
  /**Specified by {@link AttributeList#getSCDCalibFile() getSCDCalibFile()} from {@link AttributeList AttributeList}*/
  public String getSCDCalibFile();
  /**Specified by {@link AttributeList#getPixelInfoList() getPixelInfoList()} from {@link AttributeList AttributeList}*/
  public PixelInfoList getPixelInfoList();
  /**Specified by {@link AttributeList#getDSType() getDSType()} from {@link AttributeList AttributeList}*/
  public String getDSType();
  /**Specified by {@link AttributeList#getTimeOffset() getTimeOffset()} from {@link AttributeList AttributeList}*/
  public float getTimeOffset();
  /**Specified by {@link AttributeList#getStartTimeSec() getStartTimeSec()} from {@link AttributeList AttributeList}*/
  public int getStartTimeSec();
  /**Specified by {@link AttributeList#getTimeOfDay() getTimeOfDay()} from {@link AttributeList AttributeList}*/
  public String getTimeOfDay();
  /**Specified by {@link AttributeList#getDayOfMonth() getDayOfMonth()} from {@link AttributeList AttributeList}*/
  public String getDayOfMonth();
  /**Specified by {@link AttributeList#getUnknown() getUnknown()} from {@link AttributeList AttributeList}*/
  public String getUnknown();
  /**Specified by {@link AttributeList#getInvalidDataSet() getInvalidDataSet()} from {@link AttributeList AttributeList}*/
  public String getInvalidDataSet();
  /**Specified by {@link AttributeList#getMonitorData() getMonitorData()} from {@link AttributeList AttributeList}*/
  public String getMonitorData();
  /**Specified by {@link AttributeList#getSampleData() getSampleData()} from {@link AttributeList AttributeList}*/
  public String getSampleData();
  /**Specified by {@link AttributeList#getPulseHeightData() getPulseHeightData()} from {@link AttributeList AttributeList}*/
  public String getPulseHeightData();
  /**Specified by {@link AttributeList#getTemperatureData() getTemperatureData()} from {@link AttributeList AttributeList}*/
  public String getTemperatureData();
  /**Specified by {@link AttributeList#getPressureData() getPressureData()} from {@link AttributeList AttributeList}*/
  public String getPressureData();
}
