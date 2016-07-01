package org.anieanie.cardgame.ui.cli;

import org.anieanie.cardgame.ui.Display;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class allows input loop to be created for different types of input with constraints on
 * the format or responses expected.
 */
public final class InputLoop {

    private static final Object lock = new Object();
    private final Display display;
    private BufferedReader input;

    public InputLoop(Display display) {
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.display = display;
    }

    public String runLoop(InputLoopConstraint constraint) {
        synchronized (lock) {
            String line;
            display.showNotification(constraint.helpMessage());
            while (true) {
                try {
                    display.showNotification(constraint.promptMessage());
                    System.out.print(">>");
                    line = input.readLine();
                    if (constraint.isSatisfied(line)) {
                        return line;
                    }
                    Thread.sleep(30);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface InputLoopConstraint {
        boolean isSatisfied(String input);
        String promptMessage();
        String helpMessage();
    }

    public class TrueConstraint implements InputLoopConstraint {
        private String help;
        private String prompt;
        public TrueConstraint(String help, String prompt) {
            this.help = help;
            this.prompt = prompt;
        }
        public boolean isSatisfied(String input) {
            return true;
        }

        @Override
        public String promptMessage() {
            return prompt;
        }

        @Override
        public String helpMessage() {
            return help;
        }


    }

}

