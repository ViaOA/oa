package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.hifive.model.oa.Card;

public class HubSampleTest extends OAUnitTest {

    @Test
    public void test() {

        Hub<Card> hubCard = new Hub<>();
        for (int i=0; i<10; i++) {
            Card card = new Card();
            card.setName("card"+i);
            hubCard.add(card);
        }
        Hub<Card> hubSample = new Hub<>();
        new HubSample(hubCard, hubSample, 5);
        
        assertEquals(5, hubSample.size());
        
        for (int i=0; i<5; i++) {
            assertEquals(hubCard.getAt(i), hubSample.getAt(i));
        }
        
        hubCard.sort("name desc");
        assertEquals(5, hubSample.size());
        for (int i=0; i<5; i++) {
            assertEquals(hubCard.getAt(i), hubSample.getAt(i));
        }
        
        Card cardx = hubCard.getAt(3);
        
        hubCard.remove(3);
        assertEquals(5, hubSample.size());
        for (int i=0; i<5; i++) {
            assertEquals(hubCard.getAt(i), hubSample.getAt(i));
        }

        hubCard.insert(cardx, 1);
        assertEquals(5, hubSample.size());
        for (int i=0; i<5; i++) {
            assertEquals(hubCard.getAt(i), hubSample.getAt(i));
        }
        
        for ( ; hubCard.size() > 3; ) hubCard.remove(0);
        
        assertEquals(3, hubSample.size());
        for (int i=0; i<5; i++) {
            assertEquals(hubCard.getAt(i), hubSample.getAt(i));
        }
        
        
    }
    
}
