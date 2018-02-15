import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import com.sun.prism.paint.Paint;
import fz.imt.LogoFinder;
import fz.imt.java.utils.CustomOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.sql.Timestamp;

public class MainForm extends JPanel{

    private JFrame frame;
    private JPanel main_panel;
    private JTextField textField_trainData;
    private JTextField textField_vocabulaire;
    private JTextField textField_classifiers;
    private JTextField textField_indexJson;
    private JTextArea textArea_log;
    private JButton startTrainingButton;
    private JScrollPane scrollPane;
    private JButton button_testPrediction;

    private String trainingPath, vocabularyPath, classifiersPath, indexJsonPath;

    public MainForm(JFrame frame) {
        this.frame = frame;
        initializeComponents();
    }

    public static void main(String[] args) {
        /* Pour le LOOK AND FEEL */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("BOWKmeansTrainerSVM");
        frame.setContentPane(new MainForm(frame).main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setPreferredSize(new Dimension(800, 500));
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    private void initializeComponents() {
        textArea_log.setAutoscrolls(true);
        /*PrintStream printStream = new PrintStream(new CustomOutputStream(textArea_log));
        System.setOut(printStream);
        System.setErr(printStream);*/

        textField_trainData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = jFileChooser.showOpenDialog(MainForm.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    System.out.println("Train Data filename : " + jFileChooser.getSelectedFile().getAbsolutePath());
                    trainingPath = jFileChooser.getSelectedFile().getAbsolutePath();
                    textField_trainData.setText(trainingPath);
                }
            }
        });

        textField_vocabulaire.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = jFileChooser.showOpenDialog(MainForm.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    System.out.println("Vocabulary filename : " + jFileChooser.getSelectedFile().getAbsolutePath());
                    vocabularyPath = jFileChooser.getSelectedFile().getAbsolutePath();
                    textField_vocabulaire.setText(vocabularyPath);
                }
            }
        });

        textField_classifiers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = jFileChooser.showOpenDialog(MainForm.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    System.out.println("Classifiers filename : " + jFileChooser.getSelectedFile().getAbsolutePath());
                    classifiersPath = jFileChooser.getSelectedFile().getAbsolutePath();
                    textField_classifiers.setText(classifiersPath);
                }
            }
        });

        textField_indexJson.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int ret = jFileChooser.showOpenDialog(MainForm.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    System.out.println("Index json filename : " + jFileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        startTrainingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //textArea_log.append("Starting training...\n");
                trainingPath = "D:\\UserData\\Documents\\IA\\BagOfWordsSIFT\\BagOfWordsSIFT\\TrainImage";
                vocabularyPath = "D:\\UserData\\Documents\\IA\\Car200_2";
                classifiersPath = "D:\\UserData\\Documents\\IA\\Car200_2\\Classifiers";
                long startMs = System.currentTimeMillis();
                System.out.println("Starting training...");
                LogoFinder logoFinder = new LogoFinder(trainingPath);
                logoFinder.setVocabularyDir(vocabularyPath);
                logoFinder.setClassifierDir(classifiersPath);
                logoFinder.setMaxWords(200);
                logoFinder.train();
                System.out.println("Training completed in " + (System.currentTimeMillis() - startMs) + " ms");
            }
        });

        button_testPrediction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                trainingPath = "D:\\UserData\\Documents\\IA\\BagOfWordsSIFT\\BagOfWordsSIFT\\TrainImage";
                vocabularyPath = "D:\\UserData\\Documents\\IA\\Car200_2";
                classifiersPath = "D:\\UserData\\Documents\\IA\\Car200_2\\Classifiers";
                LogoFinder logoFinder = new LogoFinder();
                String md = logoFinder.getHashMd5(vocabularyPath + "\\vocab.yml");
                System.out.println("Hash of vocab : " + md);
                /*long startMs = System.currentTimeMillis();
                System.out.println("Starting prediction...");
                LogoFinder logoFinder = new LogoFinder(trainingPath);
                logoFinder.setClassifierDir(classifiersPath);
                logoFinder.setVocabularyDir(vocabularyPath);
                //String pred = logoFinder.predict("Coca_10.jpg");
                //System.out.println("Prediction completed in " + (System.currentTimeMillis() - startMs) + " ms");
                //System.out.println("Oh shit, a " + pred + " !");*/
            }
        });
    }
}
