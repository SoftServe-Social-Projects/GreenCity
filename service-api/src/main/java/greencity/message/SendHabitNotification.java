package greencity.message;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Message, that is used for sending emails about not marked habits.
 */
@Getter
@ToString
@AllArgsConstructor
@Builder
public class SendHabitNotification implements Serializable, EmailMessage {
    private String name;
    private String email;

    @Override
    public String getEmail() {
        return email;
    }
}
