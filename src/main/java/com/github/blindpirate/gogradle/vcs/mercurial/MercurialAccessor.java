package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import org.tmatesoft.hg.core.HgChangeset;
import org.tmatesoft.hg.core.HgCheckoutCommand;
import org.tmatesoft.hg.core.HgCloneCommand;
import org.tmatesoft.hg.core.HgException;
import org.tmatesoft.hg.core.HgIOException;
import org.tmatesoft.hg.core.HgLibraryFailureException;
import org.tmatesoft.hg.core.HgLogCommand;
import org.tmatesoft.hg.core.HgPullCommand;
import org.tmatesoft.hg.core.HgRemoteConnectionException;
import org.tmatesoft.hg.core.HgRepositoryNotFoundException;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.repo.HgLookup;
import org.tmatesoft.hg.repo.HgRemoteRepository;
import org.tmatesoft.hg.repo.HgRepository;
import org.tmatesoft.hg.repo.HgTags;
import org.tmatesoft.hg.util.CancelledException;

import javax.inject.Singleton;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Singleton
public class MercurialAccessor implements VcsAccessor {
    private HgLookup hgLookup = new HgLookup();

    @Override
    public String getRemoteUrl(File repoRoot) {
        HgRepository repository = getRepository(repoRoot);
        return getRemoteUrl(repository);
    }

    public String getRemoteUrl(HgRepository repository) {
        return repository.getConfiguration().getStringValue("paths", "default", null);
    }

    public HgRepository getRepository(File repoRoot) {
        try {
            return new HgLookup().detect(repoRoot);
        } catch (HgRepositoryNotFoundException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public long getLastCommitTimeOfPath(HgRepository repository, String relativePath) {
        try {
            List<HgChangeset> changesets = new HgLogCommand(repository).file(relativePath, false).execute();
            return changesets.get(0).getDate().getRawTime();
        } catch (HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public Optional<HgChangeset> findChangesetByTag(HgRepository repository, String tag) {
        HgTags tags = repository.getTags();
        HgTags.TagInfo tagInfo = tags.getAllTags().get(tag);
        if (tagInfo == null) {
            return Optional.empty();
        } else {
            return findChangesetByNodeId(repository, tagInfo.revision());
        }
    }

    public Optional<HgChangeset> findChangesetByNodeId(HgRepository repository, String nodeId) {
        return findChangesetByNodeId(repository, Nodeid.fromAscii(nodeId));
    }

    public Optional<HgChangeset> findChangesetByNodeId(HgRepository repository, Nodeid nodeId) {
        HgLogCommand logCommand = new HgLogCommand(repository);
        try {
            List<HgChangeset> changesets = logCommand.changeset(nodeId).execute();
            if (changesets.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(changesets.get(0));
            }
        } catch (HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }

    }

    public HgChangeset headOfBranch(HgRepository repository, String defaultBranch) {
        HgLogCommand logCommand = new HgLogCommand(repository).branch(defaultBranch);
        try {
            List<HgChangeset> changesets = logCommand.execute();
            return changesets.get(changesets.size() - 1);
        } catch (HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public void pull(HgRepository repository) {
        HgPullCommand cmd = new HgPullCommand(repository);
        try {
            cmd.execute();
        } catch (HgRemoteConnectionException | HgIOException | CancelledException | HgLibraryFailureException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public HgRepository cloneWithUrl(File dir, String url) {
        try {
            HgRemoteRepository remoteRepository = hgLookup.detectRemote(new URI(url));
            return new HgCloneCommand().source(remoteRepository).destination(dir).execute();
        } catch (URISyntaxException | CancelledException | HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public void resetToSpecificNodeId(HgRepository repository, String nodeId) {
        HgCheckoutCommand cmd = new HgCheckoutCommand(repository);
        try {
            cmd.changeset(Nodeid.fromAscii(nodeId));
            cmd.execute();
        } catch (CancelledException | HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }


}
