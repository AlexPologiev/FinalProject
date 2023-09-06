package searchengine.converter;

import searchengine.model.Status;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status,String> {
    @Override
    public String convertToDatabaseColumn(Status status) {
        return status.toString().toLowerCase();
    }

    @Override
    public Status convertToEntityAttribute(String s) {
        return Status.valueOf(s.toUpperCase());
    }
}
