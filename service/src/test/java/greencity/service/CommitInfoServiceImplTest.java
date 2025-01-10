package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.exception.exceptions.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommitInfoServiceImplTest {
    @InjectMocks
    private CommitInfoServiceImpl commitInfoService;

    @Mock
    private Repository repository;

    @Mock
    private RevCommit revCommit;

    @Mock
    private ObjectId objectId;

    @Mock
    private PersonIdent personIdent;

    private static final String COMMIT_HASH = "abc123";
    private static final String COMMIT_REF = "HEAD";
    private static final String GIT_PATH = ".git";
    private static final String REPOSITORY_FIELD = "repository";

    private void configureFileRepositoryBuilderMock(FileRepositoryBuilder builderMock) {
        when(builderMock.setGitDir(new File(GIT_PATH))).thenReturn(builderMock);
        when(builderMock.setMustExist(true)).thenReturn(builderMock);
        when(builderMock.readEnvironment()).thenReturn(builderMock);
        when(builderMock.findGitDir()).thenReturn(builderMock);
    }

    @Test
    void constructorInitializationSuccessTest() throws NoSuchFieldException, IllegalAccessException {
        try (MockedConstruction<FileRepositoryBuilder> ignored = mockConstruction(FileRepositoryBuilder.class,
            (builderMock, context) -> {
                configureFileRepositoryBuilderMock(builderMock);
                when(builderMock.build()).thenReturn(repository);
            })) {
            CommitInfoServiceImpl service = new CommitInfoServiceImpl();

            var repositoryField = CommitInfoServiceImpl.class.getDeclaredField(REPOSITORY_FIELD);
            repositoryField.setAccessible(true);
            Repository initializedRepository = (Repository) repositoryField.get(service);

            assertNotNull(initializedRepository);
        }
    }

    @Test
    void constructorInitializationFailureTest() throws NoSuchFieldException, IllegalAccessException {
        try (MockedConstruction<FileRepositoryBuilder> ignored = mockConstruction(FileRepositoryBuilder.class,
            (builderMock, context) -> {
                configureFileRepositoryBuilderMock(builderMock);
                when(builderMock.build()).thenThrow(new IOException());
            })) {
            CommitInfoServiceImpl service = new CommitInfoServiceImpl();

            var repositoryField = CommitInfoServiceImpl.class.getDeclaredField(REPOSITORY_FIELD);
            repositoryField.setAccessible(true);
            Repository initializedRepository = (Repository) repositoryField.get(service);

            assertNull(initializedRepository);
        }
    }

    @Test
    void getLatestCommitInfoWhenRepositoryNotInitializedThrowsNotFoundExceptionTest() {
        try (MockedConstruction<FileRepositoryBuilder> ignored = mockConstruction(FileRepositoryBuilder.class,
            (builderMock, context) -> {
                configureFileRepositoryBuilderMock(builderMock);
                when(builderMock.build()).thenThrow(new IOException());
            })) {
            CommitInfoServiceImpl service = new CommitInfoServiceImpl();
            ResourceNotFoundException notFoundException =
                assertThrows(ResourceNotFoundException.class, service::getLatestCommitInfo);

            assertEquals(ErrorMessage.GIT_REPOSITORY_NOT_INITIALIZED, notFoundException.getMessage());
        }
    }

    @Test
    void getLatestCommitInfoWithValidDataReturnsSuccessDtoTest() throws IOException {
        when(repository.resolve(COMMIT_REF)).thenReturn(objectId);
        when(revCommit.name()).thenReturn(COMMIT_HASH);
        when(revCommit.getAuthorIdent()).thenReturn(personIdent);

        Instant expectedDate = Instant.parse("2024-12-14T16:30:00Z");
        when(personIdent.getWhenAsInstant()).thenReturn(expectedDate);

        try (
            MockedConstruction<RevWalk> ignored = mockConstruction(RevWalk.class,
                (revWalkMock, context) -> when(revWalkMock.parseCommit(objectId)).thenReturn(revCommit))) {
            CommitInfoDto actualDto = commitInfoService.getLatestCommitInfo();

            assertEquals(COMMIT_HASH, actualDto.getCommitHash());

            String latestCommitDate = DateTimeFormatter.ofPattern(AppConstant.DATE_FORMAT)
                .withZone(ZoneId.of(AppConstant.UKRAINE_TIMEZONE))
                .format(expectedDate);
            assertEquals(latestCommitDate, actualDto.getCommitDate());
        }
    }

    @Test
    void getLatestCommitInfoWhenRepositoryResolveThrowsIOExceptionReturnsErrorDtoTest() throws IOException {
        String testExceptionMessage = "Test I/O exception";
        when(repository.resolve(COMMIT_REF)).thenThrow(new IOException(testExceptionMessage));

        ResourceNotFoundException notFoundException =
            assertThrows(ResourceNotFoundException.class, () -> commitInfoService.getLatestCommitInfo());

        assertEquals(ErrorMessage.FAILED_TO_FETCH_COMMIT_INFO + testExceptionMessage, notFoundException.getMessage());
    }

    @Test
    void getLatestCommitInfoWhenRevWalkParseCommitThrowsIOExceptionReturnsErrorDtoTest() throws IOException {
        String missingObjectMessage = "Missing object";
        when(repository.resolve(COMMIT_REF)).thenReturn(objectId);

        try (
            MockedConstruction<RevWalk> ignored = mockConstruction(RevWalk.class,
                (revWalkMock, context) -> when(revWalkMock.parseCommit(objectId)).thenThrow(
                    new IOException(missingObjectMessage)))) {
            ResourceNotFoundException notFoundException =
                assertThrows(ResourceNotFoundException.class, () -> commitInfoService.getLatestCommitInfo());

            assertEquals(ErrorMessage.FAILED_TO_FETCH_COMMIT_INFO + missingObjectMessage,
                notFoundException.getMessage());
        }
    }
}
