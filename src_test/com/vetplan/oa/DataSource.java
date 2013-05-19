package com.vetplan.oa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.viaoa.object.*;
import com.viaoa.ds.jdbc.db.*;

import com.vetplan.oa.*;

public class DataSource {
	Database db;
	
	public Database getDatabase() {
		if (db != null) return db;
		
        int NextNumber = 0;
        // TABLES
        int AUTOSELECT = 1;
        int BREED = 2;
        int CLIENT = 3;
        int CLIENTALERT = 4;
        int CLINIC = 5;
        int COMPANY = 6;
        int DICTIONARY = 7;
        int EXAM = 8;
        int EXAMITEM = 9;
        int EXAMITEMHISTORY = 10;
        int EXAMITEMSTATUS = 11;
        int EXAMITEMTASK = 12;
        int EXAMITEMTASKSTEP = 13;
        int EXAMITEMTASKSTEPRESULT = 14;
        int EXAMTEMPLATE = 15;
        int ITEM = 16;
        int ITEMCATEGORY = 17;
        int ITEMDOSAGE = 18;
        int ITEMPRODUCT = 19;
        int ITEMTASK = 20;
        int ITEMTASKSTEP = 21;
        int ITEMTASKSTEPRESULT = 22;
        int ITEMTYPE = 23;
        int LAB = 24;
        int LABTEST = 25;
        int LABTESTRESULT = 26;
        int LABTESTRESULTTYPE = 27;
        int LABTESTSPECIES = 28;
        int LABTESTTYPE = 29;
        int LANGUAGE = 30;
        int MANUFACTURER = 31;
        int PET = 32;
        int PETALERT = 33;
        int PETCOLOR = 34;
        int PETSYSTEM = 35;
        int PROBLEM = 36;
        int PROBLEMSTATUS = 37;
        int PRODUCT = 38;
        int PRODUCTCATEGORY = 39;
        int PRODUCTPACKAGE = 40;
        int RECOMMENDITEM = 41;
        int SCHDATE = 42;
        int SCHDATETIME = 43;
        int SCHDATETIMES = 44;
        int SCHTIME = 45;
        int SECTION = 46;
        int SECTIONITEM = 47;
        int SERVICE = 48;
        int SERVICECATEGORY = 49;
        int SERVICEPRODUCT = 50;
        int SPECIES = 51;
        int TEMPLATE = 52;
        int TEMPLATECATEGORY = 53;
        int TEMPLATEROW = 54;
        int USER = 55;
        int VERSION = 56;
        int VERSIONSTATUS = 57;
        
        // LINK TABLES
        int LINKITEMSITEMCATEGORIES = 58;
        int LINKTEMPLATESTEMPLATECATEGORIES = 59;
        int LINKPRODUCTSPRODUCTCATEGORIES = 60;
        int LINKSERVICESREVERSESERVICES = 61;
        int MAX = 62;
        
        db = new Database();
        Table[] tables = new Table[MAX];
        Column[] columns;
        
        // TABLES
        tables[AUTOSELECT] = new Table("AutoSelect",AutoSelect.class);
        tables[BREED] = new Table("Breed",Breed.class);
        tables[CLIENT] = new Table("Client",com.vetplan.oa.Client.class);
        tables[CLIENTALERT] = new Table("ClientAlert",com.vetplan.oa.ClientAlert.class);
        tables[CLINIC] = new Table("Clinic",Clinic.class);
        tables[COMPANY] = new Table("Company",Company.class);
        tables[DICTIONARY] = new Table("Dictionary",Dictionary.class);
        tables[EXAM] = new Table("Exam",Exam.class);
        tables[EXAMITEM] = new Table("ExamItem",ExamItem.class);
        tables[EXAMITEMHISTORY] = new Table("ExamItemHistory",ExamItemHistory.class);
        tables[EXAMITEMSTATUS] = new Table("ExamItemStatus",ExamItemStatus.class);
        tables[EXAMITEMTASK] = new Table("ExamItemTask",ExamItemTask.class);
        tables[EXAMITEMTASKSTEP] = new Table("ExamItemTaskStep",ExamItemTaskStep.class);
        tables[EXAMITEMTASKSTEPRESULT] = new Table("ExamItemTaskStepResult",ExamItemTaskStepResult.class);
        tables[EXAMTEMPLATE] = new Table("ExamTemplate",ExamTemplate.class);
        tables[ITEM] = new Table("Item",Item.class);
        tables[ITEMCATEGORY] = new Table("ItemCategory",ItemCategory.class);
        tables[ITEMDOSAGE] = new Table("ItemDosage",ItemDosage.class);
        tables[ITEMPRODUCT] = new Table("ItemProduct",ItemProduct.class);
        tables[ITEMTASK] = new Table("ItemTask",ItemTask.class);
        tables[ITEMTASKSTEP] = new Table("ItemTaskStep",ItemTaskStep.class);
        tables[ITEMTASKSTEPRESULT] = new Table("ItemTaskStepResult",ItemTaskStepResult.class);
        tables[ITEMTYPE] = new Table("ItemType",ItemType.class);
        tables[LAB] = new Table("Lab",Lab.class);
        tables[LABTEST] = new Table("LabTest",LabTest.class);
        tables[LABTESTRESULT] = new Table("LabTestResult",LabTestResult.class);
        tables[LABTESTRESULTTYPE] = new Table("LabTestResultType",LabTestResultType.class);
        tables[LABTESTSPECIES] = new Table("LabTestSpecies",LabTestSpecies.class);
        tables[LABTESTTYPE] = new Table("LabTestType",LabTestType.class);
        tables[LANGUAGE] = new Table("Language",Language.class);
        tables[MANUFACTURER] = new Table("Manufacturer",Manufacturer.class);
        tables[PET] = new Table("Pet",Pet.class);
        tables[PETALERT] = new Table("PetAlert",PetAlert.class);
        tables[PETCOLOR] = new Table("PetColor",PetColor.class);
        tables[PETSYSTEM] = new Table("PetSystem",PetSystem.class);
        tables[PROBLEM] = new Table("Problem",Problem.class);
        tables[PROBLEMSTATUS] = new Table("ProblemStatus",ProblemStatus.class);
        tables[PRODUCT] = new Table("Product",Product.class);
        tables[PRODUCTCATEGORY] = new Table("ProductCategory",ProductCategory.class);
        tables[PRODUCTPACKAGE] = new Table("ProductPackage",ProductPackage.class);
        tables[RECOMMENDITEM] = new Table("RecommendItem",RecommendItem.class);
        tables[SCHDATE] = new Table("SchDate",SchDate.class);
        tables[SCHDATETIME] = new Table("SchDateTime",SchDateTime.class);
        tables[SCHDATETIMES] = new Table("SchDateTimes",SchDateTimes.class);
        tables[SCHTIME] = new Table("SchTime",SchTime.class);
        tables[SECTION] = new Table("Section",Section.class);
        tables[SECTIONITEM] = new Table("SectionItem",SectionItem.class);
        tables[SERVICE] = new Table("Service",Service.class);
        tables[SERVICECATEGORY] = new Table("ServiceCategory",ServiceCategory.class);
        tables[SERVICEPRODUCT] = new Table("ServiceProduct",ServiceProduct.class);
        tables[SPECIES] = new Table("Species",Species.class);
        tables[TEMPLATE] = new Table("Template",Template.class);
        tables[TEMPLATECATEGORY] = new Table("TemplateCategory",TemplateCategory.class);
        tables[TEMPLATEROW] = new Table("TemplateRow",TemplateRow.class);
        tables[USER] = new Table("VPUser",User.class);
        tables[VERSION] = new Table("Version",Version.class);
        tables[VERSIONSTATUS] = new Table("VersionStatus",VersionStatus.class);
        
        // LINK TABLES
        tables[LINKITEMSITEMCATEGORIES] = new Table("LinkItemsItemCategories",true);
        tables[LINKTEMPLATESTEMPLATECATEGORIES] = new Table("LinkTemplatesTemplateCategories",true);
        tables[LINKPRODUCTSPRODUCTCATEGORIES] = new Table("LinkProductsProductCategories",true);
        tables[LINKSERVICESREVERSESERVICES] = new Table("LinkServicesReverseServices",true);
        
        // AutoSelect =================================================================
        columns = new Column[6];
        // >> ID
        columns[0] = new Column("id", "id", Types.VARCHAR, 16) {
        	public @Override void setProperty(OAObject object, ResultSet rs, int col) throws SQLException {
        		((AutoSelect) object).id = rs.getString(col);
        	}
        };
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        // >> SEQ
        columns[1] = new Column("seq", "seq", Types.INTEGER, 5) {
        	public @Override void setProperty(OAObject oaObj, ResultSet rs, int col) throws SQLException {
        		int val = rs.getInt(col);
        		if (rs.wasNull()) OAObjectReflectDelegate.setPrimitiveNull(oaObj, AutoSelect.PROPERTY_Seq);   
        		else ((AutoSelect) oaObj).seq = val;
        	}
        };
        // >> ExamitemStatus fkey
        columns[2] = new Column("examItemStatusId") {
        	public @Override void setProperty(OAObject oaObj, ResultSet rs, int col) throws SQLException {
        		String val = rs.getString(col);
        		if (!rs.wasNull()) OAObjectReflectDelegate.storeLinkValue(oaObj, AutoSelect.PROPERTY_ExamItemStatus, val);
        	}
        };

//qqqqqqq        
        columns[3] = new Column("itemId");
        columns[4] = new Column("sectionItemId");
        columns[5] = new Column("subItemId");
        tables[AUTOSELECT].setColumns(columns);
        tables[AUTOSELECT].addIndex(new Index("AutoSelectItem", "itemId"));
        tables[AUTOSELECT].addIndex(new Index("AutoSelectSectionItem", "sectionItemId"));
        tables[AUTOSELECT].addIndex(new Index("AutoSelectSubItem", "subItemId"));
        
        // Breed COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("dateValue", "date", Types.DATE, 10);
        columns[2] = new Column("name", "name", Types.VARCHAR, 50);
        columns[3] = new Column("speciesId");
        tables[BREED].setColumns(columns);
        tables[BREED].addIndex(new Index("BreedSpecies", "speciesId"));
        
        // Client COLUMNS
        columns = new Column[13];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("firstName", "firstName", Types.VARCHAR, 25);
        columns[2] = new Column("lastName", "lastName", Types.VARCHAR, 25);
        columns[2].columnLowerName = "lastNameLower";
        columns[3] = new Column("address1", "address1", Types.VARCHAR, 55);
        columns[4] = new Column("address2", "address2", Types.VARCHAR, 55);
        columns[5] = new Column("city", "city", Types.VARCHAR, 55);
        columns[6] = new Column("state", "state", Types.VARCHAR, 25);
        columns[7] = new Column("zip", "zip", Types.VARCHAR, 15);
        columns[8] = new Column("country", "country", Types.VARCHAR, 35);
        columns[9] = new Column("phone", "phone", Types.VARCHAR, 20);
        columns[10] = new Column("phone2", "phone2", Types.VARCHAR, 30);
        columns[11] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[12] = new Column("email", "email", Types.VARCHAR, 85);
        columns[12].columnLowerName = "emailLower";
        tables[CLIENT].setColumns(columns);
        tables[CLIENT].addIndex(new Index("ClientLastName", "lastNameLower"));
        tables[CLIENT].addIndex(new Index("ClientPmsId", "pmsId"));
        tables[CLIENT].addIndex(new Index("ClientEmail", "emailLower"));
        
        // ClientAlert COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("description", "description", Types.VARCHAR, 255);
        columns[2] = new Column("clientId");
        tables[CLIENTALERT].setColumns(columns);
        tables[CLIENTALERT].addIndex(new Index("ClientAlertClient", "clientId"));
        
        // Clinic COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("dateValue", "date", Types.DATE, 10);
        columns[2] = new Column("name", "name", Types.VARCHAR, 150);
        columns[3] = new Column("name2", "name2", Types.VARCHAR, 150);
        columns[4] = new Column("display1", "display1", Types.CLOB, 75);
        columns[5] = new Column("display2", "display2", Types.CLOB, 75);
        columns[6] = new Column("display3", "display3", Types.CLOB, 2);
        columns[7] = new Column("companyId");
        tables[CLINIC].setColumns(columns);
        tables[CLINIC].addIndex(new Index("ClinicCompany", "companyId"));
        
        // Company COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("dateValue", "date", Types.DATE, 10);
        columns[2] = new Column("name", "name", Types.VARCHAR, 50);
        columns[3] = new Column("name2", "name2", Types.VARCHAR, 50);
        tables[COMPANY].setColumns(columns);
        
        // Dictionary COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("word", "word", Types.VARCHAR, 75);
        columns[2] = new Column("soundex", "soundex", Types.VARCHAR, 4);
        columns[3] = new Column("dateCreated", "dateCreated", Types.DATE, 10);
        columns[4] = new Column("valid", "valid", Types.BOOLEAN, 5);
        columns[5] = new Column("baseDictionaryId");
        columns[6] = new Column("validDictionaryId");
        columns[7] = new Column("languageId");
        tables[DICTIONARY].setColumns(columns);
        tables[DICTIONARY].addIndex(new Index("DictionaryWord", "word"));
        tables[DICTIONARY].addIndex(new Index("DictionarySoundex", "soundex"));
        tables[DICTIONARY].addIndex(new Index("DictionaryBaseDictionary", "baseDictionaryId"));
        tables[DICTIONARY].addIndex(new Index("DictionaryValidDictionary", "validDictionaryId"));
        
        // Exam COLUMNS
        columns = new Column[23];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("dateValue", "date", Types.DATE, 10);
        columns[2] = new Column("report", "report", Types.CLOB, 16000);
        columns[3] = new Column("note", "note", Types.CLOB, 500);
        columns[4] = new Column("temp", "temp", Types.VARCHAR, 15);
        columns[5] = new Column("weight", "weight", Types.VARCHAR, 15);
        columns[6] = new Column("pulse", "pulse", Types.VARCHAR, 15);
        columns[7] = new Column("resp", "resp", Types.VARCHAR, 15);
        columns[8] = new Column("mmColor", "mmColor", Types.VARCHAR, 15);
        columns[9] = new Column("hydration", "hydration", Types.VARCHAR, 15);
        columns[10] = new Column("crt", "crt", Types.VARCHAR, 15);
        columns[11] = new Column("endDate", "endDate", Types.DATE, 10);
        columns[12] = new Column("endTime", "endTime", Types.TIME, 5);
        columns[13] = new Column("title", "title", Types.VARCHAR, 150); // sql ddl is 250, Safari is 150
        columns[14] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[15] = new Column("pickupDate", "pickupDate", Types.DATE, 10);
        columns[16] = new Column("pickupTime", "pickupTime", Types.TIME, 5);
        columns[17] = new Column("vetUserId");
        columns[18] = new Column("techUserId");
        columns[19] = new Column("petId");
        columns[20] = new Column("clinicId");
        columns[21] = new Column("receptionistUserId");
        columns[22] = new Column("preparedByUserId");
        tables[EXAM].setColumns(columns);
        tables[EXAM].addIndex(new Index("ExamEndDate", "endDate"));
        tables[EXAM].addIndex(new Index("ExamPet", "petId"));
        
        // ExamItem COLUMNS
        columns = new Column[33];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("checkedValue", "checkedValue", Types.INTEGER, 13);
        columns[2] = new Column("severity", "severity", Types.INTEGER, 8);
        columns[3] = new Column("priority", "priority", Types.INTEGER, 8);
        columns[4] = new Column("reminder", "reminder", Types.BOOLEAN, 7);
        columns[5] = new Column("carriedForward", "carriedForward", Types.BOOLEAN, 14);
        columns[6] = new Column("autoChecked", "autoChecked", Types.BOOLEAN, 11);
        columns[7] = new Column("dontPrint", "dontPrint", Types.BOOLEAN, 9);
        columns[8] = new Column("comment", "comment", Types.CLOB, 16000);
        columns[9] = new Column("techDescription", "techDescription", Types.CLOB, 128000);
        columns[10] = new Column("clientDescription", "clientDescription", Types.CLOB, 128000);
        columns[11] = new Column("instruction", "instruction", Types.CLOB, 128000);
        columns[12] = new Column("nextDate", "nextDate", Types.DATE, 10);
        columns[13] = new Column("price", "price", Types.DECIMAL, 9);
        columns[14] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[15] = new Column("lastDate", "lastDate", Types.DATE, 10);
        columns[16] = new Column("wellnessItem", "wellnessItem", Types.BOOLEAN, 12);
        columns[17] = new Column("reasonForVisit", "reasonForVisit", Types.BOOLEAN, 14);
        columns[18] = new Column("reminderDate", "reminderDate", Types.DATE, 10);
        columns[19] = new Column("clientDescriptionChanged", "clientDescriptionChanged", Types.BOOLEAN, 24);
        columns[20] = new Column("expiredDate", "expiredDate", Types.DATE, 10);
        columns[21] = new Column("quantity", "quantity", Types.INTEGER, 8);
        columns[22] = new Column("reportType", "reportType", Types.INTEGER, 10);
        columns[23] = new Column("shortDescriptionOnly", "shortDescriptionOnly", Types.BOOLEAN, 20);
        columns[24] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[25] = new Column("result", "result", Types.VARCHAR, 75);
        columns[26] = new Column("examId");
        columns[27] = new Column("userId");
        columns[28] = new Column("itemId");
        columns[29] = new Column("examItemStatusId");
        columns[30] = new Column("sectionItemId");
        columns[31] = new Column("problemId");
        columns[32] = new Column("productPackageId");
        tables[EXAMITEM].setColumns(columns);
        tables[EXAMITEM].addIndex(new Index("ExamItemExam", "examId"));
        tables[EXAMITEM].addIndex(new Index("ExamItemProblem", "problemId"));
        
        // ExamItemHistory COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("dateValue", "date", Types.DATE, 10);
        columns[2] = new Column("timeValue", "time", Types.TIME, 5);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("comment", "comment", Types.VARCHAR, 75);
        columns[5] = new Column("userId");
        columns[6] = new Column("examItemId");
        columns[7] = new Column("examItemStatusId");
        tables[EXAMITEMHISTORY].setColumns(columns);
        tables[EXAMITEMHISTORY].addIndex(new Index("ExamItemHistoryExamItem", "examItemId"));
        
        // ExamItemStatus COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[5] = new Column("showOnReport", "showOnReport", Types.BOOLEAN, 12);
        columns[6] = new Column("performed", "performed", Types.BOOLEAN, 9);
        columns[7] = new Column("type", "type", Types.INTEGER, 4);
        tables[EXAMITEMSTATUS].setColumns(columns);
        
        // ExamItemTask COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.VARCHAR, 255);
        columns[3] = new Column("required", "required", Types.BOOLEAN, 8);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[5] = new Column("examItemId");
        columns[6] = new Column("schDateTimesId");
        tables[EXAMITEMTASK].setColumns(columns);
        tables[EXAMITEMTASK].addIndex(new Index("ExamItemTaskExamItem", "examItemId"));
        
        // ExamItemTaskStep COLUMNS
        columns = new Column[10];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.VARCHAR, 255);
        columns[3] = new Column("required", "required", Types.BOOLEAN, 8);
        columns[4] = new Column("type", "type", Types.INTEGER, 4);
        columns[5] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[6] = new Column("minValue", "min", Types.VARCHAR, 20);
        columns[7] = new Column("maxValue", "max", Types.VARCHAR, 20);
        columns[8] = new Column("examItemTaskId");
        columns[9] = new Column("itemTaskStepId");
        tables[EXAMITEMTASKSTEP].setColumns(columns);
        tables[EXAMITEMTASKSTEP].addIndex(new Index("ExamItemTaskStepExamItemTask", "examItemTaskId"));
        
