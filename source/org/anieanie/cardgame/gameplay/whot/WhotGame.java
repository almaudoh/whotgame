package org.anieanie.cardgame.gameplay.whot;

import org.anieanie.cardgame.gameplay.GameServer;
import org.anieanie.cardgame.ui.gui.GameFrame;

import java.awt.*;

/**
 * Title:        A Complete Whot Playing Environment
 * Description:  A complete Whot playing gameplay consisting players, spectators and the umpire (or whot monitor).~nThe user can play Whot in this program
 * Copyright:    Copyright (c) 1998
 * Company:      KaySoft Intelligent Solutions
 *
 * @author Aniebiet Udoh
 */

public class WhotGame {
    boolean packFrame = false;

    /**
     * Main method
     */
    public static void main(String[] args) {
        WhotGameMonitor monitor = new WhotGameMonitor();
        GameServer server = new GameServer(monitor, 5550);

        server.start();
    }

    public WhotGame () {

    }

    /**
     * Construct the application
     */
    public void __WhotGame() {
        GameFrame frame = new GameFrame();
        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        } else {
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
}