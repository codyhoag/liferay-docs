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
package com.liferay.docs.guestbook.wrappers;

import javax.faces.context.FacesContext;

import com.liferay.docs.guestbook.model.GuestbookWrapper;
import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;


/**
 * @author  Vernon Singleton
 */
public class Guestbook extends GuestbookWrapper {

	// serialVersionUID
	private static final long serialVersionUID = -420986486105631030L;

	private static final String MODEL = "com.liferay.docs.guestbook.model.Guestbook";

	// private members
	private Boolean deleteable;
	private Boolean permissible;
	private Boolean updateable;
	private Boolean viewable;

	public Guestbook(com.liferay.docs.guestbook.model.Guestbook guestbook) {
		super(guestbook);
	}

	public Boolean getDeleteable() {

		if (deleteable == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			deleteable = LiferayPortletHelperUtil.getThemeDisplay(facesContext).getPermissionChecker().hasPermission(scopeGroupId,
					MODEL, getGuestbookId(), ActionKeys.DELETE);
		}

		return deleteable;
	}

	public Boolean getPermissible() {

		if (permissible == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			permissible = LiferayPortletHelperUtil.getThemeDisplay(facesContext).getPermissionChecker().hasPermission(scopeGroupId,
					MODEL, getGuestbookId(), ActionKeys.PERMISSIONS);
		}

		return permissible;
	}

	public Boolean getUpdateable() {

		if (updateable == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			updateable = LiferayPortletHelperUtil.getThemeDisplay(facesContext).getPermissionChecker().hasPermission(scopeGroupId,
					MODEL, getGuestbookId(), ActionKeys.UPDATE);
		}

		return updateable;
	}

	public Boolean getViewable() {

		if (viewable == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId(facesContext);
			viewable = LiferayPortletHelperUtil.getThemeDisplay(facesContext).getPermissionChecker().hasPermission(scopeGroupId,
					MODEL, getGuestbookId(), ActionKeys.VIEW);
		}

		return viewable;
	}

}
