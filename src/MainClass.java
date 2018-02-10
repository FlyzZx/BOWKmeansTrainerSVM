import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_features2d.BOWKMeansTrainer;
import org.bytedeco.javacpp.opencv_ml.SVM;
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT;

import fz.imt.LogoFinder;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class MainClass {

	public static void main(String[] args) {
		Loader.load(opencv_core.class);
		System.out.println("Librairies charg�es !");
		HashMap<Integer, String> mappingClass = new HashMap<>();
		
		LogoFinder fz = new LogoFinder("TrainImage/");
		fz.train();
		File cocaTrain = new File("coca_train.jpg");
		fz.predict(cocaTrain.getAbsolutePath());
		File pepsiTrain = new File("pepsi_test.jpg");
		fz.predict(pepsiTrain.getAbsolutePath());
		File spriteTrain = new File("sprite_test.jpg");
		fz.predict(spriteTrain.getAbsolutePath());
	}

}