        // ExamItemTaskStepResult COLUMNS
        columns = new Column[9];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("result", "result", Types.VARCHAR, 150);
        columns[2] = new Column("doneDate", "doneDate", Types.DATE, 10);
        columns[3] = new Column("doneTime", "doneTime", Types.TIME, 5);
        columns[4] = new Column("dueDate", "dueDate", Types.DATE, 10);
        columns[5] = new Column("dueTime", "dueTime", Types.TIME, 5);
        columns[6] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[7] = new Column("examItemTaskStepId");
        columns[8] = new Column("userId");
        tables[EXAMITEMTASKSTEPRESULT].setColumns(columns);
        tables[EXAMITEMTASKSTEPRESULT].addIndex(new Index("ExamItemTaskStepResultExamItemTaskStep", "examItemTaskStepId"));
        
        // ExamTemplate COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[2] = new Column("note", "note", Types.VARCHAR, 255);
        columns[3] = new Column("showOnReport", "showOnReport", Types.BOOLEAN, 12);
        columns[4] = new Column("examId");
        columns[5] = new Column("userId");
        columns[6] = new Column("templateId");
        tables[EXAMTEMPLATE].setColumns(columns);
        tables[EXAMTEMPLATE].addIndex(new Index("ExamTemplateExam", "examId"));
        
