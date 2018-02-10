package fz.imt.opencv.utils;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.WindowConstants;

import static org.bytedeco.javacpp.opencv_core.LUT;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.calcHist;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;

/**
 * Helper class that simplifies usage of OpenCV `calcHist` function for single channel images.
 */
public class Hist1D {

	public static Mat load(File file, int flags) throws IOException {
		if(!file.exists()) {
			throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath());
		}
		Mat image = imread(file.getAbsolutePath(), flags);
		if(image == null || image.empty()) {
			throw new IOException("Couldn't load image: " + file.getAbsolutePath());
		}
		return image;
	}

	public static void show(Image image, String caption) {
		CanvasFrame canvas = new CanvasFrame(caption, 1);
		canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		canvas.showImage(image);
	}
	
    /**
     * @param image
     * @param lookup
     * @return
     */
	
	public static float max(float[] array) {
        float max = Float.MIN_VALUE;
        for(float f : array) {
            if(f > max) {
                max = f;
            }
        }
        return max;
    }

    /**
     * @param array
     * @return
     */
    public static float sum(float[] array) {
        float v = 0;
        for(float f : array) {
            v += f;
        }
        return v;
    }
    public static Mat applyLookUp(Mat image, Mat lookup) {
        Mat dest = new Mat();
        LUT(image, lookup, dest);
        return dest;
    }

    /**
     * Equalize histogram of an image. The algorithm normalizes the brightness and increases the contrast of the image.
     * It is a wrapper for OpenCV function `equalizeHist`.
     *
     * @param src
     * @return
     */
    public static Mat equalize(Mat src) {
        Mat dest = new Mat();
        equalizeHist(src, dest);
        return dest;
    }

    private int numberOfBins = 200;
    private int[] channels = new int[]{0};
    public int getNumberOfBins() {
		return numberOfBins;
	}

	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}

	private float _minRange = 0.0f;
    private float _maxRange = 255.0f;

    public void setRanges(float minRange, float maxRange) {
        _minRange = minRange;
        _maxRange = maxRange;
    }

    public BufferedImage getHistogramImage(Mat image) {

        // Output image size
        int width = numberOfBins;
        int height = numberOfBins;

        float[] hist = getHistogramAsArray(image);
        // Set highest point to 90% of the number of bins
        double scale = 0.9 / max(hist) * height;

        // Create a color image to draw on
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();

        // Paint background
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Draw a vertical line for each bin
        g.setPaint(Color.BLUE);
        for (int bin = 0 ; bin < numberOfBins; bin++) {
            int h = (int) Math.round(hist[bin] * scale);
            g.drawLine(bin, height - 1, bin, height - h - 1);
        }

        // Cleanup
        g.dispose();

        return canvas;
    }

    /**
     * Computes histogram of an image.
     *
     * @param image input image
     * @return histogram represented as an array
     */
    float[] getHistogramAsArray(Mat image) {
        // Create and calculate histogram object
        Mat hist = image;

        // Extract values to an array
        float[] dest = new float[numberOfBins];
        FloatIndexer histI = hist.createIndexer();
        for (int bin = 0 ; bin < numberOfBins; bin++) {
            dest[bin] = histI.get(bin);
        }

        return dest;
    }

}