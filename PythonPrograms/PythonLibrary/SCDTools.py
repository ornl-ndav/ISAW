#"""*WIKI* 
           s2 = 10.0*np.mod(ieRot,(np.sqrt(2)))
           s3 = 10.0*np.mod(ieRot,(np.sqrt(3)))
           if (np.absolute(s2).all) <= 1:
             ieRot = ieRot /np.sqrt(2)
           elif (np.absolute(s3).all) <= 1:
             ieRot = ieRot /np.sqrt(3)
           else:
             ieRot = eRot/(np.max(np.absolute(eRot)))*6
             ieRot = ieRot/self.sINT(ieRot)
        print 'Vector from imput: ', vnorm
        vnorm = vnorm/norm(vnorm)
        signv =[x/np.absolute(x) for x in vnorm]
        r_vnorm =np.zeros(3)
        for i,vi in enumerate(vnorm):
          if np.absolute(vi) < 0.10:
            r_vnorm[i] = 0.0
          else:
            print '1/vi**2 for '+str(i)+': ', vi, 3.0/ (vi*vi),24.0/(vi*vi)
            rsqr = 24.0/(vi*vi)
            if np.absolute(rsqr - 28) < 2:
              rsqr = 28.0
            if np.absolute(rsqr - 36) < 2:
              rsqr = 36.0
            if np.absolute(rsqr - 42) < 2:
              rsqr = 42.0
            if np.absolute(rsqr - 56) < 2:
              rsqr = 56.0
            if np.absolute(rsqr - 63)<4 and (np.absolute(vnorm).max() - np.absolute(vi))>=0.08:
              rsqr = 63
            if np.absolute(rsqr - 64)<4 and (np.absolute(vnorm).max() - np.absolute(vi))<0.08:
              rsqr = 64
            if np.absolute(rsqr - 72) < 4:
              rsqr = 72.0
            if np.absolute(rsqr - 84) < 4:
              rsqr = 84.0
            if np.absolute(rsqr - 144) < 16:
              rsqr = 144.0
            if np.absolute(rsqr - 336) < 16:
              rsqr = 336
            if np.absolute(rsqr - 504) < 35:
              rsqr = 504
            r_vnorm[i] = signv[i] * np.sqrt(24.0/rsqr)
          print r_vnorm[i]
          print 'adjusted V'+str(i)+': ', r_vnorm[i]
        print 'Returned vector: ', r_vnorm/norm(r_vnorm)
        return np.array(r_vnorm/norm(r_vnorm))


    # Align the orientation matrices for runs at different setting angles
          rotation_axis = self.adjust_raxis(angle_axis[1])
          print "Rotation axis \n", rotation_axis
          alignedU = np.dot(testU, self.matrix_from_rotation_about_axis(angle, rotation_axis))
          print 'alignedU \n', alignedU
          #print "Rotation axis \n", angle_axis[2].round(4)
          #TODO Test the effect of rounding the axis vector to integer numbers