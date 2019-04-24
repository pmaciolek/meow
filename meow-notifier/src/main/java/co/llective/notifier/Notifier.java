package co.llective.notifier;

import co.llective.mq.IMeowMessageConsumer;
import co.llective.mq.MeowMQ;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;


public class Notifier implements IMeowMessageConsumer {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    // If we want Specialagent to instrument it...
    final OkHttpClient client = new OkHttpClient.Builder().build();


    final Logger logger = LoggerFactory.getLogger(Notifier.class);
    private final String url;

    public Notifier(String url) {
        this.url = url;
    }

    public static class SlackNotification {
        private String text;

        public SlackNotification() {}

        public SlackNotification(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    private String prepareJSON(String message) throws JsonProcessingException {
        SlackNotification sn = new SlackNotification(message);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(sn);
    }

    public String handleMessage(String message) throws IOException  {
        RequestBody body = RequestBody.create(JSON, prepareJSON(message));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    public void consumeMessage(String message) {
        try {
            String meowedMessage = new CatImpersonator(message).meowIt();
            logger.info("Handled a message of length: "+message.length()+" with response code: "+handleMessage(meowedMessage));
        } catch(IOException ioe) {
            logger.error("Could not consume the message.", ioe);
        }
    }

    public static String ensureEnv(String name) {
        Map<String,String> systemEnv = System.getenv();
        if (!systemEnv.containsKey(name) || systemEnv.get(name).isEmpty()) {
            throw new RuntimeException(name+" must be set as environment variable");
        }
        return systemEnv.get(name);
    }

    public static void main(String[] args) {
        MeowMQ mq = new MeowMQ(ensureEnv("KAFKA_BROKERS"), ensureEnv("KAFKA_USERNAME"), ensureEnv("KAFKA_PASSWORD"));
        Notifier notifier = new Notifier(ensureEnv("WEBHOOK_URL"));
        mq.consume(notifier);
    }
}
