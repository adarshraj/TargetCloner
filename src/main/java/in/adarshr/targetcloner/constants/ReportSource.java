package in.adarshr.targetcloner.constants;

import lombok.Getter;

@Getter
public enum ReportSource {
    //1 value for URL and 2 for File
    URL(1), FILE(2);

    private final int value;

    ReportSource(int value) {
        this.value = value;
    }
}
