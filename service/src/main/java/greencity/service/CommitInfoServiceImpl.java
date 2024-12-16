package greencity.service;

import greencity.constant.AppConstant;
import greencity.dto.commitinfo.CommitInfoDto;
import greencity.dto.commitinfo.CommitInfoErrorDto;
import greencity.dto.commitinfo.CommitInfoSuccessDto;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

/**
 * Service implementation for fetching Git commit information.
 */
@Service
public class CommitInfoServiceImpl implements CommitInfoService {
    private Repository repository;

    @PostConstruct
    private void init() {
        try {
            repository = new FileRepositoryBuilder()
                .setGitDir(new File(".git"))
                .readEnvironment()
                .findGitDir()
                .build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize repository", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommitInfoDto getLatestCommitInfo() {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit latestCommit = revWalk.parseCommit(repository.resolve("HEAD"));
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
