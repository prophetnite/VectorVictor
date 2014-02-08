Here's some stuff you might want to know if you're working with this.

the file live.py has the x,y,weight output for a heat map (although presently i don't think it's a good coordinate-making method; i gotta fix that still).


you'll need to use corpusizer.py to create the vecmodel-full1000.bin or vecmodel-partial-400.bin (number indicates size of neural net i/o layers) file from the wikipedia article (.xmlp###-p###.bz2) dumps from http://dumps.wikimedia.org/enwiki/latest/ , in a folder (relative to the .py file) of ../enwiki/ .  leave them bz2 compressed, it works with that.

