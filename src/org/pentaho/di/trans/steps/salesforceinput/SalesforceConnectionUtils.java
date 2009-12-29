/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.trans.steps.salesforceinput;

import java.util.Hashtable;

import org.pentaho.di.i18n.BaseMessages;


public class SalesforceConnectionUtils {
	
	private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public static final String LIB_VERION="16.0";
	
	public static final String TARGET_DEFAULT_URL= "https://www.salesforce.com/services/Soap/u/16.0";
	
	/**
	 * The records filter description
	 */
	public final static String recordsFilterDesc[] = {
			BaseMessages.getString(PKG, "SalesforceInputMeta.recordsFilter.All"),
			BaseMessages.getString(PKG, "SalesforceInputMeta.recordsFilter.Updated"),
			BaseMessages.getString(PKG, "SalesforceInputMeta.recordsFilter.Deleted")};
	
	/**
	 * The records filter type codes
	 */
	public final static String recordsFilterCode[] = { "all", "updated", "deleted" };

	public final static int RECORDS_FILTER_ALL = 0;

	public final static int RECORDS_FILTER_UPDATED = 1;

	public final static int RECORDS_FILTER_DELETED = 2;
	
	public static final String[] modulesList = {
		"Account",
		"AccountContactRole",
		"AccountPartner",
		"AccountShare",
		"AdditionalNumber",
		"ApexClass",
		"ApexTrigger",
		"Approval",
		"Asset",
		"AssignmentRule",
		"Attachment",
		"BrandTemplate",
		"BusinessHours",
		"BusinessProcess",
		"CallCenter",
		"Campaign",
		"CampaignMember",
		"CampaignMemberStatus",
		"Case",
		"CaseComment",
		"CaseContactRole",
		"CaseHistory",
		"CaseShare",
		"CaseSolution",
		"CaseStatus",
		"CategoryData",
		"CategoryNode",
		"Contact",
		"ContactShare",
		"Contract",
		"ContractContactRole",
		"ContractHistory",
		"ContractStatus",
		"Document",
		"DocumentAttachmentMap",
		"EmailTemplate",
		"Event",
		"EventAttendee",
		"FiscalYearSettings",
		"Folder",
		"ForecastShare",
		"Group",
		"GroupMember",
		"Lead",
		"LeadHistory",
		"LeadShare",
		"LeadStatus",
		"MailmergeTemplate",
		"Note",
		"Opportunity",
		"OpportunityCompetitor",
		"OpportunityContactRole",
		"OpportunityHistory",
		"OpportunityLineItem",
		"OpportunityPartner",
		"OpportunityShare",
		"OpportunityStage",
		"Organization",
		"Partner",
		"PartnerRole",
		"Period",
		"Pricebook2",
		"PricebookEntry",
		"ProcessInstance",
		"ProcessInstanceStep",
		"ProcessInstanceWorkitem",
		"Product2",
		"Profile",
		"QueueSobject",
		"RecordType",
		"Scontrol",
		"SelfServiceUser",
		"Solution",
		"SolutionHistory",
		"SolutionStatus",
		"Task",
		"TaskPriority",
		"TaskStatus",
		"User",
		"UserLicense",
		"UserPreference",
		"UserRole",
		"WebLink"
	};
	public static String getRecordsFilterDesc(int i) {
		if (i < 0 || i >= recordsFilterDesc.length)
			return recordsFilterDesc[0];
		return recordsFilterDesc[i];
	}
	public static int getRecordsFilterByDesc(String tt) {
		if (tt == null)
			return 0;
	
		for (int i = 0; i < recordsFilterDesc.length; i++) {
			if (recordsFilterDesc[i].equalsIgnoreCase(tt))
				return i;
		}
		// If this fails, try to match using the code.
		return getRecordsFilterByCode(tt);
	}

