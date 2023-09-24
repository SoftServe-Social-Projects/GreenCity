package greencity.enums;

import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.NotFoundException;
import lombok.Getter;

@Getter
public enum RatingCalculationEnum {
    DAYS_OF_HABIT_IN_PROGRESS(1),
    CREATE_NEWS(20),
    COMMENT_OR_REPLY(2),
    LIKE_COMMENT_OR_REPLY(10),
    SHARE_NEWS(20),
    UNDO_DAYS_OF_HABIT_IN_PROGRESS(-1),
    DELETE_NEWS(-20),
    DELETE_COMMENT_OR_REPLY(-2),
    UNLIKE_COMMENT_OR_REPLY(-10),
    UNDO_SHARE_NEWS(-20),

    ACQUIRED_HABIT_14_DAYS(20),
    ACQUIRED_HABIT_21_DAYS(30),
    ACQUIRED_HABIT_30_DAYS(40),
    CREATED_1_NEWS(10),
    CREATED_5_NEWS(10),
    CREATED_10_NEWS(10),
    CREATED_25_NEWS(10),
    CREATED_50_NEWS(10),
    CREATED_100_NEWS(10),
    COMMENT_OR_REPLY_1_TIMES(1),
    COMMENT_OR_REPLY_5_TIMES(5),
    COMMENT_OR_REPLY_10_TIMES(10),
    COMMENT_OR_REPLY_25_TIMES(25),
    COMMENT_OR_REPLY_50_TIMES(50),
    COMMENT_OR_REPLY_100_TIMES(100),
    LIKE_COMMENT_OR_REPLY_1_TIMES(1),
    LIKE_COMMENT_OR_REPLY_5_TIMES(5),
    LIKE_COMMENT_OR_REPLY_10_TIMES(10),
    LIKE_COMMENT_OR_REPLY_25_TIMES(25),
    LIKE_COMMENT_OR_REPLY_50_TIMES(50),
    LIKE_COMMENT_OR_REPLY_100_TIMES(100),
    SHARE_NEWS_1_TIMES(1),
    SHARE_NEWS_5_TIMES(5),
    SHARE_NEWS_10_TIMES(10),
    SHARE_NEWS_25_TIMES(25),
    SHARE_NEWS_50_TIMES(50),
    SHARE_NEWS_100_TIMES(100),
    FIRST_5_ACHIEVEMENTS(10),
    FIRST_10_ACHIEVEMENTS(25),
    FIRST_25_ACHIEVEMENTS(50),
    FIRST_50_ACHIEVEMENTS(100),
    FIRST_100_ACHIEVEMENTS(200),
    UNDO_ACQUIRED_HABIT_14_DAYS(-20),
    UNDO_ACQUIRED_HABIT_21_DAYS(-30),
    UNDO_ACQUIRED_HABIT_30_DAYS(-40),
    UNDO_CREATED_1_NEWS(-10),
    UNDO_CREATED_5_NEWS(-10),
    UNDO_CREATED_10_NEWS(-10),
    UNDO_CREATED_25_NEWS(-10),
    UNDO_CREATED_50_NEWS(-10),
    UNDO_CREATED_100_NEWS(-10),
    UNDO_COMMENT_OR_REPLY_1_TIMES(-1),
    UNDO_COMMENT_OR_REPLY_5_TIMES(-5),
    UNDO_COMMENT_OR_REPLY_10_TIMES(-10),
    UNDO_COMMENT_OR_REPLY_25_TIMES(-25),
    UNDO_COMMENT_OR_REPLY_50_TIMES(-50),
    UNDO_COMMENT_OR_REPLY_100_TIMES(-100),
    UNDO_LIKE_COMMENT_OR_REPLY_1_TIMES(-1),
    UNDO_LIKE_COMMENT_OR_REPLY_5_TIMES(-5),
    UNDO_LIKE_COMMENT_OR_REPLY_10_TIMES(-10),
    UNDO_LIKE_COMMENT_OR_REPLY_25_TIMES(-25),
    UNDO_LIKE_COMMENT_OR_REPLY_50_TIMES(-50),
    UNDO_LIKE_COMMENT_OR_REPLY_100_TIMES(-100),
    UNDO_SHARE_NEWS_1_TIMES(-1),
    UNDO_SHARE_NEWS_5_TIMES(-5),
    UNDO_SHARE_NEWS_10_TIMES(-10),
    UNDO_SHARE_NEWS_25_TIMES(-25),
    UNDO_SHARE_NEWS_50_TIMES(-50),
    UNDO_SHARE_NEWS_100_TIMES(-100),
    UNDO_FIRST_5_ACHIEVEMENTS(-10),
    UNDO_FIRST_10_ACHIEVEMENTS(-25),
    UNDO_FIRST_25_ACHIEVEMENTS(-50),
    UNDO_FIRST_50_ACHIEVEMENTS(-100),
    UNDO_FIRST_100_ACHIEVEMENTS(-200);

    private final int ratingPoints;

    RatingCalculationEnum(int ratingPoints) {
        this.ratingPoints = ratingPoints;
    }

    /**
     * Finds the RatingCalculationEnum object corresponding to the given name.
     *
     * @param name The name of the enum to find.
     * @return The found RatingCalculationEnum object.
     * @throws NotFoundException if the enum by the given name does not exist.
     */
    public static RatingCalculationEnum findEnumByName(String name) {
        try {
            return RatingCalculationEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(ErrorMessage.RATING_CALCULATION_ENUM_NOT_FOUND_BY_NAME + name);
        }
    }
}
