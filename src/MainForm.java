import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import com.sun.prism.paint.Paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;

public class MainForm extends JPanel{

    private JFrame frame;
    private JPanel main_panel;
    private JTextField textField_trainData;
    private JTextField textField_vocabulaire;
    private JTextField textField_classifiers;
    private JTextField textField_indexJson;
    private JTextArea textArea_log;
    private JButton startTrainingButton;

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

        textField_trainData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = jFileChooser.showOpenDialog(MainForm.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    textArea_log.append("Train Data filename : " + jFileChooser.getSelectedFile().getAbsolutePath() + "\n");
                }
            }
        });

        textField_vocabulaire.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int ret = jFileChooser.showOpenDialog(MainForm.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    textArea_log.append("Vocabulary filename : " + jFileChooser.getSelectedFile().getAbsolutePath() + "\n");
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
                    textArea_log.append("Classifiers filename : " + jFileChooser.getSelectedFile().getAbsolutePath() + "\n");
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
                    textArea_log.append("Index json filename : " + jFileChooser.getSelectedFile().getAbsolutePath() + "\n");
                    //System.out.println("Index json filename : " + jFileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        startTrainingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textArea_log.append("Starting training...\n");
            }
        });
    }
}
