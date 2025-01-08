package greencity.dto.place;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterAdminPlaceDtoTest {

    @Test
    void isEmptyWithAllFieldsNullTest() {
        FilterAdminPlaceDto filter = new FilterAdminPlaceDto();
        assertTrue(filter.isEmpty());
    }

    @Test
    void isEmptyWithAllFieldsEmptyTest() {
        FilterAdminPlaceDto filter = new FilterAdminPlaceDto();
        filter.setId("");
        filter.setName("");
        filter.setStatus("");
        filter.setAuthor("");
        filter.setAddress("");

        assertTrue(filter.isEmpty());
    }

    @Test
    void isEmptyWithSomeFieldsTest() {
        FilterAdminPlaceDto filter = new FilterAdminPlaceDto();
        filter.setId("1");

        assertFalse(filter.isEmpty());
    }

    @Test
    void isEmptyWithAllFieldsTest() {
        FilterAdminPlaceDto filter = new FilterAdminPlaceDto();
        filter.setId("1");
        filter.setName("test name");
        filter.setStatus("APPROVED");
        filter.setAuthor("author name");
        filter.setAddress("test address");

        assertFalse(filter.isEmpty());
    }
}
