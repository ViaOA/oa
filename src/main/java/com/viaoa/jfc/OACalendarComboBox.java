package com.viaoa.jfc;

import java.util.concurrent.atomic.AtomicBoolean;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.OADateComboBox;
import com.viaoa.jfc.OATextField;
import com.viaoa.jfc.model.CalendarDate;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectUniqueDelegate;
import com.viaoa.util.OADate;

/**
 * 20180629
 *
 * used by OAObjects that have a link to an object that has a Date property for Calendar.
 * 
 * see OABuilder objectdef.calendar=true
 * 
 * @author vvia
 */
public class OACalendarComboBox extends OADateComboBox {
    
    /**
     * 
     * @param hub
     * @param propertyName that is for an link object that has a 
     * @param columns
     */
	public OACalendarComboBox(final Hub<? extends OAObject> hub, final String linkName, final String datePropertyName, int columns) {
		super(new Hub(CalendarDate.class), CalendarDate.PROPERTY_Date, columns);

		// temp object used to have combo work with oaobj
		final CalendarDate calendarDate = new CalendarDate();
		final Hub hubCalendarDate = getHub();
		hubCalendarDate.add(calendarDate);
		hubCalendarDate.setPos(0);

		final OAObjectInfo oi = hub.getOAObjectInfo();
		final OALinkInfo li = oi.getLinkInfo(linkName);
		
		final AtomicBoolean abIgnore = new AtomicBoolean(); 
		
		hub.addHubListener(new HubListenerAdapter() {
			@Override
			public void afterChangeActiveObject(HubEvent evt) {
			    update();
			}
            void update() {
                if (abIgnore.get()) return;
                try {
                    abIgnore.set(true);
                }
                finally {
                    abIgnore.set(false);
                }
            }
			void _update() {
                OAObject obj = hub.getAO();
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

                OAObject obj = hub.getAO();
                if (obj == null) return;
                
				if (date == null) {
                    obj.setProperty(linkName, null);
	                return;
				}
				
				// find/create
				OAObject objx = OAObjectUniqueDelegate.getUnique(li.getToClass(), datePropertyName, date, true);
                obj.setProperty(linkName, objx);
			}
		});
		
		OATextField txt = new OATextField(hubCalendarDate, CalendarDate.PROPERTY_Date);
		setEditor(txt);
	}

}
