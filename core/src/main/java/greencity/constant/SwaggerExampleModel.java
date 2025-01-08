package greencity.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SwaggerExampleModel {
    private static final String IMAGE_DESCRIPTION = "pass image as base64 or upload image\n";

    private static final String BEFORE_EXAMPLE = """
        <div>
        \t<ul class="tab">
        \t\t<li class="tabitem active">
        \t\t\t<a class="tablinks" data-name="example">Example Value</a>
        \t\t</li>
        \t\t<li class="tabitem">
        \t\t\t<a class="tablinks" data-name="model">Model</a>
        \t\t</li>
        \t</ul>
        \t<pre>
        """;

    private static final String EXAMPLE =
        """
              "image": "string",
              "source": "https://example.org/",
              "shortInfo": "string",
              "tags": [
                "string"
              ],
              "titleTranslation":
                 {"content": "string",
                 "languageCode": "string"},
              "textTranslation":
                 {"content": "string",
                 "languageCode": "string"}
            """;

    private static final String AFTER_EXAMPLE = "\t</pre>\n"
        + "</div>";

    public static final String ADD_ECO_NEWS_REQUEST =
        "Add Eco News Request\n"
            + IMAGE_DESCRIPTION
            + BEFORE_EXAMPLE
            + "{\n"
            + EXAMPLE
            + "}\n"
            + AFTER_EXAMPLE;
    public static final String ADD_CUSTOM_HABIT_REQUEST =
        "Add Custom Habit Request\n"
            + IMAGE_DESCRIPTION
            + BEFORE_EXAMPLE
            + "{\n"
            + EXAMPLE
            + "}\n"
            + AFTER_EXAMPLE;
    public static final String UPDATE_ECO_NEWS =
        "Update Eco News\n"
            + IMAGE_DESCRIPTION
            + BEFORE_EXAMPLE
            + "{\n"
            + "  \"id\": 0,\n"
            + EXAMPLE
            + "}\n"
            + AFTER_EXAMPLE;
    public static final String ADD_EVENT = BEFORE_EXAMPLE
        + "{\n"
        + "\t\"title\":\"string\",\n"
        + "\t\"description\":\"string\",\n"
        + "\t\"open\":true,\n"
        + "\t\"datesLocations\":[\n"
        + "\t\t{\n"
        + "\t\t\t\"startDate\":\"2023-05-27T15:00:00Z\",\n"
        + "\t\t\t\"finishDate\":\"2023-05-27T17:00:00Z\",\n"
        + "\t\t\t\"coordinates\":{\n"
        + "\t\t\t\t\"latitude\":1,\n"
        + "\t\t\t\t\"longitude\":1\n"
        + "\t\t\t},\n"
        + "\t\t\t\"onlineLink\":\"string\"\n"
        + "\t\t}\n"
        + "\t],\n"
        + "\t\"tags\":[\"Social\"]\n"
        + "}" + AFTER_EXAMPLE;

    public static final String UPDATE_EVENT = BEFORE_EXAMPLE
        + "{\n"
        + "\t\"id\":\0,\n"
        + "\t\"title\":\"string\",\n"
        + "\t\"description\":\"string7string4string\",\n"
        + "\t\"datesLocations\":[\n"
        + "\t\t{\n"
        + "\t\t\t\"startDate\":\"2023-05-27T15:00:00Z\",\n"
        + "\t\t\t\"finishDate\":\"2023-05-27T17:00:00Z\",\n"
        + "\t\t\t\"onlineLink\":\"http://localhost:8080/swagger-ui.html#/events-controller\",\n"
        + "\t\t\t\"coordinates\":{\n"
        + "\t\t\t\t\"latitude\":1,\n"
        + "\t\t\t\t\"longitude\":1\n"
        + "\t\t\t}\n"
        + "\t\t}\n"
        + "\t],\n"
        + "\t\"titleImage\":\"string\",\n"
        + "\t\"additionalImages\":[\"string\"],\n"
        + "\t\"tags\":[\"Social\"],\n"
        + "\t\"open\":true\n"
        + "}";
}
