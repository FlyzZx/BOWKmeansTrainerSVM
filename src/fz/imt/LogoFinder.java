package fz.imt;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.BOWKMeansTrainer;
import org.bytedeco.javacpp.opencv_features2d.DescriptorMatcher;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_ml.SVM;
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Creer le dictionnaire Creer les histogrammes descripteurs des images de train
 * (par classe) puis train SVM Prediction
 * 
 * @author Nico
 *
 */
public class LogoFinder {

	private File rootDir;
	private int maxWords = 200;
	private Mat vocabulary = null;
	private int nFeatures = 0;
	private int nOctaveLayers = 3;
	private double contrastThreshold = 0.04;
	private double edgeThreshold = 10;
	private double sigma = 1.6;
	private String vocabularyDir = "";
	private String classifierDir = "";

	public LogoFinder() {
		this.rootDir = null;
	}

	public LogoFinder(String rootDirPath) {
		this.rootDir = new File(rootDirPath);
	}

	private Mat buildVocabulary() {

		File voc = new File(this.vocabularyDir + "/vocab.yml");
		if (voc.exists()) {
			FileStorage loader = new FileStorage(voc.getAbsolutePath(), FileStorage.READ);
			this.vocabulary = loader.get("vocabulary").mat();
			loader.close();
			System.out.println("Vocabulaire chargé !");
		} else if (rootDir != null) {
			File[] imagesTrain = rootDir.listFiles();
			BOWKMeansTrainer trainer = new BOWKMeansTrainer(this.maxWords);
			int i = 0;
			for (File imgTrain : imagesTrain) {
				Mat trainMat = opencv_imgcodecs.imread(imgTrain.getAbsolutePath());
				opencv_imgproc.resize(trainMat, trainMat, new opencv_core.Size(500, 500));
				KeyPointVector keypoints = new KeyPointVector();
				Mat descriptor = new Mat();
				SIFT sift = SIFT.create(nFeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);
				sift.detectAndCompute(trainMat, new Mat(), keypoints, descriptor);
				trainer.add(descriptor);
				System.out.println("Train Vocabulary " + (i + 1));
				i++;
			}
			this.vocabulary = trainer.cluster();

			FileStorage ds = new FileStorage(this.vocabularyDir + "/vocab.yml", FileStorage.WRITE);
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
		buildVocabulary();
		//showHist(this.vocabulary, "Vocabulaire");
		Mat samples = new Mat();
		BOWImgDescriptorExtractor extractor = new BOWImgDescriptorExtractor(
				DescriptorMatcher.create(DescriptorMatcher.FLANNBASED));
		extractor.setVocabulary(this.vocabulary);

		for (File trainImg : this.rootDir.listFiles()) {
			Mat trainMat = opencv_imgcodecs.imread(trainImg.getAbsolutePath());
			opencv_imgproc.resize(trainMat, trainMat, new opencv_core.Size(500, 500));
			Mat descriptor = new Mat();
			KeyPointVector keypoints = new KeyPointVector();
            SIFT sift = SIFT.create(nFeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);
			sift.detectAndCompute(trainMat, new Mat(), keypoints, descriptor);
			BOWKMeansTrainer trainer = new BOWKMeansTrainer(this.maxWords);
			trainer.add(descriptor);
			System.out.println("Clustering features into words for " + trainImg.getName());
			Mat clust = trainer.cluster();
			Mat histo = new Mat();

			extractor.compute(clust, histo);
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
				File classLocation = new File(this.classifierDir + "/" + class_name + ".xml");
				if(classLocation.exists()) {
					System.out.println("Existing SVM for classe " + class_name);
				} else {
					System.out.println("Save SVM for classe " + class_name);
					indexStop = globalIndex;
					if(globalIndex == this.rootDir.listFiles().length - 1) indexStop = resp.length;
					for(int j = 0; j < resp.length; j++) {
						if(j >= indexStart && j < indexStop) {
							resp[j] = 1;
						} else resp[j] = 0;
					}
					indexStart = indexStop;
					IntPointer pointerInt = new IntPointer(resp);
					Mat labels = new Mat(pointerInt);

					SVM svm = SVM.create();
					svm.setKernel(SVM.RBF);
					svm.setType(SVM.C_SVC);
					svm.train(samples, opencv_ml.ROW_SAMPLE, labels);
					svm.save(this.classifierDir + "/" + class_name + ".xml");
				}
			}
			if (!class_name.equals(trainImg.getName().split("_")[0])) {
				class_name = trainImg.getName().split("_")[0];
				System.out.println("Changing Train class " + class_name);
			}

			globalIndex++;
		}
	}

