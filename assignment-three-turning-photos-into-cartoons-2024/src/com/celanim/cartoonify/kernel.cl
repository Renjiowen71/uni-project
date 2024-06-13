int colourValue(int pixel, int colour) {
    return (pixel >> (colour * 8)) & 255;
}

int createPixel(int redValue, int greenValue, int blueValue) {
    return (redValue << (2 * 8)) + (greenValue << 8) + blueValue;
}

int __attribute__((overloadable))clamp(double value) {
    int result = (int) (value + 0.5); // round to nearest integer
    if (result <= 0) {
        return 0;
    } else if (result > 255) {
        return 255;
    } else {
        return result;
    }
}

int wrap(int pos, int size) {
    if (pos < 0) {
        pos = -1 - pos;
    } else if (pos >= size) {
        pos = (size - 1) - (pos - size);
    }
    return pos;
}

int quantizeColour(int colourValue, int numPerChannel) {
    float colour = colourValue / (255 + 1.0f) * numPerChannel;
    int discrete = round(colour - 0.49999f);
    int newColour = discrete * 255 / (numPerChannel - 1);
    return newColour;
}


__kernel void gaussianBlur(__global int *pixels, __global int *newPixels,
                           const int width, const int height) {
	int gid = get_global_id(0);
	int xCentre = gid%width;
	int yCentre = gid/width;
	int red=0, green=0, blue=0;
	double gaussianSum = 159.0;
    int filter[] = {
        2, 4, 5, 4, 2, // sum=17
        4, 9, 12, 9, 4, // sum=38
        5, 12, 15, 12, 5, // sum=49
        4, 9, 12, 9, 4, // sum=38
        2, 4, 5, 4, 2  // sum=17
    };
    for (int filterY = 0; filterY < 5; filterY++) {
        int y = wrap(yCentre + filterY - 2, height);
        for (int filterX = 0; filterX < 5; filterX++) {
            int x = wrap(xCentre + filterX - 2, width);
            int rgb = pixels[y*width+x];
            int filterVal = filter[filterY * 5 + filterX];
            red += colourValue(rgb, 2) * filterVal;
            green += colourValue(rgb, 1) * filterVal;
            blue += colourValue(rgb, 0) * filterVal;
        }
    }
    newPixels[yCentre * width + xCentre] = createPixel(clamp(red/ gaussianSum),clamp(green/ gaussianSum),clamp(blue/ gaussianSum));
}

__kernel void sobelEdgeDetect(__global int *pixels, __global int *newPixels,const int width, const int height, const int edgeThreshold) {

    int gid = get_global_id(0);
    int totalGradient;
    int redVertical=0;
    int blueVertical=0;
    int greenVertical=0;
    int redHorizontal=0;
    int blueHorizontal=0;
    int greenHorizontal=0;
	int xCentre = gid%width;
	int yCentre = gid/width;

    int filter_vertical[]= {
            -1, 0, +1,
            -2, 0, +2,
            -1, 0, +1
    };
    int filter_horizontal[] = {
            +1, +2, +1,
            0, 0, 0,
            -1, -2, -1
    };

    for (int filterY = 0; filterY < 3; filterY++) {
        int y = wrap(yCentre + filterY - 1, height);
        for (int filterX = 0; filterX < 3; filterX++) {
            int x = wrap(xCentre + filterX - 1, width);
            int rgb = pixels[y*width+x];
            int filterVertical = filter_vertical[filterY * 3 + filterX];
            int filterHorizontal = filter_horizontal[filterY * 3 + filterX];
            redHorizontal += colourValue(rgb, 2) * filterHorizontal;
            greenHorizontal += colourValue(rgb, 1) * filterHorizontal;
            blueHorizontal += colourValue(rgb, 0) * filterHorizontal;
            redVertical += colourValue(rgb, 2) * filterVertical;
            greenVertical += colourValue(rgb, 1) * filterVertical;
            blueVertical += colourValue(rgb, 0) * filterVertical;
        }
    }
    totalGradient = abs(redHorizontal) + abs(greenHorizontal) + abs(blueHorizontal)
            +abs(redVertical) + abs(greenVertical) + abs(blueVertical);
    if (totalGradient >= edgeThreshold) {
        newPixels[yCentre * width + xCentre] = 0;
    } else {
        newPixels[yCentre * width + xCentre] = 16777215;
    }
}


__kernel void reduceColours(__global int *oldPixels, __global int *newPixels,  const int numColours) {

    int gid = get_global_id(0);
    int rgb = oldPixels[gid];
    int newRed = quantizeColour(colourValue(rgb,2), numColours);
    int newGreen = quantizeColour(colourValue(rgb,1), numColours);
    int newBlue = quantizeColour(colourValue(rgb,0), numColours);
    newPixels[gid] = createPixel(newRed, newGreen, newBlue);
}

__kernel void mergeMask(__global int *maskPixels, __global int *photoPixels, __global int *newPixels, const int maskColour) {

    int gid = get_global_id(0);
    if (maskPixels[gid] == maskColour) {
        newPixels[gid] = photoPixels[gid];
    } else {
        newPixels[gid] = maskPixels[gid];
    }
}

