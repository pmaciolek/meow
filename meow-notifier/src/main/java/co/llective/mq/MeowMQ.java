package co.llective.mq;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.opentracing.util.GlobalTracer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Properties;


public class MeowMQ {
    private final String topic;
    private final Properties props;
    private final Producer<String, String> producer;
    final Logger logger = LoggerFactory.getLogger(MeowMQ.class);

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

    public void consume(IMeowMessageConsumer msgConsumer) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        logger.info("Starting polling Kafka's topic "+topic);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
            for (ConsumerRecord<String, String> record : records) {
                SpanContext spanContext = TracingKafkaUtils.extractSpanContext(record.headers(), GlobalTracer.get());
                Span span = GlobalTracer.get().buildSpan("ConsumingMessage").asChildOf(spanContext).start();
                try(Scope scope = GlobalTracer.get().scopeManager().activate(span, true)) {
                    msgConsumer.consumeMessage(record.value());
                }
            }
        }
    }

    public void produce(String msg) {
        producer.send(new ProducerRecord<String,String>(topic, "meow-msg", msg));
    }
}


