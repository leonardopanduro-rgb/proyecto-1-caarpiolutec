package com.dbp.democarpultec.event;

import com.dbp.democarpultec.model.enums.Status;
import org.springframework.context.ApplicationEvent;

public class RequestStatusChangedEvent extends ApplicationEvent {
    private final Long requestId;
    private final String email;
    private final String name;
    private final String publicationTitle;
    private final Status status;

    public RequestStatusChangedEvent(
            Object source,
            Long requestId,
            String email,
            String name,
            String publicationTitle,
            Status status
    ) {
        super(source);
        this.requestId = requestId;
        this.email = email;
        this.name = name;
        this.publicationTitle = publicationTitle;
        this.status = status;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public Status getStatus() {
        return status;
    }
}
