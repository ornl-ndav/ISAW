#
#  @overview Simple example script that demonstrates creating a DataSet 
#  using a mathematical expresion and the LoadExpression operator. 
#  The DataSet is sent to the ISAW tree, where it can be viewed, and
#  processed like any other DataSet.
#
#  @param min_x         The minimum x value for the function data block. 
#  @param max_x         The maximum x value for the function data block.
#  @param num_x         The number of points at which the function should
#                       be sampled.
#  @param is_histogram  Flag indicating whether a histogram or function 
#                       should be created.
#
#  $Date$

$Category = Macros, Examples, Scripts ( ISAW )

$ Title = Evaluate Expression A * exp( -k*x ) * sin( w*x )

$ min_x         Float(0)       The minimum x value
$ max_x         Float(10)      The maximum x value
$ num_x         Integer(1000)  Default number of x values
$ is_histogram  Boolean(false) Interpret as Histogram

function = "A * exp( -k*x ) * sin( w*x )"
arg_name = "x"
parameter_names = "A, k, w"
parameter_values = "10, 0.8, 6.28"

send LoadExpr(function, arg_name, parameter_names, parameter_values, min_x, max_x, num_x, is_histogram )
