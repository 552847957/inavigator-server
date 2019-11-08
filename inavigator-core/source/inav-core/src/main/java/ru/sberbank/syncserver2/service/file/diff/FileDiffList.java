package ru.sberbank.syncserver2.service.file.diff;

import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 02.07.14.
 */
@XmlRootElement(name = "file-diff-list",namespace = "")
public class FileDiffList {
    private List<FileDiff> diffs;

    public FileDiffList() {
        diffs = new ArrayList<FileDiff>();
    }

    public FileDiffList(List<FileDiff> diffs) {
        this.diffs = diffs;
    }

    @XmlElement(name = "file-diff")
    public List<FileDiff> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<FileDiff> diffs) {
        this.diffs = diffs;
    }

    @Override
    public String toString() {
        return "FileDiffList{" +
                "diffs=" + diffs +
                '}';
    }
}
