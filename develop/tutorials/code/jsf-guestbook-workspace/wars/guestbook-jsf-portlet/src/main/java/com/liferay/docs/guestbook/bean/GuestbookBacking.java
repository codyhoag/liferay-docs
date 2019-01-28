/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.docs.guestbook.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.liferay.docs.guestbook.service.EntryLocalService;
import com.liferay.docs.guestbook.services.EntryLocalServiceTracker;
import com.liferay.docs.guestbook.service.GuestbookLocalService;
import com.liferay.docs.guestbook.services.GuestbookLocalServiceTracker;
import com.liferay.docs.guestbook.wrappers.Entry;

import com.liferay.docs.guestbook.wrappers.Guestbook;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.context.FacesContextHelperUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil;


/**
 * @author  Vernon Singleton
 */
@ManagedBean
@ViewScoped
public class GuestbookBacking extends AbstractBacking {

	public static final String DEFAULT_GUESTBOOK_NAME = "Main";
	public static final String MODEL = "com.liferay.docs.guestbook";

	private GuestbookLocalServiceTracker guestbookLocalServiceTracker;
	private EntryLocalServiceTracker entryLocalServiceTracker;
	
	private Boolean permittedToAdd;

	// Private Data Members
	private Guestbook originalGuestbook;

	private Guestbook selectedGuestbook;
	private List<Guestbook> guestbooks;

	private Entry selectedEntry;
	private List<Entry> entries;

	private boolean editingGuestbook;
	private boolean editingEntry;

	private String portletResourcePrimaryKey;
		
