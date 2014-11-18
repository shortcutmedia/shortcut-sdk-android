/*
 * Copyright (c) 2014 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.livescanner.util;

import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class Utils {

    public static boolean nullOrEqual(Object s1, Object s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        return !(s1 == null || s2 == null) && s1.equals(s2);
    }

    public static Point getScreenResolution(WindowManager manager) {
        Display display = manager.getDefaultDisplay();
        return new Point(display.getWidth(), display.getHeight());
    }
}