        // Item COLUMNS
        columns = new Column[22];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[2] = new Column("name", "name", Types.VARCHAR, 50);
        columns[3] = new Column("description", "description", Types.VARCHAR, 254);
        columns[4] = new Column("severity", "severity", Types.INTEGER, 8);
        columns[5] = new Column("priority", "priority", Types.INTEGER, 8);
        columns[6] = new Column("reminder", "reminder", Types.BOOLEAN, 7);
        columns[7] = new Column("techDescription", "techDescription", Types.CLOB, 128000);
        columns[8] = new Column("clientDescription", "clientDescription", Types.CLOB, 128000);
        columns[9] = new Column("pickOneSubItem", "pickOneSubItem", Types.BOOLEAN, 14);
        columns[10] = new Column("subItemFlag", "subItemFlag", Types.BOOLEAN, 11);
        columns[11] = new Column("shortClientDescription", "shortClientDescription", Types.CLOB, 150);
        columns[12] = new Column("price", "price", Types.DECIMAL, 8);
        columns[13] = new Column("intervalType", "intervalType", Types.INTEGER, 6);
        columns[14] = new Column("intervalAmount", "intervalAmount", Types.INTEGER, 6);
        columns[15] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[16] = new Column("type", "type", Types.INTEGER, 11);
        columns[17] = new Column("petSystemId");
        columns[18] = new Column("parentItemId");
        columns[19] = new Column("itemTypeId");
        columns[20] = new Column("serviceId");
        columns[21] = new Column("productCategoryId");
        tables[ITEM].setColumns(columns);
        tables[ITEM].addIndex(new Index("ItemPetSystem", "petSystemId"));
        tables[ITEM].addIndex(new Index("ItemParentItem", "parentItemId"));
        tables[ITEM].addIndex(new Index("ItemService", "serviceId"));
        tables[ITEM].addIndex(new Index("ItemProductCategory", "productCategoryId"));
        
        // ItemCategory COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.VARCHAR, 150);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("parentItemCategoryId");
        tables[ITEMCATEGORY].setColumns(columns);
        tables[ITEMCATEGORY].addIndex(new Index("ItemCategoryParentItemCategory", "parentItemCategoryId"));
        
        // ItemDosage COLUMNS
        columns = new Column[10];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("minAge", "minAge", Types.INTEGER, 13);
        columns[2] = new Column("maxAge", "maxAge", Types.INTEGER, 9);
        columns[3] = new Column("uomAge", "uomAge", Types.INTEGER, 6);
        columns[4] = new Column("amount", "amount", Types.INTEGER, 6);
        columns[5] = new Column("minWeight", "minWeight", Types.INTEGER, 9);
        columns[6] = new Column("maxWeight", "maxWeight", Types.INTEGER, 9);
        columns[7] = new Column("uomWeight", "uomWeight", Types.INTEGER, 9);
        columns[8] = new Column("itemProductId");
        columns[9] = new Column("productPackageId");
        tables[ITEMDOSAGE].setColumns(columns);
        tables[ITEMDOSAGE].addIndex(new Index("ItemDosageItemProduct", "itemProductId"));
        tables[ITEMDOSAGE].addIndex(new Index("ItemDosageProductPackage", "productPackageId"));
        
        // ItemProduct COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("itemId");
        columns[2] = new Column("productId");
        columns[3] = new Column("speciesId");
        columns[4] = new Column("productPackageId","");
        tables[ITEMPRODUCT].setColumns(columns);
        tables[ITEMPRODUCT].addIndex(new Index("ItemProductItem", "itemId"));
        tables[ITEMPRODUCT].addIndex(new Index("ItemProductProduct", "productId"));
        tables[ITEMPRODUCT].addIndex(new Index("ItemProductSpecies", "speciesId"));
        