	public void add() {
		setOriginalGuestbook(getSelectedGuestbook());

		try {

			if (!guestbookLocalServiceTracker.isEmpty()) {
				GuestbookLocalService guestbookLocalService = guestbookLocalServiceTracker.getService();
				Guestbook guestbook = new Guestbook(guestbookLocalService.createGuestbook(0L));
				FacesContext facesContext = FacesContext.getCurrentInstance();
				guestbook.setGroupId(LiferayPortletHelperUtil.getScopeGroupId(facesContext));
				setSelectedGuestbook(guestbook);
				editGuestbook();
			}
			else {
				FacesContextHelperUtil.addGlobalErrorMessage("is-temporarily-unavailable", "Guestbook service");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void cancel() {

		// go back to main view
		select(getOriginalGuestbook());
	}

	public void createMainGuestbook() {

		try {

			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			GuestbookLocalService guestbookLocalService = guestbookLocalServiceTracker.getService();

			com.liferay.docs.guestbook.model.Guestbook defaultGuestbook = (com.liferay.docs.guestbook.model.Guestbook)
				guestbookLocalService.getFirstGuestbookByName(scopeGroupId, DEFAULT_GUESTBOOK_NAME);

			// Create the default guestbook if it does not exist in the database
			if (defaultGuestbook == null) {
				logger.info("postConstruct: creating a default guestbook named " + DEFAULT_GUESTBOOK_NAME + " ...");

				Guestbook guestbook = new Guestbook(guestbookLocalService.createGuestbook(0L));
				guestbook.setName(DEFAULT_GUESTBOOK_NAME);
				guestbook.setGroupId(scopeGroupId);
				guestbook.setCompanyId(LiferayPortletHelperUtil.getCompanyId(facesContext));
				guestbook.setUserId(LiferayPortletHelperUtil.getUserId(facesContext));
				guestbookLocalService.addGuestbook(guestbook);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(Guestbook guestbook) {

		entries = getEntries();
		deleteGuestbookEntries();

		try {
			GuestbookLocalService guestbookLocalService = guestbookLocalServiceTracker.getService();
			guestbookLocalService.deleteGuestbook(guestbook);
			addGlobalSuccessInfoMessage();
		}
		catch (Exception e) {
			addGlobalUnexpectedErrorMessage();
			logger.error(e);
		}

		// Re-create the Main Guestbook if we just delete the Main Guestbook ...
		if (DEFAULT_GUESTBOOK_NAME.equals(guestbook.getName())) {
			createMainGuestbook();
		}

		// We just deleted the selected Guestbook so ...
		this.selectedGuestbook = null;

		// Force Guestbooks and entries to reload
		setGuestbooks(null);
		setEntries(null);

		// Go back to the master view
		select(null);
	}

	public void deleteGuestbookEntries() {

		for (Entry entry : entries) {

			try {
				EntryLocalService entryLocalService = entryLocalServiceTracker.getService();
				entryLocalService.deleteEntry(entry);
				addGlobalSuccessInfoMessage();
			}
			catch (Exception e) {
				addGlobalUnexpectedErrorMessage();
				logger.error(e);
			}
		}
	}

	public void edit(Guestbook guestbook) {
		setSelectedGuestbook(guestbook);
		editGuestbook();
	}

	public void editEntry() {
		editingEntry = true;
		editingGuestbook = false;
	}

	public void editGuestbook() {
		editingEntry = false;
		editingGuestbook = true;
	}

	public String getPortletResourcePrimaryKey() {

		if (portletResourcePrimaryKey == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long plid = LiferayPortletHelperUtil.getPlid(facesContext);
			Portlet portlet = LiferayPortletHelperUtil.getPortlet(facesContext);
			String portletId = portlet.getPortletId();
			portletResourcePrimaryKey = PortletPermissionUtil.getPrimaryKey(plid, portletId);
		}
 
		return portletResourcePrimaryKey;
	}

	@PostConstruct
	public void postConstruct() {
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		BundleContext bundleContext = bundle.getBundleContext();
		guestbookLocalServiceTracker = new GuestbookLocalServiceTracker(bundleContext);
		guestbookLocalServiceTracker.open();
		entryLocalServiceTracker = new EntryLocalServiceTracker(bundleContext);
		entryLocalServiceTracker.open();

		createMainGuestbook();
	}

	@PreDestroy
	public void preDestroy() {
		guestbookLocalServiceTracker.close();
		entryLocalServiceTracker.close();
	}

	public void save() {
		Guestbook guestbook = getSelectedGuestbook();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		guestbook.setCompanyId(LiferayPortletHelperUtil.getCompanyId(facesContext));
		guestbook.setUserId(LiferayPortletHelperUtil.getUserId(facesContext));

		try {
			GuestbookLocalService guestbookLocalService = guestbookLocalServiceTracker.getService();

			if (guestbook.getGuestbookId() == 0) {
				guestbook = new Guestbook(guestbookLocalService.addGuestbook(guestbook,
						LiferayPortletHelperUtil.getUserId(facesContext)));
			}
			else {
				guestbook = new Guestbook(guestbookLocalService.updateGuestbook(guestbook));
			}

			addGlobalSuccessInfoMessage();
		}
		catch (Exception e) {
			addGlobalUnexpectedErrorMessage();
			logger.error(e);
		}

		// go back to master view
		select(guestbook);
	}

	public void select(Guestbook guestbook) {

		if (guestbook == null) {
			setSelectedGuestbook(null);
		}
		else {
			setSelectedGuestbook(guestbook);
		}

		// force Guestbooks and Entries to reload
		setGuestbooks(null);
		setEntries(null);

		editingEntry = false;
		editingGuestbook = false;
	}

	public void setEditingEntry(boolean editingEntry) {
		this.editingEntry = editingEntry;
	}

	public void setEditingGuestbook(boolean editingGuestbook) {
		this.editingGuestbook = editingGuestbook;
	}

	public List<Entry> getEntries() {

		if (entries == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			Guestbook selectedGuestbook = getSelectedGuestbook();

			try {
				EntryLocalService entryLocalService = entryLocalServiceTracker.getService();
				entries = new ArrayList<Entry>();

				if (selectedGuestbook == null) {
					logger.info("getEntries: selectedGuestbook == null ... ");
				}
				else {
					List<com.liferay.docs.guestbook.model.Entry> list = entryLocalService.getEntries(scopeGroupId,
							selectedGuestbook.getGuestbookId());

					for (com.liferay.docs.guestbook.model.Entry entry : list) {
						entries.add(new Entry(entry));
					}
				}

			}
			catch (SystemException e) {
				logger.error(e);
			}
		}

		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public List<Guestbook> getGuestbooks() {

		if (guestbooks == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);

			try {
				GuestbookLocalService guestbookLocalService = guestbookLocalServiceTracker.getService();
				guestbooks = new ArrayList<Guestbook>();

				List<com.liferay.docs.guestbook.model.Guestbook> list = guestbookLocalService.getGuestbooks(
						scopeGroupId);

				for (com.liferay.docs.guestbook.model.Guestbook guestbook : list) {
					guestbooks.add(new Guestbook(guestbook));
				}
			}
			catch (SystemException e) {
				logger.error(e);
			}
		}

		logger.info("getGuestbooks: guestbooks.size() = " + guestbooks.size());

		return guestbooks;
	}

	public void setGuestbooks(List<Guestbook> guestbooks) {
		this.guestbooks = guestbooks;
	}

	public Boolean getPermittedToAdd() {

		if (permittedToAdd == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			permittedToAdd = LiferayPortletHelperUtil.getThemeDisplay().getPermissionChecker().hasPermission(scopeGroupId,
					MODEL, scopeGroupId, "ADD_GUESTBOOK");
		}

		return permittedToAdd;
	}

	public void setPermittedToAdd(Boolean permittedToAdd) {
		this.permittedToAdd = permittedToAdd;
	}

	public boolean isEditingGuestbook() {
		return editingGuestbook;
	}

	public Guestbook getOriginalGuestbook() {
		return originalGuestbook;
	}

	public void setOriginalGuestbook(Guestbook originalGuestbook) {
		this.originalGuestbook = originalGuestbook;
	}

	public Entry getSelectedEntry() {
		return selectedEntry;
	}

	public void setSelectedEntry(Entry selectedEntry) {
		this.selectedEntry = new Entry(selectedEntry);
	}

	public Guestbook getSelectedGuestbook() {

		if (selectedGuestbook == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);

			try {
				GuestbookLocalService guestbookLocalService = guestbookLocalServiceTracker.getService();

				com.liferay.docs.guestbook.model.Guestbook firstGuestbookByName =
					(com.liferay.docs.guestbook.model.Guestbook) guestbookLocalService.getFirstGuestbookByName(
						scopeGroupId, DEFAULT_GUESTBOOK_NAME);

				if (firstGuestbookByName == null) {
					logger.info("getSelectedGuestbook: No Guestbook named " + DEFAULT_GUESTBOOK_NAME);
				}
				else {
					selectedGuestbook = new Guestbook(firstGuestbookByName);
				}
			}
			catch (SystemException e) {
				logger.error(e);
			}
		}

		return selectedGuestbook;
	}

	public void setSelectedGuestbook(Guestbook selectedGuestbook) {
		this.selectedGuestbook = selectedGuestbook;
	}

	public boolean isEditingEntry() {
		return editingEntry;
	}

}
