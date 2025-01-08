package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.entity.RatingPoints;
import greencity.enums.Status;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.RatingPointsRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@AllArgsConstructor
@Service
public class RatingPointsServiceImpl implements RatingPointsService {
    private RatingPointsRepo ratingPointsRepo;
    private final ModelMapper modelMapper;
    private static final String UNDO = "UNDO_";

    @Override
    public PageableAdvancedDto<RatingPointsDto> getAllRatingPointsByPage(Pageable pageable) {
        Page<RatingPoints> ratingPoints = ratingPointsRepo.findAllByStatus(Status.ACTIVE, pageable);
        return mapToPageableAdvancedDto(ratingPoints);
    }

    @Override
    public List<RatingPointsDto> createRatingPoints(String ratingPointsName) {
        List<RatingPoints> ratingPoints = List.of(ratingPointsRepo.save(new RatingPoints(ratingPointsName)),
            ratingPointsRepo.save(new RatingPoints(UNDO + ratingPointsName)));
        return ratingPoints.stream().map(rating -> modelMapper.map(rating, RatingPointsDto.class)).toList();
    }

    @Override
    public RatingPointsDto updateRatingPoints(RatingPointsDto ratingPointsDto) {
        RatingPoints existingRatingPoints = ratingPointsRepo.findById(ratingPointsDto.getId())
            .orElseThrow(
                () -> new NotFoundException(ErrorMessage.RATING_POINTS_NOT_FOUND_BY_ID + ratingPointsDto.getId()));

        existingRatingPoints.setName(ratingPointsDto.getName());
        existingRatingPoints.setPoints(ratingPointsDto.getPoints());

        return modelMapper.map(ratingPointsRepo.save(existingRatingPoints), RatingPointsDto.class);
    }

    @Override
    public void deleteRatingPoints(Long id) {
        try {
            checkByIdForExistenceOfAchievement(id);
            ratingPointsRepo.updateStatusById(id, Status.DELETE);
        } catch (EmptyResultDataAccessException | IllegalArgumentException e) {
            throw new NotFoundException(ErrorMessage.RATING_POINTS_NOT_FOUND_BY_ID + id, e);
        }
    }

    @Override
    public PageableAdvancedDto<RatingPointsDto> getDeletedRatingPoints(Pageable pageable) {
        Page<RatingPoints> ratingPoints = ratingPointsRepo.findAllByStatus(Status.DELETE, pageable);
        return mapToPageableAdvancedDto(ratingPoints);
    }

    @Override
    public void restoreDeletedRatingPoints(Long id) {
        try {
            ratingPointsRepo.updateStatusById(id, Status.ACTIVE);
        } catch (Exception e) {
            throw new NotFoundException(ErrorMessage.RATING_POINTS_NOT_FOUND_BY_ID + id, e);
        }
    }

    @Override
    public void updateRatingPointsName(String oldName, String newName) {
        try {
            ratingPointsRepo.updateRatingPointsName(oldName, newName);
            ratingPointsRepo.updateRatingPointsName(UNDO + oldName, UNDO + newName);
        } catch (Exception e) {
            throw new NotFoundException(ErrorMessage.RATING_POINTS_NOT_FOUND_BY_NAME + oldName, e);
        }
    }

    @Override
    public PageableAdvancedDto<RatingPointsDto> searchBy(Pageable pageable, String searchQuery, Status status) {
        return mapToPageableAdvancedDto(ratingPointsRepo.searchBy(pageable, searchQuery, status));
    }

    private PageableAdvancedDto<RatingPointsDto> mapToPageableAdvancedDto(Page<RatingPoints> ratingPoints) {
        List<RatingPointsDto> ratingPointsDtos = ratingPoints.stream()
            .map(rating -> modelMapper.map(rating, RatingPointsDto.class)).toList();
        return new PageableAdvancedDto<>(
            ratingPointsDtos,
            ratingPoints.getTotalElements(),
            ratingPoints.getPageable().getPageNumber(),
            ratingPoints.getTotalPages(),
            ratingPoints.getNumber(),
            ratingPoints.hasPrevious(),
            ratingPoints.hasNext(),
            ratingPoints.isFirst(),
            ratingPoints.isLast());
    }

    private void checkByIdForExistenceOfAchievement(Long id) {
        if (ratingPointsRepo.checkByIdForExistenceOfAchievement(id)) {
            throw new IllegalStateException(ErrorMessage.DELETING_RATING_POINTS_NOT_ALLOWED);
        }
    }
}
