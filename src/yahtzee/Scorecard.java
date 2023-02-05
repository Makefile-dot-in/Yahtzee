/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yahtzee;

import java.awt.GridBagLayout;
import java.util.function.Consumer;
import javax.swing.JPanel;
import yahtzee.Combinations.Category;

/**
 *
 * @author user
 */
public class Scorecard extends javax.swing.JPanel {

    public static interface ControllerWrapper {
        public void invokectrl(Consumer<GameController> f);
    }
    /**
     * Creates new form Scorecard
     */
    public Scorecard() {
        initComponents();
        
        Category[] cats = Category.values();
        scoreboxes = new Scorebox[cats.length];
        int lsi = Category.LOWER_START.ordinal();
        for (int i = 0; i < cats.length; i++) {
            JPanel panel = i < lsi ? usection : lsection;
            Scorebox s = new Scorebox(this, cats[i]);
            scoreboxes[i] = s;
            panel.add(s);
        }
    }
    


    
    public void showCurrentScore(int[] score) {
        for (int i = 0; i < scoreboxes.length; i++) 
            scoreboxes[i].showCurrentScore(score[i]);
        bonus.setText(String.valueOf(Math.max(0, score[scoreboxes.length])));
        
    }
    
    public void showPotentialScore(int[] score) {
        for (int i = 0; i < scoreboxes.length; i++)
            scoreboxes[i].showPotentialScore(score[i]);
    }
    
    public void clearScorecard() {
        for (Scorebox sb : scoreboxes) sb.clearScore();
    }


    public void onCategorySelected(Category cat) {
        ctrl.invokectrl(c->c.onCategorySelected(cat));
    }

    public void setHost(ControllerWrapper shost) { this.ctrl = shost; }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        usection = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lsection = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        bonus = new javax.swing.JLabel();

        jLabel2.setText("Upper Section");

        usection.setMaximumSize(new java.awt.Dimension(0, 100));
        usection.setMinimumSize(new java.awt.Dimension(0, 100));
        usection.setName("usection"); // NOI18N
        usection.setPreferredSize(new java.awt.Dimension(0, 100));
        usection.setLayout(new java.awt.GridLayout(1, 6, 20, 0));

        jLabel1.setText("Lower Section");

        lsection.setName("lsection"); // NOI18N
        lsection.setPreferredSize(new java.awt.Dimension(0, 100));
        lsection.setLayout(new java.awt.GridLayout(1, 7, 20, 0));

        jLabel3.setForeground(new java.awt.Color(0, 153, 0));
        jLabel3.setText("BONUS:");

        bonus.setForeground(new java.awt.Color(0, 153, 0));
        bonus.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lsection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bonus)))
                        .addGap(0, 353, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usection, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lsection, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(bonus)))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    private int[] score;
    private Scorebox[] scoreboxes;
    private ControllerWrapper ctrl;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bonus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel lsection;
    private javax.swing.JPanel usection;
    // End of variables declaration//GEN-END:variables
}