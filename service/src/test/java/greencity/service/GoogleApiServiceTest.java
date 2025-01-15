package greencity.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.PlacesApi;
import com.google.maps.NearbySearchRequest;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.errors.ApiException;
import greencity.ModelUtils;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.geocoding.AddressLatLngResponse;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void getResultsFromGeocodeTest() throws IOException, InterruptedException, ApiException {
        Locale localeUk = Locale.forLanguageTag("uk");
        Locale localeEn = Locale.forLanguageTag("en");
        try (MockedStatic<GeocodingApi> geocodingApiMockedStatic = mockStatic(GeocodingApi.class)) {

            GeocodingApiRequest requestUk = mock(GeocodingApiRequest.class);
            GeocodingApiRequest requestEn = mock(GeocodingApiRequest.class);

            String searchRequest = "testSearchRequest";

            when(GeocodingApi.newRequest(context)).thenReturn(requestUk, requestEn);

            when(requestUk.address(searchRequest)).thenReturn(requestUk);
            when(requestUk.language(localeUk.getLanguage())).thenReturn(requestUk);
            when(requestUk.await()).thenReturn(ModelUtils.getGeocodingResultUk());

            when(requestEn.address(searchRequest)).thenReturn(requestEn);
            when(requestEn.language(localeEn.getLanguage())).thenReturn(requestEn);
            when(requestEn.await()).thenReturn(ModelUtils.getGeocodingResultEn());

            List<GeocodingResult> actual = googleApiService.getResultFromGeoCode(searchRequest);

            assertEquals(ModelUtils.getGeocodingResultUk().length + ModelUtils.getGeocodingResultEn().length,
                actual.size());
            verify(requestUk, times(1)).await();
            verify(requestEn, times(1)).await();
        }
    }

    @Test
    void getResultsFromGeocodeThrowsTest() throws IOException, InterruptedException, ApiException {
        String searchRequest = "testSearchRequest";

        Locale localeUk = Locale.forLanguageTag("uk");
        Locale localeEn = Locale.forLanguageTag("en");
        try (MockedStatic<GeocodingApi> geocodingApiMockedStatic = mockStatic(GeocodingApi.class)) {

            GeocodingApiRequest requestUk = mock(GeocodingApiRequest.class);
            GeocodingApiRequest requestEn = mock(GeocodingApiRequest.class);

            when(GeocodingApi.newRequest(context)).thenReturn(requestUk, requestEn);

            when(requestUk.address(searchRequest)).thenReturn(requestUk);
            when(requestUk.language(localeUk.getLanguage())).thenReturn(requestUk);
            when(requestUk.await()).thenThrow(ApiException.class);

            when(requestEn.address(searchRequest)).thenReturn(requestEn);
            when(requestEn.language(localeEn.getLanguage())).thenReturn(requestEn);
            when(requestEn.await()).thenThrow(ApiException.class);

            assertDoesNotThrow(() -> googleApiService.getResultFromGeoCode(searchRequest));

            verify(requestUk, times(1)).await();
            verify(requestEn, times(1)).await();
        }
    }

    @Test
    void getResultFromPlacesApiTest() throws IOException, InterruptedException, ApiException {
        FilterPlacesApiDto filterDto = ModelUtils.getFilterPlacesApiDto();
        UserVO userVO = ModelUtils.getUserVO();

        Locale localeUk = Locale.forLanguageTag("uk");
        Locale localeEn = Locale.forLanguageTag("en");
        try (MockedStatic<PlacesApi> placesApiMockedStatic = mockStatic(PlacesApi.class)) {
            NearbySearchRequest requestUk = mock(NearbySearchRequest.class);
            NearbySearchRequest requestEn = mock(NearbySearchRequest.class);

            when(PlacesApi.nearbySearchQuery(context, filterDto.getLocation())).thenReturn(requestUk, requestEn);

            when(requestUk.radius(filterDto.getRadius())).thenReturn(requestUk);
            when(requestUk.language(localeUk.getLanguage())).thenReturn(requestUk);
            when(requestUk.keyword(filterDto.getKeyword())).thenReturn(requestUk);
            when(requestUk.type(filterDto.getType())).thenReturn(requestUk);
            when(requestUk.rankby(filterDto.getRankBy())).thenReturn(requestUk);
            when(requestUk.minPrice(filterDto.getMinPrice())).thenReturn(requestUk);
            when(requestUk.maxPrice(filterDto.getMaxPrice())).thenReturn(requestUk);
            when(requestUk.openNow(filterDto.isOpenNow())).thenReturn(requestUk);
            when(requestUk.name(filterDto.getName())).thenReturn(requestUk);

            when(requestEn.radius(filterDto.getRadius())).thenReturn(requestEn);
            when(requestEn.language(localeEn.getLanguage())).thenReturn(requestEn);
            when(requestEn.keyword(filterDto.getKeyword())).thenReturn(requestEn);
            when(requestEn.type(filterDto.getType())).thenReturn(requestEn);
            when(requestEn.rankby(filterDto.getRankBy())).thenReturn(requestEn);
            when(requestEn.minPrice(filterDto.getMinPrice())).thenReturn(requestEn);
            when(requestEn.maxPrice(filterDto.getMaxPrice())).thenReturn(requestEn);
            when(requestEn.openNow(filterDto.isOpenNow())).thenReturn(requestEn);
            when(requestEn.name(filterDto.getName())).thenReturn(requestEn);

            when(requestUk.await()).thenReturn(ModelUtils.getPlacesSearchResponseUk());
            when(requestEn.await()).thenReturn(ModelUtils.getPlacesSearchResponseEn());

            List<PlacesSearchResult> actual = googleApiService.getResultFromPlacesApi(filterDto, userVO);

            List<PlacesSearchResult> expected = List.of(ModelUtils.getPlacesSearchResultUk().getFirst(),
                ModelUtils.getPlacesSearchResultEn().getFirst());

            assertEquals(actual.size(), expected.size());

            verify(requestUk, times(1)).await();
            verify(requestEn, times(1)).await();
        }
    }

    @Test
    void getResultFromPlacesApiNullLocationTest() throws IOException, InterruptedException, ApiException {
        FilterPlacesApiDto filterDto = ModelUtils.getFilterPlacesApiDto();
        filterDto.setLocation(null);
        UserVO userVO = ModelUtils.getUserVO();
        userVO.getUserLocationDto().setLongitude(null);

        Locale localeUk = Locale.forLanguageTag("uk");
        Locale localeEn = Locale.forLanguageTag("en");
        try (MockedStatic<PlacesApi> placesApiMockedStatic = mockStatic(PlacesApi.class)) {
            NearbySearchRequest requestUk = mock(NearbySearchRequest.class);
            NearbySearchRequest requestEn = mock(NearbySearchRequest.class);

            when(PlacesApi.nearbySearchQuery(context, filterDto.getLocation())).thenReturn(requestUk, requestEn);

            when(requestUk.radius(filterDto.getRadius())).thenReturn(requestUk);
            when(requestUk.language(localeUk.getLanguage())).thenReturn(requestUk);
            when(requestUk.keyword(filterDto.getKeyword())).thenReturn(requestUk);
            when(requestUk.type(filterDto.getType())).thenReturn(requestUk);
            when(requestUk.rankby(filterDto.getRankBy())).thenReturn(requestUk);
            when(requestUk.minPrice(filterDto.getMinPrice())).thenReturn(requestUk);
            when(requestUk.maxPrice(filterDto.getMaxPrice())).thenReturn(requestUk);
            when(requestUk.openNow(filterDto.isOpenNow())).thenReturn(requestUk);
            when(requestUk.name(filterDto.getName())).thenReturn(requestUk);

            when(requestEn.radius(filterDto.getRadius())).thenReturn(requestEn);
            when(requestEn.language(localeEn.getLanguage())).thenReturn(requestEn);
            when(requestEn.keyword(filterDto.getKeyword())).thenReturn(requestEn);
            when(requestEn.type(filterDto.getType())).thenReturn(requestEn);
            when(requestEn.rankby(filterDto.getRankBy())).thenReturn(requestEn);
            when(requestEn.minPrice(filterDto.getMinPrice())).thenReturn(requestEn);
            when(requestEn.maxPrice(filterDto.getMaxPrice())).thenReturn(requestEn);
            when(requestEn.openNow(filterDto.isOpenNow())).thenReturn(requestEn);
            when(requestEn.name(filterDto.getName())).thenReturn(requestEn);

            when(requestUk.await()).thenReturn(ModelUtils.getPlacesSearchResponseUk());
            when(requestEn.await()).thenReturn(ModelUtils.getPlacesSearchResponseEn());

            assertThrows(NotFoundException.class, () -> googleApiService.getResultFromPlacesApi(filterDto, userVO));

            verify(requestUk, times(0)).await();
            verify(requestEn, times(0)).await();
        }
    }

    @Test
    void getResultFromPlacesApiThrowsApiExceptionTest() throws IOException, InterruptedException, ApiException {
        FilterPlacesApiDto filterDto = ModelUtils.getFilterPlacesApiDto();
        UserVO userVO = ModelUtils.getUserVO();

        Locale localeUk = Locale.forLanguageTag("uk");
        Locale localeEn = Locale.forLanguageTag("en");
        try (MockedStatic<PlacesApi> placesApiMockedStatic = mockStatic(PlacesApi.class)) {
            NearbySearchRequest requestUk = mock(NearbySearchRequest.class);
            NearbySearchRequest requestEn = mock(NearbySearchRequest.class);

            when(PlacesApi.nearbySearchQuery(context, filterDto.getLocation())).thenReturn(requestUk, requestEn);

            when(requestUk.radius(filterDto.getRadius())).thenReturn(requestUk);
            when(requestUk.language(localeUk.getLanguage())).thenReturn(requestUk);
            when(requestUk.keyword(filterDto.getKeyword())).thenReturn(requestUk);
            when(requestUk.type(filterDto.getType())).thenReturn(requestUk);
            when(requestUk.rankby(filterDto.getRankBy())).thenReturn(requestUk);
            when(requestUk.minPrice(filterDto.getMinPrice())).thenReturn(requestUk);
            when(requestUk.maxPrice(filterDto.getMaxPrice())).thenReturn(requestUk);
            when(requestUk.openNow(filterDto.isOpenNow())).thenReturn(requestUk);
            when(requestUk.name(filterDto.getName())).thenReturn(requestUk);

            when(requestEn.radius(filterDto.getRadius())).thenReturn(requestEn);
            when(requestEn.language(localeEn.getLanguage())).thenReturn(requestEn);
            when(requestEn.keyword(filterDto.getKeyword())).thenReturn(requestEn);
            when(requestEn.type(filterDto.getType())).thenReturn(requestEn);
            when(requestEn.rankby(filterDto.getRankBy())).thenReturn(requestEn);
            when(requestEn.minPrice(filterDto.getMinPrice())).thenReturn(requestEn);
            when(requestEn.maxPrice(filterDto.getMaxPrice())).thenReturn(requestEn);
            when(requestEn.openNow(filterDto.isOpenNow())).thenReturn(requestEn);
            when(requestEn.name(filterDto.getName())).thenReturn(requestEn);

            when(requestUk.await()).thenReturn(ModelUtils.getPlacesSearchResponseUk());
            when(requestEn.await()).thenThrow(ApiException.class);

            assertDoesNotThrow(() -> googleApiService.getResultFromPlacesApi(filterDto, userVO));

            verify(requestUk, times(1)).await();
            verify(requestEn, times(1)).await();
        }
    }
}
