/*
 * Created on Mar 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package Operators.TOF_Diffractometer;

/**
 * @author taoj
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Platom {
  public static final float[] flux_paras = {3.49f, 0.09f, 7.96f, 2.74f, 0.f, 1.14f, 0.15f, 0.f, 0.f};
//  public static final float[] flux_paras = {10.263f, 0.092f, 8.323f, 4.49f, 0.f, 1.482f, 0.4592f, 0.f, 0.f};
  //fepi, alpha, fmax, lambdat, cona, lambda1, lambda2, lambda3, lambda4
  public static final float NMASS = 1.00f;
  
  private static float fl1, fl2, bf1, bf2;
  
  static void run(){
    try {
      MutCross.run(MutCross.sigmatable);
    } catch(Throwable t) {
        System.out.println("unexpected error");
        t.printStackTrace();
      }  
  }
  
  private static void computePhi(float lambda){
       
    float fepi=flux_paras[0];
    float alpha=flux_paras[1];
    float fmax=flux_paras[2];
    float wat=flux_paras[3];
    float cona=flux_paras[4];
    float al4=0.00001f;
    float al1=flux_paras[5];
    float al2=flux_paras[6];
    float al3=0.0f;
    if(cona >= 0.001f){
      al3=flux_paras[7];
      al4=flux_paras[8];
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
        scattering_params = MutCross.getScatteringParams(target[j]);
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
	 
/*       
	String[] target =  {"V"};
    float[] formula = {1.0f};
    float temperature = 300.0f; //kelvin
    float scattering_angle = (float)(9.9*Math.PI/180);
    float path1 = 10.5f, path2 = 1.522f;
*/

    String[] target = {"C", "Cl"};
    float[] formula = {1.0f, 4.0f};
    float temperature = 300.0f; //kelvin
    float scattering_angle = (float)(9.9*Math.PI/180);
    float path1 = 10.5f, path2 = 1.522f;
      
   
   int nlambda = 1600;
   float lambda, q, p;
	  
    run();	  
   for (int i = 0; i < 1600; i++){
     q = 0.025f*(i+1);
     lambda = (float)(4*Math.PI*Math.sin(scattering_angle/2)/q);
     p = plaatom(lambda, target, formula, temperature, scattering_angle, path1, path2, false);
     if ( i < 40 ) System.out.println("q: "+q+" p: "+p);
   }
   
   
   
  }

}
