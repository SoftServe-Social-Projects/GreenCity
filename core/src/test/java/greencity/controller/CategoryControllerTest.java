package greencity.controller;

import greencity.dto.category.CategoryDto;
import greencity.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    CategoryService categoryService;

    @InjectMocks
    CategoryController categoryController;

    private static final String categoryLink = "/categories";

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void saveCategory() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder().name("content").build();
        mockMvc.perform(post(categoryLink)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "name": "content"
                    }
                """))
            .andExpect(status().isCreated());

        verify(categoryService, times(1)).save(categoryDto);
    }

    @Test
    void findAllTest() throws Exception {
        mockMvc.perform(get(categoryLink)).andExpect(status().isOk());
        verify(categoryService, times(1)).findAllCategoryDto();
    }

}