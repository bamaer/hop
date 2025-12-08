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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.hop.core.notifications.Notification;
import org.apache.hop.core.notifications.NotificationCategory;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.util.EnvironmentUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** Dropdown panel for displaying notifications */
public class NotificationPanel implements INotificationListener {
  private static NotificationPanel instance;

  private Shell shell;
  private Shell parentShell;
  private ScrolledComposite scrolledComposite;
  private Composite contentComposite;
  private boolean isVisible = false;

  private NotificationPanel() {
    this.parentShell = HopGui.getInstance().getShell();
    NotificationService.getInstance().addNotificationListener(this);
  }

  public static synchronized NotificationPanel getInstance() {
    if (instance == null) {
      instance = new NotificationPanel();
    }
    return instance;
  }

  /** Toggle the panel visibility */
  public void toggle() {
    if (isVisible) {
      hide();
    } else {
      show();
    }
  }

  /** Show the notification panel */
  public void show() {
    if (shell != null && !shell.isDisposed()) {
      shell.setVisible(true);
      shell.setFocus();
      return;
    }

    createPanel();
    updateNotifications();
    positionPanel();
    shell.setVisible(true);
    isVisible = true;
  }

  /** Hide the notification panel */
  public void hide() {
    if (shell != null && !shell.isDisposed()) {
      shell.setVisible(false);
    }
    isVisible = false;
  }

