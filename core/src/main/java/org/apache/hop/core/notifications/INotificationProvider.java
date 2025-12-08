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

package org.apache.hop.core.notifications;

import java.util.List;
import org.apache.hop.core.exception.HopException;

/** Interface for notification providers */
public interface INotificationProvider {
  /**
   * @return Unique identifier for this provider
   */
  String getId();

  /**
   * @return Human-readable name for this provider
   */
  String getName();

  /**
   * @return Description of what this provider does
   */
  String getDescription();

  /**
   * Fetch notifications from the source
   *
   * @return List of notifications (may be empty, never null)
   * @throws HopException if there's an error fetching notifications
   */
  List<Notification> fetchNotifications() throws HopException;

  /**
   * @return Whether this provider is enabled
   */
  boolean isEnabled();

  /**
   * @param enabled Enable or disable this provider
   */
  void setEnabled(boolean enabled);

  /**
   * @return Poll interval in milliseconds
   */
  long getPollInterval();

  /**
   * @param interval Poll interval in milliseconds
   */
  void setPollInterval(long interval);

  /**
   * Initialize the provider
   *
   * @throws HopException if initialization fails
   */
  void initialize() throws HopException;

  /** Shutdown the provider and clean up resources */
  void shutdown();
}
