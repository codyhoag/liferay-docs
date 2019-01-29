package com.liferay.docs.guestbook.services;

import com.liferay.docs.guestbook.service.GuestbookLocalService;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class GuestbookLocalServiceTracker extends ServiceTracker<GuestbookLocalService, GuestbookLocalService> {

	public GuestbookLocalServiceTracker(BundleContext bundleContext) {
		super(bundleContext, GuestbookLocalService.class, null);
	}
	
}