  /** Create the panel UI */
  private void createPanel() {
    shell = new Shell(parentShell, SWT.NO_TRIM | SWT.ON_TOP);
    shell.setLayout(new FormLayout());
    PropsUi.setLook(shell);

    // Header
    Composite header = new Composite(shell, SWT.NONE);
    header.setLayout(new FormLayout());
    PropsUi.setLook(header);
    FormData fdHeader = new FormData();
    fdHeader.left = new FormAttachment(0, 0);
    fdHeader.right = new FormAttachment(100, 0);
    fdHeader.top = new FormAttachment(0, 0);
    header.setLayoutData(fdHeader);

    Label title = new Label(header, SWT.NONE);
    title.setText("Notifications");
    PropsUi.setLook(title);
    FormData fdTitle = new FormData();
    fdTitle.left = new FormAttachment(0, 10);
    fdTitle.top = new FormAttachment(0, 10);
    fdTitle.bottom = new FormAttachment(100, -10);
    title.setLayoutData(fdTitle);

    Button markAllRead = new Button(header, SWT.PUSH);
    markAllRead.setText("Mark all read");
    PropsUi.setLook(markAllRead);
    FormData fdMarkAll = new FormData();
    fdMarkAll.right = new FormAttachment(100, -10);
    fdMarkAll.top = new FormAttachment(0, 5);
    fdMarkAll.bottom = new FormAttachment(100, -5);
    markAllRead.setLayoutData(fdMarkAll);
    markAllRead.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            NotificationService.getInstance().markAllAsRead();
          }
        });

    // Scrolled content area
    scrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
    PropsUi.setLook(scrolledComposite);
    FormData fdScrolled = new FormData();
    fdScrolled.left = new FormAttachment(0, 0);
    fdScrolled.right = new FormAttachment(100, 0);
    fdScrolled.top = new FormAttachment(header, 0);
    fdScrolled.bottom = new FormAttachment(100, -40);
    scrolledComposite.setLayoutData(fdScrolled);

    contentComposite = new Composite(scrolledComposite, SWT.NONE);
    contentComposite.setLayout(new FormLayout());
    PropsUi.setLook(contentComposite);
    scrolledComposite.setContent(contentComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);

    // Footer
    Composite footer = new Composite(shell, SWT.NONE);
    footer.setLayout(new FormLayout());
    PropsUi.setLook(footer);
    FormData fdFooter = new FormData();
    fdFooter.left = new FormAttachment(0, 0);
    fdFooter.right = new FormAttachment(100, 0);
    fdFooter.bottom = new FormAttachment(100, 0);
    footer.setLayoutData(fdFooter);

    Button closeButton = new Button(footer, SWT.PUSH);
    closeButton.setText("Close");
    PropsUi.setLook(closeButton);
    FormData fdClose = new FormData();
    fdClose.right = new FormAttachment(100, -10);
    fdClose.top = new FormAttachment(0, 5);
    fdClose.bottom = new FormAttachment(100, -5);
    closeButton.setLayoutData(fdClose);
    closeButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            hide();
          }
        });

    // Close when clicking outside
    shell.addListener(
        SWT.Deactivate,
        e -> {
          if (!shell.isDisposed()) {
            Display.getCurrent()
                .asyncExec(
                    () -> {
                      if (!shell.isDisposed() && !shell.isFocusControl()) {
                        hide();
                      }
                    });
          }
        });

    shell.setSize(400, 500);
  }

  /** Update the notifications display */
  private void updateNotifications() {
    if (contentComposite == null || contentComposite.isDisposed()) {
      return;
    }

    // Clear existing notifications
    for (Control control : contentComposite.getChildren()) {
      control.dispose();
    }

    List<Notification> notifications = NotificationService.getInstance().getNotifications(false);

    if (notifications.isEmpty()) {
      Label emptyLabel = new Label(contentComposite, SWT.CENTER | SWT.WRAP);
      emptyLabel.setText("No notifications");
      PropsUi.setLook(emptyLabel);
      FormData fdEmpty = new FormData();
      fdEmpty.left = new FormAttachment(0, 10);
      fdEmpty.right = new FormAttachment(100, -10);
      fdEmpty.top = new FormAttachment(0, 20);
      emptyLabel.setLayoutData(fdEmpty);
    } else {
      Control lastControl = null;
      for (Notification notification : notifications) {
        Composite notifComposite = createNotificationItem(notification, lastControl);
        lastControl = notifComposite;
      }
    }

    contentComposite.layout(true, true);
    scrolledComposite.setMinSize(contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  /** Create a notification item UI */
  private Composite createNotificationItem(Notification notification, Control above) {
    Composite composite = new Composite(contentComposite, SWT.BORDER);
    composite.setLayout(new FormLayout());
    PropsUi.setLook(composite);

    GuiResource guiResource = GuiResource.getInstance();

    // Set background color based on read state
    if (!notification.isRead()) {
      composite.setBackground(guiResource.getColorLightGray());
    }

    // Set border color based on priority
    if (notification.getPriority() != null) {
      switch (notification.getPriority()) {
        case ERROR:
          composite.setBackground(guiResource.getColor(255, 240, 240)); // Light red tint
          break;
        case WARNING:
          composite.setBackground(guiResource.getColor(255, 250, 240)); // Light yellow tint
          break;
        case INFO:
        default:
          if (!notification.isRead()) {
            composite.setBackground(guiResource.getColorLightGray());
          }
          break;
      }
    }

    FormData fdComposite = new FormData();
    fdComposite.left = new FormAttachment(0, 0);
    fdComposite.right = new FormAttachment(100, 0);
    if (above != null) {
      fdComposite.top = new FormAttachment(above, 5);
    } else {
      fdComposite.top = new FormAttachment(0, 5);
    }
    composite.setLayoutData(fdComposite);

    // Priority indicator (colored bar on the left)
    Composite priorityBar = new Composite(composite, SWT.NONE);
    priorityBar.setLayout(null);
    FormData fdPriorityBar = new FormData();
    fdPriorityBar.left = new FormAttachment(0, 0);
    fdPriorityBar.top = new FormAttachment(0, 0);
    fdPriorityBar.bottom = new FormAttachment(100, 0);
    fdPriorityBar.width = 4;
    priorityBar.setLayoutData(fdPriorityBar);

    if (notification.getPriority() != null) {
      switch (notification.getPriority()) {
        case ERROR:
          priorityBar.setBackground(guiResource.getColorRed());
          break;
        case WARNING:
          priorityBar.setBackground(guiResource.getColorOrange());
          break;
        case INFO:
        default:
          priorityBar.setBackground(guiResource.getColorBlue());
          break;
      }
    } else {
      priorityBar.setBackground(guiResource.getColorGray());
    }

    // Category badge
    Label categoryLabel = null;
    if (notification.getCategory() != null
        && notification.getCategory() != NotificationCategory.OTHER) {
      categoryLabel = new Label(composite, SWT.NONE);
      String categoryText =
          notification.getCategory().name().substring(0, 1).toUpperCase()
              + notification.getCategory().name().substring(1).toLowerCase();
      categoryLabel.setText(categoryText);
      PropsUi.setLook(categoryLabel);
      categoryLabel.setBackground(guiResource.getColorGray());
      categoryLabel.setForeground(guiResource.getColorWhite());
      FormData fdCategory = new FormData();
      fdCategory.right = new FormAttachment(100, -10);
      fdCategory.top = new FormAttachment(0, 8);
      categoryLabel.setLayoutData(fdCategory);
      categoryLabel.pack();
    }

    // Title
    Label titleLabel = new Label(composite, SWT.WRAP);
    titleLabel.setText(notification.getTitle() != null ? notification.getTitle() : "");
    PropsUi.setLook(titleLabel);
    if (!notification.isRead()) {
      titleLabel.setFont(guiResource.getFontBold());
    }
    FormData fdTitle = new FormData();
    fdTitle.left = new FormAttachment(priorityBar, 10);
    if (categoryLabel != null) {
      fdTitle.right = new FormAttachment(categoryLabel, -5);
    } else {
      fdTitle.right = new FormAttachment(100, -10);
    }
    fdTitle.top = new FormAttachment(0, 10);
    titleLabel.setLayoutData(fdTitle);

    // Message
    Label messageLabel = new Label(composite, SWT.WRAP);
    String message = notification.getMessage();
    if (message != null && message.length() > 150) {
      message = message.substring(0, 147) + "...";
    }
    messageLabel.setText(message != null ? message : "");
    PropsUi.setLook(messageLabel);
    FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment(priorityBar, 10);
    fdMessage.right = new FormAttachment(100, -10);
    fdMessage.top = new FormAttachment(titleLabel, 5);
    messageLabel.setLayoutData(fdMessage);

    // Footer with timestamp and source
    Composite footer = new Composite(composite, SWT.NONE);
    footer.setLayout(new FormLayout());
    PropsUi.setLook(footer);
    FormData fdFooter = new FormData();
    fdFooter.left = new FormAttachment(priorityBar, 10);
    fdFooter.right = new FormAttachment(100, -10);
    fdFooter.top = new FormAttachment(messageLabel, 8);
    fdFooter.bottom = new FormAttachment(100, -10);
    footer.setLayoutData(fdFooter);

    // Timestamp
    Label timeLabel = new Label(footer, SWT.NONE);
    String timeText = formatTimestamp(notification.getTimestamp());
    timeLabel.setText(timeText);
    PropsUi.setLook(timeLabel);
    timeLabel.setForeground(guiResource.getColorDarkGray());
    FormData fdTime = new FormData();
    fdTime.left = new FormAttachment(0, 0);
    fdTime.top = new FormAttachment(0, 0);
    fdTime.bottom = new FormAttachment(100, 0);
    timeLabel.setLayoutData(fdTime);

    // Source (if available)
    if (notification.getSource() != null && !notification.getSource().isEmpty()) {
      Label sourceLabel = new Label(footer, SWT.NONE);
      sourceLabel.setText(" • " + notification.getSource());
      PropsUi.setLook(sourceLabel);
      sourceLabel.setForeground(guiResource.getColorDarkGray());
      FormData fdSource = new FormData();
      fdSource.left = new FormAttachment(timeLabel, 0);
      fdSource.top = new FormAttachment(0, 0);
      fdSource.bottom = new FormAttachment(100, 0);
      sourceLabel.setLayoutData(fdSource);
    }

    // Click handler
    composite.addListener(
        SWT.MouseDown,
        e -> {
          NotificationService.getInstance().markAsRead(notification.getId());
          if (notification.getLink() != null && !notification.getLink().isEmpty()) {
            try {
              EnvironmentUtils.getInstance().openUrl(notification.getLink());
            } catch (Exception ex) {
              // Silently ignore URL opening errors
            }
          }
        });

    return composite;
  }

  /** Format timestamp for display */
  private String formatTimestamp(Date timestamp) {
    if (timestamp == null) {
      return "";
    }
    long diff = System.currentTimeMillis() - timestamp.getTime();
    long minutes = diff / 60000;
    long hours = diff / 3600000;
    long days = diff / 86400000;

    if (minutes < 1) {
      return "Just now";
    } else if (minutes < 60) {
      return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
    } else if (hours < 24) {
      return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
    } else if (days < 7) {
      return days + " day" + (days > 1 ? "s" : "") + " ago";
    } else {
      return new SimpleDateFormat("MMM d, yyyy").format(timestamp);
    }
  }

  /** Position the panel below the bell icon */
  private void positionPanel() {
    // TODO: Position relative to bell icon
    // For now, position in top-right corner
    Point shellSize = parentShell.getSize();
    Point panelSize = shell.getSize();
    shell.setLocation(shellSize.x - panelSize.x - 20, 60);
  }

  @Override
  public void notificationsChanged() {
    if (shell != null && !shell.isDisposed()) {
      Display.getCurrent()
          .asyncExec(
              () -> {
                if (shell != null && !shell.isDisposed() && isVisible) {
                  updateNotifications();
                }
              });
    }
  }

  /** Dispose the panel */
  public void dispose() {
    if (shell != null && !shell.isDisposed()) {
      shell.dispose();
    }
    NotificationService.getInstance().removeNotificationListener(this);
    instance = null;
  }
}
