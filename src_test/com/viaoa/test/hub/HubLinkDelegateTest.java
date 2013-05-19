package com.viaoa.test.hub;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.vetplan.oa.*;

public class HubLinkDelegateTest {

	void testLink() {
		Hub hubSpecies = new Hub();
		for (int i=0; i<4; i++) {
			Species sp = new Species();
			for (int j=0;j<10; j++) {
				Breed breed = new Breed();
				breed.setName("name"+j);
				sp.getBreeds().add(breed);
			}
			hubSpecies.add(sp);
		}
		
		Hub hubClient = new Hub();
		for (int i=0; i<5; i++) {
			Client c = new Client();
			hubClient.add(c);
			for (int j=0; j<5; j++) {
				Pet p = new Pet();
				Species sp = (Species) hubSpecies.getAt(j);
				p.setSpecies(sp);
				if (sp != null) p.setBreed(((Breed)sp.getBreeds().getAt(j)).getName());
				c.getPets().add(p);
			}			
		}
		
		Hub hubPet = hubClient.getDetailHub(Client.PROPERTY_Pets);
		Hub hubPetLink = hubPet.createSharedHub();
		hubPetLink.setLinkHub(hubClient, Client.PROPERTY_SpecialPet);
		hubSpecies.setLinkHub(hubPet, Pet.PROPERTY_Species);
		Hub hubBreed = hubSpecies.getDetailHub(Species.PROPERTY_Breeds);
		hubBreed.setLinkHub(Breed.PROPERTY_Name, hubPet, Pet.PROPERTY_Breed);
		
		hubClient.setPos(0);
		Client c = (Client) hubClient.getAO();
		
		if (c.getSpecialPet() != hubPetLink.getAO()) error("1");
		c.setSpecialPet((Pet)c.getPets().getAt(0));
		if (c.getSpecialPet() != hubPetLink.getAO()) error("2");
		
		Pet pet = (Pet) hubPet.getAt(2);
		hubPet.setAO(pet);
		if (pet.getSpecies() != hubSpecies.getAO()) error("3");
		Breed b = (Breed) hubBreed.getAO();
		if (pet.getBreed() != (b==null?null:b.getName())) error("4");
		
		Species sp = (Species) hubSpecies.getAt(3);
		pet.setSpecies(sp);
		if (sp != hubSpecies.getAO()) error("5");
		
	}
	
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		try {
			testLink();
		}
		catch (Exception e) {
			System.out.println("Error: in "+methodName+" "+e);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HubLinkDelegateTest test = new HubLinkDelegateTest();
		test.test();
		System.out.println("HubLinkDelegateTest done");
	}

	
}
