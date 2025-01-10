package greencity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ArticleType {
    EVENT("Event", "events"),
    ECO_NEWS("Eco news", "econews"),
    HABIT("Habit", "habits");

    private final String name;
    private final String link;
}
