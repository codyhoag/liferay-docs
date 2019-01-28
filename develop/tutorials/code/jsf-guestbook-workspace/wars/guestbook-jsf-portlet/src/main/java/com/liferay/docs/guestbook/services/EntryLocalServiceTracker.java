package com.liferay.docs.guestbook.services;

import com.liferay.docs.guestbook.service.EntryLocalService;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class EntryLocalServiceTracker extends ServiceTracker<EntryLocalService, EntryLocalService> {

	public EntryLocalServiceTracker(BundleContext bundleContext) {
		super(bundleContext, EntryLocalService.class, null);
	}

}
