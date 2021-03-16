package greencity.config;

import static greencity.constant.RabbitConstants.*;

import greencity.dto.econews.EcoNewsVO;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that is used for managing RabbitMQ-related settings. It is
 * responsible for exchanges and queues declarations as well as binding them
 * together.
 */
@Configuration
@EnableRabbit
public class EmailServiceRabbitConfig {
    @Value("${messaging.rabbit.email.topic}")
    private String emailTopicExchangeName;

    /**
     * Topic exchange declaration that is used for email-related queues.
     *
     * @return topic exchange for email-related messages and queues.
     */
    @Bean
    public TopicExchange emailTopicExchange() {
        return new TopicExchange(emailTopicExchangeName);
    }

    /**
     * Queue that is used for sending password recovery emails. It is durable since
     * password recovery is security related functionality.
     *
     * @return durable queue that is meant for sending password recovery email
     *         letters.
     */
    @Bean
    public Queue passwordRecoveryEmailQueue() {
        return new Queue("password-recovery-queue", true);
    }

    /**
     * The binding that is used for linking email topic exchange to password
     * recovery email queue.
     *
     * @return Binding with topic exchange and password recovery queue linked.
     */
    @Bean
    public Binding passwordRecoveryQueueToEmailTopicBinding(TopicExchange emailTopicExchange) {
        return BindingBuilder
            .bind(passwordRecoveryEmailQueue())
            .to(emailTopicExchange)
            .with(PASSWORD_RECOVERY_ROUTING_KEY);
    }

    /**
     * Queue that is used for sending emails for finishing user approval. It is
     * durable since user approval is security related functionality.
     *
     * @return durable queue that is meant for sending user approval email letters.
     */
    @Bean
    public Queue userApprovalQueue() {
        return new Queue("finish-user-approval", true);
    }

    /**
     * The binding that is used for linking email topic exchange to user approval
     * queue.
     *
     * @return Binding with topic exchange and user approval queue linked.
     */
    @Bean
    public Binding userApprovalQueueToEmailTopicBinding(TopicExchange emailTopicExchange) {
        return BindingBuilder
            .bind(userApprovalQueue())
            .to(emailTopicExchange)
            .with(SEND_USER_APPROVAL_ROUTING_KEY);
    }

    /**
     * Queue that is used for change place status emails.
     *
     * @return durable queue that is meant for sending change place status email
     *         letters.
     */
    @Bean
    public Queue changePlaceStatusEmailQueue() {
        return new Queue("change-place-status", true);
    }

    /**
     * The binding that is used for linking email topic exchange to change place
     * status email queue.
     *
     * @return Binding with topic exchange and change place status queue linked.
     */
    @Bean
    public Binding changePlaceStatusQueueToEmailTopicBinding(TopicExchange emailTopicExchange) {
        return BindingBuilder
            .bind(changePlaceStatusEmailQueue())
            .to(emailTopicExchange)
            .with(CHANGE_PLACE_STATUS_ROUTING_KEY);
    }

    /**
     * Queue, which stores messages for sending notification about adding new
     * {@link EcoNewsVO}.
     *
     * @return Queue, for sending notification about adding new {@link EcoNewsVO}.
     */
    @Bean
    Queue ecoNewsEmailQueue() {
        return new Queue("eco_news_queue", true);
    }

    /**
     * Method, that bind {@link this#ecoNewsEmailQueue()} with
     * {@link this#emailTopicExchange()}.
     *
     * @param emailTopicExchange exchange to bind queue with.
     * @param ecoNewsEmailQueue  queue to bind exchange with.
     * @return binding with {@link this#ecoNewsEmailQueue()} and
     *         {@link this#emailTopicExchange()}.
     */
    @Bean
    public Binding ecoNewsQueueToEmailTopicBinding(TopicExchange emailTopicExchange, Queue ecoNewsEmailQueue) {
        return BindingBuilder
            .bind(ecoNewsEmailQueue)
            .to(emailTopicExchange)
            .with(ADD_ECO_NEWS_ROUTING_KEY);
    }

    /**
     * Queue that is used for verify email.
     *
     * @return durable queue that is meant for sending verify email.
     */
    @Bean
    public Queue signUpVerifyEmailQueue() {
        return new Queue("verify-email-queue", true);
    }

    /**
     * The binding that is used for send verify email..
     *
     * @return Binding with send verify email.
     */
    @Bean
    public Binding verifyEmailQueueToEmailTopicBinding(TopicExchange emailTopicExchange) {
        return BindingBuilder
            .bind(signUpVerifyEmailQueue())
            .to(emailTopicExchange)
            .with(VERIFY_EMAIL_ROUTING_KEY);
    }

    /**
     * Queue that is used for sending report emails .
     *
     * @return durable queue that is meant for sending report email letters.
     */
    @Bean
    public Queue sendReportEmailQueue() {
        return new Queue("send-report", true);
    }

    /**
     * The binding that is used for linking email topic exchange to send report
     * email queue.
     *
     * @return Binding with topic exchange and send report queue linked.
     */
    @Bean
    public Binding sendReportEmailTopicBinding(TopicExchange emailTopicExchange) {
        return BindingBuilder
            .bind(sendReportEmailQueue())
            .to(emailTopicExchange)
            .with(SEND_REPORT_ROUTING_KEY);
    }

    /**
     * Queue that is used for sending notifications about not marked habits .
     *
     * @return durable queue that is meant for sending notification email letters.
     */
    @Bean
    public Queue sendHabitNotificationQueue() {
        return new Queue("send-habit-notification-queue", true);
    }

    /**
     * The binding that is used for linking email topic exchange to send habit
     * notification email queue.
     *
     * @return Binding with topic exchange and send notification queue linked.
     */
    @Bean
    public Binding sendHabitNotificationTopicBinding(TopicExchange emailTopicExchange) {
        return BindingBuilder
            .bind(sendHabitNotificationQueue())
            .to(emailTopicExchange)
            .with(SEND_HABIT_NOTIFICATION_ROUTING_KEY);
    }
}
