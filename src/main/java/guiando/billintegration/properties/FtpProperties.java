package guiando.billintegration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpProperties {
    @NotNull(message = "'ftp.protocol' must be set in application.yml" )
    private String protocol;

    @NotNull(message = "'ftp.hostname' must be set in application.yml")
    private String hostname;

    @NotNull(message = "'ftp.port' must be set in application.yml")
    private Integer port;

    @NotNull(message = "'ftp.username' must be set in application.yml")
    private String username;

    @NotNull(message = "'ftp.password' must be set in application.yml")
    private String password;

    @NotNull(message = "'ftp.folder' must be set in application.yml")
    private String folder;

    @NotNull(message = "'ftp.filePrefix' must be set in application.yml")
    private String filePrefix;

    @NotNull(message = "'ftp.regionFilePrefixes' must be set in application.yml")
    private List<String> typeFilePrefixes;
}
