package com.github.cobrijani.kafkaconsumerdemo;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@Testcontainers
@Import(SimpleKafkaConsumerIntTest.KafkaTestContainersConfiguration.class)
@SpringBootTest(classes = KafkaConsumerDemoApplication.class)
@DirtiesContext
class SimpleKafkaConsumerIntTest {

    @Container
    public static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                    .withEmbeddedZookeeper();

    @Autowired
    private SimpleKafkaConsumer consumer;

    @Autowired
    private KafkaTemplate<String, String> template;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void testPublishMessage() throws Exception {
        var msg = "Hello " + UUID.randomUUID();
        template.send(SimpleKafkaConsumer.TOPIC_NAME, msg);
        template.flush();

        Unreliables.retryUntilTrue(
                20,
                TimeUnit.SECONDS,
                () -> {
                    //                    final ConsumerRecords<String, String> records =
                    //                            template.receive(List.of(new TopicPartitionOffset(SimpleKafkaConsumer.TOPIC_NAME, 0)));
                    if (consumer.getMessages().isEmpty()) {
                        return false;
                    }

                    assertThat(consumer.getMessages()).contains(msg);
                    return true;
                }
        );
    }

    protected void testKafkaFunctionality(String bootstrapServers, int partitions, int rf) throws Exception {
        try (
                AdminClient adminClient = AdminClient.create(
                        ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                );
                KafkaProducer<String, String> producer = new KafkaProducer<>(
                        ImmutableMap.of(
                                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                bootstrapServers,
                                ProducerConfig.CLIENT_ID_CONFIG,
                                UUID.randomUUID().toString()
                        ),
                        new StringSerializer(),
                        new StringSerializer()
                );
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                        ImmutableMap.of(
                                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                bootstrapServers,
                                ConsumerConfig.GROUP_ID_CONFIG,
                                "tc-" + UUID.randomUUID(),
                                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                                "earliest"
                        ),
                        new StringDeserializer(),
                        new StringDeserializer()
                );
        ) {
            String topicName = "messages-" + UUID.randomUUID();

            Collection<NewTopic> topics = Collections.singletonList(new NewTopic(topicName, partitions, (short) rf));
            adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);

            consumer.subscribe(Collections.singletonList(topicName));

            producer.send(new ProducerRecord<>(topicName, "testcontainers", "rulezzz")).get();

            Unreliables.retryUntilTrue(
                    10,
                    TimeUnit.SECONDS,
                    () -> {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                        if (records.isEmpty()) {
                            return false;
                        }

                        assertThat(records)
                                .hasSize(1)
                                .extracting(ConsumerRecord::topic, ConsumerRecord::key, ConsumerRecord::value)
                                .containsExactly(tuple(topicName, "testcontainers", "rulezzz"));
                        return true;
                    }
            );

            consumer.unsubscribe();
        }
    }

    @TestConfiguration
    public static class KafkaTestContainersConfiguration {

        @Bean
        public ConsumerFactory<Integer, String> consumerFactory(KafkaProperties properties) {
            Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        public ProducerFactory<String, String> producerFactory(KafkaProperties properties) {
            Map<String, Object> configProps = new HashMap<>(properties.buildProducerProperties());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        @Primary
        public NewTopic newTopic() {
            return TopicBuilder.name(SimpleKafkaConsumer.TOPIC_NAME)
                    .build();
        }
    }
}

