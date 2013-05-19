package com.viaoa.test;

import java.lang.ref.WeakReference;
import java.util.*;

import javax.swing.JFrame;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;


public class ModelTest {

	Hub hubClients;
	Hub hubUsers;
	Hub hubSpecies;
	Hub hubItems;
	Hub hubItemCategories;
	Hub hubTemplates;
	Hub hubExamItemStatus;
	
	
//qqqqqqqq testing	
	int cId;
	Client cx;
	
	LinkedList ll = new LinkedList();
	
	class Tester {
	    protected String id;
	    protected String firstName;
	    protected String lastName;
	    protected String address1;
	    protected String address2;
	    protected String city;
	    protected String state;
	    protected String zip;

	    protected String country;
	    protected String phone;
	    protected String phone2;
	    protected String pmsId;
	    protected String email;
	    
	    protected transient Hub hubPets;
	    protected transient Hub hubClientAlerts;
	    protected transient Pet specialPet;

	    protected int         guid;          // global identifier for this object
	    protected OAObjectKey objectKey;     // Object identifier, used by Hub/HubController for hashing, etc.
	    protected boolean 	  changedFlag;   // flag to know if this object has been changed
	    protected boolean     newFlag=true;  // flag to know if this object is new (not yet saved).  The object key properties can be changed as long as isNew is true.
	    protected Hashtable   hashNull;      // keeps track of which primitive type propertys are NULL.  Keys are uppercase Strings.
	    protected Hashtable   hashProperty;  // used to store name/value pairs.  Used by get/setProperty.  Key are uppercase Strings, value is object.

	    protected transient int[]           iCascades;      // Used and maintained by OAObjectDelegate for cascading.  Used to know if an object has already been visited within a certain method call. 
	    protected transient WeakReference[] weakHubs;       // list of Hub Collections that this object is a member of.  OAObject uses these Hubs for sending events.  See: OAObjectHubDelegate
	    protected transient boolean         bServerCache;
	}
	public void TEST_createClients(Hub h) {
		for (int i=0; i<200000; i++) {
			if ((i % 500)==0) System.out.println(i+"");

//			ll.add(new Tester());  // 21
//			ll.add(new Client());  // 23m
			
//			ll.add(new Hub());  // 68m
//			ll.add(new Hub(Client.class));  // 71m

//			ll.add(new Hub(Client.class, 300, 100, 297, .90f));  // out of mem
//			ll.add(new Hub(Client.class, 2, 2, 3, .90f));  // 49m 
			
//			ll.add(new Hub(Client.class, 3,5,3,.99f));  // 49m
//			ll.add(new Hub(Client.class, 1,1,1,.99f));  // 47m
			
			
//			Client c = new Client(); // 41
//		c.setProperty("xxx", new Pet()); // 116
//			c.setProperty("xxx", new Hub()); // 206			
//			c.getPets(); // 185
	
Hub c = new Hub(Client.class); // 136
c.add(new Client()); // 190

//Hub c = new Hub(); // 132
//String c = new String(); // 32
			
// client = 250bytes
// hub = 500bytes

			
//			c.setProperty("xxx", new Pet());  // 171m
//			ll.add(new Pet());  // 230
//ll.add(new Hub(Pet.class));
//			h.add(c);
			ll.add(c);
		}	
	}	
	
	
	
	
	public Hub getClients() {
		if (hubClients != null) return hubClients;
		hubClients = new Hub();
		
		for (int i=0; i<10; i++) {
			Client c = new Client("mt"+i);
			// System.out.println(i+") new client");			
			// createPets(null);
			createPets(c.getPets());
			c.setSpecialPet((Pet) c.getPets().elementAt(0));
			hubClients.add(c);
		}
		hubClients.resizeToFit();
		return hubClients;
	}

	int petId;
	public void createPets(Hub h) {
		for (int i=0; i<3; i++) {
			Pet p = new Pet();
			p.setId("mt"+(++petId));
			p.setSpecies(getSpecie());
			p.setBreed("breed2");
			if (h != null) h.add(p);
			// createExams(null);
			createExams(p.getExams());
		}
		if (h != null) h.resizeToFit();
	}
	public void createExams(Hub h) {
		for (int i=0; i<3; i++) {
			Exam e = new Exam();
			e.setReceptionistUser(getUser());
			e.setTechUser(getUser());
			e.setVetUser(getUser());
			if (h != null) h.add(e);
			//createExamItems(null);
			//createExamTemplates(null);
			createExamItems(e.getExamItems());
			createExamTemplates(e.getExamTemplates());
		}
		if (h != null) h.resizeToFit();
	}
	
