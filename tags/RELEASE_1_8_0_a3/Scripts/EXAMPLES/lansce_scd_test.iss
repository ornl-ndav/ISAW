#
# @overview This operator loads an SCD data file from LANSCE or IPNS
#           and produces up to different views of the data.  If you are 
#           running this on a system with limited memory resources, 
#           it will probably be necessary to only use one of the 
#           display options at a time 
#
# @param file            The name of the file containing the data
# @param tof_data_index  The index of the time-of-flight DataSet in the runfile.
#                        For IPNS SCD runfiles this is currently 2.
#                        For LANSCE SCD NeXus files, this is currently 3. 
# @param show_recip_space  If selected the histogram bins above a threshold of
#                          of 50 will be displayed in reciprocal space.
# @param show_image        If selected the time-of-flight spectra will be
#                          displayed together as the rows of an image.
# @param show_detectors    If selected, a 3D display of the detectors will
#                          be shown with the data mapped onto the detectors
#
# @param author Dennis Mikkelson
# $Date$
# 
$file              LoadFile("/home/dennis/LANSCE_1_9_06/RUBY_11_x_05/SCD_E000005_R000725.nx.hdf")  SCD File
$tof_data_index    Integer(3)         Index of the time-of-flight DataSet in the data file
$show_recip_space  Boolean(false)     Show the 3D view of reciprocal space
$show_image        Boolean(true)      Show the list of all spectra as an image
$show_detectors    Boolean(false)     Show the detectors in 3D with the data

$Category=Macros,Utils,Examples
#

load file,"ds"
vec[0] = ds[tof_data_index]

if show_recip_space 
  Display_SCD_Reciprocal_Space( vec, 50 )
endif

if show_image
  Display vec[0]
endif

if show_detectors
  Display vec[0], "3D View"
endif

return "Success"
