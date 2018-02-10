package fz.imt;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.BOWKMeansTrainer;
import org.bytedeco.javacpp.opencv_features2d.DescriptorMatcher;
import org.bytedeco.javacpp.opencv_ml.SVM;
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT;
import org.bytedeco.javacpp.indexer.ByteIndexer;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.javacpp.indexer.IntRawIndexer;

import fz.imt.opencv.utils.Hist1D;

/**
 * Creer le dictionnaire Creer les histogrammes descripteurs des images de train
 * (par classe) puis train SVM Prediction
 * 
 * @author Nico
 *
 */
public class LogoFinder {

	private File rootDir;
	private int maxWords = 50;
	private Mat vocabulary = null;
	private List<SVM> classifiers = null;

	public LogoFinder(String rootDirPath) {
		this.rootDir = new File(rootDirPath);
	}
	
	public void showHist(Mat hist, String name) {
		Hist1D histo = new Hist1D();
		//histo.setNumberOfBins(hist.rows());
		histo.show(histo.getHistogramImage(hist), name);
	}

	private Mat buildVocabulary(File[] imagesTrain) {
		File voc = new File("classifier/vocab.yml");
		if(voc.exists()) {
			FileStorage loader = new FileStorage(voc.getAbsolutePath(), FileStorage.READ);
			this.vocabulary = loader.get("vocabulary").mat();
			loader.close();
			System.out.println("Vocabulaire chargé !");
		}
		else if (imagesTrain != null) {
			BOWKMeansTrainer trainer = new BOWKMeansTrainer(this.maxWords);
			int i = 0;
			for (File imgTrain : imagesTrain) {
				Mat trainMat = opencv_imgcodecs.imread(imgTrain.getAbsolutePath());
				KeyPointVector keypoints = new KeyPointVector();
				Mat descriptor = new Mat();
				SIFT sift = SIFT.create();
				sift.detectAndCompute(trainMat, new Mat(), keypoints, descriptor);
				trainer.add(descriptor);
				System.out.println("Train Vocabulary " + (i + 1));
				i++;
			}
			this.vocabulary = trainer.cluster();
			
			FileStorage ds = new FileStorage("classifier/vocab.yml", FileStorage.WRITE);
			ds.write("vocabulary", this.vocabulary);
			ds.close();
			return this.vocabulary;
		}
		return null;
	}
	
	private void showMat(Mat m) {
		
		FloatRawIndexer ind = m.createIndexer();
		for(int rows = 0; rows < m.rows(); rows++) {
			for(int cols = 0; cols < m.cols(); cols++) {
				System.out.print(ind.get(rows, cols));
			}
			System.out.println("");
		}
	}

	public void train() {
		buildVocabulary(this.rootDir.listFiles());
		//showHist(this.vocabulary, "Vocabulaire");
		Mat samples = new Mat();
		BOWImgDescriptorExtractor extractor = new BOWImgDescriptorExtractor(
				DescriptorMatcher.create(DescriptorMatcher.FLANNBASED));
		extractor.setVocabulary(this.vocabulary);

		for (File trainImg : this.rootDir.listFiles()) {
			Mat trainMat = opencv_imgcodecs.imread(trainImg.getAbsolutePath());
			opencv_imgproc.resize(trainMat, trainMat, new opencv_core.Size(400, 400));
			Mat descriptor = new Mat();
			KeyPointVector keypoints = new KeyPointVector();
			SIFT sift = SIFT.create();
			sift.detectAndCompute(trainMat, new Mat(), keypoints, descriptor);
			BOWKMeansTrainer trainer = new BOWKMeansTrainer(this.maxWords);
			trainer.add(descriptor);
			Mat clust = trainer.cluster();
			Mat histo = new Mat();

			extractor.compute(clust, histo);
			//showHist(histo, trainImg.getName());
			samples.push_back(histo);
		}

		//Fabrication des SVM avec les labels
		int globalIndex = 0;
		int currentSvm = 0;
		int indexStart = 0;
		int indexStop = samples.rows();
		String class_name = "";
		int[] resp = new int[samples.rows()];
		for (File trainImg : this.rootDir.listFiles()) {

			if (globalIndex != 0 && (!class_name.equals(trainImg.getName().split("_")[0])
					|| globalIndex == this.rootDir.listFiles().length - 1)) {
				System.out.println("Save SVM for classe " + class_name);
				indexStop = globalIndex;
				if(globalIndex == this.rootDir.listFiles().length - 1) indexStop = resp.length;
				for(int j = 0; j < resp.length; j++) {
					if(j >= indexStart && j < indexStop) {
						resp[j] = 1;
					} else resp[j] = -1;
				}
				indexStart = indexStop;
				IntPointer pointerInt = new IntPointer(resp);
				Mat labels = new Mat(pointerInt);

				SVM svm = SVM.create();
				svm.setKernel(SVM.RBF);
				svm.setType(SVM.C_SVC);
				svm.train(samples, opencv_ml.ROW_SAMPLE, labels);
				svm.save("classifier/" + class_name + ".xml");

				int a = 0;
			}
			if (!class_name.equals(trainImg.getName().split("_")[0])) {
				class_name = trainImg.getName().split("_")[0];
				System.out.println("Train class " + class_name);
			}

			globalIndex++;
		}


	}

	public File getRootDir() {
		return rootDir;
	}

	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}

	public int getMaxWords() {
		return maxWords;
	}

	public void setMaxWords(int maxWords) {
		this.maxWords = maxWords;
	}

	public Mat getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(Mat vocabulary) {
		this.vocabulary = vocabulary;
	}

}
