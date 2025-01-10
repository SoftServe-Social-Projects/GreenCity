package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.constant.LogMessage;
import greencity.dto.category.CategoryDto;
import greencity.dto.category.CategoryDtoResponse;
import greencity.entity.Category;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.CategoryRepo;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Category entity.
 *
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepo categoryRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryDtoResponse save(CategoryDto dto) {
        log.info(LogMessage.IN_SAVE);

        Category category = categoryRepo.findByName(dto.getName());

        if (category != null) {
            throw new BadCategoryRequestException(
                ErrorMessage.CATEGORY_ALREADY_EXISTS_BY_THIS_NAME);
        }
        Category categoryToSave = modelMapper.map(dto, Category.class);
        if (dto.getParentCategoryId() != 0) {
            Category parentCategory =
                modelMapper.map(findById(dto.getParentCategoryId()), Category.class);
            if (parentCategory.getParentCategory() == null) {
                categoryToSave.setParentCategory(parentCategory);
            } else {
                throw new BadRequestException(ErrorMessage.CANNOT_ADD_PARENT_CATEGORY);
            }
        }

        return modelMapper.map(categoryRepo.save(categoryToSave), CategoryDtoResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CategoryDtoResponse> findAll() {
        log.info(LogMessage.IN_FIND_ALL);

        return modelMapper.map(categoryRepo.findAll(), new TypeToken<List<CategoryDtoResponse>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryDtoResponse findById(Long id) {
        log.info(LogMessage.IN_FIND_BY_ID, id);

        return modelMapper.map(categoryRepo
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException(
                    ErrorMessage.CATEGORY_NOT_FOUND_BY_ID + id)),
            CategoryDtoResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryDtoResponse update(Long id, String name) {
        Category category = modelMapper.map(findById(id), Category.class);
        category.setName(name);
        return modelMapper.map(categoryRepo.save(category), CategoryDtoResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long deleteById(Long id) {
        log.info(LogMessage.IN_DELETE_BY_ID, id);
        Category category = modelMapper.map(findById(id), Category.class);

        if (!category.getPlaces().isEmpty()) {
            throw new BadRequestException(ErrorMessage.NOT_SAVE_DELETION);
        }

        categoryRepo.delete(category);
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryDtoResponse findByName(String name) {
        Category category = categoryRepo.findByName(name);
        if (category == null) {
            throw new NotFoundException(ErrorMessage.CATEGORY_NOT_FOUND_BY_NAME + name);
        }
        return modelMapper.map(category, CategoryDtoResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CategoryDto> findAllCategoryDto() {
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
            .map(category -> modelMapper.map(category, CategoryDto.class))
            .collect(Collectors.toList());
    }
}
