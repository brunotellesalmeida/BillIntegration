package guiando.billintegration.transform;

import guiando.billintegration.model.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileTypeTransformer {
    @Transformer
    public MessageProcessor<String> toFileTypeJava(){
        return (Message<?> message) -> {
            try {
                return Files.probeContentType((Paths.get(message.getPayload().toString())))  ;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return FileType.DEFAULT.getFileType();
        };
    }

    public MessageProcessor<String> toFileTypeApacheTika(){
        Tika tika = new Tika();
        return (Message<?> message) -> tika.detect(message.getPayload().toString());
    }
}
