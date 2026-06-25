package com.aimr.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration",
    "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@EnableScheduling
@ConfigurationPropertiesScan
@EnableJpaRepositories(basePackages = "com.aimr.notify.infra.postgres.repo")
@EnableMongoRepositories(basePackages = "com.aimr.notify.infra.mongo.repo")
public class NotifyApplication {

	public static void main(String[] args) {
        Runtime runtime=Runtime.getRuntime();
        System.out.println("this runtime has "+runtime.availableProcessors()+" available processors");

		SpringApplication.run(NotifyApplication.class, args);

        /*
            building a generic filter functionality that will wiil work together with the common notification and template or any other repo entity

            @Class -> sortRequest(Collection<sortkey>, sortOrder)
            @class -> BaseSearchDTOt(pageSize, page, String sortRequest);

            @Methodology -> Build using repo criteria
         */
	}

}
