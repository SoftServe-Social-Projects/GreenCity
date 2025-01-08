package greencity.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.ModelUtils;
import greencity.dto.geocoding.AddressLatLngResponse;
import greencity.exception.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GoogleApiServiceTest {
    @Mock
    private GeoApiContext context;

    @InjectMocks
    private GoogleApiService googleApiService;

    @Test
    void getResultFromGeoCodeByCoordinatesTest() throws IOException, InterruptedException, ApiException {
        AddressLatLngResponse expected = ModelUtils.getAddressLatLngResponse();
        LatLng latLng = new LatLng(expected.getLatitude(), expected.getLongitude());
        try (MockedStatic<GeocodingApi> geocodingApiMockedStatic = mockStatic(GeocodingApi.class)) {
            GeocodingApiRequest request = mock(GeocodingApiRequest.class);
            GeocodingApiRequest requestEn = mock(GeocodingApiRequest.class);
            when(GeocodingApi.newRequest(context)).thenReturn(request);
            when(request.latlng(latLng)).thenReturn(request);
            when(request.language(Locale.of("uk").getLanguage())).thenReturn(request);
            when(request.language(Locale.of("en").getLanguage())).thenReturn(requestEn);
            when(request.await()).thenReturn(ModelUtils.getGeocodingResultUk());
            when(requestEn.await()).thenReturn(ModelUtils.getGeocodingResultEn());
            assertEquals(expected, googleApiService.getResultFromGeoCodeByCoordinates(latLng));
            verify(request, times(1)).await();
            verify(requestEn, times(1)).await();
        }
    }

    @Test
    void getResultFromGeoCodeByCoordinatesWithInvalidUkrainianRequestTest()
        throws IOException, InterruptedException, ApiException {
        AddressLatLngResponse expected = ModelUtils.getAddressLatLngResponse();
        LatLng latLng = new LatLng(expected.getLatitude(), expected.getLongitude());
        try (MockedStatic<GeocodingApi> geocodingApiMockedStatic = mockStatic(GeocodingApi.class)) {
            GeocodingApiRequest request = mock(GeocodingApiRequest.class);
            when(GeocodingApi.newRequest(context)).thenReturn(request);
            when(request.latlng(latLng)).thenReturn(request);
            when(request.language(Locale.of("uk").getLanguage())).thenReturn(request);
            when(request.await()).thenThrow(ApiException.class);
            assertThrows(BadRequestException.class, () -> googleApiService.getResultFromGeoCodeByCoordinates(latLng));
            verify(request, times(1)).await();
        }
    }

    @Test
    void getResultFromGeoCodeByCoordinatesWithInvalidCoordinatesTest()
        throws IOException, InterruptedException, ApiException {
        LatLng latLng = new LatLng(0.0, 0.0);
        try (MockedStatic<GeocodingApi> geocodingApiMockedStatic = mockStatic(GeocodingApi.class)) {
            GeocodingApiRequest request = mock(GeocodingApiRequest.class);
            when(GeocodingApi.newRequest(context)).thenReturn(request);
            when(request.latlng(latLng)).thenReturn(request);
            when(request.language(Locale.of("uk").getLanguage())).thenReturn(request);
            when(request.await()).thenReturn(new GeocodingResult[0]);

            assertThrows(BadRequestException.class, () -> googleApiService.getResultFromGeoCodeByCoordinates(latLng));

            verify(request).latlng(latLng);
            verify(request).language(Locale.of("uk").getLanguage());
            verify(request).await();
        }
    }

    @Test
    void getResultFromGeoCodeByCoordinatesWithNullResultsTest()
        throws IOException, InterruptedException, ApiException {
        LatLng latLng = new LatLng(0.0, 0.0);
        try (MockedStatic<GeocodingApi> geocodingApiMockedStatic = mockStatic(GeocodingApi.class)) {
            GeocodingApiRequest request = mock(GeocodingApiRequest.class);
            when(GeocodingApi.newRequest(context)).thenReturn(request);
            when(request.latlng(latLng)).thenReturn(request);
            when(request.language(Locale.of("uk").getLanguage())).thenReturn(request);
            when(request.await()).thenReturn(null);

            assertThrows(BadRequestException.class, () -> googleApiService.getResultFromGeoCodeByCoordinates(latLng));

            verify(request).latlng(latLng);
            verify(request).language(Locale.of("uk").getLanguage());
            verify(request).await();
        }
    }
}
