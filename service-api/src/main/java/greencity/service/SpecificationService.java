package greencity.service;

import greencity.dto.specification.SpecificationNameDto;
import greencity.dto.specification.SpecificationVO;
import java.util.List;

public interface SpecificationService {
    /**
     * Save SpecificationVO to DB.
     *
     * @param specificationVO - of Specification.
     * @return saved {@link SpecificationVO }.
     */
    SpecificationVO save(SpecificationVO specificationVO);

    /**
     * Method for finding SpecificationVO by id.
     *
     * @param id - specificationVO's id.
     * @return a specificationVO.
     */
    SpecificationVO findById(Long id);

    /**
     * Method for finding all SpecificationVOs.
     *
     * @return list of SpecificationVOs.
     */
    List<SpecificationVO> findAll();

    /**
     * Method for deleting SpecificationVO by id.
     *
     * @param id - specificationVO's id.
     * @return id of deleted SpecificationVO.
     */
    Long deleteById(Long id);

    /**
     * Finds specificationVO by name.
     *
     * @param name to find by.
     * @return a specificationVO by name.
     */
    SpecificationVO findByName(String name);

    /**
     * Method for finding all SpecificationNameDto.
     *
     * @return list of SpecificationNameDto.
     */
    List<SpecificationNameDto> findAllSpecificationDto();
}
