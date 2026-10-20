package com.learnloop.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String TOPIC_ENROLLMENT = "learnloop.enrollment.events";
    public static final String TOPIC_COURSE = "learnloop.course.events";
    public static final String TOPIC_DISCUSSION = "learnloop.discussion.events";
    public static final String TOPIC_ASSIGNMENT = "learnloop.assignment.events";
    public static final String TOPIC_NOTIFICATION = "learnloop.notification.events";
    public static final String TOPIC_LEARNING_ACTIVITY = "learnloop.learning-activity.events";
    public static final String TOPIC_DLT = "learnloop.dlt";

    @Bean
    public NewTopic enrollmentTopic() {
        return TopicBuilder.name(TOPIC_ENROLLMENT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic courseTopic() {
        return TopicBuilder.name(TOPIC_COURSE).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic discussionTopic() {
        return TopicBuilder.name(TOPIC_DISCUSSION).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic assignmentTopic() {
        return TopicBuilder.name(TOPIC_ASSIGNMENT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic learningActivityTopic() {
        return TopicBuilder.name(TOPIC_LEARNING_ACTIVITY).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(TOPIC_DLT).partitions(3).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // Retries 3 times with 1000ms delay, then forwards to Dead Letter Topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
