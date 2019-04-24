package co.llective.notifier;

import co.llective.mq.MeowMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatImpersonator {
    final Logger logger = LoggerFactory.getLogger(CatImpersonator.class);
    private String message;

    public CatImpersonator(String message) {
        this.message = message;
    }

    private String meowCommas(String input) {
        return input.replaceAll(",", ", meow,");
    }

    private String meowExclamationMarks(String input) {
        return input.replaceAll("!", ", meow!");
    }

    private String meowEnding(String input) {
        if (input.endsWith(".") || input.endsWith("!") || input.endsWith("?")) {
            return input+" Meow.";
        } else {
            return meowEnding(input+'.');
        }
    }

    public String meowIt() {
        if (message.contains("mouse")) {
            try {
                logger.warn("Oops, a message with this type of message takes more time to process");
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return meowEnding(meowExclamationMarks(meowCommas(this.message)));
    }
}
