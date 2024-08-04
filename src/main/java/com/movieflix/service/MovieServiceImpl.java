package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.entities.Movie;
import com.movieflix.exceptions.FileExitsException;
import com.movieflix.exceptions.MovieNotFoundException;
import com.movieflix.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseURL;

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. upload the file
        if (Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExitsException("File already exists! Please enter another file name!");
        }
        String uploadedFileName = fileService.uploadFile(path, file);

        // 2. set the value of field 'poster' as filename
        movieDto.setPoster(uploadedFileName);

        // 3. map dto to Movie Object
        Movie movie = new Movie(movieDto);

        // 4. save the movie object -> saved Movie object
        Movie savedMovie = movieRepository.save(movie);

        // 5. generate the posterUrl
        String posterUrl = baseURL + "/file/" + uploadedFileName;

        // 6. map the saved Movie object to MovieDto and return it
        MovieDto movieDtoResponse = new MovieDto (
                movie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMoviesCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
        movieDtoResponse.setPosterUrl(posterUrl);

        return movieDtoResponse;

    }

    @Override
    public MovieDto getMovie(Long id) {
        // 1. check the data in DB and if exits, fetch the data of given ID
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found!!!"));

        // 2. generate postersUrl
        String posterUrl = baseURL + "/file/" + movie.getPoster();

        // 3. map to MovieDto object and return it
        MovieDto movieDto = new MovieDto (
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMoviesCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
        movieDto.setPosterUrl(posterUrl);

        return movieDto;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // 1. fetch all movies from DB
        List<Movie> movies = movieRepository.findAll();
        List<MovieDto> movieDtos = new ArrayList<MovieDto>();

        // 2. iterate through the list, generate posterUrl for each movie object and map to MovieDto object and return
        for (Movie movie : movies) {
            String posterUrl = baseURL + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto (
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMoviesCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Long movieId, MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. check if movie object exists with given movieId
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found!!!"));

        // 2. if file is null, do nothing if file is not null, then delete existing file associated with the record,
        // and upload the new file
        String fileName = movie.getPoster();
        if (file != null) {
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }

        // 3. set movieDto's poster value, according to step2
        movieDto.setPoster(fileName);

        // 4. map it to Movie object
        Movie mv = new Movie(movieDto);
        mv.setMovieId(movieId);

        // 5. save the movie object -> return saved movie object
        Movie updateMovie = movieRepository.save(mv);

        // 6. generate posterUrl
        String posterUrl = baseURL + "/file/" + fileName;
        movieDto.setPosterUrl(posterUrl);

        // 7. map the saved Movie object to MovieDto and return it
        return new MovieDto (
                updateMovie.getMovieId(),
                updateMovie.getTitle(),
                updateMovie.getDirector(),
                updateMovie.getStudio(),
                updateMovie.getMoviesCast(),
                updateMovie.getReleaseYear(),
                updateMovie.getPoster(),
                posterUrl
        );
    }

    @Override
    public String deleteMovie(Long movieId) throws IOException {
        // 1. check if movie object exists in DB
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found!!!"));

        // 2. delete the file associated with this object
        Files.deleteIfExists(Paths.get(path + File.separator + mv.getPoster()));

        // 3. delete the movie object
        movieRepository.deleteById(movieId);

        return "Movie deleted successfully!";
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        pageNumber = pageNumber - 1;
        if (pageNumber < 0) {
            throw new RuntimeException("Page Number small then 0, Please enter again");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        pageNumber = pageNumber + 1;

        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = new ArrayList<MovieDto>();

        // 2. iterate through the list, generate posterUrl for each movie object and map to MovieDto object and return
        for (Movie movie : movies) {
            String posterUrl = baseURL + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto (
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMoviesCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {

        pageNumber = pageNumber - 1;
        if (pageNumber < 0) {
            throw new RuntimeException("Page Number small then 0, Please enter again");
        }

        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        pageNumber = pageNumber + 1;

        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = new ArrayList<MovieDto>();

        // 2. iterate through the list, generate posterUrl for each movie object and map to MovieDto object and return
        for (Movie movie : movies) {
            String posterUrl = baseURL + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto (
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMoviesCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }

        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }
}
