package org.anieanie.whot;

import javax.swing.UIManager;
import java.awt.*;

/**
 * Title:        A Complete Whot Playing Environment
 * Description:  A complete Whot playing environment consisting players, spectators and the umpire (or game monitor).~nThe user can play Whot in this program
 * Copyright:    Copyright (c) 1998
 * Company:      KaySoft Intelligent Solutions
 * @author Aniebiet Udoh
 * @version 
 */

public class WhotGame {
  boolean packFrame = false;

  /**Construct the application*/
  public WhotGame() {
    GameFrame frame = new GameFrame();
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }
  /**Main method*/
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new WhotGame();
  }
}