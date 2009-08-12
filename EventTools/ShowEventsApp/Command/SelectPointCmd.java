package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class SelectPointCmd
{
   private float qx;
   private float qy;
   private float qz;
   private float dx;
   private float dy;
   private float dz;
   
   public SelectPointCmd(float qx, float qy, float qz,
                         float dx, float dy, float dz)
   {
      this.qx = qz;
      this.qy = qy;
      this.qz = qz;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
   }

   public SelectPointCmd(Vector3D Q, Vector3D D)
   {
      this.qx = Q.getX();
      this.qy = Q.getY();
      this.qz = Q.getZ();
      this.dx = D.getX();
      this.dy = D.getY();
      this.dz = D.getZ();
   }
   
   public float getQx()
   {
      return qx;
   }

   public float getQy()
   {
      return qy;
   }

   public float getQz()
   {
      return qz;
   }

   public float getDx()
   {
      return dx;
   }

   public float getDy()
   {
      return dy;
   }

   public float getDz()
   {
      return dz;
   }
   
   public String toString()
   {
      return "\nQ(x,y,z) (" + qx + ", " + qy + ", " + qz + ")" +
             "\nD(x,y,z) (" + dx + ", " + dy + ", " + dz + ")";
   }
}
