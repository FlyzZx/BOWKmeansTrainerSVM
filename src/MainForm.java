import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import com.sun.prism.paint.Paint;
import fz.imt.LogoFinder;
import fz.imt.java.utils.CustomOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.SocketException;
import java.sql.Timestamp;

public class MainForm extends JPanel{

    private JFrame frame;
    private JPanel main_panel;
    private JTextField textField_trainData;
    private JTextField textField_vocabulaire;
    private JTextField textField_classifiers;
    private JTextArea textArea_log;
    private JButton startTrainingButton;
    private JScrollPane scrollPane;
    private JCheckBox checkBox_ftp;
    private JTextField textField_ftp_hote;
    private JTextField textField_ftp_user;
    private JPasswordField textField_ftp_passwd;
    private JButton button_debug;
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

        startTrainingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //textArea_log.append("Starting training...\n");

                long startMs = System.currentTimeMillis();
                System.out.println("Starting training...");
                LogoFinder logoFinder = new LogoFinder(trainingPath);
                logoFinder.setVocabularyDir(vocabularyPath);
                logoFinder.setClassifierDir(classifiersPath);
                logoFinder.setMaxWords(200);
                logoFinder.train();

                System.out.println("Training completed in " + (System.currentTimeMillis() - startMs) + " ms");
                if(checkBox_ftp.isSelected()) {
                    sendToFtp(logoFinder);
                }
            }
        });

        button_debug.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LogoFinder logoFinder = new LogoFinder();
                logoFinder.setVocabularyDir("D:\\UserData\\Documents\\IA\\Car200_2");
                logoFinder.setClassifierDir("D:\\UserData\\Documents\\IA\\Car200_2\\Classifiers");
                sendToFtp(logoFinder);
            }
        });
    }

    private void sendToFtp(LogoFinder logoFinder) {
        System.out.println("Uploading to server...");
        File vocabulary = new File(logoFinder.getVocabularyDir() + "\\vocab.yml");
        File classifierDirs = new File(logoFinder.getClassifierDir());
        File indexJson = new File(logoFinder.getVocabularyDir() + "\\index.json");

        String host = textField_ftp_hote.getText();
        String user = textField_ftp_user.getText();
        String password = new String(textField_ftp_passwd.getPassword());

        if(host.equals("") || user.equals("")) {
            System.out.println("Wrong FTP host or username");
        }

        if(classifierDirs.listFiles().length > 0 && vocabulary.exists() && indexJson.exists()) {
            System.out.println("Files found, starting upload");
            FTPClient client = new FTPClient();
            try {
                client.connect(host);
                client.login(user, password);

                client.changeWorkingDirectory("/public_html");
                FTPFile[] ftpFile = client.listFiles("Classifiers/");
                for (FTPFile file : ftpFile) {
                    client.deleteFile("Classifiers/" + file.getName());
                }
                client.deleteFile("index.json");
                client.deleteFile("vocab.yml");
                System.out.println("All files removed");

                //Envois des nouveaux fichiers
                FileInputStream inputStream = new FileInputStream(vocabulary);
                client.storeFile("vocab.yml", inputStream);
                inputStream = new FileInputStream(indexJson);
                client.storeFile("index.json", inputStream);
                client.changeWorkingDirectory("Classifiers/");
                for(File classifier : classifierDirs.listFiles()) {
                    inputStream = new FileInputStream(classifier);
                    client.storeFile(classifier.getName(), inputStream);
                }
                System.out.println("All files updated");

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error, can't login");
            }
        }


    }
}
