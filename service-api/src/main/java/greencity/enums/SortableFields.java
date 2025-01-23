package greencity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SortableFields {
    ID("id"),
    AMOUNT_ACQUIRED_USERS("amountAcquiredUsers"),
    COMPLEXITY("complexity"),
    COUNT_COMMENTS("countComments"),
    COUNT_OF_ECO_NEWS("countOfEcoNews"),
    CREATE_DATE("createDate"),
    CREATED_DATE("createdDate"),
    CREATION_DATE("creationDate"),
    DEFAULT_DURATION("defaultDuration"),
    DISLIKES("dislikes"),
    EMAIL("email"),
    EVENT_NAME("eventName"),
    IS_FAVORITE("isFavorite"),
    LIKES("likes"),
    MODIFIED_DATE("modifiedDate"),
    MUTUAL_FRIENDS("mutualFriends"),
    NAME("name"),
    POINTS("points"),
    RATING("rating"),
    REPLIES("replies"),
    TITLE("title");

    private final String fieldName;
}
