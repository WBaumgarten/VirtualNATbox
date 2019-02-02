# VirtualNATbox
Java program that simulates how a NATbox works, with customizable table refresh rate and time to live values.

To compile:
	- Open terminal in the working directory
	- Run the command "make"

To clean:
	- Open terminal in the working directory
	- Run the command "make clean"

To start server/router:
	- Compile the program
	- Open terminal in the working directory
	- Run the command "java NATRouter <port> <size of ip pool> <table refresh rate> <ttl>"

To start client:
	- Compile the program
	- Open terminal in the working directory
	- <type of client>: 0 - internale; 1 - external
	- Run the command "java Client <server ip> <server port> <type of client>"
