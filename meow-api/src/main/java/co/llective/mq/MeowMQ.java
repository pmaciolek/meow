package co.llective.mq;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class MeowMQ {
    private final String topic;
    private final Properties props;
    private final Producer<String, String> producer;
    final Logger logger = LoggerFactory.getLogger(MeowMQ.class);

    private final static String ensureEnv(String name) {
        Map<String,String> systemEnv = System.getenv();
        Properties systemProp = System.getProperties();
        if (!systemEnv.containsKey(name) || systemEnv.get(name).isEmpty()) {
            if (systemProp.containsKey(name) && !systemProp.getProperty(name).isEmpty()) {
                return systemProp.getProperty(name);
            }
            throw new RuntimeException(name+" must be set as environment variable");
        }
        return systemEnv.get(name);
    }

    public MeowMQ() {
        this(ensureEnv("KAFKA_BROKERS"), ensureEnv("KAFKA_USERNAME"), ensureEnv("KAFKA_PASSWORD"));
    }

    public MeowMQ(String brokers, String username, String password) {
        this.topic = "meow-msg";

        String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);

        String serializer = StringSerializer.class.getName();
        String deserializer = StringDeserializer.class.getName();

        props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", username + "-consumer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", deserializer);
        props.put("value.deserializer", deserializer);
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config", jaasCfg);

        producer = new KafkaProducer<>(props);
    }

    public void produce(String msg) {
        producer.send(new ProducerRecord<String,String>(topic, "meow-msg", msg));
    }
}