	public void createExamItems(Hub h) {
		for (int i=0; i<9; i++) {
			ExamItem ei = new ExamItem();
			Item it = getItem();
			ei.setItem(it);
			ei.setExamItemStatus(getExamItemStatus());
			if (h != null) h.add(ei);
			createExamItemHistories(ei.getExamItemHistories());
			//createExamItemHistories(null);
		}
		if (h != null) h.resizeToFit();
	}
	public void createExamItemHistories(Hub h) {
		for (int i=0; i<3; i++) {
			ExamItemHistory eih = new ExamItemHistory();
			eih.setExamItemStatus(getExamItemStatus());
			if (h != null) h.add(eih);
		}
		if (h != null) h.resizeToFit();
	}
	public void createExamTemplates(Hub h) {
		for (int i=0; i<3; i++) {
			ExamTemplate et = new ExamTemplate();
			et.setTemplate(getTemplate());
			if (h != null) h.add(et);
		}
		if (h != null) h.resizeToFit();
	}
	
	public Client getClient() {
		return (Client) getClients().elementAt(getRandom(getClients()));
	}
	public User getUser() {
		return (User) getUsers().elementAt(getRandom(getUsers()));
	}
	public Species getSpecie() {
		return (Species) getSpecies().elementAt(getRandom(getSpecies()));
	}

	private int cntItem;
	public Item getItem() {
		cntItem = getRandom((int) (getItems().getSize()*.70));
		return (Item) getItems().elementAt(cntItem);
	}
	public Item getNextItem() {
		return (Item) getItems().elementAt(++cntItem);
	}

	public ExamItemStatus getExamItemStatus() {
		return (ExamItemStatus) getExamItemStatuses().elementAt(getRandom(getExamItemStatuses()));
	}
	
	public Template getTemplate() {
		return (Template) getTemplates().elementAt(getRandom(getTemplates()));
	}
	
	public ItemCategory getItemCategory() {
		//qqqqqqq recursive, randomly get lower level object		
		return (ItemCategory) getItemCategories().elementAt(getRandom(getItemCategories()));
	}
	public Object[] getAllItemCategories(ItemCategory icParent) {
		ArrayList al = new ArrayList(64);
		getAllItemCategories(al, icParent);
		return al.toArray();
	}
	private void getAllItemCategories(ArrayList al, ItemCategory icParent) {
		al.add(icParent);
		for (int i=0; ;i++) {
			ItemCategory ic = (ItemCategory) icParent.getItemCategories().elementAt(i);
			if (ic == null) break;
			getAllItemCategories(al, ic);
		}
	}
	
	public Object[] getAllTemplateCategories(Species sp) {
		ArrayList al = new ArrayList(64);
		getAllTemplateCategories(al, sp.getTemplateCategories());
		return al.toArray();
	}
	private void getAllTemplateCategories(ArrayList al, Hub hubTemplateCategories) {
		for (int i=0; ;i++) {
			TemplateCategory tc = (TemplateCategory) hubTemplateCategories.elementAt(i);
			if (tc == null) break;
			al.add(tc);
			getAllTemplateCategories(al, tc.getTemplateCategories());
		}
	}
	
	
	
	// Generate Lookups --------------------------------
	public Hub getUsers() {
		if (hubUsers != null) return hubUsers;
		hubUsers = new Hub();
		for (int i=0; i<15; i++) {
			User u = new User(); 
			hubUsers.add(u);
		}
		return hubUsers;
	}
	public Hub getSpecies() {
		if (hubSpecies != null) return hubSpecies;
		hubSpecies = new Hub();
		for (int i=0; i<4; i++) {
			Species sp = new Species(); 
			hubSpecies.add(sp);
			createBreeds(sp.getBreeds());
			createTemplateCategories(sp.getTemplateCategories(), 2);
		}
		return hubSpecies;
	}
	public void createBreeds(Hub h) {
		for (int i=0; i<4; i++) {
			Breed b = new Breed();
			b.setName("breed"+i);
			h.add(b);
		}
		h.resizeToFit();
	}
	public void createTemplateCategories(Hub h, int levels) {
		for (int i=0; i<4; i++) {
			TemplateCategory tc = new TemplateCategory();
			h.add(tc);
			if (levels > 0) createTemplateCategories(tc.getTemplateCategories(), levels-1);
		}
		h.resizeToFit();
	}
		
