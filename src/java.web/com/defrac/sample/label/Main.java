package com.defrac.sample.label;

import defrac.ui.FrameBuilder;

/**
 *
 */
public final class Main {
  public static void main(String[] args) {
    FrameBuilder.
        forScreen(new LabelSample()).
        title("defrac.display.Label").
        show();
  }
}
