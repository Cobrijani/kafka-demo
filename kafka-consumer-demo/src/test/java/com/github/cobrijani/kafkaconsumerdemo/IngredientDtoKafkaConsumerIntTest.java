package com.github.cobrijani.kafkaconsumerdemo;

import com.github.cobrijani.core.EventAction;
import com.github.cobrijani.core.IngredientMessage;
import com.github.cobrijani.core.Version;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.cobrijani.kafkaconsumerdemo.IngredientDtoKafkaConsumer.TOPIC_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@Import(IngredientDtoKafkaConsumerIntTest.KafkaTestContainersConfiguration.class)
@SpringBootTest(classes = KafkaConsumerDemoApplication.class)
@DirtiesContext
class IngredientDtoKafkaConsumerIntTest {

    @Container
    public static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                    .withEmbeddedZookeeper();
    @Container
    public static final PostgreSQLContainer<?> db = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.3"))
            .withDatabaseName("consumer-user")
            .withUsername("consumer-user")
            .withPassword("consumer-pass");

    @Autowired
    private KafkaTemplate<String, IngredientMessage> template;

    @Autowired
    private IngredientJpaRepository ingredientJpaRepository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", db::getJdbcUrl);
        registry.add("spring.datasource.username", db::getUsername);
        registry.add("spring.datasource.password", db::getPassword);
    }

    @BeforeEach
    public void init() {
        ingredientJpaRepository.deleteAll();
    }

    @Test
    void testAdd() {
        var data = IngredientMessage.newBuilder()
                .setAction(EventAction.ADDED)
                .setReferenceId(UUID.randomUUID().toString())
                .setName("Milk")
                .setVersion(Version.V1)
                .build();

        Message<IngredientMessage> msg = MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                .build();
        template.send(msg);
        template.flush();

        Unreliables.retryUntilTrue(
                20,
                TimeUnit.SECONDS,
                () -> {
                    if (ingredientJpaRepository.count() == 0) {
                        return false;
                    }

                    var result = ingredientJpaRepository.findAll();

                    assertEquals(1, result.size());

                    var entity = result.get(0);
                    assertEquals(data.getName(), entity.getName());
                    assertEquals(data.getReferenceId(), entity.getReferenceId());
                    return true;
                }
        );
    }

    @Test
    void testUpdate() throws Exception {
        var id = UUID.randomUUID().toString();
        var ingredient = new Ingredient();
        ingredient.setName("Milk");
        ingredient.setReferenceId(id);
        ingredientJpaRepository.saveAndFlush(ingredient);

        var data = IngredientMessage.newBuilder()
                .setAction(EventAction.CHANGED)
                .setReferenceId(ingredient.getReferenceId())
                .setName("Milk-change")
                .setVersion(Version.V1)
                .build();

        Message<IngredientMessage> msg = MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                .build();
        template.send(msg);
        template.flush();

        Thread.sleep(2000);

        Unreliables.retryUntilTrue(
                20,
                TimeUnit.SECONDS,
                () -> {
                    if (ingredientJpaRepository.count() == 0) {
                        return false;
                    }

                    var result = ingredientJpaRepository.findAll();

                    assertEquals(1, result.size());

                    var entity = result.get(0);
                    assertEquals(data.getName(), entity.getName());
                    assertEquals(data.getReferenceId(), entity.getReferenceId());
                    return true;
                }
        );
    }

    @Test
    void testDelete() {

        var id = UUID.randomUUID().toString();
        var ingredient = new Ingredient();
        ingredient.setName("Milk");
        ingredient.setReferenceId(id);
        ingredientJpaRepository.saveAndFlush(ingredient);

        var data = IngredientMessage.newBuilder()
                .setAction(EventAction.DELETED)
                .setReferenceId(ingredient.getReferenceId())
                .setName(ingredient.getName())
                .setVersion(Version.V1)
                .build();

        Message<IngredientMessage> msg = MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                .build();
        template.send(msg);
        template.flush();

        Unreliables.retryUntilTrue(
                20,
                TimeUnit.SECONDS,
                () -> {
                    if (ingredientJpaRepository.count() == 1) {
                        return false;
                    }

                    var result = ingredientJpaRepository.findAll();

                    assertEquals(0, result.size());
                    return true;
                }
        );
    }

    @TestConfiguration
    public static class KafkaTestContainersConfiguration {
        @Bean
        @Primary
        public NewTopic newTopic() {
            return TopicBuilder.name(TOPIC_NAME)
                    .build();
        }
    }
}
