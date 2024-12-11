package greencity.message;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ScheduledEmailMessage implements EmailMessage {
    private String username;
    private String email;
    private String baseLink;
    private String subject;
    private String body;
    private String language;
    private boolean isUbs;
}
