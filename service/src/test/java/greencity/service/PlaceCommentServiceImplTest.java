package greencity.service;

import greencity.ModelUtils;
import greencity.achievement.AchievementCalculation;
import greencity.client.RestClient;
import greencity.dto.PageableDto;
import greencity.dto.placecomment.PlaceCommentRequestDto;
import greencity.dto.placecomment.PlaceCommentAdminDto;
import greencity.dto.placecomment.PlaceCommentResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.PlaceComment;
import greencity.entity.RatingPoints;
import greencity.enums.UserStatus;
import greencity.rating.RatingCalculation;
import greencity.repository.PlaceCommentRepo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import greencity.repository.RatingPointsRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PlaceCommentServiceImplTest {
    @Mock
    private PlaceCommentRepo placeCommentRepo;
    @Mock
    private PlaceService placeService;
    @Mock
    private RestClient restClient;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private PlaceCommentServiceImpl placeCommentService;
    @Mock
    private UserService userService;
    @Mock
    private RatingPointsRepo ratingPointsRepo;
    @Mock
    private RatingCalculation ratingCalculation;
    @Mock
    private AchievementCalculation achievementCalculation;

    @Test
    void findByIdTest() {
        PlaceComment comment = ModelUtils.getPlaceComment();
        PlaceCommentResponseDto placeCommentResponseDto = ModelUtils.getCommentReturnDto();
        when(placeCommentRepo.findById(1L))
            .thenReturn(Optional.of(comment));
        when(modelMapper.map(comment, PlaceCommentResponseDto.class))
            .thenReturn(placeCommentResponseDto);
        PlaceCommentResponseDto result = placeCommentService.findById(1L);
        assertEquals(1, (long) result.getId());
    }

    @Test
    void deleteByIdTest() {
        String accessToken = "Token";
        UserVO userVO = ModelUtils.getUserVO();
        PlaceComment comment = ModelUtils.getPlaceComment();
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("UNDO_LIKE_COMMENT_OR_REPLY").points(-1).build();
        when(ratingPointsRepo.findByNameOrThrow("UNDO_LIKE_COMMENT_OR_REPLY")).thenReturn(ratingPoints);
        when(placeCommentRepo.findById(anyLong())).thenReturn(Optional.of(comment));
        doNothing().when(placeCommentRepo).delete(comment);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("email");
        when(restClient.findByEmail("email")).thenReturn(userVO);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(accessToken);

        when(userService.findById(any())).thenReturn(ModelUtils.getUserVO());

        placeCommentService.deleteById(1L);
        verify(placeCommentRepo, times(1)).delete(comment);
    }

    @Test
    void saveTest() {
        String token = "token";
        PlaceCommentRequestDto placeCommentRequestDto = ModelUtils.getAddCommentDto();
        PlaceComment comment = ModelUtils.getPlaceComment();
        RatingPoints ratingPoints = RatingPoints.builder().id(1L).name("LIKE_COMMENT_OR_REPLY").points(1).build();
        when(ratingPointsRepo.findByNameOrThrow("LIKE_COMMENT_OR_REPLY")).thenReturn(ratingPoints);
        when(placeService.findById(anyLong())).thenReturn(ModelUtils.getPlaceVO());
        UserVO userVO = ModelUtils.getUserVO();
        userVO.setUserStatus(UserStatus.ACTIVATED);
        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(placeCommentRequestDto, PlaceComment.class)).thenReturn(comment);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(token);
        when(modelMapper.map(comment, PlaceCommentResponseDto.class))
            .thenReturn(ModelUtils.getCommentReturnDto());
        when(placeCommentRepo.save(any())).thenReturn(comment);
        placeCommentService.save(1L, placeCommentRequestDto, "email");
        verify(placeCommentRepo, times(1)).save(comment);
    }

    @Test
    void getAllCommentsTest() {
        PageRequest pageRequest = PageRequest.of(0, 2);
        List<PlaceCommentAdminDto> commentAdminDtos = Collections.singletonList(new PlaceCommentAdminDto());
        List<PlaceComment> list = Collections.singletonList(ModelUtils.getPlaceComment());
        Page<PlaceComment> comments = new PageImpl<>(list, pageRequest, list.size());

        PageableDto<PlaceCommentAdminDto> result = new PageableDto<>(commentAdminDtos, commentAdminDtos.size(), 0, 1);
        when(modelMapper.map(list.getFirst(), PlaceCommentAdminDto.class)).thenReturn(new PlaceCommentAdminDto());
        when(placeCommentRepo.findAll(pageRequest)).thenReturn(comments);

        assertEquals(result, placeCommentService.getAllComments(pageRequest));
    }
}
