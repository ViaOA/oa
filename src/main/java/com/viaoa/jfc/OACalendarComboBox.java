package com.viaoa.jfc;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.OADateComboBox;
import com.viaoa.jfc.OATextField;
import com.viaoa.jfc.model.CalendarDate;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectUniqueDelegate;
import com.viaoa.util.OADate;

/**
 * 20180629
 *
 *  This is used by OABuilder codegen for objects that have calendar=true
 *  
 *  It is used by objects that have a ONE link to an object (calendar) with a date field.  When the
 *  user selects a date, it will find the calendar object where it's date property = input date.
 *  If not found it will create a new one.  It will then set the object.linkONE to this calendar object.
 *  
 *  ex:
 *      Order has a DeliveryDate, which has a date property
 *      Order.deliveryDate = deliveryDate.date=20150114 id=53
 *      - user then selects a new date 20150305
 *      - call OAObjectUniqueDelegate.getUnique(..) to find an existing deliveryDate with date=20150305
 *      -   or create a new instance and set date=20150305
 *      - the returned deliveryDate  id=127 
 *      - set Order.deliveryDate= obj with id=127 
 *  
 *  This will have the UI to allow the user select any date and then to 
 *  get the existing object for that date.  If it does not exist, then it will be created.
 *  
 *  This uses:
 *    OAObjectUniqueDelegate.getUnique() to get/create the new calendar OAObject with date=inputDate 
 *
 * see OABuilder objectdef.calendar=true
 * 
 * @author vvia
 */
public class OACalendarComboBox extends OADateComboBox {
    
    protected final Hub<? extends OAObject> hubMain;
    protected final String linkName;
    protected final String datePropertyName;
    protected final OALinkInfo linkInfo;
    
    
    public OACalendarComboBox(final Hub<? extends OAObject> hubCalendar, final String datePropertyName, int columns) {
        // uses a temp hub
        super(new Hub(CalendarDate.class), CalendarDate.PROPERTY_Date, columns);
        
        linkInfo = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(hubCalendar);
        if (linkInfo == null) throw new RuntimeException("must have a master hub to use calendar combo");
        if (linkInfo.getType() != linkInfo.ONE) throw new RuntimeException("can only be used with link.type=ONE");

        // need to include the real hub
        getController().getEnabledChangeListener().addAoNotNull(hubCalendar);
        getController().addExtendedChecking(hubCalendar, datePropertyName);
        
        this.hubMain = hubCalendar.getMasterHub();
        this.linkName = linkInfo.getName();
        this.datePropertyName = datePropertyName;
    
		// temp object used to have combo work with oaobj
		final CalendarDate calendarDate = new CalendarDate();
		final Hub hubCalendarDate = getHub();
		hubCalendarDate.add(calendarDate);
		hubCalendarDate.setPos(0);

		final AtomicBoolean abIgnore = new AtomicBoolean(); 
		
		hubMain.addHubListener(new HubListenerAdapter() {
			@Override
			public void afterChangeActiveObject(HubEvent evt) {
			    update();
			}
            void update() {
                if (abIgnore.get()) return;
                try {
                    abIgnore.set(true);
                    _update();
                }
                finally {
                    abIgnore.set(false);
                }
            }
			void _update() {
                OAObject obj = hubMain.getAO();
                Object objx;
                if (obj == null) objx = null;
                else {
                    objx = obj.getProperty(linkName);
                    if (objx instanceof OAObject) {
                        objx = ((OAObject)objx).getProperty(datePropertyName);
                    }
                    else objx = null;
                }
                
                if (!(objx instanceof OADate)) objx = null;
                OADate d = (OADate) objx;
                calendarDate.setDate(d);
			}
			@Override
			public void afterPropertyChange(HubEvent e) {
                if (!linkName.equalsIgnoreCase(e.getPropertyName())) return;
                update();
			}
		});
		
		hubCalendarDate.addHubListener(new HubListenerAdapter() {
			@Override
			public void afterPropertyChange(HubEvent e) {
                if (abIgnore.get()) return;
                String prop = e.getPropertyName();
                if (prop == null || !prop.equalsIgnoreCase(CalendarDate.PROPERTY_Date)) return;
                
                try {
                    abIgnore.set(true);
                    update();
                }
                finally {
                    abIgnore.set(false);
                }
			}
			void update() {
				OADate date = calendarDate.getDate();

                OAObject obj = hubMain.getAO();
                if (obj == null) return;
                
				if (date == null) {
                    obj.setProperty(linkName, null);
	                return;
				}
				
				// find/create
				OAObject objx = OAObjectUniqueDelegate.getUnique(linkInfo.getToClass(), datePropertyName, date, true);
                obj.setProperty(linkName, objx);
			}
		});
		
		OATextField txt = new OATextField(hubCalendarDate, CalendarDate.PROPERTY_Date);
		setEditor(txt);
	}

	@Override
	public void customizeTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver) {
	    Object objx = getValue(table, row, column, value);
	    
        if (objx instanceof OADate) {
            OADate d = (OADate) objx;
            value = d;
            String s = d.toString(getFormat());
            renderer.setText(s);
            renderer.setHorizontalAlignment(SwingConstants.LEFT);
        }
        else {
            value = "";
            renderer.setText("");
        }
	    super.customizeTableRenderer(renderer, table, value, isSelected, hasFocus, row, column, wasChanged, wasMouseOver);
	}

	@Override
	public Object getValue(JTable table, int row, int col, Object defaultValue) {
        OAObject oaObj = hubMain.getAt(row);
        boolean bSet = false;
        if (oaObj != null) {
            Object objx = linkInfo.getValue(oaObj);
            if (objx instanceof OAObject) {
                objx = ((OAObject) objx).getProperty(datePropertyName);
                if (objx instanceof OADate) {
                    defaultValue = (OADate) objx;
                    bSet = true;
                }
            }
        }
        if (!bSet) {
            defaultValue = null;
        }
        return defaultValue;
	}
	
	@Override
	public String getTablePropertyPath(OATable table) {
	    Hub hubTable1 = table.getHub();
	    Hub hubTable2 = table.getMasterFilterHub();
	    String pp = linkName+"."+datePropertyName;
	    Hub h = hubMain;
	    for ( ; h != null && h != hubTable1 && h != hubTable2; h=h.getMasterHub()) {
	        OALinkInfo li = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(h);
	        if (li == null) break;
	        pp = li.getName()+"."+pp;
	    }
	    return pp;
	}
}