        // ItemTask COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.CLOB, 11);
        columns[3] = new Column("required", "required", Types.BOOLEAN, 8);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[5] = new Column("itemId");
        columns[6] = new Column("useItemId");
        columns[7] = new Column("schDateTimesId");
        tables[ITEMTASK].setColumns(columns);
        tables[ITEMTASK].addIndex(new Index("ItemTaskItem", "itemId"));
        
        // ItemTaskStep COLUMNS
        columns = new Column[9];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.CLOB, 11);
        columns[3] = new Column("required", "required", Types.BOOLEAN, 8);
        columns[4] = new Column("type", "type", Types.INTEGER, 4);
        columns[5] = new Column("minValue", "min", Types.VARCHAR, 20);
        columns[6] = new Column("maxValue", "max", Types.VARCHAR, 20);
        columns[7] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[8] = new Column("itemTaskId");
        tables[ITEMTASKSTEP].setColumns(columns);
        tables[ITEMTASKSTEP].addIndex(new Index("ItemTaskStepItemTask", "itemTaskId"));
        
        // ItemTaskStepResult COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 25);
        columns[2] = new Column("value", "value", Types.VARCHAR, 155);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("minValue", "min", Types.VARCHAR, 20);
        columns[5] = new Column("maxValue", "max", Types.VARCHAR, 20);
        columns[6] = new Column("itemTaskStepId");
        tables[ITEMTASKSTEPRESULT].setColumns(columns);
        tables[ITEMTASKSTEPRESULT].addIndex(new Index("ItemTaskStepResultItemTaskStep", "itemTaskStepId"));
        
        // ItemType COLUMNS
        columns = new Column[6];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[5] = new Column("type", "type", Types.INTEGER, 4);
        tables[ITEMTYPE].setColumns(columns);
        
        // Lab COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        tables[LAB].setColumns(columns);
        
        // LabTest COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[2] = new Column("name", "name", Types.VARCHAR, 35);
        columns[3] = new Column("description", "description", Types.VARCHAR, 175);
        columns[4] = new Column("uom", "uom", Types.VARCHAR, 25);
        columns[5] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[6] = new Column("labId");
        columns[7] = new Column("labTestTypeId");
        tables[LABTEST].setColumns(columns);
        tables[LABTEST].addIndex(new Index("LabTestLab", "labId"));
        tables[LABTEST].addIndex(new Index("LabTestLabTestType", "labTestTypeId"));
        
        // LabTestResult COLUMNS
        columns = new Column[6];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("lowRange", "lowRange", Types.DOUBLE, 8);
        columns[2] = new Column("highRange", "highRange", Types.DOUBLE, 9);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("labTestSpeciesId");
        columns[5] = new Column("labTestResultTypeId");
        tables[LABTESTRESULT].setColumns(columns);
        tables[LABTESTRESULT].addIndex(new Index("LabTestResultLabTestSpecies", "labTestSpeciesId"));
        tables[LABTESTRESULT].addIndex(new Index("LabTestResultLabTestResultType", "labTestResultTypeId"));
        
        // LabTestResultType COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        tables[LABTESTRESULTTYPE].setColumns(columns);
        
        // LabTestSpecies COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("lowRange", "lowRange", Types.DOUBLE, 8);
        columns[2] = new Column("highRange", "highRange", Types.DOUBLE, 9);
        columns[3] = new Column("speciesId");
        columns[4] = new Column("labTestId");
        tables[LABTESTSPECIES].setColumns(columns);
        tables[LABTESTSPECIES].addIndex(new Index("LabTestSpeciesSpecies", "speciesId"));
        tables[LABTESTSPECIES].addIndex(new Index("LabTestSpeciesLabTest", "labTestId"));
        
        // LabTestType COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        tables[LABTESTTYPE].setColumns(columns);
        
        // Language COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("code", "code", Types.VARCHAR, 6);
        columns[2] = new Column("countryCode", "countryCode", Types.VARCHAR, 35);
        columns[3] = new Column("description", "description", Types.VARCHAR, 75);
        tables[LANGUAGE].setColumns(columns);
        
        // Manufacturer COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        tables[MANUFACTURER].setColumns(columns);
        
        // Pet COLUMNS
        columns = new Column[17];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[1].columnLowerName = "nameLower";
        columns[2] = new Column("nickName", "nickName", Types.VARCHAR, 35);
        columns[3] = new Column("sex", "sex", Types.VARCHAR, 25);
        columns[4] = new Column("birthDate", "birthDate", Types.DATE, 10);
        columns[5] = new Column("color", "color", Types.VARCHAR, 150);
        columns[6] = new Column("breed", "breed", Types.VARCHAR, 50);
        columns[7] = new Column("inactiveDate", "inactiveDate", Types.DATE, 10);
        columns[8] = new Column("inactiveReason", "inactiveReason", Types.VARCHAR, 75);
        columns[9] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[10] = new Column("microChip", "microChip", Types.VARCHAR, 35);
        columns[11] = new Column("microChipDate", "microChipDate", Types.DATE, 10);
        columns[12] = new Column("rabies", "rabies", Types.VARCHAR, 35);
        columns[13] = new Column("rabiesDate", "rabiesDate", Types.DATE, 10);
        columns[14] = new Column("history", "history", Types.CLOB, 128000);
        columns[15] = new Column("speciesId");
        columns[16] = new Column("clientId");
        tables[PET].setColumns(columns);
        tables[PET].addIndex(new Index("PetName", "nameLower"));
        tables[PET].addIndex(new Index("PetClient", "clientId"));
        
        // PetAlert COLUMNS
        columns = new Column[3];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("description", "description", Types.VARCHAR, 255);
        columns[2] = new Column("petId");
        tables[PETALERT].setColumns(columns);
        tables[PETALERT].addIndex(new Index("PetAlertPet", "petId"));
        
        // PetColor COLUMNS
        columns = new Column[2];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        tables[PETCOLOR].setColumns(columns);
        
        // PetSystem COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 150);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        tables[PETSYSTEM].setColumns(columns);
        
        // Problem COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("note", "note", Types.VARCHAR, 255);
        columns[3] = new Column("endDate", "endDate", Types.DATE, 5);
        columns[4] = new Column("petId");
        tables[PROBLEM].setColumns(columns);
        tables[PROBLEM].addIndex(new Index("ProblemPet", "petId"));
        
        // ProblemStatus COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        tables[PROBLEMSTATUS].setColumns(columns);
        
        // Product COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("description", "description", Types.CLOB, 150);
        columns[3] = new Column("dateCreated", "dateCreated", Types.DATE, 10);
        columns[4] = new Column("warning", "warning", Types.CLOB, 7);
        columns[5] = new Column("label", "label", Types.CLOB, 5);
        columns[6] = new Column("manufacturerId");
        tables[PRODUCT].setColumns(columns);
        tables[PRODUCT].addIndex(new Index("ProductManufacturer", "manufacturerId"));
        
        // ProductCategory COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 55);
        columns[1].columnLowerName = "nameLower";
        columns[2] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[3] = new Column("parentProductCategoryId");
        tables[PRODUCTCATEGORY].setColumns(columns);
        tables[PRODUCTCATEGORY].addIndex(new Index("ProductCategoryName", "nameLower"));
        tables[PRODUCTCATEGORY].addIndex(new Index("ProductCategoryParentProductCategory", "parentProductCategoryId"));
        
        // ProductPackage COLUMNS
        columns = new Column[10];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 35);
        columns[2] = new Column("description", "description", Types.CLOB, 150);
        columns[3] = new Column("amount", "amount", Types.INTEGER, 4);
        columns[4] = new Column("uom", "uom", Types.INTEGER, 3);
        columns[5] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[6] = new Column("label", "label", Types.CLOB, 5);
        columns[7] = new Column("manCode", "manCode", Types.VARCHAR, 15);
        columns[8] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[9] = new Column("productId");
        tables[PRODUCTPACKAGE].setColumns(columns);
        tables[PRODUCTPACKAGE].addIndex(new Index("ProductPackageProduct", "productId"));
        
        // RecommendItem COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("description", "description", Types.VARCHAR, 155);
        columns[2] = new Column("complianceType", "complianceType", Types.INTEGER, 4);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("insertFlag", "insert", Types.BOOLEAN, 6);
        columns[5] = new Column("itemId");
        columns[6] = new Column("itemTaskStepResultId");
        tables[RECOMMENDITEM].setColumns(columns);
        tables[RECOMMENDITEM].addIndex(new Index("RecommendItemItemTaskStepResult", "itemTaskStepResultId"));
        
        // SchDate COLUMNS
        columns = new Column[9];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("type", "type", Types.INTEGER, 4);
        columns[2] = new Column("every", "every", Types.INTEGER, 5);
        columns[3] = new Column("days", "days", Types.INTEGER, 4);
        columns[4] = new Column("months", "months", Types.INTEGER, 6);
        columns[5] = new Column("monthDay", "monthDay", Types.INTEGER, 8);
        columns[6] = new Column("monthWeeks", "monthWeeks", Types.INTEGER, 10);
        columns[7] = new Column("quarters", "quarters", Types.INTEGER, 8);
        columns[8] = new Column("quarterMonths", "quarterMonths", Types.INTEGER, 13);
        tables[SCHDATE].setColumns(columns);
        tables[SCHDATE].addIndex(new Index("SchDateSchDateTime", "schDateTimeId"));
        
        // SchDateTime COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("includeSchDateTimesId");
        columns[2] = new Column("excludeSchDateTimesId");
        columns[3] = new Column("schDateId");
        columns[4] = new Column("schTimeId");
        tables[SCHDATETIME].setColumns(columns);
        tables[SCHDATETIME].addIndex(new Index("SchDateTimeIncludeSchDateTimes", "includeSchDateTimesId"));
        tables[SCHDATETIME].addIndex(new Index("SchDateTimeExcludeSchDateTimes", "excludeSchDateTimesId"));
        tables[SCHDATETIME].addIndex(new Index("SchDateTimeSchDate", "schDateId"));
        tables[SCHDATETIME].addIndex(new Index("SchDateTimeSchTime", "schTimeId"));
        
        // SchDateTimes COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("durationType", "durationType", Types.INTEGER, 4);
        columns[2] = new Column("minValue", "min", Types.INTEGER, 3);
        columns[3] = new Column("maxValue", "max", Types.INTEGER, 3);
        columns[4] = new Column("units", "units", Types.INTEGER, 5);
        tables[SCHDATETIMES].setColumns(columns);
        
        // SchTime COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("type", "type", Types.INTEGER, 4);
        columns[2] = new Column("beginTime", "beginTime", Types.TIME, 5);
        columns[3] = new Column("endTime", "endTime", Types.TIME, 5);
        columns[4] = new Column("every", "every", Types.INTEGER, 5);
        columns[5] = new Column("minValue", "min", Types.INTEGER, 3);
        columns[6] = new Column("maxValue", "max", Types.INTEGER, 3);
        columns[7] = new Column("length", "length", Types.INTEGER, 6);
        tables[SCHTIME].setColumns(columns);
        tables[SCHTIME].addIndex(new Index("SchTimeSchDateTime", "schDateTimeId"));
        
        // Section COLUMNS
        columns = new Column[9];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 150);
        columns[2] = new Column("heading", "heading", Types.VARCHAR, 150);
        columns[3] = new Column("description", "description", Types.VARCHAR, 175);
        columns[4] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[5] = new Column("columns", "columns", Types.INTEGER, 7);
        columns[6] = new Column("rowCount", "rows", Types.INTEGER, 4);
        columns[7] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[8] = new Column("templateRowId");
        tables[SECTION].setColumns(columns);
        tables[SECTION].addIndex(new Index("SectionTemplateRow", "templateRowId"));
        
        // SectionItem COLUMNS
        columns = new Column[10];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[2] = new Column("itemName", "itemName", Types.VARCHAR, 50);
        columns[3] = new Column("itemSubItemFlag", "itemSubItemFlag", Types.BOOLEAN, 11);
        columns[4] = new Column("autoCheckFlag", "autoCheckFlag", Types.BOOLEAN, 13);
        columns[5] = new Column("type", "type", Types.INTEGER, 11);
        columns[6] = new Column("examItemStatusId");
        columns[7] = new Column("itemId");
        columns[8] = new Column("labTestSpeciesId");
        columns[9] = new Column("sectionId");
        tables[SECTIONITEM].setColumns(columns);
        tables[SECTIONITEM].addIndex(new Index("SectionItemItem", "itemId"));
        tables[SECTIONITEM].addIndex(new Index("SectionItemLabTestSpecies", "labTestSpeciesId"));
        tables[SECTIONITEM].addIndex(new Index("SectionItemSection", "sectionId"));
        
        // Service COLUMNS
        columns = new Column[8];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("description", "description", Types.CLOB, 150);
        columns[3] = new Column("dateCreated", "dateCreated", Types.DATE, 10);
        columns[4] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        columns[5] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[6] = new Column("seq2", "seq2", Types.INTEGER, 5);
        columns[7] = new Column("serviceCategoryId");
        tables[SERVICE].setColumns(columns);
        tables[SERVICE].addIndex(new Index("ServiceServiceCategory", "serviceCategoryId"));
        
        // ServiceCategory COLUMNS
        columns = new Column[4];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 55);
        columns[2] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[3] = new Column("parentServiceCategoryId");
        tables[SERVICECATEGORY].setColumns(columns);
        tables[SERVICECATEGORY].addIndex(new Index("ServiceCategoryParentServiceCategory", "parentServiceCategoryId"));
        
        // ServiceProduct COLUMNS
        columns = new Column[6];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("qty", "qty", Types.DOUBLE, 3);
        columns[2] = new Column("note", "note", Types.VARCHAR, 150);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("serviceId");
        columns[5] = new Column("productPackageId");
        tables[SERVICEPRODUCT].setColumns(columns);
        tables[SERVICEPRODUCT].addIndex(new Index("ServiceProductService", "serviceId"));
        tables[SERVICEPRODUCT].addIndex(new Index("ServiceProductProductPackage", "productPackageId"));
        
        // Species COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 75);
        columns[2] = new Column("description", "description", Types.VARCHAR, 150);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[5] = new Column("defaultGifFileName", "defaultGifFileName", Types.VARCHAR, 75);
        columns[6] = new Column("wellnessGifFileName", "wellnessGifFileName", Types.VARCHAR, 75);
        tables[SPECIES].setColumns(columns);
        
        // Template COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 150);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("type", "type", Types.INTEGER, 4);
        tables[TEMPLATE].setColumns(columns);
        
        // TemplateCategory COLUMNS
        columns = new Column[6];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 150);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("speciesId");
        columns[5] = new Column("parentTemplateCategoryId");
        tables[TEMPLATECATEGORY].setColumns(columns);
        tables[TEMPLATECATEGORY].addIndex(new Index("TemplateCategorySpecies", "speciesId"));
        tables[TEMPLATECATEGORY].addIndex(new Index("TemplateCategoryParentTemplateCategory", "parentTemplateCategoryId"));
        
        // TemplateRow COLUMNS
        columns = new Column[5];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 150);
        columns[2] = new Column("heading", "heading", Types.VARCHAR, 175);
        columns[3] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[4] = new Column("templateId");
        tables[TEMPLATEROW].setColumns(columns);
        tables[TEMPLATEROW].addIndex(new Index("TemplateRowTemplate", "templateId"));
        
        // User COLUMNS
        columns = new Column[12];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("loginId", "loginId", Types.VARCHAR, 25);
        columns[2] = new Column("password", "password", Types.VARCHAR, 25);
        columns[3] = new Column("admin", "admin", Types.BOOLEAN, 5);
        columns[4] = new Column("vet", "vet", Types.BOOLEAN, 3);
        columns[5] = new Column("firstName", "firstName", Types.VARCHAR, 30);
        columns[6] = new Column("lastName", "lastName", Types.VARCHAR, 35);
        columns[7] = new Column("title", "title", Types.VARCHAR, 35);
        columns[8] = new Column("prefixName", "prefixName", Types.VARCHAR, 25);
        columns[9] = new Column("inactiveDate", "inactiveDate", Types.DATE, 10);
        columns[10] = new Column("inactiveReason", "inactiveReason", Types.VARCHAR, 75);
        columns[11] = new Column("pmsId", "pmsId", Types.VARCHAR, 25);
        tables[USER].setColumns(columns);
        
        // Version COLUMNS
        columns = new Column[7];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("dateValue", "date", Types.DATE, 10);
        columns[2] = new Column("timeValue", "time", Types.TIME, 5);
        columns[3] = new Column("note", "note", Types.CLOB, 150);
        columns[4] = new Column("userId");
        columns[5] = new Column("versionStatusId");
        columns[6] = new Column("itemId");
        tables[VERSION].setColumns(columns);
        tables[VERSION].addIndex(new Index("VersionItem", "itemId"));
        
        // VersionStatus COLUMNS
        columns = new Column[6];
        columns[0] = new Column("id", "id", Types.VARCHAR, 16);
        columns[0].primaryKey = true;
        columns[0].guid = true;
        columns[0].assignNextNumber = true;
        columns[1] = new Column("name", "name", Types.VARCHAR, 50);
        columns[2] = new Column("description", "description", Types.VARCHAR, 175);
        columns[3] = new Column("gifFileName", "gifFileName", Types.VARCHAR, 75);
        columns[4] = new Column("seq", "seq", Types.INTEGER, 5);
        columns[5] = new Column("locked", "locked", Types.BOOLEAN, 9);
        tables[VERSIONSTATUS].setColumns(columns);
        
        // Link Tables Columns
        
        // LinkItemsItemCategories COLUMNS
        columns = new Column[2];
        columns[0] = new Column("itemsId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("itemCategoriesId",null);
        columns[1].primaryKey = true;
        tables[LINKITEMSITEMCATEGORIES].setColumns(columns);
        
        // LinkTemplatesTemplateCategories COLUMNS
        columns = new Column[2];
        columns[0] = new Column("templatesId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("templateCategoriesId",null);
        columns[1].primaryKey = true;
        tables[LINKTEMPLATESTEMPLATECATEGORIES].setColumns(columns);
        
        // LinkProductsProductCategories COLUMNS
        columns = new Column[2];
        columns[0] = new Column("productsId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("productCategoriesId",null);
        columns[1].primaryKey = true;
        tables[LINKPRODUCTSPRODUCTCATEGORIES].setColumns(columns);
        
        // LinkServicesReverseServices COLUMNS
        columns = new Column[2];
        columns[0] = new Column("servicesId",null);
        columns[0].primaryKey = true;
        columns[1] = new Column("reverseServicesId",null);
        columns[1].primaryKey = true;
        tables[LINKSERVICESREVERSESERVICES].setColumns(columns);
        
        // LINKS
        // table.addLink( propertyName, toTableName, reversePropertyName, FKey ColumnNumber
        tables[AUTOSELECT].addLink("examItemStatus", tables[EXAMITEMSTATUS], "autoSelects", new int[] {2});
        tables[AUTOSELECT].addLink("item", tables[ITEM], "autoSelects", new int[] {3});
        tables[AUTOSELECT].addLink("sectionItem", tables[SECTIONITEM], "autoSelects", new int[] {4});
        tables[AUTOSELECT].addLink("subItem", tables[ITEM], "autoSelectSubitems", new int[] {5});
        tables[BREED].addLink("species", tables[SPECIES], "breeds", new int[] {3});
        tables[CLIENT].addLink("pets", tables[PET], "client", new int[] {0});
        tables[CLIENT].addLink("clientAlerts", tables[CLIENTALERT], "client", new int[] {0});
        tables[CLIENTALERT].addLink("client", tables[CLIENT], "clientAlerts", new int[] {2});
        tables[CLINIC].addLink("exams", tables[EXAM], "clinic", new int[] {0});
        tables[CLINIC].addLink("company", tables[COMPANY], "clinics", new int[] {7});
        tables[COMPANY].addLink("clinics", tables[CLINIC], "company", new int[] {0});
        tables[DICTIONARY].addLink("dictionaries", tables[DICTIONARY], "baseDictionary", new int[] {0});
        tables[DICTIONARY].addLink("baseDictionary", tables[DICTIONARY], "dictionaries", new int[] {5});
        tables[DICTIONARY].addLink("invalidDictionaries", tables[DICTIONARY], "validDictionary", new int[] {0});
        tables[DICTIONARY].addLink("validDictionary", tables[DICTIONARY], "invalidDictionaries", new int[] {6});
        tables[DICTIONARY].addLink("language", tables[LANGUAGE], "dictionaries", new int[] {7});
        tables[EXAM].addLink("examItems", tables[EXAMITEM], "exam", new int[] {0});
        tables[EXAM].addLink("vetUser", tables[USER], "vetExams", new int[] {17});
        tables[EXAM].addLink("techUser", tables[USER], "techExams", new int[] {18});
        tables[EXAM].addLink("examTemplates", tables[EXAMTEMPLATE], "exam", new int[] {0});
        tables[EXAM].addLink("pet", tables[PET], "exams", new int[] {19});
        tables[EXAM].addLink("clinic", tables[CLINIC], "exams", new int[] {20});
        tables[EXAM].addLink("receptionistUser", tables[USER], "receptionistExams", new int[] {21});
        tables[EXAM].addLink("preparedByUser", tables[USER], "preparedByExams", new int[] {22});
        tables[EXAMITEM].addLink("exam", tables[EXAM], "examItems", new int[] {26});
        tables[EXAMITEM].addLink("user", tables[USER], "examItems", new int[] {27});
        tables[EXAMITEM].addLink("item", tables[ITEM], "examItems", new int[] {28});
        tables[EXAMITEM].addLink("examItemStatus", tables[EXAMITEMSTATUS], "examItems", new int[] {29});
        tables[EXAMITEM].addLink("sectionItem", tables[SECTIONITEM], "examItems", new int[] {30});
        tables[EXAMITEM].addLink("problem", tables[PROBLEM], "examItems", new int[] {31});
        tables[EXAMITEM].addLink("ExamItemHistories", tables[EXAMITEMHISTORY], "examItem", new int[] {0});
        tables[EXAMITEM].addLink("examItemTasks", tables[EXAMITEMTASK], "examItem", new int[] {0});
        tables[EXAMITEM].addLink("productPackage", tables[PRODUCTPACKAGE], "examItems", new int[] {32});
        tables[EXAMITEMHISTORY].addLink("user", tables[USER], "examItemHistories", new int[] {5});
        tables[EXAMITEMHISTORY].addLink("examItem", tables[EXAMITEM], "ExamItemHistories", new int[] {6});
        tables[EXAMITEMHISTORY].addLink("examItemStatus", tables[EXAMITEMSTATUS], "examItemHistories", new int[] {7});
        tables[EXAMITEMSTATUS].addLink("examItems", tables[EXAMITEM], "examItemStatus", new int[] {0});
        tables[EXAMITEMSTATUS].addLink("examItemHistories", tables[EXAMITEMHISTORY], "examItemStatus", new int[] {0});
        tables[EXAMITEMSTATUS].addLink("sectionItems", tables[SECTIONITEM], "examItemStatus", new int[] {0});
        tables[EXAMITEMSTATUS].addLink("autoSelects", tables[AUTOSELECT], "examItemStatus", new int[] {0});
        tables[EXAMITEMTASK].addLink("examItem", tables[EXAMITEM], "examItemTasks", new int[] {5});
        tables[EXAMITEMTASK].addLink("examItemTaskSteps", tables[EXAMITEMTASKSTEP], "examItemTask", new int[] {0});
        tables[EXAMITEMTASK].addLink("schDateTimes", tables[SCHDATETIMES], "examItemTasks", new int[] {6});
        tables[EXAMITEMTASKSTEP].addLink("examItemTask", tables[EXAMITEMTASK], "examItemTaskSteps", new int[] {8});
        tables[EXAMITEMTASKSTEP].addLink("examItemTaskStepResults", tables[EXAMITEMTASKSTEPRESULT], "examItemTaskStep", new int[] {0});
        tables[EXAMITEMTASKSTEP].addLink("itemTaskStep", tables[ITEMTASKSTEP], "examItemTaskSteps", new int[] {9});
        tables[EXAMITEMTASKSTEPRESULT].addLink("examItemTaskStep", tables[EXAMITEMTASKSTEP], "examItemTaskStepResults", new int[] {7});
        tables[EXAMITEMTASKSTEPRESULT].addLink("user", tables[USER], "examItemTaskStepResults", new int[] {8});
        tables[EXAMTEMPLATE].addLink("exam", tables[EXAM], "examTemplates", new int[] {4});
        tables[EXAMTEMPLATE].addLink("user", tables[USER], "examTemplates", new int[] {5});
        tables[EXAMTEMPLATE].addLink("template", tables[TEMPLATE], "examTemplates", new int[] {6});

        tables[ITEM].addLink("examItems", tables[EXAMITEM], "item", new int[] {0});
        tables[ITEM].addLink("versions", tables[VERSION], "item", new int[] {0});
        tables[ITEM].addLink("sectionItems", tables[SECTIONITEM], "item", new int[] {0});
        tables[ITEM].addLink("itemCategories", tables[LINKITEMSITEMCATEGORIES], "items", new int[] {0});
        tables[ITEM].addLink("petSystem", tables[PETSYSTEM], "items", new int[] {17});
        tables[ITEM].addLink("parentItem", tables[ITEM], "items", new int[] {18});
        tables[ITEM].addLink("items", tables[ITEM], "parentItem", new int[] {0});
        tables[ITEM].addLink("autoSelects", tables[AUTOSELECT], "item", new int[] {0});
        tables[ITEM].addLink("itemType", tables[ITEMTYPE], "items", new int[] {19});
        tables[ITEM].addLink("itemProducts", tables[ITEMPRODUCT], "item", new int[] {0});
        tables[ITEM].addLink("recommendItems", tables[RECOMMENDITEM], "item", new int[] {0});
        tables[ITEM].addLink("itemTasks", tables[ITEMTASK], "item", new int[] {0});
        tables[ITEM].addLink("useItemTasks", tables[ITEMTASK], "useItem", new int[] {0});
        tables[ITEM].addLink("service", tables[SERVICE], "items", new int[] {20});
        tables[ITEM].addLink("productCategory", tables[PRODUCTCATEGORY], "items", new int[] {21});
        tables[ITEM].addLink("autoSelectSubitems", tables[AUTOSELECT], "subItem", new int[] {0});
        
        tables[ITEMCATEGORY].addLink("items", tables[LINKITEMSITEMCATEGORIES], "itemCategories", new int[] {0});
        tables[ITEMCATEGORY].addLink("parentItemCategory", tables[ITEMCATEGORY], "itemCategories", new int[] {4});
        tables[ITEMCATEGORY].addLink("itemCategories", tables[ITEMCATEGORY], "parentItemCategory", new int[] {0});
        tables[ITEMDOSAGE].addLink("itemProduct", tables[ITEMPRODUCT], "itemDosages", new int[] {8});
        tables[ITEMDOSAGE].addLink("productPackage", tables[PRODUCTPACKAGE], "itemDosages", new int[] {9});
        tables[ITEMPRODUCT].addLink("item", tables[ITEM], "itemProducts", new int[] {1});
        tables[ITEMPRODUCT].addLink("itemDosages", tables[ITEMDOSAGE], "itemProduct", new int[] {0});
        tables[ITEMPRODUCT].addLink("product", tables[PRODUCT], "itemProducts", new int[] {2});
        tables[ITEMPRODUCT].addLink("species", tables[SPECIES], "itemProducts", new int[] {3});
        tables[ITEMPRODUCT].addLink("productPackage", tables[PRODUCTPACKAGE], "itemProducts", new int[] {4});

        
        tables[ITEMTASK].addLink("item", tables[ITEM], "itemTasks", new int[] {5});
        tables[ITEMTASK].addLink("useItem", tables[ITEM], "useItemTasks", new int[] {6});
        tables[ITEMTASK].addLink("itemTaskSteps", tables[ITEMTASKSTEP], "itemTask", new int[] {0});
        tables[ITEMTASK].addLink("schDateTimes", tables[SCHDATETIMES], "itemTasks", new int[] {7});
        tables[ITEMTASKSTEP].addLink("examItemTaskSteps", tables[EXAMITEMTASKSTEP], "itemTaskStep", new int[] {0});
        tables[ITEMTASKSTEP].addLink("itemTask", tables[ITEMTASK], "itemTaskSteps", new int[] {8});
        tables[ITEMTASKSTEP].addLink("itemTaskStepResults", tables[ITEMTASKSTEPRESULT], "itemTaskStep", new int[] {0});
        tables[ITEMTASKSTEPRESULT].addLink("recommendItems", tables[RECOMMENDITEM], "itemTaskStepResult", new int[] {0});
        tables[ITEMTASKSTEPRESULT].addLink("itemTaskStep", tables[ITEMTASKSTEP], "itemTaskStepResults", new int[] {6});
        tables[ITEMTYPE].addLink("items", tables[ITEM], "itemType", new int[] {0});
        tables[LAB].addLink("labTests", tables[LABTEST], "lab", new int[] {0});
        tables[LABTEST].addLink("labTestSpecies", tables[LABTESTSPECIES], "labTest", new int[] {0});
        tables[LABTEST].addLink("lab", tables[LAB], "labTests", new int[] {6});
        tables[LABTEST].addLink("labTestType", tables[LABTESTTYPE], "labTests", new int[] {7});
        tables[LABTESTRESULT].addLink("labTestSpecies", tables[LABTESTSPECIES], "labTestResults", new int[] {4});
        tables[LABTESTRESULT].addLink("labTestResultType", tables[LABTESTRESULTTYPE], "labTestResults", new int[] {5});
        tables[LABTESTRESULTTYPE].addLink("labTestResults", tables[LABTESTRESULT], "labTestResultType", new int[] {0});
        tables[LABTESTSPECIES].addLink("species", tables[SPECIES], "labTestSpecies", new int[] {3});
        tables[LABTESTSPECIES].addLink("labTest", tables[LABTEST], "labTestSpecies", new int[] {4});
        tables[LABTESTSPECIES].addLink("labTestResults", tables[LABTESTRESULT], "labTestSpecies", new int[] {0});
        tables[LABTESTSPECIES].addLink("sectionItems", tables[SECTIONITEM], "labTestSpecies", new int[] {0});
        tables[LABTESTTYPE].addLink("labTests", tables[LABTEST], "labTestType", new int[] {0});
        tables[LANGUAGE].addLink("dictionaries", tables[DICTIONARY], "language", new int[] {0});
        tables[MANUFACTURER].addLink("products", tables[PRODUCT], "manufacturer", new int[] {0});
        tables[PET].addLink("exams", tables[EXAM], "pet", new int[] {0});
        tables[PET].addLink("species", tables[SPECIES], "pets", new int[] {15});
        tables[PET].addLink("client", tables[CLIENT], "pets", new int[] {16});
        tables[PET].addLink("problems", tables[PROBLEM], "pet", new int[] {0});
        tables[PET].addLink("petAlerts", tables[PETALERT], "pet", new int[] {0});
        tables[PETALERT].addLink("pet", tables[PET], "petAlerts", new int[] {2});
        tables[PETSYSTEM].addLink("items", tables[ITEM], "petSystem", new int[] {0});
        tables[PROBLEM].addLink("pet", tables[PET], "problems", new int[] {4});
        tables[PROBLEM].addLink("examItems", tables[EXAMITEM], "problem", new int[] {0});
        tables[PRODUCT].addLink("itemProducts", tables[ITEMPRODUCT], "product", new int[] {0});
        tables[PRODUCT].addLink("manufacturer", tables[MANUFACTURER], "products", new int[] {6});
        tables[PRODUCT].addLink("productCategories", tables[LINKPRODUCTSPRODUCTCATEGORIES], "products", new int[] {0});
        tables[PRODUCT].addLink("productPackages", tables[PRODUCTPACKAGE], "product", new int[] {0});
        tables[PRODUCTCATEGORY].addLink("products", tables[LINKPRODUCTSPRODUCTCATEGORIES], "productCategories", new int[] {0});
        tables[PRODUCTCATEGORY].addLink("parentProductCategory", tables[PRODUCTCATEGORY], "productCategories", new int[] {3});
        tables[PRODUCTCATEGORY].addLink("productCategories", tables[PRODUCTCATEGORY], "parentProductCategory", new int[] {0});
        tables[PRODUCTCATEGORY].addLink("items", tables[ITEM], "productCategory", new int[] {0});
        
        tables[PRODUCTPACKAGE].addLink("product", tables[PRODUCT], "productPackages", new int[] {9});
        tables[PRODUCTPACKAGE].addLink("itemDosages", tables[ITEMDOSAGE], "productPackage", new int[] {0});
        tables[PRODUCTPACKAGE].addLink("serviceProducts", tables[SERVICEPRODUCT], "productPackage", new int[] {0});
        tables[PRODUCTPACKAGE].addLink("examItems", tables[EXAMITEM], "productPackage", new int[] {0});
        tables[PRODUCTPACKAGE].addLink("itemProducts", tables[ITEMPRODUCT], "productPackage", new int[] {0});

        tables[RECOMMENDITEM].addLink("item", tables[ITEM], "recommendItems", new int[] {5});
        tables[RECOMMENDITEM].addLink("itemTaskStepResult", tables[ITEMTASKSTEPRESULT], "recommendItems", new int[] {6});
        tables[SCHDATE].addLink("schDateTime", tables[SCHDATETIME], "schDate", new int[] {0});
        tables[SCHDATETIME].addLink("includeSchDateTimes", tables[SCHDATETIMES], "includeSchDateTimes", new int[] {1});
        tables[SCHDATETIME].addLink("excludeSchDateTimes", tables[SCHDATETIMES], "excludeSchDateTimes", new int[] {2});
        tables[SCHDATETIME].addLink("schDate", tables[SCHDATE], "schDateTime", new int[] {3});
        tables[SCHDATETIME].addLink("schTime", tables[SCHTIME], "schDateTime", new int[] {4});
        tables[SCHDATETIMES].addLink("examItemTasks", tables[EXAMITEMTASK], "schDateTimes", new int[] {0});
        tables[SCHDATETIMES].addLink("itemTasks", tables[ITEMTASK], "schDateTimes", new int[] {0});
        tables[SCHDATETIMES].addLink("includeSchDateTimes", tables[SCHDATETIME], "includeSchDateTimes", new int[] {0});
        tables[SCHDATETIMES].addLink("excludeSchDateTimes", tables[SCHDATETIME], "excludeSchDateTimes", new int[] {0});
        tables[SCHTIME].addLink("schDateTime", tables[SCHDATETIME], "schTime", new int[] {0});
        tables[SECTION].addLink("templateRow", tables[TEMPLATEROW], "sections", new int[] {8});
        tables[SECTION].addLink("sectionItems", tables[SECTIONITEM], "section", new int[] {0});
        tables[SECTIONITEM].addLink("examItems", tables[EXAMITEM], "sectionItem", new int[] {0});
        tables[SECTIONITEM].addLink("examItemStatus", tables[EXAMITEMSTATUS], "sectionItems", new int[] {6});
        tables[SECTIONITEM].addLink("item", tables[ITEM], "sectionItems", new int[] {7});
        tables[SECTIONITEM].addLink("labTestSpecies", tables[LABTESTSPECIES], "sectionItems", new int[] {8});
        tables[SECTIONITEM].addLink("autoSelects", tables[AUTOSELECT], "sectionItem", new int[] {0});
        tables[SECTIONITEM].addLink("section", tables[SECTION], "sectionItems", new int[] {9});
        tables[SERVICE].addLink("items", tables[ITEM], "service", new int[] {0});
        tables[SERVICE].addLink("serviceCategory", tables[SERVICECATEGORY], "services", new int[] {7});
        tables[SERVICE].addLink("serviceProducts", tables[SERVICEPRODUCT], "service", new int[] {0});
        tables[SERVICE].addLink("reverseServices", tables[LINKSERVICESREVERSESERVICES], "services", new int[] {0});
        tables[SERVICE].addLink("services", tables[LINKSERVICESREVERSESERVICES], "reverseServices", new int[] {0});
        tables[SERVICECATEGORY].addLink("services", tables[SERVICE], "serviceCategory", new int[] {0});
        tables[SERVICECATEGORY].addLink("parentServiceCategory", tables[SERVICECATEGORY], "serviceCategories", new int[] {3});
        tables[SERVICECATEGORY].addLink("serviceCategories", tables[SERVICECATEGORY], "parentServiceCategory", new int[] {0});
        tables[SERVICEPRODUCT].addLink("service", tables[SERVICE], "serviceProducts", new int[] {4});
        tables[SERVICEPRODUCT].addLink("productPackage", tables[PRODUCTPACKAGE], "serviceProducts", new int[] {5});
        tables[SPECIES].addLink("pets", tables[PET], "species", new int[] {0});
        tables[SPECIES].addLink("templateCategories", tables[TEMPLATECATEGORY], "species", new int[] {0});
        tables[SPECIES].addLink("breeds", tables[BREED], "species", new int[] {0});
        tables[SPECIES].addLink("labTestSpecies", tables[LABTESTSPECIES], "species", new int[] {0});
        tables[SPECIES].addLink("itemProducts", tables[ITEMPRODUCT], "species", new int[] {0});
        tables[TEMPLATE].addLink("examTemplates", tables[EXAMTEMPLATE], "template", new int[] {0});
        tables[TEMPLATE].addLink("templateCategories", tables[LINKTEMPLATESTEMPLATECATEGORIES], "templates", new int[] {0});
        tables[TEMPLATE].addLink("templateRows", tables[TEMPLATEROW], "template", new int[] {0});
        tables[TEMPLATECATEGORY].addLink("species", tables[SPECIES], "templateCategories", new int[] {4});
        tables[TEMPLATECATEGORY].addLink("templates", tables[LINKTEMPLATESTEMPLATECATEGORIES], "templateCategories", new int[] {0});
        tables[TEMPLATECATEGORY].addLink("parentTemplateCategory", tables[TEMPLATECATEGORY], "templateCategories", new int[] {5});
        tables[TEMPLATECATEGORY].addLink("templateCategories", tables[TEMPLATECATEGORY], "parentTemplateCategory", new int[] {0});
        tables[TEMPLATEROW].addLink("template", tables[TEMPLATE], "templateRows", new int[] {4});
        tables[TEMPLATEROW].addLink("sections", tables[SECTION], "templateRow", new int[] {0});
        tables[USER].addLink("vetExams", tables[EXAM], "vetUser", new int[] {0});
        tables[USER].addLink("techExams", tables[EXAM], "techUser", new int[] {0});
        tables[USER].addLink("receptionistExams", tables[EXAM], "receptionistUser", new int[] {0});
        tables[USER].addLink("preparedByExams", tables[EXAM], "preparedByUser", new int[] {0});
        tables[USER].addLink("examItems", tables[EXAMITEM], "user", new int[] {0});
        tables[USER].addLink("examTemplates", tables[EXAMTEMPLATE], "user", new int[] {0});
        tables[USER].addLink("versions", tables[VERSION], "user", new int[] {0});
        tables[USER].addLink("examItemHistories", tables[EXAMITEMHISTORY], "user", new int[] {0});
        tables[USER].addLink("examItemTaskStepResults", tables[EXAMITEMTASKSTEPRESULT], "user", new int[] {0});
        tables[VERSION].addLink("user", tables[USER], "versions", new int[] {4});
        tables[VERSION].addLink("versionStatus", tables[VERSIONSTATUS], "versions", new int[] {5});
        tables[VERSION].addLink("item", tables[ITEM], "versions", new int[] {6});
        tables[VERSIONSTATUS].addLink("versions", tables[VERSION], "versionStatus", new int[] {0});
        
        // Links for Link Tables
        
        // LinkItemsItemCategories LINKS
        tables[LINKITEMSITEMCATEGORIES].addLink("items", tables[ITEM], "itemCategories", new int[] {0});
        tables[LINKITEMSITEMCATEGORIES].addLink("itemCategories", tables[ITEMCATEGORY], "items", new int[] {1});
        
        // LinkTemplatesTemplateCategories LINKS
        tables[LINKTEMPLATESTEMPLATECATEGORIES].addLink("templates", tables[TEMPLATE], "templateCategories", new int[] {0});
        tables[LINKTEMPLATESTEMPLATECATEGORIES].addLink("templateCategories", tables[TEMPLATECATEGORY], "templates", new int[] {1});
        
        // LinkProductsProductCategories LINKS
        tables[LINKPRODUCTSPRODUCTCATEGORIES].addLink("products", tables[PRODUCT], "productCategories", new int[] {0});
        tables[LINKPRODUCTSPRODUCTCATEGORIES].addLink("productCategories", tables[PRODUCTCATEGORY], "products", new int[] {1});
        
        // LinkServicesReverseServices LINKS
        tables[LINKSERVICESREVERSESERVICES].addLink("services", tables[SERVICE], "reverseServices", new int[] {0});
        tables[LINKSERVICESREVERSESERVICES].addLink("reverseServices", tables[SERVICE], "services", new int[] {1});
        
        db.setTables(tables);
        return db;
	}
	


}
