package greencity.service;

import greencity.constant.AppConstant;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.dto.commitinfo.CommitInfoErrorDto;
import greencity.dto.commitinfo.CommitInfoSuccessDto;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for fetching Git commit information.
 */
@Service
public class CommitInfoServiceImpl implements CommitInfoService {
    private Repository repository;

    private static final String COMMIT_REF = "HEAD";

    private static final Logger log = LoggerFactory.getLogger(CommitInfoServiceImpl.class);

    public CommitInfoServiceImpl() {
        try {
            repository = new FileRepositoryBuilder()
                .setGitDir(new File(".git"))
                .readEnvironment()
                .findGitDir()
                .build();
        } catch (IOException e) {
            repository = null;
            log.warn("WARNING: .git directory not found. Git commit info will be unavailable.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommitInfoDto getLatestCommitInfo() {
        if (repository == null) {
            return new CommitInfoErrorDto("Git repository not initialized. Commit info is unavailable.");
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit latestCommit = revWalk.parseCommit(repository.resolve(COMMIT_REF));
            String latestCommitHash = latestCommit.name();
            String latestCommitDate = DateTimeFormatter.ofPattern(AppConstant.DATE_FORMAT)
                .withZone(ZoneId.of(AppConstant.UKRAINE_TIMEZONE))
                .format(latestCommit.getAuthorIdent().getWhenAsInstant());

            return new CommitInfoSuccessDto(latestCommitHash, latestCommitDate);
        } catch (IOException e) {
            return new CommitInfoErrorDto("Failed to fetch commit info due to I/O error: " + e.getMessage());
        }
    }
}