	public static int getRecordsFilterByCode(String tt) {
		if (tt == null)
			return 0;
	
		for (int i = 0; i < recordsFilterCode.length; i++) {
			if (recordsFilterCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	public static String getRecordsFilterCode(int i) {
		if (i < 0 || i >= recordsFilterCode.length)
			return recordsFilterCode[0];
		return recordsFilterCode[i];
	}
	
	
	static private Hashtable<String, String[]>	fieldsHash;
	static {
		fieldsHash = new Hashtable<String, String[]>();
		fieldsHash.put("ACCOUNT",		new String[] {"Id" ,"IsDeleted" ,"MasterRecordId" ,"Name" ,"Type" ,"ParentId" ,"BillingStreet" ,"BillingCity" ,"BillingState" ,"BillingPostalCode" ,"BillingCountry" ,"ShippingStreet" ,"ShippingCity" ,"ShippingState" ,"ShippingPostalCode" ,"ShippingCountry" ,"Phone" ,"Fax" ,"AccountNumber" ,"Website" ,"Sic" ,"Industry" ,"AnnualRevenue" ,"NumberOfEmployees" ,"Ownership" ,"TickerSymbol" ,"Description" ,"Rating" ,"Site" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"LastActivityDate" ,"CustomerPriority__c" ,"SLA__c" ,"Active__c" ,"NumberofLocations__c" ,"UpsellOpportunity__c" ,"SLASerialNumber__c" ,"SLAExpirationDate__c"}); 
		fieldsHash.put("ACCOUNTCONTACTROLE",		new String[] {"Id" ,"AccountId" ,"ContactId" ,"Role" ,"IsPrimary" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("ACCOUNTPARTNER",		new String[] {"Id" ,"AccountFromId" ,"AccountToId" ,"OpportunityId" ,"Role" ,"IsPrimary" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted" ,"ReversePartnerId"}); 
		fieldsHash.put("ACCOUNTSHARE",		new String[] {"Id" ,"AccountId" ,"UserOrGroupId" ,"AccountAccessLevel" ,"OpportunityAccessLevel" ,"CaseAccessLevel" ,"ContactAccessLevel" ,"RowCause" ,"LastModifiedDate" ,"LastModifiedById" ,"IsDeleted"}); 
		fieldsHash.put("ADDITIONALNUMBER",		new String[] {"Id" ,"IsDeleted" ,"CallCenterId" ,"Name" ,"Description" ,"Phone" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("APEXCLASS",		new String[] {"Id" ,"NamespacePrefix" ,"Name" ,"ApiVersion" ,"Status" ,"IsValid" ,"BodyCrc" ,"Body" ,"LengthWithoutComments" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("APEXTRIGGER",		new String[] {"Id" ,"NamespacePrefix" ,"Name" ,"TableEnumOrId" ,"UsageBeforeInsert" ,"UsageAfterInsert" ,"UsageBeforeUpdate" ,"UsageAfterUpdate" ,"UsageBeforeDelete" ,"UsageAfterDelete" ,"UsageIsBulk" ,"UsageAfterUndelete" ,"ApiVersion" ,"Status" ,"IsValid" ,"BodyCrc" ,"Body" ,"LengthWithoutComments" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("APPROVAL",		new String[] {"Id" ,"IsDeleted" ,"ParentId" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"Status" ,"RequestComment" ,"ApproveComment" ,"SystemModstamp"}); 
		fieldsHash.put("ASSET",		new String[] {"Id" ,"ContactId" ,"AccountId" ,"Product2Id" ,"IsCompetitorProduct" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted" ,"Name" ,"SerialNumber" ,"InstallDate" ,"PurchaseDate" ,"UsageEndDate" ,"Status" ,"Price" ,"Quantity" ,"Description"}); 
		fieldsHash.put("ASSIGNMENTRULE",		new String[] {"Id" ,"Name" ,"SobjectType" ,"Active" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("ATTACHMENT",		new String[] {"Id" ,"IsDeleted" ,"ParentId" ,"Name" ,"IsPrivate" ,"ContentType" ,"BodyLength" ,"Body" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("BRANDTEMPLATE",		new String[] {"Id" ,"Name" ,"DeveloperName" ,"IsActive" ,"Description" ,"Value" ,"NamespacePrefix" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("BUSINESSHOURS",		new String[] {"Id" ,"Name" ,"IsActive" ,"IsDefault" ,"SundayStartTime" ,"SundayEndTime" ,"MondayStartTime" ,"MondayEndTime" ,"TuesdayStartTime" ,"TuesdayEndTime" ,"WednesdayStartTime" ,"WednesdayEndTime" ,"ThursdayStartTime" ,"ThursdayEndTime" ,"FridayStartTime" ,"FridayEndTime" ,"SaturdayStartTime" ,"SaturdayEndTime" ,"TimeZoneSidKey" ,"SystemModstamp" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById"}); 
		fieldsHash.put("BUSINESSPROCESS",		new String[] {"Id" ,"Name" ,"Description" ,"TableEnumOrId" ,"IsActive" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("CALLCENTER",		new String[] {"Id" ,"Name" ,"InternalName" ,"SystemModstamp" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById"}); 
		fieldsHash.put("CAMPAIGN",		new String[] {"Id" ,"IsDeleted" ,"Name" ,"Type" ,"Status" ,"StartDate" ,"EndDate" ,"ExpectedRevenue" ,"BudgetedCost" ,"ActualCost" ,"ExpectedResponse" ,"NumberSent" ,"IsActive" ,"Description" ,"NumberOfLeads" ,"NumberOfConvertedLeads" ,"NumberOfContacts" ,"NumberOfResponses" ,"NumberOfOpportunities" ,"NumberOfWonOpportunities" ,"AmountAllOpportunities" ,"AmountWonOpportunities" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"LastActivityDate"}); 
		fieldsHash.put("CAMPAIGNMEMBER",		new String[] {"Id" ,"IsDeleted" ,"CampaignId" ,"LeadId" ,"ContactId" ,"Status" ,"HasResponded" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"FirstRespondedDate"}); 
		fieldsHash.put("CAMPAIGNMEMBERSTATUS",		new String[] {"Id" ,"IsDeleted" ,"CampaignId" ,"Label" ,"SortOrder" ,"IsDefault" ,"HasResponded" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("CASE",		new String[] {"Id" ,"IsDeleted" ,"CaseNumber" ,"ContactId" ,"AccountId" ,"AssetId" ,"SuppliedName" ,"SuppliedEmail" ,"SuppliedPhone" ,"SuppliedCompany" ,"Type" ,"Status" ,"Reason" ,"Origin" ,"Subject" ,"Priority" ,"Description" ,"IsClosed" ,"ClosedDate" ,"IsEscalated" ,"HasCommentsUnreadByOwner" ,"HasSelfServiceComments" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"EngineeringReqNumber__c" ,"SLAViolation__c" ,"Product__c" ,"PotentialLiability__c"}); 
		fieldsHash.put("CASECOMMENT",		new String[] {"Id" ,"ParentId" ,"IsPublished" ,"CommentBody" ,"CreatedById" ,"CreatedDate" ,"SystemModstamp" ,"LastModifiedDate" ,"LastModifiedById" ,"IsDeleted"}); 
		fieldsHash.put("CASECONTACTROLE",		new String[] {"Id" ,"CasesId" ,"ContactId" ,"Role" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("CASEHISTORY",		new String[] {"Id" ,"IsDeleted" ,"CaseId" ,"CreatedById" ,"CreatedDate" ,"Field" ,"OldValue" ,"NewValue"}); 
		fieldsHash.put("CASESHARE",		new String[] {"Id" ,"CaseId" ,"UserOrGroupId" ,"CaseAccessLevel" ,"RowCause" ,"LastModifiedDate" ,"LastModifiedById" ,"IsDeleted"}); 
		fieldsHash.put("CASESOLUTION",		new String[] {"Id" ,"CaseId" ,"SolutionId" ,"CreatedById" ,"CreatedDate" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("CASESTATUS",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"IsDefault" ,"IsClosed" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("CATEGORYDATA",		new String[] {"Id" ,"CategoryNodeId" ,"RelatedSobjectId" ,"IsDeleted" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("CATEGORYNODE",		new String[] {"Id" ,"ParentId" ,"MasterLabel" ,"SortOrder" ,"SortStyle" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("CONTACT",		new String[] {"Id" ,"IsDeleted" ,"MasterRecordId" ,"AccountId" ,"LastName" ,"FirstName" ,"Salutation" ,"Name" ,"OtherStreet" ,"OtherCity" ,"OtherState" ,"OtherPostalCode" ,"OtherCountry" ,"MailingStreet" ,"MailingCity" ,"MailingState" ,"MailingPostalCode" ,"MailingCountry" ,"Phone" ,"Fax" ,"MobilePhone" ,"HomePhone" ,"OtherPhone" ,"AssistantPhone" ,"ReportsToId" ,"Email" ,"Title" ,"Department" ,"AssistantName" ,"LeadSource" ,"Birthdate" ,"Description" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"LastActivityDate" ,"LastCURequestDate" ,"LastCUUpdateDate" ,"EmailBouncedReason" ,"EmailBouncedDate" ,"Level__c" ,"Languages__c"}); 
		fieldsHash.put("CONTACTSHARE",		new String[] {"Id" ,"ContactId" ,"UserOrGroupId" ,"ContactAccessLevel" ,"RowCause" ,"LastModifiedDate" ,"LastModifiedById" ,"IsDeleted"}); 
		fieldsHash.put("CONTRACT",		new String[] {"Id" ,"AccountId" ,"OwnerExpirationNotice" ,"StartDate" ,"EndDate" ,"BillingStreet" ,"BillingCity" ,"BillingState" ,"BillingPostalCode" ,"BillingCountry" ,"ContractTerm" ,"OwnerId" ,"Status" ,"CompanySignedId" ,"CompanySignedDate" ,"CustomerSignedId" ,"CustomerSignedTitle" ,"CustomerSignedDate" ,"SpecialTerms" ,"ActivatedById" ,"ActivatedDate" ,"StatusCode" ,"Description" ,"IsDeleted" ,"ContractNumber" ,"LastApprovedDate" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"LastActivityDate"}); 
		fieldsHash.put("CONTRACTCONTACTROLE",		new String[] {"Id" ,"ContractId" ,"ContactId" ,"Role" ,"IsPrimary" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("CONTRACTHISTORY",		new String[] {"Id" ,"IsDeleted" ,"ContractId" ,"CreatedById" ,"CreatedDate" ,"Field" ,"OldValue" ,"NewValue"}); 
		fieldsHash.put("CONTRACTSTATUS",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"IsDefault" ,"StatusCode" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("DOCUMENT",		new String[] {"Id" ,"FolderId" ,"IsDeleted" ,"Name" ,"DeveloperName" ,"NamespacePrefix" ,"ContentType" ,"Type" ,"IsPublic" ,"BodyLength" ,"Body" ,"Url" ,"Description" ,"Keywords" ,"IsInternalUseOnly" ,"AuthorId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsBodySearchable"}); 
		fieldsHash.put("DOCUMENTATTACHMENTMAP",		new String[] {"Id" ,"ParentId" ,"DocumentId" ,"DocumentSequence" ,"CreatedDate" ,"CreatedById"}); 
		fieldsHash.put("EMAILTEMPLATE",		new String[] {"Id" ,"Name" ,"DeveloperName" ,"NamespacePrefix" ,"OwnerId" ,"FolderId" ,"BrandTemplateId" ,"TemplateStyle" ,"IsActive" ,"TemplateType" ,"Encoding" ,"Description" ,"Subject" ,"HtmlValue" ,"Body" ,"TimesUsed" ,"LastUsedDate" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"ApiVersion" ,"Markup"}); 
		fieldsHash.put("EVENT",		new String[] {"Id" ,"WhoId" ,"WhatId" ,"Subject" ,"Location" ,"IsAllDayEvent" ,"ActivityDateTime" ,"ActivityDate" ,"DurationInMinutes" ,"StartDateTime" ,"EndDateTime" ,"Description" ,"AccountId" ,"OwnerId" ,"IsPrivate" ,"ShowAs" ,"IsDeleted" ,"IsChild" ,"IsGroupEvent" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsArchived" ,"RecurrenceActivityId" ,"IsRecurrence" ,"RecurrenceStartDateTime" ,"RecurrenceEndDateOnly" ,"RecurrenceTimeZoneSidKey" ,"RecurrenceType" ,"RecurrenceInterval" ,"RecurrenceDayOfWeekMask" ,"RecurrenceDayOfMonth" ,"RecurrenceInstance" ,"RecurrenceMonthOfYear" ,"ReminderDateTime" ,"IsReminderSet"}); 
		fieldsHash.put("EVENTATTENDEE",		new String[] {"Id" ,"EventId" ,"AttendeeId" ,"Status" ,"RespondedDate" ,"Response" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("FISCALYEARSETTINGS",		new String[] {"Id" ,"PeriodId" ,"StartDate" ,"EndDate" ,"Name" ,"IsStandardYear" ,"YearType" ,"QuarterLabelScheme" ,"PeriodLabelScheme" ,"WeekLabelScheme" ,"QuarterPrefix" ,"PeriodPrefix" ,"WeekStartDay" ,"Description" ,"SystemModstamp"}); 
		fieldsHash.put("FOLDER",		new String[] {"Id" ,"Name" ,"DeveloperName" ,"AccessType" ,"IsReadonly" ,"Type" ,"NamespacePrefix" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("FORECASTSHARE",		new String[] {"Id" ,"UserRoleId" ,"UserOrGroupId" ,"AccessLevel" ,"CanSubmit" ,"RowCause" ,"LastModifiedDate" ,"LastModifiedById"}); 
		fieldsHash.put("GROUP",		new String[] {"Id" ,"Name" ,"RelatedId" ,"Type" ,"Email" ,"OwnerId" ,"DoesSendEmailToMembers" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("GROUPMEMBER",		new String[] {"Id" ,"GroupId" ,"UserOrGroupId" ,"SystemModstamp"}); 
		fieldsHash.put("LEAD",		new String[] {"Id" ,"IsDeleted" ,"MasterRecordId" ,"LastName" ,"FirstName" ,"Salutation" ,"Name" ,"Title" ,"Company" ,"Street" ,"City" ,"State" ,"PostalCode" ,"Country" ,"Phone" ,"MobilePhone" ,"Fax" ,"Email" ,"Website" ,"Description" ,"LeadSource" ,"Status" ,"Industry" ,"Rating" ,"AnnualRevenue" ,"NumberOfEmployees" ,"OwnerId" ,"IsConverted" ,"ConvertedDate" ,"ConvertedAccountId" ,"ConvertedContactId" ,"ConvertedOpportunityId" ,"IsUnreadByOwner" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"LastActivityDate" ,"EmailBouncedReason" ,"EmailBouncedDate" ,"SICCode__c" ,"ProductInterest__c" ,"Primary__c" ,"CurrentGenerators__c" ,"NumberofLocations__c"}); 
		fieldsHash.put("LEADHISTORY",		new String[] {"Id" ,"IsDeleted" ,"LeadId" ,"CreatedById" ,"CreatedDate" ,"Field" ,"OldValue" ,"NewValue"}); 
		fieldsHash.put("LEADSHARE",		new String[] {"Id" ,"LeadId" ,"UserOrGroupId" ,"LeadAccessLevel" ,"RowCause" ,"LastModifiedDate" ,"LastModifiedById" ,"IsDeleted"}); 
		fieldsHash.put("LEADSTATUS",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"IsDefault" ,"IsConverted" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("MAILMERGETEMPLATE",		new String[] {"Id" ,"IsDeleted" ,"Name" ,"Description" ,"Filename" ,"BodyLength" ,"Body" ,"LastUsedDate" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("NOTE",		new String[] {"Id" ,"IsDeleted" ,"ParentId" ,"Title" ,"IsPrivate" ,"Body" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("OPPORTUNITY",		new String[] {"Id" ,"IsDeleted" ,"AccountId" ,"IsPrivate" ,"Name" ,"Description" ,"StageName" ,"Amount" ,"Probability" ,"ExpectedRevenue" ,"TotalOpportunityQuantity" ,"CloseDate" ,"Type" ,"NextStep" ,"LeadSource" ,"IsClosed" ,"IsWon" ,"ForecastCategory" ,"ForecastCategoryName" ,"CampaignId" ,"HasOpportunityLineItem" ,"Pricebook2Id" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"LastActivityDate" ,"FiscalQuarter" ,"FiscalYear" ,"Fiscal" ,"DeliveryInstallationStatus__c" ,"TrackingNumber__c" ,"OrderNumber__c" ,"CurrentGenerators__c" ,"MainCompetitors__c"}); 
		fieldsHash.put("OPPORTUNITYCOMPETITOR",		new String[] {"Id" ,"OpportunityId" ,"CompetitorName" ,"Strengths" ,"Weaknesses" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("OPPORTUNITYCONTACTROLE",		new String[] {"Id" ,"OpportunityId" ,"ContactId" ,"Role" ,"IsPrimary" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("OPPORTUNITYHISTORY",		new String[] {"Id" ,"OpportunityId" ,"CreatedById" ,"CreatedDate" ,"StageName" ,"Amount" ,"ExpectedRevenue" ,"CloseDate" ,"Probability" ,"ForecastCategory" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("OPPORTUNITYLINEITEM",		new String[] {"Id" ,"OpportunityId" ,"SortOrder" ,"PricebookEntryId" ,"Quantity" ,"TotalPrice" ,"UnitPrice" ,"ListPrice" ,"ServiceDate" ,"Description" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted"}); 
		fieldsHash.put("OPPORTUNITYPARTNER",		new String[] {"Id" ,"OpportunityId" ,"AccountToId" ,"Role" ,"IsPrimary" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted" ,"ReversePartnerId"}); 
		fieldsHash.put("OPPORTUNITYSHARE",		new String[] {"Id" ,"OpportunityId" ,"UserOrGroupId" ,"OpportunityAccessLevel" ,"RowCause" ,"LastModifiedDate" ,"LastModifiedById" ,"IsDeleted"}); 
		fieldsHash.put("OPPORTUNITYSTAGE",		new String[] {"Id" ,"MasterLabel" ,"IsActive" ,"SortOrder" ,"IsClosed" ,"IsWon" ,"ForecastCategory" ,"ForecastCategoryName" ,"DefaultProbability" ,"Description" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("ORGANIZATION",		new String[] {"Id" ,"Name" ,"Division" ,"Street" ,"City" ,"State" ,"PostalCode" ,"Country" ,"Phone" ,"Fax" ,"PrimaryContact" ,"DefaultLocaleSidKey" ,"LanguageLocaleKey" ,"ReceivesInfoEmails" ,"ReceivesAdminInfoEmails" ,"PreferencesRequireOpportunityProducts" ,"FiscalYearStartMonth" ,"UsesStartDateAsFiscalYearName" ,"DefaultAccountAccess" ,"DefaultContactAccess" ,"DefaultOpportunityAccess" ,"DefaultLeadAccess" ,"DefaultCaseAccess" ,"DefaultCalendarAccess" ,"DefaultPricebookAccess" ,"DefaultCampaignAccess" ,"SystemModstamp" ,"ComplianceBccEmail" ,"UiSkin" ,"TrialExpirationDate" ,"OrganizationType" ,"WebToCaseDefaultOrigin" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById"}); 
		fieldsHash.put("PARTNER",		new String[] {"Id" ,"OpportunityId" ,"AccountFromId" ,"AccountToId" ,"Role" ,"IsPrimary" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsDeleted" ,"ReversePartnerId"}); 
		fieldsHash.put("PARTNERROLE",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"ReverseRole" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("PERIOD",		new String[] {"Id" ,"FiscalYearSettingsId" ,"Type" ,"StartDate" ,"EndDate" ,"SystemModstamp" ,"IsForecastPeriod" ,"QuarterLabel" ,"PeriodLabel" ,"Number"}); 
		fieldsHash.put("PRICEBOOK2",		new String[] {"Id" ,"Name" ,"IsActive" ,"LastModifiedDate" ,"SystemModstamp" ,"IsStandard" ,"Description" ,"LastModifiedById" ,"CreatedDate" ,"CreatedById" ,"IsDeleted"}); 
		fieldsHash.put("PRICEBOOKENTRY",		new String[] {"Id" ,"Name" ,"Pricebook2Id" ,"Product2Id" ,"UnitPrice" ,"IsActive" ,"UseStandardPrice" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"ProductCode" ,"IsDeleted"}); 
		fieldsHash.put("PROCESSINSTANCE",		new String[] {"Id" ,"TargetObjectId" ,"Status" ,"IsDeleted" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("PROCESSINSTANCESTEP",		new String[] {"Id" ,"ProcessInstanceId" ,"StepStatus" ,"OriginalActorId" ,"ActorId" ,"Comments" ,"CreatedDate" ,"CreatedById" ,"SystemModstamp"}); 
		fieldsHash.put("PROCESSINSTANCEWORKITEM",		new String[] {"Id" ,"ProcessInstanceId" ,"OriginalActorId" ,"ActorId" ,"IsDeleted" ,"CreatedDate" ,"CreatedById" ,"SystemModstamp"}); 
		fieldsHash.put("PRODUCT2",		new String[] {"Id" ,"Name" ,"ProductCode" ,"Description" ,"IsActive" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"Family" ,"IsDeleted"}); 
		fieldsHash.put("PROFILE",		new String[] {"Id" ,"Name" ,"PermissionsEditTask" ,"PermissionsEditEvent" ,"PermissionsManageUsers" ,"PermissionsModifyAllData" ,"PermissionsManageCases" ,"PermissionsManageSolutions" ,"PermissionsCustomizeApplication" ,"PermissionsEditReadonlyFields" ,"PermissionsRunReports" ,"PermissionsViewSetup" ,"PermissionsTransferAnyEntity" ,"PermissionsManageSelfService" ,"PermissionsManageCssUsers" ,"PermissionsImportLeads" ,"PermissionsManageLeads" ,"PermissionsTransferAnyLead" ,"PermissionsViewAllData" ,"PermissionsEditPublicDocuments" ,"PermissionsManageDashboards" ,"PermissionsSendSitRequests" ,"PermissionsManageRemoteAccess" ,"PermissionsManageCategories" ,"PermissionsConvertLeads" ,"PermissionsPasswordNeverExpires" ,"PermissionsUseTeamReassignWizards" ,"PermissionsInstallMultiforce" ,"PermissionsPublishMultiforce" ,"PermissionsEditOppLineItemUnitPrice" ,"PermissionsCreateMultiforce" ,"PermissionsSolutionImport" ,"PermissionsManageCallCenters" ,"PermissionsEditReports" ,"PermissionsAuthorApex" ,"PermissionsManageMobile" ,"PermissionsApiEnabled" ,"PermissionsManageCustomReportTypes" ,"PermissionsEditCaseComments" ,"PermissionsTransferAnyCase" ,"PermissionsManageAnalyticSnapshots" ,"PermissionsScheduleReports" ,"PermissionsManageBusinessHourHolidays" ,"PermissionsCustomSidebarOnAllPages" ,"PermissionsDisableNotifications" ,"UserLicenseId" ,"UserType" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"Description"}); 
		fieldsHash.put("QUEUESOBJECT",		new String[] {"Id" ,"QueueId" ,"SobjectType" ,"CreatedById" ,"SystemModstamp"}); 
		fieldsHash.put("RECORDTYPE",		new String[] {"Id" ,"Name" ,"DeveloperName" ,"NamespacePrefix" ,"Description" ,"BusinessProcessId" ,"SobjectType" ,"IsActive" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("SCONTROL",		new String[] {"Id" ,"Name" ,"DeveloperName" ,"Description" ,"EncodingKey" ,"HtmlWrapper" ,"Filename" ,"BodyLength" ,"Binary" ,"ContentSource" ,"SupportsCaching" ,"NamespacePrefix" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("SELFSERVICEUSER",		new String[] {"Id" ,"LastName" ,"FirstName" ,"Name" ,"Username" ,"Email" ,"IsActive" ,"TimeZoneSidKey" ,"LocaleSidKey" ,"ContactId" ,"LanguageLocaleKey" ,"SuperUser" ,"LastLoginDate" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		fieldsHash.put("SOLUTION",		new String[] {"Id" ,"IsDeleted" ,"SolutionNumber" ,"SolutionName" ,"IsPublished" ,"IsPublishedInPublicKb" ,"Status" ,"IsReviewed" ,"SolutionNote" ,"OwnerId" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"TimesUsed" ,"IsHtml"}); 
		fieldsHash.put("SOLUTIONHISTORY",		new String[] {"Id" ,"IsDeleted" ,"SolutionId" ,"CreatedById" ,"CreatedDate" ,"Field" ,"OldValue" ,"NewValue"}); 
		fieldsHash.put("SOLUTIONSTATUS",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"IsDefault" ,"IsReviewed" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("TASK",		new String[] {"Id" ,"WhoId" ,"WhatId" ,"Subject" ,"ActivityDate" ,"Status" ,"Priority" ,"OwnerId" ,"Description" ,"IsDeleted" ,"AccountId" ,"IsClosed" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"IsArchived" ,"CallDurationInSeconds" ,"CallType" ,"CallDisposition" ,"CallObject" ,"ReminderDateTime" ,"IsReminderSet" ,"RecurrenceActivityId" ,"IsRecurrence" ,"RecurrenceStartDateOnly" ,"RecurrenceEndDateOnly" ,"RecurrenceTimeZoneSidKey" ,"RecurrenceType" ,"RecurrenceInterval" ,"RecurrenceDayOfWeekMask" ,"RecurrenceDayOfMonth" ,"RecurrenceInstance" ,"RecurrenceMonthOfYear"}); 
		fieldsHash.put("TASKPRIORITY",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"IsDefault" ,"IsHighPriority" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("TASKSTATUS",		new String[] {"Id" ,"MasterLabel" ,"SortOrder" ,"IsDefault" ,"IsClosed" ,"CreatedById" ,"CreatedDate" ,"LastModifiedById" ,"LastModifiedDate" ,"SystemModstamp"}); 
		fieldsHash.put("USER",		new String[] {"Id" ,"Username" ,"LastName" ,"FirstName" ,"Name" ,"CompanyName" ,"Division" ,"Department" ,"Title" ,"Street" ,"City" ,"State" ,"PostalCode" ,"Country" ,"Email" ,"Phone" ,"Fax" ,"MobilePhone" ,"Alias" ,"CommunityNickname" ,"IsActive" ,"TimeZoneSidKey" ,"UserRoleId" ,"LocaleSidKey" ,"ReceivesInfoEmails" ,"ReceivesAdminInfoEmails" ,"EmailEncodingKey" ,"ProfileId" ,"UserType" ,"LanguageLocaleKey" ,"EmployeeNumber" ,"DelegatedApproverId" ,"ManagerId" ,"LastLoginDate" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"OfflineTrialExpirationDate" ,"OfflinePdaTrialExpirationDate" ,"UserPermissionsMarketingUser" ,"UserPermissionsOfflineUser" ,"UserPermissionsCallCenterAutoLogin" ,"UserPermissionsMobileUser" ,"ForecastEnabled" ,"UserPreferencesActivityRemindersPopup" ,"UserPreferencesEventRemindersCheckboxDefault" ,"UserPreferencesTaskRemindersCheckboxDefault" ,"UserPreferencesReminderSoundOff" ,"UserPreferencesApexPagesDeveloperMode" ,"ContactId" ,"CallCenterId" ,"Extension"}); 
		fieldsHash.put("USERLICENSE",		new String[] {"Id" ,"LicenseDefinitionKey" ,"Name" ,"SystemModstamp"}); 
		fieldsHash.put("USERPREFERENCE",		new String[] {"Id" ,"UserId" ,"Preference" ,"Value" ,"SystemModstamp"}); 
		fieldsHash.put("USERROLE",		new String[] {"Id" ,"Name" ,"ParentRoleId" ,"RollupDescription" ,"OpportunityAccessForAccountOwner" ,"CaseAccessForAccountOwner" ,"ContactAccessForAccountOwner" ,"ForecastUserId" ,"MayForecastManagerShare" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp" ,"PortalAccountId" ,"PortalType" ,"PortalAccountOwnerId"}); 
		fieldsHash.put("WEBLINK",		new String[] {"Id" ,"PageOrSobjectType" ,"Name" ,"IsProtected" ,"Url" ,"EncodingKey" ,"LinkType" ,"OpenType" ,"Height" ,"Width" ,"ShowsLocation" ,"HasScrollbars" ,"HasToolbar" ,"HasMenubar" ,"ShowsStatus" ,"IsResizable" ,"Position" ,"ScontrolId" ,"MasterLabel" ,"Description" ,"DisplayType" ,"RequireRowSelection" ,"NamespacePrefix" ,"CreatedDate" ,"CreatedById" ,"LastModifiedDate" ,"LastModifiedById" ,"SystemModstamp"}); 
		
	}
	
	static public String[] getFields( String module )  {
		return fieldsHash.get( module.toUpperCase() );
	}

}
