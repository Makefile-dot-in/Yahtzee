/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package yahtzee;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import static yahtzee.Combinations.Category.*;
import static yahtzee.Combinations.*;
/**
 *
 * @author kgaritis
 */
public class CombinationsTest {
    public CombinationsTest() {
    }

    public int m(Category c) { return 1 << c.ordinal(); }
    public int[] a(int... a) { return a; }
    
    public void assertLowerPoints(int idx, int[] dice, int mask, int points) {
        int sum = 0;
        for (int d : dice) sum += d;
        int[] expected = {sum, sum, 25, 30, 40, 50, sum};
        
        int[] ps = points(dice);

        int lsi = LOWER_START.ordinal();
        for (int i = lsi; i < ps.length; i++) {
            if (((mask >> i) & 1) == 0) continue;
            int expectedpts = ((points >> i) & 1) == 1 ? expected[i-lsi] : 0;
            assertEquals(idx+": testing " + Category.values()[i].toString(), expectedpts, ps[i]);
        }
    }
    
    public void assertLowerPoints(int idx, int[] dice, int mask) {
        assertLowerPoints(idx, dice, mask, mask);
    }
    
    public void assertHigherPoints(int idx, int[] dice, int[] expected) {
        int lsi = LOWER_START.ordinal();
        Category[] cats = Category.values();
        int[] ps = points(dice);
        for (int i = 0; i < lsi; i++)
            assertEquals(idx+": testing "+cats[i].toString(), (i+1)*expected[i], ps[i]);
    }
    
    @Test
    public void testStraights() {
        int i = 0;
        int mask = m(L_STRAIGHT) | m(S_STRAIGHT);
        assertLowerPoints(i++, a(1, 2, 3, 4, 5), mask);
        assertLowerPoints(i++, a(4, 1, 2, 3, 4), mask, m(S_STRAIGHT));
        assertLowerPoints(i++, a(2, 2, 3, 1, 4), mask, m(S_STRAIGHT));
        assertLowerPoints(i++, a(6, 2, 4, 3, 5), mask);
        assertLowerPoints(i++, a(1, 2, 3, 3, 5), mask, 0);
    }
    
    @Test
    public void testKinds() {
        int i = 0;
        int mask = m(THREE_K) | m(FOUR_K) | m(F_HOUSE) | m(YAHTZEE);
        assertLowerPoints(i++, a(1, 1, 1, 1, 1), mask, m(THREE_K) | m(FOUR_K) | m(YAHTZEE));
        assertLowerPoints(i++, a(2, 2, 3, 3, 3), mask, m(THREE_K) | m(F_HOUSE));
        assertLowerPoints(i++, a(4, 4, 4, 4, 6), mask, m(THREE_K) | m(FOUR_K));
        assertLowerPoints(i++, a(3, 3, 3, 1, 2), mask, m(THREE_K));
        assertLowerPoints(i++, a(3, 3, 2, 2, 1), mask, 0);
    }
    @Test
    public void testChance() {
        int mask = m(CHANCE);
        int i = 0;
        assertLowerPoints(i++, a(2, 3, 4, 5, 6), mask);
        assertLowerPoints(i++, a(4, 5, 6, 3, 2), mask);
        assertLowerPoints(i++, a(4, 3, 5, 6, 3), mask);
    }
    
    @Test
    public void testHigher() {
        int i = 0;
        assertHigherPoints(i++, a(5, 4, 3, 2, 4), a(0, 1, 1, 2, 1, 0));
        assertHigherPoints(i++, a(3, 4, 5, 6, 1), a(1, 0, 1, 1, 1, 1));
        assertHigherPoints(i++, a(4, 5, 4, 6, 6), a(0, 0, 0, 2, 1, 2));
        assertHigherPoints(i++, a(1, 1, 1, 3, 3), a(3, 0, 2, 0, 0, 0));
    }
}
