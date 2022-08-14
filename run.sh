rm *.class
mpijavac Color.java Stencil.java StopWatch.java Mpi.java
mpirun -c 4 java Mpi

