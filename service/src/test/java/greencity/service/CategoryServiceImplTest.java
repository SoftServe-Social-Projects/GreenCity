package greencity.service;

import greencity.dto.category.CategoryDto;
import greencity.dto.category.CategoryDtoResponse;
import greencity.entity.Category;
import greencity.entity.Place;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.CategoryRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepo categoryRepo;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    private final Category category = Category.builder()
        .id(1L)
        .name("Test")
        .places(Collections.emptyList())
        .build();
    private final CategoryDtoResponse categoryDtoResponse = CategoryDtoResponse.builder()
        .id(1L)
        .name("Test")
        .build();

    private final CategoryDto categoryDto = CategoryDto.builder()
        .name("Test")
        .build();

    @Test
    void saveDtoTest() {
        Category parentCategory = Category.builder().id(2L)
            .name("parent")
            .build();
        categoryDto.setParentCategoryId(2L);
        when(modelMapper.map(categoryDto, Category.class)).thenReturn(category);
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(parentCategory));
        when(modelMapper.map(parentCategory, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);
        when(modelMapper.map(categoryDtoResponse, Category.class)).thenReturn(parentCategory);
        when(categoryRepo.save(category)).thenReturn(category);

        when(modelMapper.map(category, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);
        assertEquals(categoryDtoResponse, categoryService.save(categoryDto));
    }

    @Test
    void saveDtoTestException() {
        Category parentCategory = Category.builder().id(2L)
            .name("parent")
            .parentCategory(category)
            .build();
        categoryDto.setParentCategoryId(2L);
        when(modelMapper.map(categoryDto, Category.class)).thenReturn(category);
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(parentCategory));
        when(modelMapper.map(parentCategory, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);
        when(modelMapper.map(categoryDtoResponse, Category.class)).thenReturn(parentCategory);
        assertThrows(BadRequestException.class,
            () -> categoryService.save(categoryDto));
    }

    @Test
    void saveDtoWhenFindByNameTrueTest() {
        when(categoryRepo.findByName(any())).thenReturn(category);
        Assertions
            .assertThrows(BadCategoryRequestException.class,
                () -> categoryService.save(categoryDto));
    }

    @Test
    void findByNameTest() {
        when(categoryRepo.findByName("Test")).thenReturn(category);
        when(modelMapper.map(category, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);

        CategoryDtoResponse foundEntity = categoryService.findByName("Test");
        assertEquals(categoryDtoResponse, foundEntity);
    }

    @Test
    void findByNameWhenCategoryNullTest() {
        when(categoryRepo.findByName(anyString())).thenReturn(null);
        Assertions
            .assertThrows(NotFoundException.class,
                () -> categoryService.findByName("test"));
    }

    @Test
    void findAllCategoryDtoTest() {
        List<Category> genericEntityList = Arrays.asList(
            Category.builder()
                .name("Test")
                .build(),
            Category.builder()
                .name("Test1")
                .build());
        when(categoryRepo.findAll()).thenReturn(genericEntityList);
        List<CategoryDto> mappedList = genericEntityList
            .stream()
            .map(c -> modelMapper.map(c, CategoryDto.class))
            .collect(Collectors.toList());
        List<CategoryDto> allCategoryDto = categoryService.findAllCategoryDto();
        assertEquals(mappedList, allCategoryDto);
    }

    @Test
    void findByIdTest() {
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);

        CategoryDtoResponse foundEntity = categoryService.findById(1L);
        assertEquals(categoryDtoResponse, foundEntity);
    }

    @Test
    void findByIdGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> categoryService.findById(null));
    }

    @Test
    void updateTest() {
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);
        when(modelMapper.map(categoryDtoResponse, Category.class)).thenReturn(category);

        when(categoryRepo.save(category)).thenReturn(category);

        CategoryDtoResponse foundEntity = categoryService.update(1L, "newTest");
        assertEquals(categoryDtoResponse, foundEntity);
    }

    @Test
    void updateGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> categoryService.update(null, "newTest"));
    }

    @Test
    void deleteByIdTest() {
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(category));
        when(modelMapper.map(categoryDtoResponse, Category.class)).thenReturn(category);
        when(modelMapper.map(category, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);
        assertEquals(1L, categoryService.deleteById(1L));
    }

    @Test
    void deleteByIdGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> categoryService.deleteById(null));
    }

    @Test
    void deleteByIdGivenCategoryRelatedToExistencePlaceThrowException() {
        Category generatedEntity = Category.builder().places(Collections.singletonList(new Place())).build();
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(generatedEntity));
        when(modelMapper.map(generatedEntity, CategoryDtoResponse.class)).thenReturn(categoryDtoResponse);
        when(modelMapper.map(categoryDtoResponse, Category.class)).thenReturn(generatedEntity);
        Assertions
            .assertThrows(BadRequestException.class,
                () -> categoryService.deleteById(1L));
    }

    @Test
    void findAllTest() {
        List<Category> genericEntities =
            new ArrayList<>(Arrays.asList(new Category(), new Category()));
        List<CategoryDtoResponse> categoryDtoResponseList =
            new ArrayList<>(Arrays.asList(new CategoryDtoResponse(), new CategoryDtoResponse()));
        when(categoryRepo.findAll()).thenReturn(genericEntities);
        when(modelMapper.map(genericEntities, new TypeToken<List<CategoryDtoResponse>>() {
        }.getType())).thenReturn(categoryDtoResponseList);
        List<CategoryDtoResponse> foundEntities = categoryService.findAll();
        assertEquals(categoryDtoResponseList, foundEntities);
    }
}
