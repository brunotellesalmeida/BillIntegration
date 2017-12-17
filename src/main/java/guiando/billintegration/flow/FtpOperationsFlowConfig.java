package guiando.billintegration.flow;

import guiando.billintegration.properties.FtpProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.FileInfo;
import org.springframework.integration.ftp.session.AbstractFtpSessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.GenericHandler;
import java.util.LinkedList;
import java.util.List;

@Setter @Getter
@Configuration
@RequiredArgsConstructor
public class FtpOperationsFlowConfig {
    private final FtpProperties ftpProperties;

    @Bean
    AbstractFtpSessionFactory<FTPClient> assembleFtpSessionFactory() {
        DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
        sf.setPort(ftpProperties.getPort());
        sf.setPassword(ftpProperties.getPassword());
        sf.setUsername(ftpProperties.getUsername());
        sf.setHost(ftpProperties.getHostname());
        sf.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        sf.setConfig(new FTPClientConfig(UnixFTPEntryParser.class.getTypeName()));

        return sf;
    }


    @Bean
    GenericHandler<Object> getLastFilePath() {
        return (p, h) -> {
            List<FileInfo> fileInfo = (List<FileInfo>) p;
            List<String> prefixes = ftpProperties.getTypeFilePrefixes();
            List<String> filePaths = new LinkedList<>();

            prefixes.forEach( pr -> {
                    fileInfo.stream()
                            .filter(f1 -> f1.getFilename().contains(pr))
                            .max((f1, f2) -> Long.compare( f1.getModified(), f2.getModified()))
                            .map(m -> String.format("%s%s", m.getRemoteDirectory(), m.getFilename()))
                            .ifPresent(path -> filePaths.add(path));
                }
            );
            return filePaths;
        };
    }

    @Bean
    GenericHandler<Object> getAllFilePath() {
        return (p, h) -> {
            List<FileInfo> fileInfo = (List<FileInfo>) p;
            List<String> prefixes = ftpProperties.getTypeFilePrefixes();
            List<String> filePaths = new LinkedList<>();

            prefixes.forEach( pr -> {
                fileInfo.stream()
                        .filter(f1 -> f1.getFilename().contains(pr))
                        .forEach(fi -> {
                            filePaths.add(String.format("%s%s", fi.getRemoteDirectory(), fi.getFilename()));
                        });
            });
            return filePaths;
        };
    }

}