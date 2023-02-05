/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package yahtzee;

import java.util.Arrays;
import java.util.stream.IntStream;
/**
 *
 * @author kgaritis
 */
public class Combinations {
    public static enum Category {
        ACES, TWOS, THREES, FOURS, FIVES, SIXES,
        THREE_K, FOUR_K, F_HOUSE, S_STRAIGHT, L_STRAIGHT, YAHTZEE, CHANCE;
        
        static final Category LOWER_START = THREE_K; 
        
        
        public int idx(int[] a) {
            return a[this.ordinal()];
        }
        
        @Override
        public String toString() {
            String[] names = {
                "Aces", "Twos", "Threes", "Fours", "Fives", "Sixes",
                "Three Of a Kind", "Four Of a Kind", "Full House", 
                "Small Straight", "Large Straight", "Yahtzee", "Chance"
            };
            return names[this.ordinal()];
        }
    }

    static int[] frequencies(int[] input) {
        int[] retval = new int[7];
        for (int i = 0; i < input.length; i++) retval[input[i]]++;
        return retval; 
    }

    static boolean sequential(int[] a, int i, int n) {
        a = a.clone();
        Arrays.sort(a);
        n--;
        for (; i < n; i++) if (a[i+1] - a[i] != 1) return false;
        return true;
    }
    static IntStream s(int[] a) { return Arrays.stream(a); }
    @FunctionalInterface
    static interface CombinationTest {
        int test(int[] f, int[] a, int sum);
    }
    static CombinationTest[] lowerCombinations = {
        (f, a, sum) -> s(f).max().getAsInt() >= 3 ? sum : 0,
        (f, a, sum) -> s(f).max().getAsInt() >= 4 ? sum : 0,
        (f, a, sum) -> s(f).anyMatch(n -> n == 2) && s(f).anyMatch(n -> n == 3) ? 25 : 0,
        (f, a, sum) -> IntStream.rangeClosed(0, 1).anyMatch(i -> sequential(a, i, 4)) ? 30 : 0,
        (f, a, sum) -> sequential(a, 0, 5) ? 40 : 0,
        (f, a, sum) -> s(f).max().getAsInt() == 5 ? 50 : 0,
        (f, a, sum) -> sum
    };

    public static int[] points(int[] dice) {
        int sum = Arrays.stream(dice).sum();
        int[] freq = frequencies(dice);
        Category[] categories = Category.values();
        int[] retval = new int[categories.length];
        int lsi = Category.LOWER_START.ordinal(); // lower start index
        for (int i = 1; i <= lsi; i++) retval[i-1] = i*freq[i];
        for (int i = lsi; i < retval.length; i++) 
            retval[i] = lowerCombinations[i-lsi].test(freq, dice, sum);
        return retval;
    }
}
