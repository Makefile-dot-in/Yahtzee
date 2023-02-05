/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package yahtzee;

import yahtzee.Combinations.Category;

/**
 *
 * @author kgaritis
 */
public interface GameController {

    static final int DICE_NO = 5;
    static final int SCORE_LENGTH = Category.values().length + 1;

    interface GameUI {
        void setActivePlayer(int idx);
        void startGame(String[] playerNames);
        void setRollingEnabled(boolean s);
        void setScorecardInteractable(boolean s);
        void setDiceState(int[] dice);
        void showPotentialScores(int[] score);
        void showCurrentScores(int[] score);
        void setPlayerStats(int idx, int pts, int up, int down);
        void error(String msg);
        void fatal(String msg);
        void showEndScreen(String[] name, short[] pts);

        default void destroy() {}
    }

    void setUI(GameUI ui);
    void onClickRoll(boolean[] toggled);
    void onCategorySelected(Category cat);
    void quitGame();
    default void destroy() {}
}
