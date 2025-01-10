package greencity.service;

import greencity.dto.commitinfo.CommitInfoDto;

/**
 * Interface for fetching Git commit information.
 */
public interface CommitInfoService {
    /**
     * Fetches the latest Git commit hash and date.
     *
     * @return {@link CommitInfoDto}
     */
    CommitInfoDto getLatestCommitInfo();
}
