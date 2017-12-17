package guiando.billintegration.model;

public enum FileType {

    PDF("application/pdf");

    private final String fileType;

    FileType(String fileType){
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }
}
