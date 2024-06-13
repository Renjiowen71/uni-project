This is an edited _stream.py script.

Line 429 of the original script 'targets = targets.astype(int)' automatically converts the ground truth to an integer value, which is a problem because the majority of values are -0.07 for example it causes most of the values to be 0.

I commented on this line to make this work properly.