/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yahtzee;

import java.nio.ByteBuffer;
import org.junit.Test;
import yahtzee.NetworkProtocol.Decoder;

/**
 *
 * @author user
 */
public class SerializationTest {
    private final byte[] test1 = {0x00,0x02,0x00,0x04,0x44,0x6f,0x6e,0x69,0x00,0x03,0x6f,0x6f,0x62};
    public SerializationTest() {
        
    }
    @Test
    public void test1() {
        String[] result = (String[])Decoder.decode(ByteBuffer.wrap(test1), String[].class);
        String[] expected = {"Doni", "oob"};
        org.junit.Assert.assertArrayEquals("test 1:", result, expected);
    }
    
}
