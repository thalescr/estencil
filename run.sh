rm *.class
rm *.log
mpijavac Color.java Stencil.java Mpi.java
mpirun -c 2 java Mpi
