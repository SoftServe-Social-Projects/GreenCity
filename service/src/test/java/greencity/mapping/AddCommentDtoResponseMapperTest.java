package greencity.mapping;

import greencity.dto.comment.AddCommentDtoResponse;
import greencity.entity.Comment;
import greencity.entity.CommentImages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class AddCommentDtoResponseMapperTest {

    @InjectMocks
    AddCommentDtoResponseMapper mapper;

    @Test
    void testConvert_WithAdditionalImages() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Comment");
        comment.setCreatedDate(LocalDateTime.now());

        CommentImages image1 = new CommentImages();
        image1.setLink("http://example.com/image1.jpg");
        comment.setAdditionalImages(List.of(image1));

        AddCommentDtoResponse result = mapper.convert(comment);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Comment", result.getText());
        assertNotNull(result.getCreatedDate());
        assert result.getAdditionalImages() != null;
        assertEquals("http://example.com/image1.jpg", result.getAdditionalImages().getFirst());
    }

    @Test
    void testConvert_WithoutAdditionalImages() {
        Comment comment = new Comment();
        comment.setId(2L);
        comment.setText("Comment");
        comment.setCreatedDate(LocalDateTime.now());
        comment.setAdditionalImages(null);

        AddCommentDtoResponse result = mapper.convert(comment);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Comment", result.getText());
        assertNotNull(result.getCreatedDate());
        assertNull(result.getAdditionalImages());
    }
}
