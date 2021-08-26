// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.utils;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import javax.management.MBeanServer;

public class HeapUtils {

  private static final String HOTSPOT_MBEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
  private static volatile HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean;

  private static void initHotSpotMBean() throws IOException {
    if (hotSpotDiagnosticMXBean == null) {
      synchronized (HeapUtils.class) {
        if (hotSpotDiagnosticMXBean == null) {
          hotSpotDiagnosticMXBean = getHotSpotDiagnosticMXBean();
        }
      }
    }
  }

  private static HotSpotDiagnosticMXBean getHotSpotDiagnosticMXBean() throws IOException {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    return ManagementFactory.newPlatformMXBeanProxy(
        server, HOTSPOT_MBEAN_NAME, HotSpotDiagnosticMXBean.class);
  }

  public static void dumpHeap(Path fileName, boolean live) throws IOException {
    initHotSpotMBean();
    hotSpotDiagnosticMXBean.dumpHeap(fileName.toString(), live);
  }
}
