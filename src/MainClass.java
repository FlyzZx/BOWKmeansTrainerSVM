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

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		Loader.load(opencv_core.class);
		System.out.println("Librairies chargées !");
		HashMap<Integer, String> mappingClass = new HashMap<>();
		
		LogoFinder fz = new LogoFinder("TrainImage/");
		fz.train();
		int a = 0;
		/**
		 * Training
		 */
		/*File trainDir = new File("TrainImage");
		File[] trainImages = trainDir.listFiles();
		// Contruction des labels
		String class_name = "";
		int index = 0;
		for (File trainImg : trainImages) {
			if (!class_name.equals(trainImg.getName().split("_")[0])) {
				class_name = trainImg.getName().split("_")[0];
				mappingClass.put(index, class_name);
				index++;
			}
		}

		index = 0;
		class_name = "";
		int[] labelsValue = new int[trainImages.length];
		for (File trainImg : trainImages) {
			if (!class_name.equals(trainImg.getName().split("_")[0])) {
				class_name = trainImg.getName().split("_")[0];
			}
			Integer key = getKeyByValue(mappingClass, class_name);
			labelsValue[index] = key;
			index++;
		}
		IntPointer intP = new IntPointer(labelsValue);
		Mat labels = new Mat(intP);

		// Extraction des descripteurs SIFT + BOW
		class_name = "";
		index = 0;
		int nfeatures = 0;
		Mat trainDatas = new Mat();
		BOWKMeansTrainer bowTrain = new BOWKMeansTrainer(20);
		Mat vocabulary = new Mat();
		for (File trainImg : trainImages) {
			int nOctaveLayers = 3;
			double contrastThreshold = 0.03;
			int edgeThreshold = 10;
			double sigma = 1.6;
			Mat descriptor = new Mat();
			Mat trainMat = imread(trainImg.getAbsolutePath());
			//resize(trainMat, trainMat, new Size(400, 400));
			KeyPointVector keypoints = new KeyPointVector();
			
			SIFT sift = SIFT.create(nfeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);
			sift.detectAndCompute(trainMat, new Mat(), keypoints, descriptor);
			bowTrain.add(descriptor);
			/*while(descriptor.rows() > nfeatures) 
				descriptor.pop_back();*/
			
			/*descriptor = bowTrain.cluster().reshape(1, 1);
			
			trainDatas.push_back(descriptor);
			index++;
			System.out.println("Train number " + index);
		}
		
		FileStorage ds = new FileStorage("classifier/vocav.yml", FileStorage.WRITE);
		ds.write("vocabulary", bowTrain.cluster());
		ds.close();

		//imwrite("classifier/vocav.yaml", trainDatas);
		//Training SVM
		SVM svm = SVM.create();
		svm.setType(SVM.C_SVC);
		svm.setKernel(SVM.RBF);
		
		svm.train(trainDatas, opencv_ml.ROW_SAMPLE, labels);
		svm.save("classifier/classify.xml");

		System.out.println("Training terminé");*/
	}

}
