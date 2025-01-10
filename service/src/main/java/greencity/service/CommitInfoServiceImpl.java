package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.exception.exceptions.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

/**
 * Service implementation for fetching Git commit information.
 */
@Service
@Slf4j
public class CommitInfoServiceImpl implements CommitInfoService {
    private Repository repository;

    private static final String COMMIT_REF = "HEAD";

    public CommitInfoServiceImpl() {
        try {
            repository = new FileRepositoryBuilder()
                .setGitDir(new File(".git"))
                .setMustExist(true)
                .readEnvironment()
                .findGitDir()
                .build();
        } catch (IOException e) {
            repository = null;
            log.warn(ErrorMessage.WARNING_GIT_DIRECTORY_NOT_FOUND);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the Git repository cannot be found, is
     *                                   not initialized, or commit information
     *                                   cannot be fetched due to an I/O error.
     */
    @Override
    public CommitInfoDto getLatestCommitInfo() {
        if (repository == null) {
            throw new ResourceNotFoundException(ErrorMessage.GIT_REPOSITORY_NOT_INITIALIZED);
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit latestCommit = revWalk.parseCommit(repository.resolve(COMMIT_REF));
            String latestCommitHash = latestCommit.name();
            String latestCommitDate = DateTimeFormatter.ofPattern(AppConstant.DATE_FORMAT)
                .withZone(ZoneId.of(AppConstant.UKRAINE_TIMEZONE))
                .format(latestCommit.getAuthorIdent().getWhenAsInstant());

            return new CommitInfoDto(latestCommitHash, latestCommitDate);
        } catch (IOException e) {
            throw new ResourceNotFoundException(ErrorMessage.FAILED_TO_FETCH_COMMIT_INFO + e.getMessage());
        }
    }
}
