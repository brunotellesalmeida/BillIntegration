package guiando.billintegration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "billintegration")
public class BillIntegrationProperties {

    @NotNull(message = "'billintegration.startUrl' must be set in application.yml")
    private String startUrl;

    @NotNull(message = "'billintegration.cronExpression' must be set in application.yml")
    private String cronExpression;
}
