package rgb;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class RGBCompressionGUI extends JFrame {
    private boolean browsed = false;
    private int[][][] result;
    private String absolutePath;
    private JLabel jLabel2;
    private JLabel jLabel1;
    private JButton browse;
    private JLabel inputText;
    private JButton compress;
    private JButton decompress;
    private JProgressBar jProgressBar1;
    public RGBCompressionGUI() {
        initComponents();
    }
    private void initComponents() {
        jLabel2 = new JLabel();
        jLabel1 = new JLabel();
        browse = new JButton();
        inputText = new JLabel();
        compress = new JButton();
        decompress = new JButton();
        jProgressBar1 = new JProgressBar();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel2.setText("Vector Quantization Compression/Decompression (ٌRGB)");
        jLabel1.setText("Input File:");
        browse.setText("Browse");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        inputText.setText("none");
        compress.setText("Compress");
        compress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                compressActionPerformed(evt);
            }
        });
        decompress.setText("Decompress");
        decompress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                decompressActionPerformed(evt);
            }
        });
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(jLabel1)
                                .addGap(130, 130, 130)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(inputText, GroupLayout.PREFERRED_SIZE, 310, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(browse, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jLabel2)))
                                                .addGap(96, 113, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(compress, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(decompress, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE)
                                                .addGap(126, 126, 126))))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 262, GroupLayout.PREFERRED_SIZE)
                                .addGap(220, 220, 220))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browse)
                                        .addComponent(inputText, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(compress, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(decompress, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                .addGap(68, 68, 68))
        );
        pack();
    }
    private void browseActionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            absolutePath = selectedFile.getAbsolutePath();
            inputText.setText(selectedFile.getName());
            browsed = true;
        }
    }
    private void compressActionPerformed(ActionEvent evt) {
        if (browsed == false) {
            JOptionPane.showMessageDialog(this, "You didn't choose a file");
        }
        else {
            String vectorSizeInput = JOptionPane.showInputDialog(this, "Enter Vector Size:");
            if (vectorSizeInput == null) {
                return;
            }
            String codebookInput = JOptionPane.showInputDialog(this, "Enter Codebook:");
            if (codebookInput == null) {
                return;
            }

            try {
                int vectorSize = Integer.parseInt(vectorSizeInput);
                int codebook = Integer.parseInt(codebookInput);
                result = VectorQuantizationRGB.readImageColored(absolutePath);
                VectorQuantizationRGB.compress(result, vectorSize, codebook);
                JOptionPane.showMessageDialog(this, "Compression Done Successfully!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid integers for Vector Size and Codebook.");
            }
        }
    }
    private void decompressActionPerformed(ActionEvent evt) {
        if(browsed == false){
            JOptionPane.showMessageDialog(this, "You didn't choose a file");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedDirectory = fileChooser.getSelectedFile();
            String selectedDirectoryPath = selectedDirectory.getAbsolutePath();
            VectorQuantizationRGB.decompress(absolutePath,selectedDirectoryPath + "/decompressed.jpg");
            JOptionPane.showMessageDialog(this, "Decompression Done Successfully!");
        }
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RGBCompressionGUI().setVisible(true);
            }
        });
    }
}