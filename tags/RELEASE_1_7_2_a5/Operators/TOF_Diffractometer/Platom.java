/*
 * File:  Platom.java
 *
 * Copyright (C) 2004 J. Tao
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
 * Contact : Julian Tao <taoj@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/computing/ISAW/>
 *
 *
 *
 * Modified:
 * $Log$
 * Revision 1.2  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.1  2004/07/23 17:43:37  taoj
 * test version.
 *
 */

package Operators.TOF_Diffractometer;

public class Platom {
  public static final float[] flux_paras0 = {3.49f, 0.09f, 7.96f, 2.74f, 0.f, 1.14f, 0.15f, 0.f, 0.f};
  //ISIS flux function: fepi, alpha, fmax, lambdat, cona, lambda1, lambda2, lambda3, lambda4
  public static final float[] flux_paras = {16.725f, 4.592f, 18.878f, 0.086f, 1.359f, 1.058f};
                                    //GLAD flux function: fmax, lambda0, fepi,      alpha, lambda1, s;
  public static final float NMASS = 1.00f;
  
  private static float fl1, fl2, bf1, bf2;
  
  
  private static void computePhi(float lambda){
    
    float fmax=flux_paras[0], lambda0=flux_paras[1], fepi=flux_paras[2], alpha=flux_paras[3], lambda1=flux_paras[4], s=flux_paras[5];
    double t1, t2, t3, t4, t5, t10, t16, t22, t23, t25, t29, t30, t31, t33, t36, t52;
    double t6, t9, t11, t17, t32, t34, t38, t39, t40, t41, t45, t47, t49, t59, t60, t81;
//    fl1=(float)(lambda * (-0.5e1 * fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.6e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + 0.2e1 * fmax * Math.pow(lambda0, 0.6e1) * Math.pow(lambda, -0.8e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) - fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) / (0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s))) * (double) (1 + 2 * alpha) / lambda - 0.2e1 * fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) * Math.pow(0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)), -0.2e1) * Math.pow(lambda / lambda1, (double) (2 * s)) * (double) s / lambda) / (fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.5e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) / (0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)))));
//    fl2 =(float)( lambda * lambda * (0.30e2 * fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.7e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) - 0.26e2 * fmax * Math.pow(lambda0, 0.6e1) * Math.pow(lambda, -0.9e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + 0.4e1 * fmax * Math.pow(lambda0, 0.8e1) * Math.pow(lambda, -0.11e2) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) / (0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s))) * (double) Math.pow((double) (1 + 2 * alpha), (double) 2) * Math.pow(lambda, -0.2e1) + 0.4e1 * fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) * Math.pow(0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)), -0.2e1) * (double) (1 + 2 * alpha) * Math.pow(lambda, -0.2e1) * Math.pow(lambda / lambda1, (double) (2 * s)) * (double) s + fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) / (0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s))) * (double) (1 + 2 * alpha) * Math.pow(lambda, -0.2e1) + 0.8e1 * fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) * Math.pow(0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)), -0.3e1) * Math.pow(Math.pow(lambda / lambda1, (double) (2 * s)), 0.2e1) * (double) (s * s) * Math.pow(lambda, -0.2e1) - 0.4e1 * fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) * Math.pow(0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)), -0.2e1) * Math.pow(lambda / lambda1, (double) (2 * s)) * (double) (s * s) * Math.pow(lambda, -0.2e1) + 0.2e1 * fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) * Math.pow(0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)), -0.2e1) * Math.pow(lambda / lambda1, (double) (2 * s)) * (double) s * Math.pow(lambda, -0.2e1)) / (fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.5e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + fepi / Math.pow(lambda, (double) (1 + 2 * alpha)) / (0.1e1 + Math.pow(lambda / lambda1, (double) (2 * s)))));

    t1 = lambda0 * lambda0;
    t2 = t1 * t1;
    t3 = fmax * t2;
    t4 = lambda * lambda;
    t5 = t4 * t4;
    t10 = Math.exp(-t1 / t4);
    t16 = t5 * t5;
    t22 = 1 + 2 * alpha;
    t23 = Math.pow(lambda, (double) t22);
    t25 = fepi / t23;
    t29 = Math.pow(lambda / lambda1, (double) (2 * s));
    t30 = 0.1e1 + t29;
    t31 = 0.1e1 / t30;
    t33 = 0.1e1 / lambda;
    t36 = t30 * t30;
    t52 = lambda * (-0.5e1 * t3 / t5 / t4 * t10 + 0.2e1 * fmax * t2 * t1 / t16 * t10 - t25 * t31 * (double) t22 * t33 - 0.2e1 * t25 / t36 * t29 * (double) s * t33) / (t3 / t5 / lambda * t10 + t25 * t31);
    fl1 = (float) t52;
    
    t1 = lambda * lambda;
    t2 = lambda0 * lambda0;
    t3 = t2 * t2;
    t4 = fmax * t3;
    t5 = t1 * lambda;
    t6 = t1 * t1;
    t9 = 0.1e1 / t1;
    t11 = Math.exp(-t2 * t9);
    t17 = t6 * t6;
    t23 = t3 * t3;
    t31 = 1 + 2 * alpha;
    t32 = Math.pow(lambda, (double) t31);
    t34 = fepi / t32;
    t38 = Math.pow(lambda / lambda1, (double) (2 * s));
    t39 = 0.1e1 + t38;
    t40 = 0.1e1 / t39;
    t41 = t31 * t31;
    t45 = t39 * t39;
    t47 = t34 / t45;
    t49 = t38 * (double) s;
    t59 = t38 * t38;
    t60 = s * s;
    t81 = t1 * (0.30e2 * t4 / t6 / t5 * t11 - 0.26e2 * fmax * t3 * t2 / t17 / lambda * t11 + 0.4e1 * fmax * t23 / t17 / t5 * t11 + t34 * t40 * (double) t41 * t9 + 0.4e1 * t47 * (double) t31 * t9 * t49 + t34 * t40 * (double) t31 * t9 + 0.8e1 * t34 / t45 / t39 * t59 * (double) t60 * t9 - 0.4e1 * t47 * t38 * (double) t60 * t9 + 0.2e1 * t47 * t49 * t9) / (t4 / t6 / lambda * t11 + t34 * t40);
    fl2 = (float) t81;
  }
  
