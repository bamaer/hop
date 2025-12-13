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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.notifications.INotificationProvider;
import org.apache.hop.core.notifications.Notification;

/** Service for managing notifications */
public class NotificationService {
  private static NotificationService instance;

  private final ILogChannel log;
  private final Map<String, INotificationProvider> providers;
  private final List<Notification> notifications;
  private final List<INotificationListener> listeners;
  private ScheduledExecutorService scheduler;
  private final Map<String, ScheduledFuture<?>> scheduledTasks;

  private NotificationService() {
    this.log = new LogChannel("NotificationService");
    this.providers = new ConcurrentHashMap<>();
    this.notifications = new CopyOnWriteArrayList<>();
    this.listeners = new CopyOnWriteArrayList<>();
    this.scheduledTasks = new ConcurrentHashMap<>();
  }

  public static synchronized NotificationService getInstance() {
    if (instance == null) {
      instance = new NotificationService();
    }
    return instance;
  }

  /**
   * Register a notification provider
   *
   * @param provider The provider to register
   */
  public void registerProvider(INotificationProvider provider) {
    if (provider == null || provider.getId() == null) {
      log.logError("Cannot register null provider or provider without ID");
      return;
    }

    providers.put(provider.getId(), provider);
    log.logBasic("Registered notification provider: " + provider.getName());

    // Schedule polling if scheduler is running
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduleProviderPolling(provider);
    }
  }

  /**
   * Unregister a notification provider
   *
   * @param providerId The ID of the provider to unregister
   */
  public void unregisterProvider(String providerId) {
    INotificationProvider provider = providers.remove(providerId);
    if (provider != null) {
      // Cancel scheduled polling
      ScheduledFuture<?> task = scheduledTasks.remove(providerId);
      if (task != null) {
        task.cancel(false);
      }
      provider.shutdown();
      log.logBasic("Unregistered notification provider: " + provider.getName());
    }
  }

  /**
   * Get all notifications
   *
   * @param unreadOnly If true, only return unread notifications
   * @return List of notifications sorted by date (descending - newest first)
   */
  public List<Notification> getNotifications(boolean unreadOnly) {
    List<Notification> result;
    if (unreadOnly) {
      result = notifications.stream().filter(n -> !n.isRead()).collect(Collectors.toList());
    } else {
      result = new ArrayList<>(notifications);
    }

    // Sort by timestamp descending (newest first)
    result.sort(
        (n1, n2) -> {
          Date d1 = n1.getTimestamp();
          Date d2 = n2.getTimestamp();
          if (d1 == null && d2 == null) {
            return 0;
          }
          if (d1 == null) {
            return 1; // null dates go to end
          }
          if (d2 == null) {
            return -1;
          }
          return d2.compareTo(d1); // Descending order
        });

    return result;
  }

  /**
   * Get count of unread notifications
   *
   * @return Number of unread notifications
   */
  public int getUnreadCount() {
    return (int) notifications.stream().filter(n -> !n.isRead()).count();
  }

  /**
   * Mark a notification as read
   *
   * @param notificationId The ID of the notification to mark as read
   */
  public void markAsRead(String notificationId) {
    notifications.stream()
        .filter(n -> notificationId.equals(n.getId()))
        .forEach(n -> n.setRead(true));
    notifyListeners();
  }

  /** Mark all notifications as read */
  public void markAllAsRead() {
    notifications.forEach(n -> n.setRead(true));
    notifyListeners();
  }

  /**
   * Add a notification
   *
   * @param notification The notification to add
   */
  public void addNotification(Notification notification) {
    if (notification == null || notification.getId() == null) {
      return;
    }

    // Check for duplicates
    boolean exists = notifications.stream().anyMatch(n -> notification.getId().equals(n.getId()));

    if (!exists) {
      notifications.add(notification);
      notifyListeners();
      log.logDetailed("Added notification: " + notification.getTitle());
    }
  }

  /**
   * Remove a notification
   *
   * @param notificationId The ID of the notification to remove
   */
  public void removeNotification(String notificationId) {
    notifications.removeIf(n -> notificationId.equals(n.getId()));
    notifyListeners();
  }

  /**
   * Add a listener for notification changes
   *
   * @param listener The listener to add
   */
  public void addNotificationListener(INotificationListener listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  /**
   * Remove a listener
   *
   * @param listener The listener to remove
   */
  public void removeNotificationListener(INotificationListener listener) {
    listeners.remove(listener);
  }

  /** Notify all listeners of changes */
  private void notifyListeners() {
    for (INotificationListener listener : listeners) {
      try {
        listener.notificationsChanged();
      } catch (Exception e) {
        log.logError("Error notifying listener", e);
      }
    }
  }

  /**
   * Fetch notifications from all enabled providers
   *
   * @throws HopException if there's an error
   */
  public void fetchFromProviders() throws HopException {
    for (INotificationProvider provider : providers.values()) {
      if (!provider.isEnabled()) {
        continue;
      }

      try {
        List<Notification> fetched = provider.fetchNotifications();
        for (Notification notification : fetched) {
          addNotification(notification);
        }
      } catch (Exception e) {
        log.logError("Error fetching notifications from provider: " + provider.getName(), e);
      }
    }
  }

  /** Start the notification service */
  public void start() {
    log.logBasic("Starting notification service");

    // Initialize scheduler
    if (scheduler == null || scheduler.isShutdown()) {
      scheduler = Executors.newScheduledThreadPool(2);
    }

    // Initialize and schedule polling for all providers
    for (INotificationProvider provider : providers.values()) {
      try {
        provider.initialize();
        scheduleProviderPolling(provider);
      } catch (Exception e) {
        log.logError("Error initializing provider: " + provider.getName(), e);
      }
    }
  }

  /** Schedule periodic polling for a provider */
  private void scheduleProviderPolling(INotificationProvider provider) {
    if (!provider.isEnabled()) {
      return;
    }

    long pollInterval = provider.getPollInterval();
    if (pollInterval <= 0) {
      pollInterval = 3600000; // Default to 1 hour
    }

    // Cancel existing task if any
    ScheduledFuture<?> existingTask = scheduledTasks.get(provider.getId());
    if (existingTask != null) {
      existingTask.cancel(false);
    }

    // Schedule new polling task
    ScheduledFuture<?> task =
        scheduler.scheduleWithFixedDelay(
            () -> {
              try {
                if (provider.isEnabled()) {
                  List<Notification> fetched = provider.fetchNotifications();
                  for (Notification notification : fetched) {
                    addNotification(notification);
                  }
                }
              } catch (Exception e) {
                log.logError("Error polling provider: " + provider.getName(), e);
              }
            },
            pollInterval, // Initial delay
            pollInterval, // Period
            TimeUnit.MILLISECONDS);

    scheduledTasks.put(provider.getId(), task);
    log.logDetailed(
        "Scheduled polling for provider "
            + provider.getName()
            + " every "
            + (pollInterval / 60000)
            + " minutes");
  }

  /** Stop the notification service */
  public void stop() {
    log.logBasic("Stopping notification service");

    // Cancel all scheduled tasks
    for (ScheduledFuture<?> task : scheduledTasks.values()) {
      if (task != null) {
        task.cancel(false);
      }
    }
    scheduledTasks.clear();

    // Shutdown scheduler
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }

    // Shutdown providers
    for (INotificationProvider provider : providers.values()) {
      try {
        provider.shutdown();
      } catch (Exception e) {
        log.logError("Error shutting down provider: " + provider.getName(), e);
      }
    }
    listeners.clear();
  }
}
