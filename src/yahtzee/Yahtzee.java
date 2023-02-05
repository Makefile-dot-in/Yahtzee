    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package yahtzee;

import java.awt.CardLayout;
import java.awt.Container;
import java.util.Arrays;
import java.awt.EventQueue;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import yahtzee.Scorecard.ControllerWrapper;

/**
 *
 * @author kgaritis
 */
public class Yahtzee extends javax.swing.JFrame 
        implements GameController.GameUI, ControllerWrapper, EndScreen.Handler {

    /**
     * Creates new form Yahtzee
     */
    public Yahtzee() {
        initComponents();
        for (int i = 0; i < dice.length; i++) {
            dice[i] = new Die();
            diceholder.add(dice[i]);
        }
        playerList1.setActiveidx(0);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startScreen1 = new yahtzee.StartScreen();
        playArea = new javax.swing.JPanel();
        diceholder = new javax.swing.JPanel();
        rollbutton = new javax.swing.JButton();
        scorecard1 = new yahtzee.Scorecard();
        jScrollPane2 = new javax.swing.JScrollPane();
        playerList1 = new yahtzee.PlayerList();
        quitGame = new javax.swing.JButton();
        endScreen1 = new yahtzee.EndScreen();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.CardLayout());

        startScreen1.setCallback(this::setController);
        getContentPane().add(startScreen1, "start");

        diceholder.setLayout(new java.awt.GridLayout(1, 5, 0, 30));

        rollbutton.setText("Roll");
        rollbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rollbuttonActionPerformed(evt);
            }
        });

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setMinimumSize(new java.awt.Dimension(0, 180));

        playerList1.setPlayerNames(new String[] {"Player1", "Player2"});
        playerList1.setLayout(new java.awt.GridLayout(10, 0, 0, 20));
        jScrollPane2.setViewportView(playerList1);

        quitGame.setText("Quit Game");
        quitGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitGameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout playAreaLayout = new javax.swing.GroupLayout(playArea);
        playArea.setLayout(playAreaLayout);
        playAreaLayout.setHorizontalGroup(
            playAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(playAreaLayout.createSequentialGroup()
                .addGroup(playAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(playAreaLayout.createSequentialGroup()
                        .addComponent(diceholder, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(quitGame))
                    .addComponent(rollbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scorecard1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
        );
        playAreaLayout.setVerticalGroup(
            playAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(playAreaLayout.createSequentialGroup()
                .addGroup(playAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(diceholder, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(playAreaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(quitGame)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rollbutton)
                .addGap(18, 18, 18)
                .addComponent(scorecard1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        getContentPane().add(playArea, "play");

        endScreen1.setHandler(this);
        getContentPane().add(endScreen1, "endscr");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rollbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rollbuttonActionPerformed
        // TODO add your handling code here:
        boolean[] toggled = new boolean[dice.length];
        for (int i = 0; i < dice.length; i++) toggled[i] = dice[i].isSelected();
        invokectrl(c->c.onClickRoll(toggled));
    }//GEN-LAST:event_rollbuttonActionPerformed

    private void quitGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitGameActionPerformed
        // TODO add your handling code here:
        showCard("start");
        ctrl.quitGame();
    }//GEN-LAST:event_quitGameActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Yahtzee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Yahtzee().setVisible(true);
        });
    }

    void setController(GameController ctrl) {
        this.ctrl = ctrl;
        scorecard1.setHost(this);
        ctrl.setUI(this);
    }
    
    private void showCard(String name) {
        Container contentp = getContentPane();
        CardLayout cl = (CardLayout)contentp.getLayout();
        cl.show(contentp, name);
    }
    
    @Override
    public void setActivePlayer(int idx) {
        EventQueue.invokeLater(() -> {
            playerList1.setActiveidx(idx);
            for (Die d : dice) d.setSelected(false);
        });
    }

    @Override
    public void startGame(String[] playerNames) {
        EventQueue.invokeLater(() -> {
            playerList1.removeAll();
            scorecard1.clearScorecard();
            showCard("play");
            playerList1.setPlayerNames(playerNames);
            playerList1.setActiveidx(0);
        });
    }

    @Override
    public void setRollingEnabled(boolean s) {
        EventQueue.invokeLater(() -> rollbutton.setEnabled(s));
    }

    @Override
    public void setScorecardInteractable(boolean s) {
        EventQueue.invokeLater(() -> scorecard1.setEnabled(s));
    }

    @Override
    public void setDiceState(int[] dice) {
        EventQueue.invokeLater(() -> { 
            for (int i = 0; i < dice.length; i++) this.dice[i].setN(dice[i]); 
        });
    }

    @Override
    public void showPotentialScores(int[] score) {
        EventQueue.invokeLater(() -> scorecard1.showPotentialScore(score));
    }

    @Override
    public void showCurrentScores(int[] score) {
        EventQueue.invokeLater(() -> scorecard1.showCurrentScore(score));
    }

    @Override
    public void setPlayerStats(int idx, int pts, int up, int down) {
        EventQueue.invokeLater(() -> playerList1.setPlayerStats(idx, pts, up, down));
    }
    
    
    @Override
    public void error(String msg) {
        EventQueue.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.WARNING_MESSAGE));
    }

    @Override
    public void fatal(String msg) {
        EventQueue.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, msg, "Fatal", JOptionPane.ERROR_MESSAGE);
                showCard("start");
        });
    }
    
    @Override
    public void invokectrl(Consumer<GameController> c) {
        try {
            c.accept(ctrl);
        } catch (AssertionError e) {
            error(e.getMessage());
        }
    }
    
    private GameController ctrl = null;
    Die[] dice = new Die[5];
    private int turn;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel diceholder;
    private yahtzee.EndScreen endScreen1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel playArea;
    private yahtzee.PlayerList playerList1;
    private javax.swing.JButton quitGame;
    private javax.swing.JButton rollbutton;
    private yahtzee.Scorecard scorecard1;
    private yahtzee.StartScreen startScreen1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void showEndScreen(String[] name, short[] pts) {
        endScreen1.showResults(name, pts);
        showCard("endscr");
    }
    

    @Override
    public void switchToStartScreen() {
        showCard("start");
    }

}
