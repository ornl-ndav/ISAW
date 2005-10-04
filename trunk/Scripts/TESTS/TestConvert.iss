vec = IntListToVector( "1:4,10,20:25" )
for i in [0:9]
  Display vec[i]
endfor
str = VectorToIntListString( vec )
Display str

gsas_calib = VectorToGsasCalib( vec )
Display gsas_calib
new_vec = GsasCalibToVector( gsas_calib )
for i in [0:2]
  Display new_vec[i]
endfor

Display "Testing conversion between Vector and intArray"
vec[0] = -3.45
vec[1] = -2.55
vec[2] =  12.3
int_array = VectorTo_intArray( vec )
vec3 = IntListToVector( int_array )
str3 = VectorToIntListString( vec3 )
Display str3

int_vec = intArrayToVector( int_array )
for i in [0:10]
  Display int_vec[i]
endfor

Display "Testing conversion between Vector and floatArray"
float_array = VectorTo_floatArray( vec )
float_vec = floatArrayToVector( float_array )
for i in [0:10]
  Display float_vec[i]
endfor
