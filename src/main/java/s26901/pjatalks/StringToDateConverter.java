package s26901.pjatalks;

import com.mongodb.lang.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class StringToDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(@NonNull String source) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
