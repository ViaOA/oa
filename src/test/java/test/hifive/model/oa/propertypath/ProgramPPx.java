// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class ProgramPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private AddOnItemPPx addOnItems;
    private EmployeePPx anniversaryEmployees;
    private ProgramDocumentPPx announcementDocument;
    private AwardTypePPx awardTypes;
    private ProgramDocumentPPx blogDocuments;
    private CardPPx cards;
    private ImageStorePPx ceoImageStore;
    private ImageStorePPx ceoSignatureImageStore;
    private CharityPPx charities;
    private CompanyPPx company;
    private CountryCodePPx countryCode;
    private CustomDataPPx customData;
    private EcardPPx ecards;
    private EmployeePPx employees;
    private HifiveQualityPPx hifiveQualities;
    private HifiveReasonPPx hifiveReasons;
    private ImageStorePPx imageStores;
    private CardPPx imagineCard;
    private InspireAwardLevelPPx inspireAwardLevels;
    private AwardTypePPx inspireAwardType;
    private InspireCoreValuePPx inspireCoreValues;
    private LocationPPx locations;
    private LoginImageSetPPx loginImageSet;
    private ImageStorePPx logoImageStores;
    private ImageStorePPx logoStampImageStore;
    private SurveyPPx managerHifiveSurvey;
    private QuizPPx nominationQuiz;
    private PageThemePPx pageTheme;
    private PointsAwardLevelPPx pointsAwardLevels;
    private PointsConfigurationPPx pointsConfiguration;
    private PointsCoreValuePPx pointsCoreValues;
    private PointsRecordPPx pointsFromRecord;
    private ProgramEmailTypePPx programEmailTypes;
    private ProgramEventPPx programEvents;
    private ProgramFaqPPx programFaqs;
    private ProgramPageGroupPPx programPageGroups;
    private ProgramPageInfoPPx programPageInfos;
    private SurveyPPx quizSurveys;
    private InspireRecipientPPx recentInspireRecipients;
    private SurveyPPx surveys;
    private WidgetPPx widgets;
     
    public ProgramPPx(String name) {
        this(null, name);
    }

    public ProgramPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public AddOnItemPPx addOnItems() {
        if (addOnItems == null) addOnItems = new AddOnItemPPx(this, Program.P_AddOnItems);
        return addOnItems;
    }

    public EmployeePPx anniversaryEmployees() {
        if (anniversaryEmployees == null) anniversaryEmployees = new EmployeePPx(this, Program.P_AnniversaryEmployees);
        return anniversaryEmployees;
    }

    public ProgramDocumentPPx announcementDocument() {
        if (announcementDocument == null) announcementDocument = new ProgramDocumentPPx(this, Program.P_AnnouncementDocument);
        return announcementDocument;
    }

    public AwardTypePPx awardTypes() {
        if (awardTypes == null) awardTypes = new AwardTypePPx(this, Program.P_AwardTypes);
        return awardTypes;
    }

    public ProgramDocumentPPx blogDocuments() {
        if (blogDocuments == null) blogDocuments = new ProgramDocumentPPx(this, Program.P_BlogDocuments);
        return blogDocuments;
    }

    public CardPPx cards() {
        if (cards == null) cards = new CardPPx(this, Program.P_Cards);
        return cards;
    }

    public ImageStorePPx ceoImageStore() {
        if (ceoImageStore == null) ceoImageStore = new ImageStorePPx(this, Program.P_CeoImageStore);
        return ceoImageStore;
    }

    public ImageStorePPx ceoSignatureImageStore() {
        if (ceoSignatureImageStore == null) ceoSignatureImageStore = new ImageStorePPx(this, Program.P_CeoSignatureImageStore);
        return ceoSignatureImageStore;
    }

    public CharityPPx charities() {
        if (charities == null) charities = new CharityPPx(this, Program.P_Charities);
        return charities;
    }

    public CompanyPPx company() {
        if (company == null) company = new CompanyPPx(this, Program.P_Company);
        return company;
    }

    public CountryCodePPx countryCode() {
        if (countryCode == null) countryCode = new CountryCodePPx(this, Program.P_CountryCode);
        return countryCode;
    }

    public CustomDataPPx customData() {
        if (customData == null) customData = new CustomDataPPx(this, Program.P_CustomData);
        return customData;
    }

    public EcardPPx ecards() {
        if (ecards == null) ecards = new EcardPPx(this, Program.P_Ecards);
        return ecards;
    }

    public EmployeePPx employees() {
        if (employees == null) employees = new EmployeePPx(this, Program.P_Employees);
        return employees;
    }

    public HifiveQualityPPx hifiveQualities() {
        if (hifiveQualities == null) hifiveQualities = new HifiveQualityPPx(this, Program.P_HifiveQualities);
        return hifiveQualities;
    }

    public HifiveReasonPPx hifiveReasons() {
        if (hifiveReasons == null) hifiveReasons = new HifiveReasonPPx(this, Program.P_HifiveReasons);
        return hifiveReasons;
    }

    public ImageStorePPx imageStores() {
        if (imageStores == null) imageStores = new ImageStorePPx(this, Program.P_ImageStores);
        return imageStores;
    }

    public CardPPx imagineCard() {
        if (imagineCard == null) imagineCard = new CardPPx(this, Program.P_ImagineCard);
        return imagineCard;
    }

    public InspireAwardLevelPPx inspireAwardLevels() {
        if (inspireAwardLevels == null) inspireAwardLevels = new InspireAwardLevelPPx(this, Program.P_InspireAwardLevels);
        return inspireAwardLevels;
    }

    public AwardTypePPx inspireAwardType() {
        if (inspireAwardType == null) inspireAwardType = new AwardTypePPx(this, Program.P_InspireAwardType);
        return inspireAwardType;
    }

    public InspireCoreValuePPx inspireCoreValues() {
        if (inspireCoreValues == null) inspireCoreValues = new InspireCoreValuePPx(this, Program.P_InspireCoreValues);
        return inspireCoreValues;
    }

    public LocationPPx locations() {
        if (locations == null) locations = new LocationPPx(this, Program.P_Locations);
        return locations;
    }

    public LoginImageSetPPx loginImageSet() {
        if (loginImageSet == null) loginImageSet = new LoginImageSetPPx(this, Program.P_LoginImageSet);
        return loginImageSet;
    }

    public ImageStorePPx logoImageStores() {
        if (logoImageStores == null) logoImageStores = new ImageStorePPx(this, Program.P_LogoImageStores);
        return logoImageStores;
    }

    public ImageStorePPx logoStampImageStore() {
        if (logoStampImageStore == null) logoStampImageStore = new ImageStorePPx(this, Program.P_LogoStampImageStore);
        return logoStampImageStore;
    }

    public SurveyPPx managerHifiveSurvey() {
        if (managerHifiveSurvey == null) managerHifiveSurvey = new SurveyPPx(this, Program.P_ManagerHifiveSurvey);
        return managerHifiveSurvey;
    }

    public QuizPPx nominationQuiz() {
        if (nominationQuiz == null) nominationQuiz = new QuizPPx(this, Program.P_NominationQuiz);
        return nominationQuiz;
    }

    public PageThemePPx pageTheme() {
        if (pageTheme == null) pageTheme = new PageThemePPx(this, Program.P_PageTheme);
        return pageTheme;
    }

    public PointsAwardLevelPPx pointsAwardLevels() {
        if (pointsAwardLevels == null) pointsAwardLevels = new PointsAwardLevelPPx(this, Program.P_PointsAwardLevels);
        return pointsAwardLevels;
    }

    public PointsConfigurationPPx pointsConfiguration() {
        if (pointsConfiguration == null) pointsConfiguration = new PointsConfigurationPPx(this, Program.P_PointsConfiguration);
        return pointsConfiguration;
    }

    public PointsCoreValuePPx pointsCoreValues() {
        if (pointsCoreValues == null) pointsCoreValues = new PointsCoreValuePPx(this, Program.P_PointsCoreValues);
        return pointsCoreValues;
    }

    public PointsRecordPPx pointsFromRecord() {
        if (pointsFromRecord == null) pointsFromRecord = new PointsRecordPPx(this, Program.P_PointsFromRecord);
        return pointsFromRecord;
    }

    public ProgramEmailTypePPx programEmailTypes() {
        if (programEmailTypes == null) programEmailTypes = new ProgramEmailTypePPx(this, Program.P_ProgramEmailTypes);
        return programEmailTypes;
    }

    public ProgramEventPPx programEvents() {
        if (programEvents == null) programEvents = new ProgramEventPPx(this, Program.P_ProgramEvents);
        return programEvents;
    }

    public ProgramFaqPPx programFaqs() {
        if (programFaqs == null) programFaqs = new ProgramFaqPPx(this, Program.P_ProgramFaqs);
        return programFaqs;
    }

    public ProgramPageGroupPPx programPageGroups() {
        if (programPageGroups == null) programPageGroups = new ProgramPageGroupPPx(this, Program.P_ProgramPageGroups);
        return programPageGroups;
    }

    public ProgramPageInfoPPx programPageInfos() {
        if (programPageInfos == null) programPageInfos = new ProgramPageInfoPPx(this, Program.P_ProgramPageInfos);
        return programPageInfos;
    }

    public SurveyPPx quizSurveys() {
        if (quizSurveys == null) quizSurveys = new SurveyPPx(this, Program.P_QuizSurveys);
        return quizSurveys;
    }

    public InspireRecipientPPx recentInspireRecipients() {
        if (recentInspireRecipients == null) recentInspireRecipients = new InspireRecipientPPx(this, Program.P_RecentInspireRecipients);
        return recentInspireRecipients;
    }

    public SurveyPPx surveys() {
        if (surveys == null) surveys = new SurveyPPx(this, Program.P_Surveys);
        return surveys;
    }

    public WidgetPPx widgets() {
        if (widgets == null) widgets = new WidgetPPx(this, Program.P_Widgets);
        return widgets;
    }

    public String id() {
        return pp + "." + Program.P_Id;
    }

    public String created() {
        return pp + "." + Program.P_Created;
    }

    public String code() {
        return pp + "." + Program.P_Code;
    }

    public String name() {
        return pp + "." + Program.P_Name;
    }

    public String beginDate() {
        return pp + "." + Program.P_BeginDate;
    }

    public String endDate() {
        return pp + "." + Program.P_EndDate;
    }

    public String awardBeginDate() {
        return pp + "." + Program.P_AwardBeginDate;
    }

    public String inactiveDate() {
        return pp + "." + Program.P_InactiveDate;
    }

    public String urlName() {
        return pp + "." + Program.P_UrlName;
    }

    public String fromEmailAddress() {
        return pp + "." + Program.P_FromEmailAddress;
    }

    public String pointsName() {
        return pp + "." + Program.P_PointsName;
    }

    public String pointValue() {
        return pp + "." + Program.P_PointValue;
    }

    public String usesInspire() {
        return pp + "." + Program.P_UsesInspire;
    }

    public String usesHifive() {
        return pp + "." + Program.P_UsesHifive;
    }

    public String usesSurveys() {
        return pp + "." + Program.P_UsesSurveys;
    }

    public String ecardType() {
        return pp + "." + Program.P_EcardType;
    }

    public String hifiveName() {
        return pp + "." + Program.P_HifiveName;
    }

    public String birthdayDisplayDays() {
        return pp + "." + Program.P_BirthdayDisplayDays;
    }

    public String anniversaryDisplayDays() {
        return pp + "." + Program.P_AnniversaryDisplayDays;
    }

    public String seq() {
        return pp + "." + Program.P_Seq;
    }

    public String loginMessage() {
        return pp + "." + Program.P_LoginMessage;
    }

    public String employeeAwardExpireDays() {
        return pp + "." + Program.P_EmployeeAwardExpireDays;
    }

    public String charityGoal() {
        return pp + "." + Program.P_CharityGoal;
    }

    public String charityTotal() {
        return pp + "." + Program.P_CharityTotal;
    }

    public String itemUpcharge() {
        return pp + "." + Program.P_ItemUpcharge;
    }

    public String cardUpcharge() {
        return pp + "." + Program.P_CardUpcharge;
    }

    public String charityUpcharge() {
        return pp + "." + Program.P_CharityUpcharge;
    }

    public String cashUpcharge() {
        return pp + "." + Program.P_CashUpcharge;
    }

    public String testEmail() {
        return pp + "." + Program.P_TestEmail;
    }

    public String useAdvancedReports() {
        return pp + "." + Program.P_UseAdvancedReports;
    }

    public String packetInstructions() {
        return pp + "." + Program.P_PacketInstructions;
    }

    public String ssoFailureUrl() {
        return pp + "." + Program.P_SsoFailureUrl;
    }

    public String ssoCode() {
        return pp + "." + Program.P_SsoCode;
    }

    public String ssoRedirectUrl() {
        return pp + "." + Program.P_SsoRedirectUrl;
    }

    public String usesPoints() {
        return pp + "." + Program.P_UsesPoints;
    }

    public String pointsBillingType() {
        return pp + "." + Program.P_PointsBillingType;
    }

    public String ssoLogoutUrl() {
        return pp + "." + Program.P_SsoLogoutUrl;
    }

    public String usesPeerToPeer() {
        return pp + "." + Program.P_UsesPeerToPeer;
    }

    public String usesDiscretionary() {
        return pp + "." + Program.P_UsesDiscretionary;
    }

    public String employeeAwardDaysBefore() {
        return pp + "." + Program.P_EmployeeAwardDaysBefore;
    }

    public String companyPaysShipping() {
        return pp + "." + Program.P_CompanyPaysShipping;
    }

    public String usesNominations() {
        return pp + "." + Program.P_UsesNominations;
    }

    public String usesManagerToolkit() {
        return pp + "." + Program.P_UsesManagerToolkit;
    }

    public String calcCharityTotal() {
        return pp + "." + Program.P_CalcCharityTotal;
    }

    public String programPoints() {
        return pp + "." + Program.P_ProgramPoints;
    }

    public String runSqlServerImport() {
        return pp + ".runSqlServerImport";
    }

    public String activeFilter() {
        return pp + ":active()";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
