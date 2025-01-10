package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.placecomment.PlaceCommentRequestDto;
import greencity.dto.placecomment.PlaceCommentAdminDto;
import greencity.dto.placecomment.PlaceCommentResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Provides the interface to manage {@code Comment} entity.
 *
 * @author Marian Milian
 * @version 1.0
 */
public interface PlaceCommentService {
    /**
     * Method witch return comment by id.
     *
     * @param id of search Comment
     * @return {@link PlaceCommentResponseDto}.
     */
    PlaceCommentResponseDto findById(Long id);

    /**
     * Save Comment by Place id.
     *
     * @param placeId                Place id to witch related.
     * @param placeCommentRequestDto DTO witch contain data of.
     * @param email                  of User who add comment.
     * @return @{link {@link PlaceCommentResponseDto}}.
     * @author Marian Milian.
     */
    PlaceCommentResponseDto save(Long placeId, PlaceCommentRequestDto placeCommentRequestDto, String email);

    /**
     * Method witch delete Comment by Id.
     *
     * @param id of delete comment.
     * @author Rostyslav Khasanov.
     */
    void deleteById(Long id);

    /**
     * Method witch return all comments by page.
     *
     * @param pageable pageable configuration.
     * @return {@link PageableDto}
     * @author Rostyslav Khasanov.
     */
    PageableDto<PlaceCommentAdminDto> getAllComments(Pageable pageable);
}
