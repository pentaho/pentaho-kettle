
#include <unistd.h>

/**********************************************************
 
   This tool launches another program and eats the log.
   This way, it can be run without a console.
	
   The program to launch starts from argument 2.
   The first argument is the log-file to append to.

************************************************************/
  
int main(int argc, char *args[])
{
	int i;

	if (argc<2)
	{
		printf("USAGE: kettle < logfile to append >  < spoon | chef | ... > [ argument, argument, ... ]\n");
		return(1);
	}

	printf("args: \n");
	for (i=0;i<argc;i++)
	{
		printf("  #%2d : %s\n", i, args[i]); 
	}
	
}
