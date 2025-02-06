package greencity.service;

import greencity.achievement.AchievementCalculation;
import greencity.enums.AchievementAction;
import greencity.enums.AchievementCategoryType;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.placecomment.PlaceCommentRequestDto;
import greencity.dto.placecomment.PlaceCommentAdminDto;
import greencity.dto.placecomment.PlaceCommentResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.PlaceComment;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserBlockedException;
import greencity.repository.PlaceCommentRepo;
import greencity.repository.RatingPointsRepo;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The class provides implementation of the {@code CommentService}.
 *
 * @author Marian Milian
 * @version 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class PlaceCommentServiceImpl implements PlaceCommentService {
    private RestClient restClient;
    private PlaceCommentRepo placeCommentRepo;
    private PlaceService placeService;
    private PhotoService photoService;
    private ModelMapper modelMapper;
    private final greencity.rating.RatingCalculation ratingCalculation;
    private final HttpServletRequest httpServletRequest;
    private AchievementCalculation achievementCalculation;
    private final RatingPointsRepo ratingPointsRepo;

    /**
     * {@inheritDoc}
     *
     * @author Marian Milian
     */
    @Override
    public PlaceCommentResponseDto findById(Long id) {
        PlaceComment comment = placeCommentRepo.findById(id).orElseThrow(() -> new NotFoundException(""));
        return modelMapper.map(comment, PlaceCommentResponseDto.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Marian Milian
     */
    @Override
    public PlaceCommentResponseDto save(Long placeId, PlaceCommentRequestDto placeCommentRequestDto, String email) {
        UserVO userVO = restClient.findByEmail(email);
        if (userVO.getUserStatus().equals(UserStatus.BLOCKED)) {
            throw new UserBlockedException(ErrorMessage.USER_HAS_BLOCKED_STATUS);
        }
        Place place = modelMapper.map(placeService.findById(placeId), Place.class);
        User user = modelMapper.map(userVO, User.class);
        PlaceComment comment = modelMapper.map(placeCommentRequestDto, PlaceComment.class);
        comment.setPlace(place);
        comment.setUser(user);
        if (comment.getEstimate() != null) {
            comment.getEstimate().setUser(user);
            comment.getEstimate().setPlace(place);
        }
        comment.getPhotos().forEach(photo -> {
            if (photoService.findByName(photo.getName()).isPresent()) {
                throw new BadRequestException(ErrorMessage.PHOTO_IS_PRESENT);
            }
            photo.setUser(user);
            photo.setComment(comment);
            photo.setPlace(place);
        });
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("COMMENT_OR_REPLY"), userVO);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.COMMENT_OR_REPLY, AchievementAction.ASSIGN);

        return modelMapper.map(placeCommentRepo.save(comment), PlaceCommentResponseDto.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Marian Milian
     */
    @Override
    public void deleteById(Long id) {
        placeCommentRepo.delete(placeCommentRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_EXCEPTION)));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserVO userVO = restClient.findByEmail(authentication.getName());
        ratingCalculation.ratingCalculation(ratingPointsRepo.findByNameOrThrow("UNDO_COMMENT_OR_REPLY"), userVO);
        achievementCalculation.calculateAchievement(userVO,
            AchievementCategoryType.COMMENT_OR_REPLY, AchievementAction.DELETE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<PlaceCommentAdminDto> getAllComments(Pageable pageable) {
        Page<PlaceComment> comments;
        List<PlaceCommentAdminDto> commentList;
        comments = placeCommentRepo.findAll(pageable);
        commentList =
            comments.getContent()
                .stream().map(comment -> modelMapper.map(comment, PlaceCommentAdminDto.class))
                .collect(Collectors.toList());
        return new PageableDto<>(
            commentList,
            comments.getTotalElements(),
            comments.getPageable().getPageNumber(),
            comments.getTotalPages());
    }
}
