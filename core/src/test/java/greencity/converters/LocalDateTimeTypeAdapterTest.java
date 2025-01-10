package greencity.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LocalDateTimeTypeAdapterTest {
    @InjectMocks
    private LocalDateTimeTypeAdapter localDateTimeTypeAdapter;

    @Test
    void serializeAndDeserializeTest() {
        LocalDateTime originalDateTime = LocalDateTime.now();

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter)
            .create();

        String json = gson.toJson(originalDateTime);
        LocalDateTime deserializedDateTime = gson.fromJson(json, LocalDateTime.class);

        assertEquals(originalDateTime, deserializedDateTime);
    }
}