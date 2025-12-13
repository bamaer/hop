/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.ui.hopgui.notifications;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPoint;
import org.apache.hop.core.extension.IExtensionPoint;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.hopgui.notifications.providers.TestNotificationProvider;
import org.eclipse.swt.widgets.Display;

/**
 * Initializes the notification system when Hop GUI starts. This class registers providers and
 * starts the notification service.
 */
@GuiPlugin
@ExtensionPoint(
    id = "NotificationSystemInitializer",
    extensionPointId = "HopGuiStart",
    description = "Initialize the notification system")
public class NotificationSystemInitializer implements IExtensionPoint<Object> {

  @Override
  public void callExtensionPoint(ILogChannel log, IVariables variables, Object object)
      throws HopException {
    try {
      NotificationService service = NotificationService.getInstance();

      // Register test provider for manual testing
      // TODO: Remove or disable in production
      TestNotificationProvider testProvider = new TestNotificationProvider();
      service.registerProvider(testProvider);

      // Register RSS/Atom feed providers
      // Apache Hop releases feed
      org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider apacheHopFeed =
          new org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider(
              "https://github.com/apache/hop/releases.atom",
              "rss-apache-hop-releases",
              "Apache Hop Releases");
      apacheHopFeed.setPollInterval(3600000); // 1 hour
      service.registerProvider(apacheHopFeed);

      // Knowbi Putki releases feed (or another repository)
      org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider knowbiFeed =
          new org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider(
              "https://github.com/knowbi/knowbi-putki/releases.atom",
              "rss-knowbi-putki-releases",
              "Knowbi Putki Releases");
      knowbiFeed.setPollInterval(3600000); // 1 hour
      service.registerProvider(knowbiFeed);

      // Register GitHub Releases API providers (for better filtering)
      // Apache Hop via GitHub API
      org.apache.hop.ui.hopgui.notifications.providers.GitHubReleasesNotificationProvider
          apacheHopApi =
              new org.apache.hop.ui.hopgui.notifications.providers
                  .GitHubReleasesNotificationProvider(
                  "apache", "hop", "github-apache-hop", "Apache Hop (GitHub API)");
      apacheHopApi.setPollInterval(3600000); // 1 hour
      apacheHopApi.setIncludePreReleases(false); // Only stable releases
      service.registerProvider(apacheHopApi);

      // Start the service (this will initialize providers and start polling)
      service.start();

      // Fetch initial notifications immediately
      service.fetchFromProviders();

      // Initialize badge manager with a delay to ensure toolbar is ready
      Display.getCurrent()
          .asyncExec(
              () -> {
                Display.getCurrent()
                    .timerExec(
                        500,
                        () -> {
                          NotificationBadgeManager badgeManager =
                              NotificationBadgeManager.getInstance();
                          badgeManager.initialize();
                        });
              });

      log.logBasic("Notification system initialized");
    } catch (Exception e) {
      log.logError("Error initializing notification system", e);
    }
  }
}
