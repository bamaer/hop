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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.hop.core.config.HopConfig;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPoint;
import org.apache.hop.core.extension.IExtensionPoint;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.notifications.INotificationProvider;
import org.apache.hop.core.util.JsonUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.ui.hopgui.notifications.config.NotificationSourceConfig;
import org.eclipse.swt.widgets.Display;

/**
 * Initializes the notification system when Hop GUI starts. This class registers providers and
 * starts the notification service.
 */
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
      // DISABLED: Uncomment to enable test notifications for debugging
      // TestNotificationProvider testProvider = new TestNotificationProvider();
      // testProvider.setEnabled(false); // Disable by default
      // service.registerProvider(testProvider);

      // Register RSS/Atom feed providers
      // DISABLED: Using GitHub API provider instead (better content and filtering)
      // org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider apacheHopFeed =
      //     new org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider(
      //         "https://github.com/apache/hop/releases.atom",
      //         "rss-apache-hop-releases",
      //         "Apache Hop Releases");
      // apacheHopFeed.setPollInterval(3600000); // 1 hour
      // service.registerProvider(apacheHopFeed);

      // Knowbi Putki releases feed - DISABLED: URL returns 404
      // org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider knowbiFeed =
      //     new org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider(
      //         "https://github.com/knowbi/knowbi-putki/releases.atom",
      //         "rss-knowbi-putki-releases",
      //         "Knowbi Putki Releases");
      // knowbiFeed.setPollInterval(3600000); // 1 hour
      // service.registerProvider(knowbiFeed);

      // Load notification sources from config
      boolean notificationsEnabled =
          HopConfig.readOptionString("notification.system.enabled", "true")
              .equalsIgnoreCase("true");

      if (!notificationsEnabled) {
        log.logBasic("Notification system is disabled in configuration");
        return; // Don't start the service if disabled
      }

      // Load sources from config
      List<NotificationSourceConfig> sources = loadNotificationSources();

      if (sources.isEmpty()) {
        // If no sources configured, create a default one for backward compatibility
        log.logBasic("No notification sources configured, creating default Apache Hop source");
        NotificationSourceConfig defaultSource = new NotificationSourceConfig();
        defaultSource.setId("github-apache-hop");
        defaultSource.setName("Apache Hop Releases");
        defaultSource.setType(NotificationSourceConfig.SourceType.GITHUB_RELEASES);
        defaultSource.setEnabled(true);
        defaultSource.setGithubOwner("apache");
        defaultSource.setGithubRepo("hop");
        defaultSource.setGithubIncludePrereleases(false);
        defaultSource.setPollIntervalMinutes("60");
        defaultSource.setColor("#FF5733"); // Default color
        sources.add(defaultSource);

        // Save the default source to config so it appears in the configuration UI
        try {
          ObjectMapper mapper = JsonUtil.jsonMapper();
          String sourcesJson = mapper.writeValueAsString(sources);
          HopConfig.getInstance().saveOption("notification.sources", sourcesJson);
          HopConfig.getInstance().saveToFile();
          log.logBasic("Saved default notification source to configuration");
        } catch (Exception e) {
          log.logError("Error saving default notification source to config", e);
        }
      }

      // Register providers for each enabled source
      for (NotificationSourceConfig source : sources) {
        if (!source.isEnabled()) {
          log.logBasic("Skipping disabled notification source: " + source.getName());
          continue;
        }

        try {
          registerNotificationProvider(service, source, log);
        } catch (Exception e) {
          log.logError(
              "Error registering notification source '" + source.getName() + "': " + e.getMessage(),
              e);
        }
      }

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

      log.logBasic("Notification system initialized with " + sources.size() + " source(s)");
    } catch (Exception e) {
      log.logError("Error initializing notification system", e);
    }
  }

  /**
   * Load notification sources from HopConfig
   *
   * @return List of notification source configurations
   */
  private List<NotificationSourceConfig> loadNotificationSources() {
    try {
      String sourcesJson = HopConfig.readOptionString("notification.sources", null);
      if (!Utils.isEmpty(sourcesJson)) {
        ObjectMapper mapper = JsonUtil.jsonMapper();
        return mapper.readValue(
            sourcesJson, new TypeReference<List<NotificationSourceConfig>>() {});
      }
    } catch (Exception e) {
      // If loading fails, return empty list
    }
    return new java.util.ArrayList<>();
  }

  /**
   * Register a notification provider based on source configuration
   *
   * @param service Notification service
   * @param source Source configuration
   * @param log Log channel
   */
  private void registerNotificationProvider(
      NotificationService service, NotificationSourceConfig source, ILogChannel log) {
    long pollIntervalMs = 3600000; // Default 1 hour
    try {
      String pollIntervalStr = source.getPollIntervalMinutes();
      if (pollIntervalStr != null && !pollIntervalStr.isEmpty()) {
        int minutes = Integer.parseInt(pollIntervalStr);
        if (minutes > 0) {
          pollIntervalMs = minutes * 60L * 1000L;
        }
      }
    } catch (NumberFormatException e) {
      log.logError(
          "Invalid poll interval for source '"
              + source.getName()
              + "': "
              + source.getPollIntervalMinutes()
              + ", using default");
    }

    switch (source.getType()) {
      case GITHUB_RELEASES:
        String owner = source.getGithubOwner();
        String repo = source.getGithubRepo();
        if (owner == null || owner.isEmpty() || repo == null || repo.isEmpty()) {
          log.logError(
              "GitHub source '"
                  + source.getName()
                  + "' is missing owner or repository configuration");
          return;
        }
        org.apache.hop.ui.hopgui.notifications.providers.GitHubReleasesNotificationProvider
            githubProvider =
                new org.apache.hop.ui.hopgui.notifications.providers
                    .GitHubReleasesNotificationProvider(
                    owner, repo, source.getId(), source.getName());
        githubProvider.setPollInterval(pollIntervalMs);
        githubProvider.setIncludePreReleases(source.isGithubIncludePrereleases());
        service.registerProvider(githubProvider);
        log.logBasic(
            "Registered GitHub provider: "
                + owner
                + "/"
                + repo
                + " (poll interval: "
                + (pollIntervalMs / 60000)
                + " minutes, pre-releases: "
                + source.isGithubIncludePrereleases()
                + ")");
        break;

      case RSS_FEED:
        String url = source.getRssUrl();
        if (url == null || url.isEmpty()) {
          log.logError("RSS source '" + source.getName() + "' is missing URL configuration");
          return;
        }
        org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider rssProvider =
            new org.apache.hop.ui.hopgui.notifications.providers.RssNotificationProvider(
                url, source.getId(), source.getName());
        rssProvider.setPollInterval(pollIntervalMs);
        service.registerProvider(rssProvider);
        log.logBasic(
            "Registered RSS provider: "
                + url
                + " (poll interval: "
                + (pollIntervalMs / 60000)
                + " minutes)");
        break;

      case CUSTOM_PLUGIN:
        String pluginId = source.getPluginId();
        if (pluginId == null || pluginId.isEmpty()) {
          log.logError("Custom plugin source '" + source.getName() + "' is missing plugin ID");
          return;
        }
        // Custom plugins can register notification providers in two ways:
        // 1. Via extension point (preferred): Plugins can implement an extension point that
        //    provides INotificationProvider instances. This would be discovered automatically.
        // 2. Direct registration: Plugins can call
        // NotificationService.getInstance().registerProvider()
        //    during their initialization (e.g., in a @GuiPlugin or @ExtensionPoint class).
        //
        // For now, we look for a provider that was already registered with the matching ID.
        // If found, update its poll interval. If not found, log a warning.
        INotificationProvider existingProvider = service.getProvider(pluginId);
        if (existingProvider != null) {
          existingProvider.setPollInterval(pollIntervalMs);
          log.logBasic(
              "Updated poll interval for custom plugin provider '"
                  + pluginId
                  + "' to "
                  + (pollIntervalMs / 60000)
                  + " minutes");
        } else {
          log.logBasic(
              "Custom plugin provider '"
                  + pluginId
                  + "' not found. "
                  + "Plugins should register their INotificationProvider implementation "
                  + "via NotificationService.getInstance().registerProvider() during initialization.");
        }
        break;
    }
  }
}