  private static void computePhi0(float lambda){ //from ISIS platom.for;
       
    float fepi=flux_paras0[0];
    float alpha=flux_paras0[1];
    float fmax=flux_paras0[2];
    float wat=flux_paras0[3];
    float cona=flux_paras0[4];
    float al4=0.00001f;
    float al1=flux_paras0[5];
    float al2=flux_paras0[6];
    float al3=0.0f;
    if(cona >= 0.001f){
      al3=flux_paras0[7];
      al4=flux_paras0[8];
    }
    float wt2=wat*wat;
    double w2, rw2, xpt, fim, fim1, fim2, wal, xp1, xp2, del1, del2, del, tw1, tw2, win1, fiep, fep1, fep2, fmx, fep;
    
      w2=lambda*lambda;
      rw2=wt2/w2;
      xpt=Math.exp(-rw2);
      fim=rw2*rw2*xpt/lambda;
      fim2=fim*(30.-rw2*(26.-4.*rw2));
      fim1=fim*(2.*rw2-5.);
      wal=(lambda-al1)/al2;
      if (wal > 80.) xp1=1.0e30;
      else xp1=Math.exp(wal);
      if (cona > 0.001) xp2=Math.exp((al3-lambda)/al4);
      else xp2=0.f;
      del1=1./(1.+xp1);
      del2=1.+cona/(1.+xp2);
      del=del1*del2;
      tw1=2.*alpha+1.;
      tw2=tw1+1.;
      win1=Math.pow(lambda, tw1);
      fiep=del/win1;
      fep1=-fiep*(tw1+lambda/al2/(1./xp1+1.));
      fep2=fiep*(Math.pow(tw1+lambda/al2/(1./xp1+1.),2)+tw1-lambda*lambda/al2/al2/xp1/(1.+1./xp1)/(1.+1./xp1));
      fmx=fim*fmax;
      fep=fiep*fepi;
      fl1 = (float)((fim1*fmax +fep1*fepi)/(fmx+fep));
      fl2 = (float)((fim2*fmax +fep2*fepi)/(fmx+fep));
/* code generated using Maple;
    float lambda0 = wat, a=alpha, lambda1=al1,lambda2=al2;
    fl1 = (float)(lambda * (-0.5e1 * fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.6e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + 0.2e1 * fmax * Math.pow(lambda0, 0.6e1) * Math.pow(lambda, -0.8e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) - fepi / Math.pow(lambda, (double) (1 + 2 * a)) / (0.1e1 + Math.exp((lambda - lambda1) / lambda2)) * (double) (1 + 2 * a) / lambda - fepi / Math.pow(lambda, (double) (1 + 2 * a)) * Math.pow(0.1e1 + Math.exp((lambda - lambda1) / lambda2), -0.2e1) / lambda2 * Math.exp((lambda - lambda1) / lambda2)) / (fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.5e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + fepi / Math.pow(lambda, (double) (1 + 2 * a)) / (0.1e1 + Math.exp((lambda - lambda1) / lambda2))));
    fl2 = (float)(lambda * lambda * (0.30e2 * fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.7e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) - 0.26e2 * fmax * Math.pow(lambda0, 0.6e1) * Math.pow(lambda, -0.9e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + 0.4e1 * fmax * Math.pow(lambda0, 0.8e1) * Math.pow(lambda, -0.11e2) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + fepi / Math.pow(lambda, (double) (1 + 2 * a)) / (0.1e1 + Math.exp((lambda - lambda1) / lambda2)) * (double) Math.pow((double) (1 + 2 * a), (double) 2) * Math.pow(lambda, -0.2e1) + 0.2e1 * fepi / Math.pow(lambda, (double) (1 + 2 * a)) * Math.pow(0.1e1 + Math.exp((lambda - lambda1) / lambda2), -0.2e1) * (double) (1 + 2 * a) / lambda / lambda2 * Math.exp((lambda - lambda1) / lambda2) + fepi / Math.pow(lambda, (double) (1 + 2 * a)) / (0.1e1 + Math.exp((lambda - lambda1) / lambda2)) * (double) (1 + 2 * a) * Math.pow(lambda, -0.2e1) + 0.2e1 * fepi / Math.pow(lambda, (double) (1 + 2 * a)) * Math.pow(0.1e1 + Math.exp((lambda - lambda1) / lambda2), -0.3e1) * Math.pow(lambda2, -0.2e1) * Math.pow(Math.exp((lambda - lambda1) / lambda2), 0.2e1) - fepi / Math.pow(lambda, (double) (1 + 2 * a)) * Math.pow(0.1e1 + Math.exp((lambda - lambda1) / lambda2), -0.2e1) * Math.pow(lambda2, -0.2e1) * Math.exp((lambda - lambda1) / lambda2)) / (fmax * Math.pow(lambda0, 0.4e1) * Math.pow(lambda, -0.5e1) * Math.exp(-lambda0 * lambda0 * Math.pow(lambda, -0.2e1)) + fepi / Math.pow(lambda, (double) (1 + 2 * a)) / (0.1e1 + Math.exp((lambda - lambda1) / lambda2))));
*/
  }
  