	public Hub getItems() {
		if (hubItems != null) return hubItems;
		hubItems = new Hub();
		for (int i=0; i<100; i++) {
			Item item = new Item();
			//qqqqqqqqqqqqqq create subItems, etc ......................			
			hubItems.add(item);
		}
		hubItems.resizeToFit();
		return hubItems;
	}
	
	public Hub getExamItemStatuses() {
		if (hubExamItemStatus != null) return hubExamItemStatus;
		hubExamItemStatus = new Hub();
		for (int i=0; i<15; i++) {
			ExamItemStatus eis = new ExamItemStatus();
			hubExamItemStatus.add(eis);
		}
		return hubExamItemStatus;
	}

	
	// Templates
	public Hub getTemplates() {
		if (hubTemplates != null) return hubTemplates;
		hubTemplates = new Hub();
		for (int i=0; i<10; i++) {
			Template t = new Template();
			hubTemplates.add(t);
			createTemplateRows(t.getTemplateRows());
		}
		hubTemplates.resizeToFit();
		return hubTemplates;
	}
	
	public void createTemplateRows(Hub h) {
		for (int i=0; i<2; i++) {
			TemplateRow tr = new TemplateRow(); 
			h.add(tr);
			createSections(tr.getSections());
		}
		h.resizeToFit();
	}
		
	public void createSections(Hub h) {
		for (int i=0; i<2; i++) {
			Section sec = new Section(); 
			h.add(sec);
			createSectionItems(sec.getSectionItems());
		}
		h.resizeToFit();
	}
	
	public void createSectionItems(Hub h) {
		for (int i=0; i<4; i++) {
			SectionItem si = new SectionItem(); 
			h.add(si);
			if (i == 0) createAutoSelects(si.getAutoSelects());
		}
		h.resizeToFit();
	}
	
	public void createAutoSelects(Hub h) {
		for (int i=0; i<3; i++) {
			AutoSelect x = new AutoSelect(); 
			h.add(x);
		}
		h.resizeToFit();
	}
	
	// ItemCategories
	public Hub getItemCategories() {
		if (hubItemCategories != null) return hubItemCategories;
		hubItemCategories = new Hub();
		for (int i=0; i<3; i++) {
			ItemCategory ic = new ItemCategory();
			hubItemCategories.add(ic);
			createItemCategories(ic.getItemCategories(), getRandom(5));
		}
		return hubItemCategories;
	}
	public void createItemCategories(Hub h, int levels) {
		for (int i=0; i<3; i++) {
			ItemCategory ic = new ItemCategory();
			ic.getItems().add(getItem());
			ic.getItems().add(getNextItem());
			ic.getItems().add(getNextItem());
			h.add(ic);
			if (levels > 0) createItemCategories(ic.getItemCategories(), levels-1);
		}
		h.resizeToFit();
	}
	
	
	
	
	
	
	// Helper Methods =============================
	public int getRandom(Hub h) {
		int x = (int) (Math.random() * h.getSize());
		return x;
	}
	public int getRandom(int max) {
		int x = (int) (Math.random() * max);
		return x;
	}

	
	
	

	public static void main(String[] args) throws Exception {
		ModelTest test = new ModelTest();
		Hub h = null;
		for (int i=0; i<1; i++) {
//			h = new Hub(Client.class, 100050, 1000, 100000, .90f);
			
			h = test.getClients();
			if (i==0) break;//qqqqqqqqqq			
			
			
			switch (test.getRandom(5)) {
			case 0: h.saveAll(); break;
			case 1: h.saveAll(OAObject.CASCADE_ALL_LINKS); break;
			case 2: h.saveAll(OAObject.CASCADE_NONE);
			case 3:
			case 4:
				Client c = (Client) h.elementAt(0);
				Pet p = (Pet) c.getPets().elementAt(0);
				Exam ex = (Exam) p.getExams().elementAt(0);
				ex.save();
				ex = (Exam) p.getExams().elementAt(0);
				ex.save();
				p.save();
			}
			System.gc();
		}
		System.gc();
		System.out.println("done");
		System.in.read();
		// (new JFrame()).setVisible(true);
	}

}















