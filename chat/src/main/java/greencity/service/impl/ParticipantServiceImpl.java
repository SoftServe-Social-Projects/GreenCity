package greencity.service.impl;

import greencity.constant.ErrorMessage;
import greencity.dto.ParticipantDto;
import greencity.entity.Participant;
import greencity.exception.exceptions.UserNotFoundException;
import greencity.repository.ParticipantRepo;
import greencity.service.ParticipantService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {
    private final ParticipantRepo participantRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant findByEmail(String email) {
        return participantRepo.findNotDeactivatedByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + email));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Participant findById(Long id) {
        return participantRepo.findById(id)
            .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + id));
    }

    /**
     * {@inheritDoc}
     */
    public List<ParticipantDto> findAll() {
        return modelMapper.map(participantRepo.findAll(), new TypeToken<List<ParticipantDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParticipantDto getCurrentParticipantByEmail(String email) {
        return modelMapper.map(
            participantRepo.findNotDeactivatedByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL + email)),
            ParticipantDto.class);
    }
}