  private static void computeEpsilon(float lambda){
   
    double dk=2.*Math.PI/1.44, k, rk, a, b; //dk is the parameter for HE_GAS;

    k = 2*Math.PI/lambda;
      rk=dk/k;
      a=Math.exp(-rk);
      b=1-a;
      bf1=(float)(-rk*a/b);
      bf2=(float)(rk*(2.-rk)*a/b);
    
  }
  
  public static float plaatom(float lambda, String[] target, float[] formula, 
               float temperature, float scattering_angle, float path_incident, float path_scattering, 
               boolean ISdimensionless){
    
    float p = 0.0f;
    float[] scattering_params;
    double f=path_incident/(path_incident+path_scattering), sth=Math.sin(scattering_angle/2);
    double k, kte, cor1, cor2;
    
    computePhi(lambda);
    computeEpsilon(lambda);
   
                     
   
//      k= 2.*Math.PI/lambda[i];
      kte=1.0534026e-3*lambda*lambda*temperature;
//      System.out.println("f: "+f+" fl1: "+fl1[i]+" bf1: "+bf1[i]+" "+((f-1)*fl1[i]-f*bf1[i]+f-3));
      cor1= 2*sth*sth*((f-1)*fl1-f*bf1+f-3)  // first order term
             +kte/2.        // second order terms
             +kte*sth*sth*((8*f-9)*(f-1)*fl1
             +3*f*(3-2*f)*bf1
             +2*f*(1-f)*fl1*bf1
             +(1-f)*(1-f)*fl2
             +f*f*bf2
             +3*(4*f-5)*(f-1));
      cor2= 2*sth*sth
             +2*Math.pow(sth, 4)*((4*f-7)*(f-1)*fl1
             +f*(7-2*f)*bf1
             +2*f*(1-f)*bf1*fl1
             +(1-f)*(1-f)*fl2
             +f*f*bf2
             +(2*f*f-7*f+8));
//    System.out.println("lambda: "+lambda+" cor1: "+cor1+" cor2: "+cor2);
    
//      p[i]=0.f;
      float sigmas_sum = 0.0f, formula_sum = 0.0f;
      for (int j = 0; j < target.length; j++){
        scattering_params = MutCross.loadSigmaTable().getScatteringParams(target[j]);
        formula_sum += formula[j];
        sigmas_sum += formula[j]*scattering_params[2]/4/Math.PI;  
//        System.out.println("scattering_params[2]: "+scattering_params[2]);
        p += formula[j]*scattering_params[2]/4./Math.PI*(1.0+NMASS/scattering_params[0]*(cor1+NMASS/scattering_params[0]*cor2));
//        p += NMASS/scattering_params[0]*formula[j]*(cor1+NMASS/scattering_params[0]*cor2);
//        p += formula[j]*NMASS/scattering_params[0]*(cor1+NMASS/scattering_params[0]*cor2);
      }
      
      if (ISdimensionless) {
        p /= sigmas_sum;
      } else {
      p /= formula_sum;
      }        
   
    return p;
                   
  }
  
