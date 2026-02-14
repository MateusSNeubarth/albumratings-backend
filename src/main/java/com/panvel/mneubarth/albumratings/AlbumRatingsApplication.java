package com.panvel.mneubarth.albumratings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class AlbumRatingsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlbumRatingsApplication.class, args);
	}

}
