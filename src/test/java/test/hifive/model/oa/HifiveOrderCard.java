// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;
 
@OAClass(
    shortName = "hoc",
    displayName = "Hi5 Order Card",
    displayProperty = "card.name"
)
@OATable(
    indexes = {
        @OAIndex(name = "HifiveOrderCardHifiveOrder", columns = { @OAIndexColumn(name = "HifiveOrderId") })
    }
)
public class HifiveOrderCard extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_PointsUsed = "PointsUsed";
    public static final String P_PointsUsed = "PointsUsed";
    public static final String PROPERTY_CompletedDate = "CompletedDate";
    public static final String P_CompletedDate = "CompletedDate";
     
     
    public static final String PROPERTY_Card = "Card";
    public static final String P_Card = "Card";
    public static final String PROPERTY_HifiveOrder = "HifiveOrder";
    public static final String P_HifiveOrder = "HifiveOrder";
     
    protected int id;
    protected OADate created;
    protected int seq;
    protected double pointsUsed;
    protected OADate completedDate;
     
    // Links to other objects.
    protected transient Card card;
    protected transient HifiveOrder hifiveOrder;
     
    public HifiveOrderCard() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public HifiveOrderCard(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        fireBeforePropertyChange(P_Id, this.id, newValue);
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    @OAProperty(defaultValue = "new OADate()", displayLength = 8, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreated() {
        return created;
    }
    
    public void setCreated(OADate newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADate old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(displayLength = 5, isAutoSeq = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int newValue) {
        fireBeforePropertyChange(P_Seq, this.seq, newValue);
        int old = seq;
        this.seq = newValue;
        firePropertyChange(P_Seq, old, this.seq);
    }
    @OAProperty(displayName = "Points Used", decimalPlaces = 2, displayLength = 7, hasCustomCode = true)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getPointsUsed() {
        return pointsUsed;
    }
    public void setPointsUsed(double newValue) {
        if (!isLoading()) {
            HifiveOrder cardOrder = getHifiveOrder();
            if (cardOrder == null) throw new RuntimeException("CardOrder must be assigned before issuing pointsUsed");
            
            Employee emp = cardOrder.getEmployee();
            if (emp == null) throw new RuntimeException("employee must be assigned before issuing pointsUsed");
            
            double x = emp.getInspirePointsBalance();
            x += this.pointsUsed;
            if (newValue > x) {
                throw new RuntimeException("pointsUsed can not be more then "+x);
            }
        }
        
        double old = pointsUsed;
        fireBeforePropertyChange(PROPERTY_PointsUsed, old, newValue);
        this.pointsUsed = newValue;
        firePropertyChange(PROPERTY_PointsUsed, old, this.pointsUsed);
    }
    @OAProperty(displayName = "Completed Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(OADate newValue) {
        fireBeforePropertyChange(P_CompletedDate, this.completedDate, newValue);
        OADate old = completedDate;
        this.completedDate = newValue;
        firePropertyChange(P_CompletedDate, old, this.completedDate);
    }
    @OAOne(
        reverseName = Card.P_HifiveOrderCards, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"CardId"})
    public Card getCard() {
        if (card == null) {
            card = (Card) getObject(P_Card);
        }
        return card;
    }
    
    public void setCard(Card newValue) {
        fireBeforePropertyChange(P_Card, this.card, newValue);
        Card old = this.card;
        this.card = newValue;
        firePropertyChange(P_Card, old, this.card);
    }
    
    @OAOne(
        displayName = "Hi5 Order", 
        reverseName = HifiveOrder.P_HifiveOrderCards, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"HifiveOrderId"})
    public HifiveOrder getHifiveOrder() {
        if (hifiveOrder == null) {
            hifiveOrder = (HifiveOrder) getObject(P_HifiveOrder);
        }
        return hifiveOrder;
    }
    
    public void setHifiveOrder(HifiveOrder newValue) {
        fireBeforePropertyChange(P_HifiveOrder, this.hifiveOrder, newValue);
        HifiveOrder old = this.hifiveOrder;
        this.hifiveOrder = newValue;
        firePropertyChange(P_HifiveOrder, old, this.hifiveOrder);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, HifiveOrderCard.P_Seq, true);
        }
        this.pointsUsed = (double) rs.getDouble(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, HifiveOrderCard.P_PointsUsed, true);
        }
        date = rs.getDate(5);
        if (date != null) this.completedDate = new OADate(date);
        int cardFkey = rs.getInt(6);
        if (!rs.wasNull() && cardFkey > 0) {
            setProperty(P_Card, new OAObjectKey(cardFkey));
        }
        int hifiveOrderFkey = rs.getInt(7);
        if (!rs.wasNull() && hifiveOrderFkey > 0) {
            setProperty(P_HifiveOrder, new OAObjectKey(hifiveOrderFkey));
        }
        if (rs.getMetaData().getColumnCount() != 7) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
