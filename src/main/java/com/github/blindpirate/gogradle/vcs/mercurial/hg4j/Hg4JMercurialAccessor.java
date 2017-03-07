package com.github.blindpirate.gogradle.vcs.mercurial.hg4j;

import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.vcs.mercurial.HgChangeset;
import com.github.blindpirate.gogradle.vcs.mercurial.HgRepository;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialAccessor;
import org.tmatesoft.hg.core.HgCheckoutCommand;
import org.tmatesoft.hg.core.HgCloneCommand;
import org.tmatesoft.hg.core.HgException;
import org.tmatesoft.hg.core.HgLogCommand;
import org.tmatesoft.hg.core.HgRepositoryNotFoundException;
import org.tmatesoft.hg.core.Nodeid;
import org.tmatesoft.hg.repo.HgLookup;
import org.tmatesoft.hg.repo.HgRemoteRepository;
import org.tmatesoft.hg.repo.HgTags;
import org.tmatesoft.hg.util.CancelledException;

import javax.inject.Singleton;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Singleton
public class Hg4JMercurialAccessor implements MercurialAccessor {
    private HgLookup hgLookup = new HgLookup();

    @Override
    public String getRemoteUrl(File repoRoot) {
        HgRepository repository = getRepository(repoRoot);
        return getRemoteUrl(repository);
    }

    @Override
    public String getRemoteUrl(HgRepository repository) {
        return peel(repository).getConfiguration().getStringValue("paths", "default", null);
    }

    @Override
    public HgRepository getRepository(File repoRoot) {
        try {
            return Hg4JRepository.fromUnderlying(new HgLookup().detect(repoRoot));
        } catch (HgRepositoryNotFoundException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    @Override
    public long getLastCommitTimeOfPath(HgRepository repository, String relativePath) {
        try {
            List<org.tmatesoft.hg.core.HgChangeset> changesets = new HgLogCommand(peel(repository))
                    .file(relativePath, false)
                    .execute();
            return changesets.get(0).getDate().getRawTime();
        } catch (HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    @Override
    public Optional<HgChangeset> findChangesetByTag(HgRepository repository, String tag) {
        HgTags.TagInfo tagInfo = peel(repository).getTags().getAllTags().get(tag);
        if (tagInfo == null) {
            return Optional.empty();
        } else {
            return findChangesetByNodeId(repository, tagInfo.revision());
        }
    }

    @Override
    public Optional<HgChangeset> findChangesetById(HgRepository repository, String nodeId) {
        return findChangesetByNodeId(repository, Nodeid.fromAscii(nodeId));
    }

    private Optional<HgChangeset> findChangesetByNodeId(HgRepository repository, Nodeid nodeId) {
        HgLogCommand logCommand = new HgLogCommand(peel(repository));
        try {
            List<org.tmatesoft.hg.core.HgChangeset> changesets = logCommand.changeset(nodeId).execute();
            if (changesets.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(Hg4JChangeset.fromUnderlying(changesets.get(0)));
            }
        } catch (HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    private org.tmatesoft.hg.repo.HgRepository peel(HgRepository repository) {
        return Hg4JRepository.class.cast(repository).getUnderlying();
    }

    private HgChangeset wrap(org.tmatesoft.hg.core.HgChangeset changeset) {
        return Hg4JChangeset.fromUnderlying(changeset);
    }

    private HgRepository wrap(org.tmatesoft.hg.repo.HgRepository repository) {
        return Hg4JRepository.fromUnderlying(repository);
    }

    @Override
    public HgChangeset headOfBranch(HgRepository repository, String defaultBranch) {
        HgLogCommand logCommand = new HgLogCommand(peel(repository)).branch(defaultBranch);
        try {
            List<org.tmatesoft.hg.core.HgChangeset> changesets = logCommand.execute();
            return wrap(changesets.get(changesets.size() - 1));
        } catch (HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    // Walkaround: there's issues in Hg4J's pull functionality
    @Override
    public void pull(HgRepository repository) {
        String url = getRemoteUrl(repository);
        try {
            File dir = peel(repository).getWorkingDir();
            IOUtils.clearDirectory(dir);
            cloneWithUrl(dir, url);
//            HgRemoteRepository remote = hgLookup.detectRemote(url, peel(repository));
//            HgPullCommand cmd = new HgPullCommand(peel(repository)).source(remote);
//            cmd.execute();
        } catch (Exception e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public HgRepository cloneWithUrl(File dir, String url) {
        try {
            HgRemoteRepository remoteRepository = hgLookup.detectRemote(new URI(url));
            return wrap(new HgCloneCommand().source(remoteRepository).destination(dir).execute());
        } catch (URISyntaxException | CancelledException | HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    @Override
    public void resetToSpecificNodeId(HgRepository repository, String nodeId) {
        HgCheckoutCommand cmd = new HgCheckoutCommand(peel(repository));
        try {
            cmd.clean(true).changeset(Nodeid.fromAscii(nodeId));
            cmd.execute();
        } catch (CancelledException | HgException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }
}
