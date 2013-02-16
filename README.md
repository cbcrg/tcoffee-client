T-Coffee client
===============

A lightweight client for the T-Coffee web server. 

It makes possible to run T-Coffee alignments through a remote server, 
i.e. without having a local T-Coffee installation  


Example usage 
-------------

The client accepts almost all the T-Coffee legacy application command line options, with the only difference 
that file names HAVE to be prefixed with the `file:` string. 

For example: 

    $ ./dist/c-coffee file:<your fasta file> [t-coffee cmd line options]


The client options use a doble `--` prefix to distinguish with the T-Coffee options. 
To get a full list of the avilable options, type the following command: 

	$ ./dist/c-coffee --help
	

For the T-Coffee command line options, see 
http://www.tcoffee.org/Documentation/t_coffee/t_coffee_technical.htm

