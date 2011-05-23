/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.webservices.rest.web.resource;

import java.util.Arrays;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

/**
 * {@link Resource} for Patients, supporting standard CRUD operations
 */
@Resource("patient")
@Handler(supports = Patient.class, order = 1)
public class PatientResource extends DataDelegatingCrudResource<Patient> {
	
	public PatientResource() {
		remappedProperties.put("preferredIdentifier", "patientIdentifier");
	}
	
	@PropertySetter("preferredIdentifier")
	public static void setPreferredIdentifier(Patient instance, PatientIdentifier id) {
		if (id.getId() == null) {
			// adding a new identifier
			id.setPreferred(true);
			instance.addIdentifier(id);
		} else {
			// switching which identifier is preferred
			for (PatientIdentifier existing : instance.getActiveIdentifiers()) {
				if (existing.isPreferred() && !existing.equals(id))
					existing.setPreferred(false);
			}
			id.setPreferred(true);
			instance.addIdentifier(id);
		}
	}
	
	/**
	 * FIND OUT HOW TO SET THIS VIA THE PERSON PERSON INSTEAD
	 * 
	 * @param instance
	 * @param name
	 */
	@PropertySetter("preferredName")
	public static void setPreferredName(Patient instance, PersonName name) {
		if (name.getId() == null) {
			// adding a new identifier
			name.setPreferred(true);
			instance.addName(name);
		} else {
			// switching which name is preferred
			for (PersonName existing : instance.getNames()) {
				if (existing.isVoided())
					continue;
				if (existing.isPreferred() && !existing.equals(name))
					existing.setPreferred(false);
			}
			name.setPreferred(true);
			instance.addName(name);
		}
	}
	
	@PropertySetter("preferredAddress")
	public static void setPreferredAddress(Patient instance, PersonAddress address) {
		//un mark the current preferred address as preferred if any
		for (PersonAddress existing : instance.getAddresses()) {
			if (existing.isVoided())
				continue;
			if (existing.isPreferred() && !OpenmrsUtil.nullSafeEquals(existing, address))
				existing.setPreferred(false);
		}
		address.setPreferred(true);
		if (address.getPersonAddressId() == null)
			instance.addAddress(address);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("uri", findMethod("getUri"));
			description.addProperty("display", findMethod("getDisplayString"));
			return description;
		} else if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("gender");
			description.addProperty("age");
			description.addProperty("birthdate");
			description.addProperty("birthdateEstimated");
			description.addProperty("dead");
			description.addProperty("deathDate");
			description.addProperty("causeOfDeath", Representation.REF);
			description.addProperty("preferredName", "personName", Representation.REF);
			description.addProperty("preferredAddress", "personAddress", Representation.REF);
			description.addProperty("activeIdentifiers", Representation.REF);
			description.addProperty("activeAttributes", Representation.REF);
			description.addProperty("uri", findMethod("getUri"));
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("gender");
			description.addProperty("age");
			description.addProperty("birthdate");
			description.addProperty("birthdateEstimated");
			description.addProperty("dead");
			description.addProperty("deathDate");
			description.addProperty("causeOfDeath");
			description.addProperty("preferredName", "personName", Representation.DEFAULT);
			description.addProperty("preferredAddress", "personAddress", Representation.DEFAULT);
			description.addProperty("names");
			description.addProperty("addresses");
			description.addProperty("identifiers");
			description.addProperty("attributes");
			description.addProperty("auditInfo", findMethod("getAuditInfo"));
			description.addProperty("uri", findMethod("getUri"));
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public Patient newDelegate() {
		return new Patient();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public Patient save(Patient patient) {
		return Context.getPatientService().savePatient(patient);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Patient getByUniqueId(String uuid) {
		return Context.getPatientService().getPatientByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(Patient patient, String reason, RequestContext context) throws ResponseException {
		if (patient.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getPatientService().voidPatient(patient, reason);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Patient patient, RequestContext context) throws ResponseException {
		if (patient == null) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getPatientService().purgePatient(patient);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(java.lang.String,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected List<Patient> doSearch(String query, RequestContext context) {
		return Context.getPatientService().getPatients(query, context.getStartIndex(), context.getLimit());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#propertiesToExposeAsSubResources()
	 */
	@Override
	protected List<String> propertiesToExposeAsSubResources() {
		return Arrays.asList("identifiers");
	}
	
	/**
	 * @param patient
	 * @return identifier + name (for concise display purposes)
	 */
	public String getDisplayString(Patient patient) {
		return patient.getPatientIdentifier().getIdentifier() + " - " + patient.getPersonName().getFullName();
	}
	
}
