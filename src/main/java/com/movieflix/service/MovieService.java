package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MovieService {
    MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException;
    MovieDto getMovie(Long id);
    List<MovieDto> getAllMovies();
    MovieDto updateMovie(Long movieId, MovieDto movieDto, MultipartFile file) throws IOException;
    String deleteMovie(Long movieId) throws IOException;
    MoviePageResponse getAllMoviesWithPagination (Integer pageNumber, Integer pageSize);
    MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir);
}
