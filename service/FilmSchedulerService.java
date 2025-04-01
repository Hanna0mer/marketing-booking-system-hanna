package marketing.service;

import marketing.model.Film;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FilmSchedulerService {
    private final List<Film> scheduledFilms = new ArrayList<>();

    public boolean scheduleFilm(Film film) {
        if (isSlotAvailable(film.getScreeningTime())) {
            scheduledFilms.add(film);
            System.out.println("Film scheduled: " + film.getTitle() + " at " + film.getScreeningTime());
            // Simulate notifying the Box Office
            return true;
        }
        return false;
    }

    public boolean isSlotAvailable(LocalDateTime screeningTime) {
        for (Film f : scheduledFilms) {
            if (f.getScreeningTime().equals(screeningTime)) {
                return false;
            }
        }
        return true;
    }

    public List<Film> getScheduledFilms(LocalDateTime from, LocalDateTime to) {
        List<Film> result = new ArrayList<>();
        for (Film film : scheduledFilms) {
            if (!film.getScreeningTime().isBefore(from) && !film.getScreeningTime().isAfter(to)) {
                result.add(film);
            }
        }
        return result;
    }
}
