# *** This file is given as part of the programming assignment. *** 

SHELL  = /bin/sh

# pretty minimal makefile
LL:
	javac *.java

# invoke via "make clean".
# WARNING: make sure you know what this is going to do before you invoke it!!!
clean:
	/bin/rm -f *.class *~ LL core* *.output