  public static void main(String[] args) {
	 
       
	String[] target =  {"V"};
    float[] formula = {1.0f};
    float temperature = 300.0f; //kelvin
    float scattering_angle = (float)(12.448*Math.PI/180);
    float path1 = 10.5f, path2 = 1.522f;

/*
//    String[] target = {"C", "Cl"};
//    float[] formula = {1.0f, 4.0f};
    String[] target = {"D", "O"};
    float[] formula = {2.0f, 1.0f};
    float temperature = 300.0f; //kelvin
    float scattering_angle = 113.624f/180*(float)Math.PI;
    float path1 = 10.5f, path2 = 1.5015f;  //group ID: 349, run 9011, largest total counts;
*/      
   
    int nlambda = 1600;
    float lambda, q, p;
    String output ="";	
    
    long t0 = System.currentTimeMillis();	  
    for (int i = 1; i < 42; i++){
//      q = 0.05f*(i+1);
//      lambda = (float)(4*Math.PI*Math.sin(scattering_angle/2)/q);
      lambda = 0.1f*i;
      q = (float)(4*Math.PI*Math.sin(scattering_angle/2)/lambda);
      p = plaatom(lambda, target, formula, temperature, scattering_angle, path1, path2, false);
//      System.out.println(q+" "+p);

    
//       System.out.println("q: "+q+" p: "+p);
       output += "["+lambda+","+p+"]"+",";
          

    }
    long t1 = System.currentTimeMillis();
    System.out.println("time: "+(t1-t0));       
   System.out.println(output); 

  
//    float lambda; 
    for (int i = 1; i < 70; i++){
      lambda = i*0.1f;
      computePhi(lambda);
      System.out.println("lambda: "+lambda+" fl1: "+fl1+" fl2: "+fl2); 
    }
   
  }

}
