package com.github.blindpirate.gogradle.vcs.git

import com.github.blindpirate.gogradle.util.Assert
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.eclipse.jgit.revwalk.RevCommit
import org.junit.Test

class RevCommitUtils {
    static RevCommit aCommit() {
        return of('123456abcd' * 4)
    }

    static RevCommit of(String sha1) {
        RevCommit ret = RevCommit.parse([48] * 64 as byte[])
        Assert.isTrue(sha1.size() == 40)
        byte[] bytes = sha1.decodeHex()
        ReflectionUtils.setField(ret, 'w1', toInt(bytes[0..3]))
        ReflectionUtils.setField(ret, 'w2', toInt(bytes[4..7]))
        ReflectionUtils.setField(ret, 'w3', toInt(bytes[8..11]))
        ReflectionUtils.setField(ret, 'w4', toInt(bytes[12..15]))
        ReflectionUtils.setField(ret, 'w5', toInt(bytes[16..19]))
        return ret
    }

    static int toInt(List<Byte> bytes) {
        int x = bytes[0] << 24
        return ((bytes[0] << 24) & 0xff000000) |
                ((bytes[1] << 16) & 0xff0000) |
                ((bytes[2] << 8) & 0xff00) |
                (bytes[3] & 0xff)
    }

    @Test
    void simpleTest() {
        assert of('1234abcdef' * 4).name == '1234abcdef' * 4
    }

}
