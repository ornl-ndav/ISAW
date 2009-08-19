package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.SlicePlane3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class SlicePlaneInformationCmd
{
   private int          sliceMode;
   private SlicePlane3D slicePlane;
   private float        depth;
   private float        width;
   private float        height;
   private float        thickness;
   //private int          sliceNumber;
   
   public SlicePlaneInformationCmd(int sliceMode,
                                SlicePlane3D slicePlane,
                                Vector3D normal,
                                float depth,
                                float width,
                                float height,
                                float thickness)
                                //int sliceNumber)
   {
      this.sliceMode = sliceMode;
      this.slicePlane = slicePlane;
      this.depth = depth;
      this.width = width;
      this.height = height;
      this.thickness = thickness;
      //this.sliceNumber = sliceNumber;
   }

   public int getSliceMode()
   {
      return sliceMode;
   }

   public SlicePlane3D getSlicePlane()
   {
      return slicePlane;
   }

   public float getDepth()
   {
      return depth;
   }

   public float getWidth()
   {
      return width;
   }

   public float getHeight()
   {
      return height;
   }

   public float getThickness()
   {
      return thickness;
   }

   //public int getSliceNumber()
   //{
   //   return sliceNumber;
   //}
   
   public String toString()
   {
      return "\nSlice Mode: "  + getSliceMode()  +
             "\n"              + getSlicePlane() +
             "\nDepth: "       + getDepth()      +
             "\nWidth: "       + getWidth()      +
             "\nHeight: "      + getHeight()     +
             "\nThickness: "   + getThickness();//  + 
             //"\nSliceNumber: " + getSliceNumber();
   }
}
