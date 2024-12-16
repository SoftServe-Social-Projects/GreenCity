package greencity.service;

import greencity.constant.AppConstant;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.dto.commitinfo.CommitInfoErrorDto;
import greencity.dto.commitinfo.CommitInfoSuccessDto;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void postConstructInitSuccessTest() throws Exception {
        try (MockedConstruction<FileRepositoryBuilder> ignored = mockConstruction(FileRepositoryBuilder.class,
            (builderMock, context) -> {
                when(builderMock.setGitDir(new File(".git"))).thenReturn(builderMock);
                when(builderMock.readEnvironment()).thenReturn(builderMock);
                when(builderMock.findGitDir()).thenReturn(builderMock);
                when(builderMock.build()).thenReturn(repository);
            })) {

            var initMethod = CommitInfoServiceImpl.class.getDeclaredMethod("init");
            initMethod.setAccessible(true);

            assertDoesNotThrow(() -> initMethod.invoke(commitInfoService));

            var repositoryField = CommitInfoServiceImpl.class.getDeclaredField("repository");
            repositoryField.setAccessible(true);
            Repository initializedRepository = (Repository) repositoryField.get(commitInfoService);

            assertNotNull(initializedRepository);
        }
    }

    @Test
    void postConstructInitFailureThrowsExceptionTest() {
        try (MockedConstruction<FileRepositoryBuilder> ignored = mockConstruction(FileRepositoryBuilder.class,
            (builderMock, context) -> {
                when(builderMock.setGitDir(new File(".git"))).thenReturn(builderMock);
                when(builderMock.readEnvironment()).thenReturn(builderMock);
                when(builderMock.findGitDir()).thenReturn(builderMock);
                when(builderMock.build()).thenThrow(new IOException("Repository not found"));
            })) {
            InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
                var initMethod = CommitInfoServiceImpl.class.getDeclaredMethod("init");
                initMethod.setAccessible(true);
                initMethod.invoke(commitInfoService);
            });

            Throwable cause = exception.getCause();
            assertInstanceOf(IllegalStateException.class, cause);
            assertEquals("Failed to initialize repository", cause.getMessage());
        }
    }

    @Test
    void getLatestCommitInfoWithValidDataReturnsSuccessDtoTest() throws IOException {
        when(repository.resolve("HEAD")).thenReturn(objectId);
        when(revCommit.name()).thenReturn(COMMIT_HASH);
        when(revCommit.getAuthorIdent()).thenReturn(personIdent);

        Instant expectedDate = Instant.parse("2024-12-14T16:30:00Z");
        when(personIdent.getWhenAsInstant()).thenReturn(expectedDate);

        try (
            MockedConstruction<RevWalk> ignored = mockConstruction(RevWalk.class,
                (revWalkMock, context) -> when(revWalkMock.parseCommit(objectId)).thenReturn(revCommit))) {
            CommitInfoDto actualDto = commitInfoService.getLatestCommitInfo();

            assertInstanceOf(CommitInfoSuccessDto.class, actualDto);
            CommitInfoSuccessDto successDto = (CommitInfoSuccessDto) actualDto;
            assertEquals(COMMIT_HASH, successDto.getCommitHash());

            String latestCommitDate = DateTimeFormatter.ofPattern(AppConstant.DATE_FORMAT)
                .withZone(ZoneId.of(AppConstant.UKRAINE_TIMEZONE))
                .format(expectedDate);
            assertEquals(latestCommitDate, successDto.getCommitDate());
        }
    }

    @Test
    void getLatestCommitInfoWhenRepositoryResolveThrowsIOExceptionReturnsErrorDtoTest() throws IOException {
        when(repository.resolve("HEAD")).thenThrow(new IOException("Test I/O exception"));

        CommitInfoDto actualDto = commitInfoService.getLatestCommitInfo();
        assertInstanceOf(CommitInfoErrorDto.class, actualDto);

        CommitInfoErrorDto errorDto = (CommitInfoErrorDto) actualDto;
        assertEquals("Failed to fetch commit info due to I/O error: Test I/O exception", errorDto.getError());
    }

    @Test
    void getLatestCommitInfoWhenRevWalkParseCommitThrowsIOExceptionReturnsErrorDtoTest() throws IOException {
        when(repository.resolve("HEAD")).thenReturn(objectId);

        try (
            MockedConstruction<RevWalk> ignored = mockConstruction(RevWalk.class,
                (revWalkMock, context) -> when(revWalkMock.parseCommit(objectId)).thenThrow(
                    new IOException("Missing object")))) {
            CommitInfoDto actualDto = commitInfoService.getLatestCommitInfo();
            assertInstanceOf(CommitInfoErrorDto.class, actualDto);

            CommitInfoErrorDto errorDto = (CommitInfoErrorDto) actualDto;
            assertEquals("Failed to fetch commit info due to I/O error: Missing object", errorDto.getError());
        }
    }
}