	public String predict(String filePath) {
		//On vérifie l'existence du vocabulaire
		if(vocabulary == null){
			buildVocabulary();
		}

		//Chargement des classifieurs en m�moire
		ArrayList<String> classPath = new ArrayList<>();
		File classiLocation = new File(this.classifierDir);
		for(File classiFile : classiLocation.listFiles()) {
			classPath.add(classiFile.getAbsolutePath());
		}

		//Chargement du vocabulaire en m�moire
		BOWImgDescriptorExtractor extractor = new BOWImgDescriptorExtractor(
				DescriptorMatcher.create(DescriptorMatcher.FLANNBASED));
		extractor.setVocabulary(this.vocabulary);

		//Pr�diction
		System.out.println("Predicting file " + filePath);
		Mat testImg = opencv_imgcodecs.imread(filePath);
		opencv_imgproc.resize(testImg, testImg, new opencv_core.Size(500, 500));
		Mat descriptor = new Mat();
		KeyPointVector keypoints = new KeyPointVector();
        SIFT sift = SIFT.create(nFeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);
		System.out.println("Detecting features");
		sift.detectAndCompute(testImg, new Mat(), keypoints, descriptor);
		BOWKMeansTrainer trainer = new BOWKMeansTrainer(this.maxWords);
		trainer.add(descriptor);
		System.out.println("Clustering features into words");
		Mat clust = trainer.cluster();
		Mat histo = new Mat();
		System.out.println("Calculate words frequencies's histogram");
		extractor.compute(clust, histo);

        float minF = Float.MAX_VALUE;
        String bestMatch = null;
		for(String classP : classPath) {
			SVM svm = SVM.create();
			svm = SVM.load(classP);
			Mat retM = new Mat();
			float ret = svm.predict(histo, retM, 1);

            FloatRawIndexer indexer = retM.createIndexer();
            if(retM.cols() > 0 && retM.rows() > 0) {
                ret = indexer.get(0, 0); //R�cup�ration de la valeur dans la MAT
            }
            if(ret < minF) {
                minF = ret;
                bestMatch = classP;
            }
            System.out.println("Prediction for class " + classP + " : " + ret);
		}

		System.out.println("Prediction for file " + filePath + " is " + bestMatch + " : " + minF);
		return bestMatch;
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

    public int getnFeatures() {
        return nFeatures;
    }

    public void setnFeatures(int nFeatures) {
        this.nFeatures = nFeatures;
    }

    public int getnOctaveLayers() {
        return nOctaveLayers;
    }

    public void setnOctaveLayers(int nOctaveLayers) {
        this.nOctaveLayers = nOctaveLayers;
    }

    public double getContrastThreshold() {
        return contrastThreshold;
    }

    public void setContrastThreshold(double contrastThreshold) {
        this.contrastThreshold = contrastThreshold;
    }

    public double getEdgeThreshold() {
        return edgeThreshold;
    }

    public void setEdgeThreshold(double edgeThreshold) {
        this.edgeThreshold = edgeThreshold;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

	public String getVocabularyDir() {
		return vocabularyDir;
	}

	public void setVocabularyDir(String vocabularyDir) {
		this.vocabularyDir = vocabularyDir;
	}

	public String getClassifierDir() {
		return classifierDir;
	}

	public void setClassifierDir(String classifierDir) {
		this.classifierDir = classifierDir;
	}
}
