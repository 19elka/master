package task1.generator;

import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import task1.entity.Genre;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Component
public class BookDataGenerator {
    private final Faker faker = new Faker(); // генерирует книги

    public String generateTitle() {
        return faker.book().title();
    }

    public String generateDescription() {
        return faker.lorem().paragraph(2);
    }

    public Genre generateGenre() {
        Genre[] genres = Genre.values();
        return genres[faker.random().nextInt(genres.length)];
    }

    public LocalDate generatePublishedDate() {
        return faker.date().past(3650, TimeUnit.DAYS)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public String generateFirstName() {
        return faker.name().firstName();
    }

    public String generateLastName() {
        return faker.name().lastName();
    }
}