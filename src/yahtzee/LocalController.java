/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package yahtzee;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import yahtzee.Combinations.Category;

/**
 *
 * @author kgaritis
 */
public class LocalController implements GameController {
    public LocalController(String[] playerNames) {
        this.playerNames = playerNames;
        scores = new int[playerNames.length][SCORE_LENGTH];
        playerfinished = new boolean[playerNames.length];
        for (int i = 0; i < playerNames.length; i++) 
            for (int j = 0; j < SCORE_LENGTH; j++) 
                scores[i][j] = -1;
    }



    @Override
    public void setUI(GameUI ui) {
        if (playerNames.length == 0) ui.fatal("There must be at least one player");
        HashSet<String> s = new HashSet<>();
        for (String name : playerNames) {
            if (s.contains(name)) {
                ui.fatal("There cannot be two players with the same name");
                return;
            }
            s.add(name);
        }
        this.ui = ui;
        ui.startGame(playerNames);
    }
    @Override
    public void onClickRoll(boolean[] toggled) {
        assert !finished : "Game finished.";
        assert toggled.length == DICE_NO : "Invalid number of dice.";
        turn++;
        assert turn <= 3 : "Cannot roll more than 3 times in one turn.";
        if (turn >= 3) ui.setRollingEnabled(false);
        for (int i = 0; i < DICE_NO; i++) {
            if (toggled[i] && dice[i] != 0) continue;
            dice[i] = die.nextInt(6) + 1;
        }
        ui.setDiceState(dice);
        processDice();
    }

    private void processDice() {
        pscore = Combinations.points(dice);
        int[] score = cScore();
        if (Category.YAHTZEE.idx(pscore) > 0 && Category.YAHTZEE.idx(score) > 0) {
            score[Category.YAHTZEE.ordinal()] += 100;
            pscore[Category.F_HOUSE.ordinal()] = 25;
            pscore[Category.S_STRAIGHT.ordinal()] = 30;
            pscore[Category.L_STRAIGHT.ordinal()] = 40;
        }
        ui.showPotentialScores(pscore);
    }

    // returns the current score.
    private int[] cScore() { return scores[activePlayer]; }
    
    public void addUpperBonus() {
       int[] score = cScore();
       int cnum = SCORE_LENGTH - 1;
       int sum = 0;

       if (score[cnum] > 0) return;
       for (Category c : Category.values()) {
           if (c == Category.LOWER_START) break;
           sum += Math.max(0, c.idx(score));
       }
       if (sum >= 63) score[cnum] = 35;
    }

    @Override
    public void onCategorySelected(Category cat) {
        assert !finished : "game finished";
        int[] score = cScore();
        int nscore = cat.idx(pscore);
        assert cat.idx(score) >= 0 : "Cannot select a category that has already been filled in.";
        score[cat.ordinal()] = nscore;
        addUpperBonus();
        TimerTask turnFinished = new TimerTask() {
            @Override
            public void run() {
                nextPlayer();
                turn = 0;
                onClickRoll(new boolean[DICE_NO]);
            }
        };
        timer.schedule(turnFinished, 1000);
        ui.showCurrentScores(score);
    }
    
    public void updatePlayerStats() {
        int[] score = cScore();
        int sum = 0;
        int upper = 0;
        int lower = 0;
        int lsi = Category.LOWER_START.ordinal();
        int end = Category.values().length;
        for (int i = 0; i < score.length; i++) {
            if (score[i] < 0) continue;

            sum += score[i];
            if      (i < lsi) upper++;
            else if (i < end) lower++;
        }
        ui.setPlayerStats(activePlayer, sum, upper, lower);
    }
    
    private static class EndScreenEntry {
        public EndScreenEntry(String name, short pts) {
            this.name = name;
            this.pts = pts;
        }
        String name;
        short pts;
    }
    
    private boolean gameFinished() {
        int end = Category.values().length;
        EndScreenEntry[] entries = new EndScreenEntry[scores.length];
        for (int i = 0; i < scores.length; i++) {
            entries[i] = new EndScreenEntry(playerNames[i], (short)0);
            for (int j = 0; j < scores[i].length; j++) {
                if (j < end && scores[i][j] < 0) return false;
                if (scores[i][j] < 0) continue;
                entries[i].pts += scores[i][j];
            }
        }
        Arrays.sort(entries, (a, b) -> b.pts - a.pts);
        String[] names = new String[entries.length];
        short[] pts = new short[entries.length];
        for (int i = 0; i < entries.length; i++) {
            names[i] = entries[i].name;
            pts[i] = entries[i].pts;
        }
        ui.showEndScreen(names, pts);
        quitGame();
        return true;
    }
    
    private boolean playerFinished(int idx) {
        if (playerfinished[idx]) return true;
        int end = Category.values().length;
        for (int j = 0; j < end; j++) if (scores[idx][j] < 0) return false;
        return playerfinished[idx] = true;
    }
    
    public void nextPlayer() {
        updatePlayerStats();
        if (gameFinished()) return;
        for (int i = 0; i < pscore.length; i++) pscore[i] = 0;
        do {
            activePlayer++;
            activePlayer %= playerNames.length;
        } while (playerFinished(activePlayer));
        ui.setActivePlayer(activePlayer);
        ui.setRollingEnabled(true);
        ui.showCurrentScores(cScore());
    }

    @Override
    public synchronized void quitGame() {
        timer.cancel();
        finished = true;
    }
    
    private boolean finished = false;
    private final boolean[] playerfinished;
    private int turn;
    private int activePlayer;
    private final int[][] scores;
    private int[] pscore = new int[SCORE_LENGTH-1];
    private final int[] dice = new int[DICE_NO];
    private final String[] playerNames;
    private GameUI ui;
    private final Random die = new Random();
    private final Timer timer = new Timer();
}
