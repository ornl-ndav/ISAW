#Testing arrays
# $Date$

d = [1,3,5,5+2]
# or d[]=[1,3,5,7]

Display d
Display d[1]
Display d&[8,9,10]
Display d+2

# NOTE d[9] below would give an error
d[7]=12
Display d
Display d+[1,2,3,4,5,6,7,8]
Display 2*d


# Can store structure information, multiple arrays, etc.
d=[[1,3,5],2,"John"]
Display d

# Arrays are stored internally as Vectors. Operators in Java can now
# take Vectors that, if careful, act like Reference arguments.

