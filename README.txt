All code was written by John Romo.  I did not create the 
png files used for the chess pieces. Other than those png
files, I own all the code.  You can download it, but even if
you wanted to sell it (which you wouldn't) you can't. 

This is a complete Chess analysis board GUI with many
features including save position and load position among
many others.  To use the analysis features and to play
against the engine, you will need to download a copy of 
Stockfish which can be found at stockfishchess.org.  
This program was written for stockfish 15.1, so there 
is no guaruntee it will work for other version. 

To get stockfish working:  edit Engine.java and change the 
engineLocation variable (line 4) to the directory where your 
stockfish executable is located.  Then edit Game.java and
change the engineActive variable (line 21) to true. 

The above process was tested on both windows 10 and archlinux. 
I cannot be sure it will work on anything else, but I suspect
it wil work on any windows version, any linux distro, and 
probably mac.

Unfortunately, the font did not scale well.  I'm not sure how
to fix that, and since this was my first large scale non-school
project, and I wrote it months ago, I'll just leave it be. 


