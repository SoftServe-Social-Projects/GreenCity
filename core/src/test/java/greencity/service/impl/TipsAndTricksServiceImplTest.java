package greencity.service.impl;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.dto.PageableDto;
import greencity.dto.search.SearchTipsAndTricksDto;
import greencity.dto.tipsandtricks.TipsAndTricksDtoManagement;
import greencity.dto.tipsandtricks.TipsAndTricksDtoRequest;
import greencity.dto.tipsandtricks.TipsAndTricksDtoResponse;
import greencity.entity.Tag;
import greencity.entity.TipsAndTricks;
import greencity.entity.TipsAndTricksComment;
import greencity.entity.User;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.TipsAndTricksRepo;
import greencity.service.FileService;
import greencity.service.TagsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TipsAndTricksServiceImplTest {
    @Mock
    FileService fileService;
    @Mock
    private TipsAndTricksRepo tipsAndTricksRepo;
    @Mock
    private TagsService tagService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserServiceImpl userService;
    @InjectMocks
    private TipsAndTricksServiceImpl tipsAndTricksService;

    private TipsAndTricksDtoRequest tipsAndTricksDtoRequest = ModelUtils.getTipsAndTricksDtoRequest();
    private TipsAndTricks tipsAndTricks = ModelUtils.getTipsAndTricks();
    private TipsAndTricksDtoResponse tipsAndTricksDtoResponse = ModelUtils.getTipsAndTricksDtoResponse();
    private Tag tipsAndTricksTag = ModelUtils.getTag();
    private TipsAndTricksComment tipsAndTricksComment = ModelUtils.getTipsAndTricksComment();
    private User user = ModelUtils.getUser();

    @Test
    void saveTest() {
        when(modelMapper.map(tipsAndTricksDtoRequest, TipsAndTricks.class)).thenReturn(tipsAndTricks);
        when(userService.findByEmail(TestConst.EMAIL)).thenReturn(ModelUtils.getUser());
        when(tagService.findTipsAndTricksTagsByNames(anyList()))
            .thenReturn(Collections.singletonList(tipsAndTricksTag));
        when(modelMapper.map(tipsAndTricks, TipsAndTricksDtoResponse.class)).thenReturn(tipsAndTricksDtoResponse);

        TipsAndTricksDtoResponse actual =
            tipsAndTricksService.save(tipsAndTricksDtoRequest, null, ModelUtils.getUser().getEmail());

        assertEquals(tipsAndTricksDtoResponse, actual);
    }

    @Test
    void saveFailedTest() {
        String email = ModelUtils.getUser().getEmail();

        when(modelMapper.map(tipsAndTricksDtoRequest, TipsAndTricks.class)).thenReturn(tipsAndTricks);
        when(userService.findByEmail(TestConst.EMAIL)).thenReturn(ModelUtils.getUser());
        when(tagService.findTipsAndTricksTagsByNames(anyList()))
            .thenReturn(Collections.singletonList(tipsAndTricksTag));
        when(tipsAndTricksRepo.save(tipsAndTricks)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(NotSavedException.class, () ->
            tipsAndTricksService.save(tipsAndTricksDtoRequest, null, email));
    }

    @Test
    void saveUploadImageTest() throws IOException {
        MultipartFile image = ModelUtils.getFile();
        String imageToEncode = Base64.getEncoder().encodeToString(image.getBytes());
        tipsAndTricksDtoRequest.setImage(imageToEncode);

        when(modelMapper.map(tipsAndTricksDtoRequest, TipsAndTricks.class)).thenReturn(tipsAndTricks);
        when(userService.findByEmail(TestConst.EMAIL)).thenReturn(ModelUtils.getUser());
        when(modelMapper.map(tipsAndTricksDtoRequest.getImage(), MultipartFile.class)).thenReturn(image);
        when(fileService.upload(any(MultipartFile.class))).thenReturn(ModelUtils.getUrl());
        when(tagService.findTipsAndTricksTagsByNames(anyList()))
            .thenReturn(Collections.singletonList(tipsAndTricksTag));
        when(modelMapper.map(tipsAndTricks, TipsAndTricksDtoResponse.class)).thenReturn(tipsAndTricksDtoResponse);

        TipsAndTricksDtoResponse actual =
            tipsAndTricksService.save(tipsAndTricksDtoRequest, image, ModelUtils.getUser().getEmail());

        assertEquals(tipsAndTricksDtoResponse, actual);
    }

    @Test
    void findAllTest() {
        List<TipsAndTricks> tipsAndTricks = Collections.singletonList(ModelUtils.getTipsAndTricks());
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<TipsAndTricks> page = new PageImpl<>(tipsAndTricks, pageRequest, tipsAndTricks.size());
        List<TipsAndTricksDtoResponse> dtoList = Collections.singletonList(ModelUtils.getTipsAndTricksDtoResponse());
        PageableDto<TipsAndTricksDtoResponse> pageableDto = new PageableDto<>(dtoList, dtoList.size(), 0,1);

        when(tipsAndTricksRepo.findAllByOrderByCreationDateDesc(pageRequest)).thenReturn(page);
        when(modelMapper.map(tipsAndTricks.get(0), TipsAndTricksDtoResponse.class)).thenReturn(dtoList.get(0));

        PageableDto<TipsAndTricksDtoResponse> actual = tipsAndTricksService.findAll(pageRequest);

        assertEquals(pageableDto, actual);
    }

    @Test
    void findTest() {
        List<TipsAndTricks> tipsAndTricks = Collections.singletonList(ModelUtils.getTipsAndTricks());
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<TipsAndTricks> page = new PageImpl<>(tipsAndTricks, pageRequest, tipsAndTricks.size());
        List<TipsAndTricksDtoResponse> dtoList = Collections.singletonList(ModelUtils.getTipsAndTricksDtoResponse());
        PageableDto<TipsAndTricksDtoResponse> pageableDto = new PageableDto<>(dtoList, dtoList.size(), 0,1);

        when(modelMapper.map(tipsAndTricks.get(0), TipsAndTricksDtoResponse.class)).thenReturn(dtoList.get(0));
        when(tipsAndTricksRepo.find(pageRequest, Collections.singletonList(ModelUtils.getTag().getName())))
            .thenReturn(page);
        when((tipsAndTricksRepo.findAllByOrderByCreationDateDesc(pageRequest))).thenReturn(page);

        PageableDto<TipsAndTricksDtoResponse> actual =
            tipsAndTricksService.find(pageRequest, Collections.singletonList(ModelUtils.getTag().getName()));

        assertEquals(pageableDto, actual);
        assertEquals(pageableDto, tipsAndTricksService.find(pageRequest, null));
    }

    @Test
    void findDtoByIdTest() {
        TipsAndTricksDtoResponse tipsAndTricksDtoResponse = ModelUtils.getTipsAndTricksDtoResponse();

        when(tipsAndTricksRepo.findById(1L)).thenReturn(Optional.of(tipsAndTricks));
        when(modelMapper.map(tipsAndTricks, TipsAndTricksDtoResponse.class)).thenReturn(tipsAndTricksDtoResponse);

        TipsAndTricksDtoResponse actual = tipsAndTricksService.findDtoById(1L);

        assertEquals(tipsAndTricksDtoResponse, actual);
    }

    @Test
    void findDtoByIdFailedTest() {
        TipsAndTricksDtoResponse tipsAndTricksDtoResponse = ModelUtils.getTipsAndTricksDtoResponse();

        when(tipsAndTricksRepo.findById(1L)).thenReturn(Optional.empty());
        when(modelMapper.map(tipsAndTricks, TipsAndTricksDtoResponse.class)).thenReturn(tipsAndTricksDtoResponse);

        assertThrows(NotFoundException.class, () -> tipsAndTricksService.findDtoById(1L));
    }

    @Test
    void delete() {
        doNothing().when(tipsAndTricksRepo).deleteById(1L);
        when(tipsAndTricksRepo.findById(1L))
            .thenReturn(Optional.of(ModelUtils.getTipsAndTricks()));
        tipsAndTricksService.delete(1L);

        verify(tipsAndTricksRepo, times(1)).deleteById(1L);
    }

    @Test
    void search() {
        List<TipsAndTricks> tipsAndTricks = Collections.singletonList(ModelUtils.getTipsAndTricks());
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<TipsAndTricks> page = new PageImpl<>(tipsAndTricks, pageRequest, tipsAndTricks.size());
        List<SearchTipsAndTricksDto> dtoList = page.stream()
            .map(t -> modelMapper.map(t, SearchTipsAndTricksDto.class))
            .collect(Collectors.toList());
        PageableDto<SearchTipsAndTricksDto> pageableDto = new PageableDto<>(dtoList, dtoList.size(), 0,1);

        when(tipsAndTricksRepo.searchTipsAndTricks(pageRequest, tipsAndTricks.get(0).getTitle())).thenReturn(page);
        when(modelMapper.map(tipsAndTricks.get(0), SearchTipsAndTricksDto.class)).thenReturn(dtoList.get(0));

        PageableDto<SearchTipsAndTricksDto> actual = tipsAndTricksService.search(tipsAndTricks.get(0).getTitle());

        assertEquals(pageableDto, actual);
    }

    @Test
    void update() {
        List<String> tags = Collections.singletonList("test");
        TipsAndTricksDtoManagement tipsAndTricksDtoManagement  = TipsAndTricksDtoManagement.builder()
                .id(1L)
                .title("test")
                .text("test")
                .tags(tags)
                .emailAuthor("orest@gmail.com")
                .build();

        TipsAndTricks inital = tipsAndTricks;
        tipsAndTricks.setTitle("test");
        tipsAndTricks.setText("test");
        tipsAndTricks.setTags(Collections.singletonList(tipsAndTricksTag));
        tipsAndTricks.setAuthor(user);

        when(tipsAndTricksRepo.findById(1L)).thenReturn(Optional.of(tipsAndTricks));
        when(tagService.findTipsAndTricksTagsByNames(tipsAndTricksDtoManagement.getTags())).thenReturn(Collections.singletonList(tipsAndTricksTag));
        when(userService.findByEmail(tipsAndTricksDtoManagement.getEmailAuthor())).thenReturn(user);
        tipsAndTricksService.update(tipsAndTricksDtoManagement, null);
        verify(tipsAndTricksRepo).save(tipsAndTricks);


    }

    @Test
    void deleteAll() {
        List<Long> listId = Collections.singletonList(1L);
        when(tipsAndTricksRepo.findById(1L)).thenReturn(Optional.of(tipsAndTricks));
        tipsAndTricksService.deleteAll(listId);
        verify(tipsAndTricksRepo, times(1)).deleteById(1L);
    }

    @Test
    void findManagementDtoById() {
        Long id = 1L;
        List<String> tags = Collections.singletonList("test");
        TipsAndTricksDtoManagement tipsAndTricksDtoManagement  = TipsAndTricksDtoManagement.builder()
                .id(id)
                .title("test")
                .text("test")
                .tags(tags)
                .emailAuthor("orest@gmail.com")
                .build();
        when(tipsAndTricksRepo.findById(1L)).thenReturn(Optional.of(tipsAndTricks));
        when(modelMapper.map(tipsAndTricks, TipsAndTricksDtoManagement.class)).thenReturn(tipsAndTricksDtoManagement);
        assertEquals(tipsAndTricksDtoManagement, tipsAndTricksService.findManagementDtoById(id));
    }

    @Test
    void testSearch() {
        List<TipsAndTricks> tipsAndTricks = Collections.singletonList(ModelUtils.getTipsAndTricks());
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<TipsAndTricks> page = new PageImpl<>(tipsAndTricks, pageRequest, tipsAndTricks.size());
        List<SearchTipsAndTricksDto> dtoList = page.stream()
                .map(t -> modelMapper.map(t, SearchTipsAndTricksDto.class))
                .collect(Collectors.toList());
        PageableDto<SearchTipsAndTricksDto> pageableDto = new PageableDto<>(dtoList, dtoList.size(), 0,1);

        when(tipsAndTricksRepo.searchTipsAndTricks(pageRequest, tipsAndTricks.get(0).getTitle())).thenReturn(page);
        when(modelMapper.map(tipsAndTricks.get(0), SearchTipsAndTricksDto.class)).thenReturn(dtoList.get(0));

        PageableDto<SearchTipsAndTricksDto> actual = tipsAndTricksService.search(pageRequest, tipsAndTricks.get(0).getTitle());

        assertEquals(pageableDto, actual);
    }

    @Test
    void searchBy() {
        List<TipsAndTricks> tipsAndTricks = Collections.singletonList(ModelUtils.getTipsAndTricks());
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<TipsAndTricks> page = new PageImpl<>(tipsAndTricks, pageRequest, tipsAndTricks.size());
        List<TipsAndTricksDtoResponse> tipsAndTricksDtoResponses = page.stream()
                .map(t -> modelMapper.map(t, TipsAndTricksDtoResponse.class))
                .collect(Collectors.toList());
        PageableDto<TipsAndTricksDtoResponse> tipsAndTricksDtoResponsePageableDto = new PageableDto<>(tipsAndTricksDtoResponses,
                page.getTotalElements(),
                page.getPageable().getPageNumber(),
                page.getTotalPages());


        when(tipsAndTricksRepo.searchBy(pageRequest, tipsAndTricks.get(0).getTitle())).thenReturn(page);

        assertEquals(tipsAndTricksDtoResponsePageableDto, tipsAndTricksService.searchBy(pageRequest, tipsAndTricks.get(0).getTitle()));
    }

    @Test
    void getAmountOfWrittenTipsAndTrickByUserId() {
        Long id = 1L;
        when(tipsAndTricksRepo.getAmountOfWrittenTipsAndTrickByUserId(id)).thenReturn(1L);
        assertEquals(id, tipsAndTricksService.getAmountOfWrittenTipsAndTrickByUserId(id));
    }

    @Test
    void likeComment() {
        TipsAndTricksComment initial = tipsAndTricksComment;
        Set<User> userSet = new HashSet<>();
        userSet.add(user);
        initial.setUsersLiked(userSet);
        tipsAndTricksService.likeComment(user, tipsAndTricksComment);
        assertEquals(initial, tipsAndTricksComment);
    }

    @Test
    void unlikeComment() {
        TipsAndTricksComment initial = tipsAndTricksComment;
        Set<User> userSet = new HashSet<>();
        userSet.add(user);
        tipsAndTricksComment.setUsersLiked(userSet);
        tipsAndTricksService.unlikeComment(user, tipsAndTricksComment);
        assertEquals(initial, tipsAndTricksComment);

    }

}
