# BOWKmeansTrainerSVM
Génère des classifiers, un vocabulaire et un fichier d'index en fonction d'image de train

## Convention de nommage des fichiers
Les fichiers doivent être nommée selon [CLASSNAME]_[FileName].jpg

## Création de l'objet et exemple d'utilisation
```java
LogoFinder fz = new LogoFinder("DataImage/"); //DataImage for the train, give "" if u dont need to train
//SIFT PARAMETERS
fz.setSigma(1.4);
fz.setMaxWords(100);
//File Dir
fz.setVocabularyDir("vocabulary");
fz.setClassifierDir("classifier");
fz.train(); //TRAIN
//Predict
File cocaTrain = new File("coca_train.jpg");
fz.predict(cocaTrain.getAbsolutePath());
```
