package gent.d09.servicefactory.email.worker.module.email.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
public class Attachment {
    private Long id;
    private String name;
    private String content;
    private String contentType;
}
