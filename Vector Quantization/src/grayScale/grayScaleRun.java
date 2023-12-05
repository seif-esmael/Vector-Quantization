package grayScale;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
public class grayScaleRun extends javax.swing.JFrame {
    boolean browsed = false;
    int result[][];
    String absolutePath;
    public grayScaleRun() {
        initComponents();
    }
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        browse = new javax.swing.JButton();
        inputText = new javax.swing.JLabel();
        compress = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Vector Quantization Compression/Decompression (Grayscale)");
        jLabel1.setText("Input File:");
        browse.setText("Browse");
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        inputText.setText("none");
        compress.setText("Compress");
        compress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compressActionPerformed(evt);
            }
        });
        jButton1.setText("Decompress");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decompressAction(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(jLabel1)
                                .addGap(130, 130, 130)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(inputText, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(browse, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(18, 18, 18)
                                                                .addComponent(jLabel2)))
                                                .addGap(96, 113, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(compress, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(126, 126, 126))))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(220, 220, 220))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browse)
                                        .addComponent(inputText, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(compress, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(68, 68, 68))
        );
        pack();
    }
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            absolutePath = selectedFile.getAbsolutePath();
            inputText.setText(selectedFile.getName().toString());
            browsed = true;
        }
    }
    private void compressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compressActionPerformed
        // TODO add your handling code here:

        if (browsed == false) {
            JOptionPane.showMessageDialog(this, "You didn't choose a file");
        } else {
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
                result = VectorQuantizationGreyScale.readImage(absolutePath);
                VectorQuantizationGreyScale.compress(result, vectorSize, codebook);
                JOptionPane.showMessageDialog(this, "Compression Done Successfully!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid integers for Vector Size and Codebook.");
            }
        }
    }
    private void decompressAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedDirectory = fileChooser.getSelectedFile();
            String selectedDirectoryPath = selectedDirectory.getAbsolutePath();
            VectorQuantizationGreyScale.decompress(absolutePath, selectedDirectoryPath + "/decompressed.jpg");
            JOptionPane.showMessageDialog(this, "Decompression Done Successfully!");
        }
    }
    private javax.swing.JButton browse;
    private javax.swing.JButton compress;
    private javax.swing.JLabel inputText;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JProgressBar jProgressBar1;
}
class VectorQuantization {
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new grayScaleRun().setVisible(true);
            }
        });
    }
}