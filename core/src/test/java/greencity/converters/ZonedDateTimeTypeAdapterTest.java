package greencity.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.ZonedDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ZonedDateTimeTypeAdapterTest {
    @InjectMocks
    private ZonedDateTimeTypeAdapter zonedDateTimeTypeAdapter;

    @Test
    void serializeAndDeserializeTest() {
        ZonedDateTime originalDateTime = ZonedDateTime.now();

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, zonedDateTimeTypeAdapter)
            .create();

        String json = gson.toJson(originalDateTime);
        ZonedDateTime deserializedDateTime = gson.fromJson(json, ZonedDateTime.class);

        assertEquals(originalDateTime, deserializedDateTime);
    }
}