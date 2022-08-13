# Estêncil em Java

## Requisitos

- Java >= 1.8

## Descrição dos arquivos

- **Stencil.java**: classe que contém alguns métodos úteis para o algoritmo.

- **StopWatch.java**: cronômetro para medir o tempo de execução

- **Color.java**: classe para abstrair uma cor guardando os valores R, G e B em bytes que vão de -128 a 127.

- **Sequential.java**: contém instruções para rodar o algoritmo de estêncil sequencialmente.

- **Client.java**: descreve um cliente que pode ser instanciado

- **Server.java**: descreve um servidor que aceitará conexões de vários clientes uma vez instanciado

- **Main.java**: instruções principais para executar o algoritmo de estencil de forma distribuída

### Para rodar sequencialmente

```sh
javac Sequential.java
java Sequential
```

### Versão MPI

Necessário instalar o OpenMPI seguindo [este tutorial](http://charith.wickramaarachchi.org/2014/07/how-to-install-open-mpi-java.html), em seguida executar os seguintes comandos:

```sh
mpijavac Mpi.java
mpirun -c 2 java Mpi
```
