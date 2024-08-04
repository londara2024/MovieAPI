package com.movieflix.entities;

import com.movieflix.dto.MovieDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Please provide movie's title!")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's director!")
    private String director;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's studio!")
    private String studio;

    @ElementCollection
    @CollectionTable(name = "movie_cast")
    private Set<String> moviesCast;

    @Column(nullable = false)
    @NotNull(message = "Please provide movie's release year!")
    private Integer releaseYear;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's poster!")
    private String poster;

    public Movie(MovieDto movieDto) {
        this.title = movieDto.getTitle();
        this.director = movieDto.getDirector();
        this.studio = movieDto.getStudio();
        this.moviesCast = movieDto.getMoviesCast();
        this.releaseYear = movieDto.getReleaseYear();
        this.poster = movieDto.getPoster();
    }
}
