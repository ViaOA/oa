package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.hifive.model.oa.AwardCardOrder;
import test.hifive.model.oa.Card;
import test.hifive.model.oa.Value;
import test.hifive.model.oa.propertypath.CardPP;

public class HubLinkDelegateTest extends OAUnitTest {

    @Test
    public void testLinkPropertyToProperty() {
        Hub<AwardCardOrder> hubAwardCardOrder = new Hub<>(AwardCardOrder.class);
        
        Hub<Card> hubCard = new Hub<>(Card.class);
        
        Card card = new Card();
        hubCard.add(card);
        hubCard.setPos(0);
        for (int i=5; i<50; i+=5) {
            Value val = new Value();
            val.setValue(i);
            card.getValues().add(val);
        }
        
        AwardCardOrder aco = new AwardCardOrder();
        hubAwardCardOrder.add(aco);
        
        Hub<Value> hubValue = hubCard.getDetailHub(CardPP.values().pp);
        hubValue.setLinkHub(Value.P_Value, hubAwardCardOrder, AwardCardOrder.P_Value);

        assertTrue(hubValue.getAO() == null);
        
        hubAwardCardOrder.setPos(0);
        assertTrue(hubValue.getAO() == null);
        
        aco.setValue(10.0);
        
        Value val = hubValue.getAO();
        assertNotNull(val);
        assertEquals(10.0, val.getValue(), 0);
        
        assertEquals(20.0, hubValue.getAt(3).getValue(), 0);

        hubValue.setPos(3);
        assertEquals(20.0, aco.getValue(), 0);
        
        aco.setValue(12.0);
        
        assertEquals(hubCard.getAO(), card);
        assertNull(hubValue.getAO());
        assertEquals(12.0, aco.getValue(), 0);
    }
    
}
