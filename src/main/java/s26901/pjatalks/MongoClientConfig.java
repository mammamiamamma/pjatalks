package s26901.pjatalks;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Collections;

@Configuration
public class MongoClientConfig extends AbstractMongoClientConfiguration {

    @Value(value = "${spring.data.mongodb.database}")
    private String database;
    @Value(value = "${spring.data.mongodb.uri}")
    private String connectionString;
    @Override
    protected String getDatabaseName() { return database; }
    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Collections.emptyList());
    }
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    @Bean
    public MongoClientSettings mongoClientSettings(){
        return MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionString)).build();
    }
}
