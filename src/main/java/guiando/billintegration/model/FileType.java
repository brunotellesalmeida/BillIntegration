package guiando.billintegration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    PDF("application/pdf"),
    XML("xml"),
    TXT("text/plain"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    DEFAULT("application/pdf");

    private final String fileType;
}
