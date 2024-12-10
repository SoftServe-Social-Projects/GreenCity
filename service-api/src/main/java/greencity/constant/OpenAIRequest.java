package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OpenAIRequest {
    public static final String FORECAST =
        "respond in a personalized manner, concisely, and accurately. Use numbers to present approximate data. "
            + "Provide a forecast of this person's impact on the global environment, considering their habits "
            + "over the specified number of days:";
    public static final String ADVICE = "answer by starting with the words \"since you don't have active habits\" "
        + "in the specified language. and then advertise the benefits of this habit:";
}
