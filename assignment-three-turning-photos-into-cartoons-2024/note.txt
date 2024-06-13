Turning Photos into Cartoons
============================

The 'Cartoonify' program in this project processes a set of photos
and uses edge detection and colour reduction to make them cartoon-like.

Each input image, eg. xyz.jpg, is processed and then output
to a file called xyz_cartoon.jpg.

To run the program, you can either:

1. run com.celanim.cartoonify.Cartoonify.main from within IntelliJ.

2. run as: java -cp out/production/cartoonify com.celanim.cartoonify.Cartoonify

3. run ant task script from 'cartoonify' directory: ant main

Run the program with no arguments to see the usage message.

The clean.sh script can be used to delete all output images when they are no longer needed.


IMPORTANT: the unit tests should be run after any code changes.


Copyright 2014, CelAnim.com.
All rights reserved.

